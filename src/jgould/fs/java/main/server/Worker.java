package jgould.fs.java.main.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import jgould.fs.java.main.FileServerConstants;

public class Worker implements Runnable {

	private static final int BUFFER_SIZE = 1024;
	private int workerID;
	private Thread thread;
	private boolean running = false;
	
	private Socket socket;
	
	private OutputStreamWriter writer;
	private BufferedReader reader;
	
	protected Worker(Socket socket, int workerID) throws IOException {
		writer = new OutputStreamWriter(socket.getOutputStream(), FileServerConstants.CHARSET);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), FileServerConstants.CHARSET));
		this.socket = socket;
		this.workerID = workerID;
		
		this.start();
	}

	@Override
	public void run() {
		try {
			//byte[] buffer = new byte[BUFFER_SIZE];
			String input = null;
			while((input = reader.readLine()) != null) {
				System.out.println("Data received:" + input);
				
				if(input.trim().equals("echo")) {
					writer.write("echo\n");
					writer.flush();
				}
				
			}
			
			writer.flush();
			writer.close();
			reader.close();
			socket.close();
		} catch(IOException e) {
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
			/*if(socket != null) {
				if(socket.isClosed()) {
					removeQueue.add(socket);
				}
			}*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected Socket getSocket() {
		return socket;
	}
	
}
