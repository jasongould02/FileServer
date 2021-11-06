package jgould.fs.java.main.client;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectDialog extends JDialog implements ActionListener {

	private JTextField serverIPField;
	private JTextField serverPortField;
	private JTextField serverTimeoutField;
	
	private JButton connectButton;
	private JButton cancelButton;
	
	private JPanel mainPanel;
	private GridLayout layout;
	
	private String serverIP;
	private String serverPort;
	private String serverTimeout;
	
	private ClientView cv;

	public ConnectDialog(ClientView cv, JFrame parent, boolean modal) {
		super(parent, modal);
		this.cv = cv;
		
		//this.setUndecorated(true);
		
		mainPanel = new JPanel();
		layout = new GridLayout(0, 2, 10, 10);
		mainPanel.setLayout(layout);
		
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
		
		mainPanel.add(new JLabel("Server IP:"));
		mainPanel.add(serverIPField);
		
		mainPanel.add(new JLabel("Port Number:"));
		mainPanel.add(serverPortField);
		
		mainPanel.add(new JLabel("Timeout:"));
		mainPanel.add(serverTimeoutField);
		
		mainPanel.add(connectButton);
		mainPanel.add(cancelButton);
		
		connectButton.addActionListener(connectButtonListener);
		
		this.add(mainPanel);
		mainPanel.setMinimumSize(new Dimension(250, 200));
		this.setMinimumSize(mainPanel.getMinimumSize());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
	}
	
	public String[] getConnectDialogInput() {
		return new String[] {serverIPField.getText(), serverPortField.getText(), serverTimeoutField.getText()};
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
			
			serverIP = serverIPField.getText();
			serverPort = serverPortField.getText();
			serverTimeout = serverTimeoutField.getText();
			
			int portNumber = Integer.parseInt(serverPort);
			int timeout = Integer.parseInt(serverTimeout);
			
			cv.getClient().connectToServer(serverIP, portNumber, timeout);
			try {
				cv.getClient().sendDirectoryListingRequest();
				//cv.refreshServerTreeModel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			setVisible(false);
		}
	};
	

}
