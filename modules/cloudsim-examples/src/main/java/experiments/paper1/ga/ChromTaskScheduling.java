package experiments.paper1.ga;


public class ChromTaskScheduling extends Chromosome {
	protected GeneTaskScheduling[] genes;
	protected double energy;
	
	public ChromTaskScheduling(int numTasks) {
		genes = new GeneTaskScheduling[numTasks];
	}

	public GeneTaskScheduling[] getGenes() {
		return genes;
	}
	
	public GeneTaskScheduling getGene(int index) {
		return genes[index];
	}

	public void setGene(GeneTaskScheduling gene, int index) {
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
	
	public int getFreqByVm(int vmId) {
		if(vmId <= genes.length-1)
			return genes[vmId].getFrequency();
		else
			return 0;
	}

	@Override
	public void copyChromGenes(Chromosome chromosome) {
		ChromTaskScheduling chromTaskScheduling = (ChromTaskScheduling) chromosome;
		for(int i=0; i<genes.length; i++) {
			this.genes[i] = chromTaskScheduling.genes[i];
		}
		this.fitness = chromosome.fitness;
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
        	frequencies.append(genes[i].getFrequency());
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
		ChromTaskScheduling chromTaskScheduling = (ChromTaskScheduling) chromosome;
		for(int i=0; i<genes.length; i++) {
			if(this.genes[i].equals(chromTaskScheduling.genes[i])) {
				numGenesInCommon++;
			}
		}
		return numGenesInCommon;
	}
	
	public Domination dominates(Chromosome chrom) {
		if(fitness > chrom.fitness)
			return Domination.Ture;
		else if(fitness < chrom.fitness)
			return Domination.False;
		else
			return Domination.NoDomination;
	}
	
	public String toString() {
		return getGenesAsStr();
	}
}
