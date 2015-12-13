package experiments.paper1.temp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.CycleCrossover;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomKeyMutation;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;

public class SchedulingGAMain {
	public static void main(String[] args) {
		CrossoverPolicy crossoverPolicy = new CycleCrossover<Object>(true);
		MutationPolicy mutationPolicy = new SchedulingMutationPolicy();
		SelectionPolicy selectionPolicy = new TournamentSelection(5);
		StoppingCondition stoppingCondition = new FixedGenerationCount(50);
		Population initialPopulation = new SchedulingPopulation(getInitialPopulation(10, 10), 10);
		GeneticAlgorithm ga = new GeneticAlgorithm(crossoverPolicy, 0.7, mutationPolicy, 0.4, selectionPolicy);
		ga.evolve(initialPopulation, stoppingCondition);
		
		SchedulingChrom bestChromosome = (SchedulingChrom) initialPopulation.getFittestChromosome();
		System.out.println(bestChromosome.getEnergy());
	}
	
	private static List<Chromosome> getInitialPopulation(int populationDim, int chromosomeDim) {
		List<Chromosome> chromosomes = new ArrayList<Chromosome>();
		for(int i=0; i<populationDim; i++) {
			chromosomes.add(getRandomChromosome(chromosomeDim));
		}
		return chromosomes;
	}
	
	public static Chromosome getRandomChromosome(int chromosomeDim) {
		int[] hosts = getRandomHosts(chromosomeDim, 0, chromosomeDim);
		Chromosome chromsome = new SchedulingChrom(chromosomeDim, hosts);
		return chromsome;
	}
	
	public static int[] getRandomHosts(int length, int min, int max) {
		Random rand = new Random(new Random().nextInt());
		int[] numbers = new int[length];
		for (int i = 0; i < length; i++) {
			numbers[i] = rand.nextInt(max)%(max-min+1)+min;
		}
		return numbers;
	}
}
