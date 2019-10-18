package pt.uminho.ceb.biosystems.tools.blast;

import java.util.Set;

public class test {

	public static void main(String[] args) throws Exception {
		
		
		
		String queryPath = "C:\\Users\\Diogo\\Desktop\\query.faa";
		String subjectPath = "C:\\Users\\Diogo\\Desktop\\subject.faa";
		
		App.runBlast(null, queryPath, subjectPath, "10","0","0","0", 500, "C:\\Users\\Diogo\\Desktop\\bigBlast\\", "C:\\\\Users\\\\Diogo\\\\Desktop\\\\bigBlast\\\\bigBlastResults.json");
		
		
//		App.fixJsonFile2("C:\\Users\\Davide\\Desktop\\n.txt");
		
//		App.fixJsonFile("C:\\Users\\Davide\\Desktop\\Sophia\\FINAL_RESULT_SOPHIA4.json");
		
		//Set<String> refs = ProcessResults.readDataBackupFile("C:\\Users\\Davide\\Desktop\\Sophia\\FINAL_RESULT_SOPHIA5.json");
		
		//System.out.println("AA -> " + refs.size());
		
//		Set<String> ref = ProcessResults.readDataBackupFile("C:\\Users\\Davide\\Desktop\\allResults\\FINAL_RESULT2.json");
//		
//		System.out.println("A -> " + ref.size());
//		
////		for(String s : ref)
////			System.out.println(s);
//		
//		Set<String> res = ProcessResults.getAccessionsByOrganism("C:\\Users\\Davide\\Desktop\\rafaela\\BLAST\\allResults\\FINAL_RESULT_ALLRAFAELA.json");
//		
//		System.out.println("B -> " + res.size());
//		
//		for(String s : res)
//			System.out.println(s);
//		
//		res.removeAll(ref);
//		
//		System.out.println("C -> " + res.size());
		
		
		
//		ProcessResults.readJsonAndbuildMatrix("C:\\Users\\Davide\\Desktop\\rafaela\\BLAST\\allResults\\FINAL_RESULT_ALLRAFAELA.json");
		
		
//		File file = new File("C:\\Users\\Davide\\Desktop\\FINAL_RESULTS\\FINAL_RESULT.json"); 
//		  
//		String line = null;
//		
//        FileReader fr = new FileReader(file);
//        BufferedReader br = new BufferedReader(fr);
//        
//        line = br.readLine().replace("}{", ",");
//        
//        fr.close();
//        br.close();
//
//        FileWriter fw = new FileWriter(file);
//        BufferedWriter out = new BufferedWriter(fw);
//        out.write(line);
//        out.flush();
//        out.close();
//		  
//		System.out.println("done! ");
	}

}
