package main.java.jgould.fs.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;

import main.java.jgould.fs.commons.FSConstants;
import main.java.jgould.fs.commons.FSUtil;

public class Worker implements Runnable {

	//private static final int BUFFER_SIZE = 1024;
	private int workerID;
	private Thread thread;
	private boolean running = false;
	
	private Socket socket;
	
	private OutputStreamWriter writer;
	private BufferedReader reader;
	
	protected Worker(Socket socket, int workerID) throws IOException {
		writer = new OutputStreamWriter(socket.getOutputStream(), FSConstants.CHARSET);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), FSConstants.CHARSET));
		this.socket = socket;
		this.workerID = workerID;
		
		this.start();
	}
	
	private void parseCommand(String input) throws IOException {
		if(input.startsWith(FSConstants.REQUEST)) {
			String[] split = input.split(FSConstants.DELIMITER);
			String command = split[0];
			String source = split[1];
			String destination = split[2];
			File src = new File(source);
			if(src.exists()) {
				if(src.isFile()) {
					sendFile(src, destination);
				} else if(src.isDirectory()) {
					write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(src.getName()));
					String dest = destination + File.separator + src.getName(); 
					System.out.println("root destination:["+dest+ "]");
					sendDirectory(src, dest, dest);
				}
			} 
		} else if(input.startsWith(FSConstants.DIRECTORY_LIST_REQUEST)) {
			ArrayList<String> listing = FSUtil.searchDirectory(Server.getFSWorkspace().getWorkspace());
			String string_listing = "";
			for(String s : listing) {
				string_listing += s + ":";
			}
			sendListing(string_listing);
		} else if(input.startsWith(FSConstants.FILE)) {
			String[] split = input.split(FSConstants.DELIMITER);
			if(split.length != 5) {
				System.out.println("Error in command received");
				return;
			}
			
			String destination = split[1];
			String name = split[2];
			String size = split[3]; // Not actually needed since the byte[] data is separated using ':' and included with the string of data sent over the server's worker OutputStream 
			String string_data = split[4];
			
			byte[] data = Base64.getDecoder().decode(string_data);
			if(destination.isEmpty()) {
				destination = Server.getFSWorkspace().getWorkspace().getPath();
			}
			if(!destination.startsWith(Server.getFSWorkspace().getWorkspace().getPath())) {
				destination = Server.getFSWorkspace().getWorkspace().getPath() + File.separator + destination;
			}
			Server.getFSWorkspace().addFile(name, data, destination, StandardOpenOption.CREATE);
		} else if(input.startsWith(FSConstants.FOLDER)) {
			String[] split = input.split(FSConstants.DELIMITER);
			System.out.println("adding dir");
			
			String destination = split[1];
			String folderName = split[2];
			
			if(!destination.startsWith(Server.getFSWorkspace().getWorkspace().getPath())) {
				destination = Server.getFSWorkspace().getWorkspace().getPath() + File.separator + destination;
			}
			Server.getFSWorkspace().addDirectory(folderName, destination);
		} else if(input.startsWith(FSConstants.REMOVE)) {
			String[] split = input.split(FSConstants.DELIMITER);
			String pathToFile = split[1];
			try {
				Server.getFSWorkspace().deleteFile(pathToFile, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("deleting file" + pathToFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(input.startsWith(FSConstants.RENAME)) {
			String[] split = input.split(FSConstants.DELIMITER);
			String sourcePath = split[1];
			String sourceName = split[2];
			String targetName = split[3];
			try {
				Server.getFSWorkspace().renameFile(sourcePath, sourceName, targetName, StandardCopyOption.REPLACE_EXISTING);
			} catch(Exception e) {
				System.out.println("Failed to rename file");
				e.printStackTrace();
			}
		}
	}
	
	private void sendListing(String listing) throws IOException {
		write(FSConstants.DIRECTORY_LIST + ":" + listing);
	}
	
	private void sendDirectory(File file, final String originalDestination, String destination) throws IOException {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(f.getName()));
				sendDirectory(f, originalDestination, destination + File.separator + f.getName());
			} else {
				sendFile(f, destination);
			}
		}
	}
	
	private void sendFile(File src, String destination) throws IOException {
		byte[] data = FSUtil.getFileBytes(src);
		String string_data = Base64.getEncoder().encodeToString(data);
		write(FSConstants.FILE + ":" + destination + ":" + src.getName() + ":" + data.length + ":" + string_data);
	}
	
	@Override
	public void run() {
		try {
			String input;
			while((input = reader.readLine()) != null) {
				System.out.println("[" + workerID + "] Data received:" + input);
				if(input.startsWith(FSConstants.END_CONNECTION)) {
					socket.close();
					continue;
				}
				parseCommand(input);
			}
			writer.flush();
			writer.close();
			reader.close();
			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * If the Worker's writer is not null, this method will write the given string and append '\r\n'.
	 * This method flushes after the String has been written
	 * @param data
	 */
	private void write(String data) {
		if(writer != null) {
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
	
	private String getAddress() {
		return socket.getRemoteSocketAddress().toString().replaceAll("/", " ").trim();
	}
	
	protected synchronized void start() {
		running = true;
		thread = new Thread(this, "Worker" + workerID + "/" + getAddress());
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}
	
	protected synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected Socket getSocket() {
		return socket;
	}
	
}
