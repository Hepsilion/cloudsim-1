package experiments.paper1.main.twoEncode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

public class GaInitialVmAllocationPolicy extends PowerVmAllocationPolicyMigrationAbstract {
	ChromTaskScheduling chromosome = null;
	
	public GaInitialVmAllocationPolicy(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy, Chromosome chromosome) {
		super(hostList, vmSelectionPolicy);
		this.chromosome = (ChromTaskScheduling) chromosome;
	}
	
	public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
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
				return hosts.get(i);
			} else if(hosts.get(i).MakeSuitableHostForVm(vm)) {
				gene.setVm(vm.getId());
				gene.setHost(hosts.get(i).getId());
				return hosts.get(i);
			}
		}
		return null;
	}

	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		return false;
	}
}
