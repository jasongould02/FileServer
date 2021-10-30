package jgould.fs.java.main.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;

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
	
	
	public static void main(String[] args) {
		try {
			FSWorkspace w = new FSWorkspace();
			w.setWorkspace("workspace/");
			Client c = new Client("127.0.0.1", 80, 5000, w);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Client() {
		socket = new Socket();
		this.start();
	}
	
	public Client(String serverIP, int portNumber, int timeout) throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverIP, portNumber), timeout);
		System.out.println("connection made");
		
		this.start();
	}
	
	/**
	 * Creates a client instance and connects to given server IP at selected port with specified timeout. Sets the workspace to the absolute path given.
	 * 
	 * @param serverIP server IP address
	 * @param portNumber port number to connect to 
	 * @param timeout socket timeout
	 * @param pathToWorkspace absolute path to workspace
	 * @throws IOException
	 */
	public Client(String serverIP, int portNumber, int timeout, String pathToWorkspace) throws IOException, Exception {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverIP, portNumber), timeout);
		System.out.println("connection made");
		
		this.workspace = new FSWorkspace();
		this.workspace.setWorkspace(pathToWorkspace);
		
		this.start();
	}
	
	private Client(String serverIP, int portNumber, int timeout, FSWorkspace workspace) throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverIP, portNumber), timeout);
		System.out.println("connection made");
		
		this.workspace = workspace; 
		
		this.start();
	}

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

	private boolean connectToServer(String ipAddress, String portNumber) {
		if (socket == null) {
			socket = new Socket();
		} else if (socket.isClosed()) {
			socket = null;
			socket = new Socket();
		}

		try {
			socket.connect(new InetSocketAddress(ipAddress, Integer.parseInt(portNumber)));
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			System.out.println("Failed to connect.");
		}

		if (socket.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	protected void sendFileRequest(String pathToFile, String filename, String destination) throws IOException {
		destination = FSUtil.checkDirectoryEnding(destination);
		if(pathToFile.endsWith(filename)) {
			filename = "";
		} 
		pathToFile = FSUtil.checkDirectoryEnding(pathToFile);
		writer.write(FSConstants.REQUEST + FSConstants.DELIMITER + pathToFile + filename + FSConstants.DELIMITER + destination + "\r\n");
		writer.flush();
	}
	
	protected void sendDirectoryListingRequest() throws IOException {
		writer.write(FSConstants.DIRECTORY_LIST_REQUEST + "\r\n");
		writer.flush();
	}
	
	private void parseCommand(String input) throws IOException {
		if(input.startsWith(FSConstants.FILE)) {
			String[] split = input.split(FSConstants.DELIMITER);
			System.out.println("command length:" + split.length);
			if(split.length != 5) {
				System.out.println("Error in command received");
				return;
			}
			
			String destination = split[1];
			String name = split[2];
			String size = split[3]; // Not actually needed since the byte[] data is separated using ':' and included with the string of data sent over the server's worker OutputStream 
			String string_data = split[4];
			
			byte[] data = Base64.getDecoder().decode(string_data);
			workspace.addFile(name, data, destination, StandardOpenOption.CREATE);
		} else if(input.startsWith(FSConstants.FOLDER)) {
			String[] split = input.split(FSConstants.DELIMITER);
			
			String destination = split[1];
			String folderName = split[2];
			
			workspace.addDirectory(folderName, destination);
		} else if(input.startsWith(FSConstants.DIRECTORY_LIST)) {
			ArrayList<String> listing = new ArrayList<String>();
			String[] split = input.split(FSConstants.DELIMITER);
			
			for(int i = 1; i < split.length; i++) {
				listing.add(split[i]);
			}
			
			this.setRemoteFileTree(FSRemoteFileTree.constructRemoteFileTree(listing));
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
			
			// Send server requests here until client GUI is implemented
			//sendFileRequest("server_workspace/image - Copy (2).png", "image - Copy (2).png",  "workspace/test/");
			
			sendFileRequest("server_workspace/testfolder/", "",  "workspace/temp/test/");
						
			while((input = reader.readLine()) != null) { // Not very good for large files (AFAIK >~ 4 MB) 
				//System.out.println("Data received:" + input);
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
	
}
