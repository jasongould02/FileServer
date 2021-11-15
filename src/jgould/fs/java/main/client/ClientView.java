package jgould.fs.java.main.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import jgould.fs.java.main.util.FSConstants;
import jgould.fs.java.main.util.FSUtil;
import jgould.fs.java.main.util.FSWorkspace;

public class ClientView {

	private JFrame frame;
	private String TITLE = "FileServer - Client";
	private int width = 1280;
	private int height = 720;
	
	private JPanel mainPanel;
	
	private FSWorkspace clientWorkspace;
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem connectItem;
	private JMenuItem disconnectItem;
	
	// Client File Tree
    private FSRemoteFile clientJTreeSelection = null;
    private FSRemoteFileTree clientTree = null;
    // Server File Tree
    private FSRemoteFile serverJTreeSelection = null;
    private FSRemoteFileTree serverTree = null;
    
    private JPanel centerPanel;
    private JButton filePushButton;
    private JButton filePullButton;
    private JButton refreshTreesButton;
    private JButton fileDeleteButton;
    
    private GridBagLayout layout;
    
    private ConnectDialog dialog;
	
	private Client client;
	
	public static void main(String[] args) {
		try {
			Client c = new Client();
			ClientView clientView = new ClientView(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createJMenu(JFrame parent) {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		
		connectItem = new JMenuItem("Connect");
		connectItem.addActionListener(connectActionListener);
		
		disconnectItem = new JMenuItem("Disconnect");
		disconnectItem.addActionListener(disconnectActionListener);
		
		fileMenu.add(connectItem);
		fileMenu.add(disconnectItem);
		menuBar.add(fileMenu);
		parent.setJMenuBar(menuBar);
	}
	
	public ClientView(Client client) {
		client.setFSWorkspace("workspace/");
		this.client = client;
		//File f = new File("trash");
		FSConstants.setTrashBin("trash" + File.separator);
		
		frame = new JFrame(TITLE);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		createJMenu(frame);
		
		showConnectDialog();
		
		mainPanel = new JPanel();
		
		layout = new GridBagLayout();
		mainPanel.setLayout(layout);
		
		try {
			this.client.sendDirectoryListingRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		serverTree = new FSRemoteFileTree(client.getRemotePathList());
		JScrollPane serverJTreeScrollPane = new JScrollPane(serverTree.getTree());
        serverJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
        clientTree = new FSRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace()));
		JScrollPane clientJTreeScrollPane = new JScrollPane(clientTree.getTree());
		clientJTreeScrollPane.setMinimumSize(new Dimension(1000, 100));
        clientJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        clientJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(serverJTreeScrollPane, c);
        
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        mainPanel.add(createCenterPanel(), c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 2;
        c.gridy = 0;
		mainPanel.add(clientJTreeScrollPane, c);
		
		/*c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 0;*/
        /*requestFilesButton = new JButton("Request");
		requestFilesButton.addActionListener(requestActionListener);
		mainPanel.add(requestFilesButton, c);*/
		
		frame.add(mainPanel);
		mainPanel.setMinimumSize(mainPanel.getSize());
		
		frame.setVisible(true);

		for(File temp : getClient().getFSWorkspace().getWorkspace().listFiles()) {
			try {
				System.out.println("Canonical:" + temp.getCanonicalPath() + ":Absolute:"+temp.getAbsolutePath()+":Path:"+temp.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		serverTree.getTree().setBorder(new EmptyBorder(0,10,0,10));
		clientTree.getTree().setBorder(new EmptyBorder(0,10,0,10));
		
		clientTree.addFocusListener(clientJTreeFocusListener);
		serverTree.addFocusListener(serverJTreeFocusListener);
		//clientTree.addTreeSelectionListener(clientJTreeSelectionListener);
		//serverTree.addTreeSelectionListener(serverJTreeSelectionListener);
		clientTree.addMouseListener(clientTreeMouseListener);
		serverTree.addMouseListener(serverTreeMouseListener);
		this.client.addFSRemoteFileTreeListener(fsRemoteFileTreeListener);
		//this.refreshServerTreeModel();
		serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
	}
	
	private JPanel createCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setMinimumSize(new Dimension(200, 200));
		
		filePushButton = new JButton("Upload File");
		filePullButton = new JButton("Download File");
		refreshTreesButton = new JButton("Refresh Tree");
		fileDeleteButton = new JButton("Delete File");
		
		filePushButton.setEnabled(false);
		filePullButton.setEnabled(false);
		fileDeleteButton.setEnabled(false);
		refreshTreesButton.setEnabled(true);
		
		filePushButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		filePullButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		fileDeleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshTreesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		filePushButton.addActionListener(filePushButtonActionListener);
		filePullButton.addActionListener(filePullButtonActionListener);
		fileDeleteButton.addActionListener(fileDeleteButtonActionListener);
		refreshTreesButton.addActionListener(requestActionListener);
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		centerPanel.add(filePushButton);
		centerPanel.add(filePullButton);
		centerPanel.add(fileDeleteButton);
		centerPanel.add(refreshTreesButton);
		
		centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		return centerPanel;
	}
	
	private void updateCenterPanelButtons() {
		if(clientJTreeSelection != null && serverJTreeSelection != null) {
			filePushButton.setEnabled(true);
			filePullButton.setEnabled(true);
			fileDeleteButton.setEnabled(false);
		} else if(clientJTreeSelection != null || serverJTreeSelection != null) {
			filePushButton.setEnabled(false);
			filePullButton.setEnabled(false);
			fileDeleteButton.setEnabled(true);
		} else {
			filePushButton.setEnabled(false);
			filePullButton.setEnabled(false);
			fileDeleteButton.setEnabled(false);
		}
	}
	
	public Client getClient() {
		return client;
	}
	
	private ActionListener requestActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				client.sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mainPanel.revalidate();
			serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
			clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
		}
	};
	
	private void showConnectDialog() {
		dialog = new ConnectDialog(this, frame, true);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
	
	private ActionListener connectActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showConnectDialog();
		}
	};
	
	private ActionListener disconnectActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			client.disconnect();
			serverTree.refreshTreeModel(null);
		}
	};
	
	private ActionListener filePushButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String destination;
				if(FSRemoteFileTreeUtil.getExtension(clientJTreeSelection) == null) { 											// sending a folder
					if(FSRemoteFileTreeUtil.getExtension(serverJTreeSelection) == null) { 										// sending directory to a folder
						destination = serverJTreeSelection.getPath() + File.separator + clientJTreeSelection.getName();
					} else { 																								// sending file to parent folder of a file
						destination = FSUtil.getParent(serverJTreeSelection.getPath()) + File.separator + clientJTreeSelection.getName();
					}
					client.sendDirectory(new File(clientJTreeSelection.getPath()), destination, destination);
				} else { 																									// sending a regular file
					if(FSRemoteFileTreeUtil.getExtension(serverJTreeSelection) == null) { 										// sending file to a folder
						destination = serverJTreeSelection.getPath();
					} else { 																								// sending file to parent folder of a file
						destination = FSUtil.getParent(serverJTreeSelection.getPath());
					}
					client.sendFile(new File(clientJTreeSelection.getPath()), destination);
				}
				
				client.sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
				clearTreeSelections();
			}
		}
	};
	
	private ActionListener filePullButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(serverJTreeSelection != null && clientJTreeSelection != null) {
					if(clientJTreeSelection.getPath() != null) {
						if(FSRemoteFileTreeUtil.getExtension(clientJTreeSelection) == null) { // sending file to a folder
							client.sendFileRequest(serverJTreeSelection.getPath(), serverJTreeSelection.getName(), clientJTreeSelection.getPath());
						} else {
							client.sendFileRequest(serverJTreeSelection.getPath(), serverJTreeSelection.getName(), FSUtil.getParent(clientJTreeSelection.getPath()));
						}
					} else {
						client.sendFileRequest(serverJTreeSelection.getPath(), serverJTreeSelection.getName(), client.getFSWorkspace().getWorkspace().getPath());
					}
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				mainPanel.revalidate();
				clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
				clearTreeSelections();
			}
		}
	};
	
	private ActionListener fileDeleteButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(clientJTreeSelection == null && serverJTreeSelection != null) { // Send remove file command to server
					client.sendFileRemove(serverJTreeSelection.getPath(), serverJTreeSelection.getName());
					client.sendDirectoryListingRequest();
					serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
				} else if(clientJTreeSelection != null && serverJTreeSelection == null) { // delete local file
					client.getFSWorkspace().deleteFile(clientJTreeSelection.getPath(), StandardCopyOption.REPLACE_EXISTING);
					clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
				}
				updateTrees();
				clearTreeSelections();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	};
	
	private FocusListener serverJTreeFocusListener = new FocusListener() {
		@Override
		public void focusGained(FocusEvent e) {
			System.out.println("selected:isTemporary()" +e.isTemporary());
		}
		@Override
		public void focusLost(FocusEvent e) {
			System.out.println("selected:isTemporary()" +e.isTemporary());
		}
	};
	
	private FocusListener clientJTreeFocusListener = new FocusListener() {
		@Override
		public void focusGained(FocusEvent e) {
			if(e.getComponent() != null) {
				System.out.println("e.getComponent == " + e.getComponent().getName()+ e.getComponent().getComponentListeners().length);
			}
			System.out.println("selected:isTemporary()" +e.isTemporary());
		}
		@Override
		public void focusLost(FocusEvent e) {
			System.out.println("selected:isTemporary()" +e.isTemporary());
		}
	};

	public void clearTreeSelections() {
		this.clientTree.getTree().clearSelection();
		this.serverTree.getTree().clearSelection();
		
		clientJTreeSelection = null;
		serverJTreeSelection = null;
		
		updateCenterPanelButtons();
	}
	
	public void updateTrees() {
		//serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
		serverTree.refreshTreeModel(client.getRemoteFileTree());
		clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
	}
	
	FSRemoteFileTreeListener fsRemoteFileTreeListener = new FSRemoteFileTreeListener() {
		@Override
		public void remoteFileTreeChange() {
			System.out.println("called");
			updateTrees();
		}
	};
	
	MouseListener clientTreeMouseListener = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(clientTree.getTree().getPathForLocation(e.getX(), e.getY()) != null) {
				FSRemoteFile file = (FSRemoteFile) ((DefaultMutableTreeNode) clientTree.getTree().getPathForLocation(e.getX(), e.getY()).getLastPathComponent()).getUserObject();
				if(file != null && clientJTreeSelection != null) {
					if(file.getPath().equals(clientJTreeSelection.getPath())) {
						System.out.println("node is already selected, deselecting");
						clearTreeSelections();
						updateCenterPanelButtons();
						return;
					}
				}
				
				if(file != null) {
					clientJTreeSelection = file;
				}
				updateCenterPanelButtons();
			} else { 
				System.out.println("invalid selection");
				clearTreeSelections();
				updateCenterPanelButtons();
			}
		}
	};
	
	MouseListener serverTreeMouseListener = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(clientTree.getTree().getPathForLocation(e.getX(), e.getY()) != null) {
				FSRemoteFile file = (FSRemoteFile) ((DefaultMutableTreeNode) serverTree.getTree().getPathForLocation(e.getX(), e.getY()).getLastPathComponent()).getUserObject();
				if(file != null && serverJTreeSelection != null) {
					if(file.getPath().equals(serverJTreeSelection.getPath())) {
						clearTreeSelections();
						updateCenterPanelButtons();
						return;
					}
				}
				
				if(file != null) {
					serverJTreeSelection = file;
				}
				updateCenterPanelButtons();
			} else {
				System.out.println("invalid selection on server tree");
				clearTreeSelections();
				updateCenterPanelButtons();
			}
		}
		
	};
	
	
}
