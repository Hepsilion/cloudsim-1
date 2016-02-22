package scheduling.base_approach;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

public class FFPowerVmAllocation extends PowerVmAllocationPolicyAbstract{

	public FFPowerVmAllocation(List<? extends Host> list) {
		super(list);
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}

	@Override
	public PowerHost findHostForVm(Vm vm) {
		PowerHost chosenHost = null;
		for(PowerHost host : this.<PowerHost>getHostList()){
			if(host.isSuitableForVm(vm)){
				chosenHost = host;
				break;
			}
		}
		return chosenHost;
	}
}
