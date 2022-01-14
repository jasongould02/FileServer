package main.java.jgould.fs.commons;

import java.io.File;
import java.util.ArrayList;

public class FSRemoteFile {

	private String remotePath; // remotePath includes the filename at the end of the path
	private String filename;
	private ArrayList<FSRemoteFile> children;
	
	// Notes:
	// Can be checked if it is a directory by seeing filename. directories won't have file extension or tailing File.separator
	// private boolean isDir = false;
	
	public FSRemoteFile() {
		children = new ArrayList<FSRemoteFile>();
	}
	
	public FSRemoteFile(String filename) {
		this.filename = filename;
		this.children = new ArrayList<FSRemoteFile>();
	}
	
	public void addChild(FSRemoteFile child) {
		children.add(child);
	}
	
	public boolean hasChild(FSRemoteFile child) {
		if(getChildren().contains(child)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasChild(String filename) { // Does not check if looking for a directory or a file of the same name
		for(FSRemoteFile rf : getChildren()) {
			if(rf.getName().equals(filename)) {
				return true;
			} 
		}
		return false;
	}
	
	public boolean hasChildren() {
		if(getChildren().size() != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public FSRemoteFile getChild(String filename) {
		for(FSRemoteFile rf : getChildren()) {
			if(rf.getName().equals(filename)) {
				return rf;
			}
		}
		return null;
	}
		
	public FSRemoteFile checkForPath(FSRemoteFile root, String filePath) {
		if(root != null && filePath != null) {
			if(filePath.equals(root.getPath())) {
				return root;
			} else {
				for(FSRemoteFile c : root.getChildren()) {
					// Added the FSUtil.getFileName(filePath).equals(c.getName()) for checking if the workspace root file contains the desired file (when filePath is just the filename itself and no folders)
					if(filePath.equals(c.getPath()) || new File(filePath).getName().equals(c.getName())) {
						return c;
					} else {
						FSRemoteFile f = checkForPath(c, filePath);
						if (f != null) {
							if (filePath.equals(f.getPath())) {
								return f;
							} else {
								continue;
							}
						}
					}
				}
				return null;
			}
		} else {
			return null;
		}
	}
	
	public boolean isFolder() {
		return FSUtil.getExtension(this.getName()) == null ? true : false;
	}
	
	public boolean isFile() {
		return FSUtil.getExtension(this.getName()) != null ? true : false; 
	}
	
	public String getPath() {
		return remotePath;
	}
	
	public void setPath(String path) {
		this.remotePath = path;
	}
	
	public void setName(String name) {
		this.filename = name;
	}
	
	public String getName() {
		return filename;
	}
	
	public ArrayList<FSRemoteFile> getChildren() {
		return children;
	}
	
	public String toString() {
		return filename;
	}
	
}
