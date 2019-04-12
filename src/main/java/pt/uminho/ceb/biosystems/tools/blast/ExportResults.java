package pt.uminho.ceb.biosystems.tools.blast;

import java.io.FileWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.AlignmentCapsule;

public class ExportResults {

	/**
	 * @param results
	 * @param failed
	 */
	@SuppressWarnings("unchecked")
	public static void exportToJSON(ConcurrentLinkedQueue<AlignmentCapsule> capsules, String resultsName) {
		
		try {
			
//			logger.info("Saving retrieved information into JSON object...");
			System.out.println("Saving information into JSON object...");
			
			FileWriter file = new FileWriter(resultsName, true);

			JSONObject mappingObj = new JSONObject();
			
			for(AlignmentCapsule capsule : capsules) {
				
				String query = capsule.getQuery();
				String target = capsule.getTargetLocus();
				
				if(!mappingObj.containsKey(query))
					mappingObj.put(query, new JSONObject());
				
				JSONObject similarities = (JSONObject) mappingObj.get(query);
				
				if(!similarities.containsKey(target))
					similarities.put(target, new JSONObject());
				
				JSONObject alignmentDetails = (JSONObject) similarities.get(target);
				
				alignmentDetails.put(BlastParameters.bitScore.toString(), capsule.getBitScore()+"");
				alignmentDetails.put(BlastParameters.e_value.toString(), capsule.getEvalue()+"");
				alignmentDetails.put(BlastParameters.align_len.toString(), capsule.getAlignmentLength()+"");
				alignmentDetails.put(BlastParameters.identity.toString(), capsule.getIdentityScore()+"");
				alignmentDetails.put(BlastParameters.score.toString(), capsule.getScore()+"");
				alignmentDetails.put(BlastParameters.query_coverage.toString(), capsule.getCoverageQuery()+"");
				alignmentDetails.put(BlastParameters.target_coverage.toString(), capsule.getCoverageTarget()+"");
				
				similarities.put(target, alignmentDetails);
				mappingObj.put(query, similarities);
				
			}

			file.write(mappingObj.toJSONString());		//fazer um jsonify no final em que faz replace de "}{" por nada

			file.close();

//			logger.info("JSON object saved successfully to file...");
			System.out.println("JSON object saved successfully to file...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
