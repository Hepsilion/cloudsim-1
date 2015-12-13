package org.cloudbus.cloudsim.power.dvfs;

import java.util.HashMap;

import org.cloudbus.cloudsim.Log;

/**
 * 
 * @author Hepsilion
 *
 */
public class MyGovernor extends AbstractGovernor{	
	public MyGovernor(HashMap<String, Integer> configConservative_) {
		setName("MyGovernor");
		setDownThreshold(20);
		setUpThreshold(80);
		ConfigParameters(configConservative_);
	}
	
	public int SpecificDecision(double utilPe) {
		if(utilPe==0)
			return -2;
		else{
			int desc = decision(utilPe);
			return desc;
		}
	}

	@Override
	public void setDefautIndexFreq(int nb_freq) {
		this.defautIndexFreq=nb_freq;
	}
	
	private void ConfigParameters(HashMap<String, Integer> configMy){
		if(configMy.containsKey("up_threshold")) {
			Object o = configMy.get("up_threshold");
			setUpThreshold((int) o);
		}
		if(configMy.containsKey("down_threshold")) {
			Object o = configMy.get("down_threshold");
			setDownThreshold((int) o);
		}
		if(configMy.containsKey("frequency")) {
			Object o = configMy.get("frequency");
			int freq = (int) o;
			int userFreq = freq - 1;
			setDefautIndexFreq(userFreq);
			Log.printLine("UserSpace Frequency value : f" + freq);
		}
	}
}
