package pt.uminho.ceb.biosystems.tools.blast;

import java.io.FileWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

import pt.uminho.ceb.biosystems.merlin.core.containers.alignment.AlignmentContainer;


public class ExportResults {

	/**
	 * @param results
	 * @param failed
	 */
	@SuppressWarnings("unchecked")
	public static void exportToJSON(ConcurrentLinkedQueue<AlignmentContainer> containers, String resultsName, boolean replaceFirst, boolean replaceLast) {
		
		try {
			
//			logger.info("Saving retrieved information into JSON object...");
			System.out.println("Saving information into JSON object...");
			
			FileWriter file = new FileWriter(resultsName, true);

			JSONObject mappingObj = new JSONObject();
			
			for(AlignmentContainer container : containers) {
				
				String query = container.getQuery();
				String target = container.getTarget();
				
				if(target == null)
					target = container.getTargetLocus();
				
				if(!mappingObj.containsKey(query))
					mappingObj.put(query, new JSONObject());
				
				JSONObject similarities = (JSONObject) mappingObj.get(query);
				
				if(!similarities.containsKey(target))
					similarities.put(target, new JSONObject());
				
				JSONObject alignmentDetails = (JSONObject) similarities.get(target);
				
				alignmentDetails.put(BlastParameters.bitScore.toString(), container.getBitScore()+"");
				alignmentDetails.put(BlastParameters.e_value.toString(), container.getEvalue()+"");
				alignmentDetails.put(BlastParameters.align_len.toString(), container.getAlignmentLength()+"");
				alignmentDetails.put(BlastParameters.identity.toString(), container.getIdentityScore()+"");
				alignmentDetails.put(BlastParameters.score.toString(), container.getScore()+"");
				alignmentDetails.put(BlastParameters.query_coverage.toString(), container.getCoverageQuery()+"");
				alignmentDetails.put(BlastParameters.target_coverage.toString(), container.getCoverageTarget()+"");
				
				similarities.put(target, alignmentDetails);
				mappingObj.put(query, similarities);
				
			}
			String line = mappingObj.toJSONString();		//fazer um jsonify no final em que faz replace de "}{" por nada
			
			if(replaceFirst)
				line = line.replaceAll("^\\{", "");
			
			if(replaceLast)
				line = line.replaceAll("\\}$", ",");
			
			file.write(line);
			file.write(System.getProperty( "line.separator" ));
			file.close();

//			logger.info("JSON object saved successfully to file...");
			System.out.println("JSON object saved successfully to file...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
