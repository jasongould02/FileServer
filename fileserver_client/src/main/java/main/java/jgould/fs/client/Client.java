package main.java.jgould.fs.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;

import main.java.jgould.fs.client.remote.FSRemoteFileTreeListener;
import main.java.jgould.fs.client.remote.FSRemoteFileTreeUtil;
import main.java.jgould.fs.commons.FSConstants;
import main.java.jgould.fs.commons.FSRemoteFile;
import main.java.jgould.fs.commons.FSUtil;
import main.java.jgould.fs.commons.FSWorkspace;
import main.java.jgould.fs.commons.FSWorkspaceListener;

public class Client implements Runnable {
	private FSWorkspaceListener remoteWorkspaceListener = null;
	
	public void setRemoteWorkspaceListener(FSWorkspaceListener remoteWorkspaceListener) {
		this.remoteWorkspaceListener = remoteWorkspaceListener;
	}
	
	public FSWorkspaceListener getConflictListener() {
		return this.remoteWorkspaceListener;
	}
	
	private Socket socket;
	private OutputStreamWriter writer;
	private BufferedReader reader;
	
	private FSWorkspace workspace = null;
	private FSRemoteFile remoteFileTree = null;
	private ArrayList<String> remotePathList = new ArrayList<String>();
	
	private Thread thread = null;
	private boolean running = true;
	
	private FSRemoteFileTreeListener fsRemoteFileTreeListener = null;
		
	public Client() {}

	private void disconnect() {
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			socket = null;
		}
	}

	protected synchronized boolean connectToServer(String ipAddress, int portNumber, int timeout) {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ipAddress, portNumber), timeout);
			writer = new OutputStreamWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			if(this.running == false || this.thread == null) {
				this.start();
			}
			return true;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			System.out.println("Failed to connect.");
			return false;
		}
	}
	
	// This method solves the problem of unable to perform actions on a server after reconnecting
	// (There was a bug where reconnected to a server would not allow the client to receive any information from the server, 
	// even though the server itself could send and receive data, the client would never be able to process it)
	// use this method instead of Client#disconnect();
	protected void sendDisconnectMessage() throws IOException {
		write(FSConstants.END_CONNECTION);
	}
	
	protected void sendFileRequest(String pathToFile, String filename, String destination) throws IOException {
		destination = FSUtil.checkDirectoryEnding(destination);
		if(pathToFile.endsWith(filename)) {
			filename = "";
		} 
		pathToFile = FSUtil.checkDirectoryEnding(pathToFile);
		write(FSConstants.REQUEST + FSConstants.DELIMITER + pathToFile + filename + FSConstants.DELIMITER + destination);
	}
	
	protected void sendFileRemove(String pathToFile, String filename) throws IOException {
		if(pathToFile.endsWith(filename)) {
			filename = "";
		}
		pathToFile = FSUtil.checkDirectoryEnding(pathToFile);
		write(FSConstants.REMOVE + FSConstants.DELIMITER + pathToFile + filename);
	}
	
	protected void sendFileRename(String sourcePath, String sourceName, String targetName) throws IOException {
		write(FSConstants.RENAME + FSConstants.DELIMITER + sourcePath + FSConstants.DELIMITER + sourceName + FSConstants.DELIMITER + targetName);
	}
	
	public void sendDirectoryListingRequest() throws IOException {
		write(FSConstants.DIRECTORY_LIST_REQUEST);
	}
	
	// destination - serverTreeSelection.getPath + SEPARATOR + sourceFile.getName (for folder sent to folder)
	// destination - serverTreeSelection.getParent.getPath + SEPARATOR + sourceFile.getName (folder sent to file (goes into the files parent folder)
	// destination - sourceFile.getName (folder sent to root folder (sever_workspace)
	// destination -
	protected void sendDirectory(File file, final String originalDestination, String destination) throws IOException {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(f.getName()));
				sendDirectory(f, originalDestination, destination + File.separator + f.getName());
			} else {
				sendFile(f, f.getName(), destination);
			}
		}
	}
	
	/**
	 * Do not include the src file name in the destination, destination needs to be the parent folder the src file object will be placed in
	 * */
	protected void sendFile(File src, String nameAtDestination, String destination) throws IOException {
		byte[] data = FSUtil.getFileBytes(src);
		if(data == null) {
			System.out.println("Tried to send a empty file. [" + src.getPath() + ";" + src.getName() + "]");
			return;
		}
		String string_data = Base64.getEncoder().encodeToString(data);
		//write(FSConstants.FILE + ":" + destination + ":" + src.getName() + ":" + data.length + ":" + string_data);
		write(FSConstants.FILE + ":" + destination + ":" + nameAtDestination + ":" + data.length + ":" + string_data);
	}
	
	private void parseCommand(String input) throws IOException {
		System.out.println("received command:" + input);
		boolean workspaceChange = true;
		if(input.startsWith(FSConstants.FILE)) {
			String[] split = input.split(FSConstants.DELIMITER);
			if(split.length != 5) {
				System.out.println("Error in command received");
				return;
			}
			
			String destination = split[1];
			String name = split[2];
			//String size = split[3]; // Not actually needed since the byte[] data is separated using ':' and included with the string of data sent over the server's worker OutputStream 
			String string_data = split[4];
			
			byte[] data = Base64.getDecoder().decode(string_data);
			System.out.println("Receiving File:" + name + "\tDestination:" + destination);
			//System.out.println("writing file:" + name + "\tdest:" + destination);
			workspace.addFile(name, data, destination, StandardOpenOption.CREATE);
		} else if(input.startsWith(FSConstants.FOLDER)) {
			String[] split = input.split(FSConstants.DELIMITER);
			String destination = split[1];
			String folderName = split[2];
			
			workspace.addDirectory(folderName, destination);
		} else if(input.startsWith(FSConstants.DIRECTORY_LIST)) {
			//System.out.println("received listing:");
			ArrayList<String> listing = new ArrayList<String>();
			String[] split = input.split(FSConstants.DELIMITER);
			
			for(int i = 1; i < split.length; i++) {
				listing.add(split[i]);
				//System.out.println("listing:"+split[i]);
			}
			this.remotePathList.clear();
			remotePathList.addAll(listing);
			this.setRemoteFileTree(FSRemoteFileTreeUtil.constructRemoteFileTree(listing));
		} else {
			System.out.println("Received non tree changing command");
			workspaceChange = false;
		}
		
		// Allows the client itself to immediately refresh the FSRemoteFileTrees after receiving changes
		if(fsRemoteFileTreeListener != null && workspaceChange == true) {
			fsRemoteFileTreeListener.workspaceChanged();
		}
	}
	
	/**
	 * If the Client's writer is not null, this method will write the given string and append '\r\n'.
	 * This method flushes after the String has been written
	 * @param data
	 */
	protected void write(String data) throws IOException {
		if(data.contains(FSConstants.DELIMITER)) {
			FSRemoteFile remoteTree = getConflictListener().getRemoteFileTree();
			
			final String message_type = data.substring(0, data.indexOf(FSConstants.DELIMITER));
			System.out.println("message_type:" + message_type);
			// TODO: move most of this logic back to ClientView.java
			if(message_type != null) {				
				System.out.println("intercepted:" + data);
				System.out.println("doest type equal FILE" + message_type.equals(FSConstants.FILE));
				String split[] = data.split(FSConstants.DELIMITER);
				if(message_type.equals(FSConstants.FILE)) { // sending a file 
					//write(FSConstants.FILE + ":" + destination + ":" + nameAtDestination + ":" + data.length + ":" + string_data);
					String destination = FSUtil.checkDirectoryEnding(split[1]);
					String name = split[2];
					String size = split[3]; // Not actually needed since the byte[] data is separated using ':' and included with the string of data sent over the server's worker OutputStream 
					String string_data = split[4];
					String finalPath = destination + name;
					
					if(remoteTree.getChild(name) != null) {
						System.out.println("overwriting a root file");
					}
					
					boolean conflict = getConflictListener().conflictCheck(false, finalPath);
					FSRemoteFile destinationFile = remoteTree.checkForPath(remoteTree, finalPath);
					System.out.println("RemoteTree.Name:"+remoteTree.getName() + "\t path:" + remoteTree.getPath());
					System.out.println("conflict" + conflict + "\tdestinationFile" + destinationFile);
					System.out.println("checkForPath:" + finalPath);
					if(conflict && destinationFile != null) { // theres a remoteFile already at the destination
						int choice = getConflictListener().promptFileConflict(false, destinationFile, finalPath, name);
						System.out.println("user chose" + choice);
						switch(choice) {
							case 0: // overwrite
								this.sendFileRemove(finalPath, name);
								conflict_write(FSConstants.FILE + ":" + destination + ":" + name + ":" + size + ":" + string_data);
								break;
							case 1:  // rechange the name
								String newName;
								boolean newNameConflict = true;
								
								do {
									newName = getConflictListener().promptNewName(finalPath, name);
									if(newName == null || newName.equals(name)) {
										System.out.println("file renamed to itself_rename canceled");
										return;
									}
									newNameConflict = getConflictListener().conflictCheck(false, destination + newName);
									System.out.println("newNameConflict is now" + newNameConflict);
									if(!newNameConflict) {
										System.out.println("sending new file name");
										conflict_write(FSConstants.FILE + ":" + destination + ":" + newName + ":" + size + ":" + string_data);
										return;
									}
								} while(newNameConflict == true);
								break;
							case 2: // cancel and dont move that file;
								System.out.println("send file cancelled");
								return;
						}
					}
					
					
				} else if(message_type.equals(FSConstants.FOLDER)) {
					//write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(f.getName()));
					String destination = split[1];
					String folderName = split[2];
					String finalPath = FSUtil.checkDirectoryEnding(destination) + folderName + File.separator;
					boolean conflict = getConflictListener().conflictCheck(false, finalPath);
					FSRemoteFile destinationFile = remoteTree.checkForPath(remoteTree, finalPath);
					
					if(conflict && destinationFile != null) { // theres a remoteFolder already at the destination
						System.out.println("Folder already exists in the remoteworkspace");
						return;
					}
					
				} else if(message_type.equals(FSConstants.RENAME)) {
					//write(FSConstants.RENAME + FSConstants.DELIMITER + sourcePath + FSConstants.DELIMITER + sourceName + FSConstants.DELIMITER + targetName);
					String sourcePath = split[1];
					String sourceName = split[2];
					String targetName = split[3];
					String newPath = FSUtil.getParent(sourcePath) + targetName;
					boolean conflict = getConflictListener().conflictCheck(false, newPath);
					FSRemoteFile destinationFile = remoteTree.checkForPath(remoteTree, newPath);
					FSRemoteFile sourceFile = remoteTree.checkForPath(remoteTree, sourcePath);
					if(sourceFile == null) {
						System.out.println("cannot find the source file that is being renamed");
					}
					
					// TODO: Allow for merging of folders or overwrite
					if(conflict && destinationFile != null) { // a file already exists with the desired name
						
						String newName;
						boolean newNameConflict = true;
						
						do {
							newName = getConflictListener().promptNewName(sourceFile.getPath(), sourceFile.getName());
							if(newName == null || newName.equals(targetName)) {
								System.out.println("file renamed to itself_rename canceled");
								return;
							}
							newNameConflict = getConflictListener().conflictCheck(false, FSUtil.getParent(sourcePath) + newName);
							System.out.println("newNameConflict is now" + newNameConflict);
							if(!newNameConflict) {
								System.out.println("sending new file name");
								conflict_write(FSConstants.RENAME + FSConstants.DELIMITER + sourcePath + FSConstants.DELIMITER + sourceName + FSConstants.DELIMITER + newName);
								return;
							}
						} while(newNameConflict == true);
					}
				}
			}
		}
		
		if(writer != null && socket.isConnected()) {
				writer.write(data + "\r\n");
				writer.flush();
		} else {
			System.out.println("Writer is null, client cannot send data.");
		}
	}
	
	/**
	 * If the Client's writer is not null, this method will write the given string and append '\r\n'.
	 * This method flushes after the String has been written
	 * 
	 * Only call this method from inside {@link Client#write(String)} once the file conflict has been addressed/fixed since this method does not check for further conflicts
	 * @param data
	 */
	private void conflict_write(String data) throws IOException {
		if(writer != null && socket.isConnected()) {
				writer.write(data + "\r\n");
				writer.flush();
		} else {
			System.out.println("Writer is null, client cannot send data.");
		}
	}
	
	@Override
	public void run() {
		String input;
		try {
			if(socket != null && !socket.isClosed()) {
				writer = new OutputStreamWriter(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			
			this.sendDirectoryListingRequest();
			
			while(running) { // As long as the client is running keep checking if the socket is connected or not closed
				if(socket != null && !socket.isClosed() && socket.isConnected()) {
					// start
					while((input = reader.readLine()) != null) { // TODO: Change to batches/buffer 
						if(writer == null && !socket.isClosed()) {
							System.out.println("Reconnected writer");
							writer = new OutputStreamWriter(socket.getOutputStream());
						}
						if(reader == null && !socket.isClosed()) {
							System.out.println("odd reader reconnect");
							reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						}
						System.out.println("Input:" + input);
						if(input.startsWith(FSConstants.END_CONNECTION)) {
							this.disconnect();
							break;
						}
						parseCommand(input);
					}
				}
			}
			writer.flush();
			writer.close();
			reader.close();
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setRemoteFileTree(FSRemoteFile root) {
		this.remoteFileTree = root;
	}
	
	protected FSRemoteFile getRemoteFileTree() {
		return this.remoteFileTree;
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Client");
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		if (socket == null || socket.isClosed()) {
			return false;
		} else {
			return socket.isConnected();
		}
	}

	public String getServerIP() {
		return socket.getInetAddress().getHostAddress();
	}
	
	public FSWorkspace getFSWorkspace() {
		return workspace;
	}
	public void setFSWorkspace(FSWorkspace w) {
		this.workspace = w;
	}
	
	public void setFSWorkspace(String pathToWorkspace) {
		try {
			File f = new File(pathToWorkspace);
			if(!f.exists()) {
				f.mkdirs();
			}
			this.workspace = new FSWorkspace(pathToWorkspace);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getRemotePathList() {
		return remotePathList;
	}
	
	public void setFSRemoteFileTreeListener(FSRemoteFileTreeListener ls) {
		this.fsRemoteFileTreeListener = ls;
	}
}
