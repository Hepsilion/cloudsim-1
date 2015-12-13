package experiments.paper1.dvfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import experiments.paper1.main.PowerHostList;


public class MyDvfsPowerVmAllocation extends PowerVmAllocationPolicyAbstract {
	public MyDvfsPowerVmAllocation(List<? extends Host> list) {
		super(list);
	}

	public PowerHost findHostForVm(Vm vm) {
    	ArrayList<PowerHost> hosts = new ArrayList<PowerHost>();
		for(PowerHost host : this.<PowerHost>getHostList()) {
			hosts.add(host);
		}
		PowerHostList.sortDvfsHosts(hosts);
		for(int i=0; i<hosts.size(); i++) {
			if(hosts.get(i).isSuitableForVm(vm))
				return hosts.get(i);
			else if(hosts.get(i).MakeSuitableHostForVm(vm)) {
				return hosts.get(i);
			}
		}
		return null;
    }

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
}
