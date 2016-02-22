package scheduling.our_approach.utility;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;


public class SchedulingDatacenter extends PowerDatacenter {
    public SchedulingDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, 
    		List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }
    
    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	
    	Log.printLine(this.getName()+" compute resource usage and energy before VM#"+vm.getId()+" is created");
    	//TODO 修改以支持节约仿真时间
    	this.updateCloudletProcessing();
        
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
            
            Log.printLine(this.getName()+" compute resource usage and energy after VM#"+vm.getId()+" is created");
          //TODO 修改以支持节约仿真时间
            this.updateCloudletProcessing();
        }
    }
    
    protected void processVmDestroy(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	
    	Log.printLine(this.getName()+" compute resource usage and energy before VM#"+vm.getId()+" is destroyed");
    	//TODO 修改以支持节约仿真时间
    	this.updateCloudletProcessing();
    	
        super.processVmDestroy(ev, ack);
        Log.printLine("Vm "+vm.getId()+" is destroyed.");
        
        //TODO 添加 虚拟机销毁成功之后，调节主机频率
		for(PowerHost host : this.<PowerHost>getHostList()){
			host.dvfs();
		}
		
		Log.printLine(this.getName()+" compute resource usage and energy after VM#"+vm.getId()+" is destroyed");
		//TODO 修改以支持节约仿真时间
		this.updateCloudletProcessing();
    }
    
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		//TODO 修改以支持节约仿真时间，这样在虚拟机创建之后和销毁之后，可以更新主机的CPU利用率
		//if (currentTime > getLastProcessTime()) { 
			Log.printLine(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine(
									"%.2f: Migration of VM #%d to Host #%d is started",
									currentTime, vm.getId(), targetHost.getId());
						} else {
							Log.formatLine(
									"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime, vm.getId(), oldHost.getId(),
									targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(getId(), vm.getRam()/((double) targetHost.getBw()/(2*8000)), CloudSimTags.VM_MIGRATE, migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				//TODO 修改以支持节约仿真时间
				send(getId(), minTime-currentTime, CloudSimTags.VM_DATACENTER_EVENT); 
			}

			setLastProcessTime(currentTime);
		//}
	}
}
