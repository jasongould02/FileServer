package main.java.jgould.fs.client.connection;

import javax.swing.JMenuItem;

public class ConnectionMenuItem extends JMenuItem {
	
	private final Connection connection;
	
	public ConnectionMenuItem(Connection connection) {
		this.connection = connection;
		this.setText(connection.getServerName());
		this.setEnabled(true);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
}
