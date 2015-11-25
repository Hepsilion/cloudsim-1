package experiments.paper1.ga;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import experiments.paper1.main.RealtimeHost;
import experiments.paper1.main.RealtimeVm;

public class GaPowerVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {

	public GaPowerVmAllocationPolicy(List<? extends Host> list) {
		super(list);
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}

	@Override
	public PowerHost findHostForVm(Vm vm) {
		RealtimeVm rv = (RealtimeVm) vm;
		RealtimeHost host = (RealtimeHost) HostList.getById(this.getHostList(), rv.getHostId());
		int frequency = rv.getFrequency();
		if(host.getFrequency() < frequency) {
		    host.setFrequency(frequency);
		    if(!host.isSuitableForVm(vm))
		        return null;
		} else if(host.getFrequency() > frequency) {
		    int old_freq = host.getFrequency();
		    host.setFrequency(frequency);
		    if(!host.isSuitableForVm(vm)) {
		        host.setFrequency(old_freq);
		        if(!host.isSuitableForVm(vm))
		            return null;
		    }
		}
		return host ;
	}
	
    // 解除分配虚拟机后，可以更新主机频率
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            if(host.isEnableDVFS())
                host.regrowVmMipsAfterVmEnd(vm);
            host.vmDestroy(vm);
        }
    }
}
