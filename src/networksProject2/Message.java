package networksProject2;

public class Message {
	private int seqNum;
	private boolean isFromeTimer;

	public Message(int seqNum, boolean isFromeTimer) {
		super();
		this.seqNum = seqNum;
		this.isFromeTimer = isFromeTimer;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public boolean isFromeTimer() {
		return isFromeTimer;
	}

	public void setFromeTimer(boolean isFromeTimer) {
		this.isFromeTimer = isFromeTimer;
	}
	

}
