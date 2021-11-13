package jgould.fs.java.main.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FSRemoteFileTreeUtil {

	private FSRemoteFileTreeUtil() {}
	
	/**
	 * Will search given directory and all sub-directories. All files and directories will be returned in an ArrayList
	 * 
	 * @param file
	 * @return an unsorted ArrayList<{@link String}> of paths found in directory and sub-directories
	 */
	public static ArrayList<String> searchDirectory(File file) {
		//System.out.println("searchDirectory");
		ArrayList<String> list = new ArrayList<String>();
		if(file != null) {
			for (File f : file.listFiles()) {
				list.add(f.getPath());
				//System.out.println(f.getPath());

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
	public static FSRemoteFile constructRemoteFileTree(ArrayList<String> pathList) {
		ArrayList<String> temp = new ArrayList<String>(); // Have to place strings into a temporary ArrayList to sort to prevent concurrent modification
		temp.addAll(pathList);
		Collections.sort(temp);
		FSRemoteFile rootFile = new FSRemoteFile();
		
		for(String path : temp) {
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
			currNode.setPath(path);
		}
		//System.out.println("finished constructing new tree.");
		return rootFile;
	}
	
	/**
	 * Returns file extension, if the given file name has no extension then null is returned
	 * @param filename the name of a file {@link FSRemoteFile#getName()}
	 * @return file extension
	 */
	public static String getExtension(String filename) { // Technically FSRemoteFile.getPath() will also work here
		if(filename != null) {
			if(filename.contains(".")) {
				return filename.substring(filename.lastIndexOf("."));
			}
		}
		return null;
	}
	
	/**
	 * Calls {@link FSRemoteFileTreeUtil#getExtension(String)}
	 * @param f
	 * @return file extension
	 */
	public static String getExtension(FSRemoteFile file) {
		return getExtension(file.getName());
	}
	
	public static void printFSRemoteFileTree(FSRemoteFile root, int depth) {
		if(root != null) {
			if(root.hasChildren()) {
				for(FSRemoteFile c : root.getChildren()) {
					for(int i=0; i < depth; i++) {
						System.out.print(" ");
					}
					System.out.println(c.getName() + " > " + c.getPath());
					printFSRemoteFileTree(c, depth + 1);
				}
			}
		}
	}
	
}
