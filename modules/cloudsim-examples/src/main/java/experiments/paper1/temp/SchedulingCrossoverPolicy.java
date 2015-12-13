package experiments.paper1.temp;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;

public class SchedulingCrossoverPolicy implements CrossoverPolicy{
	@Override
	public ChromosomePair crossover(Chromosome first, Chromosome second)throws MathIllegalArgumentException {
		return null;
	}
}
