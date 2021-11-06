package jgould.fs.java.main.client;

import java.awt.Color;
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

import javax.swing.BorderFactory;
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
	
	private JButton requestFilesButton;
	
	// Client File Tree
	private DefaultMutableTreeNode clientJTreeRoot = null;
    private DefaultTreeModel clientJTreeModel = null;
    private JTree clientJTree = null;
    // Server File Tree
    private DefaultMutableTreeNode serverJTreeRoot = null;
    private DefaultTreeModel serverJTreeModel = null;
    private JTree serverJTree = null;
    
    private JPanel centerPanel;
    private JButton filePushButton;
    private JButton filePullButton;
    private JButton fileDeleteButton;
    
    private GridBagLayout layout;
    
    private ConnectDialog dialog;
	
	private Client client;
	//private ClientView clientView;
	
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
		File f = new File("trash");
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
		
		requestFilesButton = new JButton("Request");
		
		requestFilesButton.addActionListener(requestActionListener);
		
		try {
			this.client.sendDirectoryListingRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//serverJTree = this.createJTree(serverJTree, client.getRemotePathList(), serverJTreeRoot, serverJTreeModel);
		serverJTree = this.createJTree(serverJTree, this.client.getRemotePathList(), serverJTreeRoot, serverJTreeModel);
		JScrollPane serverJTreeScrollPane = new JScrollPane(this.serverJTree);
        serverJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        serverJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        serverJTreeScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		clientJTree = this.createJTree(clientJTree, FSRemoteFileTree.searchDirectory(client.getFSWorkspace().getWorkspace()), clientJTreeRoot, clientJTreeModel);
		JScrollPane clientJTreeScrollPane = new JScrollPane(this.clientJTree);
		clientJTreeScrollPane.setMinimumSize(new Dimension(200, 600));
        clientJTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        clientJTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(serverJTreeScrollPane, c);
        
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        mainPanel.add(createCenterPanel());
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
		mainPanel.add(clientJTreeScrollPane, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 0;
		mainPanel.add(requestFilesButton, c);
		
		frame.add(mainPanel);
		
		frame.setVisible(true);

		for(File temp : getClient().getFSWorkspace().getWorkspace().listFiles()) {
			try {
				System.out.println("Canonical:" + temp.getCanonicalPath() + ":Absolute:"+temp.getAbsolutePath()+":Path:"+temp.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		serverJTree.setName("serverJTree");
		clientJTree.setName("clientJTree");
		serverJTree.addFocusListener(serverJTreeFocusListener);
		clientJTree.addFocusListener(clientJTreeFocusListener);
		
		clientJTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) clientJTree.getLastSelectedPathComponent();
				FSRemoteFile a = (FSRemoteFile) node.getUserObject();
				System.out.println("remoteFile:" + a.getPath());
			}
			
		});
		//System.out.println("\n");
		//System.out.println(getClient().getFSWorkspace().listContents());
	}
	
	private JPanel createCenterPanel() {
		centerPanel = new JPanel();
		
		filePushButton = new JButton("Upload File");
		filePullButton = new JButton("Download File");
		fileDeleteButton = new JButton("Delete File");
		
		filePushButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		filePullButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		fileDeleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		centerPanel.add(filePushButton);
		centerPanel.add(filePullButton);
		centerPanel.add(fileDeleteButton);
		
		centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		return centerPanel;
	}
	
	public Client getClient() {
		return client;
	}
	
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
	private JTree createJTree(JTree tree, ArrayList<String> pathList, DefaultMutableTreeNode root, DefaultTreeModel treeModel) {
		if(pathList != null) {
			serverWorkspaceRootFile = FSRemoteFileTree.constructRemoteFileTree(pathList);
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
	
	protected void setServerTreeModel(DefaultTreeModel treeModel) {
		this.serverJTree.setModel(treeModel);
		this.serverJTree.validate();
		this.serverJTree.revalidate();
	}
	
	protected void refreshServerTreeModel() {
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
			serverWorkspaceRootFile = FSRemoteFileTree.constructRemoteFileTree(client.getRemotePathList());
		}
		serverJTree.setModel(new DefaultTreeModel(generateTreeNode(serverWorkspaceRootFile, null)));
		((DefaultTreeModel) serverJTree.getModel()).reload();
		serverJTree.revalidate();
		mainPanel.revalidate();
		System.out.println("finished refreshing tree");
	}
	
	private ActionListener requestActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				client.sendDirectoryListingRequest();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			serverJTree.revalidate();
			mainPanel.revalidate();
			serverWorkspaceRootFile = FSRemoteFileTree.constructRemoteFileTree(client.getRemotePathList());
			setServerTreeModel(new DefaultTreeModel(generateTreeNode(serverWorkspaceRootFile, null)));
			FSRemoteFileTree.printFSRemoteFileTree(serverWorkspaceRootFile, 0);
			//refreshServerTreeModel();
		}
	};
	
	private ActionListener connectActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//client.connectToServer(ipAddress, portNumber, timeout)
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
}
