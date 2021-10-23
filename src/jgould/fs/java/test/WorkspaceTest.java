package jgould.fs.java.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import jgould.fs.java.main.util.FSConstants;
import jgould.fs.java.main.util.FSUtil;
import jgould.fs.java.main.util.Workspace;

public class WorkspaceTest {

	public static void main(String[] args) {
		Workspace w = new Workspace();
		File f = new File("trash");
		try {
			w.setWorkspace("workspace");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		FSConstants.setTrashBin("trash" + File.separator);
		
		System.out.println(w.getAbsolutePath());
		
		try {
			
			w.moveFile("image1.png", "copyAttributes/", StandardCopyOption.REPLACE_EXISTING);
			String temp = w.moveFile("image2.png", "moveFile/", StandardCopyOption.REPLACE_EXISTING).getCanonicalPath();
			w.deleteFile("image3.png", StandardCopyOption.REPLACE_EXISTING);
			
			File test = new File("workspace\\copyAttributes\\image1.png");
			System.out.println("parent file:" + test.getParent());
			byte[] data = Files.readAllBytes(test.toPath());
			System.out.println(new String(data, FSConstants.CHARSET));
			
			
			//System.out.println(w.endsWithFile("workspace\\copyAttributes\\image1.png", "workspace\\copyAttributes"));
			
			String destination = "workspace\\copyAttributes\\image1.png";
			String filename = "image1.png";
			
			//FSUtil.getParent(FSUtil.getParent(FSUtil.getParent(destination)));
			FSUtil.getParent("workspace\\copyAttributes\\");
			FSUtil.getParent(destination);
			
			if(destination.endsWith(filename)) {
				System.out.println("lastIndexof:" + destination.lastIndexOf(filename));
				System.out.println("trimmed:" + destination.substring(0, destination.lastIndexOf(filename)));
			}
			
			
			System.out.println("FileName:"+FSUtil.getFileName("workspace\\copyAttributes\\image1.png") + ":length:" + FSUtil.getFileName("workspace\\copyAttributes\\image1.png").length());
			//System.out.println("FileName:"+FSUtil.getFileName("workspace\\copyAttributes\\") + ":length:" + FSUtil.getFileName("workspace\\copyAttributes\\").length()); // will cause NullPointerException
			
			w.moveFile(new File("copyAttributes" + File.separator), new File("test" + File.separator), StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println(temp);
			//w.moveFile("image.png", "tempfolder", StandardCopyOption.REPLACE_EXISTING);
			//System.out.println(w.deleteFile(w.getAbsolutePath() + "testfolder/image.png", StandardCopyOption.REPLACE_EXISTING));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
