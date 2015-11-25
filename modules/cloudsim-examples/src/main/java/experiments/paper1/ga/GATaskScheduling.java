package experiments.paper1.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import experiments.paper1.main.RealtimeConstants;
import experiments.paper1.main.RealtimeDatacenterBroker;
import experiments.paper1.main.RealtimeHelper;

public class GATaskScheduling extends GA {

	/** The broker. */
	protected static RealtimeDatacenterBroker broker;
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;
	/** The host list. */
	protected static List<PowerHost> hostList;
	/** The datacenter. */
	private static PowerDatacenter datacenter;
	
	private OutputStream os;
	
	List<Chromosome> pareto;
	
	public GATaskScheduling(int chromosomeDim, int populationDim, double crossoverProb, int randomSelectionChance, int maxGenerations, int numPrelimRuns, int maxPrelimGenerations, double mutationProb, int crossoverType, boolean computeStatistics) {
		super(chromosomeDim, populationDim, crossoverProb, randomSelectionChance, maxGenerations, numPrelimRuns, maxPrelimGenerations, mutationProb, crossoverType, computeStatistics);
		
		File outputFile = new File("/home/hepsilion/Workspace/Temp/ga_result.txt");
		try {
			os = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<populationDim; i++) {
			this.chromosomes[i] = new ChromTaskScheduling(chromosomeDim);
			this.chromNextGen[i] = new ChromTaskScheduling(chromosomeDim);
			this.prelimChrom[i] = new ChromTaskScheduling(chromosomeDim);
			
			ChromTaskScheduling cs = ((ChromTaskScheduling)chromosomes[i]);
			ChromTaskScheduling cn = ((ChromTaskScheduling)chromNextGen[i]);
			ChromTaskScheduling pc = ((ChromTaskScheduling)prelimChrom[i]);
			for(int j=0; j<chromosomeDim; j++) {
				cs.genes[j] = new GeneTaskScheduling(j, 0, 3);
				cn.genes[j] = new GeneTaskScheduling(j, 0, 3);
				pc.genes[j] = new GeneTaskScheduling(j, 0, 3);
			}
		}
		initPopulation();
	}
	
	@Override
	protected void initPopulation() {
		for(int i=0; i<populationDim; i++) {
			//random hosts for vms
			int[] rn = RealtimeHelper.getRandomIntegers(chromosomeDim, 0, RealtimeConstants.NUMBER_OF_HOSTS-1);
			//random frequency level for vms
			int[] fl = RealtimeHelper.getRandomIntegers(chromosomeDim, 0, 4);
			
			ChromTaskScheduling cs = ((ChromTaskScheduling)chromosomes[i]);
			for(int j=0; j<chromosomeDim; j++) {
				cs.getGene(j).setVm(j);
				cs.getGene(j).setHost(rn[j]);
				cs.getGene(j).setFrequency(fl[j]);
				cs.fitness = 0.0;
			}
		}
	}
	
	protected void doRandomMutation_origional(int iChromIndex) {
		int geneIndex = getRandom(chromosomeDim);
		int vmId = ((ChromTaskScheduling)this.chromosomes[iChromIndex]).getGene(geneIndex).getVm();
		int hostId = RealtimeHelper.getRandomInteger(0, RealtimeConstants.NUMBER_OF_HOSTS-1);
        int frequency = RealtimeHelper.getRandomInteger(0, 4);
		GeneTaskScheduling gene = new GeneTaskScheduling(vmId, hostId, frequency);

        setGeneValue(iChromIndex, geneIndex, gene);
        this.chromosomes[iChromIndex].fitness = 0.0;
	}
	
	//TODO 暂时替换成这个
	protected void doRandomMutation(int iChromIndex) {
        for (int i=0; i < chromosomeDim; i++) {
            if (getRandom(100) > 50) {
                int vmId = ((ChromTaskScheduling)this.chromosomes[iChromIndex]).getGene(i).getVm();
                int hostId = RealtimeHelper.getRandomInteger(0, RealtimeConstants.NUMBER_OF_HOSTS-1);
                int frequency = RealtimeHelper.getRandomInteger(0, 4);
                GeneTaskScheduling gene = new GeneTaskScheduling(vmId, hostId, frequency);

                setGeneValue(iChromIndex, i, gene);
            }
        }
        this.chromosomes[iChromIndex].fitness = 0.0;
    }
	
	/**
     * Sets the value of a gene for the given chromosome at the given geneIndex
     * @param iChromIndex
     * @param geneIndex
     * @param gene
     */
    private void setGeneValue(int iChromIndex, int geneIndex, GeneTaskScheduling gene) {
    	ChromTaskScheduling chromTaskScheduling = ((ChromTaskScheduling)this.chromosomes[iChromIndex]);
    	chromTaskScheduling.setGene(gene, geneIndex);
    }

	@Override
	protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		int iCrossoverPoint = getRandom(chromosomeDim-2);
		GeneTaskScheduling gene1 = ((ChromTaskScheduling)Chrom1).getGene(iCrossoverPoint);
		GeneTaskScheduling gene2 = ((ChromTaskScheduling)Chrom2).getGene(iCrossoverPoint);

                // CREATE OFFSPRING ONE
        ((ChromTaskScheduling)Chrom1).setGene(gene2, iCrossoverPoint);
                // CREATE OFFSPRING TWO
        ((ChromTaskScheduling)Chrom2).setGene(gene1, iCrossoverPoint);
        Chrom1.fitness = 0.0;
        Chrom2.fitness = 0.0;
	}

	//@Override
	protected void doTwoPtCrossover1(Chromosome Chrom1, Chromosome Chrom2) {
		int iCrossoverPoint1, iCrossoverPoint2;

        iCrossoverPoint1 = 1 + getRandom(chromosomeDim-2);
        iCrossoverPoint2 = iCrossoverPoint1 + 1 + getRandom(chromosomeDim-iCrossoverPoint1-1);

        if (iCrossoverPoint2 == (iCrossoverPoint1+1)) {
            doOnePtCrossover(Chrom1, Chrom2);
        } else {
        	GeneTaskScheduling gene1_Chrom1 = ((ChromTaskScheduling)Chrom1).getGene(iCrossoverPoint1);
        	GeneTaskScheduling gene1_Chrom2 = ((ChromTaskScheduling)Chrom2).getGene(iCrossoverPoint1);
        	GeneTaskScheduling gene2_Chrom1 = ((ChromTaskScheduling)Chrom1).getGene(iCrossoverPoint2);
        	GeneTaskScheduling gene2_Chrom2 = ((ChromTaskScheduling)Chrom2).getGene(iCrossoverPoint2);

                // CREATE OFFSPRING ONE
            ((ChromTaskScheduling)Chrom1).setGene(gene1_Chrom2, iCrossoverPoint1);
            ((ChromTaskScheduling)Chrom1).setGene(gene2_Chrom2, iCrossoverPoint2);
        
                // CREATE OFFSPRING TWO
            ((ChromTaskScheduling)Chrom2).setGene(gene1_Chrom1, iCrossoverPoint1);
            ((ChromTaskScheduling)Chrom2).setGene(gene2_Chrom1, iCrossoverPoint2);
        }
        Chrom1.fitness = 0.0;
        Chrom2.fitness = 0.0;
	}
	
	protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		int iCrossoverPoint1, iCrossoverPoint2;

        iCrossoverPoint1 = 1 + getRandom(chromosomeDim-2);
        iCrossoverPoint2 = iCrossoverPoint1 + 1 + getRandom(chromosomeDim-iCrossoverPoint1-1);
        
        if (iCrossoverPoint2 == (iCrossoverPoint1+1)) {
            doOnePtCrossover(Chrom1, Chrom2);
        } else {
        	GeneTaskScheduling gene_Chrom1, gene_Chrom2;
        	for(int i=iCrossoverPoint1; i<=iCrossoverPoint2; i++) {
        		gene_Chrom2 = ((ChromTaskScheduling)Chrom2).getGene(i);
        		GeneTaskScheduling temp_gene_Chrom2 = new GeneTaskScheduling(gene_Chrom2.getVm(), gene_Chrom2.getHost(), gene_Chrom2.getFrequency());
        		
        		gene_Chrom1 = ((ChromTaskScheduling)Chrom1).getGene(i);
        		GeneTaskScheduling temp_gene_Chrom1 = new GeneTaskScheduling(gene_Chrom1.getVm(), gene_Chrom1.getHost(), gene_Chrom1.getFrequency());
        		((ChromTaskScheduling)Chrom2).setGene(temp_gene_Chrom1, i);
        		
        		((ChromTaskScheduling)Chrom1).setGene(temp_gene_Chrom2, i);
        	}
        }
        Chrom1.fitness = 0.0;
        Chrom2.fitness = 0.0;
	}

	@Override
	protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		int iGeneToSwap;

        for (int i=0; i < chromosomeDim; i++) {
            if (getRandom(100) > 50) {
                iGeneToSwap = getRandom(chromosomeDim);
                GeneTaskScheduling gene1 = ((ChromTaskScheduling)Chrom1).getGene(iGeneToSwap);
                GeneTaskScheduling gene2 = ((ChromTaskScheduling)Chrom2).getGene(iGeneToSwap);
            
                ((ChromTaskScheduling)Chrom1).setGene(gene2, iGeneToSwap);
                ((ChromTaskScheduling)Chrom2).setGene(gene1, iGeneToSwap);
            }
        }
        Chrom1.fitness = 0.0;
        Chrom2.fitness = 0.0;
	}
	
	@Override
	public int evolve() {
		int iGen;
        int iPrelimChrom, iPrelimChromToUsePerRun;

        String startTime = new Date().toString();
        OutputStream origional_output = Log.getOutput();
        
        // 预处理，生成初始种群
        if (numPrelimRuns > 0){
            iPrelimChrom = 0;
            //number of fittest prelim chromosomes to use with final run
            iPrelimChromToUsePerRun = populationDim / numPrelimRuns;

            for (int iPrelimRuns = 1; iPrelimRuns <= numPrelimRuns; iPrelimRuns++) {
                initPopulation();

                //create a somewhat fit chromosome population for this prelim run
                iGen = 0;
                while (iGen < maxPrelimGenerations) {
                    Log.printLine(iPrelimRuns + " of " + numPrelimRuns + " prelim runs --> " + (iGen + 1) + " of " + maxPrelimGenerations + " generations");

                    computeFitnessRankings(); //计算适应度值及排序
                    doGeneticMating(); //选择、交叉
                    copyNextGenToThisGen(); //遗传

                    if (computeStatistics == true) {
                        this.genAvgDeviation[iGen] = getAvgDeviationAmongChroms();
                        this.genAvgFitness[iGen] = getAvgFitness();
                    }
                    iGen++;
                }

                computeFitnessRankings();

                //copy these somewhat fit chromosomes to the main chromosome pool
                int iNumPrelimSaved = 0;
                for (int i = 0; i < populationDim && iNumPrelimSaved < iPrelimChromToUsePerRun; i++){
                    if(this.chromosomes[i].fitnessRank >= populationDim - iPrelimChromToUsePerRun) {
                        this.prelimChrom[iPrelimChrom + iNumPrelimSaved].copyChromGenes(this.chromosomes[i]);
                        iNumPrelimSaved++;
                    }
                }
                iPrelimChrom += iNumPrelimSaved;
            }
            for (int i = 0; i < iPrelimChrom; i++)
                this.chromosomes[i].copyChromGenes(this.prelimChrom[i]);
            Log.printLine("INITIAL POPULATION AFTER PRELIM RUNS:");
        } else {
            Log.printLine("INITIAL POPULATION (NO PRELIM RUNS):");
        }
        //Add Preliminary Chromosomes to list box
        addChromosomesToLog(0, 10);

        // 正式处理
        iGen = 0;
        while (iGen < maxGenerations){
            Log.setOutput(os);
            Log.printLine("****************************************************************************************");
            Log.printLine("**************************" + iGen + "th Generation Chromsome**************************");
            System.out.println("**************************" + iGen + "th Generation Chromsome**************************");
            Log.printLine("****************************************************************************************");
            Log.setOutput(origional_output);
            computeFitnessRankings();//计算适应度值排序
            doGeneticMating();//选择，交叉
            copyNextGenToThisGen();//变异

            if (computeStatistics == true) {
                this.genAvgDeviation[iGen] = getAvgDeviationAmongChroms();
                this.genAvgFitness[iGen] = getAvgFitness();
            }
            iGen++;
        }

        Log.printLine("GEN " + (iGen + 1) + " AVG FITNESS = " + this.genAvgFitness[iGen-1] + " AVG DEV = " + this.genAvgDeviation[iGen-1]);
        addChromosomesToLog(iGen, 10); //display Chromosomes to system.out

        computeFitnessRankings();
		Log.setOutput(os);
        Log.printLine("Best Chromosome Found: " + this.bestFitnessChromIndex + "th chromosome");
        Log.printLine(this.chromosomes[this.bestFitnessChromIndex].getGenesAsStr());
        Log.printLine("Fitness= " + this.chromosomes[this.bestFitnessChromIndex].fitness*(-1));
        Log.printLine(startTime);
        Log.printLine("GA end time: " + new Date().toString());
        Log.setOutput(origional_output);
        return (iGen);
	}

	public double simulation(int iChromIndex) {
		double sla = 0.0;
		
		Log.printLine(iChromIndex+ "th Simulation started!");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);
	
			broker = (RealtimeDatacenterBroker) RealtimeHelper.createBroker();
			int brokerId = broker.getId();
	
			cloudletList = RealtimeHelper.createRealtimeCloudlet(brokerId, RealtimeConstants.NUMBER_OF_CLOUDLETS);
			vmlist = RealtimeHelper.createVmList(brokerId, cloudletList.size(), this.chromosomes[iChromIndex]);
			hostList = RealtimeHelper.createHostList(RealtimeConstants.NUMBER_OF_HOSTS);
	
			datacenter = (PowerDatacenter) RealtimeHelper.createDatacenter("Datacenter", GADatacenter.class, hostList);
			datacenter.setDisableMigrations(true);
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.terminateSimulation(RealtimeConstants.SIMULATION_LIMIT);
			
			double lastClock = CloudSim.startSimulation();
			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
			Log.printLine("Received " + received_cloudlets.size() + " cloudlets");
			System.out.println("Received " + received_cloudlets.size() + " cloudlets");
			CloudSim.stopSimulation();

			OutputStream origional_output = Log.getOutput();
			Log.setOutput(os);
			Log.printLine();
			Log.printLine("--------"+iChromIndex + "th Chromsome Simulation Result: --------");
			Log.printLine(this.chromosomes[iChromIndex].getGenesAsStr());
			sla = RealtimeHelper.printResults(datacenter, vmlist, cloudletList, received_cloudlets, lastClock, RealtimeConstants.OUTPUT_CSV);
			Log.printLine("Fitness = " + sla);
			Log.setOutput(origional_output);
			
			Log.printLine(iChromIndex + "th Simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
		
		return sla;
	}
	
	@Override
	protected double getFitness(int iChromIndex) {
		return simulation(iChromIndex);
	}
}
