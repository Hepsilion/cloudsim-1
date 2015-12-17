package experiments.paper1.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyDVFSMinimumUsedHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationInterQuartileRange;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegressionRobust;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimpleWattPerMipsMetric;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyRandomSelection;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_ME;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.xml.DvfsDatas;

import experiments.paper1.dvfs.BasePowerVmAllocation;
import experiments.paper1.dvfs.DvfsBase1PowerVmAllocation;
import experiments.paper1.dvfs.DvfsBase2PowerVmAllocation;
import experiments.paper1.dvfs.DvfsPowerVmAllocation;
import experiments.paper1.ga.ChromTaskScheduling;
import experiments.paper1.ga.Chromosome;
import experiments.paper1.ga.GaInitialVmAllocationPolicy;
import experiments.paper1.ga.GaPowerVmAllocationPolicy;


public class RealtimeHelper {
	private static Random rand = new Random(RealtimeConstants.RANDOM_SEED);
	/**
	 * Creates the broker.
	 * @return the datacenter broker
	 */
	public static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new RealtimeDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return broker;
	}

	public static List<Cloudlet> createRealtimeCloudlet(int userId, List<Vm> vmlist, int num_cloudlets) {
		// Creates a container to store Cloudlets
		List<Cloudlet> cloudlets = new LinkedList<Cloudlet>();

		// cloudlet parameters
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		// length, start time and deadline
		int[] length = getRandomIntegers(num_cloudlets, RealtimeConstants.CLOUDLET_LENGTH/10, RealtimeConstants.CLOUDLET_LENGTH);
		int[] startTime = getRandomIntegers(num_cloudlets, 0, 3600*24);
		int[] execution_time = getRandomIntegers(num_cloudlets, 3600*14,  3600*16);

		Log.printLine("My all cloudlets time information is as follow :");
		RealtimeCloudlet cloudlet= null;
		for (int i = 0; i < num_cloudlets; i++) {
			cloudlet = new RealtimeCloudlet(
					i, 
					(long)(VmList.getById(vmlist, i).getMaxMips()*execution_time[i]),//length[i],
					RealtimeConstants.CLOUDLET_PES,
					fileSize, 
					outputSize, 
					utilizationModel, 
					utilizationModel, 
					utilizationModel, 
					startTime[i]-100,
					startTime[i]+execution_time[i]);//startTime[i]+length[i]/VmList.getById(vmlist, i).getMaxMips()+50);
			// setting the owner of these Cloudlets
			cloudlet.setVmId(i);
			cloudlet.setUserId(userId);
			cloudlets.add(cloudlet);
			((RealtimeVm)VmList.getById(vmlist, i)).setCloudlet(cloudlet);
			Log.printLine("MyCloudlet #" + cloudlet.getCloudletId() + "   $Length:" + cloudlet.getCloudletLength() + ";$request start time:" + cloudlet.getStartTime() + ";$request deadline:" + cloudlet.getDeadline());
		}

		return cloudlets;
	}

	public static int[] getRandomHosts(int length, int min, int max) {
		Random random = new Random(new Random().nextInt());
		//System.out.println("Random Hosts:");
		int[] numbers = new int[length];
		for (int i = 0; i < length; i++) {
			numbers[i] = random.nextInt(max)%(max-min+1)+min;
			//System.out.print(numbers[i]+" ");
		}
		//System.out.println();
		return numbers;
	}
	
	public static int[] getRandomFrequencies(int length, int min, int max) {
		Random rand = new Random(new Random().nextInt());
		//System.out.println("Random Frequencies:");
		int[] numbers = new int[length];
		for (int i = 0; i < length; i++) {
			numbers[i] = rand.nextInt(max)%(max-min+1)+min;
			//System.out.print(numbers[i]+" ");
		}
		//System.out.println();
		return numbers;
	}
	
	public static int[] getRandomIntegers(int length, int min, int max) {
		Random rand = new Random(RealtimeConstants.RANDOM_SEED);
		int[] numbers = new int[length];
		//System.out.println("Generate some random numbers");
		for (int i = 0; i < length; i++) {
			numbers[i] = rand.nextInt(max)%(max-min+1)+min;
			//System.out.print(numbers[i]+" ");
		}
		//System.out.println();
		return numbers;
	}
	
	public static int getRandomInteger(int min, int max) {
		int number = rand.nextInt(max)%(max-min+1)+min;
		return number;
	}

	/**
	 * Creates the vm list.
	 * 
	 * @param brokerId
	 *            the broker id
	 * @param vmsNumber
	 *            the vms number
	 * 
	 * @return the list< vm>
	 */
	public static List<Vm> createVmList(int brokerId, int vmsNumber, Chromosome chrom) {
		List<Vm> vms = new ArrayList<Vm>();
		int[] MIPSs = getRandomMIPSs(vmsNumber, 200, 1000);//200 2000
		for (int i = 0; i < vmsNumber; i++) {
			int vmType = i / (int) Math.ceil((double) vmsNumber / RealtimeConstants.VM_TYPES);
			int hostId = ((ChromTaskScheduling)chrom).getHostByVm(i);
			int frequency = ((ChromTaskScheduling) chrom).getFreqByVm(i);
			vms.add(new RealtimeVm(
					i, 
					brokerId, 
					MIPSs[i],//RealtimeConstants.VM_MIPS[vmType], //
					RealtimeConstants.VM_PES[vmType],
					RealtimeConstants.VM_RAM[vmType], 
					RealtimeConstants.VM_BW, 
					RealtimeConstants.VM_SIZE, 
					1, 
					"Xen",
					new CloudletSchedulerSpaceShared(),
					RealtimeConstants.SCHEDULING_INTERVAL, 
					hostId==-1 ? getRandomInteger(0, RealtimeConstants.NUMBER_OF_HOSTS-1):hostId, 
					frequency));
		}
		return vms;
	}

	public static List<Vm> createVmList(int brokerId, int vmsNumber) {
		List<Vm> vms = new ArrayList<Vm>();
		int[] MIPSs = getRandomMIPSs(vmsNumber, 200, 1000);
		for (int i = 0; i < vmsNumber; i++) {
			int vmType = i / (int) Math.ceil((double) vmsNumber / RealtimeConstants.VM_TYPES);
			vms.add(new RealtimeVm(
					i, 
					brokerId, 
					MIPSs[i],//RealtimeConstants.VM_MIPS[vmType], 
					RealtimeConstants.VM_PES[vmType],
					RealtimeConstants.VM_RAM[vmType], 
					RealtimeConstants.VM_BW, 
					RealtimeConstants.VM_SIZE, 
					1, 
					"Xen",
					new CloudletSchedulerSpaceShared(),
					RealtimeConstants.SCHEDULING_INTERVAL));
		}
		return vms;
	}
	
	public static int[] getRandomMIPSs(int vmNum, int min, int max) {
		Random rand = new Random(200);
		int[] MIPSs = new int[vmNum];
		for (int i = 0; i < vmNum; i++) {
			MIPSs[i] = rand.nextInt(max)%(max-min+1)+min;
		}
		return MIPSs;
	}

	/**
	 * Creates the host list.
	 * 
	 * @param hostsNumber
	 *            the hosts number
	 * 
	 * @return the list< power host>
	 */
	public static List<PowerHost> createHostList(int hostsNumber) {
		// frequencies available by the CPU
		ArrayList<Double> freqs = new ArrayList<Double>(); 
		freqs.add(59.925); // frequencies are defined in % , it make free to use Host MIPS like we want.
		freqs.add(69.93); // frequencies must be in increase order !
		freqs.add(79.89);
		freqs.add(89.89);
		freqs.add(100.0);

		// Define wich governor is used by each CPU
		HashMap<Integer, String> govs = new HashMap<Integer, String>(); 
		govs.put(0, "My"); // CPU 0 use My(UserSpace、Performance、Conservative、OnDemand) Dvfs mode 

		List<PowerHost> hostList = new ArrayList<PowerHost>();
		RealtimeHost host = null;
		int[] num=new int[2];
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
			tmp_HM_My.put("frequency", RealtimeConstants.DefautFrequency);

			DvfsDatas ConfigDvfs = new DvfsDatas();
			ConfigDvfs.setHashMapOnDemand(tmp_HM_OnDemand);
			ConfigDvfs.setHashMapConservative(tmp_HM_Conservative);
			ConfigDvfs.setHashMapUserSpace(tmp_HM_UserSpace);
			ConfigDvfs.setHashMapMy(tmp_HM_My);

			int hostType = (i+1) % RealtimeConstants.HOST_TYPES;
			num[hostType]++;
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < RealtimeConstants.HOST_PES[hostType]; j++) {
				peList.add(new Pe(
						j, 
						new PeProvisionerSimple(RealtimeConstants.HOST_MIPS[hostType]), 
						freqs, 
						govs.get(j),
						ConfigDvfs));
			}
			host=new RealtimeHost(
					i, 
					new RamProvisionerSimple(RealtimeConstants.HOST_RAM[hostType]),
					new BwProvisionerSimple(RealtimeConstants.HOST_BW), 
					RealtimeConstants.HOST_STORAGE, 
					peList,
					new VmSchedulerTimeShared(peList),
					new PowerModelSpecPower_BAZAR_ME(peList),
					RealtimeConstants.ENABLE_ONOFF, 
					RealtimeConstants.ENABLE_DVFS);
			host.setFrequency(host.getPeList().get(0).getIndexFreq());
			hostList.add(host);
		}
//		System.out.println("Num of Host:");
//		for(int i=0; i<2; i++)
//			System.out.println(num[i]);
		return hostList;
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * @param datacenterClass
	 *            the datacenter class
	 * @param hostList
	 *            the host list
	 * @param vmAllocationPolicy
	 *            the vm allocation policy
	 * @param simulationLength
	 * 
	 * @return the power datacenter
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static Datacenter createDatacenter(String name, Class<? extends Datacenter> datacenterClass, List<PowerHost> hostList, String vmAllocationPolicy, Chromosome chrom) throws Exception {
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
					getVmAllocationPolicy(vmAllocationPolicy,RealtimeConstants.VmSelectionPolicy, RealtimeConstants.Parameter, hostList, chrom),
					new LinkedList<Storage>(), 
					RealtimeConstants.SCHEDULING_INTERVAL);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return datacenter;
	}

	/**
	 * Gets the vm allocation policy.
	 * 
	 * @param vmAllocationPolicyName
	 *            the vm allocation policy name
	 * @param vmSelectionPolicyName
	 *            the vm selection policy name
	 * @param parameterName
	 *            the parameter name
	 * @return the vm allocation policy
	 */
	protected static VmAllocationPolicy getVmAllocationPolicy(String vmAllocationPolicyName,
			String vmSelectionPolicyName, String parameterName, List<PowerHost> hostList, Chromosome chrom) {
		VmAllocationPolicy vmAllocationPolicy = null;
		PowerVmSelectionPolicy vmSelectionPolicy = null;
		if (!vmSelectionPolicyName.isEmpty()) {
			vmSelectionPolicy = getVmSelectionPolicy(vmSelectionPolicyName);
		}
		double parameter = 0;
		if (!parameterName.isEmpty()) {
			parameter = Double.valueOf(parameterName);
		}
		if (vmAllocationPolicyName.equals("iqr")) {
			PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, vmSelectionPolicy, 1);//TODO 我把过载阈值从0.7改成了0.9
			vmAllocationPolicy = new PowerVmAllocationPolicyMigrationInterQuartileRange(hostList, vmSelectionPolicy, parameter, fallbackVmSelectionPolicy);
		} else if (vmAllocationPolicyName.equals("mad")) {
			PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, vmSelectionPolicy, 0.9);
			vmAllocationPolicy = new PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation(hostList,vmSelectionPolicy, parameter, fallbackVmSelectionPolicy);
		} else if (vmAllocationPolicyName.equals("lr")) {
			PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, vmSelectionPolicy, 0.9);
			vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegression(hostList, vmSelectionPolicy, parameter, RealtimeConstants.SCHEDULING_INTERVAL, fallbackVmSelectionPolicy);
		} else if (vmAllocationPolicyName.equals("lrr")) {
			PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, vmSelectionPolicy, 0.9);
			vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegressionRobust(hostList, vmSelectionPolicy, parameter, RealtimeConstants.SCHEDULING_INTERVAL, fallbackVmSelectionPolicy);
		} else if (vmAllocationPolicyName.equals("thr")) {
			vmAllocationPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, vmSelectionPolicy, parameter);
		} else if (vmAllocationPolicyName.equals("base")) {
            vmAllocationPolicy = new BasePowerVmAllocation(hostList);
        } else if (vmAllocationPolicyName.equals("dvfs_my")) {
            vmAllocationPolicy = new DvfsPowerVmAllocation(hostList);
        } else if (vmAllocationPolicyName.equals("dvfs_base1")) {
            vmAllocationPolicy = new DvfsBase1PowerVmAllocation(hostList);
        } else if (vmAllocationPolicyName.equals("dvfs_base2")) {
        	vmAllocationPolicy = new DvfsBase2PowerVmAllocation(hostList);
        } else if(vmAllocationPolicyName.equals("dvfs_muh")) {
		    vmAllocationPolicy = new PowerVmAllocationPolicyDVFSMinimumUsedHost(hostList);
		} else if (vmAllocationPolicyName.equals("dvfs_SimpleWatt")) {
			vmAllocationPolicy = new PowerVmAllocationPolicySimpleWattPerMipsMetric(hostList);
		} else if (vmAllocationPolicyName.equals("ga")) {
			vmAllocationPolicy = new GaPowerVmAllocationPolicy(hostList, chrom);
		} else if (vmAllocationPolicyName.equals("ga_init")) {
			vmAllocationPolicy = new GaInitialVmAllocationPolicy(hostList, chrom);
		} else {
			System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
			System.exit(0);
		}
		return vmAllocationPolicy;
	}

	/**
	 * Gets the vm selection policy.
	 * 
	 * @param vmSelectionPolicyName
	 *            the vm selection policy name
	 * @return the vm selection policy
	 */
	protected static PowerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
		PowerVmSelectionPolicy vmSelectionPolicy = null;
		if (vmSelectionPolicyName.equals("mc")) {
			vmSelectionPolicy = new PowerVmSelectionPolicyMaximumCorrelation(new PowerVmSelectionPolicyMinimumMigrationTime());
		} else if (vmSelectionPolicyName.equals("mmt")) {
			vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
		} else if (vmSelectionPolicyName.equals("mu")) {
			vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
		} else if (vmSelectionPolicyName.equals("rs")) {
			vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
		} else {
			System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
			System.exit(0);
		}
		return vmSelectionPolicy;
	}

	public static void printCloudletList(List<Cloudlet> cloudlets, List<Host> hosts, List<Vm>vms) {
		RealtimeCloudlet cloudlet = null;

		String indent = "\t";
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + indent + "ResourceID" + indent + "VmID" + indent+"VM MIPS" + indent+ indent
				+ "Length" + indent + indent + "CPUTime" + indent + "StartExecTime" + indent + "FinishTime" + indent
				+ "RequestStartTime" + indent + "Deadline");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < cloudlets.size(); i++) {
			cloudlet = (RealtimeCloudlet) cloudlets.get(i);
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

	protected static double getDeadlineMissingRate(List<Cloudlet> received_cloudlets) {
        int num_missing_deadline = 0;
        for (Cloudlet cl : received_cloudlets) {
            RealtimeCloudlet cloudlet = (RealtimeCloudlet) cl;
            double finishTime = cloudlet.getFinishTime();
            double deadline = cloudlet.getDeadline();
            if (finishTime > deadline) {
                num_missing_deadline++;
            }
        }
        return 1.0 * num_missing_deadline / received_cloudlets.size();
    }

	/**
	 * Prints the results.
	 * 
	 * @param datacenter
	 *            the datacenter
	 * @param lastClock
	 *            the last clock
	 * @param experimentName
	 *            the experiment name
	 * @param outputInCsv
	 *            the output in csv
	 * @param outputFolder
	 *            the output folder
	 */
	public static double[] printResults(PowerDatacenter datacenter, List<Vm> vms, List<Cloudlet> cloudlets, double[][] result, int index, List<Cloudlet> received_cloudlets, double lastClock, boolean outputInCsv) {
		Log.enable();

		List<Host> hosts = datacenter.getHostList();
		Log.printLine("Received " + received_cloudlets.size() + " cloudlets of " + cloudlets.size()+ " submitted cloudlets");
		printCloudletList(received_cloudlets, hosts, vms);
		
		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);
		int numberOfMigrations = datacenter.getMigrationCount();
		
		double tdr = (cloudlets.size()-received_cloudlets.size()) * 1.0 / cloudlets.size();
		double dmr = getDeadlineMissingRate(received_cloudlets);
		
		double tmp1=10*(1-tdr), tmp2=10*(1-dmr);
		//double overall_sla = tmp1*tmp1*tmp2*tmp2*tmp2*(100-energy);
		long numInstructions=0;
		RealtimeCloudlet rc=null;
		for(int i=0; i<received_cloudlets.size(); i++) {
			rc = (RealtimeCloudlet) received_cloudlets.get(i);
			//if(rc.getFinishTime()<=rc.getDeadline())
				numInstructions+=rc.getCloudletLength();
		}
		double overall_sla=numInstructions/datacenter.getPower();
		//System.out.println("#Instr="+numInstructions+", #Energy="+energy+", fitness="+overall_sla);
		
		double[] ga_result = new double[4];
		ga_result[0]=tdr;
		ga_result[1]=dmr;
		ga_result[2]=energy;
		ga_result[3]=overall_sla;
		
		if(result != null) {
			result[index][0] = tdr;
			result[index][1] = dmr;
			result[index][2] = energy;
		}
		
		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		// double slaTimePerVmWithMigration =
		// slaMetrics.get("sla_time_per_vm_with_migration");
		// double slaTimePerVmWithoutMigration =
		// slaMetrics.get("sla_time_per_vm_without_migration");
		// double slaTimePerHost = getSlaTimePerHost(hosts);
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

		int numberOfHostShutdowns = timeBeforeHostShutdown.size();

		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}

		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}

		String experimentName = getExperimentName(RealtimeConstants.VmAllocationPolicy, RealtimeConstants.VmSelectionPolicy, RealtimeConstants.Parameter);
		String outputFolder = RealtimeConstants.OutputFolder;
		if (outputInCsv) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File folder1 = new File(outputFolder + "/stats");
			if (!folder1.exists()) {
				folder1.mkdir();
			}
			File folder2 = new File(outputFolder + "/time_before_host_shutdown");
			if (!folder2.exists()) {
				folder2.mkdir();
			}
			File folder3 = new File(outputFolder + "/time_before_vm_migration");
			if (!folder3.exists()) {
				folder3.mkdir();
			}
			File folder4 = new File(outputFolder + "/metrics");
			if (!folder4.exists()) {
				folder4.mkdir();
			}

			StringBuilder data = new StringBuilder();
			String delimeter = ",";

			data.append(experimentName + delimeter);
			data.append(parseExperimentName(experimentName));
			data.append(String.format("%d", numberOfHosts) + delimeter);
			data.append(String.format("%d", numberOfVms) + delimeter);
			data.append(String.format("%.2f", totalSimulationTime) + delimeter);
			data.append(String.format("%.5f", energy) + delimeter);
			data.append(String.format("%d", numberOfMigrations) + delimeter);
			data.append(String.format("%.10f", sla) + delimeter);
			data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
			data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
			data.append(String.format("%.10f", slaOverall) + delimeter);
			data.append(String.format("%.10f", slaAverage) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithMigration) +
			// delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithoutMigration) +
			// delimeter);
			// data.append(String.format("%.5f", slaTimePerHost) + delimeter);
			data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());

				data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

				writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName + "_metric");
			}

			data.append("\n");

			writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
			writeDataColumn(timeBeforeHostShutdown,
					outputFolder + "/time_before_host_shutdown/" + experimentName + "_time_before_host_shutdown.csv");
			writeDataColumn(timeBeforeVmMigration,
					outputFolder + "/time_before_vm_migration/" + experimentName + "_time_before_vm_migration.csv");

		} else {
			Log.setDisabled(false);
			Log.printLine();
			Log.printLine(String.format("Experiment name: " + experimentName));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine(String.format("Energy consumption: %.5f kWh", energy));
			//System.out.println(String.format("Energy consumption: %.5f kWh", energy));
			Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
			Log.printLine(String.format("SLA: %.5f%%", sla * 100));
			Log.printLine(String.format("SLA perf degradation due to migration: %.2f%%", slaDegradationDueToMigration * 100));
			Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
			Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
			Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
			Log.printLine(String.format("Declined Clouelet Rate: %.2f%%", tdr * 100));
			//System.out.println(String.format("Declined Clouelet Rate: %.2f%%", tdr * 100));
			Log.printLine(String.format("Deadline Missing Rate: %.2f%%", dmr * 100));
			//System.out.println(String.format("Deadline Missing Rate: %.2f%%", dmr * 100));
			// Log.printLine(String.format("SLA time per VM with migration:
			// %.2f%%", slaTimePerVmWithMigration * 100));
			// Log.printLine(String.format("SLA time per VM without migration:
			// %.2f%%", slaTimePerVmWithoutMigration * 100));
			// Log.printLine(String.format("SLA time per host: %.2f%%",
			// slaTimePerHost * 100));
			Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
			Log.printLine(String.format("Mean time before a host shutdown: %.2f sec", meanTimeBeforeHostShutdown));
			Log.printLine(String.format("StDev time before a host shutdown: %.2f sec", stDevTimeBeforeHostShutdown));
			Log.printLine(String.format("Mean time before a VM migration: %.2f sec", meanTimeBeforeVmMigration));
			Log.printLine(String.format("StDev time before a VM migration: %.2f sec", stDevTimeBeforeVmMigration));

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());

				Log.printLine(
						String.format("Execution time - VM selection mean: %.5f sec", executionTimeVmSelectionMean));
				Log.printLine(
						String.format("Execution time - VM selection stDev: %.5f sec", executionTimeVmSelectionStDev));
				Log.printLine(String.format("Execution time - host selection mean: %.5f sec",
						executionTimeHostSelectionMean));
				Log.printLine(String.format("Execution time - host selection stDev: %.5f sec",
						executionTimeHostSelectionStDev));
				Log.printLine(String.format("Execution time - VM reallocation mean: %.5f sec",
						executionTimeVmReallocationMean));
				Log.printLine(String.format("Execution time - VM reallocation stDev: %.5f sec",
						executionTimeVmReallocationStDev));
				Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
				Log.printLine(String.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
			}
			Log.printLine();
		}

		// Log.setDisabled(true);

		return ga_result;
	}

	/**
	 * Gets the experiment name.
	 * 
	 * @param args
	 *            the args
	 * @return the experiment name
	 */
	protected static String getExperimentName(String... args) {
		StringBuilder experimentName = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (args[i].isEmpty()) {
				continue;
			}
			if (i != 0) {
				experimentName.append("_");
			}
			experimentName.append(args[i]);
		}
		return experimentName.toString();
	}

	/**
	 * Parses the experiment name.
	 * 
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String parseExperimentName(String name) {
		Scanner scanner = new Scanner(name);
		StringBuilder csvName = new StringBuilder();
		scanner.useDelimiter("_");
		for (int i = 0; i < 4; i++) {
			if (scanner.hasNext()) {
				csvName.append(scanner.next() + ",");
			} else {
				csvName.append(",");
			}
		}
		scanner.close();
		return csvName.toString();
	}

	/**
	 * Gets the sla time per active host.
	 * 
	 * @param hosts
	 *            the hosts
	 * @return the sla time per active host
	 */
	protected static double getSlaTimePerActiveHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsActive = true;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1 && previousIsActive) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsActive = entry.isActive();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla time per host.
	 * 
	 * @param hosts
	 *            the hosts
	 * @return the sla time per host
	 */
	protected static double getSlaTimePerHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla metrics.
	 * 
	 * @param vms
	 *            the vms
	 * @return the sla metrics
	 */
	protected static Map<String, Double> getSlaMetrics(List<Vm> vms) {
		Map<String, Double> metrics = new HashMap<String, Double>();
		List<Double> slaViolation = new LinkedList<Double>();
		double totalAllocated = 0;
		double totalRequested = 0;
		double totalUnderAllocatedDueToMigration = 0;

		for (Vm vm : vms) {
			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
			double vmUnderAllocatedDueToMigration = 0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsInMigration = false;

			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						slaViolation.add((previousRequested - previousAllocated) / previousRequested);
						if (previousIsInMigration) {
							vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated) * timeDiff;
						}
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsInMigration = entry.isInMigration();
			}

			totalAllocated += vmTotalAllocated;
			totalRequested += vmTotalRequested;
			totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration;
		}

		metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
		if (slaViolation.isEmpty()) {
			metrics.put("average", 0.);
		} else {
			metrics.put("average", MathUtil.mean(slaViolation));
		}
		metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
		// metrics.put("sla_time_per_vm_with_migration",
		// slaViolationTimePerVmWithMigration / totalTime);
		// metrics.put("sla_time_per_vm_without_migration",
		// slaViolationTimePerVmWithoutMigration / totalTime);

		return metrics;
	}

	/**
	 * Write data column.
	 * 
	 * @param data
	 *            the data
	 * @param outputPath
	 *            the output path
	 */
	public static void writeDataColumn(List<? extends Number> data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Number value : data) {
				writer.write(value.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write data row.
	 * 
	 * @param data
	 *            the data
	 * @param outputPath
	 *            the output path
	 */
	public static void writeDataRow(String data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write metric history.
	 * 
	 * @param hosts
	 *            the hosts
	 * @param vmAllocationPolicy
	 *            the vm allocation policy
	 * @param outputPath
	 *            the output path
	 */
	public static void writeMetricHistory(List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy, String outputPath) {
		// for (Host host : hosts) {
		for (int j = 0; j < 10; j++) {
			Host host = hosts.get(j);

			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			File file = new File(outputPath + "_" + host.getId() + ".csv");
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
				List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
				List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

				for (int i = 0; i < timeData.size(); i++) {
					writer.write(String.format("%.2f,%.2f,%.2f\n", timeData.get(i), utilizationData.get(i),
							metricData.get(i)));
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * Gets the times before vm migration.
	 * 
	 * @param vms
	 *            the vms
	 * @return the times before vm migration
	 */
	public static List<Double> getTimesBeforeVmMigration(List<Vm> vms) {
		List<Double> timeBeforeVmMigration = new LinkedList<Double>();
		for (Vm vm : vms) {
			boolean previousIsInMigration = false;
			double lastTimeMigrationFinished = 0;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousIsInMigration == true && entry.isInMigration() == false) {
					timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
				}
				if (previousIsInMigration == false && entry.isInMigration() == true) {
					lastTimeMigrationFinished = entry.getTime();
				}
				previousIsInMigration = entry.isInMigration();
			}
		}
		return timeBeforeVmMigration;
	}

	/**
	 * Gets the times before host shutdown.
	 * 
	 * @param hosts
	 *            the hosts
	 * @return the times before host shutdown
	 */
	public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
		List<Double> timeBeforeShutdown = new LinkedList<Double>();
		for (Host host : hosts) {
			boolean previousIsActive = true;
			double lastTimeSwitchedOn = 0;
			for (HostStateHistoryEntry entry : ((HostDynamicWorkload) host).getStateHistory()) {
				if (previousIsActive == true && entry.isActive() == false) {
					timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
				}
				if (previousIsActive == false && entry.isActive() == true) {
					lastTimeSwitchedOn = entry.getTime();
				}
				previousIsActive = entry.isActive();
			}
		}
		return timeBeforeShutdown;
	}

	/**
	 * Inits the log output.
	 * 
	 * @param enableOutput
	 *            the enable output
	 * @param outputToFile
	 *            the output to file
	 * @param outputFolder
	 *            the output folder
	 * @param workload
	 *            the workload
	 * @param vmAllocationPolicy
	 *            the vm allocation policy
	 * @param vmSelectionPolicy
	 *            the vm selection policy
	 * @param parameter
	 *            the parameter
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	protected static void initLogOutput(
			boolean enableOutput, 
			boolean outputToFile, 
			String outputFolder,
			String resultFile,
			String vmAllocationPolicy, 
			String vmSelectionPolicy, 
			String parameter) throws IOException, FileNotFoundException {
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

			File file = new File(outputFolder + "/log/" + getExperimentName(vmAllocationPolicy, vmSelectionPolicy, parameter) + ".txt");
			file.createNewFile();
			Log.setOutput(new FileOutputStream(file));
			
			File result = new File(outputFolder + "/result/" + resultFile + ".txt");
			result.createNewFile();
		}
	}
}
