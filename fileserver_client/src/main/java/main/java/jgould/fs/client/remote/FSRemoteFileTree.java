package main.java.jgould.fs.client.remote;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import main.java.jgould.fs.commons.FSConstants;
import main.java.jgould.fs.commons.FSRemoteFile;

public class FSRemoteFileTree {

	private final int TREE_TYPE; 
	
    private JTree tree = null;
    private DefaultMutableTreeNode rootNode = null;
    private DefaultTreeModel treeModel = null;
    
    private JPopupMenu popupMenu = null;
    private JMenuItem renameItem = null;
    private JMenuItem pullItem = null;
    private JMenuItem pushItem = null;
    private JMenuItem removeItem = null;
    
    /**
     * Used to setup popup menu and instantiate JMenuItems
     * @return JPopupMenu instance
     */
    public JPopupMenu createPopupMenu() {
    	popupMenu = new JPopupMenu();
    	
    	renameItem = new JMenuItem("Rename");
    	pullItem = new JMenuItem("Download");
    	pushItem = new JMenuItem("Upload");
    	removeItem = new JMenuItem("Remove");
    	
    	System.out.println("created popup tree");
    	return popupMenu;
    }
    
    /**
     * Adds the correct menu items depending on the {@link FSRemoteFileTree#TREE_TYPE}
     */
    public void addMenu() {
    	if(TREE_TYPE == FSConstants.CLIENT_TREE) {
    		popupMenu.add(renameItem);
        	popupMenu.add(pushItem);
        	popupMenu.add(removeItem);
        	
        	pullItem = null;
    	} else if(TREE_TYPE == FSConstants.SERVER_TREE) {
    		popupMenu.add(renameItem);
        	popupMenu.add(pullItem);
        	popupMenu.add(removeItem);
        	
        	pushItem = null;
    	} else {
    		System.out.println("Invalid TREE_TYPE.");
    	}
    }
    
    public JPopupMenu getPopupMenu() {
    	return popupMenu;
    }
    
    public JMenuItem getRenameItem() {
    	return renameItem;
    }
    
    public JMenuItem getPullItem() {
    	return pullItem;
    }
    
    public JMenuItem getPushItem() {
    	return pushItem;
    }
    
    public JMenuItem getRemoveItem() {
    	return removeItem;
    }
    
    //private void updatePopupMenu() {}
    
    public FSRemoteFileTree(final int TREE_TYPE, ArrayList<String> pathList) {
    	this.TREE_TYPE = TREE_TYPE;
    	synchronized(this) {
    		tree = createJTree(tree, pathList, rootNode, treeModel);
    	}
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
	private synchronized JTree createJTree(JTree tree, ArrayList<String> pathList, DefaultMutableTreeNode root, DefaultTreeModel treeModel) {
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
	public void refreshTreeModel(FSRemoteFile rootFile) {
		DefaultTreeModel model = ((DefaultTreeModel) getTree().getModel());
		
		if(model == null) {
			System.out.println("The current tree model is null");
		}
		
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
		if(!expandedNodes.equals("")) {
			for(String s : expandedNodes.split(FSConstants.DELIMITER)) {
				tree.expandRow(Integer.parseInt(s));
			}
		}
		
		getTree().validate();
		getTree().revalidate();
	}
	
	/**
	 * Checks the given tree node as well as child nodes for a node with the same path 
	 * 
	 * uses {@link String#equals(Object)} to compare paths
	 * 
	 * @param node
	 * @param filePath
	 * @return will return null if the tree does not contain a FSRemoteFile with the given path, else it will return the FSRemoteFile that contains that path 
	 */
	public FSRemoteFile checkForPath(DefaultMutableTreeNode node, String filePath) {
		FSRemoteFile rf = null;
		if(node != null && filePath != null) {
			rf = (FSRemoteFile) node.getUserObject();
			if(rf != null) {
				if(filePath.equals(rf.getPath())) {
					return rf;
				} 
			}
			
			for(int i = 0; i < node.getChildCount(); i++) {
				return checkForPath((DefaultMutableTreeNode) node.getChildAt(i), filePath);
			}
		}
		return rf;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		this.tree.addTreeSelectionListener(listener);
	}
	
	public void addFocusListener(FocusListener listener) {
		this.tree.addFocusListener(listener);
	}
	
	public void addMouseListener(MouseListener listener) {
		this.tree.addMouseListener(listener);
	}
	
	public JTree getTree() {
		return tree;
	}
	
	public TreeModel getTreeModel() {
		return tree.getModel();
	}

}
