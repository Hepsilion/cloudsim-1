package experiments.paper1.scheduling;


public class SchedulingChromosome extends Chromosome {
	protected SchedulingGene[] genes;
	protected double tdr;
	protected double dmr;
	protected double energy;
	
	public SchedulingChromosome(int numTasks) {
		genes = new SchedulingGene[numTasks];
	}

	public SchedulingGene[] getGenes() {
		return genes;
	}
	
	public SchedulingGene getGene(int index) {
		return genes[index];
	}

	public void setGene(SchedulingGene gene, int index) {
		genes[index] = gene;
	}
	
    public double getEnergy() {
        return energy;
    }
    
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public int getHostByVm(int vmId) {
		if(vmId <= genes.length-1)
			return genes[vmId].getHost();
		else
			return -1;
	}

	@Override
	public void copyChromGenes(Chromosome chromosome) {
		SchedulingChromosome chromTaskScheduling = (SchedulingChromosome) chromosome;
		for(int i=0; i<genes.length; i++) {
			this.genes[i] = chromTaskScheduling.genes[i];
		}
		this.fitness = chromTaskScheduling.fitness;
		this.tdr = chromTaskScheduling.tdr;
		this.dmr = chromTaskScheduling.dmr;
		this.energy = chromTaskScheduling.energy;
	}

	@Override
	public String getGenesAsStr() {
		int genesLength = genes.length;
        
        StringBuffer vms = new StringBuffer(genesLength);
        StringBuffer hosts = new StringBuffer(genesLength);
        StringBuffer frequencies = new StringBuffer(genesLength);
        for (int i = 0; i < genesLength; i++) {
        	vms.append(genes[i].getVm());
        	hosts.append(genes[i].getHost());
            if (i < genesLength - 1) {
            	vms.append("\t");
            	hosts.append("\t");
            	frequencies.append("\t");
            }
        }
        return (vms.toString() + "\n" + hosts.toString() + "\n" + frequencies.toString());
	}

	@Override
	public int getNumGenesInCommon(Chromosome chromosome) {
		int numGenesInCommon = 0;
		SchedulingChromosome chromTaskScheduling = (SchedulingChromosome) chromosome;
		for(int i=0; i<genes.length; i++) {
			if(this.genes[i].equals(chromTaskScheduling.genes[i])) {
				numGenesInCommon++;
			}
		}
		return numGenesInCommon;
	}
	
	public String toString() {
		return getGenesAsStr();
	}
}
