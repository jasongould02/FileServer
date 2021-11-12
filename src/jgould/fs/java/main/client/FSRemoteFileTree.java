package jgould.fs.java.main.client;

import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public class FSRemoteFileTree implements FSRemoteFileTreeListener {

    private JTree tree = null;
    private DefaultMutableTreeNode rootNode = null;
    private DefaultTreeModel treeModel = null;
    
    private FSRemoteFile currentFile = null;
    
    public FSRemoteFileTree(ArrayList<String> pathList) {
    	tree =createJTree(tree, pathList, rootNode, treeModel);
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
	
	//private FSRemoteFile serverWorkspaceRootFile;
	private JTree createJTree(JTree tree, ArrayList<String> pathList, DefaultMutableTreeNode root, DefaultTreeModel treeModel) {
		if(pathList != null) {
			//serverWorkspaceRootFile = FSRemoteFileTreeUtil.constructRemoteFileTree(pathList);
			//root = generateTreeNode(serverWorkspaceRootFile, null);
			root = generateTreeNode(FSRemoteFileTreeUtil.constructRemoteFileTree(pathList), null);
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
	
	// Use FSRemoteFileTreeUtil.constructRemoteFileTree(ArrayList<String> paths) to construct a tree of FSRemoteFile objects, then tree structure will then be placed into DefaultMutableTreeNodes and into the JTree
	protected void refreshTreeModel(FSRemoteFile rootFile) {
		TreeSelectionListener[] listeners = getTree().getTreeSelectionListeners();
		for(TreeSelectionListener l : getTree().getTreeSelectionListeners()) {
			getTree().removeTreeSelectionListener(l);
		}
		//getTree().removeTreeSelectionListener(getTree().getTreeSelectionListeners()[0]);
		
		//DefaultMutableTreeNode rootNode = generateTreeNode(FSRemoteFileTreeUtil.constructRemoteFileTree(FSRemoteFileTreeUtil.searchDirectory(client.getFSWorkspace().getWorkspace())), null);
		DefaultMutableTreeNode rootNode = generateTreeNode(rootFile, null);
		getTree().setModel(new DefaultTreeModel(rootNode));
		//clientJTree.addTreeSelectionListener(clientJTreeSelectionListener);
		for(TreeSelectionListener l : listeners) {
			getTree().addTreeSelectionListener(l);
		}
		((DefaultTreeModel) getTree().getModel()).reload();
		getTree().validate();
		getTree().revalidate();
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		this.tree.addTreeSelectionListener(listener);
	}
	
	public void addFocusListener(FocusListener listener) {
		this.tree.addFocusListener(listener);
	}
	
	public JTree getTree() {
		return tree;
	}
	
	public TreeModel getTreeModel() {
		return tree.getModel();
	}

	@Override
	public void remoteFileTreeChange() {
		
	}
    
	/*TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) clientJTree.getLastSelectedPathComponent();
			clientJTreeSelection = (FSRemoteFile) node.getUserObject();
			//System.out.println("local file selected:" + clientJTreeSelection.getPath());
			
			updateCenterPanelButtons();
		}
	};*/
	
	/*private FocusListener treeFocusListener = new FocusListener() {
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
	};*/
	
}
