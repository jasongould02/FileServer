package jgould.fs.java.main.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FSUtil {

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
	
	public static String getParent(String path) {
		/*if(destination.endsWith(filename)) {
			System.out.println("lastIndexof:" + destination.lastIndexOf(filename));
			System.out.println("trimmed:" + destination.substring(0, destination.lastIndexOf(filename)));
		}*/
		path = checkPath(path);
		if(path.endsWith(File.separator)) { // is a directory find parent folder 
			//path =  (path.substring(0, path.lastIndexOf(File.separator))).substring(0, path.lastIndexOf(File.separator));
			
			path = (path.substring(0, path.lastIndexOf(File.separator)));
			path = path.substring(0, path.lastIndexOf(File.separator) + 1);
			System.out.println("twice:" + path);
			
			//System.out.println("once:" + path.substring(0, path.lastIndexOf(File.separator)));
			//System.out.println("twice:" + path.substring(0, path.lastIndexOf(File.separator)).substring(0, ));
			
		} else { // is a file return parent folder
			path = path.substring(0, path.lastIndexOf(File.separator) + 1);
		}
		
		System.out.println("removeFileName:" + path);
		return path;
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
			return Files.readAllBytes(file.toPath());
		} else {
			return null;
		}
	}
	
}
