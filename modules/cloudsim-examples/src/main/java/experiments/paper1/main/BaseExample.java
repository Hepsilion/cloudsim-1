package experiments.paper1.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

public class BaseExample {
	/** The broker. */
	protected static RealtimeDatacenterBroker broker;
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;
	/** The host list. */
	protected static List<PowerHost> hostList;
	/** The datacenter. */
	private static PowerDatacenter datacenter;
	
	private static OutputStream os;
	
	public static void main(String[] args) {
	    File outputFile = new File("/home/hepsilion/Workspace/Temp/base_result.txt");
        try {
            os = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        
		try {
			RealtimeHelper.initLogOutput(
					RealtimeConstants.ENABLE_OUTPUT,
					RealtimeConstants.OUTPUT_TO_FILE,
					RealtimeConstants.OutputFolder,
					RealtimeConstants.VmAllocationPolicy,
					RealtimeConstants.VmSelectionPolicy,
					RealtimeConstants.Parameter);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
		
		OutputStream origional_output = Log.getOutput();
        Log.setOutput(os);
        
        Log.printLine("Base Example start time: " + new Date().toString());
        Log.setOutput(origional_output);
		Log.printLine("Base Example Simulation started!");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			broker = (RealtimeDatacenterBroker) RealtimeHelper.createBroker();
			int brokerId = broker.getId();

			cloudletList = RealtimeHelper.createRealtimeCloudlet(brokerId, RealtimeConstants.NUMBER_OF_CLOUDLETS);
			vmlist = RealtimeHelper.createVmList(brokerId, cloudletList.size());
			hostList = RealtimeHelper.createHostList(RealtimeConstants.NUMBER_OF_HOSTS);

			datacenter = (PowerDatacenter) RealtimeHelper.createDatacenter("Datacenter", PowerDatacenter.class, hostList);
			datacenter.setDisableMigrations(true);
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.terminateSimulation(RealtimeConstants.SIMULATION_LIMIT);
			
			double lastClock = CloudSim.startSimulation();
			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
			Log.printLine("Received " + received_cloudlets.size() + " cloudlets");
			CloudSim.stopSimulation();

			Log.setOutput(os);
			Log.printLine("Base Example end time: " + new Date().toString());
			RealtimeHelper.printResults(datacenter, vmlist, cloudletList, received_cloudlets, lastClock, RealtimeConstants.OUTPUT_CSV);
			Log.setOutput(origional_output);
			
			Log.printLine("Base Example Simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
}