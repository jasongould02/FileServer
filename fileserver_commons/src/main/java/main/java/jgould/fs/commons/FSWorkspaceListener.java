package main.java.jgould.fs.commons;

public interface FSWorkspaceListener {	
	/**
	 * Shows JOptionPane to get a new file name,
	 * returns null if the name is empty, no name entered or if the rename was cancelled
	 * */
	public String promptNewName(String filePath, String fileName);
	public int promptFolderConflict(boolean localWorkspace, FSRemoteFile originalFolder, String newFolderPath, String newFolderName);
	public int promptFileConflict(boolean localWorkspace, FSRemoteFile originalFile, String newArrivalPath, String newArrivalName);

	// Original tree is essentially the destination and is the file where sourceFile will be moving to / becoming
	public boolean conflictCheck(boolean isOriginalTreeLocal, FSRemoteFile sourceFile);
	public boolean conflictCheck(boolean isOriginalTreeLocal, String sourcePath);
	
	public FSRemoteFile getRemoteFileTree();
	public FSRemoteFile getLocalFileTree();
}
