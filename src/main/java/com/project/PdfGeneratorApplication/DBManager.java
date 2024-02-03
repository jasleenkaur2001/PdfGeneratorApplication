package com.project.PdfGeneratorApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Properties;

public class DBManager {
	private static int counter = 1;
	private static HashMap<PdfDetails, String> map = new HashMap<>();
	private static final String mapPath = DBManager.class.getClassLoader().getResource("").getPath() + "/map.ser";

	public DBManager() {
		
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("counter.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            counter = Integer.parseInt(prop.getProperty("counter"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
		serializeMapToFile();
		deserializeMapFromFile();
		
	}
	public int getCounter() {
		return counter;
	}
	public void updateCounter() {
		int count = DBManager.counter + 1;
		counter = count;
		try (FileOutputStream output = new FileOutputStream("src/main/resources/counter.properties")) {
            Properties prop = new Properties();
            prop.setProperty("counter", String.valueOf(count));
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
	}
	public void serializeMapToFile() {
		
        try (FileOutputStream fileOut = new FileOutputStream(mapPath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	public void deserializeMapFromFile() {

        try (FileInputStream fileIn = new FileInputStream(mapPath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            map = (HashMap<PdfDetails, String>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
    }
	public void addToMap(PdfDetails key , String value) {
		map.put(key, value);
	}
	public boolean checkInMap(PdfDetails key) {
		return map.containsKey(key);
	}
	public String getFileName(PdfDetails key) {
		return map.get(key);
	}
	protected void finalize()  
	{  
		serializeMapToFile();
	}  
	
}
