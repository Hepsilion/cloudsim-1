package scheduling.our_approach;

import java.io.OutputStream;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;

public class OverallExchangeProcessor {
	int id;
	AllocationMapping overallMapping;
	AllocationMapping localMapping;
	
	SchedulingHost[] hosts = null;
	
	List<Cloudlet> cloudletList;
	List<PowerHost> hostList;
	List<Vm> vmList;
	
	List<Cloudlet> tempCloudlets = null;
	
	OutputStream result_output = null;
	OutputStream mediate_result_output;
	OutputStream originOutput;
	
	public OverallExchangeProcessor(int id, AllocationMapping overallMapping, List<PowerHost> hostList, List<Cloudlet> cloudletList, List<Vm> vmList, OutputStream result_output, OutputStream mediate_result_output, OutputStream originOutput) {
		this.id = id;
		this.overallMapping = overallMapping;
		localMapping = new AllocationMapping(this.overallMapping.getNumVms());
		for(int i=0; i<this.overallMapping.getNumVms(); i++) {
			localMapping.setHostOfVm(i, overallMapping.getHostOfVm(i));
		}
		
		this.cloudletList = cloudletList;
		this.hostList = hostList;
		this.vmList = vmList;
		
		this.result_output = result_output;
		this.mediate_result_output = mediate_result_output;
		this.originOutput = originOutput;
	}
	
	public void run() {
		int iCnt = 1;
		SchedulingHelper.outputToResultFile(originOutput, this.result_output, "++++++++++++++++++++++++++++++++++++++++"+this.id+"th Processor++++++++++++++++++++++++++++++++++++++++");
		while(iCnt <= SchedulingConstants.NUMBER_OF_ITERATION_PER_PROCESSOR) {
			SchedulingHelper.outputToResultFile(originOutput, this.mediate_result_output, "########################"+this.id+" th Processor----"+iCnt+"th Iteration:########################");
			this.hosts = new SchedulingHost[SchedulingConstants.NUMBER_OF_HOSTS];
			for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++) {
				hosts[i] = new SchedulingHost(HostList.getById(hostList, i));
			}
			
			tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
			SchedulingHelper.getOrderedCloudletOnSchedulingHost(this.localMapping, hosts, tempCloudlets);
			int num[] = SchedulingHelper.getRandomPermitation(SchedulingConstants.NUMBER_OF_HOSTS);
			for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS;) {
				new LocalExchangeProcessor(vmList, this.localMapping).doExchange(hosts[num[i++]], hosts[num[i++]], this.mediate_result_output, this.originOutput);
			}
			
			tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
			SchedulingHelper.simulation(tempCloudlets, hostList, vmList, this.localMapping, SchedulingConstants.normal_vmAllocationPolicy);
			SchedulingHelper.outputResultToResultFile(this.id + "th processor----"+ iCnt +" iteration ", this.originOutput, this.result_output, this.localMapping, 3);
		
			iCnt++;
		}
		
		//reallocate host for these tasks which has no host	
		this.hosts = new SchedulingHost[SchedulingConstants.NUMBER_OF_HOSTS];
		for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++) {
			hosts[i] = new SchedulingHost(HostList.getById(hostList, i));
		}
		tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
		SchedulingHelper.getOrderedCloudletOnSchedulingHost(this.localMapping, hosts, tempCloudlets);
		ReallocateProcessor rp = new ReallocateProcessor(overallMapping, hosts, vmList, tempCloudlets);
		rp.reAllocate();
		
		tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
		SchedulingHelper.simulation(tempCloudlets, hostList, vmList, this.localMapping, SchedulingConstants.normal_vmAllocationPolicy);
		SchedulingHelper.outputResultToResultFile(this.id+"th processor Final----", originOutput, result_output, this.localMapping, 2);
		SchedulingHelper.outputToResultFile(originOutput, this.result_output, "++++++++++++++++++++++++++++++++++++++++"+this.id+"th Processor++++++++++++++++++++++++++++++++++++++++\n\n");
	}
	
	public AllocationMapping getLocalMapping() {
		return localMapping;
	}

	public void setLocalMapping(AllocationMapping localMapping) {
		this.localMapping = localMapping;
	}
}