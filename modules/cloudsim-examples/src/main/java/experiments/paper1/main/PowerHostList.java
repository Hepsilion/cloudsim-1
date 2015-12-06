package experiments.paper1.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.power.PowerHost;

public class PowerHostList {
	public static <T extends Host> void sortByAscAvailableMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Double am = ((PowerHost) a).getAvailableMips();
				Double bm = ((PowerHost) b).getAvailableMips();
				if(am>bm)
					return 1;
				else if(am<bm)
					return -1;
				else
					return 0;
			}
		});
	}

}
