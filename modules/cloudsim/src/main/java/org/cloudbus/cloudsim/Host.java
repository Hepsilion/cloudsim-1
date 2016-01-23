/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * Host executes actions related to management of virtual machines (e.g., creation and destruction).
 * A host has a defined policy for provisioning memory and bw, as well as an allocation policy for
 * Pe's to virtual machines. A host is associated to a datacenter. It can host virtual machines.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Host {

	/** The id. */
	private int id;

	/** The storage. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy. */
	private VmScheduler vmScheduler;

	/** The vm list. */
	private final List<? extends Vm> vmList = new ArrayList<Vm>();

	/** The pe list. */
	private List<? extends Pe> peList;

	/** Tells whether this machine is working properly or has failed. */
	private boolean failed;

	/** The vms migrating in. */
	private final List<Vm> vmsMigratingIn = new ArrayList<Vm>();

	/** The datacenter where the host is placed. */
	private Datacenter datacenter;

		private boolean enableDVFS = false;
	    private boolean enableONOFF = false;
    
	/**
	 * Instantiates a new host.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the vm scheduler
	 */
	public Host(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		setId(id);
		setRamProvisioner(ramProvisioner);
		setBwProvisioner(bwProvisioner);
		setStorage(storage);
		setVmScheduler(vmScheduler);

		setPeList(peList);
		setFailed(false);
	}

	/**
	 * Requests updating of processing of cloudlets in the VMs running in this host.
	 * 
	 * @param currentTime the current time
	 * @return expected time of completion of the next cloudlet in all VMs in this host.
	 *         Double.MAX_VALUE if there is no future events expected in this host
	 * @pre currentTime >= 0.0
	 * @post $none
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (Vm vm : getVmList()) {
			double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	/**
	 * Adds the migrating in vm.
	 * 
	 * @param vm the vm
	 */
	public void addMigratingInVm(Vm vm) {
		vm.setInMigration(true);

		if (!getVmsMigratingIn().contains(vm)) {
			if (getStorage() < vm.getSize()) {
				Log.printConcatLine("[VmScheduler.addMigratingInVm] Allocation of VM #", vm.getId(), " to Host #",
						getId(), " failed by storage");
				System.exit(0);
			}

			if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
				Log.printConcatLine("[VmScheduler.addMigratingInVm] Allocation of VM #", vm.getId(), " to Host #",
						getId(), " failed by RAM");
				System.exit(0);
			}

			if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
				Log.printLine("[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getId() + " to Host #"
						+ getId() + " failed by BW");
				System.exit(0);
			}

			getVmScheduler().getVmsMigratingIn().add(vm.getUid());
			if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
				Log.printLine("[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getId() + " to Host #"
						+ getId() + " failed by MIPS");
				System.exit(0);
			}

			setStorage(getStorage() - vm.getSize());

			getVmsMigratingIn().add(vm);
			getVmList().add(vm);
			updateVmsProcessing(CloudSim.clock());
			vm.getHost().updateVmsProcessing(CloudSim.clock());
		}
	}

	/**
	 * Removes the migrating in vm.
	 * 
	 * @param vm the vm
	 */
	public void removeMigratingInVm(Vm vm) {
		vmDeallocate(vm);
		getVmsMigratingIn().remove(vm);
		getVmList().remove(vm);
		getVmScheduler().getVmsMigratingIn().remove(vm.getUid());
		vm.setInMigration(false);
	}

	/**
	 * Reallocate migrating in vms.
	 */
	public void reallocateMigratingInVms() {
		for (Vm vm : getVmsMigratingIn()) {
			if (!getVmList().contains(vm)) {
				getVmList().add(vm);
			}
			if (!getVmScheduler().getVmsMigratingIn().contains(vm.getUid())) {
				getVmScheduler().getVmsMigratingIn().add(vm.getUid());
			}
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
			setStorage(getStorage() - vm.getSize());
		}
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @param vm the vm
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForVm(Vm vm) {
		return (getVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
				&& getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) 
				&& getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}
		
		// Original Implementation
		/*
		public boolean MakeSuitableHostForVm(Vm vm) {
			// If DVFS Enabled, it's possible that the first vm ask more than the start capacity of the PE(depending of the dvfs mode),
			// in this case, we have to increase directly PE capacity! But it's not possible with all governor.
			if (this.getVmList().isEmpty() && this.isEnableDVFS() && getVmScheduler().getAvailableMips() < vm.getCurrentRequestedTotalMips()) {
				for (Pe pe : getPeList()) {
					if (pe.changeToMaxFrequency()){ // if the Freq has been set to MAX , return true
						setAvailableMips(getTotalMips());
						Log.printLine("Current Total Mips: "+getTotalMips());
						return true;
					}
				}
				return false;
			} else
				return false;
		}*/
	
		public boolean MakeSuitableHostForVm(Vm vm) {
			return this.increaseHostMipsForNewVm(vm);
		}
	
		/**
		 * @author Hepsilion
		 */
		public boolean increaseHostMipsForNewVm(Vm vm) {
	    	if(this.isEnableDVFS() && getVmScheduler().getAvailableMips()<vm.getCurrentRequestedTotalMips()) {
	    		int[] frequencies = new int[getPeList().size()];
	    		double oldTotalMips = this.getTotalMips();
	    		Log.printLine("Host#"+this.getId()+" Before,Available Mips:"+this.getAvailableMips());
	    		for(int i=0; i<getPeList().size(); i++) {
	    			frequencies[i]=getPeList().get(i).getIndexFreq();
	    			Log.printLine(frequencies[i]+" ");
	    			getPeList().get(i).changeToMaxFrequency();
	    		}
	    		
	    		double totalMips = this.getTotalMips();
	    		double newAvailableMips = totalMips-oldTotalMips+this.getAvailableMips();
	    		Log.printLine("NewAvailableMips:"+newAvailableMips);
	    		if(newAvailableMips >= vm.getCurrentRequestedTotalMips()) {
	    			this.getVmScheduler().setAvailableMips(newAvailableMips);
	    			Log.printLine("Host#"+this.getId()+" After,Available Mips:"+this.getAvailableMips());
	    			return true;
	    		}else{
	    			for(int i=0; i<getPeList().size(); i++) {
	    				getPeList().get(i).setFrequency(frequencies[i]);
	    			}
	    			Log.printLine("Host#"+this.getId()+" Increasing failed: now max avaiable mips="+(this.getTotalMaxMips()-this.getTotalMips()+this.getAvailableMips()));
	    			return false;
	    		}
	    	}
	    	return false;
		}
		
		/**
		 * @author Hepsilion
		 * @param vm
		 * @return
		 */
		public boolean tryIncreaseHostMipsForNewVm(Vm vm, int[] frequencies) {
			if(this.isEnableDVFS() && getVmScheduler().getAvailableMips()<vm.getCurrentRequestedTotalMips()) {
	    		double oldTotalMips = this.getTotalMips();
	    		Log.printLine("Host#"+this.getId()+" Before,Available Mips:"+this.getAvailableMips());
	    		for(int i=0; i<getPeList().size(); i++) {
	    			frequencies[i]=getPeList().get(i).getIndexFreq();
	    			Log.printLine(frequencies[i]+" ");
	    			getPeList().get(i).changeToMaxFrequency();
	    		}
	    		
	    		double totalMips = this.getTotalMips();
	    		double newMips = totalMips-oldTotalMips+this.getAvailableMips();
	    		Log.printLine("New:"+newMips);
	    		if(newMips > vm.getCurrentRequestedTotalMips()) {
	    			this.getVmScheduler().setAvailableMips(newMips);
	    			Log.printLine("Host#"+this.getId()+" After,Available Mips:"+this.getAvailableMips());
	    			return true;
	    		}else{
	    			for(int i=0; i<getPeList().size(); i++) {
	    				getPeList().get(i).setFrequency(frequencies[i]);
	    			}
	    			Log.printLine("Host#"+this.getId()+" Increasing failed: now max avaiable mips="+(this.getTotalMaxMips()-this.getTotalMips()+this.getAvailableMips()));
	    			return false;
	    		}
	    	}
			return false;
		}
		
		public void backupHostMips(int[] frequencies) {
			double oldTotalMips = this.getTotalMips();
			for(int i=0; i<getPeList().size(); i++) {
				getPeList().get(i).setFrequency(frequencies[i]);
			}
			double newMips = this.getTotalMips()-oldTotalMips+this.getAvailableMips();
			this.getVmScheduler().setAvailableMips(newMips);
		}
	
		/**
		 * This methode modifies VMs size to fit them into one Host
		 * 
		 * The VMs size have to be modified when at the same time there is : 
		 * - their size (sum) is > than the Host capacity - a new VM has to be fitted into this Host
		 * 
		 * @param new_vm
		 *            (the VM that has to be to added into the Host)
		 * @return true (false will be implemented next)
		 */
		public boolean decreaseVMMipsToHostNewVm(Vm new_vm) {
			double NewTotalVmMips = 0;
			double HostCapacity = getTotalMips();
			double percent;
	
			percent = reducePercentVmMips(new_vm, true);
			NewTotalVmMips = reduceAllVmMips(percent);
	
			new_vm.setMips(new_vm.getMaxMips() - (new_vm.getMaxMips() * percent));
	
			setAvailableMips(HostCapacity - NewTotalVmMips);
			return true;
		}
	
		/**
		 * This method modifies VMs size.
		 * 
		 * Called after CPU frequency decrease To avoid Host Capacity OverFlow (
		 * theoritical "CPU utilization > 100%")
		 */
		public void decreaseVmMips() {
			double NewTotalVmMips = 0;
			double HostCapacity = getTotalMips();
			double percent;
			percent = reducePercentVmMips(null, false);
			NewTotalVmMips = reduceAllVmMips(percent);
			setAvailableMips(HostCapacity - NewTotalVmMips);
		}
	
		/**
		 * Compute the percentage of size reduction to apply on ALL Vms
		 * 
		 * @param new_vm
		 * @param isNewVmToHost
		 * @return percent
		 */
		private double reducePercentVmMips(Vm new_vm, boolean isNewVmToHost) {
			double HostCapacity = getTotalMips();
			double SumVmMaxMips = 0;
			for (Vm vm : getVmList())
				SumVmMaxMips += vm.getMaxMips();
			if (isNewVmToHost)
				SumVmMaxMips += new_vm.getMaxMips();
	
			double percent = ((SumVmMaxMips - HostCapacity) / SumVmMaxMips);
			percent += percent * 0.002;
			return percent;
		}
	
		/**
		 * Function that really modify the VM Mips relating to the percentage
		 * 
		 * @param percent
		 * @return
		 */
		private double reduceAllVmMips(double percent) {
			double NewTotalVmMips = 0;
			for (Vm vm : getVmList()) {
				double new_mips;
				new_mips = vm.getMaxMips() - (vm.getMaxMips() * percent);
				vm.setMips(new_mips);
	
				List<Double> updatedMipsVm = new ArrayList<>();
				Map<String, List<Double>> tmp_Map = getVmScheduler().getMipsMap();
				tmp_Map.put(vm.getUid(), updatedMipsVm);
				tmp_Map.get(vm.getUid()).add(new_mips);
	
				NewTotalVmMips += new_mips;
			}
			return NewTotalVmMips;
		}
	
		/**
		 * This method regrow VM size (maximum to their initial capacity) 
		 * To use the Host at its maximum potential regarding the CPU frequency.
		 * 
		 * Function called when a VM finished its execution
		 * 
		 * @return void
		 */
		public void regrowVmMipsAfterVmEnd(Vm vmFinished) {
			Log.printLine("Host# "+this.getId()+" : Regrow VM mips : after VM#"+vmFinished.getId()+"("+vmFinished.getMips()+")"+" end");
			double FreeMips;
			List<Vm> ListVMRunning = getVmList();
	
			// remove the VM finished from the tmp VM list before the increase %
			FreeMips = vmFinished.getMips();
			ListVMRunning.remove(vmFinished);
			double percent = increasePercentVmMips(FreeMips);
			increaseVmMips(ListVMRunning, percent);
			Log.printLine("Host# "+this.getId()+" : Available Mips on HOST after regrow vm mips(after VM#"+vmFinished.getId()+") = " + getAvailableMips());
		}
	
		/**
		 * This method regrow VM size (maximum to their initial capacity) 
		 * To use the Host at its maximum potential regarding the CPU frequency.
		 * 
		 * Function called when the CPU frequency is increased.
		 */
		public void regrowVmMips() {
			Log.printLine("Host# "+this.getId()+" : Regrow VM mips : after Frequency Increase");
			increaseVmMips(getVmList(), increasePercentVmMips(0));
		}
	
		/**
		 * Compute the percentage of size increase to apply on all VM
		 * 
		 * @param double freeMips : free mips on host
		 * @return percentage
		 * 
		 */
		private double increasePercentVmMips(double FreeMips) {
			double availableMips = this.getAvailableMips() + FreeMips;
			double maxMipsHost = this.getTotalMips();
			double Percent = (maxMipsHost / (maxMipsHost - availableMips));
			return Percent;
		}
	
		/**
		 * Function that really modify the VM size.
		 * 
		 * @param ListVMRunning
		 * @param Percent
		 */
		private void increaseVmMips(List<Vm> ListVMRunning, double Percent) {
			double HostCapacity = getTotalMips();//TODO 新添加
			double NewSumVmMips = 0;
			for (Vm vm : ListVMRunning) {
				Log.printLine("Host# "+this.getId()+" : VM#"+vm.getId()+" Current MIPS(Before Increase) = " + vm.getMips() + "  / percent = " + Percent);
				double TmpNewVmMips = vm.getMips() * Percent * 0.998;
	
				if (TmpNewVmMips > vm.getMaxMips()) {
					TmpNewVmMips = vm.getMaxMips();
					//continue;// TODO 这里似乎错了，修改
				}
				vm.setMips(TmpNewVmMips);
				vm.setSizeHasBeenModified(true);
	
				List<Double> updatedMipsVm = new ArrayList<>();
				Map<String, List<Double>> tmp_Map = getVmScheduler().getMipsMap();
				tmp_Map.put(vm.getUid(), updatedMipsVm);
				tmp_Map.get(vm.getUid()).add(TmpNewVmMips);
				Log.printLine("Host# "+this.getId()+" : VM#"+vm.getId()+" New MIPS(After Increase) = " + vm.getMips());
				NewSumVmMips += TmpNewVmMips;
			}
			this.setAvailableMips(HostCapacity-NewSumVmMips);//TODO 新添加
		}

	/**
	 * Allocates PEs and memory to a new VM in the Host.
	 * 
	 * @param vm Vm being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(Vm vm) {
		if (getStorage() < vm.getSize()) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
					" failed by storage");
			return false;
		}

		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
					" failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
					" failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
					" failed by MIPS");
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}

		setStorage(getStorage() - vm.getSize());
		getVmList().add(vm);
		vm.setHost(this);
		return true;
	}

	/**
	 * Destroys a VM running in the host.
	 * 
	 * @param vm the VM
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(Vm vm) {
		if (vm != null) {
			vmDeallocate(vm);
			getVmList().remove(vm);
			vm.setHost(null);
		}
	}

	/**
	 * Destroys all VMs running in the host.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroyAll() {
		vmDeallocateAll();
		for (Vm vm : getVmList()) {
			vm.setHost(null);
			setStorage(getStorage() + vm.getSize());
		}
		getVmList().clear();
	}

	/**
	 * Deallocate all hostList for the VM.
	 * 
	 * @param vm the VM
	 */
	protected void vmDeallocate(Vm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getVmScheduler().deallocatePesForVm(vm);
		setStorage(getStorage() + vm.getSize());
	}

	/**
	 * Deallocate all hostList for the VM.
	 */
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	/**
	 * Returns a VM object.
	 * 
	 * @param vmId the vm id
	 * @param userId ID of VM's owner
	 * @return the virtual machine object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Vm getVm(int vmId, int userId) {
		for (Vm vm : getVmList()) {
			if (vm.getId() == vmId && vm.getUserId() == userId) {
				return vm;
			}
		}
		return null;
	}

	/**
	 * Gets the pes number.
	 * 
	 * @return the pes number
	 */
	public int getNumberOfPes() {
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 * 
	 * @return the free pes number
	 */
	public int getNumberOfFreePes() {
		return PeList.getNumberOfFreePes(getPeList());
	}

	/**
	 * Gets the total mips under current frequency.
	 * 
	 * @return the total mips
	 */
	public int getTotalMips() {
		return PeList.getTotalMips(getPeList());
	}
	
		/**
		 * Gets the total mips under max frequency.
		 * 
		 * @return the total mips
		 * @author Hepsilion
		 */
		public int getTotalMaxMips() {
			return PeList.getTotalMaxMips(getPeList());
		}

	/**
	 * Allocates PEs for a VM.
	 * 
	 * @param vm the vm
	 * @param mipsShare the mips share
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
		return getVmScheduler().allocatePesForVm(vm, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 * 
	 * @param vm the vm
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForVm(Vm vm) {
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param vm the vm
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getAllocatedMipsForVm(vm);
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 * 
	 * @param vm the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getTotalAllocatedMipsForVm(vm);
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getVmScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the free mips.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getVmScheduler().getAvailableMips();
	}
	
		public void setAvailableMips(double AvailableMips) {
			getVmScheduler().setAvailableMips(AvailableMips);
		}

	/**
	 * Gets the machine bw.
	 * 
	 * @return the machine bw
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the machine memory.
	 * 
	 * @return the machine memory
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the machine storage.
	 * 
	 * @return the machine storage
	 * @pre $none
	 * @post $result >= 0
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the ram provisioner.
	 * 
	 * @return the ram provisioner
	 */
	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}

	/**
	 * Sets the ram provisioner.
	 * 
	 * @param ramProvisioner the new ram provisioner
	 */
	protected void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	/**
	 * Gets the bw provisioner.
	 * 
	 * @return the bw provisioner
	 */
	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}

	/**
	 * Sets the bw provisioner.
	 * 
	 * @param bwProvisioner the new bw provisioner
	 */
	protected void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	/**
	 * Gets the VM scheduler.
	 * 
	 * @return the VM scheduler
	 */
	public VmScheduler getVmScheduler() {
		return vmScheduler;
	}

	/**
	 * Sets the VM scheduler.
	 * 
	 * @param vmScheduler the vm scheduler
	 */
	protected void setVmScheduler(VmScheduler vmScheduler) {
		this.vmScheduler = vmScheduler;
	}

	/**
	 * Gets the pe list.
	 * 
	 * @param <T> the generic type
	 * @return the pe list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the pe list.
	 * 
	 * @param <T> the generic type
	 * @param peList the new pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the storage.
	 * 
	 * @param storage the new storage
	 */
	protected void setStorage(long storage) {
		this.storage = storage;
	}

	/**
	 * Checks if is failed.
	 * 
	 * @return true, if is failed
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Sets the PEs of this machine to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
	 * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
	 * this information.
	 * 
	 * @param resName the name of the resource
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(String resName, boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), resName, getId(), failed);
		return true;
	}

	/**
	 * Sets the PEs of this machine to a FAILED status.
	 * 
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), failed);
		return true;
	}

	/**
	 * Sets the particular Pe status on this Machine.
	 * 
	 * @param peId the pe id
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
	 *         be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int peId, int status) {
		return PeList.setPeStatus(getPeList(), peId, status);
	}

	/**
	 * Gets the vms migrating in.
	 * 
	 * @return the vms migrating in
	 */
	public List<Vm> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Gets the data center.
	 * 
	 * @return the data center where the host runs
	 */
	public Datacenter getDatacenter() {
		return datacenter;
	}

	/**
	 * Sets the data center.
	 * 
	 * @param datacenter the data center from this host
	 */
	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

		public boolean isEnableDVFS() {
	        return enableDVFS;
	    }
	
	    protected void setEnableDVFS(boolean enableDVFS) {
	        this.enableDVFS = enableDVFS;
	    }
	    
	    public boolean isEnableONOFF() {
	        return enableONOFF;
	    }
	
	    public void setEnableONOFF(boolean enableONOFF) {
	        this.enableONOFF = enableONOFF;
	    }
}
