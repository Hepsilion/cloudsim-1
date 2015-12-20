package scheduling.our_approach;

import java.util.ArrayList;
import java.util.List;

public class OrderedList {
	private OrderedListElementCloudlet head;
	
	public OrderedList() {
		super();
		this.head = null;
	}

	public void addCloudlet(SchedulingCloudlet cloudlet) {
		OrderedListElementCloudlet current = this.head;
		OrderedListElementCloudlet previous = null;
		
		OrderedListElementCloudlet cl = new OrderedListElementCloudlet(cloudlet);
		
		while(current!=null && current.compareTo(cl)==-1) {
			previous = current;
			current = current.getNext();
		}
		
		if(previous==null) {
			if(current==null){
				this.head=cl;
			}else{
				cl.setNext(current);
				this.head=cl;
			}
		}else{
			if(current==null) {
				previous.setNext(cl);
			}else{
				cl.setNext(current);
				previous.setNext(cl);
			}
		}
	}
	
	public boolean delCloudlet(SchedulingCloudlet cloudlet) {
		OrderedListElementCloudlet current = this.head;
		OrderedListElementCloudlet previous = null;
		
		while(current!=null && !current.getCloudlet().equals(cloudlet)) {
			previous = current;
			current=current.getNext();
		}
		if(current.getCloudlet().equals(cloudlet)){
			if(previous==null)
				this.head=current.getNext();
			else{
				previous.setNext(current.getNext());
				current=null;
			}
			return true;
		}else{
			return false;
		}
	}
	
	public List<SchedulingCloudlet> getCloudlets() {
		List<SchedulingCloudlet> cloudlets=new ArrayList<SchedulingCloudlet>();
		OrderedListElementCloudlet current = this.head;
		while(current!=null) {
			cloudlets.add(current.getCloudlet());
			current = current.getNext();
		}
		return cloudlets;
	}
}
