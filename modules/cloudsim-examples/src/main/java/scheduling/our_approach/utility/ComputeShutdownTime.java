package scheduling.our_approach.utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ComputeShutdownTime {
	public static void main(String[] args){
		long[] shutdown_time=new long[SchedulingConstants.NUMBER_OF_HOSTS];
		String file="E:\\Workspace\\Github\\cloudsim\\modules\\cloudsim-examples\\output\\shutdown.txt";
		String line=null;
		long seg_start=0, seg_end=0;
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			while((line=br.readLine())!=null){
				String s1=line.substring(line.indexOf(':')+1, line.indexOf(','));
				line=line.substring(line.indexOf(',')+1);
				String s2=line.substring(line.indexOf(':')+1);
				seg_start=(long) Double.parseDouble(s1);
				seg_end=(long) Double.parseDouble(s2);
				System.out.println(seg_start+" "+seg_end);
				for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++){
					line=br.readLine();
					line=line.substring(line.indexOf(':')+1);
					if(line.equals("shutdown")){
						shutdown_time[i]+=(seg_end-seg_start);
						System.out.println("1");
					}else
						System.out.println("0");
				}
			}
			System.out.println("Total time:");
			for(int i=0; i<SchedulingConstants.NUMBER_OF_HOSTS; i++)
				System.out.println("Host "+i+": "+shutdown_time[i]);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
