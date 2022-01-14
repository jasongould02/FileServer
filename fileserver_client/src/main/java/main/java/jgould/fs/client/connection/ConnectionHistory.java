package main.java.jgould.fs.client.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ConnectionHistory {	
	private static ConcurrentHashMap<String, Connection> connectionsMap = new ConcurrentHashMap<String, Connection>();
	
	// Connection key names to search when requesting specific values from a JSONObject form of a Connection
	private final static String SERVER_NAME_KEY = "serverName";
	private final static String SERVER_IP_KEY = "serverIP";
	private final static String SERVER_PORT_KEY = "serverPort";
	private final static String SERVER_TIMEOUT_KEY = "serverTimeout";
	
	private final static String PREVIOUS_SERVER_NAME = "PREVIOUS_SERVER_NAME"; // This is the field inside the savedConnections.json file that contains the serverName of the previous connected server
	private static String recent_connection_name = "";
	
	private ConnectionHistory() {}
	
	/**
	 * Will load the file at the given file path (if it exists) and also return the JSONObject (the contents of the file as a JSONObject)
	 * @param filePath path to the desired .json file
	 * @return JSONObject of the file at given path: filePath
	 * @throws FileNotFoundException if the file is not found however, the file.isFile() should not allow this to happen
	 * @throws JSONException thrown if the JSON file is invalid or an error occurs while parsing the file 
	 */
	public static JSONObject loadJSONFile(String filePath) throws FileNotFoundException, JSONException {
		File file = new File(filePath);
		if(!file.isFile()) { // file not found
			return null;
		}
	
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			JSONObject obj = new JSONObject(new JSONTokener(reader));
			return obj;
		} catch(JSONException e) {
			System.out.println("Invalid JSON file.");
			return null;
		}
	}
	
	/**
	 * Returns {@link Connection} object of a previous connection. This method returns null if the Connection can't be found in the HashMap
	 * @param objectName the server name of the connection
	 * @return desired JSONObject if it exists, null if requested object does not exist or if no JSONObject has been loaded (see {@link ConnectionHistory#loadJSONFile(String)} 
	 */
	public static Connection getConnection(String objectName) {
		if(connectionsMap == null || objectName == null) {
			return null;
		}
		if(connectionsMap.containsKey(objectName)) {
			return connectionsMap.get(objectName);
		} else {
			return null;
		}
	}
	
	/**
	 * If the HashMap contains a key with the same name as {@link Connection#getServerName()} then it will be overwritten and the {@link Connection} will be replaced 
	 * @param connection to be added to the connectionMap HashMap
	 */
	public static void addConnection(Connection connection) {
		connectionsMap.put(connection.getServerName(), connection);
	}
	
	// Call after ConnectionHistory#loadJSONFile(String filePath) in order to load into HashMap
	// If there is a previous connections with the same serverName, it will be overwritten 
	/**
	 * Searches the JSONObject for child objects (Connections). The connections parameter should be the JSONObject that is loaded from a file (the object that represents the entire file)
	 * @param connections JSONObject (from {@link ConnectionHistory#loadJSONFile(String)})
	 * @throws JSONException thrown if the JSONObject is invalid causing an error while parsing {@link Connection} data
	 */
	public static void addAllConnections(JSONObject connections) throws JSONException {
		if(connections == null) {
			return;
		}
		Iterator<?> i = connections.keys();
		while(i.hasNext()) {
			String key = (String) i.next();
			if(key.equals(PREVIOUS_SERVER_NAME)) {
				setMostRecentConnectionName(connections.getString(PREVIOUS_SERVER_NAME));
				continue;
			}
			JSONObject obj = connections.getJSONObject(key);
			if(obj == null) {
				continue;
			}
			try {
				String serverName = obj.getString(SERVER_NAME_KEY);
				String serverIP = obj.getString(SERVER_IP_KEY);
				int serverPort = obj.getInt(SERVER_PORT_KEY);
				int serverTimeout = obj.getInt(SERVER_TIMEOUT_KEY);
		
				connectionsMap.put(serverName, new Connection(serverName, serverIP, serverPort, serverTimeout));
			} catch (JSONException e) {
				System.out.println("Cannot find key in object, skipping.");
			}
		}
	}
	
	/**
	 * Saves the {@link Connection} data currently stored in the {@link ConcurrentHashMap}.
	 * @param target is the path to the file where the data should be stored
	 * @throws JSONException if there is an error while putting the {@link Connection} data into JSONObjects
	 * @throws IOException if there is an error while writing the data
	 */
	public static void saveConnections(String target) throws JSONException, IOException {
		File file = new File(target);
		if(!file.exists()) {
			file.createNewFile();
		}
		
		JSONObject root = new JSONObject();
		for(String key : connectionsMap.keySet()) {
			JSONObject c = new JSONObject();
			Connection connection = connectionsMap.get(key);
			c.putOnce("serverName", connection.getServerName());
			c.putOnce("serverIP", connection.getServerIP());
			c.putOnce("serverPort", connection.getServerPort());
			c.putOnce("serverTimeout", connection.getServerTimeout());
			
			root.putOnce(connection.getServerName(), c);
		}
		
		root.putOnce(PREVIOUS_SERVER_NAME, recent_connection_name);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		root.write(writer);
		writer.flush(); // necessary since JSONObject.write() will not flush the writer stream
	}
	
	/**
	 * Sets the value of the {@link PREVIOUS_SERVER_NAME} key to the desired server name 
	 * @param name is the name of the most recent server connected to
	 */
	public static void setMostRecentConnectionName(String name) {
		recent_connection_name = name;
	}
	
	/**
	 * @return the value of the {@link PREVIOUS_SERVER_NAME} key. It can return null, "" (an empty string), or a valid name 
	 */
	public static String getMostRecentConnectionName() {
		return recent_connection_name;
	}
	
	/**
	 * @return number of saved connections in the {@link ConnectionHistory#connectionsMap}
	 */
	public static int getConnectionCount() {
		return connectionsMap.size();
	}
	
	/**
	 * @return an array of available {@link Connection}s
	 */
	public static Connection[] getAvailableConnections() {
		Connection[] connections = null;
		if(connectionsMap.size() >= 1) {
			connections = new Connection[connectionsMap.size()];
			int position = 0;
			for(String key : connectionsMap.keySet()) {
				connections[position] = connectionsMap.get(key);
				position += 1;
			}
			return connections;
		} else {
			return null;
		}
	}
}
