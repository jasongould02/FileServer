package jgould.fs.java.main.client;

import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import jgould.fs.java.main.util.FSConstants;

public class FSRemoteFileTree {

    private JTree tree = null;
    private DefaultMutableTreeNode rootNode = null;
    private DefaultTreeModel treeModel = null;
    
    public FSRemoteFileTree(ArrayList<String> pathList) {
    	tree = createJTree(tree, pathList, rootNode, treeModel);
    	tree.setRowHeight(25);
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
		DefaultTreeModel model = ((DefaultTreeModel) getTree().getModel());
		
		// Save which nodes are expanded;
		String expandedNodes = "";
		for(int i=0;i < getTree().getRowCount(); i++) {
			if(tree.isExpanded(i)) {
				expandedNodes += i + FSConstants.DELIMITER;
			}
		}
		
		DefaultMutableTreeNode rootNode = generateTreeNode(rootFile, null);
		model.setRoot(rootNode); // ((DefaultTreeModel) getTree().getModel()).setRoot(rootNode);

		// Re-expand nodes that were open before refreshing tree
		for(String s : expandedNodes.split(FSConstants.DELIMITER)) {
			tree.expandRow(Integer.parseInt(s));
		}
		
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
