package experiments.paper1.main;

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
		int numExes = 100;
        double result[][] = new double[numExes+1][3];
        for(int i=0; i<=numExes; i++) {
        	int temp_numCloudlets = RealtimeConstants.NUMBER_OF_CLOUDLETS + i*10;
        	System.out.println("#Cloudlet="+temp_numCloudlets);
        	String resultFile = "base_f"+RealtimeConstants.DefautFrequency+"_" + temp_numCloudlets;
    		try {
    			RealtimeHelper.initLogOutput(
    					RealtimeConstants.ENABLE_OUTPUT,
    					RealtimeConstants.OUTPUT_TO_FILE,
    					RealtimeConstants.OutputFolder,
    					resultFile,
    					RealtimeConstants.VmAllocationPolicy,
    					RealtimeConstants.VmSelectionPolicy,
    					RealtimeConstants.Parameter);
    			os = new FileOutputStream(RealtimeConstants.OutputFolder+"/result/" + resultFile + ".txt");
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

    			vmlist = RealtimeHelper.createVmList(brokerId, temp_numCloudlets);
    			cloudletList = RealtimeHelper.createRealtimeCloudlet(brokerId, vmlist, temp_numCloudlets);
    			hostList = RealtimeHelper.createHostList(RealtimeConstants.NUMBER_OF_HOSTS);

    			datacenter = (PowerDatacenter) RealtimeHelper.createDatacenter("Datacenter", PowerDatacenter.class, hostList, RealtimeConstants.VmAllocationPolicy, null);
    			datacenter.setDisableMigrations(true);
    			broker.submitVmList(vmlist);
    			broker.submitCloudletList(cloudletList);
    			
    			CloudSim.terminateSimulation(RealtimeConstants.SIMULATION_LIMIT);
    			
    			double lastClock = CloudSim.startSimulation();
    			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
    			Log.printLine("Received " + received_cloudlets.size() + " cloudlets");
    			System.out.println(received_cloudlets.size());
    			CloudSim.stopSimulation();

    			Log.setOutput(os);
    			Log.printLine("Base Example end time: " + new Date().toString());
    			RealtimeHelper.printResults(datacenter, vmlist, cloudletList, result, i, received_cloudlets, lastClock, RealtimeConstants.OUTPUT_CSV);
    			Log.setOutput(origional_output);
    			
    			Log.printLine("Base Example Simulation finished!");
    		} catch (Exception e) {
    			e.printStackTrace();
    			Log.printLine("Unwanted errors happen");
    		}
        }
        
        for(int i=0; i<=numExes; i++) {
        	System.out.println(result[i][0]+"  "+result[i][1]+"  "+result[i][2]);
        }
	}
}