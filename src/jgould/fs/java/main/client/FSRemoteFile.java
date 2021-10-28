package jgould.fs.java.main.client;

import java.util.ArrayList;

public class FSRemoteFile {

	//private String remotePath;
	private String filename;
	private ArrayList<FSRemoteFile> children;
	
	// Can be checked if directory by seeing filename. directories won't have file extension
	// private boolean isDir = false;
	
	public FSRemoteFile() {
		children = new ArrayList<FSRemoteFile>();
	}
	
	//public FSRemoteFile(String filename, String remotePath, FSRemoteFile... files) {
	public FSRemoteFile(String filename, FSRemoteFile... files) {
		this.filename = filename;
		//this.remotePath = remotePath;
		
		children = new ArrayList<FSRemoteFile>();
		for(FSRemoteFile r : files) {
			this.children.add(r);
		}
	}
	
	//public FSRemoteFile(String filename, String remotePath) {
	public FSRemoteFile(String filename) {
		this.filename = filename;
		//this.remotePath = remotePath;
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
	
	public FSRemoteFile getChild(String filename) {
		for(FSRemoteFile rf : getChildren()) {
			if(rf.getName().equals(filename)) {
				return rf;
			}
		}
		return null;
	}
	
	/*public String getPath() {
		return remotePath;
	}*/
	
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
