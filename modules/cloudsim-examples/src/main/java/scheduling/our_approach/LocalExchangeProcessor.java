package scheduling.our_approach;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.lists.PowerVmList;


public class LocalExchangeProcessor{
	AllocationMapping overallMapping;
	List<Vm> allVms;
	int num_all_cloudlets;
	List<Cloudlet> cloudlets;
	List<PowerHost> hostList;
	List<Vm> vmList;
	
	public LocalExchangeProcessor(List<Vm> allVms, int num_all_cloudlets, AllocationMapping mapping) {
		this.overallMapping = mapping;
		this.allVms = allVms;
		this.num_all_cloudlets = num_all_cloudlets;
		this.cloudlets = new ArrayList<Cloudlet>();
		this.hostList = new ArrayList<PowerHost>();
		this.vmList = new ArrayList<Vm>();
	}

	public void doExchange(SchedulingHost host1, SchedulingHost host2, OutputStream result_output, OutputStream originOutput) {
		Log.printLine("~~~~~~~~~~~~Starting exchange Host #"+host1.getHost().getId()+" and #"+host2.getHost().getId()+"~~~~~~~~~~~~");
		SchedulingHelper.outputToResultFile(originOutput, result_output, "~~~~~~~~~~~~Starting exchange Host #"+host1.getHost().getId()+" and #"+host2.getHost().getId()+"~~~~~~~~~~~~");
		
		cloudlets.addAll(host1.getCloudlets());
		cloudlets.addAll(host2.getCloudlets());
		
		hostList.add(host1.getHost());
		hostList.add(host2.getHost());
		
		for(Cloudlet cl : cloudlets) {
			vmList.add(PowerVmList.getById(allVms, cl.getCloudletId()));
		}
		
		SchedulingHost t1 = host1.clone();
		SchedulingHost t2 = host2.clone();
		this.generateOffSpring(t1, t2, 2);//A--->B
		
		SchedulingHost t3 = host1.clone();
		SchedulingHost t4 = host2.clone();
		this.generateOffSpring(t3, t4, 3);//A<---B
		
		SchedulingHost t5 = host1.clone();
		SchedulingHost t6 = host2.clone();
		this.generateOffSpring(t5, t6, 4);//A<-->B
		
		simulator simulator1 = new simulator(overallMapping, host1, host2);
		simulator simulator2 = new simulator(overallMapping, t1, t2);
		simulator simulator3 = new simulator(overallMapping, t3, t4);
		simulator simulator4 = new simulator(overallMapping, t5, t6);
		
		simulator1.run();
		simulator2.run();
		simulator3.run();
		simulator4.run();
		
		int host1Id = host1.getHost().getId();
		int host2Id = host2.getHost().getId();
		SchedulingHelper.outputToResultFile("1th Child["+host1Id+"("+host1.getCloudlets().size()+")----"+host2Id+"("+host2.getCloudlets().size()+")]", originOutput, result_output, simulator1.getMapping(), 4);
		SchedulingHelper.outputToResultFile("\n2th Child["+host1Id+"("+t1.getCloudlets().size()+")--->"+host2Id+"("+t2.getCloudlets().size()+")]", originOutput, result_output, simulator2.getMapping(), 4);
		SchedulingHelper.outputToResultFile("\n3th Child["+host1Id+"("+t3.getCloudlets().size()+")<---"+host2Id+"("+t4.getCloudlets().size()+")]", originOutput, result_output, simulator3.getMapping(), 4);
		SchedulingHelper.outputToResultFile("\n4th Child["+host1Id+"("+t5.getCloudlets().size()+")<-->"+host2Id+"("+t6.getCloudlets().size()+")]", originOutput, result_output, simulator4.getMapping(), 4);
		SchedulingHelper.outputToResultFile(originOutput, result_output, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		simulator chosenChild = simulator1;
		if(chosenChild.getMapping().getFitness()<simulator2.getMapping().getFitness()) {
			chosenChild = simulator2;
		}
		if(chosenChild.getMapping().getFitness()<simulator3.getMapping().getFitness()) {
			chosenChild = simulator3;
		}
		if(chosenChild.getMapping().getFitness()<simulator4.getMapping().getFitness()) {
			chosenChild = simulator4;
		}
		if(chosenChild!=simulator1) {
			for(Cloudlet cl : cloudlets){
				int hostId = chosenChild.getMapping().getHostOfVm(cl.getCloudletId());
				this.overallMapping.setHostOfVm(cl.getCloudletId(), hostId);
			}
			host1 = chosenChild.host1;
			host2 = chosenChild.host2;
		}
	}
	
	private void generateOffSpring(SchedulingHost host1, SchedulingHost host2, int kind) {
		//System.out.println("A----B   #1="+host1.getCloudlets().size()+" : #2="+host2.getCloudlets().size());
		if(kind==1) {
			//just keep the orginal parents
		}else if(kind==2) {
			//transfer a cloudlet from host1 to host2
			SchedulingCloudlet c2 = host1.getMaxIntersectCloudletOnHost();
			if(c2!=null) {
				host1.delCloudlet(c2);
				host2.addCloudlet(c2);
			}
			//System.out.println("A--->B   #1="+host1.getCloudlets().size()+" : #2="+host2.getCloudlets().size());
		}else if(kind==3) {
			//transfer a cloudlet from host2 to host1
			SchedulingCloudlet c3 = host2.getMaxIntersectCloudletOnHost();
			if(c3!=null) {
				host2.delCloudlet(c3);
				host1.addCloudlet(c3);
			}
			//System.out.println("A<---B   #1="+host1.getCloudlets().size()+" : #2="+host2.getCloudlets().size());
		}else if(kind==4) {
			//exchange cloudlet between host1 and host2
			SchedulingCloudlet c41 = host1.getMaxIntersectCloudletOnHost();
			SchedulingCloudlet c42 = host2.getMaxIntersectCloudletOnHost();
			if(c41!=null){
				host1.delCloudlet(c41);
				host2.addCloudlet(c41);
			}
			if(c42!=null) {
				host2.delCloudlet(c42);
				host1.addCloudlet(c42);
			}
			//System.out.println("A<-->B   #1="+host1.getCloudlets().size()+" : #2="+host2.getCloudlets().size());
		}
	}
	
	class simulator extends Thread {
		SchedulingHost host1;
		SchedulingHost host2;
		AllocationMapping mapping;
		
		public simulator(AllocationMapping overallMapping, SchedulingHost host1, SchedulingHost host2) {
			super();
			this.host1 = host1;
			this.host2 = host2;
			mapping = new AllocationMapping(num_all_cloudlets);
		}
		
		@Override
		public void run() {
			int host1Id = host1.getHost().getId();
			int host2Id = host2.getHost().getId();
			for(Cloudlet c1 : host1.getCloudlets()) {
				this.mapping.setHostOfVm(c1.getCloudletId(), host1Id);
			}
			for(Cloudlet c2 : host2.getCloudlets()) {
				this.mapping.setHostOfVm(c2.getCloudletId(), host2Id);
			}
			SchedulingHelper.simulation(cloudlets, hostList, vmList, mapping, SchedulingConstants.normal_vmAllocationPolicy);
		}

		public AllocationMapping getMapping() {
			return mapping;
		}
	}
}
