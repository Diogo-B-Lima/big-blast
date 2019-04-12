package pt.uminho.ceb.biosystems.tools.blast;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.biojava.nbio.core.sequence.template.AbstractSequence;

import pt.uminho.ceb.biosystems.merlin.local.alignments.core.ModelMerge.BlastAlignment;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.AlignmentPurpose;
import pt.uminho.ceb.biosystems.merlin.utilities.blast.ncbi_blastparser.BlastOutput;
import pt.uminho.ceb.biosystems.merlin.utilities.blast.ncbi_blastparser.NcbiBlastParser;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.AlignmentCapsule;

public class Blast extends BlastAlignment{
	
	private String blastProgram = "";
	private String evalue = "";
	
	public Blast(String blastProgram, String queryFasta, String subjectFasta, String evalue,
			Map<String,AbstractSequence<?>> querySequences, double threshold, 
			ConcurrentLinkedQueue<AlignmentCapsule> alignmentContainerSet) throws JAXBException {
		
		super(queryFasta, subjectFasta, querySequences, threshold, false, new AtomicBoolean(false), alignmentContainerSet, JAXBContext.newInstance(BlastOutput.class));
		
		this.blastProgram = blastProgram;
		this.evalue = evalue;
		
		this.setBlastPurpose(AlignmentPurpose.OTHER);
		
		this.setEvalueThreshold(1);
		this.setBitScoreThreshold(0);
		this.setQueryCoverageThreshold(0.0);
		this.setTargetCoverageThreshold(0.0);
		this.setAlignmentMinScore(0);
		
	}
	
	@Override
	public void run(){

		if(!this.cancel.get()) {

			try {
				
				File queryFile = new File(queryFasta);

				String outputFileName = queryFasta.substring(queryFasta.lastIndexOf("/")).replace(".faa", "").concat("_blastReport.xml");
				if(isTransportersSearch)
					outputFileName = outputFileName.replace(".xml", "_transporters.xml");
				
				File outputFile;
				
				if(this.blastOutputFolderPath!=null && !this.blastOutputFolderPath.isEmpty()){
					outputFile = new File(this.blastOutputFolderPath.concat(outputFileName));
				}
				else{
					outputFile = new File(queryFile.getParent().concat("reports").concat(outputFileName));
//					outputFile = new File(tcdbfile.getParent().concat("\\..\\").concat("reports").concat(outputFileName));
				}
				
				outputFile.getParentFile().mkdirs();
				
				System.out.println(this.blastProgram + " -query " + this.queryFasta + " -subject " 
						+ this.subjectFasta + " " + this.evalue + " -out " + outputFile.getAbsolutePath() + " -outfmt 5");
				
				Process blastProcess = Runtime.getRuntime().exec(this.blastProgram + " -query " + this.queryFasta + " -subject " 
						+ this.subjectFasta + " " + this.evalue + " -out " + outputFile.getAbsolutePath() + " -outfmt 5");

				int exitValue = blastProcess.waitFor();
				
				if (exitValue != 0) {
					logger.warn("Abnormal process termination");
				}
				else{
					logger.info("BLAST search completed with success!");
				}
				
				blastProcess.destroy();
				
				System.out.println(outputFile.getAbsolutePath());
				
				if(outputFile.exists()){
				
					this.blout = new NcbiBlastParser(outputFile, this.jc);
					this.alignmentMatrix = blout.getMatrix();

					buildAlignmentCapsules();
				}
				else{
					
					logger.warn("blast output .xml file wasn't generated on {}", outputFile.getAbsolutePath());
				}
				

			} catch (IOException | InterruptedException e) {

				e.printStackTrace();
			}
			catch (OutOfMemoryError oue) {

				oue.printStackTrace();
			}
			
			System.gc();

			setChanged();
			notifyObservers();
		}

		setChanged();
		notifyObservers();
	}
	
}
