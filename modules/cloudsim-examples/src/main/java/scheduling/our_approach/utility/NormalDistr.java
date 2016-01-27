package scheduling.our_approach.utility;

import java.io.File;
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
//		int[] execution_time = SchedulingHelper.getExecutionTime(50, 3600*15,  250);
//		for(int i=0; i<50; i++){
//			System.out.println(execution_time[i]);
//		}
	}
}
