package scheduling.our_approach;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class ReallocateProcessor {
	AllocationMapping overallMapping;
	SchedulingHost[] hosts;
	List<Vm> allVms;
	List<Cloudlet> cloudletList;
	
	public ReallocateProcessor(AllocationMapping overallMapping, SchedulingHost[] hosts, List<Vm> allVms, List<Cloudlet> cloudletList) {
		super();
		this.overallMapping = overallMapping;
		this.hosts = hosts;
		this.allVms = allVms;
		this.cloudletList = cloudletList;
	}

	public void reAllocate() {
		int hostId;
		for(int vmId=0; vmId<this.overallMapping.getNumVms(); vmId++) {
			hostId = this.overallMapping.getHostOfVm(vmId);
			if(hostId==-1){
				hostId = findHostForVm(vmId);
				if(hostId!=-1){
					this.overallMapping.setHostOfVm(vmId, hostId);
				} 
			}
		}
	}
	
	public int findHostForVm(int vmId){
		int hostId = -1;
		SchedulingCloudlet cloudlet= (SchedulingCloudlet) cloudletList.get(vmId);
		SchedulingHost host = null;
		List<SchedulingCloudlet> cloudlets = null;
		
		double maxTotalMips = 0; 
		for(int hid=0; hid<SchedulingConstants.NUMBER_OF_HOSTS; hid++){
			host = hosts[hid];
			cloudlets = host.getCloudlets();
			for(SchedulingCloudlet cl : cloudlets){
				int kind = cloudlet.intersected(cl);
				if(kind==1)
					maxTotalMips += allVms.get(cl.getCloudletId()).getMaxMips();
			}
			
			if(host.getHost().getVmScheduler().getMaxAvailableMips()-maxTotalMips>=allVms.get(cloudlet.getCloudletId()).getMaxMips()){
				hostId = hid; 
				host.addCloudlet(cloudlet);
				return hostId;
			}
		}
		return hostId;
	}
}