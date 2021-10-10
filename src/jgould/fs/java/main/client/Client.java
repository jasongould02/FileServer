package jgould.fs.java.main.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable {

	private Socket socket;
	private OutputStreamWriter writer;
	private BufferedReader reader;
	
	private Thread thread = null;
	private boolean running = true;
	
	public static void main(String[] args) {
		try {
			Client c = new Client("127.0.0.1", 80, 5000);
		} catch (IOException e) {
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

	private void disconnect() {
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				e.printStackTrace();
				// System.out.println("Failed to disconnect. Error disconnecting.");
			}
		} else {
			// System.out.println("Failed to disconnect. Null socket.");
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

	@Override
	public void run() {
		String input;
		
		try {
			System.out.println("here");
			if(socket != null && !socket.isClosed()) {
				writer = new OutputStreamWriter(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			writer.write("echo\n");
			writer.flush();
			while((input = reader.readLine()) != null && socket.isConnected()) {
				System.out.println("Data received:" + input);
				
				if(input.trim().equals("echo")) {
					writer.write("echo\n");
					writer.flush();
				}
			}
			writer.flush();
			writer.close();
			reader.close();
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
		
	}
	
	/*@Override
	public void run() {
		String input;
		while (socket.isConnected() == false) { // waiting thread until socket is connected? should already be connected
			if (socket.isConnected()) {
				break;
			}
		} // Awaiting connection before allowing the run statement to commence

		try {
			// connect as soon as client starts
			if (!socket.isClosed()) {
				writer = new OutputStreamWriter(socket.getOutputStream());
				reader = new InputStreamReader(socket.getInputStream());
			}
			
			while ((input = reader.readLine()) != null || socket.isClosed()) { // Once this loop ends the entire socket gets closed
				System.out.println("hello");
				System.out.println("Data received:" + input);
				
				writer.write(input.getBytes());
				
			}
			System.out.println("flushign");
			writer.flush();
			writer.close();
			ImageIO.write(image, "png", new File("image.png"));
			
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

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

}
