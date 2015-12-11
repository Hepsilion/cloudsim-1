package org.cloudbus.cloudsim.power.dvfs;

import java.util.HashMap;

/**
 * 
 * @author Hepsilion
 *
 */
public class MyGovernor extends ConservativeGovernor{

	public MyGovernor(HashMap<String, Integer> configConservative_) {
		super(configConservative_);
	}
	
	public int SpecificDecision(double utilPe) {
		if(utilPe==0)
			return -2;
		else
			return super.SpecificDecision(utilPe);
	}

	@Override
	public void setDefautIndexFreq(int nb_freq) {
		this.defautIndexFreq=nb_freq;
	}
}
