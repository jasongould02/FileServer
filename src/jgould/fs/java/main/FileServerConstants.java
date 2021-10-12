package jgould.fs.java.main;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileServerConstants {

	public static final Charset CHARSET = StandardCharsets.UTF_8;
	
	private static File trashBin;

	public static void setTrashBin(String path) {
		trashBin = new File(path);
		if(!trashBin.exists() || !trashBin.isDirectory()) {
			trashBin.mkdir();
		}
	}
	
	public static File getTrashBin() {
		return trashBin;
	}
	
}
