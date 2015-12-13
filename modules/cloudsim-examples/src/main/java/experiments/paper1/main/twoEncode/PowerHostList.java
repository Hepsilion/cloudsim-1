package experiments.paper1.main.twoEncode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.power.PowerHost;

public class PowerHostList {
	public static <T extends Host> void sortDvfsHosts(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException{
				PowerHost aHost = (PowerHost) a;
				PowerHost bHost = (PowerHost) b;
				if(aHost.getTotalMaxMips()>bHost.getTotalMaxMips())
					return 1;
				else if(aHost.getTotalMaxMips()<bHost.getTotalMaxMips())
					return -1;
				else{
					if(aHost.getMaxAvailableMips()>bHost.getMaxAvailableMips())
						return 1;
					else if(aHost.getMaxAvailableMips()<bHost.getMaxAvailableMips())
						return -1;
					else 
						return 0;
				}
			}
		});
	}
	
	/**
	 * sort host in asc order by total mips under max frequency
	 * @param hosts
	 */
	public static <T extends Host> void sortByAscMaxTotalMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Integer am = ((PowerHost) a).getTotalMaxMips();
				Integer bm = ((PowerHost) b).getTotalMaxMips();
				if(am>bm)
					return 1;
				else if(am<bm)
					return -1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in desc order by total mips under max frequency
	 * @param hosts
	 */
	public static <T extends Host> void sortByDescMaxTotalMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Integer am = ((PowerHost) a).getTotalMaxMips();
				Integer bm = ((PowerHost) b).getTotalMaxMips();
				if(am>bm)
					return -1;
				else if(am<bm)
					return 1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in asc order by available mips under current frequency
	 * @param hosts
	 */
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

	/**
	 * sort host in desc order by available mips under current frequency
	 * @param hosts
	 */
	public static <T extends Host> void sortByDescAvailableMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Double am = ((PowerHost) a).getAvailableMips();
				Double bm = ((PowerHost) b).getAvailableMips();
				if(am>bm)
					return -1;
				else if(am<bm)
					return 1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in asc order by available mips under max frequency
	 * @param hosts
	 */
	public static <T extends Host> void sortByAscMaxAvailableMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				PowerHost aHost = (PowerHost) a;
				PowerHost bHost = (PowerHost) b;
				Double am = aHost.getTotalMaxMips()-(aHost.getTotalMips()-aHost.getAvailableMips());
				Double bm = bHost.getTotalMaxMips()-(bHost.getTotalMips()-bHost.getAvailableMips());
				if(am>bm)
					return 1;
				else if(am<bm)
					return -1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in desc order by available mips under max frequency
	 * @param hosts
	 */
	public static <T extends Host> void sortByDescMaxAvailableMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				PowerHost aHost = (PowerHost) a;
				PowerHost bHost = (PowerHost) b;
				Double am = aHost.getTotalMaxMips()-(aHost.getTotalMips()-aHost.getAvailableMips());
				Double bm = bHost.getTotalMaxMips()-(bHost.getTotalMips()-bHost.getAvailableMips());
				if(am>bm)
					return -1;
				else if(am<bm)
					return 1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in asc order by used mips 
	 * @param hosts
	 */
	public static <T extends Host> void sortByAscUsedMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				PowerHost aHost = (PowerHost) a;
				PowerHost bHost = (PowerHost) b;
				Double am = aHost.getTotalMips()-aHost.getAvailableMips();
				Double bm = bHost.getTotalMips()-bHost.getAvailableMips();
				if(am>bm)
					return 1;
				else if(am<bm)
					return -1;
				else
					return 0;
			}
		});
	}
	
	/**
	 * sort host in desc order by used mips 
	 * @param hosts
	 */
	public static <T extends Host> void sortByDescUsedMips(List<T> hosts) {
		Collections.sort(hosts, new Comparator<T>() {
			@Override
			public int compare(T a, T b) throws ClassCastException {
				PowerHost aHost = (PowerHost) a;
				PowerHost bHost = (PowerHost) b;
				Double am = aHost.getTotalMips()-aHost.getAvailableMips();
				Double bm = bHost.getTotalMips()-bHost.getAvailableMips();
				if(am>bm)
					return -1;
				else if(am<bm)
					return 1;
				else
					return 0;
			}
		});
	}
}
