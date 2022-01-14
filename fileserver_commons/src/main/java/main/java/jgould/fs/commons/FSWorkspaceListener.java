package main.java.jgould.fs.commons;

public interface FSWorkspaceListener {

	//public static final int CLIENT_ORIGIN = 0;
	//public static final int SERVER_ORIGIN = 1;
	
	
	// Meant to take path and file name, and check for a conflict for files of same parent folder and name
	// Needs to check client and server
	// Best to check server file conflicts before sending the file to the server (use the Clients remote file list)
	//public String[] checkForNameConflict(boolean isSourceLocal, String sourcePath, String sourceName, String invalidPath, String invalidName);
	
	
}
