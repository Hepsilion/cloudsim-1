package experiments.paper1.main;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;

import experiments.paper1.ga.Crossover;

public class RealtimeConstants {
	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_TO_FILE = true;

	//Base Algorithm
	public final static String VmAllocationPolicy = "base";//最大频率下，功耗增加最少
	public final static String VmSelectionPolicy = "mc";
	public final static String Parameter = "1.5";
		
	//Dvfs Algorithm
	//public final static String VmAllocationPolicy = "dvfs_my";//最小容量，最小剩余容量
	//public final static String VmAllocationPolicy = "dvfs_base1";//当前频率下能耗增加最少
	//public final static String VmAllocationPolicy = "dvfs_base2";//最大频率下能耗增加最少
	//public final static String VmAllocationPolicy = "dvfs_muh";
	//public final static String VmAllocationPolicy = "dvfs_SimpleWatt";
	
	//Genetic Algorithm
    //public final static String VmAllocationPolicy = "ga";
	
	public final static String OutputFolder = "output";
	
	public final static int DefautFrequency = 5;
	public final static boolean ENABLE_DVFS = false;
	public final static boolean ENABLE_ONOFF = true;
	
	public final static boolean OUTPUT_CSV    = false;
	
	public final static double SCHEDULING_INTERVAL = 1;
	public final static double SIMULATION_LIMIT = 24 * 60 * 60 * 2;
	
	public final static int CLOUDLET_LENGTH	= 200* (int) SIMULATION_LIMIT;//10 * 
	public final static int CLOUDLET_PES	= 1;
	
	public final static int NUMBER_OF_CLOUDLETS = 100;
	public final static int NUMBER_OF_HOSTS = 50;
	
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
	
	public final static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
		new PowerModelSpecPowerHpProLiantMl110G5Xeon3075()
	};
	
	public final static int RANDOM_SEED = 50;
	public final static int CHROMSOME_DIM = NUMBER_OF_CLOUDLETS;
	public final static int POPULATION_DIM = 20;
	public final static double CROSSOVER_PROB = 0.7; 
	public final static int CROSSOVER_TYPE = Crossover.ctUniform;
	public final static double MUTATION_PROB = 0.4;
	public final static int RANDOM_SELECTION_CHANCE = 10;
	
	public final static int numPrelimRuns = 0;//20
    public final static int maxPrelimGenerations = 2;
    
    public final static int MAX_GENERATIONS = 20;//5
	
	public final static boolean COMPUTE_STATISTICS = false;
}
