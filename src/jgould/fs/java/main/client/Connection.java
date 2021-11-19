package jgould.fs.java.main.client;

public class Connection {

	private final String serverName;
	private final String serverIP;
	private final int serverPort;
	private final int serverTimeout;
	
	public Connection(String serverName, String serverIP, int serverPort, int serverTimeout) {
		this.serverName = serverName;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.serverTimeout = serverTimeout;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getServerIP() {
		return serverIP;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public int getServerTimeout() {
		return serverTimeout;
	}
	
}
