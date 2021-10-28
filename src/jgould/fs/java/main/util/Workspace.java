package jgould.fs.java.main.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;

import jgould.fs.java.main.client.FSRemoteFile;

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
		} else if(ws.isFile()){
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

	private String getListing(File root) {
		if(root.isFile()) {
			return "";
		}
		String listing = "";
		String rootPath = root.getPath();
		rootPath = FSUtil.checkDirectoryEnding(rootPath);
		
		listing += rootPath + ":";
		int i = 0;
		File[] list = root.listFiles();
		if(list != null) {
			for(i = 0; i < list.length; i++) {
				if(list[i] == null) { // TODO: fix
					continue;
					//System.out.println("its null!");
				}
				listing += list[i].getName();
				if(list[i].isDirectory()) {
					//System.out.println("dir:" + list[i].getName());
					listing += File.separator;
				}
				if(i == (list.length-1)) {
					//System.out.println("last index");
					listing += ";";
				} else {
					listing += ":";
				}
			}
		}
		return listing;
	}
	
	/**
	 * Prints out list of files in the current workspace
	 * @return
	 */
	public String listContents() {
		File root = this.getWorkspace();
		String output = "";
		
		output += getListing(root);
		for(File f : root.listFiles()) {
			output += getListing(f);
		}
		
		return output;
	}
	
	/**
	 * Will search given directory and all sub-directories. All files and directories will be returned in an ArrayList
	 * 
	 * @param file
	 * @return an unsorted ArrayList<{@link String}> of paths found in directory and sub-directories
	 */
	public ArrayList<String> searchDirectory(File file) {
		ArrayList<String> list = new ArrayList<String>();
		if(file != null) {
			for (File f : file.listFiles()) {
				list.add(f.getPath());
				System.out.println(f.getPath());

				if (f.isDirectory()) {
					ArrayList<String> temp = searchDirectory(f);
					//list.addAll(searchDirectory(f));
					list.addAll(temp);
				}
			}
		}
		return list;
	}
	
	/**
	 * Creates a FSRemoteFile tree from list of paths 
	 * 
	 * @param pathList
	 * @return
	 */
	public FSRemoteFile constructRemoteFileTree(ArrayList<String> pathList) {
		Collections.sort(pathList);
		FSRemoteFile rootFile = new FSRemoteFile();
		
		for(String path : pathList) {
			String[] split = path.split("\\\\");
			
			// DEBUG
			/*for(String s : split) {
				System.out.print(s + ",");
			} System.out.print("\n");*/
			
			int splitLength = split.length;
			FSRemoteFile currNode = rootFile;
			
			for(int i=0;i < splitLength; i++) {
				if(currNode.getName() == null) {
					currNode.setName(split[i]);
					//rootFile = new FSRemoteFile(split[i]);
					currNode = rootFile;
					//continue;
				}
				
				if(currNode.getName().equals(split[i])) {
					if((i+1) < splitLength) {
						if(currNode.hasChild(split[i+1])) {
							currNode = currNode.getChild(split[i+1]);
							continue;
						} else {
							FSRemoteFile newNode = new FSRemoteFile(split[i+1]);
							currNode.addChild(newNode);
							currNode = currNode.getChild(split[i+1]);
						}
					}
				}
			}
		}
		return rootFile;
	}
	
	@Deprecated
	public void printRemoteFiles(FSRemoteFile f, int depth) {
		System.out.print(f.getName() + "\n");
		if(f.getChildren().size() != 0) {
			for(FSRemoteFile child : f.getChildren()) {
				for(int i = 0; i < depth; i++) {
					System.out.print("\t child");
				}
				printRemoteFiles(child, depth + 1);
			}
		}
	}
	
	public void printWorkspace() {
		for(String file : ws.list()) {
			System.out.println(file);
		}
	}
	
	public String getAbsolutePath() {
		return ws.getAbsolutePath() + File.separator;
	}
	
	@Deprecated
	public File[] listWorkspace() {
		return getWorkspace().listFiles(); 
	}
	
	public File getWorkspace() {
		return ws;
	}
	
}
