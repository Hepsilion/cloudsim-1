package experiments.paper1.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import experiments.paper1.main.PowerHostList;
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
		PowerHost chosenHost = null;
		RealtimeVm rv = (RealtimeVm) vm;
		RealtimeHost host = (RealtimeHost) HostList.getById(this.getHostList(), rv.getHostId());
		int frequency = rv.getFrequency();
		if(host.getFrequency()<frequency) {
		    host.setFrequency(frequency);
		    if(host.isSuitableForVm(vm))
		        chosenHost = host;
		} else if(host.getFrequency()>frequency) {
		    int old_freq = host.getFrequency();
		    host.setFrequency(frequency);
		    if(!host.isSuitableForVm(vm)) {
		        host.setFrequency(old_freq);
		        if(host.isSuitableForVm(vm))
		            chosenHost=host;
		    }
		}
		//if(chosenHost!=null)
			return chosenHost;
		//else
			//return dvfsFindHostForVm(vm);
	}
	
	public PowerHost dvfsFindHostForVm(Vm vm) {
		PowerHost chosenHost = null;
		chosenHost = findHostFromNotEmptyHosts(vm);
		if(chosenHost == null)
			chosenHost = findHostFromEmptyHosts(vm);
		return chosenHost;
	}
	
	public RealtimeHost findHostFromNotEmptyHosts(Vm vm) {
		List<RealtimeHost> notEmptyHosts = new ArrayList<RealtimeHost>();
		for(RealtimeHost host: this.<RealtimeHost>getHostList()) {
			if(host.getVmList().size()!=0)
				notEmptyHosts.add(host);
		}
		PowerHostList.sortByAscAvailableMips(notEmptyHosts);
		for(int i=0; i<notEmptyHosts.size(); i++) {
			if(notEmptyHosts.get(i).isSuitableForVm(vm)) {
				//System.out.println("Chosen from not empty hosts for #VM"+vm.getId());
				return notEmptyHosts.get(i);
			}
		}
		for(int i=0; i<notEmptyHosts.size(); i++) {
			if(notEmptyHosts.get(i).increaseHostMipsForNewVm(vm)) {
				//System.out.println("Chosen from not empty hosts for #VM"+vm.getId());
				return notEmptyHosts.get(i);
			}
		}
		return null;
	}
	
	public RealtimeHost findHostFromEmptyHosts(Vm vm) {
		List<RealtimeHost> emptyHosts = new ArrayList<RealtimeHost>();
		for(RealtimeHost host: this.<RealtimeHost>getHostList()) {
			if(host.getVmList().size()==0)
				emptyHosts.add(host);
		}
		if(emptyHosts.size()==0)
			return null;
		else {
			for(int i=0; i<emptyHosts.size(); i++) {
				if(emptyHosts.get(i).isSuitableForVm(vm)){
					//System.out.println("Chosen from empty hosts for #VM"+vm.getId());
					return emptyHosts.get(i);
				}
			}
			for(int i=0; i<emptyHosts.size(); i++) {
				if(emptyHosts.get(i).increaseHostMipsForNewVm(vm)) {
					//System.out.println("Chosen from empty hosts for #VM"+vm.getId());
					return emptyHosts.get(i);
				}
			}
			return null;
		}
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
