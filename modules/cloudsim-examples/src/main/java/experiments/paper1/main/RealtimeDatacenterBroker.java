package experiments.paper1.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

public class RealtimeDatacenterBroker extends PowerDatacenterBroker {
	int vmIndex;

	public RealtimeDatacenterBroker(String name) throws Exception {
		super(name);
	}

	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case CloudSimTags.VM_CREATE_ACK:
			processVmCreate(ev);
			break;
		case CloudSimTags.CREATE_CLOUDLET:
			submitCloudlet(ev);
			break;
		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;
		default:
			super.processEvent(ev);
		}
	}

	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId, " has been created in Datacenter #",
					datacenterId, ", Host #", VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
					" failed in Datacenter #", datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
			// createCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
					// createCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	protected void submitCloudlets() {
		vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
		for (Cloudlet cloudlet : getCloudletList()) {
			RealtimeCloudlet rc = (RealtimeCloudlet) cloudlet;
			if (rc.getStartTime() <= CloudSim.clock()) {
				Vm vm;
				// if user didn't bind this cloudlet and it has not been
				// executed yet
				if (cloudlet.getVmId() == -1) {
					vm = getVmsCreatedList().get(vmIndex);
				} else { // submit to the specific vm
					vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
					if (vm == null) { // vm was not created
						if (!Log.isDisabled()) {
							Log.printConcatLine(CloudSim.clock(), ": ", getName(),
									": Postponing execution of cloudlet ", cloudlet.getCloudletId(),
									": bount VM not available");
						}
						continue;
					}
				}

				if (!Log.isDisabled()) {
					Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
							cloudlet.getCloudletId(), " to VM #", vm.getId());
				}

				cloudlet.setVmId(vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
				cloudletsSubmitted++;
				vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
				getCloudletSubmittedList().add(cloudlet);
				successfullySubmitted.add(cloudlet);
			} else {
				send(getId(), rc.getStartTime() - CloudSim.clock(), CloudSimTags.CREATE_CLOUDLET, cloudlet);
			}
		}
		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
	}

	/*
	private void createCloudlets() {
		for (Cloudlet cloudlet : getCloudletList()) {
			RealtimeCloudlet rc = (RealtimeCloudlet) cloudlet;
			send(getId(), rc.getStartTime(), CloudSimTags.CREATE_CLOUDLET, rc);
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Create clodlet " + rc.getCloudletId() + " at:"
					+ rc.getStartTime());
		}
	}
	*/

	private void submitCloudlet(SimEvent ev) {
		RealtimeCloudlet rc = (RealtimeCloudlet) ev.getData();
		Vm vm = null;

		if (rc.getVmId() == -1) {
			vm = getVmsCreatedList().get(vmIndex);
		} else {
			vm = VmList.getById(getVmsCreatedList(), rc.getVmId());
			if (vm == null) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
						+ rc.getCloudletId() + ": bount VM not available");
				return;
			}
		}
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Create clodlet " + rc.getCloudletId()
				+ "(request start time:" + rc.getStartTime() + ")");
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet " + rc.getCloudletId() + " to VM #"
				+ vm.getId());
		rc.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, rc);
		cloudletsSubmitted++;
		vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
		getCloudletSubmittedList().add(rc);
		getCloudletList().remove(rc);
	}

	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ", cloudlet.getCloudletId(), " received");
		cloudletsSubmitted--;
		
		Vm vm = PowerVmList.getById(this.getVmList(), cloudlet.getVmId());
        sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
        Log.printConcatLine(CloudSim.clock(), ": " + getName(), ": Destroying VM #", vm.getId());
        getVmsCreatedList().remove(vm);
        //vmIndex = getVmsCreatedList().size() == 0 ? 0 : vmIndex % getVmsCreatedList().size();
        
		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { 
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
			processPostEvent();
			// Maybe create an "Broker End Event" ?
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				// clearDatacenters();
				// createVmsInDatacenter(0);
			}
		}
	}
}
