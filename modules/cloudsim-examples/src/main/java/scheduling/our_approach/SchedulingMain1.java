package scheduling.our_approach;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;

import scheduling.our_approach.utility.SchedulingConstants;
import scheduling.our_approach.utility.SchedulingHelper;

public class SchedulingMain1 {
	public static void main(String[] args) {
		for(int ca=0; ca<SchedulingConstants.NUMBER_OF_CASE; ca++){
			int num_all_cloudlets = SchedulingConstants.NUMBER_OF_CLOUDLETS + ca*20;
		
			SchedulingHelper.initOutput(SchedulingConstants.our_log_file+num_all_cloudlets, SchedulingConstants.our_result_file+num_all_cloudlets, SchedulingConstants.our_result_temp_file+num_all_cloudlets);
			OutputStream result_output = SchedulingHelper.getOutputStream(SchedulingConstants.our_result_file+num_all_cloudlets);
			OutputStream mediate_result_output = SchedulingHelper.getOutputStream(SchedulingConstants.our_result_temp_file+num_all_cloudlets);
			OutputStream originOutput = Log.getOutput();
			
			AllocationMapping mapping = new AllocationMapping(num_all_cloudlets);
			
			List<Vm> vmList = new ArrayList<Vm>();
			List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
			List<PowerHost> hostList = new ArrayList<PowerHost>();
			SchedulingHelper.simulation(cloudletList, hostList, vmList, mapping, SchedulingConstants.our_initial_vmAllocationPolicy);
			SchedulingHelper.outputResultToResultFile("Initial", originOutput, result_output, mapping, 1);
			
			int iCnt = 1;
			SchedulingHost[] hosts = null;
			List<Cloudlet> tempCloudlets = null;
			while(iCnt <= 1) {
				Log.printLine(iCnt+"th Iteration:");
				SchedulingHelper.outputToResultFile(originOutput, mediate_result_output, "########################"+iCnt+"th Iteration:########################");
				hosts = new SchedulingHost[SchedulingConstants.NUMBER_OF_HOSTS];
				for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++) {
					hosts[i] = new SchedulingHost(HostList.getById(hostList, i));
				}
				tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
				SchedulingHelper.getOrderedCloudletOnSchedulingHost(mapping, hosts, tempCloudlets);
				int num[] = SchedulingHelper.getRandomPermitation(SchedulingConstants.NUMBER_OF_HOSTS);
				for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS;) {
					new LocalExchangeProcessor(vmList, mapping).doExchange(hosts[num[i++]], hosts[num[i++]], mediate_result_output, originOutput);
				}
				tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
				SchedulingHelper.simulation(tempCloudlets, hostList, vmList, mapping, SchedulingConstants.our_normal_vmAllocationPolicy);
				SchedulingHelper.outputResultToResultFile(iCnt++ +"th iteration ", originOutput, result_output, mapping, 2);
			}
			tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
			SchedulingHelper.simulation(tempCloudlets, hostList, vmList, mapping, SchedulingConstants.our_normal_vmAllocationPolicy);
			SchedulingHelper.outputResultToResultFile("Final", originOutput, result_output, mapping, 1);
		}
	}
}