package experiments.paper1.scheduling;

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

public class SchedulingGA extends GA {
	private OutputStream ga_log;
	private OutputStream result_log;
	
	public SchedulingGA(int chromosomeDim, int populationDim, double crossoverProb, int randomSelectionChance, int maxGenerations, int numPrelimRuns, int maxPrelimGenerations, double mutationProb, int crossoverType, boolean computeStatistics) {
		super(chromosomeDim, populationDim, crossoverProb, randomSelectionChance, maxGenerations, numPrelimRuns, maxPrelimGenerations, mutationProb, crossoverType, computeStatistics);
		
		String logFile = "ga_"+chromosomeDim;
		String resultFile = "ga_result_"+chromosomeDim;
		try {
			ga_log = new FileOutputStream(RealtimeConstants.OutputFolder+"/result/" + logFile + ".txt");
			result_log = new FileOutputStream(RealtimeConstants.OutputFolder+"/result/" + resultFile + ".txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<populationDim; i++) {
			this.chromosomes[i] = new SchedulingChromosome(chromosomeDim);
			this.chromNextGen[i] = new SchedulingChromosome(chromosomeDim);
			this.prelimChrom[i] = new SchedulingChromosome(chromosomeDim);
			
			SchedulingChromosome cs = ((SchedulingChromosome)chromosomes[i]);
			SchedulingChromosome cn = ((SchedulingChromosome)chromNextGen[i]);
			SchedulingChromosome pc = ((SchedulingChromosome)prelimChrom[i]);
			for(int j=0; j<chromosomeDim; j++) {
				cs.genes[j] = new SchedulingGene(j, 0);
				cn.genes[j] = new SchedulingGene(j, 0);
				pc.genes[j] = new SchedulingGene(j, 0);
			}
		}
		initPopulation();
	}
	
	protected void initPopulation() {
		double[] result = this.getGaFitness((SchedulingChromosome)(this.chromosomes[0]));
		((SchedulingChromosome)(this.chromosomes[0])).tdr=result[0];
		((SchedulingChromosome)(this.chromosomes[0])).dmr=result[1];
		((SchedulingChromosome)(this.chromosomes[0])).energy=result[2];
		((SchedulingChromosome)(this.chromosomes[0])).fitness=result[3];
		
		for(int i=1; i<populationDim; i++) {
			//random hosts for vms
			int[] rn = RealtimeHelper.getRandomHosts(chromosomeDim, 0, RealtimeConstants.NUMBER_OF_HOSTS-1);
			
			SchedulingChromosome cs = ((SchedulingChromosome)chromosomes[i]);
			for(int j=0; j<chromosomeDim; j++) {
				cs.getGene(j).setVm(j);
				cs.getGene(j).setHost(rn[j]);
				cs.fitness = 0.0;
			}
		}
	}
	
	/**
     * Sets the value of a gene for the given chromosome at the given geneIndex
     * @param iChromIndex
     * @param geneIndex
     * @param gene
     */
    private void setGeneValue(int iChromIndex, int geneIndex, SchedulingGene gene) {
    	SchedulingChromosome chromTaskScheduling = ((SchedulingChromosome)this.chromosomes[iChromIndex]);
    	chromTaskScheduling.setGene(gene, geneIndex);
    }
    
	@Override
	public int evolve() {
		int iGen = 0;
        String startTime = new Date().toString();
        OutputStream origional_output = Log.getOutput();
        while(stop() && iGen < maxGenerations){
            Log.setOutput(ga_log);
            Log.printLine("****************************************************************************************");
            Log.printLine("                               "+iGen+"th Generation Chromsome                          ");
            Log.printLine("****************************************************************************************");
            Log.setOutput(result_log);
            Log.printLine("****************************************************************************************");
            Log.printLine("                               "+iGen+"th Generation Chromsome                          ");
            System.out.println("                               "+iGen+"th Generation Chromsome                          ");
            Log.printLine("****************************************************************************************");
            Log.setOutput(origional_output);
            
            computeFitnessRankings();//计算适应度值排序
            Log.setOutput(result_log);
            for(int i=0; i<populationDim; i++) {
            	SchedulingChromosome chrom = (SchedulingChromosome) this.chromosomes[i];
            	Log.printLine(chrom.fitness+" "+chrom.tdr+"	"+chrom.dmr+"	"+chrom.energy);
            }
            Log.setOutput(origional_output);
            
            doGeneticMating();//选择，交叉
            copyNextGenToThisGen();//变异
            iGen++;
        }       
        
        Log.setOutput(result_log);
		Log.printLine("****************************************************************************************");
        Log.printLine("                               "+iGen+"th Generation Chromsome                          ");
        System.out.println("                               "+iGen+"th Generation Chromsome                          ");
        Log.printLine("****************************************************************************************");
        Log.setOutput(origional_output);
        
        computeFitnessRankings();
        
        Log.setOutput(result_log);
        for(int i=0; i<populationDim; i++) {
        	SchedulingChromosome chrom = (SchedulingChromosome) this.chromosomes[i];
        	Log.printLine(chrom.fitness+" "+chrom.tdr+"	"+chrom.dmr+"	"+chrom.energy);
        }
		Log.printLine("######################################Final Result######################################");
		Log.printLine("GA start  time: " + startTime);
        Log.printLine("GA finish time: " + new Date().toString());
        Log.printLine("Best Chromosome Found: " + this.bestFitnessChromIndex + "th chromosome");
        Log.printLine(this.chromosomes[this.bestFitnessChromIndex].getGenesAsStr());
        Log.printLine("Fitness= " + this.chromosomes[this.bestFitnessChromIndex].fitness);
        Log.printLine("Task Declined Rate  = " + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).tdr);
        Log.printLine("Deadline Missing Rate = " + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).dmr);
        Log.printLine("Energy = " + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).energy);
        Log.printLine("######################################Final Result######################################");
        Log.setOutput(ga_log);
		Log.printLine("######################################Final Result######################################");
		Log.printLine("GA start  time: " + startTime);
        Log.printLine("GA finish time: " + new Date().toString());
        Log.printLine("Best Chromosome Found: " + this.bestFitnessChromIndex + "th chromosome");
        Log.printLine(this.chromosomes[this.bestFitnessChromIndex].getGenesAsStr());
        Log.printLine("Fitness= " + this.chromosomes[this.bestFitnessChromIndex].fitness);
        Log.printLine(" Task Declined Rate  =" + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).tdr);
        Log.printLine("Deadline Missing Rate=" + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).dmr);
        Log.printLine("       Energy        =" + ((SchedulingChromosome)(this.chromosomes[this.bestFitnessChromIndex])).energy);
        Log.printLine("######################################Final Result######################################");
        Log.setOutput(origional_output);
        
        return (iGen);
	}

	boolean stop() {
		boolean result = true;
		for(int i=1; i<this.populationDim; i++) {
			result = result && (this.chromosomes[0].fitness==this.chromosomes[i].fitness);
		}
		return !result;
	}
	
	void doGeneticMating() {
		int iCnt = 0;
		Chromosome Chrom1, Chrom2;

//		this.chromNextGen[iCnt++].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
//		this.chromNextGen[iCnt++].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
//		for(int i=0; i<=1; i++) {
//			for(int j=0; j<this.populationDim; j++) {
//				if(this.chromosomes[j].fitnessRank==this.chromosomes[this.bestFitnessChromIndex].fitnessRank-i)
//					this.chromNextGen[iCnt++].copyChromGenes(this.chromosomes[j]);
//			}
//		}
		
		Chrom1 = new SchedulingChromosome(chromosomeDim);
		Chrom2 = new SchedulingChromosome(chromosomeDim);
		
		this.chromNextGen[iCnt++].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
		Chrom1.copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
		for(int i=0; i<this.populationDim; i++) {
			if(i!=this.bestFitnessChromIndex) {
				Chrom2.copyChromGenes(this.chromosomes[i]);
				doUniformCrossover(Chrom1, Chrom2);
				this.chromNextGen[iCnt++].copyChromGenes(Chrom2);
			}
		}
		
//		do {
//			int indexes[] = { indexParent1, indexParent2 };
//			selectTwoParents(indexes);
//			indexParent1 = indexes[0];
//			indexParent2 = indexes[1];
			
//			Random random = new Random();
//			do{
//				indexParent1=random.nextInt(this.populationDim-1)%(this.populationDim);
//			}while(this.bestFitnessChromIndex==indexParent1);
//			do{
//				indexParent2=random.nextInt(this.populationDim-1)%(this.populationDim);
//			}while(this.bestFitnessChromIndex==indexParent2);

//			Chrom1.copyChromGenes(this.chromosomes[indexParent1]);
//			Chrom2.copyChromGenes(this.chromosomes[indexParent2]);
//			Chrom1.copyChromGenes(this.chromosomes[0]);
//			Chrom2.copyChromGenes(this.chromosomes[1]);
//			if(Chrom1.fitness==Chrom2.fitness)
//				continue;
//			
//			// do crossover
//			doUniformCrossover(Chrom1, Chrom2);
//			this.chromNextGen[iCnt++].copyChromGenes(Chrom1);
//			this.chromNextGen[iCnt++].copyChromGenes(Chrom2);
//		} while (iCnt < populationDim-1);
	}
	
	@Override
	protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		int iGeneToSwap;
		boolean crossovered = false;
		
		Chromosome old1 = new SchedulingChromosome(chromosomeDim);
		Chromosome old2 = new SchedulingChromosome(chromosomeDim);
		old1.copyChromGenes(Chrom1);
		old2.copyChromGenes(Chrom2);

        for (int i=0; i < chromosomeDim; i++) {
            if (getRandom(1.0) < this.crossoverProb) {
                iGeneToSwap = getRandom(chromosomeDim);
                SchedulingGene gene1 = ((SchedulingChromosome)Chrom1).getGene(iGeneToSwap);
                SchedulingGene gene2 = ((SchedulingChromosome)Chrom2).getGene(iGeneToSwap);
            
                ((SchedulingChromosome)Chrom1).setGene(gene2, iGeneToSwap);
                ((SchedulingChromosome)Chrom2).setGene(gene1, iGeneToSwap);
                
                crossovered = true;
            }
        }
        if(crossovered){
        	double[] result;
        	result = simulation(((SchedulingChromosome)Chrom1));
        	((SchedulingChromosome)Chrom1).tdr=result[0];
        	((SchedulingChromosome)Chrom1).dmr=result[1];
        	((SchedulingChromosome)Chrom1).energy=result[2];
        	Chrom1.fitness=result[3];
        	
        	result = simulation(((SchedulingChromosome)Chrom2));
        	((SchedulingChromosome)Chrom2).tdr=result[0];
        	((SchedulingChromosome)Chrom2).dmr=result[1];
        	((SchedulingChromosome)Chrom2).energy=result[2];
        	Chrom2.fitness=result[3];
        }
        if(Chrom1.fitness<old1.fitness)
        	Chrom1.copyChromGenes(old1);
        if(Chrom2.fitness<old2.fitness)
        	Chrom2.copyChromGenes(old2);
	}
	
	protected void doRandomMutation2(int iChromIndex) {
		int geneIndex = getRandom(chromosomeDim);
		int vmId = ((SchedulingChromosome)this.chromosomes[iChromIndex]).getGene(geneIndex).getVm();
		int hostId = RealtimeHelper.getRandomInteger(0, RealtimeConstants.NUMBER_OF_HOSTS-1);
		SchedulingGene gene = new SchedulingGene(vmId, hostId);

        setGeneValue(iChromIndex, geneIndex, gene);
        this.chromosomes[iChromIndex].fitness = 0.0;
	}
	
	//TODO 暂时替换成这个
	protected void doRandomMutation(int iChromIndex) {
		SchedulingChromosome chrom = (SchedulingChromosome)this.chromosomes[iChromIndex];
		SchedulingChromosome old = new SchedulingChromosome(chromosomeDim);
		old.copyChromGenes(chrom);
		boolean mutated = false;
        for (int i=0; i<chromosomeDim; i++) {
            if (getRandom(100)>75) {//TODO 暂时50->70
                int vmId = chrom.getGene(i).getVm();
                int hostId = RealtimeHelper.getRandomInteger(0, RealtimeConstants.NUMBER_OF_HOSTS-1);
                SchedulingGene gene = new SchedulingGene(vmId, hostId);

                setGeneValue(iChromIndex, i, gene);
                mutated = true;
            }
        }
        if(mutated){
        	double[] result = simulation(chrom);
        	chrom.tdr=result[0];
        	chrom.dmr=result[1];
        	chrom.energy=result[2];
        	chrom.fitness=result[3];
        	if(chrom.fitness<old.fitness)
        		chrom.copyChromGenes(old);
        }
    }
	
	void computeFitnessRankings() {
		for (int i=0; i<populationDim; i++) {
			if(this.chromosomes[i].fitness == 0.0){
				double[] ga_result=getGaFitness((SchedulingChromosome)(this.chromosomes[i]));
				this.chromosomes[i].fitness = ga_result[3];
				((SchedulingChromosome)(this.chromosomes[i])).tdr=ga_result[0];
				((SchedulingChromosome)(this.chromosomes[i])).dmr=ga_result[1];
				((SchedulingChromosome)(this.chromosomes[i])).energy=ga_result[2];
			}
		}

		for (int i=0; i<populationDim; i++)
			this.chromosomes[i].fitnessRank = getFitnessRank(this.chromosomes[i].fitness, i);
		
		for (int i=0; i<populationDim; i++) {
			if (this.chromosomes[i].fitnessRank == populationDim - 1) {
				this.bestFitnessChromIndex = i;
			}
			if (this.chromosomes[i].fitnessRank == 0) {
				this.worstFitnessChromIndex = i;
			}
		}
	}
	
	//TODO 修改原遗传算法中，存在相同元素时的错误
	int getFitnessRank(double fitness, int index) {
		int fitnessRank = -1;
		int preEqual=0;
		for (int i = 0; i < populationDim; i++) {
			if (fitness >= this.chromosomes[i].fitness) {
				if(fitness==this.chromosomes[i].fitness && i<index) {
					preEqual++;
				}
				fitnessRank++;
			}
		}
		fitnessRank-=preEqual;
		return fitnessRank;
	}
	
	protected double[] getGaFitness(SchedulingChromosome chrom) {
		return simulation(chrom);
	}

	@Override
	protected double getFitness(int iChromIndex) {
		//return simulation(iChromIndex);
		return 0;
	}

	public double[] simulation(SchedulingChromosome chrom) {
		double[] ga_result = new double[4];
		
		Log.printLine("Simulation started!");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);
	
			RealtimeDatacenterBroker broker = (RealtimeDatacenterBroker) RealtimeHelper.createBroker();
			int brokerId = broker.getId();
	
			List<Vm> vmlist = RealtimeHelper.createVmList(brokerId, chromosomeDim, chrom);
			List<Cloudlet> cloudletList = RealtimeHelper.createRealtimeCloudlet(brokerId, vmlist, chromosomeDim);
			List<PowerHost> hostList = RealtimeHelper.createHostList(RealtimeConstants.NUMBER_OF_HOSTS);
	
			//TODO 沿着这条线改了
			PowerDatacenter datacenter = (PowerDatacenter) RealtimeHelper.createDatacenter("Datacenter", GADatacenter.class, hostList, RealtimeConstants.VmAllocationPolicy, chrom);
			datacenter.setDisableMigrations(true);
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.terminateSimulation(RealtimeConstants.SIMULATION_LIMIT);
			
			double lastClock = CloudSim.startSimulation();
			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();

			OutputStream origional_output = Log.getOutput();
			Log.setOutput(ga_log);
			Log.printLine();
			Log.printLine("Chromsome Simulation Result: --------");
			Log.printLine(chrom.getGenesAsStr());
			ga_result = RealtimeHelper.printResults(datacenter, vmlist, cloudletList, null, 0, received_cloudlets, lastClock, RealtimeConstants.OUTPUT_CSV);
			Log.printLine("Fitness = " + ga_result[3]);
			Log.printLine();
			Log.printLine();
			Log.setOutput(origional_output);
			
			Log.printLine("Simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
		
		return ga_result;
	}

	@Override
	protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		
	}

	@Override
	protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
	}
}
