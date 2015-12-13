package experiments.paper1.main.twoEncode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

public class GaPowerVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {
	ChromTaskScheduling chromosome = null;
	
	public GaPowerVmAllocationPolicy(List<? extends Host> list) {
		super(list);
	}
	
	public GaPowerVmAllocationPolicy(List<? extends Host> list, Chromosome chromosome) {
		super(list);
		this.chromosome = (ChromTaskScheduling) chromosome;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
	
	public PowerHost findHostForVm(Vm vm) {
		PowerHost chosenHost = null;
		RealtimeVm rv = (RealtimeVm) vm;
		PowerHost host = (PowerHost) HostList.getById(this.getHostList(), rv.getHostId());
		
		GeneTaskScheduling gene = this.chromosome.getGene(vm.getId());
		
		if(host.isSuitableForVm(vm))
			return host;
		else if(host.MakeSuitableHostForVm(vm))
			return host;
		else {
			List<PowerHost> hosts = new ArrayList<PowerHost>();
			for(PowerHost h : this.<PowerHost>getHostList()) {
				hosts.add(h);
			}
			PowerHostList.sortDvfsHosts(hosts);
			for(int i=0; i<hosts.size(); i++) {
				if(hosts.get(i).isSuitableForVm(vm)) {
					chosenHost = (PowerHost) hosts.get(i);
					gene.setHost(chosenHost.getId());
					return chosenHost;
				}else if(hosts.get(i).MakeSuitableHostForVm(vm)){
					chosenHost = (PowerHost) hosts.get(i);
					gene.setHost(chosenHost.getId());
					return hosts.get(i);
				}
			}
		}
		
		return chosenHost;
	}
}
