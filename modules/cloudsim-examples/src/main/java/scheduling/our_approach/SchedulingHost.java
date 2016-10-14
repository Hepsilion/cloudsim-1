package scheduling.our_approach;

import java.util.List;

import org.cloudbus.cloudsim.power.PowerHost;

import scheduling.our_approach.utility.SchedulingCloudlet;

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
	
	public SchedulingCloudlet getMinIntersectCloudletOnHost() {
		SchedulingCloudlet cloudlet = null;
		double minIntersectionTime = Double.MAX_VALUE;
		if(this.getCloudlets().size()==0)
			return cloudlet;
		else if(this.getCloudlets().size()==1)
			return this.getCloudlets().get(0);
		else{
			for(SchedulingCloudlet c1 : this.getCloudlets()) {
				double intersectionTime = 0;
				for(SchedulingCloudlet c2 : this.getCloudlets()) {
					if(c1.getCloudletId() == c2.getCloudletId()) {
						continue;
					} else {
						intersectionTime+=c1.getIntersectionTime(c2);
						//System.out.println("Host"+this.getHost().getId()+"---"+c1.getCloudletId()+":"+c2.getCloudletId()+"---"+c1.getIntersectionTime(c2));
					}
				}
				if(minIntersectionTime>intersectionTime) {
					minIntersectionTime = intersectionTime;
					cloudlet = c1;
				}
			}
		}
		//System.out.println(this.getHost().getId()+"---"+cloudlet.getCloudletId()+":"+minIntersectionTime);
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
