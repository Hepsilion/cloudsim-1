package scheduling.our_approach.utility;

import java.util.Random;

public class PossionDistribution {
	public static int[] getArrivalTime(int seed, int num){
		double lambda=num*1.0/24/3600;
		Random rand=new Random(seed);
		int[] times=new int[num];
		times[0]=(int) (-Math.log(rand.nextDouble())/lambda);
		for(int i=1; i<num; i++){
			times[i]= times[i-1]+(int)(-Math.log(rand.nextDouble())/lambda);
		}
		return times;
	}
	
	public static void main(String[] args){
		int num=100;
		int[] times=getArrivalTime(200, num);
		for(int i=0; i<num; i++)
			System.out.println(times[i]);
	}
}
