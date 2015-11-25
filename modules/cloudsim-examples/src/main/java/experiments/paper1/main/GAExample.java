package experiments.paper1.main;

import org.cloudbus.cloudsim.Log;

import experiments.paper1.ga.GATaskScheduling;

public class GAExample {
	public static void main(String[] args) {
		try {
			RealtimeHelper.initLogOutput(
					RealtimeConstants.ENABLE_OUTPUT,
					RealtimeConstants.OUTPUT_TO_FILE,
					RealtimeConstants.OutputFolder,
					RealtimeConstants.VmAllocationPolicy,
					RealtimeConstants.VmSelectionPolicy,
					RealtimeConstants.Parameter);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
		
		Log.printLine("Starting GAExample...");
		try {
			GATaskScheduling example = new GATaskScheduling(
					RealtimeConstants.CHROMSOME_DIM, // chromosomeDim
					RealtimeConstants.POPULATION_DIM, // populationDim
					RealtimeConstants.CROSSOVER_PROB, // crossoverProb
					RealtimeConstants.RANDOM_SELECTION_CHANCE, // randomSelectionChance
					RealtimeConstants.MAX_GENERATIONS, // maxGenerations
					RealtimeConstants.numPrelimRuns, // numPrelimRuns
				    RealtimeConstants.maxPrelimGenerations, // maxPrelimGenerations
					RealtimeConstants.MUTATION_PROB, // mutationProb
					RealtimeConstants.CROSSOVER_TYPE, // crossoverType
					RealtimeConstants.COMPUTE_STATISTICS); // computeStatistics
			example.evolve();
			// Thread taskScheduling = new Thread(example);
			// taskScheduling.start();
			Log.printLine("GAExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}