package scheduling.our_approach;

import scheduling.our_approach.utility.SchedulingConstants;


public class AllocationMapping {
	private int numVms;
	private int[] hostsMapping;
	private double task_acceptance_rate;
	private double energy;
	private double fitness;
	private long numInstruction;
	
	public AllocationMapping(int numVms) {
		this.numVms = numVms;
		this.hostsMapping = new int[numVms];
		for(int i=0; i<numVms; i++)
			this.hostsMapping[i]=-1;
		this.task_acceptance_rate = 0;
		this.energy = 0;
		this.fitness = -1;
		this.numInstruction = 0;
	}
	
	public void setHostOfVm(int vmId, int hostId) {
		this.hostsMapping[vmId] = hostId;
	}
	
	public int getHostOfVm(int vmId) {
		return this.hostsMapping[vmId];
	}
	
	public int getNumVms() {
		return numVms;
	}

	public void setNumVms(int numVms) {
		this.numVms = numVms;
	}

	public int[] getHostsMapping() {
		return hostsMapping;
	}

	public void setHostsMapping(int[] hostsMapping) {
		this.hostsMapping = hostsMapping;
	}
	
	public double getTask_acceptance_rate() {
		return task_acceptance_rate;
	}

	public void setTask_acceptance_rate(double task_acceptance_rate) {
		this.task_acceptance_rate = task_acceptance_rate;
	}
	
	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}
	
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public long getNumInstruction() {
		return numInstruction;
	}

	public void setNumInstruction(long numInstruction) {
		this.numInstruction = numInstruction;
	}

	@Override
	public String toString() {
		String mapping = "";
		
		StringBuffer vms = new StringBuffer(this.numVms);
		StringBuffer hosts = new StringBuffer(this.numVms);
		for(int i=0; i<this.numVms; i++) {
			vms.append(i);
        	hosts.append(this.hostsMapping[i]);
        	if (i < this.numVms - 1) {
            	vms.append("\t");
            	hosts.append("\t");
            }
		}
		
		mapping = mapping + vms.toString() + "\n" + hosts.toString()+"\nNumber of Cloudlets on each Host:\n";
		
		StringBuffer hostsId = new StringBuffer(SchedulingConstants.NUMBER_OF_HOSTS);
		StringBuffer numTask = new StringBuffer(SchedulingConstants.NUMBER_OF_HOSTS);
		int[] numTasks = new int[SchedulingConstants.NUMBER_OF_HOSTS];
		int hostId;
		for(int i=0; i<this.getNumVms(); i++) {
			hostId = this.getHostOfVm(i);
			if(hostId!=-1){
				numTasks[hostId]++; // record number of tasks on each host
			}
		}
		hostsId.append("#Host:\t");
		numTask.append("#Task:\t");
		for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++){
			hostsId.append(i);
			numTask.append(numTasks[i]);
        	if (i < SchedulingConstants.NUMBER_OF_HOSTS - 1) {
        		hostsId.append("\t");
        		numTask.append("\t");
            }
		}
		mapping = mapping + hostsId.toString() + "\n" + numTask.toString();
		return mapping;
	}
}
