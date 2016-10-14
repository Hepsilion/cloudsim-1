package scheduling.our_approach.utility;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class Compute2PowerModel extends PowerModelSpecPower {
	private final double[] power={43.58775018,44.15208276,44.81764322,45.78051931,46.8439785,49.06838991,53.00102839,55.29646693,59.50913508,64.92617579,71.07366076};
	
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}
}
