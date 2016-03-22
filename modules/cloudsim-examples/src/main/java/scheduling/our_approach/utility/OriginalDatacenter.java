package scheduling.our_approach.utility;

import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

public class OriginalDatacenter extends PowerDatacenter {
    public OriginalDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
	}

	@Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	
        boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();

            if (result) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
            //send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
        }

        if (result) {
            getVmList().add(vm);
            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }

            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
            
            //TODO 添加 虚拟机创建成功之后，调节主机频率
            Log.printLine("Vm "+vm.getId()+" is created.");
            for(PowerHost host : this.<PowerHost>getHostList()){
            	host.isDvfsActivatedOnHost();
            }
        }
    }
    
    protected void processVmDestroy(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	
        super.processVmDestroy(ev, ack);
        Log.printLine("Vm "+vm.getId()+" is destroyed.");
        
        //TODO 添加 虚拟机销毁成功之后，调节主机频率
		for(PowerHost host : this.<PowerHost>getHostList()){
			host.dvfs();
		}
    }
}
