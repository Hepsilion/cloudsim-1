package experiments.paper1.main;

import java.util.List;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class RealtimeHost extends PowerHostUtilizationHistory {
    private int frequency;
    
	public RealtimeHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel, boolean enableOnOff,
			boolean enableDvfs) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel, enableOnOff, enableDvfs);
	}
	
	public void setFrequency(int frequency) {
	    this.frequency = frequency;
	    int oldTotalMips = getTotalMips();
		for(Pe pe : getPeList()) {
			pe.setFrequency(frequency);
		}
		int newTotalMips = getTotalMips();
		double availableMips = getAvailableMips() + newTotalMips - oldTotalMips;
		setAvailableMips(availableMips);
	}
	
    public int getFrequency() {
        return frequency;
    }

    @Override
	public double updateVmsProcessing(double currentTime) {
		double time = super.updateVmsProcessing(currentTime);
		isDvfsActivatedOnHost();
		return time;
	}
}
