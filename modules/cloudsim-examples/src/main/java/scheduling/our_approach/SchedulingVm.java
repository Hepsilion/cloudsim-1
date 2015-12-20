package scheduling.our_approach;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;

public class SchedulingVm extends PowerVm{
	private SchedulingCloudlet cloudlet;
	
	public SchedulingVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority, String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval) {
		super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
	}

	public SchedulingCloudlet getCloudlet() {
		return cloudlet;
	}

	public void setCloudlet(SchedulingCloudlet cloudlet) {
		this.cloudlet = cloudlet;
	}
}
