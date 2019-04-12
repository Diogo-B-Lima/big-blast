package pt.uminho.ceb.biosystems.tools.blast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.JAXBException;

import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.template.AbstractSequence;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.local.alignments.core.ModelMerge.ModelAlignments;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.AlignmentCapsule;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final int LIMIT = 500;
	private static final String FINAL_RESULTS = "finalResults.json";

	public static void main(String[] args ){
		
//		args = new String[] {"-b", "tblastn", "-q", "/home/1- Chlamydomonas/GCF_000002595.1_C.reinhardtii_v3.0_protein.faa", "-s", "/home/Haematococcus pluvialis_fasta/GCA_003970955.1_H.pluvialis_ASM397095v1_genomic.fna", "-w", "/home/", "-r", "coiso.json", "-e", "1", "-p", "500"};
		
		String query = "";
		String subject = "";
		String blastProgram = "";
		String evalue = "";
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
			
			if(args.length > 9) {

				for(int i = 0; i < args.length; i++) {
					
					try {
						if(args[i].equals("-p")) {
							i++;
							newLimit = Integer.valueOf(args[i]);
						}

						else if(args[i].equals("-e")) {
							i++;
							evalue = "-evalue ".concat(args[i]);
						}
						else if(args[i].equals("-r")) {
							i++;
							resultsFileName = args[i];
						}
					} 
					catch (NumberFormatException e) {
						newLimit = LIMIT;
						System.out.println("-p command not accepted! Default value of " + LIMIT + " assumed!");
					}
					catch (Exception e) {
						newLimit = LIMIT;
						resultsFileName = FINAL_RESULTS;
						evalue = "";
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
			success = runBlast(blastProgram, query, subject, evalue, newLimit, workDirectory, resultsFileName);
		} 
		catch (InterruptedException e) {
			System.out.println("An error occurred while running!!");
			e.printStackTrace();
			System.exit(3);
		}
		
		if(success)
			System.out.println("Done! Blast complete!");
		else
			System.out.println("Something went wrong! Please try again!");
		
	}

	

	private static boolean runBlast(String blastProgram, String query, String subject, String evalue, int sequencesLimit, String workdir, String resultsFileName) throws InterruptedException {

//		String subFasta = workdir.concat("/newTemporaryFasta.faa");

		try {
			
			PrintWriter writer = new PrintWriter(resultsFileName);	//clean the file if already exists
			writer.print("");
			writer.close();

			System.out.println("query: " + query);
			System.out.println("subject: " + subject);
			
			ConcurrentHashMap<String, AbstractSequence<?>> querySequences= new ConcurrentHashMap<String, AbstractSequence<?>>();
			querySequences.putAll(FastaReaderHelper.readFastaProteinSequence(new File(query)));
			
			int totalBlasts = querySequences.size() / sequencesLimit;
			
			int count = 1;
			
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
				
				
				List<Thread> threads = new ArrayList<Thread>();
				//			ConcurrentLinkedQueue<String> queryArray = new ConcurrentLinkedQueue<String>(this.querySequences.keySet());
				int numberOfCores = Runtime.getRuntime().availableProcessors();
				//int numberOfCores = new Double(Runtime.getRuntime().availableProcessors()*1.5).intValue();

				if(subQuerySequences.keySet().size()<numberOfCores)
					numberOfCores=subQuerySequences.keySet().size();
				
				String path = workdir.concat("/queryBlast");
				
				File f = new File (path);
				if(!f.exists())
					f.mkdir();
				
				//			this.querySize.set(new Integer(this.querySequences.size()));
				//			setChanged();
				//			notifyObservers();

				//Distribute querySequences into fastaFiles

				//			logger.debug("Writting query sequences temporary fasta files... ");
				System.out.println("Writting query sequences temporary fasta files... ");

				List<String> queryFilesPaths = new ArrayList<>();
				List<Map<String,AbstractSequence<?>>> queriesSubSetList = new ArrayList<>();

				CreateGenomeFile.buildSubFastaFiles(workdir, subQuerySequences, queriesSubSetList, queryFilesPaths, numberOfCores);

				ConcurrentLinkedQueue<AlignmentCapsule> alignmentContainerSet = new ConcurrentLinkedQueue<>();

				for(int i=0; i<numberOfCores; i++) {

					ModelAlignments blastAlign;

					blastAlign= new Blast(blastProgram, queryFilesPaths.get(i), subject, evalue, queriesSubSetList.get(i), 0.0, alignmentContainerSet);
					
					Thread thread = new Thread(blastAlign);
					threads.add(thread);
					thread.start();
				}

				for(Thread thread :threads)
					thread.join();


				ExportResults.exportToJSON(alignmentContainerSet, resultsFileName);
				
				count++;
			}

			fixJsonFile(resultsFileName);

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

        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write(line);
        out.flush();
        out.close();
        
        System.out.println("JSON file fixed!");
	}
}
