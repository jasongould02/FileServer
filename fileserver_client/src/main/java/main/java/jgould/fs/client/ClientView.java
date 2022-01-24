package main.java.jgould.fs.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONException;

import main.java.jgould.fs.client.connection.Connection;
import main.java.jgould.fs.client.connection.ConnectionHistory;
import main.java.jgould.fs.client.connection.ConnectionMenuItem;
import main.java.jgould.fs.client.remote.FSRemoteFileTree;
import main.java.jgould.fs.client.remote.FSRemoteFileTreeListener;
import main.java.jgould.fs.client.remote.FSRemoteFileTreeUtil;
import main.java.jgould.fs.commons.FSConstants;
import main.java.jgould.fs.commons.FSRemoteFile;
import main.java.jgould.fs.commons.FSUtil;
import main.java.jgould.fs.commons.FSWorkspaceListener;
import main.java.jgould.fs.commons.log.Logger;

public class ClientView {

	private JFrame frame;
	private String TITLE = "FileServer - Client";
	private int width = 1280;
	private int height = 720;
	
	private JPanel mainPanel;
	
	// Top menu bar 
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu connectToMenu; // Quick Connect drop down list
	private JMenuItem connectItem;
	private JMenuItem disconnectItem;
	
	// Client File Tree
    private FSRemoteFileTree clientTree = null;
    private FSRemoteFile clientTreeSelection = null;
    private DefaultMutableTreeNode clientTreeSelectionNode = null;
    // Server File Tree
    private FSRemoteFileTree serverTree = null;
    private FSRemoteFile serverTreeSelection = null;
    private DefaultMutableTreeNode serverTreeSelectionNode = null;
    
    private JPanel centerPanel;
    private JButton filePushButton;
    private JButton filePullButton;
    private JButton fileDeleteButton;
    private JButton refreshTreesButton;
    
    private GridBagLayout layout;
    
    private ConnectDialog dialog;
	
	private Client client;
	
	public static void main(String[] args) {
			try {
				Logger.initLogger(1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Client c = new Client();
			ClientView clientView = new ClientView(c);
	}
	
	private ConnectionMenuItem[] createQuickConnect() {
		ConnectionMenuItem[] itemList = new ConnectionMenuItem[ConnectionHistory.getConnectionCount()];
		Connection[] connectionList = ConnectionHistory.getAvailableConnections();
		for(int i = 0; i < ConnectionHistory.getConnectionCount(); i++) {
			itemList[i] = new ConnectionMenuItem(connectionList[i]);
			itemList[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e.getSource() instanceof ConnectionMenuItem) {
						ConnectionMenuItem c = (ConnectionMenuItem) e.getSource();
						try {
							client.sendDisconnectMessage();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						client.connectToServer(c.getConnection().getServerIP(), c.getConnection().getServerPort(), c.getConnection().getServerTimeout());
						if(client.isConnected()) {
							try {
								client.sendDirectoryListingRequest();
								clearTreeSelections();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});
		}
		return itemList;
	}
	
	private void createJMenu(JFrame parent) {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		
		connectItem = new JMenuItem("Connect");
		connectItem.addActionListener(connectActionListener);
		
		disconnectItem = new JMenuItem("Disconnect");
		disconnectItem.addActionListener(disconnectActionListener);
		
		connectToMenu = new JMenu("Connect To");
		for(JMenuItem i : createQuickConnect()) {
			connectToMenu.add(i);
		}
		
		fileMenu.add(connectItem);
		fileMenu.add(connectToMenu);
		fileMenu.add(disconnectItem);
		menuBar.add(fileMenu);
		parent.setJMenuBar(menuBar);
	}
	
	public ClientView(Client client) {
		client.setFSWorkspace("workspace/");
		client.setRemoteWorkspaceListener(remoteFileConflictListener);
		client.getFSWorkspace().setWorkspaceListener(remoteFileConflictListener);
		this.client = client;
		//File f = new File("trash");
		FSConstants.setTrashBin("trash" + File.separator);
		
		try {
			ConnectionHistory.addAllConnections(ConnectionHistory.loadJSONFile("savedConnections.json"));
		} catch (FileNotFoundException | JSONException e1) {
			e1.printStackTrace();
		}
		
		frame = new JFrame(TITLE);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(windowListener);
		
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
		
		serverTree = new FSRemoteFileTree(FSConstants.SERVER_TREE, client.getRemotePathList());
		JScrollPane serverJTreeScrollPane = new JScrollPane(serverTree.getTree());
	    serverJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    serverJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
        clientTree = new FSRemoteFileTree(FSConstants.CLIENT_TREE, FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace()));
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
		
		frame.add(mainPanel);
		mainPanel.setMinimumSize(mainPanel.getSize());
		
		frame.setVisible(true);

		serverTree.getTree().setBorder(new EmptyBorder(0,10,0,10));
		clientTree.getTree().setBorder(new EmptyBorder(0,10,0,10));
		
		clientTree.addMouseListener(clientTreeMouseListener);
		serverTree.addMouseListener(serverTreeMouseListener);
		this.client.setFSRemoteFileTreeListener(fsRemoteFileTreeListener);
		serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
		
		/***
		 * If ActionListeners aren't added before the ComponentPopupMenu is set, then they aren't actually added even though
		 * MouseListeners can be added to Components after the Component has been added to a parent object. 
		 ***/
		clientTree.createPopupMenu();
		serverTree.createPopupMenu();
		
		clientTree.getPullItem().addActionListener(filePullButtonActionListener);
		serverTree.getPullItem().addActionListener(filePullButtonActionListener);
		
		clientTree.getPushItem().addActionListener(filePushButtonActionListener);
		serverTree.getPushItem().addActionListener(filePushButtonActionListener);
		
		clientTree.getRemoveItem().addActionListener(fileDeleteButtonActionListener);
		serverTree.getRemoveItem().addActionListener(fileDeleteButtonActionListener);
		
		clientTree.getRenameItem().addActionListener(fileRenameButtonActionListener);
		serverTree.getRenameItem().addActionListener(fileRenameButtonActionListener);
		
		clientTree.addMenu();
		serverTree.addMenu();
		
		clientTree.getPopupMenu().setEnabled(true);
		serverTree.getPopupMenu().setEnabled(true);
		clientTree.getTree().setComponentPopupMenu(clientTree.getPopupMenu());
		serverTree.getTree().setComponentPopupMenu(serverTree.getPopupMenu());
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
		refreshTreesButton.addActionListener(refreshActionListener);
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
	
		centerPanel.add(filePushButton);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		centerPanel.add(filePullButton);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		centerPanel.add(fileDeleteButton);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		centerPanel.add(refreshTreesButton);
		
		centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		return centerPanel;
	}
	
	private void updateCenterPanelButtons() {
		if(clientTreeSelection != null && serverTreeSelection != null) { // selection on both Server and Client JTrees
			filePushButton.setEnabled(true);
			filePullButton.setEnabled(true);
			fileDeleteButton.setEnabled(false);
		} else {
			if(clientTreeSelection != null && serverTreeSelection == null) { // selection only on Client JTree
				filePushButton.setEnabled(true);
				filePullButton.setEnabled(false);
				fileDeleteButton.setEnabled(true);
			} else if(serverTreeSelection != null && clientTreeSelection == null) { // selection only on Server JTree
				filePushButton.setEnabled(false);
				filePullButton.setEnabled(true);
				fileDeleteButton.setEnabled(true);
			} else { // nothing selected
				filePushButton.setEnabled(false);
				filePullButton.setEnabled(false);
				fileDeleteButton.setEnabled(false);
			}
		}
	}
	
	public Client getClient() {
		return client;
	}
	
	public void clearTreeSelections() {
		this.clientTree.getTree().clearSelection();
		this.serverTree.getTree().clearSelection();
		
		clientTreeSelection = null;
		serverTreeSelection = null;
		
		updateCenterPanelButtons();
	}
	
	public void refreshTrees() {
		if(serverTree != null) {
			serverTree.refreshTreeModel(client.getRemoteFileTree());
		}
		if(clientTree != null) {
			clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
		}
	}
	
	private ActionListener refreshActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				client.sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mainPanel.revalidate();
			serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
			clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
		}
	};
	
	private void showConnectDialog() {
		dialog = new ConnectDialog(this, frame, false);
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
			try {
				client.sendDisconnectMessage();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//client.disconnect();
			serverTree.refreshTreeModel(null);
		}
	};
	
	private ActionListener filePushButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String destination;
				
				if(FSUtil.getExtension(clientTreeSelection.getName()) == null) { // sending a folder
					if(serverTreeSelection == null || serverTreeSelection.getPath().isEmpty()) { // NOTE: File push check if has destination
						destination = client.getRemoteFileTree().getPath() + clientTreeSelection.getName(); // server_workspace_rootfolder + separator + clientTreeSelection name
						System.out.println("file push with no destination:[" + destination + "]");
					} else {
						if(FSUtil.getExtension(serverTreeSelection.getName()) == null) { // sending directory to a folder
							destination = serverTreeSelection.getPath() + File.separator + clientTreeSelection.getName();
							System.out.println("file push " + serverTreeSelection.getName() + "\t to destination:" + destination );
						} else { // sending file to parent folder of a file
							destination = FSUtil.getParent(serverTreeSelection.getPath()) + File.separator + clientTreeSelection.getName();
							System.out.println("file push " + serverTreeSelection.getName() + "\t to destination:" + destination );
						}
					}
					client.sendDirectory(new File(clientTreeSelection.getPath()), destination, destination);
					
				} else { // sending a regular file
					if(serverTreeSelection == null || serverTreeSelection.getPath().isEmpty()) {
						destination = "";
					} else {
						if(FSUtil.getExtension(serverTreeSelection.getName()) == null) { // sending file to a folder
							destination = serverTreeSelection.getPath();
							System.out.println("file push " + serverTreeSelection.getName() + "\t to destination:" + destination );
						} else { // sending file to parent folder of a file
							destination = FSUtil.getParent(serverTreeSelection.getPath());
							System.out.println("file push " + serverTreeSelection.getName() + "\t to destination:" + destination );
						}
					}
					client.sendFile(new File(clientTreeSelection.getPath()), clientTreeSelection.getName(), destination);
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
				if(serverTreeSelection != null) {
					if(clientTreeSelection == null) {
						client.sendFileRequest(serverTreeSelection.getPath(), serverTreeSelection.getName(), FSUtil.checkDirectoryEnding(client.getFSWorkspace().getWorkspace().getPath()));
					} else {
						if(clientTreeSelection.getPath() != null) {
							if(FSUtil.getExtension(clientTreeSelection.getName()) == null) { // sending file to a folder
								client.sendFileRequest(serverTreeSelection.getPath(), serverTreeSelection.getName(), clientTreeSelection.getPath());
							} else {
								client.sendFileRequest(serverTreeSelection.getPath(), serverTreeSelection.getName(), FSUtil.getParent(clientTreeSelection.getPath()));
							}
						} else {
							System.out.println("Error: Sending file to Client's workspace directory."); // This should only occur when the clientJTreeSelection is the root folder.
							client.sendFileRequest(serverTreeSelection.getPath(), serverTreeSelection.getName(), FSUtil.checkDirectoryEnding(client.getFSWorkspace().getWorkspace().getPath())); 
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				mainPanel.revalidate();
				clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
				clearTreeSelections();
			}
		}
	};
	
	private ActionListener fileDeleteButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(clientTreeSelection == null && serverTreeSelection != null) { // Send remove file command to server
					client.sendFileRemove(serverTreeSelection.getPath(), serverTreeSelection.getName());
					client.sendDirectoryListingRequest();
					serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
				} else if(clientTreeSelection != null && serverTreeSelection == null) { // delete local file
					client.getFSWorkspace().deleteFile(clientTreeSelection.getPath(), StandardCopyOption.REPLACE_EXISTING);
					clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
				}
				refreshTrees();
				clearTreeSelections();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	};
	
	private ActionListener fileRenameButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				System.out.println("\nRename source:" + e.getSource() + "\n");
				String newFileName;
				if(clientTreeSelection == null && serverTreeSelection != null) { // Send rename file command to server
					newFileName = JOptionPane.showInputDialog(clientTree.getPopupMenu(), "Enter new name:\n", serverTreeSelection.getName());
					if(serverTreeSelection.getName().equals(newFileName) || newFileName == null) { // keeping same name
						System.out.println("rename server file keeping same name");
						client.sendDirectoryListingRequest();
						refreshTrees();
						clearTreeSelections();
						return;
					}
					client.sendFileRename(serverTreeSelection.getPath(), serverTreeSelection.getName(), newFileName);
					client.sendDirectoryListingRequest();
					serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
				} else if(clientTreeSelection != null && serverTreeSelection == null) { // rename local file
					newFileName = JOptionPane.showInputDialog(serverTree.getPopupMenu(), "Enter new name:\n", clientTreeSelection.getName());
					if(clientTreeSelection.getName().equals(newFileName) || newFileName == null) {
						System.out.println("renaming client file keeping same name");
						refreshTrees();
						clearTreeSelections();
						return;
					}
					client.getFSWorkspace().renameFile(clientTreeSelection.getPath(), clientTreeSelection.getName(), newFileName, StandardCopyOption.REPLACE_EXISTING);
					clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
				}
				refreshTrees();
				clearTreeSelections();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	};
	
	private FSRemoteFileTreeListener fsRemoteFileTreeListener = new FSRemoteFileTreeListener() {
		@Override
		public void workspaceChanged() {
			System.out.println("called");
			refreshTrees();
		}

	};
	
	private MouseAdapter clientTreeMouseListener = new MouseAdapter() {
		@Override
		public void mouseReleased(MouseEvent e) {
			if(clientTree.getTree().getPathForLocation(e.getX(), e.getY()) != null) {
				DefaultMutableTreeNode treeNode = ((DefaultMutableTreeNode) clientTree.getTree().getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
				FSRemoteFile file = ((FSRemoteFile) treeNode.getUserObject());
				clientTreeSelectionNode = treeNode;
				if(file != null && clientTreeSelection != null && SwingUtilities.isLeftMouseButton(e)) {
					if(file.getPath().equals(clientTreeSelection.getPath())) {
						System.out.println("ClientTree MouseListener: A node is already selecting, deselecting node");
						System.out.println("node is already selected, deselecting");
						clientTreeSelection = null;
						clientTree.getTree().clearSelection();
						updateCenterPanelButtons();
						return;
					}
				}
				clientTreeSelection = file;
				updateCenterPanelButtons();
				if(SwingUtilities.isRightMouseButton(e)) {
					clientTree.getTree().clearSelection();
					clientTreeSelection = null;
				}
			} else { 
				System.out.println("invalid client tree selection");
				clearTreeSelections();
				updateCenterPanelButtons();
			}
		}
	};
	private MouseAdapter serverTreeMouseListener = new MouseAdapter() {
		@Override
		public void mouseReleased(MouseEvent e) {
			if(serverTree.getTree().getPathForLocation(e.getX(), e.getY()) != null) {
				FSRemoteFile file = (FSRemoteFile) ((DefaultMutableTreeNode) serverTree.getTree().getPathForLocation(e.getX(), e.getY()).getLastPathComponent()).getUserObject();
				serverTreeSelectionNode = (DefaultMutableTreeNode) serverTree.getTree().getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
				System.out.println("servernode is now:" + ((FSRemoteFile)serverTreeSelectionNode.getUserObject()).getPath() );
				
				if(file != null && serverTreeSelection != null) {
					if(file.getPath().equals(serverTreeSelection.getPath())) {
						serverTreeSelection = null;
						serverTree.getTree().clearSelection();
						updateCenterPanelButtons();
						return;
					}
				}
				serverTreeSelection = file;
				updateCenterPanelButtons();
			} else {
				System.out.println("invalid selection on server tree");
				clearTreeSelections();
				updateCenterPanelButtons();
			}
		}
		
	};
	
	private WindowAdapter windowListener = new WindowAdapter() {
		@Override
	    public void windowClosing(WindowEvent e) {
			try {
				getClient().sendDisconnectMessage();
				ConnectionHistory.saveConnections("savedConnections.json");
			} catch (JSONException | IOException e1) {
				e1.printStackTrace();
			}
	    }
	};
	
	private FSWorkspaceListener remoteFileConflictListener = new FSWorkspaceListener() {

		@Override
		public int promptFolderConflict(boolean localWorkspace, FSRemoteFile originalFolder, String newFolderPath, String newFolderName) {
			String[] conflictOptions = new String[] {"Replace Folder", "Merge Folders", "Change Name", "Cancel"};
			String fileSpace = localWorkspace ? " in a local workspace." : " in a remote workspace.";
			String message = originalFolder.getPath() + " has a name conflict with " + newFolderName + fileSpace;
			int choice = JOptionPane.showOptionDialog(frame, message, "Naming Conflict", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, conflictOptions, conflictOptions[3]);
			return choice;
		}

		@Override
		public int promptFileConflict(boolean localWorkspace, FSRemoteFile originalFile, String newArrivalPath,	String newArrivalName) {
			String[] conflictOptions = new String[] {"Overwrite", "Change Name", "Cancel"}; 
			String fileSpace = localWorkspace ? " in a local workspace." : " in a remote workspace.";
			String message = originalFile.getPath() + " has a name conflict with " + newArrivalName + fileSpace;
			int choice = JOptionPane.showOptionDialog(frame, message, "Naming Conflict", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, conflictOptions, conflictOptions[2]);
			return choice;
		}

		@Override
		public String promptNewName(String filePath, String fileName) {
			String newName;
			if(fileName.contains(".")) { // ensure that there is some kind of extension appened when renaming a file and not a folder
				do {
					newName = JOptionPane.showInputDialog(frame, "Enter new name for " + fileName + ":\n", fileName);
					if(!newName.isEmpty() && !newName.contains(".")) {
						newName = null;
					}
				} while(newName == null);
				return newName;
			}
			newName = JOptionPane.showInputDialog(frame, "Enter new name for " + fileName + ":\n", fileName);
			if(newName.isEmpty()) {
				return null;
			}
			return newName;
		}

		@Override
		public boolean conflictCheck(boolean isOriginalTreeLocal, FSRemoteFile sourceFile) {
			return this.conflictCheck(isOriginalTreeLocal, sourceFile.getPath());
		}
		
		public boolean conflictCheck(boolean isOriginalTreeLocal, String sourcePath) {
			FSRemoteFile oTree = isOriginalTreeLocal ? this.getLocalFileTree() : this.getRemoteFileTree();
			if((oTree.checkForPath(oTree, sourcePath)) != null) {
				return true;
			} else  {
				return false;
			}
		}

		@Override
		public FSRemoteFile getRemoteFileTree() {
			return client.getRemoteFileTree();
		}

		@Override
		public FSRemoteFile getLocalFileTree() {
			return ((FSRemoteFile) ((DefaultMutableTreeNode) clientTree.getTree().getModel().getRoot()).getUserObject());
		}
		
	};
}
