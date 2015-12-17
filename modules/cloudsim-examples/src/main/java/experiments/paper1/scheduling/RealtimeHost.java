package experiments.paper1.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class RealtimeHost extends PowerHostUtilizationHistory {
    private List<Cloudlet> cloudlets;
    
	public RealtimeHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel, boolean enableOnOff,
			boolean enableDvfs) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel, enableOnOff, enableDvfs);
		cloudlets = new ArrayList<Cloudlet>();
	}
    
    public List<Cloudlet> getCloudlets() {
		return cloudlets;
	}
}
