package experiments.paper1.main.twoEncode;

public class GeneTaskScheduling {
	private int vm;
	private int host;

	public GeneTaskScheduling(int vm, int host) {
		this.vm = vm;
		this.host = host;
	}

	public int getVm() {
		return vm;
	}

	public void setVm(int vm) {
		this.vm = vm;
	}
	
	public int getHost() {
		return host;
	}

	public void setHost(int host) {
		this.host = host;
	}


	@Override
	public boolean equals(Object obj) {
		GeneTaskScheduling another = (GeneTaskScheduling) obj;
		if (this.vm == another.getVm() && this.host == another.getHost())
			return true;
		else
			return false;
	}
}
