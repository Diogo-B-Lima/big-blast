package pt.uminho.ceb.biosystems.tools.blast;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
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
	public static Set<String> readDataBackupFile(String filePath) {

		JSONParser parser = new JSONParser();
		Set<String> res = new HashSet<>();

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
						if(Double.valueOf(hitStats.get(BlastParameters.query_coverage.toString()).toString()) >= 0.8 
						&& Double.valueOf(hitStats.get(BlastParameters.target_coverage.toString()).toString()) >= 0.5) {
//							System.out.println(query + "\t" + hitStats);
							
							Double evalueAux = Double.valueOf(hitStats.get(BlastParameters.e_value.toString()));
							
							if(evalueAux > evalueMax)
								evalueMax =evalueAux;
							
							if(evalueAux < evalueMin)
								evalueMin =evalueAux;
							
							res.add(query.split("\\s+")[0]);
							
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
		return res;
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
			
			System.out.println(keys.size());
			
			Map<String, Map<String, Integer>> counts = new HashMap<>();  
			Map<String, Map<String, Double>> maxEvalue = new HashMap<>();
			Map<String, Map<String, Double>> minEvalue = new HashMap<>();

			String q = "";
			String s = "";
			
			for(String query : keys) {
				
				JSONObject hits = (JSONObject) allObjects.get(query);
				
				Set<String> allHits = hits.keySet();	//queries
				
				for(String subject : allHits) {
					
					Map<String, String> hitStats = (JSONObject) hits.get(subject);
//					System.out.println();
//					System.out.println(query + "\t" + subject);
					
					if(hitStats.containsKey(BlastParameters.query_coverage.toString())) {
						if(Double.valueOf(hitStats.get(BlastParameters.query_coverage.toString()).toString()) >= 0.8) {
							
							q = selectOrganism(query);
							s = selectOrganism(subject);
							
							if(q != null && s != null){
								
//								Map<String, Integer> submaxEvalue = new HashMap<>();
//								Map<String, Integer> subminEvalue = new HashMap<>();
								
								Map<String, Integer> subCount = null;
								
								if(!counts.containsKey(q)) {
									
									counts.put(q,  new HashMap<>());
									maxEvalue.put(q, new HashMap<>());
									minEvalue.put(q, new HashMap<>());
								}
								
								if(!counts.get(q).containsKey(s)) {
									
									subCount = counts.get(q);
									
									subCount.put(s, 0);
									counts.put(q, subCount);
								}
								
								subCount = counts.get(q);
								int newValue = subCount.get(s) + 1;
								
								subCount.put(s, newValue);
								
								counts.put(q, subCount);
								
							}
							else {
								System.out.println("q or s == NULL for query " + query + " or subject " + subject);
							}
							
							Double evalue = Double.valueOf(hitStats.get(BlastParameters.e_value.toString()));
							
							if(!maxEvalue.get(q).containsKey(s) || maxEvalue.get(q).get(s) < evalue) {
								
								maxEvalue.get(q).put(s, evalue);
								
							}
							
							if(!minEvalue.get(q).containsKey(s) || minEvalue.get(q).get(s) > evalue) {
								
								minEvalue.get(q).put(s, evalue);
							}
								
							
//							System.out.println(q);
							
//							System.out.println(query + "\t" + hitStats);
							
//							Double evalueAux = Double.valueOf(hitStats.get(BlastParameters.e_value.toString()));
//							
//							if(evalueAux > evalueMax)
//								evalueMax =evalueAux;
//							
//							if(evalueAux < evalueMin)
//								evalueMin =evalueAux;
//							
						}
						
				
				
				}
					
					
//				System.out.println();
				}
			}
			System.out.println(counts);
			System.out.println(maxEvalue);
			System.out.println(minEvalue);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}
	
	public static String selectOrganism(String s) {
		
		String chlamydomonas = "Chlamydomonas";
		String olucimarinus = "Olucimarinus";
		String otauri = "Otauri";
		String auxenochlorella = "Auxenochlorella";
		
		if(s.contains("NP_"))
			return chlamydomonas;
		else if(s.contains("XP2_") || s.contains("YP_"))
			return otauri;
		else if(s.contains("XP_"))
			return olucimarinus;
		else if(s.contains("AUX_"))
			return auxenochlorella;
		
		return null;
		
	}
	
	public static Set<String> getAccessionsByOrganism(String filePath) {

		JSONParser parser = new JSONParser();
		Set<String> res = new HashSet<>();
		
//		Map<String, ?> data = new HashMap<>();

		try {

//			Object obj = parser.parse(new FileReader("C:\\Users\\Davide\\Documents\\Exceptions.json"));
			Object obj = parser.parse(new FileReader(filePath));

			JSONObject allObjects = (JSONObject) obj;

			Set<String> keys = allObjects.keySet();	//queries
			
			System.out.println(keys.size());
			
			String q = "";
			String s = "";
			
			for(String query : keys) {
				
				JSONObject hits = (JSONObject) allObjects.get(query);
				
				Set<String> allHits = hits.keySet();	//queries
				
				for(String subject : allHits) {
					
					Map<String, String> hitStats = (JSONObject) hits.get(subject);
//					System.out.println();
//					System.out.println(query + "\t" + subject);
					
					if(hitStats.containsKey(BlastParameters.query_coverage.toString())) {
						if(Double.valueOf(hitStats.get(BlastParameters.query_coverage.toString()).toString()) >= 0.8) {
							
							q = selectOrganism(query);
							s = selectOrganism(subject);
							
							if(q != null && s != null){
								
								if(q.equals("Chlamydomonas")) {
									
									
									if(s.equals("Olucimarinus"))
										res.add(subject.split("\\s+")[0]);
									
								}
							}
							else {
								System.out.println("q or s == NULL for query " + query + " or subject " + subject);
							}
							
						}
						
				
				
				}
					
					
//				System.out.println();
				}
			}
		}
		catch(Exception e) {

			e.printStackTrace();
		}
		
		return res;
	}

}



