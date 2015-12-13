package experiments.paper1.temp;

import java.util.List;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ListPopulation;
import org.apache.commons.math3.genetics.Population;

public class SchedulingPopulation extends ListPopulation {
	public SchedulingPopulation(List<Chromosome> chromosomes, int populationLimit) 
			throws NullArgumentException, NotPositiveException, NumberIsTooLargeException {
		super(chromosomes, populationLimit);
	}

	@Override
	public Population nextGeneration() {
		return new SchedulingPopulation(this.getChromosomeList(), 20);
	}
}
