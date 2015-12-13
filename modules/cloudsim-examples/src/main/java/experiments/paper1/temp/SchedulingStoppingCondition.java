package experiments.paper1.temp;

import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;

public class SchedulingStoppingCondition implements StoppingCondition{
	@Override
	public boolean isSatisfied(Population population) {
		return false;
	}
}
