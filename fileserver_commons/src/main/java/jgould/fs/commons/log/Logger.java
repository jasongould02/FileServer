package jgould.fs.commons.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private static BufferedWriter writer;
	private Logger() {}
	
	public static synchronized void initLogger(int bufferSize) throws IOException {
		LocalDateTime dt = LocalDateTime.now();
		DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HHmmss");
		String fileName = "LOG_" + dt.format(dtFormatter) + ".txt";
		
		writer = new BufferedWriter(new FileWriter(fileName), bufferSize);
	}
	
	public static synchronized void error(String message) {
		writeLog("ERROR:" + message);
	}

	public static synchronized void log(String message) {
		writeLog("DEBUG:" + message);
	}
	
	private synchronized static void writeLog(String message) {
		try {
			writer.write(message);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void closeLogger() throws IOException {
		writer.close();
	}
	
}
