package scheduling.base_approach;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import scheduling.our_approach.utility.SchedulingConstants;
import scheduling.our_approach.utility.SchedulingDatacenter;
import scheduling.our_approach.utility.SchedulingDatacenterBroker;
import scheduling.our_approach.utility.SchedulingHelper;

public class Base_FF_Example {
	/** The broker. */
	protected static SchedulingDatacenterBroker broker;
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;
	/** The host list. */
	protected static List<PowerHost> hostList;
	/** The datacenter. */
	private static PowerDatacenter datacenter;
	
	private static OutputStream os;
	
	public static void main(String[] args) throws IOException {
        for(int i=0; i<SchedulingConstants.NUMBER_OF_CASE; i++) {
        	int temp_numCloudlets = SchedulingConstants.NUMBER_OF_CLOUDLETS + i*10;
        	System.out.println("#Cloudlet="+temp_numCloudlets);
        	String resultFile = SchedulingConstants.ff_result_File + "_"+ temp_numCloudlets;
        	String logFile = SchedulingConstants.ff_log_File+ "_"+ temp_numCloudlets;
    		try {
    			SchedulingHelper.initOutput(logFile, resultFile, null);
    			os = new FileOutputStream(SchedulingConstants.OutputFolder+"/" + resultFile + ".txt");
    		} catch (Exception e) {
    			e.printStackTrace();
    			System.exit(0);
    		} 
    		
    		OutputStream origional_output = Log.getOutput();
            Log.setOutput(os);
        	
        	Log.printLine("FF Example start time: " + System.currentTimeMillis());
            Log.setOutput(origional_output);
    		Log.printLine("FF Example Simulation started!");
    		try {
    			CloudSim.init(1, Calendar.getInstance(), false);

    			broker = (SchedulingDatacenterBroker) SchedulingHelper.createBroker();
    			int brokerId = broker.getId();

    			vmlist = SchedulingHelper.createVmList(brokerId, temp_numCloudlets);
    			cloudletList = SchedulingHelper.createSchedulingCloudlet(brokerId, vmlist, temp_numCloudlets);
    			hostList = SchedulingHelper.createHostList(SchedulingConstants.NUMBER_OF_HOSTS);

    			datacenter = (PowerDatacenter) SchedulingHelper.createDatacenter("Datacenter", SchedulingDatacenter.class, hostList, SchedulingConstants.ff_vmAllocationPolicy, null);
    			datacenter.setDisableMigrations(true);
    			broker.submitVmList(vmlist);
    			broker.submitCloudletList(cloudletList);
    			
    			CloudSim.terminateSimulation(SchedulingConstants.SIMULATION_LIMIT);
    			
    			double lastClock = CloudSim.startSimulation();
    			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
    			Log.printLine("Received " + received_cloudlets.size() + " cloudlets");
    			System.out.println(received_cloudlets.size());
    			CloudSim.stopSimulation();

    			Log.setOutput(os);
    			Log.printLine("FF Example end time: " + System.currentTimeMillis());
    			SchedulingHelper.printResults(datacenter, null, vmlist, cloudletList, received_cloudlets, lastClock, SchedulingConstants.OUTPUT_CSV);
    			Log.setOutput(origional_output);
    			
    			Log.printLine("FF Example Simulation finished!");
    		} catch (Exception e) {
    			e.printStackTrace();
    			Log.printLine("Unwanted errors happen");
    		}
    		
    		origional_output.close();
    		File file = new File(SchedulingConstants.OutputFolder + "/" +SchedulingConstants.ff_log_File+"_"+temp_numCloudlets+".txt");
    		if(file.exists())
    			file.delete();
        }
	}
}
