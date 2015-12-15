package experiments.paper1.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;

import experiments.paper1.main.RealtimeHost;

public class GaInitialVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {
	ChromTaskScheduling chromosome = null;
	
	public GaInitialVmAllocationPolicy(List<? extends Host> list, Chromosome chromosome) {
		super(list);
		this.chromosome = (ChromTaskScheduling) chromosome;
	}
	
	public PowerHost findHostForVm(Vm vm) {
		GeneTaskScheduling gene = this.chromosome.getGene(vm.getId());
		
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
        	allocatedHost.MakeSuitableHostForVm(vm);
        	((RealtimeHost)allocatedHost).setFrequency(allocatedHost.getPeList().get(0).getIndexFreq());
			gene.setFrequency(((RealtimeHost)allocatedHost).getFrequency());
			gene.setHost(allocatedHost.getId());
			return allocatedHost;
        }
//		ArrayList<PowerHost> hosts = new ArrayList<PowerHost>();
//		for(PowerHost host : this.<PowerHost>getHostList()) {
//			hosts.add(host);
//		}
//		PowerHostList.sortDvfsHosts(hosts);
//		for(int i=0; i<hosts.size(); i++) {
//			if(hosts.get(i).isSuitableForVm(vm)) {
//				gene.setVm(vm.getId());
//				gene.setHost(hosts.get(i).getId());
//				gene.setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
//				((RealtimeHost)(hosts.get(i))).setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
//				return hosts.get(i);
//			} else if(hosts.get(i).MakeSuitableHostForVm(vm)) {
//				gene.setVm(vm.getId());
//				gene.setHost(hosts.get(i).getId());
//				gene.setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
//				((RealtimeHost)(hosts.get(i))).setFrequency(hosts.get(i).getPeList().get(0).getIndexFreq());
//				return hosts.get(i);
//			}
//		}
		return null;
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
	    
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
}
