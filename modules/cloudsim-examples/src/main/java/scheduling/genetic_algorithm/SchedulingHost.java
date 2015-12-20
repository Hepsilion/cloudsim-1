package scheduling.genetic_algorithm;

import java.util.List;

import org.cloudbus.cloudsim.power.PowerHost;


public class SchedulingHost {
	OrderedList cloudlets;
	PowerHost host;

	public SchedulingHost(PowerHost host) {
		this.cloudlets = new OrderedList();
		this.host = host;
	}
	
	public void addCloudlet(SchedulingCloudlet cloudlet) {
		this.cloudlets.addCloudlet(cloudlet);
	}
	
	public void delCloudlet(SchedulingCloudlet cloudlet) {
		this.cloudlets.delCloudlet(cloudlet);
	}
	
	public List<SchedulingCloudlet> getCloudlets() {
		return cloudlets.getCloudlets();
	}

	public SchedulingCloudlet getMaxIntersectCloudletOnHost() {
		SchedulingCloudlet cloudlet = null;
		double maxIntersectionTime = Double.MIN_VALUE;
		if(this.getCloudlets().size()==1)
			return this.getCloudlets().get(0);
		else{
			for(SchedulingCloudlet c1 : this.getCloudlets()) {
				double intersectionTime = 0;
				for(SchedulingCloudlet c2 : this.getCloudlets()) {
					if(c1 == c2) {
						continue;
					} else {
						intersectionTime+=c1.getIntersectionTime(c2);
					}
				}
				if(maxIntersectionTime<intersectionTime) {
					maxIntersectionTime = intersectionTime;
					cloudlet = c1;
				}
			}
		}
		return cloudlet;
	}
	
	public PowerHost getHost() {
		return host;
	}

	public void setHost(PowerHost host) {
		this.host = host;
	}

	protected SchedulingHost clone() {
		SchedulingHost newHost = new SchedulingHost(this.getHost());
		for(SchedulingCloudlet cloudlet : this.getCloudlets()) 
			newHost.addCloudlet(cloudlet);
		return newHost;
	}
}
