package scheduling.our_approach.utility;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class Compute1PowerModel extends PowerModelSpecPower {
	/** The power. */
	private final double[] power = { 74.87081739, 81.36354955, 86.89808713, 90.84332255, 98.67086909, 112.9219057, 119.4677516, 122.1187194, 123.8334469, 124.8915296, 126.1375981};
	//private final double[] power={81.41062533, 87.49471509, 92.40689679, 97.08700673, 104.2523337, 116.7083646, 125.1316022, 127.7008285, 129.6401782, 131.8897614, 132.2977555};
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
