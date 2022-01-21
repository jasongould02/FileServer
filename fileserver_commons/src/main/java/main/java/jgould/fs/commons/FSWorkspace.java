package main.java.jgould.fs.commons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * NOTE: Workspace uses canonical paths as the arguments for the methods when passing String objects for paths.
 *
 */
public class FSWorkspace {
	private FSWorkspaceListener workspaceListener = null;
	
	public void setWorkspaceListener(FSWorkspaceListener workspaceListener) {
		this.workspaceListener = workspaceListener;
	}
	
	private File ws = null;
	
	public FSWorkspace() {}
	
	public FSWorkspace(String dir) throws Exception {
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
		} else if(ws.isFile()){
			ws.mkdirs();
			return ws;
		} else {
			throw new Exception("Given string is not a directory.");
		}
	}
	
	public void renameFile(String sourcePath, String sourceName, String targetName, StandardCopyOption copyOption) throws IOException {
		String originalPath = null; // The sourcePath without the sourceName attached to the end of the String
		String actualName = null; // only changed from null if the String targetName contains File.separator chars (it is a new path)
		String addedTargetPath = null; // The sourcePath + new folders inside the TargetName (if its a new name) and
		
		if(!sourcePath.endsWith(sourceName)) {
			originalPath = sourcePath;
			sourcePath = sourcePath + File.separator + sourceName;
		} else { // sourcePath contains the sourceName
			originalPath = sourcePath.substring(0, sourcePath.lastIndexOf(File.separator));
		}
		
		String finalDestination = "";
		if(targetName.contains(File.separator)) { // contains new path (if original file was "workspace\test.png" then targetName should be in a format similar to: "workspace\new\folder\path\targetName"
			actualName = targetName.substring(targetName.lastIndexOf(File.separator));
			String targetPath = targetName.substring(0, targetName.lastIndexOf(File.separator)); // This is the the added folders (removes the new file name) the user wishes to create when renaming the source file
			
			// Create the new folders required to place the renamed file in
			addedTargetPath = originalPath + File.separator + targetPath;
			File pathToNewTarget = new File(addedTargetPath);
			pathToNewTarget.mkdirs();
			finalDestination = addedTargetPath + File.separator + actualName;
		} else { // targetName doesn't contain any new folders
			finalDestination = FSUtil.getParent(sourcePath) + targetName;
		}
		files_move(sourcePath, finalDestination, copyOption);
	}
	
	private void mergeFolders() {
		// TODO: complete merge folders function
		//  			for every file in a folder,	check if mergedFile_i exists in folder, if not add, if it exists ask for overwrite or cancel specific file move, or cancel the rest of the move
	}
	
	private Path files_move(String sourcePath, String destinationPath, StandardCopyOption option) throws IOException {
		if(sourcePath == null || destinationPath == null || option == null) {
			System.out.println("FSWORKSPACE: invalid files_move args");
			return null;
		} else {
			System.out.println("files_move used");
		}
		if(this.workspaceListener != null) {
			//boolean hasConflict = workspaceListener.conflictCheck(true, destinationPath);
			//FSRemoteFile destinationFile = workspaceListener.getLocalFileTree().checkForPath(workspaceListener.getLocalFileTree(), destinationPath);
			File destinationFile = new File(destinationPath);
			if(destinationFile.exists()) {
				String targetName = destinationFile.getName();
				File sourceFile = new File(sourcePath);
				//FSRemoteFile sourceFile = workspaceListener.getLocalFileTree().checkForPath(workspaceListener.getLocalFileTree(), sourcePath);
				String newName;
				boolean newNameConflict = true;
				
				do {
					newName = workspaceListener.promptNewName(sourceFile.getPath(), sourceFile.getName());
					if(sourceFile.getName().equals(newName) || newName == null) { // cancel
						System.out.println("retaining original file name");
						destinationPath = sourcePath;
						break;
					} else if(newName.equals(targetName)) { // overwrite
						System.out.println("New name set to targetName");
						break;
					}
					String newNamePath = FSUtil.getParent(destinationPath) + newName;
					File newNameFile = new File(newNamePath);
					if(!newNameFile.exists()) { // the new file name is still a conflict
						newNameConflict = false;
						destinationPath = newNamePath;
						break;
					}
				} while(newNameConflict == true); 
			}
		}
		Path src = Paths.get(sourcePath);
		Path dest = Paths.get(destinationPath);
		return Files.move(src, dest, option);
	}
	
	/**
	 * Removes file from the directory and transfers the content to a trash bin the path to which is set in {@link FSConstants#setTrashBin(String)}
	 * @param filename
	 * @return File object of the deleted File
	 * @throws IOException
	 */
	public File deleteFile(String source, StandardCopyOption copyOption) throws IOException, Exception { 
		checkWorkspace();
		File sourceFile = new File(source);
		File tempFile = new File(FSConstants.getTrashBin().getPath() + File.separator + sourceFile.getName());
		int i = 0;
		if(sourceFile.isFile()) {
			String actualName = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.'));
			String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'));
			while(tempFile.exists()) {
				tempFile = new File(FSConstants.getTrashBin().getPath() + File.separator + actualName + " (" + i + ")" + extension);
				i++;
			}
		} else {
			String actualName = sourceFile.getName();
			while(tempFile.exists()) {
				tempFile = new File(FSConstants.getTrashBin().getPath() + File.separator + actualName + " (" + i + ")");
				i++;
			}
		}
		if(sourceFile.exists()) {
			//Files.move(sourceFile.toPath(), tempFile.toPath(), copyOption);
			files_move(sourceFile.getPath(), tempFile.getPath(), copyOption);
			sourceFile.delete();
		}
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
		
		if(!dest.isDirectory() || !dest.exists()) {
			dest.mkdir();
		}
		if(temp.exists()) {
			Files.move(temp.toPath(), new File(getAbsolutePath() + destination + temp.getName()).toPath(), copyOption); // TODO: prompt users via GUI button press for the copy option
		}
		return temp;
	}
	
	@Deprecated
	public File moveFile(File source, File destination, StandardCopyOption copyOption) throws IOException, Exception {
		checkWorkspace();
		if(source.exists() && destination.exists()) {
			Files.move(source.toPath(), destination.toPath(), copyOption);
		} else {
			System.out.println("File does not exist");
		}
		
		return destination;
	}
	
	
	/**
	 * 
	 * Do not have the filename attached to the destination
	 * @param filename
	 * @param data
	 * @param destination
	 * @param option
	 * @return
	 * @throws IOException
	 */
	public File addFile(String filename, byte[] data, String destination, StandardOpenOption option) throws IOException {
		File dest = null;
		dest = FSUtil.getExtension(destination) != null ? new File(FSUtil.getParent(destination)) : new File(destination);
		dest.mkdirs(); // create any missing directories
		System.out.println("add File destination:" + destination);
		String destinationPath = FSUtil.checkDirectoryEnding(destination) + filename;
		if(workspaceListener != null) {
			File addedFile = new File(destinationPath);
			if(addedFile.exists()) {
				String newName;
				boolean newNameConflict = true;
				do {
					newName = workspaceListener.promptNewName(addedFile.getPath(), addedFile.getName());
					String newNamePath = FSUtil.getParent(destinationPath) + newName;
					if(newName == null) { // cancel adding the file
						System.out.println("retaining original file, cancelling file add");
						destinationPath = addedFile.getPath();
						return null;
					} else if (newName.equals(addedFile.getName()))  { // overwrite the original file that was there
						break;
					}
					File newNameFile = new File(newNamePath);
					if(!newNameFile.exists()) { // the new file name is still a conflict
						newNameConflict = false;
						destinationPath = newNamePath;
						break;
					}
				} while(newNameConflict == true); 
			}
		}
		
		Files.write(Paths.get(destinationPath), data, option);
		
		return dest;
	}
	
	/**
	 * Do not include the name at the end of the destination
	 * */
	public File addDirectory(String folderName, String destination) {
		File dest = new File(FSUtil.checkDirectoryEnding(destination) + folderName);
		System.out.println("add Directory destination:" + destination);
		//System.out.println("add dir" + destination + "\t" + "dest:" + dest.getPath() + "\tfoldername:" + folderName);
		dest.mkdirs();
		
		return dest;
	}
	
	public boolean checkWorkspace() throws IOException {
		if(ws == null) {
			throw new IOException("Workspace has not been set.");
			//return false;
		} else if(!ws.isDirectory()) {
			throw new IOException("Workspace is not a directory.");
		} else {
			return true;
		}
	}
	
	/**
	 * Checks if the given path exists in  the workspace
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	private File containsFile(String path) throws IOException {
		if(!isValidWorkspacePath(path)) {
			System.out.println("Path:[" + path + "] does NOT exist in the workspace.");
			return null;
		}
		File f = new File(path);
		if(f.exists()) {
			return f;
		} else {
			return null;
		}
	}
	
	/**
	 * This method checks if the given path starts with the relative path of the FSWorkspace to check that the file is accessible
	 * TODO: Add check for multiple workspaces and more in-depth check 
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private boolean isValidWorkspacePath(String path) throws IOException {
		if(checkWorkspace()) {
			if(!path.startsWith(this.getWorkspace().getPath() + File.separator)) {
				System.out.println("Path:[" + path + "] is not a valid workspace path.");
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	public String getAbsolutePath() {
		return ws.getAbsolutePath() + File.separator;
	}
		
	public File getWorkspace() {
		return ws;
	}
	
}
