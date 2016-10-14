package scheduling.our_approach.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DataCollector {
	public static void main(String[] args){
		DataCollector s=new DataCollector();
		
		String dir="C:\\Users\\Hepsilion\\Desktop\\20160929\\20160929\\";
		String[] methods={"FF", "MBFD"};
		String[] means={"3000", "5000"};
		String[] stds={"100", "500", "1000"};
		for(int i=0; i<methods.length; i++){
			for(int j=0; j<stds.length; j++){
				for(int k=0; k<means.length; k++)
					s.collect(dir, methods[i], stds[j], means[k]);
			}
		}
	}
	
	public void collect(String dir, String method, String std, String mean){
		dir=dir+method+"-"+std+"-"+mean;
		String tar_file=method+"-"+std+"-"+mean+"-tar.txt";
		String energy_file=method+"-"+std+"-"+mean+"-energy.txt";
		String efficiency_file=method+"-"+std+"-"+mean+"-efficiency.txt";
		String common_filename="DVFS_"+method+"_EX_"+std+"_"+mean+"_Result";
		
		PrintWriter tar_writer=null, energy_writer=null, efficiency_writer=null;
		try {
			tar_writer=new PrintWriter(new FileWriter(dir+"\\..\\"+tar_file));
			energy_writer=new PrintWriter(new FileWriter(dir+"\\..\\"+energy_file));
			efficiency_writer=new PrintWriter(new FileWriter(dir+"\\..\\"+efficiency_file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String data_dir=dir+"\\1";
		String[] files=new File(data_dir).list();
		
		File file=null;
		BufferedReader br=null;
		for(int i=0; i<files.length; i++){
			int filenum=10*(i+1);			
			String line_tar=null, line_energy=null, line_efficiency=null;
			double tar1=0.0, energy1=0.0, efficiency1=0.0, tar2=0.0, energy2=0.0, efficiency2=0.0;
			String line_out_tar=null, line_out_energy=null, line_out_efficiency=null;
			try {
				file=new File(data_dir+"\\"+common_filename+filenum+".txt");
				br=new BufferedReader(new FileReader(file));
				
				for(int j=0; j<8; j++)
					br.readLine();
				
				line_tar=br.readLine();
				line_energy=br.readLine();
				line_efficiency=br.readLine();
				line_tar=line_tar.substring(line_tar.indexOf("=")+2, line_tar.indexOf("%"));
				line_energy=line_energy.substring(line_energy.indexOf("=")+2, line_energy.indexOf("kwh")-1);
				line_efficiency=line_efficiency.substring(line_efficiency.indexOf(":")+2);
				tar1=Double.parseDouble(line_tar);
				energy1=Double.parseDouble(line_energy);
				efficiency1=Double.parseDouble(line_efficiency);
				
				int num_line=0;
				num_line=(SchedulingConstants.NUMBER_OF_ITERATION_PER_PROCESSOR*14+13+2+2)*SchedulingConstants.NUMBER_OF_PROCESSORS+13+1+1;
				num_line=num_line*SchedulingConstants.NUMBER_OF_ITERATIONS;
				num_line=num_line+4+7;
				for(int j=0; j<num_line; j++)
					br.readLine();
				
				line_tar=br.readLine();
				line_energy=br.readLine();
				line_efficiency=br.readLine();
				line_tar=line_tar.substring(line_tar.indexOf("=")+2, line_tar.indexOf("%"));
				line_energy=line_energy.substring(line_energy.indexOf("=")+2, line_energy.indexOf("kwh")-1);
				line_efficiency=line_efficiency.substring(line_efficiency.indexOf(":")+2);
				tar2=Double.parseDouble(line_tar);
				energy2=Double.parseDouble(line_energy);
				efficiency2=Double.parseDouble(line_efficiency);
				
				line_out_tar=""+filenum+" "+tar1+" "+tar2;
				line_out_energy=""+filenum+" "+energy1+" "+energy2;
				line_out_efficiency=""+filenum+" "+efficiency1+" "+efficiency2;
				tar_writer.println(line_out_tar);
				energy_writer.println(line_out_energy);
				efficiency_writer.println(line_out_efficiency);
				
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		tar_writer.flush(); energy_writer.flush(); efficiency_writer.flush();
		tar_writer.close(); energy_writer.close(); efficiency_writer.close();
	}
}
