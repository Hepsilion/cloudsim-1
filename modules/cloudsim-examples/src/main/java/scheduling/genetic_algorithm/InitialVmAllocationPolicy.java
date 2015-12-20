package scheduling.genetic_algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;

public class InitialVmAllocationPolicy extends PowerVmAllocationPolicyAbstract{
	private AllocationMapping mapping;
	
	public InitialVmAllocationPolicy(List<? extends Host> list, AllocationMapping mapping) {
		super(list);
		this.mapping = mapping;
	}

	public boolean allocateHostForVm(Vm vm) {
		PowerHost host = findHostForVm(vm);
		boolean result = allocateHostForVm(vm, host);
		if(result) {
			this.mapping.setHostOfVm(vm.getId(), host.getId());
		}
		return result;
	}
	
	@Override
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
        
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;
        for(PowerHost host : suitableHosts) {
            try {
            	double powerBeforeAllocation = getPowerBeforeAllocation(host);
                double powerAfterAllocation = getPowerAfterAllocation(host, vm);
                double powerDiff = powerAfterAllocation - powerBeforeAllocation;
                if (powerDiff < minPower) {
                    minPower = powerDiff;
                    allocatedHost = host;
                }
            } catch (Exception e) {
            }
        }
        
        if(allocatedHost!=null){
        	if(!allocatedHost.isSuitableForVm(vm))
        		allocatedHost.MakeSuitableHostForVm(vm);
			return allocatedHost;
        }
		return super.findHostForVm(vm);
	}
	
	protected double getPowerBeforeAllocation(PowerHost host){
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
            hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
        }
        return hostUtilizationMips;
    }
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}
}
