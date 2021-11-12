package jgould.fs.java.main.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
	
	private JButton refreshTreesButton;
	
	// Client File Tree
	//private DefaultMutableTreeNode clientJTreeRoot = null;
    //private DefaultTreeModel clientJTreeModel = null;
    //private JTree clientJTree = null;
    private FSRemoteFile clientJTreeSelection = null;
    private FSRemoteFileTree clientTree = null;
    // Server File Tree
    //private DefaultMutableTreeNode serverJTreeRoot = null;
    //private DefaultTreeModel serverJTreeModel = null;
    //private JTree serverJTree = null;
    private FSRemoteFile serverJTreeSelection = null;
    private FSRemoteFileTree serverTree = null;
    
    private JPanel centerPanel;
    private JButton filePushButton;
    private JButton filePullButton;
   // private JButton fileDeleteButton;
    
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
		
		fileMenu.add(connectItem);
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
		
		dialog = new ConnectDialog(this, frame, true);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
		
		mainPanel = new JPanel();
		
		layout = new GridBagLayout();
		mainPanel.setLayout(layout);
		
		
		
		try {
			this.client.sendDirectoryListingRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//serverJTree = this.createJTree(serverJTree, this.client.getRemotePathList(), serverJTreeRoot, serverJTreeModel);
		serverTree = new FSRemoteFileTree(client.getRemotePathList());
		JScrollPane serverJTreeScrollPane = new JScrollPane(serverTree.getTree());
        serverJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        //serverJTreeScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		//clientJTree = this.createJTree(clientJTree, FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace()), clientJTreeRoot, clientJTreeModel);
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
		
		//serverJTree.setName("serverJTree");
		//clientJTree.setName("clientJTree");
		//serverJTree.addFocusListener(serverJTreeFocusListener);
		//clientJTree.addFocusListener(clientJTreeFocusListener);
		clientTree.addFocusListener(clientJTreeFocusListener);
		serverTree.addFocusListener(serverJTreeFocusListener);
		
		//serverJTree.setBorder(new EmptyBorder(0,10,0,10));
		//clientJTree.setBorder(new EmptyBorder(0,10,0,10));
		clientTree.getTree().setBorder(new EmptyBorder(0,10,0,10));
		
		//clientJTree.addTreeSelectionListener(clientJTreeSelectionListener);
		//serverJTree.addTreeSelectionListener(serverJTreeSelectionListener);
		clientTree.addTreeSelectionListener(clientJTreeSelectionListener);
		serverTree.addTreeSelectionListener(serverJTreeSelectionListener);
		
		//this.refreshServerTreeModel();
		serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
		this.client.addFSRemoteFileTreeListener(fsRemoteFileTreeListener);
	}
	
	private JPanel createCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setMinimumSize(new Dimension(200, 200));
		
		filePushButton = new JButton("Upload File");
		filePullButton = new JButton("Download File");
		refreshTreesButton = new JButton("Refresh Tree");
		//fileDeleteButton = new JButton("Delete File");
		
		filePushButton.setEnabled(false);
		filePullButton.setEnabled(false);
		refreshTreesButton.setEnabled(true);
		//fileDeleteButton.setEnabled(false);
		
		filePushButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		filePullButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshTreesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		//fileDeleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		filePushButton.addActionListener(filePushButtonActionListener);
		filePullButton.addActionListener(filePullButtonActionListener);
		refreshTreesButton.addActionListener(requestActionListener);
		//fileDeleteButton.addActionListener(fileDeleteButtonActionListener);
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		centerPanel.add(filePushButton);
		centerPanel.add(filePullButton);
		centerPanel.add(refreshTreesButton);
		//centerPanel.add(fileDeleteButton);
		
		centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		return centerPanel;
	}
	
	private void updateCenterPanelButtons() {
		if(clientJTreeSelection != null && serverJTreeSelection != null) {
			filePushButton.setEnabled(true);
			filePullButton.setEnabled(true);
			//fileDeleteButton.setEnabled(true);
		} else {
			filePushButton.setEnabled(false);
			filePullButton.setEnabled(false);
			//fileDeleteButton.setEnabled(false);
		}
	}
	
	public Client getClient() {
		return client;
	}
	
	@Deprecated //TODO: Remove before FSRemoteFileTree refactor (or is it a decouple?)
	private DefaultMutableTreeNode generateTreeNode(FSRemoteFile rootFile, DefaultMutableTreeNode parent) {
		if(rootFile != null && parent != null) {
			for(FSRemoteFile file : rootFile.getChildren()) {
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
				parent.add(child);
				if(file.getChildren().size() != 0) {
					generateTreeNode(file, child);
				}
			}
		} else {
			if(parent == null) { // for creating first node
				DefaultMutableTreeNode newParent = new DefaultMutableTreeNode(rootFile);
				return generateTreeNode(rootFile, newParent);
			}
		}
		return parent;
	}
	
	private FSRemoteFile serverWorkspaceRootFile;
	@Deprecated // TODO: Remove before FSRemoteFileTree refactor (or is it a decouple?)
	private JTree createJTree(JTree tree, ArrayList<String> pathList, DefaultMutableTreeNode root, DefaultTreeModel treeModel) {
		if(pathList != null) {
			serverWorkspaceRootFile = FSRemoteFileTreeUtil.constructRemoteFileTree(pathList);
			root = generateTreeNode(serverWorkspaceRootFile, null);
			treeModel = new DefaultTreeModel(root);
			
			tree = new JTree(treeModel);
			tree.setShowsRootHandles(true);
			return tree;
		} else {
			if(tree == null) {
				tree = new JTree();
				tree.setShowsRootHandles(true);
			}
			return tree;
		}
	}
	
	/*protected void setServerTreeModel(DefaultTreeModel treeModel) {
		this.serverJTree.setModel(treeModel);
		this.serverJTree.validate();
		this.serverJTree.revalidate();
	}*/
	
	/*protected void refreshClientTreeModel() {
		this.clientJTree.removeTreeSelectionListener(clientJTreeSelectionListener);
		
		DefaultMutableTreeNode root = generateTreeNode(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())), null);
		this.clientJTree.setModel(new DefaultTreeModel(root));
		this.clientJTree.addTreeSelectionListener(clientJTreeSelectionListener);
		this.clientJTree.validate();
		this.clientJTree.revalidate();
	}*/
	
	/*protected void refreshServerTreeModel() {
		if(client != null && client.isConnected()) {
			try {
				client.sendDirectoryListingRequest();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		((DefaultMutableTreeNode) ((DefaultTreeModel) this.serverJTree.getModel()).getRoot()).removeAllChildren();
		((DefaultTreeModel) serverJTree.getModel()).reload();
		
		if(client.getRemotePathList() != null) {
			serverWorkspaceRootFile = FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList());
		}
		serverJTree.setModel(new DefaultTreeModel(generateTreeNode(serverWorkspaceRootFile, null)));
		((DefaultTreeModel) serverJTree.getModel()).reload();
		serverJTree.revalidate();
		mainPanel.revalidate();
		System.out.println("finished refreshing tree");
	}*/
	
	private ActionListener requestActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				client.sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//serverJTree.revalidate();
			mainPanel.revalidate();
			//serverWorkspaceRootFile = FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList());
			//refreshServerTreeModel();
			//setServerTreeModel(new DefaultTreeModel(generateTreeNode(serverWorkspaceRootFile, null)));
			//refreshClientTreeModel();
			serverTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(client.getRemotePathList()));
			clientTree.refreshTreeModel(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())));
		}
	};
	
	private ActionListener connectActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
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
				//refreshServerTreeModel();
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
	
	TreeSelectionListener clientJTreeSelectionListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) clientTree.getTree().getLastSelectedPathComponent();
			if(node != null) {
				clientJTreeSelection = (FSRemoteFile) node.getUserObject();
			}
			//System.out.println("local file selected:" + clientJTreeSelection.getPath());
			
			updateCenterPanelButtons();
		}
	};
	
	TreeSelectionListener serverJTreeSelectionListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			//DefaultMutableTreeNode node = (DefaultMutableTreeNode) serverJTree.getLastSelectedPathComponent();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) serverTree.getTree().getLastSelectedPathComponent();
			if(node != null) {
				serverJTreeSelection = (FSRemoteFile) node.getUserObject();
			}
			//System.out.println("remote file selected:" + serverJTreeSelection.getPath());
			
			updateCenterPanelButtons();
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
}
