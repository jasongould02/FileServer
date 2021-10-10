package jgould.fs.java.main.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Server {

	
	private ServerSocket server;
	private int port;
	
	private ArrayList<Worker> workerList = new ArrayList<Worker>();
	private int workerID = 0;
	
	public static void main(String[] args) {
		try {
			Server s = new Server(80);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Server(int port) throws IOException {
		this.port = port;
		server = new ServerSocket(this.port);
		
		launchServer();
	}
	
	private void launchServer() throws IOException {
		while(true) {
			System.out.println("Listenting on PORT:" + port + " for a connection...");
			Socket client = server.accept();
			
			Worker w = new Worker(server.accept(), workerID);
			workerID += 1;
			
			System.out.println("Connection made.");
			System.out.println("Clearing idle workers.");
			
		}
	}
	
	/**
	 * Iterates through all worker sockets looking for a closed socket.
	 * Adds the closed sockets to an ArrayList for removal. (Can't remove from the workerList while iterating over it)
	 */
	private void removeClosedSockets() {
		ArrayList<Worker> removeQueue = new ArrayList<Worker>();
		for(Worker w : workerList) {
			if(w.getSocket().isClosed()) {
				removeQueue.add(w);
			}
		}
		System.out.println("Removing " + removeQueue.size() + " workers.");
		workerList.remove(removeQueue);
		removeQueue.clear();
		removeQueue = null;
	}
	
	
	
	
	
}
