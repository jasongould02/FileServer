package jgould.fs.client.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import jgould.fs.commons.FSRemoteFile;

public class FSRemoteFileTreeUtil {

	private FSRemoteFileTreeUtil() {}
	
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
		if(rootFile.getPath() == null) {
			System.out.println("setting rootFilePath to:" + rootFile.getName() + File.separator);
			rootFile.setPath(rootFile.getName() + File.separator);
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
