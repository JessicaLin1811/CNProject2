package networksProject2;

import java.util.TimerTask;

public class ResendFrameThread extends TimerTask {

	RSendUDP sender;
	int seqNum;
	
	public ResendFrameThread(RSendUDP s, int seq){
		sender = s;
		seqNum = seq;
	}
	
	@Override
	public void run() {
	    sender.getmQueue().offer(new Message(seqNum, true));
		
	}

}
