package scheduling.our_approach.utility;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

public class NormalDistr implements ContinuousDistribution{
	/** The num gen. */
	private final NormalDistribution numGen;
	
	public NormalDistr(double mean, double dev) {
		this.numGen = new NormalDistribution(mean, dev);
	}
	
	public NormalDistr(int seed, double mean, double dev) {
		this(mean, dev);
		numGen.reseedRandomGenerator(seed);
	}

	@Override
	public double sample() {
		return numGen.sample();
	}
	
	public static void main(String[] args) {
		NormalDistribution nd = new NormalDistribution(10, 5);
		for(int i=0; i<50; i++){
			System.out.println((int)nd.sample());
		}
	}
}
