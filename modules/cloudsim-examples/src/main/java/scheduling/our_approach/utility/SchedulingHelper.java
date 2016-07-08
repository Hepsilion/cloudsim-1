package scheduling.our_approach.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyDVFSMinimumUsedHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimpleWattPerMipsMetric;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.xml.DvfsDatas;

import scheduling.base_approach.Base_FF_PowerVmAllocation;
import scheduling.base_approach.Base_MBFD_PowerVmAllocation;
import scheduling.base_approach.DVFS_FF_PowerVMAllocation;
import scheduling.base_approach.DVFS_MBFD_PowerVMAllocation;
import scheduling.our_approach.AllocationMapping;
import scheduling.our_approach.InitialVmAllocationPolicy;
import scheduling.our_approach.NormalVmAllocationPolicy;
import scheduling.our_approach.SchedulingHost;

public class SchedulingHelper {
	public static void simulation(List<Cloudlet> cloudletList, List<PowerHost> hostList, List<Vm> vmlist, AllocationMapping mapping, String vmAllocationPolicy) {
		Log.printLine("Simulation started!");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			SchedulingDatacenterBroker broker = (SchedulingDatacenterBroker) SchedulingHelper.createBroker();
			int brokerId = broker.getId();
			
			List<Vm> vms = null;
			List<Cloudlet> cloudlets=null;
			List<PowerHost> hosts=null;
			
			if(vmAllocationPolicy.equals(SchedulingConstants.our_initial_vmAllocationPolicy)){
				vms = SchedulingHelper.createVmList(brokerId, mapping.getNumVms());
				cloudlets = SchedulingHelper.createSchedulingCloudlet(brokerId, vms, mapping.getNumVms());
				hosts = SchedulingHelper.createHostList(SchedulingConstants.NUMBER_OF_HOSTS);
				cloudletList.addAll(cloudlets);
				hostList.addAll(hosts);
				vmlist.addAll(vms);
			} else if(vmAllocationPolicy.equals(SchedulingConstants.our_normal_vmAllocationPolicy)) {
				vms = SchedulingHelper.createVmList(brokerId, mapping.getNumVms());
				cloudlets = SchedulingHelper.createSchedulingCloudlet(brokerId, vms, mapping.getNumVms());
				hosts = SchedulingHelper.createHostList(SchedulingConstants.NUMBER_OF_HOSTS);
				
				List<Vm> remove_vms = new ArrayList<Vm>();
				List<Cloudlet> remove_cloudlets = new ArrayList<Cloudlet>();
				for(Vm vm : vms) {
					if(!vmlist.contains(vm)) {
						remove_vms.add(vm);
					}
				}
				vms.removeAll(remove_vms);
				for(Cloudlet cl : cloudlets) {
					if(!cloudletList.contains(cl)){
						remove_cloudlets.add(cl);
					}
				}
				cloudlets.removeAll(remove_cloudlets);
			} 
			
			PowerDatacenter datacenter = (PowerDatacenter) SchedulingHelper.createDatacenter("Datacenter", SchedulingDatacenter.class, hosts, vmAllocationPolicy, mapping);
			datacenter.setDisableMigrations(true);
			broker.submitVmList(vms);
			broker.submitCloudletList(cloudlets);
			CloudSim.terminateSimulation(SchedulingConstants.SIMULATION_LIMIT);
			
			double lastClock = CloudSim.startSimulation();
			List<Cloudlet> received_cloudlets = broker.getCloudletReceivedList();
			CloudSim.stopSimulation();
			
			SchedulingHelper.printResults(datacenter, mapping, vmlist, cloudletList, received_cloudlets, lastClock, SchedulingConstants.OUTPUT_CSV);
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	public static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new SchedulingDatacenterBroker("SchedulingBroker");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return broker;
	}
	
	public static List<Vm> createVmList(int brokerId, int vmsNumber) {
		List<Vm> vms = new ArrayList<Vm>();
			
		int[] MIPSs=new int[vmsNumber];
		if(SchedulingConstants.DISTRIBUTION.equals("Uniformly"))
			MIPSs= getRandomMIPSs(vmsNumber, SchedulingConstants.VM_MIPS_MIN, SchedulingConstants.VM_MIPS_MAX);
		else if(SchedulingConstants.DISTRIBUTION.equals("Gaussion"))
			MIPSs = getRandomGaussianMIPS(vmsNumber, SchedulingConstants.VM_MIPS_MEAN, SchedulingConstants.VM_MIPS_DEV);
		
		//Parameters for example in paper
		//int[] MIPSs={600,600,600,500,500};
			
		for (int i = 0; i < vmsNumber; i++) {
			int vmType = i / (int) Math.ceil((double) vmsNumber / SchedulingConstants.VM_TYPES);
			vms.add(new SchedulingVm(
					i, 
					brokerId, 
					MIPSs[i],//SchedulingConstants.VM_MIPS[vmType],//
					SchedulingConstants.VM_PES[vmType],
					SchedulingConstants.VM_RAM[vmType], 
					SchedulingConstants.VM_BW, 
					SchedulingConstants.VM_SIZE, 
					1, 
					"Xen",
					new CloudletSchedulerSpaceShared(),
					SchedulingConstants.SCHEDULING_INTERVAL));
		}
		return vms;
	}
	
	//Uniformly Distribution
	public static int[] getRandomMIPSs(int vmNum, int min, int max) {
		Random rand = new Random(200);
		int[] MIPSs = new int[vmNum];
		for (int i = 0; i < vmNum; i++) {
			MIPSs[i] = rand.nextInt(max)%(max-min+1)+min;
		}
		return MIPSs;
	}
	
	//Gaussian Distribution
	public static int[] getRandomGaussianMIPS(int vmNum, int mean, int dev) {
		NormalDistr rand = new NormalDistr(200, mean, dev);
		int[] MIPS = new int[vmNum];
		int temp;
		for(int i=0; i<vmNum; i++){
			temp=(int) rand.sample();
			if(temp<0)
				MIPS[i]=temp*(-1);
			else
				MIPS[i]=temp;
		}
		return MIPS;
	}
	
	public static List<Cloudlet> createSchedulingCloudlet(int userId, List<Vm> vmlist, int num_cloudlets) {
		List<Cloudlet> cloudlets = new LinkedList<Cloudlet>();

		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
		int[] startTime = new int[num_cloudlets];
		int[] execution_time = new int[num_cloudlets];
		if(SchedulingConstants.DISTRIBUTION.equals("Uniformly")){
			startTime = getRandomIntegers(num_cloudlets, SchedulingConstants.CLOUDLET_START_TIME_MIN, SchedulingConstants.CLOUDLET_START_TIME_MAX);
			execution_time = getRandomIntegers(num_cloudlets, SchedulingConstants.CLOUDLET_EXECUTION_TIME_MIN,  SchedulingConstants.CLOUDLET_EXECUTION_TIME_MAX);
		}else if(SchedulingConstants.DISTRIBUTION.equals("Gaussion")){
			startTime = getArrivalTime(200, num_cloudlets);
			execution_time = getRandomGaussianIntegers(num_cloudlets, SchedulingConstants.CLOUDLET_EXECUTION_TIME_MEAN,  SchedulingConstants.CLOUDLET_EXECUTION_TIME_DEV);
		}
		
		//Parameters for example in paper
		//int[] startTime = {1, 1, 1, 2, 2};
		//int[] execution_time = {2, 2, 3, 2, 3};

		Log.printLine("My all cloudlets time information is as follow :");
		SchedulingCloudlet cloudlet= null;
		SchedulingVm vm = null;
		for (int i = 0; i < num_cloudlets; i++) {
			cloudlet = new SchedulingCloudlet(
					i, 
					(long)(VmList.getById(vmlist, i).getMaxMips()*execution_time[i]),//length[i],
					SchedulingConstants.CLOUDLET_PES,
					fileSize, 
					outputSize, 
					startTime[i],
					startTime[i]+execution_time[i],
					utilizationModel, 
					utilizationModel, 
					utilizationModel);
			cloudlet.setVmId(i);
			cloudlet.setUserId(userId);
			cloudlets.add(cloudlet);
			vm = (SchedulingVm) VmList.getById(vmlist, i);
			vm.setCloudlet(cloudlet);
			Log.printLine("MyCloudlet #" + cloudlet.getCloudletId() + "   $Length:" + cloudlet.getCloudletLength() + ";$request start time:" + cloudlet.getStartTime());
		}

		return cloudlets;
	}
	
	public static int[] getArrivalTime(int seed, int num){
		double lambda=num*1.0/24/3600;
		Random rand=new Random(seed);
		int[] times=new int[num];
		times[0]=(int) (-Math.log(rand.nextDouble())/lambda);
		for(int i=1; i<num; i++){
			times[i]= times[i-1]+(int)(-Math.log(rand.nextDouble())/lambda);
		}
		return times;
	}
	
	//Uniformly Distribution
	public static int[] getRandomIntegers(int length, int min, int max) {
		Random rand = new Random(SchedulingConstants.RANDOM_SEED);
		int[] numbers = new int[length];
		//System.out.println("Generate some random numbers");
		for (int i = 0; i < length; i++) {
			numbers[i] = rand.nextInt(max)%(max-min+1)+min;
			//System.out.print(numbers[i]+" ");
		}
		//System.out.println();
		return numbers;
	}
	
	//Gaussian Distribution
	public static int[] getRandomGaussianIntegers(int length, int mean, int dev) {
		NormalDistr rand = new NormalDistr(SchedulingConstants.RANDOM_SEED, mean, dev);
		int[] numbers = new int[length];
		int temp;
		for(int i=0; i<length; i++){
			temp = (int) rand.sample();
			if(temp<0)
				numbers[i]=temp*(-1);
			else
				numbers[i]=temp;
		}
		return numbers;
	}
	
	public static List<PowerHost> createHostList(int hostsNumber) {
		ArrayList<Double> freqs = new ArrayList<Double>(); 
		freqs.add(59.925); 	
		freqs.add(69.93); 	
		freqs.add(79.89);
		freqs.add(89.89);
		freqs.add(100.0);

		HashMap<Integer, String> govs = new HashMap<Integer, String>(); 
		govs.put(0, "My"); // CPU use My(UserSpace、Performance、Conservative、OnDemand) Dvfs mode 

		List<PowerHost> hostList = new ArrayList<PowerHost>();
		PowerHost host = null;
		for (int i = 0; i < hostsNumber; i++) {
			HashMap<String, Integer> tmp_HM_OnDemand = new HashMap<String, Integer>();
			tmp_HM_OnDemand.put("up_threshold", 90);
			tmp_HM_OnDemand.put("sampling_down_factor", 10);
			HashMap<String, Integer> tmp_HM_Conservative = new HashMap<String, Integer>();
			tmp_HM_Conservative.put("up_threshold", 90);
			tmp_HM_Conservative.put("down_threshold", 85);
			tmp_HM_Conservative.put("enablefreqstep", 0);
			tmp_HM_Conservative.put("freqstep", 5);
			HashMap<String, Integer> tmp_HM_UserSpace = new HashMap<String, Integer>();
			tmp_HM_UserSpace.put("frequency", 5);
			HashMap<String, Integer> tmp_HM_My = new HashMap<String, Integer>();
			tmp_HM_My.put("up_threshold", 90);
			tmp_HM_My.put("down_threshold", 85);
			tmp_HM_My.put("frequency", SchedulingConstants.DefautFrequency);

			DvfsDatas ConfigDvfs = new DvfsDatas();
			ConfigDvfs.setHashMapOnDemand(tmp_HM_OnDemand);
			ConfigDvfs.setHashMapConservative(tmp_HM_Conservative);
			ConfigDvfs.setHashMapUserSpace(tmp_HM_UserSpace);
			ConfigDvfs.setHashMapMy(tmp_HM_My);

			int hostType = (i+1) % SchedulingConstants.HOST_TYPES;
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < SchedulingConstants.HOST_PES[hostType]; j++) {
				peList.add(new Pe(
						j, 
						new PeProvisionerSimple(SchedulingConstants.HOST_MIPS[hostType]), 
						freqs, 
						govs.get(j),
						ConfigDvfs));
			}
			host=new PowerHost(
					i, 
					new RamProvisionerSimple(SchedulingConstants.HOST_RAM[hostType]),
					new BwProvisionerSimple(SchedulingConstants.HOST_BW), 
					SchedulingConstants.HOST_STORAGE, 
					peList,
					new VmSchedulerTimeShared(peList),
					new PowerModelSpecPower_BAZAR_ME(peList),
					SchedulingConstants.ENABLE_ONOFF, 
					SchedulingConstants.ENABLE_DVFS);
			hostList.add(host);
		}
		return hostList;
	}
	
	public static Datacenter createDatacenter(String name, Class<? extends Datacenter> datacenterClass, List<PowerHost> hostList, String vmAllocationPolicy, AllocationMapping mapping) throws Exception {
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, 
				os, 
				vmm, 
				hostList, 
				time_zone,
				cost, 
				costPerMem, 
				costPerStorage, 
				costPerBw);
		Datacenter datacenter = null;
		try {
			datacenter = datacenterClass.getConstructor(String.class, DatacenterCharacteristics.class, VmAllocationPolicy.class, List.class, Double.TYPE).newInstance(
					name,
					characteristics,
					getVmAllocationPolicy(vmAllocationPolicy, hostList, mapping),
					new LinkedList<Storage>(), 
					SchedulingConstants.SCHEDULING_INTERVAL);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return datacenter;
	}
	
	protected static VmAllocationPolicy getVmAllocationPolicy(String vmAllocationPolicyName, List<PowerHost> hostList, AllocationMapping mapping) {
		VmAllocationPolicy vmAllocationPolicy = null;
		if (vmAllocationPolicyName.equals("normal")) {
			vmAllocationPolicy = new NormalVmAllocationPolicy(hostList, mapping);
		} else if (vmAllocationPolicyName.equals("init")) {
			vmAllocationPolicy = new InitialVmAllocationPolicy(hostList, mapping);
		} else if(vmAllocationPolicyName.equals("mbfd")) {
			vmAllocationPolicy = new Base_MBFD_PowerVmAllocation(hostList);
		} else if (vmAllocationPolicyName.equals("dvfs_mbfd")) {
			vmAllocationPolicy = new DVFS_MBFD_PowerVMAllocation(hostList);
		} else if (vmAllocationPolicyName.equals("ff")) {
			vmAllocationPolicy = new Base_FF_PowerVmAllocation(hostList);
		} else if (vmAllocationPolicyName.equals("dvfs_ff")) {
			vmAllocationPolicy = new DVFS_FF_PowerVMAllocation(hostList);
		} else if (vmAllocationPolicyName.equals("mu")) {
			vmAllocationPolicy = new PowerVmAllocationPolicyDVFSMinimumUsedHost(hostList);
		} else if (vmAllocationPolicyName.equals("swpmm")) {
			vmAllocationPolicy = new PowerVmAllocationPolicySimpleWattPerMipsMetric(hostList);
		} else {
			System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
			System.exit(0);
		}
		return vmAllocationPolicy;
	}
	
	public static int[] getRandomPermitation(int length) {
		int[] result = new int[length];
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<length; i++) {
			list.add(i);
		}
		Collections.shuffle(list);
		Iterator<Integer> iterator = list.iterator();
		for(int i=0; i<length; i++) {
			result[i]=iterator.next();
		}
		return result;
	}
	
	public static List<Cloudlet> getCopyOfCloudlets(List<Cloudlet> cloudlets) {
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		SchedulingCloudlet temp = null;
		for(Cloudlet cl : cloudlets) {
			temp = (SchedulingCloudlet) cl;
			cloudletList.add(new SchedulingCloudlet(temp.getCloudletId(), temp.getCloudletLength(), temp.getNumberOfPes(), temp.getCloudletFileSize(), temp.getCloudletOutputSize(), temp.getStartTime(), temp.getDeadline(), temp.getUtilizationModelCpu(), temp.getUtilizationModelRam(), temp.getUtilizationModelRam()));
		}
		return cloudletList;
	}
	
	public static void getOrderedCloudletOnSchedulingHost(AllocationMapping mapping, SchedulingHost[] hosts, List<Cloudlet> cloudletList) {
		for(int i=0; i<mapping.getNumVms(); i++) {
			int hostId = mapping.getHostOfVm(i);
			if(hostId!=-1)
				hosts[hostId].addCloudlet((SchedulingCloudlet)CloudletList.getById(cloudletList, i));
		}
	}
	
	public static void initOutput(String logFile, String resultFile, String tmpResultFile) {
		try {
			SchedulingHelper.initLogOutput(
					SchedulingConstants.ENABLE_OUTPUT,
					SchedulingConstants.OUTPUT_TO_FILE,
					SchedulingConstants.OutputFolder,
					logFile,
					resultFile,
					tmpResultFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
	}
	
	protected static void initLogOutput(
			boolean enableOutput, 
			boolean outputToFile, 
			String outputFolder,
			String logFile,
			String resultFile, 
			String tmpResultFile) throws IOException, FileNotFoundException {
		Log.setDisabled(!enableOutput);
		if (enableOutput && outputToFile) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			File folder2 = new File(outputFolder + "/log");
			if (!folder2.exists()) {
				folder2.mkdir();
			}
			
			File folder3 = new File(outputFolder + "/result");
			if (!folder3.exists()) {
				folder3.mkdir();
			}

			if(logFile!=null){
				File file = new File(outputFolder + "/" + logFile + ".txt");
				file.createNewFile();
				Log.setOutput(new FileOutputStream(file));
			}
			
			if(resultFile!=null){
				File result = new File(outputFolder + "/" + resultFile + ".txt");
				result.createNewFile();
			}
			
			if(tmpResultFile!=null){
				File tempResult = new File(outputFolder + "/" + tmpResultFile + ".txt");
				tempResult.createNewFile();
			}
		}
	}
	
	public static OutputStream getOutputStream(String output) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(SchedulingConstants.OutputFolder+"/" + output + ".txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return outputStream;
	}
	
	public static void outputResultToResultFile(String ith, OutputStream originOutput, OutputStream result_output, AllocationMapping mapping, int where) {
		if(where==1) {
			SchedulingHelper.outputToResultFile(originOutput, result_output, "************************************************************************************************************************");
			SchedulingHelper.outputToResultFile(ith, originOutput, result_output, mapping, where);
			SchedulingHelper.outputToResultFile(originOutput, result_output, "************************************************************************************************************************\n");
		}else if(where==2) {
			SchedulingHelper.outputToResultFile(originOutput, result_output, "**********************************************************************************************");
			SchedulingHelper.outputToResultFile(ith, originOutput, result_output, mapping, where);
			SchedulingHelper.outputToResultFile(originOutput, result_output, "**********************************************************************************************");
		}else if(where==3) {
			SchedulingHelper.outputToResultFile(originOutput, result_output, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			SchedulingHelper.outputToResultFile(ith, originOutput, result_output, mapping, where);
			SchedulingHelper.outputToResultFile(originOutput, result_output, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		}else if(where==4) {
			SchedulingHelper.outputToResultFile(ith, originOutput, result_output, mapping, where);
			SchedulingHelper.outputToResultFile(originOutput, result_output, "\n");
		}
	}
	
	public static void outputToResultFile(String ith, OutputStream originOutput, OutputStream result_output, AllocationMapping mapping, int where) {
		Log.setOutput(result_output);
		Log.printLine(ith+ " Allocation:\n"+mapping);
		Log.printLine(String.format("Clouelet Acceptance Rate = %.5f%%", mapping.getTask_acceptance_rate() * 100));
		Log.printLine(String.format("Energy consumption = %.5f kwh", mapping.getEnergy()/3600/1000));
		Log.printLine(String.format("Energy Efficiency: %.5f", mapping.getNumInstruction()/mapping.getEnergy()));
		Log.printLine("Num of Instructions = " + mapping.getNumInstruction());
		Log.printLine(String.format("Allocation Fitness = %.5f", mapping.getFitness()));
		
		Log.setOutput(originOutput);
	}
	
	/**
	 * Ouput <b>message</b> to <b>result_output</b>, then restore outputstream to <b>originOutput</b>
	 * @param originOutput
	 * @param result_output
	 * @param message
	 */
	public static void outputToResultFile(OutputStream originOutput, OutputStream result_output, String message) {
		Log.setOutput(result_output);
		Log.printLine(message);
		Log.setOutput(originOutput);
	}
	
	public static void printResults(PowerDatacenter datacenter, AllocationMapping mapping, List<Vm> vms, List<Cloudlet> cloudlets, List<Cloudlet> received_cloudlets, double lastClock, boolean outputInCsv) {
		Log.enable();
		Log.printLine();
		
		Log.printLine("Received " + received_cloudlets.size() + " cloudlets of " + cloudlets.size()+ " submitted cloudlets");
		SchedulingHelper.printCloudletList(received_cloudlets, datacenter.getHostList(), vms);
		
		double acceptance_rate = received_cloudlets.size() * 1.0 / cloudlets.size();
		SchedulingCloudlet rc=null;
		long numInstructions=0;
		int declined_cloudlet_num = 0;
		for(int i=0; i<received_cloudlets.size(); i++) {
			rc = (SchedulingCloudlet) received_cloudlets.get(i);
			//if(rc.getFinishTime()<=rc.getDeadline())
				numInstructions+=rc.getCloudletLength();
//			else
//				declined_cloudlet_num++;
		}
		double fitness=1.0*numInstructions/datacenter.getPower();
		if(cloudlets.size()>0) {
			fitness = fitness*Math.pow(1.0*received_cloudlets.size()/cloudlets.size(), 3);
		}
		//double fitness = received_cloudlets.size()/(datacenter.getPower()/1000/3600);
		if(mapping!=null){
			mapping.setTask_acceptance_rate(acceptance_rate);
			mapping.setEnergy(datacenter.getPower());
			mapping.setFitness(fitness);
			mapping.setNumInstruction(numInstructions);
		}
		
		Helper.printResults(datacenter, vms, lastClock, null, outputInCsv, SchedulingConstants.OutputFolder);
		Log.setDisabled(false);
		Log.printLine(String.format("Clouelet Acceptance Rate: %.5f%%", acceptance_rate * 100));
		Log.printLine(String.format("Deadline Missing Rate: %.4f%%", declined_cloudlet_num*1.0/cloudlets.size()*100));
		Log.printLine(String.format("Energy consumption: %.5f Ws", datacenter.getPower()));
		Log.printLine(String.format("Energy Efficiency: %.5f", 1.0*numInstructions/datacenter.getPower()));
		Log.printLine(String.format("Allocation Fitness: %.5f", fitness));
		
		Log.printLine();
		Log.printLine();
		Log.printLine();
		Log.printLine();
	}
	
	public static void printCloudletList(List<Cloudlet> cloudlets, List<Host> hosts, List<Vm>vms) {
		SchedulingCloudlet cloudlet = null;

		String indent = "\t";
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("CloudletID" + indent + "STATUS" + indent + indent + "ResourceID" + indent + "VmID" + indent+"VmMIPS" + indent+ indent
				+ "Length" + indent + indent + "CPUTime" + indent + "StartExecTime" + indent + "FinishTime" + indent
				+ "RequestStartTime" + indent + "Deadline");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < cloudlets.size(); i++) {
			cloudlet = (SchedulingCloudlet) cloudlets.get(i);
			Log.print(indent + cloudlet.getCloudletId());
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + indent + "SUCCESS" + indent + indent 
						+ cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent 
						+ VmList.getById(vms, cloudlet.getCloudletId()).getMaxMips() + indent + indent 
						+ cloudlet.getCloudletLength() + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()) + indent + indent 
						+ cloudlet.getStartTime() + indent + indent + indent 
						+ (cloudlet.getDeadline() == Double.MAX_VALUE ? "Not limited": dft.format(cloudlet.getDeadline())));
			}
		}
	}
}
