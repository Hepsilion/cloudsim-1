package scheduling.genetic_algorithm;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;

public class SchedulingMain2 {
	public static void main(String[] args) {
		SchedulingHelper.initOutput(SchedulingConstants.log_file, SchedulingConstants.result_file, SchedulingConstants.result_temp_file);
		OutputStream result_output = SchedulingHelper.getOutputStream(SchedulingConstants.result_file);
		OutputStream mediate_result_output = SchedulingHelper.getOutputStream(SchedulingConstants.result_temp_file);
		OutputStream originOutput = Log.getOutput();
		
		AllocationMapping mapping = new AllocationMapping(SchedulingConstants.NUMBER_OF_CLOUDLETS);
		
		List<Vm> vmList = new ArrayList<Vm>();
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		List<PowerHost> hostList = new ArrayList<PowerHost>();
		SchedulingHelper.simulation(cloudletList, hostList, vmList, mapping, SchedulingConstants.initial_vmAllocationPolicy);
		SchedulingHelper.outputResultToResultFile("Initial", originOutput, result_output, mapping, 1);
		
		//some processor along to search more search space
		int numProcessor = SchedulingConstants.NUMBER_OF_PROCESSORS;
		OverallExchangeProcessor[] processors = new OverallExchangeProcessor[numProcessor];
		for(int i=0; i<numProcessor; i++) {
			processors[i] = new OverallExchangeProcessor(i+1, mapping, hostList, cloudletList, vmList, result_output, mediate_result_output, originOutput);
			processors[i].run();
		}
		//select the max fitness processor
		double max_fitness = Double.MIN_VALUE;
		int max_num = -1;
		for(int i=0; i<numProcessor; i++) {
			if(max_fitness<processors[i].getLocalMapping().getFitness()){
				max_fitness = processors[i].getLocalMapping().getFitness();
				max_num = i;
			}
		}
		//update mapping
		for(int i=0; i<mapping.getNumVms(); i++) {
			mapping.setHostOfVm(i, processors[max_num].getLocalMapping().getHostOfVm(i));
		}
		List<Cloudlet> tempCloudlets=SchedulingHelper.getCopyOfCloudlets(cloudletList);
		SchedulingHelper.simulation(tempCloudlets, hostList, vmList, mapping, SchedulingConstants.normal_vmAllocationPolicy);
		SchedulingHelper.outputResultToResultFile("Final", originOutput, result_output, mapping, 1);
	}
}