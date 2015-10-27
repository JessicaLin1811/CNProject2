package networksProject2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP implements edu.utulsa.unet.RReceiveUDPI {
	private int PORT;
	private int reliableMode;
	private long windowSize;
	private String filename;
	private BufferedOutputStream buff;
	private int left;
	private int right;
	private HashMap<Integer, byte[]> recMap;
	private UDPSocket socket;
	private InetAddress client;

	public RReceiveUDP() {
		PORT = 32456;
		reliableMode = 0;
		windowSize = 256;
		filename = "tuout.txt";
		recMap = new HashMap<Integer, byte[]>();
		try {
			socket = new UDPSocket(PORT);
			left = 0;
			right = (int)windowSize/(socket.getReceiveBufferSize()-4);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		System.out.println("Receiving "
				+ getFilename()
				+ " on "
				+ socket.getLocalAddress().getHostAddress()
				+ ":"
				+ socket.getLocalPort()
				+ (reliableMode == 0 ? " Using stop-and-wait."
						: "Using sliding window."));
		receiveFile();
	}

	public static void main(String[] args) {
		RReceiveUDP rreceiveUDP = new RReceiveUDP();
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public int getLocalPort() {
		return PORT;
	}

	@Override
	public int getMode() {
		return reliableMode;
	}

	@Override
	public long getModeParameter() {
		return windowSize;
	}

	@Override
	public boolean receiveFile() {
		try {
			buff = new BufferedOutputStream(new FileOutputStream(filename));
			byte[] buffer = new byte[socket.getReceiveBufferSize()];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (true) {
				socket.receive(packet);
				client = packet.getAddress();
				System.out.println("Receiving "
						+ getFilename()
						+ " on "
						+ socket.getLocalAddress().getHostAddress()
						+ ":"
						+ socket.getLocalPort()
						+ " from sender "
						+ packet.getAddress().getHostAddress()
						+ ":"
						+ packet.getPort()
						+ (reliableMode == 0 ? " Using stop-and-wait."
								: "Using sliding window."));
				
				
				int seqNum = byteArrayToInt(Arrays.copyOfRange(buffer, 0, 4));
				buffer = Arrays.copyOfRange(buffer, 4, buffer.length-5);
				if(seqNum == left){
					System.out.println("seqNum == left");
					do{
						if(seqNum != left){
							buffer = recMap.get(left+1);
						}
						buff.write(buffer, 0, buffer.length);
						// Thread acksender = new ACKsenderThread(socket, packet, left);
						// acksender.start();
						left++;
						right++;
					}while(recMap.containsKey(left+1));
					byte[] ack = ByteBuffer.allocate(4).putInt(seqNum)
							.array();
					DatagramPacket sendACK = new DatagramPacket(ack, ack.length,
							packet.getAddress(), packet.getPort());
					socket.send(sendACK);
					
				}else if(seqNum > left && seqNum <=right){
					System.out.println("seqNum > left and <= right");
					recMap.put(seqNum, buffer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	@Override
	public void setFilename(String file) {
		filename = file;

	}

	@Override
	public boolean setLocalPort(int p) {
		PORT = p;
		return false;
	}

	@Override
	public boolean setMode(int mode) {
		reliableMode = mode;
		return true;
	}

	@Override
	public boolean setModeParameter(long ws) {
		windowSize = ws;
		return true;
	}
	

}
