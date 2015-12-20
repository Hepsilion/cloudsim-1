package scheduling.our_approach;

public class SchedulingConstants {
	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_TO_FILE = true;
	public final static boolean OUTPUT_CSV    = false;
	
	public final static String log_file = "log/Our_Log";
	public final static String result_file = "result/Our_Result";
	public final static String result_temp_file = "result/Our_temp_Result";
	public final static String OutputFolder = "output";
	
	public final static String normal_vmAllocationPolicy = "normal";
	public final static String initial_vmAllocationPolicy = "init";
	
	public final static int DefautFrequency = 1;
	public final static boolean ENABLE_DVFS = true;
	public final static boolean ENABLE_ONOFF = true;
	
	public final static int NUMBER_OF_CLOUDLETS = 100;
	public final static int NUMBER_OF_HOSTS = 50;
	
	public final static int NUMBER_OF_ITERATIONS = 5;
	public final static int NUMBER_OF_PROCESSORS = 20;
	public final static int NUMBER_OF_ITERATION_PER_PROCESSOR = 5;
	
	
	public final static double SCHEDULING_INTERVAL = 1;
	public final static double SIMULATION_LIMIT = 24 * 60 * 60 * 2;
	
	public final static int CLOUDLET_LENGTH	= 200* (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES = 1;
	
	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	 *
	 */
	public final static int VM_TYPES	= 4;
	public final static int[] VM_MIPS	= {250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500};//{ 2500, 2000, 1000, 500 };//{ 500, 500, 500, 500 };//
	public final static int[] VM_PES	= { 1, 1, 1, 1 };
	public final static int[] VM_RAM	= { 870,  1740, 1740, 613 };
	public final static int VM_BW		= 100000; // 100 Mbit/s
	public final static int VM_SIZE		= 2500; // 2.5 GB
	
	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 */
	public final static int HOST_TYPES	 = 2;
	public final static int[] HOST_MIPS	 = { 1860, 2660 };//{1860, 2660};//
	public final static int[] HOST_PES	 = { 1, 1 };
	public final static int[] HOST_RAM	 = {100000000, 100000000};//{ 4096, 4096 };
	public final static int HOST_BW		 = 100000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 100000000; // 1 GB
}
