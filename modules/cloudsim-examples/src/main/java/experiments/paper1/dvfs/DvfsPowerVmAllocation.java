package experiments.paper1.dvfs;

import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;


public class DvfsPowerVmAllocation extends PowerVmAllocationPolicyAbstract {
    public DvfsPowerVmAllocation(List<? extends Host> list) {
        super(list);
    }

    @Override
    public PowerHost findHostForVm(Vm vm) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;

        for (PowerHost host : this.<PowerHost> getHostList()) {
            if (host.isSuitableForVm(vm)) {
                if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                    continue;
                }

                try {
                    double powerAfterAllocation = getPowerAfterAllocation(host, vm);
                    if (powerAfterAllocation != -1) {
                        double powerDiff = powerAfterAllocation - host.getPower();
                        if (powerDiff < minPower) {
                            minPower = powerDiff;
                            allocatedHost = host;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
//        if (allocatedHost == null) {
//            Log.printLine("No host available for VM " + vm.getId() + " can be found without modifications");
//            // if we are here, it means that no Host are suitable...
//            // HOST that used DVFS can be modified to host a new VM OR Vm size has to be decrease
//
//            PowerHost moreAvailHost = null;
//            double maxAvailMips = -1;
//            // so we take the host with the higher FREE mips
//            for (int i = 0; i < getHostList().size(); i++) {
//                if (maxAvailMips < getHostList().get(i).getAvailableMips()) {
//                    maxAvailMips = getHostList().get(i).getAvailableMips();
//                    moreAvailHost = (PowerHost) getHostList().get(i);
//                }
//            }
//
//            // Normally, there is no case where moreAvailHost can be NULL
//            // moreAvailHost is NOT NULL
//            if (moreAvailHost.MakeSuitableHostForVm(vm)) // change Pe frequency
//                return moreAvailHost;
//            else {
//                if (moreAvailHost.decreaseVMMipsToHostNewVm(vm))
//                    return moreAvailHost;
//            }
//            Log.printLine("CHOSEN HOST for VM " + vm.getId() + " is : Host #" + moreAvailHost.getId());
//        }

        return allocatedHost;
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
    
    protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
        boolean isHostOverUtilizedAfterAllocation = true;
        if (host.vmCreate(vm)) {
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
            host.vmDestroy(vm);
        }
        return isHostOverUtilizedAfterAllocation;
    }
    
    protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
        double power = 0;
        try {
            power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

    /**
     * Gets the power after allocation. We assume that load is balanced between PEs. The only
     * restriction is: VM's max MIPS < PE's MIPS
     * 
     * @param host the host
     * @param vm the vm
     * 
     * @return the power after allocation
     */
    protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
        double requestedTotalMips = vm.getCurrentRequestedTotalMips();
        double hostUtilizationMips = getUtilizationOfCpuMips(host);
        double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
        double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
        return pePotentialUtilization;
    }

    private boolean isHostOverUtilized(PowerHost host) {
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }
    
}
