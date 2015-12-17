package experiments.paper1.dvfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import experiments.paper1.main.PowerHostList;


public class DvfsPowerVmAllocation extends PowerVmAllocationPolicyAbstract {
	public DvfsPowerVmAllocation(List<? extends Host> list) {
		super(list);
	}

	public PowerHost findHostForVm(Vm vm) {
        List<PowerHost> suitableHosts = new ArrayList<PowerHost>();
        for (PowerHost host : this.<PowerHost> getHostList()) {
            double maxAvailableMips = host.getTotalMaxMips()-(host.getTotalMips()-host.getAvailableMips());
            if(host.getVmScheduler().getMaxPeCapacity()>=vm.getCurrentRequestedMaxMips()
            		&& maxAvailableMips>=vm.getCurrentRequestedTotalMips()
            		&& host.getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) 
    				&& host.getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw())){
            	suitableHosts.add(host);
            }
        }

        PowerHost moreAvailHost = null;
		if (!suitableHosts.isEmpty()) {
			double maxAvailableMips = -1;
			double maxAvailMips;
			PowerHost tempHost = null;
			for (int i = 0; i < suitableHosts.size(); i++) {
				tempHost = suitableHosts.get(i);
				maxAvailMips = tempHost.getTotalMaxMips()-(tempHost.getTotalMips()-tempHost.getAvailableMips());
				if (maxAvailableMips < maxAvailMips) {
					maxAvailableMips = maxAvailMips;
					moreAvailHost = tempHost;
				}
			}
			Log.printLine("Chosen Host for VM#" + vm.getId() + " is : Host#" + moreAvailHost.getId());
			return moreAvailHost;
		} else {
			Log.printLine("No host available for VM#" + vm.getId() + " can be found without modifications");
			// if we are here, it means that no Host are suitable...
			// HOST that used DVFS can be modified to host a new VM
			// OR Vm size has to be decrease
			double maxAvailableMips = -1;
			double maxAvailMips;
			PowerHost tempHost = null;
			// so we take the host with the higher FREE mips
			for (int i = 0; i < getHostList().size(); i++) {
				tempHost = suitableHosts.get(i);
				maxAvailMips = tempHost.getTotalMaxMips()-(tempHost.getTotalMips()-tempHost.getAvailableMips());
				if (maxAvailableMips < maxAvailMips) {
					maxAvailableMips = maxAvailMips;
					moreAvailHost = tempHost;
				}
			}

			// Normally, there is no case where moreAvailHost can be NULL
			// moreAvailHost is NOT NULL
			if (moreAvailHost.MakeSuitableHostForVm(vm)){ // change Pe frequency
				Log.printLine("Change Pe Frequency on HOST#"+moreAvailHost.getId()+" to host VM#"+vm.getId());
				return moreAvailHost;
			}
			Log.printLine("Chosen host for VM#" + vm.getId() + " is : Host#" + moreAvailHost.getId());
		}
		return null;
    }
	
	public PowerHost findHostForVm2(Vm vm) {
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

