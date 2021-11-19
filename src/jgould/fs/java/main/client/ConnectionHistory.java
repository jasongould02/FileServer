package jgould.fs.java.main.client;

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
	
	private final static String PREVIOUS_SERVER_NAME = "PREVIOUS_SERVER_NAME"; // This is the name applied to connection that the user requested to be remembered

	private ConnectionHistory() throws FileNotFoundException, JSONException {
		//JSONObject connections = loadJSONFile("savedConnections.json"); // loads the file and places it into the 'connections' JSONObject (represents the JSON file)
		//addAllConnections(connections); // Call after ConnectionHistory#loadJSONFile(String filePath) in order to load into HashMap
		
		/*for(String key : connectionsMap.keySet()) { // Print out list of keys and server names
			System.out.println("Key:" + key + "\tServer Name:" + connectionsMap.get(key).getServerName() + "\tServer IP:" + connectionsMap.get(key).getServerIP());
		}*/
	}
	
	/**
	 * Will load the file at the given file path (if it exists) and also return the JSONObject (the contents of the file as a JSONObject)
	 * @param filePath path to the desired .json file
	 * @return JSONObject of the file at given path: filePath
	 * @throws FileNotFoundException 
	 * @throws JSONException
	 */
	public static JSONObject loadJSONFile(String filePath) throws FileNotFoundException, JSONException {
		File file = new File(filePath);
		if(!file.isFile()) { // file not found
			return null;
		}
	
		BufferedReader reader = new BufferedReader(new FileReader(file));
		JSONObject obj = new JSONObject(new JSONTokener(reader));

		return obj;
	}
	
	/**
	 * Returns {@link Connection} object of a previous connection. This method returns null if the Connection can't be found in the HashMap
	 * @param objectName the server name of the connection
	 * @return desired JSONObject if it exists, null if requested object does not exist or if no JSONObject has been loaded (see {@link ConnectionHistory#loadJSONFile(String)} 
	 * @throws JSONException
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
	public static void addAllConnections(JSONObject connections) throws JSONException {
		if(connections == null) {
			return;
		}
		Iterator<?> i = connections.keys();
		while(i.hasNext()) {
			String key = (String) i.next();
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
	
	public static JSONObject saveConnections(String target) throws JSONException, IOException {
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
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		root.write(writer);
		writer.flush(); // necessary since JSONObject.write() will not flush the writer stream
		
		return root;
	}
	
	
	
}
