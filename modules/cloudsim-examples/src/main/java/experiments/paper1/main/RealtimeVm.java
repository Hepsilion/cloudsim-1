package experiments.paper1.main;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;

public class RealtimeVm extends PowerVm {
	private int hostId;
	private int frequency;
	private RealtimeCloudlet cloudlet;
	private boolean modified;

	public RealtimeVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority,
			String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval, int hostId, int frequency) {
		this(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
		this.hostId = hostId;
		this.frequency = frequency;
	}
	
	public RealtimeVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority,
			String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval) {
		super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
	}

	public int getHostId() {
		return hostId;
	}

	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public RealtimeCloudlet getCloudlet() {
		return cloudlet;
	}

	public void setCloudlet(RealtimeCloudlet cloudlet) {
		this.cloudlet = cloudlet;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
