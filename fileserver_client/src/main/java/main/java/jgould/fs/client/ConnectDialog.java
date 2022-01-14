package main.java.jgould.fs.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.java.jgould.fs.client.connection.Connection;
import main.java.jgould.fs.client.connection.ConnectionHistory;

public class ConnectDialog extends JDialog {

	private JPanel mainPanel;
	//private GridLayout layout;
	private GridBagLayout layout;
	private ClientView cv;
	
	private JComboBox<Connection> dropDownMenu;
	
	private JTextField serverNameField;
	private JTextField serverIPField;
	private JTextField serverPortField;
	private JTextField serverTimeoutField;
	
	private JButton connectButton;
	private JButton cancelButton;
	
	private JCheckBox rememberServerCheckBox;
	private boolean rememberServer = false;
	
	private ItemListener dropDownMenuListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			JComboBox<Connection> source = null;
			if(e.getSource() instanceof JComboBox<?>) {
				source = (JComboBox<Connection>) e.getSource();
				setConnection(((Connection) source.getSelectedItem()));
			} else {
				System.out.println("ConnectDialog unable to cast item state change event source");
			}
		}
	};
	
	private JComboBox<Connection> createDropDownMenu(JComboBox<Connection> dropDownMenu) {
		dropDownMenu = new JComboBox<Connection>();
		Connection[] connections = ConnectionHistory.getAvailableConnections();
		
		for(Connection c : connections) { // Since ConnectDialog is instantiated everytime it is shown, the connections are always fetched from ConnectionHistory
			dropDownMenu.addItem(c);
		}
		dropDownMenu.addItemListener(dropDownMenuListener);
		
		return dropDownMenu;
	}
	
	
	public ConnectDialog(ClientView cv, JFrame parent, boolean modal) {
		super(parent, modal);
		this.cv = cv;
		
		//this.setUndecorated(true);
		
		mainPanel = new JPanel();
		//layout = new GridLayout(0, 2, 10, 10);
		layout = new GridBagLayout();
		mainPanel.setLayout(layout);
		
		serverNameField = new JTextField(20);
		serverIPField = new JTextField(20);
		serverPortField = new JTextField(10);
		serverTimeoutField = new JTextField(10);
		
		connectButton = new JButton("Connect");
		cancelButton = new JButton("Cancel");
		
		rememberServerCheckBox = new JCheckBox();
		rememberServerCheckBox.setEnabled(true);
		rememberServerCheckBox.setSelected(false);
		rememberServerCheckBox.setText("Remember Server");
		Connection previousConnection = ConnectionHistory.getConnection(ConnectionHistory.getMostRecentConnectionName());
		if(previousConnection == null) {
			System.out.println("no previous connection found");
			setConnection(null);
		} else {
			setConnection(previousConnection);
			rememberServerCheckBox.setSelected(true);
		}
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		c.fill = GridBagConstraints.NONE;
		if(ConnectionHistory.getConnectionCount() >= 2) { // Only show the dropdown menu if there is more than one option
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			mainPanel.add(this.createDropDownMenu(this.dropDownMenu), c);
		}
		
		c.gridwidth = 1;
		// name field
		mainPanel.add(new JLabel("Server Name:"), setGridBagConstraints(c, 0, 1));
		mainPanel.add(serverNameField, setGridBagConstraints(c, 1, 1));
		// ip row
		mainPanel.add(new JLabel("Server IP:"), setGridBagConstraints(c, 0, 2));
		mainPanel.add(serverIPField, setGridBagConstraints(c, 1, 2));
		// port row
		mainPanel.add(new JLabel("Port Number:"), setGridBagConstraints(c, 0, 3));
		mainPanel.add(serverPortField, setGridBagConstraints(c, 1, 3));
		// timeout row
		mainPanel.add(new JLabel("Timeout:"), setGridBagConstraints(c, 0, 4));
		mainPanel.add(serverTimeoutField, setGridBagConstraints(c, 1, 4));
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 5;
		c.insets = new Insets(0, 5, 0, 5);
		mainPanel.add(connectButton, c);
		
		c.gridx = 1;
		c.gridy = 5;
		mainPanel.add(cancelButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		rememberServerCheckBox.addItemListener(rememberServerListener);
		mainPanel.add(rememberServerCheckBox, c);
		
		connectButton.addActionListener(connectButtonListener);
		cancelButton.addActionListener(cancelButtonListener);
		
		this.add(mainPanel);
		mainPanel.setMinimumSize(new Dimension(275, 275));
		this.setMinimumSize(mainPanel.getMinimumSize());
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	private GridBagConstraints setGridBagConstraints(GridBagConstraints c, int x, int y) {
		c.gridx = x;
		c.gridy = y;
		if(x == 0) {
			c.fill = GridBagConstraints.NONE;
		} else {
			c.fill = GridBagConstraints.HORIZONTAL;
		}
		return c;
	}
	
	public String[] getConnectDialogInput() {
		return new String[] {serverNameField.getText(), serverIPField.getText(), serverPortField.getText(), serverTimeoutField.getText()};
	}
	
	private void setConnection(Connection connection) {
		if(connection == null) {
			clearTextFields();
		} else {
			serverNameField.setText(connection.getServerName());
			serverIPField.setText(connection.getServerIP());
			serverPortField.setText(""+connection.getServerPort());
			serverTimeoutField.setText(""+connection.getServerTimeout());
		}
	}
	
	private void clearTextFields() {
		serverNameField.setText("");
		serverIPField.setText("");
		serverPortField.setText("");
		serverTimeoutField.setText("");
	}
	
	/*private void setConnectButtonListener(ActionListener al) {
		this.connectButton.addActionListener(al);
	}
	
	private void setCancelButtonListener(ActionListener al) {
		this.cancelButton.addActionListener(al);
	}*/
	
	private ActionListener connectButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String serverName = serverNameField.getText();
			String serverIP = serverIPField.getText();
			String serverPort = serverPortField.getText();
			String serverTimeout = serverTimeoutField.getText();
			
			if(serverName.length() == 0 || serverIP.length() == 0 || serverPort.length() == 0 || serverTimeout.length() == 0) { // One of the fields was left empty
				System.out.println("One of the ConnectDialog fields was left empty");
				return;
			}
			
			int portNumber = Integer.parseInt(serverPort);
			int timeout = Integer.parseInt(serverTimeout);
			if(cv.getClient().isConnected()) {
				//cv.getClient().write(FSConstants.END_CONNECTION);
				try {
					cv.getClient().sendDisconnectMessage();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//cv.getClient().disconnect();
			}
			
			System.out.println("attempting connections");
			cv.getClient().connectToServer(serverIP, portNumber, timeout);
			System.out.println("finished attempting");
			try {
				cv.getClient().sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (cv.getClient().isConnected()) { // Exit out
				System.out.println("client connected");
				if(rememberServer == true) {
					ConnectionHistory.addConnection(new Connection(serverName, serverIP, portNumber, timeout));
					ConnectionHistory.setMostRecentConnectionName(serverName);
				}
				setVisible(false);
				cv.refreshTrees();
			} else { // Connection failed, alerts user of failed connection, then still shows the connection dialog
				System.out.println("CLIENT IS NOT CONNECTED");
				System.out.println("name: [" + serverName + "]" + "\tlength: [" + serverName.length() + "]");
				System.out.println("serverip [" + serverIP + "]" + "\tlength: [" + serverIP.length() + "]");
				System.out.println("serverport [" + serverPort + "]" + "\tlength: [" + serverPort.length() + "]");
				System.out.println("servertimeout [" + serverTimeout + "]" + "\tlength: [" + serverTimeout.length() + "]");
				
				JOptionPane.showMessageDialog(mainPanel, "Invalid server information, unable to connect.");
			}
		}
	};
	
	private ActionListener cancelButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			clearTextFields();
			setVisible(false);
		}
	};
	
	private ItemListener rememberServerListener = new ItemListener() {
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
