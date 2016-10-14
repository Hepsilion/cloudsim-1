package scheduling.our_approach.utility;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class ControllerPowerModel extends PowerModelSpecPower {
	/** The power. */
	//private final double[] power = { 55.09897594, 58.90513365, 63.31699306, 69.03106504, 75.60568761, 91.53967326, 93.40403135, 115.61434, 124.817518, 131.0273285, 158.0909068};
	private final double[] power={61.68288367, 64.55815984, 68.17700673, 73.25065735, 80.18424324, 86.59071688, 93.4126974, 115.6997786, 122.3716219, 129.5479598, 168.2184834};
	
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}