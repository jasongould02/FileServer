package jgould.fs.java.main.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javafx.scene.shape.Path;
import jgould.fs.java.main.FileServerConstants;

public class Workspace {
	
	private File ws = null;
	private ArrayList<File> wsFiles = null; // instead of using File#listFiles()
											// Probably will convert back to File#listFiles() instead  
	
/*	public static void main(String[] args) {
		Workspace w = new Workspace();
		File f = new File("trash");
		f.mkdir();
		FileServerConstants.setTrashBin("trash/");
		
		try {
			w.moveFile("image.png", "testfolder");
			//w.deleteFile(filename);;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	public Workspace() {
		wsFiles = new ArrayList<File>();
	}
	
	public Workspace(String dir) throws Exception {
		setWorkspace(dir);
		wsFiles = new ArrayList<File>();
	}
	
	/**
	 * Sets the current workspace, if there was a previous workspace, the file/directory list will be refreshed and will only show files and directories from the new workspace.
	 * @param dir directory of desired workspace, if the path in 'dir' is not a directory an exception is thrown.
	 * @return File object to new workspace directory
	 * @throws Exception
	 */
	public File setWorkspace(String dir) throws Exception {
		this.ws = new File(dir);
		if(ws.isDirectory()) {
			return ws;
		} else {
			throw new Exception("Given string is not a directory.");
		}
	}
	
	/**
	 * Removes file from the directory and transfers the content to a trash bin the path to which is set in {@link FileServerConstants#setTrashBin(String)}
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public File deleteFile(String filename) throws IOException { // TODO: update wsFiles for change in the workspace
		File temp = null;
		for(File f : wsFiles) {
			if(f.getName().equals(filename) && f.exists()) {
				temp = f;
			}
		}
		if(temp != null) {
			wsFiles.remove(temp);
			Files.move(temp.toPath(), FileServerConstants.getTrashBin().toPath(), StandardCopyOption.REPLACE_EXISTING); // TODO: replace with prompt asking user for copy option
			temp.delete();
		}
		return temp;
	}
	
	public File moveFile(String filename, String destination) throws IOException { // TODO: update wsFiles for change in the workspace
		if(!destination.endsWith("/")) {
			destination += "/";
		}
		File temp = new File(filename);
		File dest = new File(destination);
		
		if(!dest.isDirectory()) {
			dest.mkdir();
		}
		
		
		if(temp.exists()) {
			Files.move(temp.toPath(), new File(destination + temp.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING); // TODO: replace with prompt asking user for copy option
		}
		return temp;
	}
	
	public boolean checkWorkspace() {
		if(ws == null) {
			return false;
		} else if(!ws.isDirectory()) {
			return false;
		} else if(!ws.exists()) {
			return false;
		} else {
			return true;
		}
	}
	
	public void refreshWorkspace() throws Exception {
		if(ws == null) {
			throw new Exception("Workspace is null");
		}
		if(checkWorkspace()) {
			for(File d : ws.listFiles()) {
				for(File f : wsFiles) { // TODO: check if file exists in the wsFiles and not in ws.listFiles() -> file removed
					if(d.compareTo(f) == 0) {
						continue;
					} else {
						wsFiles.add(d);
					}
				}
			}
		}
	}
	
	public void listWorkspace() {
		for(File f : wsFiles) {
			if(f.isDirectory()) {
				System.out.println("DIR>" + f.getName());
			} else if(f.isFile()) {
				System.out.println("FILE>" + f.getName());
			}
		
		}
	}
	
	public ArrayList<File> getFiles() {
		return wsFiles;
	}
	
}
