package scheduling.our_approach.utility;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class SchedulingCloudlet extends Cloudlet {
	private double startTime;
	private double deadline;
	
	public SchedulingCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, double startTime, double deadline,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw);
		this.startTime = startTime;
		this.deadline = deadline;
	}
	
	public double getStartTime() {
		return startTime;
	}
	
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	public double getDeadline() {
		return deadline;
	}

	public void setDeadline(double deadline) {
		this.deadline = deadline;
	}
	
	@Override
	public boolean equals(Object obj) {
		SchedulingCloudlet another = (SchedulingCloudlet) obj;
		if(this.getCloudletId()==another.getCloudletId())
			return true;
		else
			return false;
	}

	public double getIntersectionTime(SchedulingCloudlet cloudlet) {
		int kind = this.intersected(cloudlet);
		//System.out.println(this.getCloudletId()+":"+cloudlet.getCloudletId()+"---"+kind);
		if(kind==1) {
			return this.getDeadline()-this.getStartTime();
		}else if(kind==2) {
			return cloudlet.getDeadline()-cloudlet.getStartTime();
		}else if(kind==3) {
			return this.getDeadline()-cloudlet.getStartTime();
		}else if(kind==4) {
			return cloudlet.getDeadline()-this.getStartTime();
		}else{
			return 0;
		}
	}
	
	/**
	 * <pre>
	 * kind1         |*********|                         <br>   
	 *           |******************|
	 * <br>
	 * kind2     |******************|                    <br>
	 *               |*********|
	 * <br>            
	 * kind3     |*********|                             <br>
	 *                 |************|
	 * <br>              
	 * kind4           |*******|                         <br>
	 *           |*********|
	 * <br>          
	 * kind5     |*******|                               <br>
	 *                       |***********|
	 * </pre>
	 * @param cloudlet
	 * @return
	 */
	public int intersected(SchedulingCloudlet cloudlet){
		if(this.getStartTime()>=cloudlet.getStartTime() && this.getDeadline()<=cloudlet.getDeadline())
			return 1;
		else if(this.getStartTime()<=cloudlet.getStartTime() && this.getDeadline()>=cloudlet.getDeadline())
			return 2;
		else if(this.getStartTime()<cloudlet.getStartTime() && this.getDeadline()<cloudlet.getDeadline() && this.getDeadline()>cloudlet.getStartTime())
			return 3;
		else if(this.getStartTime()<cloudlet.getDeadline() && this.getDeadline()>cloudlet.getDeadline() && this.getStartTime()>cloudlet.getStartTime())
			return 4;
		else
			return 5;
	}
}
