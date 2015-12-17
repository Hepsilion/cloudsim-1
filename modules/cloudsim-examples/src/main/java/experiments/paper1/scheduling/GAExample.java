package experiments.paper1.scheduling;

import java.io.File;
import java.io.IOException;

import org.cloudbus.cloudsim.Log;

public class GAExample {
	public static void main(String[] args) {
		int numExes = 0;
        for(int i=0; i<=numExes; i++) {
        	int temp_numCloudlets = RealtimeConstants.NUMBER_OF_CLOUDLETS + i*10;
        	System.out.println("#Cloudlet="+temp_numCloudlets);
        	String resultFile = "ga_result_"+temp_numCloudlets;
    		File result = new File(RealtimeConstants.OutputFolder + "/result/" + resultFile + ".txt");
    		try {
    			result.createNewFile();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
    		
        	String logFile = "ga_"+temp_numCloudlets;
    		try {
    			RealtimeHelper.initLogOutput(
    					RealtimeConstants.ENABLE_OUTPUT,
    					RealtimeConstants.OUTPUT_TO_FILE,
    					RealtimeConstants.OutputFolder,
    					logFile,
    					RealtimeConstants.VmAllocationPolicy,
    					RealtimeConstants.VmSelectionPolicy,
    					RealtimeConstants.Parameter);
    		} catch (Exception e) {
    			e.printStackTrace();
    			System.exit(0);
    		} 
    		
    		Log.printLine("Starting GAExample...");
    		try {
    			SchedulingGA example = new SchedulingGA(
    					temp_numCloudlets, 							// chromosomeDim
    					RealtimeConstants.POPULATION_DIM, 			// populationDim
    					RealtimeConstants.CROSSOVER_PROB, 			// crossoverProb
    					RealtimeConstants.RANDOM_SELECTION_CHANCE, 	// randomSelectionChance
    					RealtimeConstants.MAX_GENERATIONS, 			// maxGenerations
    					RealtimeConstants.numPrelimRuns, 			// numPrelimRuns
    				    RealtimeConstants.maxPrelimGenerations, 	// maxPrelimGenerations
    					RealtimeConstants.MUTATION_PROB, 			// mutationProb
    					RealtimeConstants.CROSSOVER_TYPE,			// crossoverType
    					RealtimeConstants.COMPUTE_STATISTICS); 		// computeStatistics
    			example.evolve();
    			Log.printLine("GAExample finished!");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
	}
}