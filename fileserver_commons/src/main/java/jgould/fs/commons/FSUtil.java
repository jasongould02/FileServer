package jgould.fs.commons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class FSUtil {

	private FSUtil() {}
	
	/**
	 * Replaces any '\\' or '/' characters with {@link File#separator}
	 * 
	 * @param path String object to be adjusted
	 * @return adjusted path 
	 */
	public static String checkPath(String path)  {
		if(path == null) {
			return null;
		}
		return (path.replace("\\", File.separator).trim());
	}
	
	/**
	 * 
	 * This method does not check if passed string is actually a directory or a file. This method appends a {@link File#separator} to the end of the string to signify to File objects that this String leads to a directory.
	 * 
	 * NOTE: This method calls {@link FSUtil#checkPath(String)} 
	 * 
	 * @param path path to directory
	 * @return the corrected path to a directory
	 */
	public static String checkDirectoryEnding(String path) {
		path = checkPath(path);
		if(!path.endsWith(File.separator)) {
			path = appendSeparator(path);
		}
		return path;
	}
	
	/**
	 * Appends a {@link File#separator} character to end of path without calling {@link FSUtil#checkPath(String)}
	 * 
	 * @param path
	 * @return
	 */
	public static String appendSeparator(String path) {
		if(!path.endsWith(File.separator)) {
			path += File.separator;
		}
		return path;
	}
	
	/**
	 * Returns the name and extension of a file.
	 * 
	 * @param path to desired file
	 * @return null if the passed path is a directory or file name length is zero
	 */
	public static String getFileName(String path) {
		path = checkPath(path);
		String filename = path.substring(path.lastIndexOf("\\") + 1);
		if(filename.length() == 0) { // passed a directory
			return null;
		} else {
			return filename;
		}
	}
	
	/**
	 * Returns the parent folder of the given path (if the path was "C:\test\folder\file.txt" then "C:\test\folder\" would be returned)
	 * 
	 * @param path
	 * @return parent folder of the path
	 */
	public static String getParent(String path) {
		/*if(destination.endsWith(filename)) {
			System.out.println("lastIndexof:" + destination.lastIndexOf(filename));
			System.out.println("trimmed:" + destination.substring(0, destination.lastIndexOf(filename)));
		}*/
		path = checkPath(path);
		if(path.endsWith(File.separator)) { // is a directory find parent folder 
			path = (path.substring(0, path.lastIndexOf(File.separator)));
			path = path.substring(0, path.lastIndexOf(File.separator) + 1);
			//System.out.println("twice:" + path);
		} else { // is a file return parent folder
			path = path.substring(0, path.lastIndexOf(File.separator) + 1);
		}
		
		//System.out.println("removeFileName:" + path);
		return path;
	}
	
	/**
	 * Returns file extension, if the given file name has no extension then null is returned
	 * @param filename the name of a file {@link FSRemoteFile#getName()} or the path of a file
	 * 
	 * @return file extension
	 */
	public static String getExtension(String filename) {
		if(filename != null && !filename.isEmpty()) {
			if(filename.contains(".")) {
				return filename.substring(filename.lastIndexOf("."));
			}
		}
		return null;
	}
	
	/**
	 * Calls {@link FSUtil#checkPath(String)} and then returns {@link FSUtil#getFileBytes(File)}
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static byte[] getFileBytes(String source) throws IOException {
		return getFileBytes(new File(checkPath(source)));
	}
	
	public static byte[] getFileBytes(File file) throws IOException {
		if(file == null) {
			return null;
		}
		if(file.exists() && file.isFile()) {
			return Files.readAllBytes(file.toPath()); // TODO: change for larger files that cant fit in reasonably sized byte array
		} else {
			return null;
		}
	}
	
	/**
	 * Will search given directory and all sub-directories. All files and directories will be returned in an ArrayList
	 * 
	 * @param file
	 * @return an unsorted ArrayList<{@link String}> of paths found in directory and sub-directories
	 */
	public static ArrayList<String> searchDirectory(File file) {
		ArrayList<String> list = new ArrayList<String>();
		if(file != null) {
			for (File f : file.listFiles()) {
				list.add(f.getPath());
				if (f.isDirectory()) {
					ArrayList<String> temp = searchDirectory(f);
					list.addAll(temp);
				}
			}
		}
		return list;
	}
	
}
