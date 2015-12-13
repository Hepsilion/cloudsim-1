package experiments.paper1.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import experiments.paper1.main.PowerHostList;
import experiments.paper1.main.RealtimeHost;

public class GaInitialVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {
	ChromTaskScheduling chromosome = null;
	
	public GaInitialVmAllocationPolicy(List<? extends Host> list, Chromosome chromosome) {
		super(list);
		this.chromosome = (ChromTaskScheduling) chromosome;
	}
	
	public PowerHost findHostForVm(Vm vm) {
		GeneTaskScheduling gene = this.chromosome.getGene(vm.getId());
		ArrayList<PowerHost> hosts = new ArrayList<PowerHost>();
		for(PowerHost host : this.<PowerHost>getHostList()) {
			hosts.add(host);
		}
		PowerHostList.sortDvfsHosts(hosts);
		for(int i=0; i<hosts.size(); i++) {
			if(hosts.get(i).isSuitableForVm(vm)) {
				gene.setVm(vm.getId());
				gene.setHost(hosts.get(i).getId());
				gene.setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
				((RealtimeHost)(hosts.get(i))).setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
				return hosts.get(i);
			} else if(hosts.get(i).MakeSuitableHostForVm(vm)) {
				gene.setVm(vm.getId());
				gene.setHost(hosts.get(i).getId());
				gene.setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
				((RealtimeHost)(hosts.get(i))).setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
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
