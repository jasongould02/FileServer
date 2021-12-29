package jgould.fs.java.main.client.remote;

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
		return rootFile;
	}
	
	public static void printRemoteFile(FSRemoteFile f) {
		System.out.println("Name:[" + f.getName() + "] Path:[" + f.getPath() + "]");
		for(FSRemoteFile c : f.getChildren()) {
			printRemoteFile(c);
		}
	}
	
}
