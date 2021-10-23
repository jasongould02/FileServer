package jgould.fs.java.main.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * NOTE: Workspace uses canonical paths as the arguments for the methods when passing String objects for paths.
 *
 */
public class Workspace {
	
	private File ws = null;
	
	public Workspace() {}
	
	public Workspace(String dir) throws Exception {
		setWorkspace(dir);
	}
	
	/**
	 * Sets the current workspace, if there was a previous workspace, the file/directory list will be refreshed and will only show files and directories from the new workspace.
	 * @param dir directory of desired workspace, if the path in 'dir' is not a directory an exception is thrown.
	 * @return File object to new workspace directory
	 * @throws Exception
	 */
	public File setWorkspace(String dir) throws Exception {
		dir = FSUtil.checkDirectoryEnding(dir);
		this.ws = new File(dir);
		if(ws.isDirectory()) {
			return ws;
		} else if(!ws.exists() || ws.isFile()){
			ws.mkdirs();
			return ws;
		} else {
			throw new Exception("Given string is not a directory.");
		}
	}
	
	/**
	 * Removes file from the directory and transfers the content to a trash bin the path to which is set in {@link FSConstants#setTrashBin(String)}
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
			Files.move(sourceFile.toPath(), FSConstants.getTrashBin().toPath(), copyOption);
			sourceFile.delete();
		}
		/*if(sourceFile.exists()) { // Check if the file still exists
			throw new IOException("Failed to delete:" + sourceFile.getAbsolutePath());
		}*/
		return sourceFile;
	}
	
	/**
	 * Moves a file from the given path and moves the file to the destination path.
	 * The destination must be a directory and not a file. The path to the 'source' and 'destination' should be relative (canonical) to the workspace path.
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
		destination = FSUtil.checkDirectoryEnding(destination);
		
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
		return temp;
	}
	
	public File moveFile(File source, File destination, StandardCopyOption copyOption) throws IOException, Exception {
		checkWorkspace();
		if(source.exists() && destination.exists()) {
			Files.move(source.toPath(), destination.toPath(), copyOption);
		} else {
			System.out.println("File does not exist");
		}
		
		return destination;
	}
	
	public File addFile(String filename, byte[] data, String destination, StandardOpenOption option) throws IOException {
		destination = FSUtil.checkPath(destination);
		File dest = null;
		
		if(FSUtil.getFileName(destination) != null) {
			destination = FSUtil.getParent(destination);
		}
		dest = new File(FSUtil.checkDirectoryEnding(destination));
		dest.mkdirs(); // create any missing directories
		
		Files.write(Paths.get(FSUtil.checkDirectoryEnding(destination) + filename), data, option);
		
		return dest;
	}
	
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
	
	public File[] listWorkspace() {
		return getWorkspace().listFiles(); 
	}
	
	public void printWorkspace() {
		for(String file : ws.list()) {
			System.out.println(file);
		}
	}
	
	public String getAbsolutePath() {
		return ws.getAbsolutePath() + File.separator;
	}
	
	public File getWorkspace() {
		return ws;
	}
	
}
