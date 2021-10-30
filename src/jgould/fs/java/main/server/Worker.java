package jgould.fs.java.main.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

import jgould.fs.java.main.client.FSRemoteFileTree;
import jgould.fs.java.main.util.FSConstants;
import jgould.fs.java.main.util.FSUtil;

public class Worker implements Runnable {

	private static final int BUFFER_SIZE = 1024;
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
					sendDirectory(src, destination, destination);
				}
			} 
		} else if(input.startsWith(FSConstants.DIRECTORY_LIST_REQUEST)) {
			ArrayList<String> listing = FSRemoteFileTree.searchDirectory(Server.getFSWorkspace().getWorkspace());
			String string_listing = "";
			for(String s : listing) {
				string_listing += s + ":";
			}
			sendListing(string_listing);
		}
	}
	
	private void sendListing(String listing) throws IOException {
		writer.write(FSConstants.DIRECTORY_LIST + ":" + listing);
		writer.flush();
	}
	
	private void sendDirectory(File file, final String originalDestination, String destination) throws IOException {
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				System.out.println("now sending to new folder:" + destination + ":" + f.getName());
				writer.write(FSConstants.FOLDER + ":" + destination + ":" + FSUtil.checkDirectoryEnding(f.getName()));
				writer.flush();
				sendDirectory(f, originalDestination, destination + f.getName());
			} else {
				sendFile(f, destination);
			}
		}
	}
	
	private void sendFile(File src, String destination) throws IOException {
		byte[] data = FSUtil.getFileBytes(src);
		String string_data = Base64.getEncoder().encodeToString(data);
		writer.write(FSConstants.FILE + ":" + destination + ":" + src.getName() + ":" + data.length + ":" + string_data + "\r\n");
		writer.flush();
	}
	
/*	private void sendDirectory(File directory, String destination, OutputStreamWriter writer) throws IOException { 
		if(directory.isDirectory() && directory.exists()) {
		}
	}
*/	
	
	@Override
	public void run() {
		try {
			String input;
			while((input = reader.readLine()) != null) {
				System.out.println("Data received:" + input);
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
