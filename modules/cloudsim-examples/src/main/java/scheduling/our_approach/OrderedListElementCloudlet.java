package scheduling.our_approach;

public class OrderedListElementCloudlet {
	private SchedulingCloudlet cloudlet;
	private OrderedListElementCloudlet next;
	
	public OrderedListElementCloudlet(SchedulingCloudlet cloudlet) {
		super();
		this.cloudlet = cloudlet;
		this.next = null;
	}

	public SchedulingCloudlet getCloudlet() {
		return cloudlet;
	}

	public void setCloudlet(SchedulingCloudlet cloudlet) {
		this.cloudlet = cloudlet;
	}

	public OrderedListElementCloudlet getNext() {
		return next;
	}

	public void setNext(OrderedListElementCloudlet next) {
		this.next = next;
	}

	public int compareTo(OrderedListElementCloudlet another) {
		if(this.getCloudlet().getStartTime() < another.getCloudlet().getStartTime())
			return -1;
		else if(this.getCloudlet().getStartTime() == another.getCloudlet().getStartTime())
			return 0;
		else
			return 1;
	}
}
