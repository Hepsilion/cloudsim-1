package experiments.paper1.dvfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationInterQuartileRange;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;


public class DvfsBase2PowerVmAllocation extends PowerVmAllocationPolicyAbstract {
    public DvfsBase2PowerVmAllocation(List<? extends Host> list) {
		super(list);
	}

	public PowerHost findHostForVm(Vm vm) {
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
//        	if (getUtilizationOfCpuMips(host) != 0 /*&& isHostOverUtilizedAfterAllocation(host, vm)*/) {
//                continue;
//            }

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
        if(allocatedHost!=null)
        	allocatedHost.MakeSuitableHostForVm(vm);
        return allocatedHost;
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
