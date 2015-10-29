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
	private long fileLength;
	private long countBytes;
	private long time;
	private long mtu;

	public RReceiveUDP() {
		time = 0;
		PORT = 32456;
		reliableMode = 0;
		windowSize = 256;
		filename = "";
		// filename = "imgout.jpg";
		recMap = new HashMap<Integer, byte[]>();
		try {
			socket = new UDPSocket(PORT);
			mtu = socket.getSendBufferSize();
			left = 0;
			right = (int) (windowSize / (mtu - 4));
		} catch (SocketException e) {
			e.printStackTrace();
		}
		countBytes = 0;
		fileLength = windowSize*10;
	}

//	public static void main(String[] args) {
//		RReceiveUDP rreceiveUDP = new RReceiveUDP();
//	}

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
			System.out.println("Receiving "
				+ getFilename()
				+ " on "
				+ socket.getLocalAddress().getHostAddress()
				+ ":"
				+ socket.getLocalPort()
				+ (reliableMode == 0 ? " Using stop-and-wait."
						: " Using sliding window."));
			buff = new BufferedOutputStream(new FileOutputStream(filename));
			byte[] buffer = new byte[socket.getSendBufferSize()];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			// while (countBytes <= fileLength) {
			while(true){
				socket.receive(packet);
				client = packet.getAddress();
				int seqNum = GlobalFunction.convertByteArrayToInt(buffer);
				if (seqNum == 0) {
					time = System.currentTimeMillis();
					fileLength = GlobalFunction
							.convertByteArrayToLong(Arrays.copyOfRange(
									packet.getData(), 4, packet.getLength()));
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
							+ " with "
							+ fileLength
							+ " bytes "
							+ (reliableMode == 0 ? " Using stop-and-wait."
									: " Using sliding window."));
				}
				byte[] databuffer = Arrays.copyOfRange(buffer, 4,
						packet.getLength());
				System.out.println("Message " + seqNum + " received.");
				if (seqNum == left) {
					do {
						if (seqNum != left) {
							databuffer = Arrays.copyOfRange(recMap.get(left),
									4, recMap.get(left).length);
						}
						if (seqNum != 0) {
							buff.write(databuffer, 0, databuffer.length);
							countBytes += databuffer.length;
							// System.out.println("bufferLength is: "+databuffer.length+", countBytes: "+countBytes+"total Length is: "+fileLength);
						}
						left++;
						right++;
					} while (recMap.containsKey(left));
				} else if (seqNum > left && seqNum <= right) {
					byte[] saveData = new byte[packet.getLength()];
					saveData = packet.getData().clone();
					recMap.put(seqNum, saveData);
				}
				if(left > 0){
					System.out.println("Sending message "+(left - 1 )+"'s ack.");
					byte[] ack = GlobalFunction.convertIntToByteArray(left - 1);
					DatagramPacket sendACK = new DatagramPacket(ack, ack.length,
							packet.getAddress(), packet.getPort());
					socket.send(sendACK);
				}
				
				if(countBytes >= fileLength){
					buff.close();
					time = System.currentTimeMillis() - time;
					System.out.println("Successfully transferred "+filename+" ("+fileLength+" bytes) in "+(time/1000)+" seconds");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		setModeParameter(windowSize);
		return true;
	}

	@Override
	public boolean setModeParameter(long size) {
		if(reliableMode == 0)
			windowSize = mtu;
		else
			windowSize = size;
		return true;
	}
	
	public static void main(String[] args){
		RReceiveUDP r = new RReceiveUDP();
		r.setMode(1);
		r.setModeParameter(1200);			
		r.setFilename("tuout.txt");
		r.setLocalPort(32456);
		r.receiveFile();
	}
}
