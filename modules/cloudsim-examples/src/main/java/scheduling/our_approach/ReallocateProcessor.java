package scheduling.our_approach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;

import scheduling.our_approach.utility.SchedulingCloudlet;
import scheduling.our_approach.utility.SchedulingConstants;

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
		SchedulingHost sHost = null;
		PowerHost host = null;
		List<SchedulingCloudlet> cloudlets = null;
		
		System.out.println("VM#"+vmId+": "+allVms.get(vmId).getMaxMips());
		for(int hid=0; hid<SchedulingConstants.NUMBER_OF_HOSTS; hid++){
			sHost = hosts[hid];
			host = sHost.getHost();
			cloudlets = sHost.getCloudlets();
			
			double maxTotalMips = 0;
			List<TimeCloudlet> timeCloudlets = new ArrayList<TimeCloudlet>();
			for(SchedulingCloudlet cl : cloudlets){
				int clId = cl.getCloudletId();
				int kind = cloudlet.intersected(cl);
				if(kind==1){
					maxTotalMips += allVms.get(clId).getMaxMips();
				}else if(kind==2){
					timeCloudlets.add(new TimeCloudlet(cl.getFinishTime(), 1, cl));
					timeCloudlets.add(new TimeCloudlet(cl.getStartTime(), 2, cl));
				}else if(kind==3){
					timeCloudlets.add(new TimeCloudlet(cl.getStartTime(), 2, cl));
				}else if(kind==4){
					timeCloudlets.add(new TimeCloudlet(cl.getFinishTime(), 1, cl));
					maxTotalMips += allVms.get(clId).getMaxMips();
				}
			}
			TimeCloudletList tcs = new TimeCloudletList();
			maxTotalMips = tcs.getMaxIntersectionMips(timeCloudlets, maxTotalMips);
			System.out.println("HOST#"+host.getId()+": "+(host.getTotalMaxMips()-maxTotalMips));
			if(host.getTotalMaxMips()-maxTotalMips>=allVms.get(vmId).getMaxMips()){
				hostId = host.getId(); 
				sHost.addCloudlet(cloudlet);
				return hostId;
			}
		}
		return hostId;
	}
	
	class TimeCloudlet {
		double time;
		int kind; //1 leave; 2 enter
		SchedulingCloudlet cloudlet;
		
		public TimeCloudlet(double time, int kind, SchedulingCloudlet cloudlet) {
			super();
			this.time = time;
			this.kind = kind;
			this.cloudlet = cloudlet;
		}

		public double getTime() {
			return time;
		}
		
		public void setTime(double time) {
			this.time = time;
		}
		
		public int getKind() {
			return kind;
		}

		public void setKind(int kind) {
			this.kind = kind;
		}

		public SchedulingCloudlet getCloudlet() {
			return cloudlet;
		}

		public void setCloudlet(SchedulingCloudlet cloudlet) {
			this.cloudlet = cloudlet;
		}
	}
	
	class TimeCloudletList {
		public <T extends TimeCloudlet> void sortTimeCloudletList(List<T> cloudlets) {
			Collections.sort(cloudlets, new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					TimeCloudlet tc1 = (TimeCloudlet) o1;
					TimeCloudlet tc2 = (TimeCloudlet) o2;
					
					if(tc1.getTime() < tc2.getTime())
						return -1;
					else if(tc1.getTime() == tc2.getTime())
						return 0;
					else
						return 1;
				}
			});
		}
		
		public double getMaxIntersectionMips(List<TimeCloudlet> cloudlets, double totalMips){
			double maxTotalMips = totalMips;
			double tempTotalMips = totalMips;
			
			sortTimeCloudletList(cloudlets);
			int cloudletId;
			for(int i=0; i<cloudlets.size(); i++){
				cloudletId = cloudlets.get(i).getCloudlet().getCloudletId();
				if(cloudlets.get(i).getKind()==1){
					tempTotalMips -= allVms.get(cloudletId).getMaxMips();
				}else if(cloudlets.get(i).getKind()==2){
					tempTotalMips += allVms.get(cloudletId).getMaxMips();
				}
				maxTotalMips = maxTotalMips>tempTotalMips ? maxTotalMips:tempTotalMips;
			}
			return maxTotalMips;
		}
	}
}