package experiments.paper1.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;

import experiments.paper1.main.PowerHostList;
import experiments.paper1.main.RealtimeHost;
import experiments.paper1.main.RealtimeVm;

public class GaPowerVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {
	ChromTaskScheduling chromosome = null;

	public GaPowerVmAllocationPolicy(List<? extends Host> list, Chromosome chromosome) {
		super(list);
		this.chromosome = (ChromTaskScheduling) chromosome;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
	
	public PowerHost findHostForVm2(Vm vm) {
		RealtimeHost chosenHost = null;
		RealtimeVm rv = (RealtimeVm) vm;
		RealtimeHost host = (RealtimeHost) HostList.getById(this.getHostList(), rv.getHostId());
		int vm_freq = rv.getFrequency();
		int old_freq = host.getFrequency();
		
		GeneTaskScheduling gene = this.chromosome.getGene(vm.getId());
		
		if(old_freq<vm_freq) {
		    host.setPeFrequency(vm_freq);
		    if(host.isSuitableForVm(vm)) {
		    	host.setFrequency(vm_freq);
		        chosenHost = host;
		    }else{
		    	host.setPeFrequency(old_freq);
		    }
		} else if(old_freq>vm_freq && host.isSuitableForVm(vm)) {
			gene.setFrequency(old_freq);
		    chosenHost=host;
		} else if(old_freq==vm_freq && host.isSuitableForVm(vm)) {
			chosenHost=host;
		}
		if(chosenHost==null && host.increaseHostMipsForNewVm(vm) ){
			host.setFrequency(host.getPeList().get(0).getIndexFreq());
			gene.setFrequency(host.getFrequency());
			chosenHost = host;
			Log.printLine("Choose Host#"+chosenHost.getId()+" for VM"+vm.getId()+"("+vm.getMips()+")"+" from hosts:chrom->F="+gene.getFrequency()+",H="+gene.getHost());
		}
		if(chosenHost==null){
			chosenHost=findHostFromNotEmptyHosts(host, vm);
			if(chosenHost!=null) {
				chosenHost.setFrequency(chosenHost.getPeList().get(0).getIndexFreq());
				gene.setFrequency(((RealtimeHost)chosenHost).getFrequency());
				gene.setHost(chosenHost.getId());
				Log.printLine("Choose Host#"+chosenHost.getId()+" for VM"+vm.getId()+"("+vm.getMips()+")"+" from not empty hosts:chrom->F="+gene.getFrequency()+",H="+gene.getHost());
			}
		}
		if(chosenHost==null){
			chosenHost = findHostFromEmptyHosts(host, vm);
			if(chosenHost!=null) {
				chosenHost.setFrequency(chosenHost.getPeList().get(0).getIndexFreq());
				gene.setFrequency(((RealtimeHost)chosenHost).getFrequency());
				gene.setHost(chosenHost.getId());
				Log.printLine("Choose Host#"+chosenHost.getId()+" for VM"+vm.getId()+"("+vm.getMips()+")"+" from empty hosts:chrom->F="+gene.getFrequency()+",H="+gene.getHost());
			}
		}
//		if(chosenHost==null) {
//			chosenHost = findHostByDecreaseVMMips(vm);
//			if(chosenHost!=null) {
//				chosenHost.setFrequency(chosenHost.getPeList().get(0).getIndexFreq());
//				gene.setFrequency(((RealtimeHost)chosenHost).getFrequency());
//				gene.setHost(chosenHost.getId());
//				Log.printLine("Choose Host#"+chosenHost.getId()+" for VM"+vm.getId()+" by decreasing vm mips:chrom->F="+gene.getFrequency()+",H="+gene.getHost());
//			}
//		}
		return chosenHost;
	}
	
	public RealtimeHost findHostFromNotEmptyHosts(RealtimeHost excludedHost, Vm vm) {
		List<RealtimeHost> notEmptyHosts = new ArrayList<RealtimeHost>();
		for(RealtimeHost host: this.<RealtimeHost>getHostList()) {
			if(host.getVmList().size()!=0 && host!=excludedHost)
				notEmptyHosts.add(host);
		}
		PowerHostList.sortByAscAvailableMips(notEmptyHosts);
		for(int i=0; i<notEmptyHosts.size(); i++) {
			if(notEmptyHosts.get(i).isSuitableForVm(vm)) {
				return notEmptyHosts.get(i);
			}
		}
		for(int i=0; i<notEmptyHosts.size(); i++) {
			if(notEmptyHosts.get(i).increaseHostMipsForNewVm(vm)) {
				return notEmptyHosts.get(i);
			}
		}
		return null;
	}
	
	public RealtimeHost findHostFromEmptyHosts(RealtimeHost excludedHost, Vm vm) {
		List<RealtimeHost> emptyHosts = new ArrayList<RealtimeHost>();
		for(RealtimeHost host: this.<RealtimeHost>getHostList()) {
			if(host.getVmList().size()==0 && host!=excludedHost)
				emptyHosts.add(host);
		}
		if(emptyHosts.size()==0)
			return null;
		else {
			PowerHostList.sortByAscAvailableMips(emptyHosts);
			for(int i=0; i<emptyHosts.size(); i++) {
				if(emptyHosts.get(i).isSuitableForVm(vm)){
					return emptyHosts.get(i);
				}
			}
			for(int i=0; i<emptyHosts.size(); i++) {
				if(emptyHosts.get(i).increaseHostMipsForNewVm(vm)) {
					return emptyHosts.get(i);
				}
			}
			return null;
		}
	}
	
	public RealtimeHost findHostByDecreaseVMMips(Vm vm){
		RealtimeHost moreAvailHost = null;
		double maxAvailMips = -1;
		for (int i = 0; i < getHostList().size(); i++) {
			if (maxAvailMips < getHostList().get(i).getAvailableMips()) {
				maxAvailMips = getHostList().get(i).getAvailableMips();
				moreAvailHost = (RealtimeHost) getHostList().get(i);
			}
		}
		if (moreAvailHost.decreaseVMMipsToHostNewVm(vm))
			return moreAvailHost;
		return null;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	public PowerHost findHostForVm(Vm vm) {
		RealtimeHost chosenHost = null;
		RealtimeVm rv = (RealtimeVm) vm;
		RealtimeHost h = (RealtimeHost) HostList.getById(this.getHostList(), rv.getHostId());
		int vm_freq = rv.getFrequency();
		int old_freq = h.getFrequency();
		
		GeneTaskScheduling gene = this.chromosome.getGene(vm.getId());
		
		if(old_freq<vm_freq) {
		    h.setPeFrequency(vm_freq);
		    if(h.isSuitableForVm(vm)) {
		    	h.setFrequency(vm_freq);
		        chosenHost = h;
		    }else{
		    	h.setPeFrequency(old_freq);
		    }
		} else if(old_freq>vm_freq && h.isSuitableForVm(vm)) {
			gene.setFrequency(old_freq);
		    chosenHost=h;
		} else if(old_freq==vm_freq && h.isSuitableForVm(vm)) {
			chosenHost=h;
		}
		if(chosenHost==null && h.increaseHostMipsForNewVm(vm) ){
			h.setFrequency(h.getPeList().get(0).getIndexFreq());
			gene.setFrequency(h.getFrequency());
			chosenHost = h;
			Log.printLine("Choose Host#"+chosenHost.getId()+" for VM"+vm.getId()+"("+vm.getMips()+")"+" from hosts:chrom->F="+gene.getFrequency()+",H="+gene.getHost());
		}
		
        if(chosenHost==null) {
        	double minPower = Double.MAX_VALUE;
            PowerHost allocatedHost = null;

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
            
            for(PowerHost host : suitableHosts) {
                try {
                    double powerAfterAllocation = getPowerAfterAllocation(host, vm);
                    double powerBeforeAllocation = getPowerAfterAllocation(host);
                    if (powerAfterAllocation != -1) {
                        double powerDiff = powerAfterAllocation - powerBeforeAllocation;
                        if (powerDiff < minPower) {
                            minPower = powerDiff;
                            allocatedHost = host;
                        }
                    }
                } catch (Exception e) {
                }
            }
            
            if(allocatedHost!=null){
            	chosenHost=(RealtimeHost) allocatedHost;
            	chosenHost.MakeSuitableHostForVm(vm);
            	chosenHost.setFrequency(chosenHost.getPeList().get(0).getIndexFreq());
    			gene.setFrequency(((RealtimeHost)chosenHost).getFrequency());
    			gene.setHost(chosenHost.getId());
            }
        }
        
        return chosenHost;
    }
    
    protected double getPowerAfterAllocation(PowerHost host){
    	double hostUtilizationMips = getUtilizationOfCpuMips(host);
    	double pePotentialUtilization = hostUtilizationMips / host.getTotalMaxMips();
    	double power = 0;
    	try {
    		power = ((PowerModelSpecPower_BAZAR_ME)host.getPowerModel()).getPower(pePotentialUtilization, 4);
    	} catch(Exception e) {
    		e.printStackTrace();
            System.exit(0);
    	}
    	return power;
    }
    
    protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
        double power = 0;
        try {
            power = ((PowerModelSpecPower_BAZAR_ME)host.getPowerModel()).getPower(getMaxUtilizationAfterAllocation(host, vm), 4);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

    protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
        double requestedTotalMips = vm.getCurrentRequestedTotalMips();
        double hostUtilizationMips = getUtilizationOfCpuMips(host);
        double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
        double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMaxMips();
        return pePotentialUtilization;
    }

    protected double getUtilizationOfCpuMips(PowerHost host) {
        double hostUtilizationMips = 0;
        for (Vm vm2 : host.getVmList()) {
            if (host.getVmsMigratingIn().contains(vm2)) {
                // calculate additional potential CPU usage of a migrating in VM
                hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
            }
            hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
        }
        return hostUtilizationMips;
    }
}