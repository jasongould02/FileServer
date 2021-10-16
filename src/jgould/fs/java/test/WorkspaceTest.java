package jgould.fs.java.test;

import java.io.File;
import java.nio.file.StandardCopyOption;

import jgould.fs.java.main.FileServerConstants;
import jgould.fs.java.main.server.Workspace;

public class WorkspaceTest {

	public static void main(String[] args) {
		Workspace w = new Workspace();
		File f = new File("trash");
		try {
			w.setWorkspace("workspace");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FileServerConstants.setTrashBin("trash\\");
		
		System.out.println(w.getAbsolutePath());
		
		try {
			
			w.moveFile("image1.png", "copyAttributes/", StandardCopyOption.REPLACE_EXISTING);
			String temp = w.moveFile("image2.png", "moveFile/", StandardCopyOption.REPLACE_EXISTING).getCanonicalPath();
			w.deleteFile("image3.png", StandardCopyOption.REPLACE_EXISTING);
			
			
			System.out.println(temp);
			//w.moveFile("image.png", "tempfolder", StandardCopyOption.REPLACE_EXISTING);
			//System.out.println(w.deleteFile(w.getAbsolutePath() + "testfolder/image.png", StandardCopyOption.REPLACE_EXISTING));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
