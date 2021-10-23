package jgould.fs.java.main.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FSConstants {

	private static File trashBin;
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	public static final String DELIMITER = ":";
	
	public static final String REQUEST = "REQUEST"; // REQUEST:path/to/file/requested:requested/file/desired/location/
	public static final String SEND = "SEND";
	public static final String REMOVE = "REMOVE";
	public static final String MOVE = "MOVE";
	
	public static final String FILE = "FILE";
	public static final String FOLDER = "FOLDER";

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
