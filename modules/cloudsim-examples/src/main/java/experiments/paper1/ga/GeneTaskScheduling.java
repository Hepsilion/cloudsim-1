package experiments.paper1.ga;

public class GeneTaskScheduling {
	private int vm;
	private int host;
	private int frequency;

	public GeneTaskScheduling(int vm, int host, int frequency) {
		this.vm = vm;
		this.host = host;
		this.frequency = frequency;
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

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public boolean equals(Object obj) {
		GeneTaskScheduling another = (GeneTaskScheduling) obj;
		if (this.vm == another.getVm() && this.host == another.getHost() && this.frequency == another.getFrequency())
			return true;
		else
			return false;
	}
}
