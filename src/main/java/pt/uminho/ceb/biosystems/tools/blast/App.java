package pt.uminho.ceb.biosystems.tools.blast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;

import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.template.AbstractSequence;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.containers.alignment.AlignmentContainer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.AlignmentScoreType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.Method;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final int LIMIT = 500;
	private static final String FINAL_RESULTS = "finalResults.json";
	private static final String EVALUE = "-6";
	private static final String BIT_SCORE = "50";
	private static final String QUERY_COVERAGE = "0.8";
	private static final String SUBJECT_COVERAGE = "0.8";
	// threshold values based on Pearson's study "An introduction to sequence similarity ("Homology") Searching"

	public static void main(String[] args ){
		
//		args = new String[] {"-b", "tblastn", "-q", "/home/1- Chlamydomonas/GCF_000002595.1_C.reinhardtii_v3.0_protein.faa", "-s", "/home/Haematococcus pluvialis_fasta/GCA_003970955.1_H.pluvialis_ASM397095v1_genomic.fna", "-w", "/home/", "-r", "coiso.json", "-e", "1", "-p", "500"};
		
		String query = "";
		String subject = "";
		String blastProgram = "";
		String evalue = "";
		String bitScore = "";
		String queryCoverage = "";
		String subjectCoverage = "";

		String workDirectory = "";
		String resultsFileName = FINAL_RESULTS;
		int newLimit = LIMIT;

		boolean go = false;
		
		System.out.println("Input: " + Arrays.asList(args));

		if(args.length > 0) {

			if(args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
				printHelp();
				System.exit(0);
			}

			else if(args[0].equalsIgnoreCase("-c") || args[0].equalsIgnoreCase("-commands")) {
				printCommands();
				System.exit(0);
			}
			else if(args.length > 7) {

				for(int i = 0; i < args.length; i++) {

//					System.out.println(i);
//					System.out.println(args[i]);
					
					if(args[i].equals("-b")) {
						i++;
						blastProgram = args[i];
					}

					else if(args[i].equals("-q")) {
						i++;
						query = args[i];
					}
						
					else if(args[i].equals("-s")) {
						i++;
						subject = args[i];
					}
						
					else if(args[i].equals("-w")) {
						i++;
						workDirectory = args[i];
					}

//					System.out.println(blastProgram + "\t"+ query + "\t" + subject + "\t" + workDirectory);
				}

				if(!blastProgram.isEmpty() && !query.isEmpty() && !subject.isEmpty() && !workDirectory.isEmpty()) {
					go = true;
				}
				else {
					debug(blastProgram, query, subject, workDirectory);
				}
			}
			
			if(args.length > 12) {

				for(int i = 0; i < args.length; i++) {
					
					try {
						if(args[i].equals("-p")) {
							i++;
							newLimit = Integer.valueOf(args[i]);
						}

						else if(args[i].equals("-e")) {
							i++;
							evalue = args[i];
						}
						else if(args[i].equals("-r")) {
							i++;
							resultsFileName = args[i];
						}
						
						else if(args[i].equals("-bit")) {
							i++;
							bitScore = args[i];
						}
						
						else if(args[i].equals("-qcov")) {
							i++;
							queryCoverage = args[i];
						}
						
						else if(args[i].equals("-scov")) {
							i++;
							subjectCoverage = args[i];
						}
					} 
					catch (NumberFormatException e) {
						newLimit = LIMIT;
						System.out.println("-p command not accepted! Default value of " + LIMIT + " assumed!");
					}
					catch (Exception e) {
						newLimit = LIMIT;
						resultsFileName = FINAL_RESULTS;
						evalue = EVALUE;
						bitScore = BIT_SCORE;
						queryCoverage = QUERY_COVERAGE;
						subjectCoverage= SUBJECT_COVERAGE;

					}
				}
			}

		}
		
		else {
			printHelp();
			System.out.println("No arguments provided!! Please insert at least the mandatory ones!");
			System.exit(1);
		}

		if(!go) {
			printHelp();
			System.exit(2);
		}
		
//		System.exit(0);
		
		File f = new File (workDirectory);
		if(!f.exists())
			f.mkdir();
		
		boolean success = false;
		
		try {
			success = runBlast(blastProgram, query, subject, evalue, bitScore, queryCoverage, subjectCoverage, newLimit, workDirectory, resultsFileName);
		} 
		catch (InterruptedException e) {
			System.out.println("An error occurred while running!!");
			e.printStackTrace();
			System.exit(3);
		} catch (Exception e) {
			System.out.println("An unknown error occurred while running!!");
			e.printStackTrace();
		}
		
		if(success)
			System.out.println("Done! Blast complete!");
		else
			System.out.println("Something went wrong! Please try again!");
		
	}

	

	public static boolean runBlast(String blastProgram, String query, String subject, String evalue, String bitScore, String queryCoverage, String subjectCoverage, int sequencesLimit, String workdir, String resultsFileName) throws Exception {

//		String subFasta = workdir.concat("/newTemporaryFasta.faa");

		try {
			
			PrintWriter writer = new PrintWriter(resultsFileName);	//clean the file if already exists
			writer.print("");
			writer.close();

			System.out.println("query: " + query);
			System.out.println("subject: " + subject);
			
			ConcurrentHashMap<String, AbstractSequence<?>> querySequences= new ConcurrentHashMap<String, AbstractSequence<?>>();
			querySequences.putAll(FastaReaderHelper.readFastaProteinSequence(new File(query)));
			
			ConcurrentHashMap<String, AbstractSequence<?>> subjectSequences= new ConcurrentHashMap<String, AbstractSequence<?>>();
			subjectSequences.putAll(FastaReaderHelper.readFastaProteinSequence(new File(subject)));
			
			int totalBlasts = querySequences.size() / sequencesLimit;
			
			int count = 1;
			
			String resultsFile2 = new File(resultsFileName).getName();
			
			resultsFile2 = resultsFileName.replace(resultsFile2, "complementary_"+resultsFile2);
			
			while(querySequences.size() > 0) {
				
				System.out.println("Blast " + count + " of " + totalBlasts +"!");

				ConcurrentHashMap<String, AbstractSequence<?>> subQuerySequences= new ConcurrentHashMap<String, AbstractSequence<?>>();

				if(querySequences.size() > 1000) {

					int i = 0;

					for(String sequence : querySequences.keySet()) {

						subQuerySequences.put(sequence, querySequences.get(sequence));
						querySequences.remove(sequence);

						i++;
						if(i == sequencesLimit)
							break;
					}

				}
				else {
					subQuerySequences.putAll(querySequences);
					querySequences = new ConcurrentHashMap<String, AbstractSequence<?>>();
				}
				
				
//				List<Thread> threads = new ArrayList<Thread>();
				//			ConcurrentLinkedQueue<String> queryArray = new ConcurrentLinkedQueue<String>(this.querySequences.keySet());
//				int numberOfCores = Runtime.getRuntime().availableProcessors();
				//int numberOfCores = new Double(Runtime.getRuntime().availableProcessors()*1.5).intValue();

//				if(subQuerySequences.keySet().size()<numberOfCores)
//					numberOfCores=subQuerySequences.keySet().size();
//				
//				String path = workdir.concat("/queryBlast");
				
//				File f = new File (path);
//				if(!f.exists())
//					f.mkdir();
				
				//			this.querySize.set(new Integer(this.querySequences.size()));
				//			setChanged();
				//			notifyObservers();

				//Distribute querySequences into fastaFiles

				//			logger.debug("Writting query sequences temporary fasta files... ");
				System.out.println("Writting query sequences temporary fasta files... ");


//				CreateGenomeFile.buildSubFastaFiles(workdir, subQuerySequences, queriesSubSetList, queryFilesPaths, numberOfCores);

				RunSimilaritySearchBigBlast similaritySearch = new RunSimilaritySearchBigBlast(subjectSequences, 0.0, Method.Blast, subQuerySequences,
						new AtomicBoolean(false), new AtomicInteger(0), new AtomicInteger(0), AlignmentScoreType.ALIGNMENT);

				similaritySearch.setWorkspaceTaxonomyFolderPath(workdir);
				
				Pair<ConcurrentLinkedQueue<AlignmentContainer>,ConcurrentLinkedQueue<AlignmentContainer>> bbHits 
				= similaritySearch.runBBBlastHits(//queryFastaFile.getAbsolutePath(),subjectFastaFile.getAbsolutePath(),
						false, Double.valueOf(evalue),  Double.valueOf(bitScore),  Double.valueOf(queryCoverage),  Double.valueOf(subjectCoverage));    // last 4 parameters are evalue, bitscore, query coverage and target coverage thresholds
				
				
				boolean replaceLast = true;
				boolean replaceFirst = true;
				
				if(count == totalBlasts)
					replaceLast = false;
				
				if(count == 1)
					replaceFirst = false;
					
				
				ExportResults.exportToJSON(bbHits.getA(), resultsFileName, replaceFirst, replaceLast);
				ExportResults.exportToJSON(bbHits.getB(), resultsFile2, replaceFirst, replaceLast);

				count++;
			}
			
			System.out.println("Blast finnished!");

//			try {
//				fixJsonFile(resultsFileName);
//			} 
//			catch (Exception e) {
//				System.out.println("JSON fix failed! Results are stored but JSON is not valid! To fix it, replace all '}{' occurrences by ','!");
//				e.printStackTrace();
//			}

			return true;

		} 
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	private static void printHelp() {

		System.out.println();
		System.out.println("################ HELP MENU ################");
		System.out.println();
		System.out.println("Recommended arguments to run this thing:");
		System.out.println("-b blast_Program -q query_file -s subject_file -w ./something -r result.json -e E-Value_threshold -p sequences_Per_Blast");
		System.out.println("NOTE1: The -e E-Value_threshold is optional. If empty, the default of the blast program is assumed!");
		System.out.println("NOTE1: -p Sequences_Per_Blast is optional, if empty the default of " + LIMIT + " is assumed!");
		System.out.println("NOTE1: -r Results file name is option, if empty  " + FINAL_RESULTS + " is assumed!");
		System.out.println("Run -c option to see all arguments!");
		System.out.println("IMPORTANT: Type the following java argument if you are getting exceptions in the xml NCBIParser:");
		System.out.println("-Djavax.xml.accessExternalDTD=all");
		System.out.println();

	}

	private static void printCommands() {

		System.out.println();
		System.out.println("################ COMMANDS MENU ################");
		System.out.println();
		System.out.println("-b: blast program to execute");
		System.out.println("-q: query file path");
		System.out.println("-s: subject file path");
		System.out.println("-w: work directory");
		System.out.println("-r: (Optional) The name of the results file. If empty, default result file name is assumed. The file is placed inside the work directory!");
		System.out.println("-e: (Optional) e-value threshold");
		System.out.println("-p: (Optional) the number of sequences per blast");
		System.out.println("-h: Display help menu");
		System.out.println("-c: Display commands menu");
		System.out.println();
	}

	private static void debug(String blastProgram, String query, String subject, String workDirectory) {
		if(blastProgram.isEmpty())
			System.out.println("ERROR!! Please insert a blast program!");
		if(query.isEmpty())
			System.out.println("ERROR!! Please insert a query file!");
		if(subject.isEmpty())
			System.out.println("ERROR!! Please insert a subject file!");
		if(workDirectory.isEmpty())
			System.out.println("ERROR!! Please insert a work directory!");
	}
	
	/**
	 * @param path
	 * @param sequences
	 */
	public static void buildSubFastaFiles(String filesPath, Map<String, AbstractSequence<?>> allSequences, 
			List<Map<String,AbstractSequence<?>>> queriesSubSetList, List<String> queryFilesPaths, int numberOfFiles){
		
		Map<String, AbstractSequence<?>> queriesSubSet = new HashMap<>();
		
		int batch_size= allSequences.size()/numberOfFiles;
		
		String fastaFileName;
		
		int c=0;
		for (String query : allSequences.keySet()) {
			
			queriesSubSet.put(query, allSequences.get(query));

			if ((c+1)%batch_size==0 && ((c+1)/batch_size < numberOfFiles)) {
				
				fastaFileName = filesPath.concat("/SubFastaFile_").concat(Integer.toString((c+1)/batch_size)).concat("_of_").
						concat(Integer.toString(numberOfFiles)).concat(FileExtensions.PROTEIN_FAA.getExtension());
				
				CreateGenomeFile.buildFastaFile(fastaFileName, queriesSubSet);
				queryFilesPaths.add(fastaFileName);
				queriesSubSetList.add(queriesSubSet);
				
				queriesSubSet = new HashMap<>();
			}
			c++;
		}
		
		fastaFileName = filesPath.concat("/SubFastaFile_").concat(Integer.toString(numberOfFiles)).concat("_of_").
				concat(Integer.toString(numberOfFiles)).concat(FileExtensions.PROTEIN_FAA.getExtension());
		
		CreateGenomeFile.buildFastaFile(fastaFileName, queriesSubSet);
		queriesSubSetList.add(queriesSubSet);
		queryFilesPaths.add(fastaFileName);

	}
	
	/**
	 * @param path
	 * @throws IOException
	 */
	public static void fixJsonFile(String path) throws IOException {
		
		File file = new File(path); 
		  
		String line = null;
		
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        
        line = br.readLine().replace("}{", ",");
        
        fr.close();
        br.close();

        FileWriter fw = new FileWriter(new File(path.replace(".json", "2.json")));
        BufferedWriter out = new BufferedWriter(fw);
        out.write(line);
        out.flush();
        out.close();
        
        System.out.println("JSON file fixed!");
	}
	
	/**
	 * @param path
	 * @throws IOException
	 */
	public static void fixJsonFile2(String path) throws IOException {
		
		PrintWriter writer = new PrintWriter(new File(path.replace(".txt", "2.txt")));
		
		File file = new File(path); 
		  
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        
        String newLine = "";
        String st = null;
        boolean replace = false;
        
        while ((st = br.readLine()) != null) {
        	
        	if(replace)
        		newLine = st.replaceAll("^\\{", "");
        	else
        		newLine = st;
        	
        	if(st.matches(".*}@$")) {
        		newLine = newLine.replaceAll("}@$", ",");
        		replace = true;
        	}
        	else
        		replace = false;
        	
        	writer.println(newLine);
        } 
        
        fr.close();
        br.close();

        writer.close();
        
        System.out.println("JSON file fixed!");
	}
}
