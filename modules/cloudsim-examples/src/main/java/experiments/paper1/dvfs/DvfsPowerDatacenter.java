package experiments.paper1.dvfs;

import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import experiments.paper1.main.RealtimeHost;


public class DvfsPowerDatacenter extends PowerDatacenter {
    public DvfsPowerDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
        throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

	@Override
	protected void updateCloudletProcessing() {
		for(RealtimeHost host : this.<RealtimeHost>getHostList())
        	host.isDvfsActivatedOnHost();
		super.updateCloudletProcessing();
	}
}
