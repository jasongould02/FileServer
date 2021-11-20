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
		System.out.println("remove these");
		serverIPField.setText("127.0.0.1");
		serverPortField.setText("80");
		serverTimeoutField.setText("5000");
		
		connectButton = new JButton("Connect");
		cancelButton = new JButton("Cancel");
		
		rememberServerCheckBox = new JCheckBox();
		rememberServerCheckBox.setEnabled(true);
		rememberServerCheckBox.setSelected(false);
		rememberServerCheckBox.setText("Remember Server");
		
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
			
			int portNumber = Integer.parseInt(serverPort);
			int timeout = Integer.parseInt(serverTimeout);
			
			boolean connected = cv.getClient().connectToServer(serverIP, portNumber, timeout);
			try {
				cv.getClient().sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if(connected) {
				ConnectionHistory.addConnection(new Connection(serverName, serverIP, portNumber, timeout));
				setVisible(false);
				cv.updateTrees();
			} else {
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
			if(e.getStateChange() == e.SELECTED) {
				rememberServer = true;
			} else {
				rememberServer = false;
			}
			
		}
		
	};

}
