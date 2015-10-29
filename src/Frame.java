import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

public class Frame {
	private byte[] seqNum;
	private byte[] data;
	private boolean ackReceived;
	long delay;
	private Timer timer;
	
	public Frame(){
		ackReceived = false;
	}
	public Frame(byte[] num, byte[] fdata, long d){
		seqNum = num;
		data = fdata;
		ackReceived = false;
		delay = d;
		timer = new Timer();
	}
	
	public void setFrame(byte[] num, byte[] fdata){
		seqNum = num;
		data = fdata;
	}
	
	public byte[] getframe(){
		byte[] frame = new byte[seqNum.length+data.length];
		System.arraycopy(seqNum, 0, frame, 0, seqNum.length);
		System.arraycopy(data, 0, frame, seqNum.length, data.length);
		return frame;
	}
	
	public void setAckReceived(boolean bool){
		ackReceived = bool;
	}

	public boolean ackReceived(){
		return ackReceived;
	}
	
	public void startTimer(RSendUDP sender){
		timer.schedule(new ResendFrameThread(sender, GlobalFunction.convertByteArrayToInt(seqNum)), delay);
	}
	
	public void cancelTimer(){
		timer.cancel();
	}

}
