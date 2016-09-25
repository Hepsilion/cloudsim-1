package scheduling.our_approach.utility;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class ControllerPowerModel extends PowerModelSpecPower {
	/** The power. */
	private final double[] power = { 55.09897594, 58.90513365, 63.31699306, 69.03106504, 75.60568761, 91.53967326, 93.40403135, 115.61434, 124.817518, 131.0273285, 158.0909068};

	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}