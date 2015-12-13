package experiments.paper1.temp;

import org.apache.commons.math3.genetics.Chromosome;

public class SchedulingChrom extends Chromosome{
	private int chromosomeDim;
	private int[] geneHosts;
	private double tdr;
	private double dmr;
	private double energy;
	private double fitness;

	public SchedulingChrom(int chromosomeDim, int[] geneHosts) {
		super();
		this.chromosomeDim = chromosomeDim;
		this.geneHosts = geneHosts;
	}
	
	public int getChromosomeDim() {
		return chromosomeDim;
	}

	public void setChromosomeDim(int chromosomeDim) {
		this.chromosomeDim = chromosomeDim;
	}

	public int[] getGeneHosts() {
		return geneHosts;
	}

	public void setGeneHosts(int[] geneHosts) {
		this.geneHosts = geneHosts;
	}

	public double getTdr() {
		return tdr;
	}

	public void setTdr(double tdr) {
		this.tdr = tdr;
	}

	public double getDmr() {
		return dmr;
	}

	public void setDmr(double dmr) {
		this.dmr = dmr;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	@Override
	public double fitness() {
		return SchedulingHelper.simulation(this);
	}
}
