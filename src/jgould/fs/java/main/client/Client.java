package jgould.fs.java.main.client;

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

import jgould.fs.java.main.client.remote.FSRemoteFile;
import jgould.fs.java.main.client.remote.FSRemoteFileTreeListener;
import jgould.fs.java.main.client.remote.FSRemoteFileTreeUtil;
import jgould.fs.java.main.util.FSConstants;
import jgould.fs.java.main.util.FSUtil;
import jgould.fs.java.main.util.FSWorkspace;

public class Client implements Runnable {

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
	
	protected void disconnect() {
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
	
	protected void sendFileRename(String sourcePath, String sourceName, String targetName) {
		write(FSConstants.RENAME + FSConstants.DELIMITER + sourcePath + FSConstants.DELIMITER + sourceName + FSConstants.DELIMITER + targetName);
	}
	
	/**
	 * If the Client's writer is not null, this method will write the given string and append '\r\n'.
	 * This method flushes after the String has been written
	 * @param data
	 */
	private void write(String data) {
		if(writer != null && socket.isConnected()) {
			try {
				writer.write(data + "\r\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Writer is null, client cannot send data.");
		}
	}
	
	public void sendDirectoryListingRequest() throws IOException {
		write(FSConstants.DIRECTORY_LIST_REQUEST);
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
			System.out.println("Sending File:" + name + "\tDestination:" + destination);
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
	
	protected void sendDirectory(File file, final String originalDestination, String destination) throws IOException {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(f.getName()));
				sendDirectory(f, originalDestination, destination + File.separator + f.getName());
			} else {
				sendFile(f, destination);
			}
		}
	}
	
	protected void sendFile(File src, String destination) throws IOException {
		byte[] data = FSUtil.getFileBytes(src);
		String string_data = Base64.getEncoder().encodeToString(data);
		write(FSConstants.FILE + ":" + destination + ":" + src.getName() + ":" + data.length + ":" + string_data);
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
						
			while((input = reader.readLine()) != null) { // TODO: Change to batches/buffer 
				if(writer == null && !socket.isClosed()) {
					writer = new OutputStreamWriter(socket.getOutputStream());
				}
				if(reader == null && !socket.isClosed()) {
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				}
				parseCommand(input);
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
