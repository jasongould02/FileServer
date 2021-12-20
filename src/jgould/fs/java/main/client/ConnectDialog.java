package jgould.fs.java.main.client;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectDialog extends JDialog implements ActionListener {

	private JTextField serverNameField;
	private JTextField serverIPField;
	private JTextField serverPortField;
	private JTextField serverTimeoutField;
	
	private JButton connectButton;
	private JButton cancelButton;
	
	private JPanel mainPanel;
	private GridLayout layout;
	
	private String serverName;
	private String serverIP;
	private String serverPort;
	private String serverTimeout;
	
	private JCheckBox rememberServerCheckBox;
	private boolean rememberServer = false;
	
	private ClientView cv;

	public ConnectDialog(ClientView cv, JFrame parent, boolean modal) {
		super(parent, modal);
		this.cv = cv;
		
		//this.setUndecorated(true);
		
		mainPanel = new JPanel();
		layout = new GridLayout(0, 2, 10, 10);
		mainPanel.setLayout(layout);
		
		serverNameField = new JTextField(20);
		serverIPField = new JTextField(20);
		serverPortField = new JTextField(10);
		serverTimeoutField = new JTextField(10);
		
		// FOR TESTING
		/*System.out.println("remove these");
		serverIPField.setText("127.0.0.1");
		serverPortField.setText("80");
		serverTimeoutField.setText("5000");*/
		
		connectButton = new JButton("Connect");
		cancelButton = new JButton("Cancel");
		
		rememberServerCheckBox = new JCheckBox();
		rememberServerCheckBox.setEnabled(true);
		rememberServerCheckBox.setSelected(false);
		rememberServerCheckBox.setText("Remember Server");
		Connection previousConnection = ConnectionHistory.getConnection(ConnectionHistory.getMostRecentConnectionName());
		if(previousConnection == null) {
			System.out.println("no previous connection found");
		} else {
			addConnectionToFields(previousConnection);
			rememberServerCheckBox.setSelected(true);
		}
		
		mainPanel.add(new JLabel("Server Name:"));
		mainPanel.add(serverNameField);
		
		mainPanel.add(new JLabel("Server IP:"));
		mainPanel.add(serverIPField);
		
		mainPanel.add(new JLabel("Port Number:"));
		mainPanel.add(serverPortField);
		
		mainPanel.add(new JLabel("Timeout:"));
		mainPanel.add(serverTimeoutField);
		
		mainPanel.add(connectButton);
		mainPanel.add(cancelButton);
		
		mainPanel.add(rememberServerCheckBox);
		rememberServerCheckBox.addItemListener(rememberServerListener);
		
		connectButton.addActionListener(connectButtonListener);
		
		this.add(mainPanel);
		mainPanel.setMinimumSize(new Dimension(250, 250));
		this.setMinimumSize(mainPanel.getMinimumSize());
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
	}
	
	public String[] getConnectDialogInput() {
		return new String[] {serverNameField.getText(), serverIPField.getText(), serverPortField.getText(), serverTimeoutField.getText()};
	}
	
	/*private Connection getMostRecentConnection() {
		return ConnectionHistory.getConnection(ConnectionHistory.getMostRecentConnectionName());
	}*/
	
	private void addConnectionToFields(Connection connection) {
		if(connection == null) {
			clearTextFields();
		} else {
			serverNameField.setText(connection.getServerName());
			serverIPField.setText(connection.getServerIP());
			serverPortField.setText(""+connection.getServerPort());
			serverTimeoutField.setText(""+connection.getServerTimeout());
		}
	}
	
	public void clearTextFields() {
		serverNameField.setText("");
		serverIPField.setText("");
		serverPortField.setText("");
		serverTimeoutField.setText("");
	}
	
	public void setConnectButtonListener(ActionListener al) {
		this.connectButton.addActionListener(al);
	}
	
	public void setCancelButtonListener(ActionListener al) {
		this.cancelButton.addActionListener(al);
	}
	
	ActionListener connectButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			serverName = serverNameField.getText();
			serverIP = serverIPField.getText();
			serverPort = serverPortField.getText();
			serverTimeout = serverTimeoutField.getText();
			
			if(serverName.length() == 0 || serverIP.length() == 0 || serverPort.length() == 0 || serverTimeout.length() == 0) {
				// No input in at least one of the fields
				/*System.out.println("name Length:" + serverName.length());
				System.out.println("serverip length:" + serverIP.length());
				System.out.println("serverport length:" + serverPort.length());
				System.out.println("servertimeout length:" + serverTimeout.length());*/
				return;
			}
			
			int portNumber = Integer.parseInt(serverPort);
			int timeout = Integer.parseInt(serverTimeout);
			System.out.println("attempting connections");
			cv.getClient().connectToServer(serverIP, portNumber, timeout);
			System.out.println("finished attempting");
			try {
				cv.getClient().sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (cv.getClient().isConnected()) {
				System.out.println("client connected");
				if(rememberServer == true) {
					ConnectionHistory.addConnection(new Connection(serverName, serverIP, portNumber, timeout));
					ConnectionHistory.setMostRecentConnectionName(serverName);
				}
				setVisible(false);
				cv.refreshTrees();
			} else {
				System.out.println("CLIENT IS NOT CONNECTED");
				System.out.println("name: [" + serverName + "]" + "\tlength: [" + serverName.length() + "]");
				System.out.println("serverip [" + serverIP + "]" + "\tlength: [" + serverIP.length() + "]");
				System.out.println("serverport [" + serverPort + "]" + "\tlength: [" + serverPort.length() + "]");
				System.out.println("servertimeout [" + serverTimeout + "]" + "\tlength: [" + serverTimeout.length() + "]");
				
				JOptionPane.showMessageDialog(mainPanel, "Invalid server information, unable to connect.");
			}
			
		}
	};
	
	ActionListener cancelButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			setVisible(false);
			
		}
	};
	
	ItemListener rememberServerListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				rememberServerCheckBox.setSelected(true);
				rememberServer = true;
			} else {
				rememberServerCheckBox.setSelected(false);
				rememberServer = false;
			}
			
		}
		
	};

}
