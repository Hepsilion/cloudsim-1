package scheduling.our_approach;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

public class NormalDistr implements ContinuousDistribution{
	/** The num gen. */
	private final NormalDistribution numGen;
	
	public NormalDistr(double mean, double dev) {
		this.numGen = new NormalDistribution(mean, dev);
	}
	
	public NormalDistr(Random seed, double mean, double dev) {
		this(mean, dev);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	@Override
	public double sample() {
		return numGen.sample();
	}
	
	public static void main(String[] args) {
		NormalDistr dist = new NormalDistr(new Random(), 10, 1);
		for(int i=0; i<50; i++) {
			System.out.println(dist.sample());
		}
	}
}
