import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

public class Frame {
	private byte[] seqNum;
	private byte[] data;
	long delay;
	private Timer timer;
	
	public Frame(byte[] num, byte[] fdata, long d){
		seqNum = num;
		data = fdata;
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
	
	public void startTimer(RSendUDP sender){
		timer.schedule(new ResendFrameThread(sender, GlobalFunction.convertByteArrayToInt(seqNum)), delay);
	}
	
	public void cancelTimer(){
		timer.cancel();
	}

}
