package scheduling.genetic_algorithm;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

public class NormalVmAllocationPolicy extends PowerVmAllocationPolicyAbstract{
	private AllocationMapping mapping;
	
	public NormalVmAllocationPolicy(List<? extends Host> list, AllocationMapping mapping) {
		super(list);
		this.mapping = mapping;
	}

	@Override
	public PowerHost findHostForVm(Vm vm) {
		PowerHost host = null;
		int hostId = this.mapping.getHostOfVm(vm.getId());
		if(hostId != -1) {
			host = (PowerHost) HostList.getById(this.getHostList(), hostId);
			if(!host.isSuitableForVm(vm))
				host.MakeSuitableHostForVm(vm);
		}
		return host;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
}
