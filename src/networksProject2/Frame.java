package networksProject2;

public class Frame {
	private byte[] seqNum;
	private byte[] data;
	
	public Frame(byte[] num, byte[] fdata){
		seqNum = num;
		data = fdata;
	}

}
