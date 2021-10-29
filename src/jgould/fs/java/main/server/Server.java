package jgould.fs.java.main.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import jgould.fs.java.main.util.FSConstants;
import jgould.fs.java.main.util.FSWorkspace;

public class Server {
	
	private ServerSocket server;
	private int port;
	
	private ArrayList<Worker> workerList = new ArrayList<Worker>();
	private int workerID = 0;
	
	private static FSWorkspace workspace = null;
	
	public static void main(String[] args) {
		try {
			File f = new File("trash");
			f.mkdir();
			FSConstants.setTrashBin("trash\\");
			
			Server s = new Server(80);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Server(int port) throws IOException, Exception {
		this.port = port;
		server = new ServerSocket(this.port);
		
		workspace = new FSWorkspace();
		workspace.setWorkspace("server_workspace\\");
		//workspace.refreshWorkspace();
		
		workspace.printWorkspace();
		
		for(String f : (new File(workspace.getAbsolutePath())).list()) {
			System.out.println(f);
		}
		
		launchServer();
	}
	
	private void launchServer() throws IOException {
		while(true) {
			System.out.println("Listenting on PORT:" + port + " for a connection...");
			//Socket client = server.accept();
			
			Worker w = new Worker(server.accept(), workerID);
			
			workerList.add(w);
			workerID += 1;
			
			System.out.println("Connection made.");
			System.out.println("Clearing idle workers.");
			removeClosedSockets();
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
	
	protected static FSWorkspace getWorkspace() {
		return workspace; 
	}
	
	
	
}
