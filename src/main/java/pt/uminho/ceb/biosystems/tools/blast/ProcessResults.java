package pt.uminho.ceb.biosystems.tools.blast;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProcessResults {

	
	
	/**
	 * Method to read JSON exceptions file.
	 * 
	 * @return
	 */
	public static void readDataBackupFile(String filePath) {

		JSONParser parser = new JSONParser();

//		Map<String, ?> data = new HashMap<>();

		try {

//			Object obj = parser.parse(new FileReader("C:\\Users\\Davide\\Documents\\Exceptions.json"));
			Object obj = parser.parse(new FileReader(filePath));

			JSONObject allObjects = (JSONObject) obj;

			Set<String> keys = allObjects.keySet();	//queries
			
			Double evalueMax = 0.0;
			Double evalueMin = 1000.0;
			
			System.out.println(keys.size());

			for(String query : keys) {
				
				JSONObject hits = (JSONObject) allObjects.get(query);
				
				Set<String> allHits = hits.keySet();	//queries
				
				for(String subject : allHits) {
					
					Map<String, String> hitStats = (JSONObject) hits.get(subject);
					
					if(hitStats.containsKey(BlastParameters.query_coverage.toString())) 
						if(Double.valueOf(hitStats.get(BlastParameters.query_coverage.toString()).toString()) >= 0.8) {
//							System.out.println(query + "\t" + hitStats);
							
							Double evalueAux = Double.valueOf(hitStats.get(BlastParameters.e_value.toString()));
							
							if(evalueAux > evalueMax)
								evalueMax =evalueAux;
							
							if(evalueAux < evalueMin)
								evalueMin =evalueAux;
							
						}
						
				
				
				}
//				System.out.println();
			}
			
			System.out.println("Max evalue: " + evalueMax);
			System.out.println("Min evalue: " + evalueMin);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * Method to read JSON exceptions file.
	 * 
	 * @return
	 */
	public static void readJsonAndbuildMatrix(String filePath) {

		JSONParser parser = new JSONParser();

//		Map<String, ?> data = new HashMap<>();

		try {

//			Object obj = parser.parse(new FileReader("C:\\Users\\Davide\\Documents\\Exceptions.json"));
			Object obj = parser.parse(new FileReader(filePath));

			JSONObject allObjects = (JSONObject) obj;

			Set<String> keys = allObjects.keySet();	//queries
			
			Double evalueMax = 0.0;
			Double evalueMin = 1000.0;
			
			System.out.println(keys.size());
			
			Map<String, Map<String, Integer>> counts = new HashMap<>();  
			Map<String, Map<String, Double>> maxEvalue = new HashMap<>();
			Map<String, Map<String, Double>> minEvalue = new HashMap<>();

			for(String query : keys) {
				
				JSONObject hits = (JSONObject) allObjects.get(query);
				
				Set<String> allHits = hits.keySet();	//queries
				
				for(String subject : allHits) {
					
					Map<String, String> hitStats = (JSONObject) hits.get(subject);
					
					if(hitStats.containsKey(BlastParameters.query_coverage.toString())) 
						if(Double.valueOf(hitStats.get(BlastParameters.query_coverage.toString()).toString()) >= 0.8) {
//							System.out.println(query + "\t" + hitStats);
							
							Double evalueAux = Double.valueOf(hitStats.get(BlastParameters.e_value.toString()));
							
							if(evalueAux > evalueMax)
								evalueMax =evalueAux;
							
							if(evalueAux < evalueMin)
								evalueMin =evalueAux;
							
						}
						
				
				
				}
//				System.out.println();
			}
			
			System.out.println("Max evalue: " + evalueMax);
			System.out.println("Min evalue: " + evalueMin);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

}



