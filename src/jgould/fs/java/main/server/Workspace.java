package jgould.fs.java.main.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import jgould.fs.java.main.FileServerConstants;

public class Workspace {
	
	private File ws = null;
	private ArrayList<File> wsFiles = null; // instead of using File#listFiles()
											// Probably will convert back to File#listFiles() instead  
	
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
			// System.out.println(getAbsolutePath());
			return ws;
		} else if(!ws.exists() || ws.isFile()){
			ws.mkdirs();
			//System.out.println(getAbsolutePath());
			return ws;
		} else {
			throw new Exception("Given string is not a directory.");
		}
	}
	
	/**
	 * Removes file from the directory and transfers the content to a trash bin the path to which is set in {@link FileServerConstants#setTrashBin(String)}
	 * @param filename
	 * @return File object of the deleted File
	 * @throws IOException
	 */
	public File deleteFile(String source, StandardCopyOption copyOption) throws IOException, Exception { 
		checkWorkspace();
		
		File sourceFile = new File(getAbsolutePath() + source);
		
		// DEBUG
		//System.out.println("Does " + sourceFile.getCanonicalPath() + " exists: " + sourceFile.exists() + " (" + sourceFile.getAbsolutePath() + ")");
		
		if(sourceFile.exists()) {
			Files.move(sourceFile.toPath(), FileServerConstants.getTrashBin().toPath(), copyOption);
			sourceFile.delete();
		}
		refreshWorkspace();
		if(sourceFile.exists()) {
			throw new IOException("Failed to delete:" + sourceFile.getAbsolutePath());
		}
		
		return sourceFile;
	}
	
	/**
	 * Moves a file from the given path and moves the file to the destination path.
	 * The destination must be a directory and not a file. The path to the 'source' should be relative to the workspace path
	 * e.g. if the workspace is '...\fileserver\workspace\' then a source file can be 'tempfolder\file.txt'
	 * The path of the source file would technically be '...\fileserver\workspace\tempfolder\file.txt'
	 * 
	 * @param path
	 * @param destination
	 * @param copyOption
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public File moveFile(String source, String destination, StandardCopyOption copyOption) throws IOException, Exception { 
		checkWorkspace();
		
		if(!destination.endsWith("\\")) {
			destination += "\\";
		}
		File temp = new File(getAbsolutePath() + source);
		File dest = new File(getAbsolutePath() + destination);
		
		// DEBUG
		//System.out.println("Does " + temp.getCanonicalPath() + " exists: " + temp.exists() + " (" + temp.getAbsolutePath() + ")");
		//System.out.println("Does " + dest.getCanonicalPath() + " exists: " + temp.exists()+ " (" + dest.getAbsolutePath() + ")");
		
		if(!dest.isDirectory() || !dest.exists()) {
			dest.mkdir();
		}
		
		if(temp.exists()) {
			Files.move(temp.toPath(), new File(getAbsolutePath() + destination + temp.getName()).toPath(), copyOption); // TODO: prompt users via GUI button press for the copy option
		}
		refreshWorkspace();
		return temp;
	}
	
	/*public File addFile(String filename, byte[] data, String destination) throws Exception {
		checkWorkspace();
		if(!destination.endsWith("\\")) {
			destination += "\\";
		}
		//File temp = new File(source);
		File dest = new File(destination + filename);
		if(dest.exists()) {
			// replace/prompt for different name
		}
		Files.write(dest.toPath(), data, StandardOpenOption.CREATE_NEW);
		return new File("temp");
	}*/
		
	public boolean checkWorkspace() throws Exception {
		if(ws == null) {
			throw new Exception("Workspace has not been set.");
			//return false;
		} else if(!ws.isDirectory()) {
			throw new Exception("Workspace is not a directory.");
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
	
	public String getAbsolutePath() {
		return ws.getAbsolutePath() + "\\";
	}
	
	public File getWorkspace() {
		return ws;
	}
	
	public ArrayList<File> getFiles() {
		return wsFiles;
	}
	
}
