package networksProject2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI {
	private InetSocketAddress SERVER;
	private int PORT;
	private int localPort;
	private int reliableMode;
	private long windowSize;
	private String filename;
	private long timeout;
	private BufferedInputStream buff;
	private int LAR; // last ack received
	private int LFS; // last frame sent
	private int CFS; // current frame sending
	private int seqNum;
	
	public RSendUDP() {
		PORT = 32456;
		SERVER = new InetSocketAddress("localhost", PORT);
		localPort = 12987;
		reliableMode = 0;
		windowSize = 256;
		filename = "tu.txt";
		timeout = 1000;
		LFS = 0;
		LAR = LFS;
		seqNum = 0;
	}

	public static void main(String[] args) {
		RSendUDP rsendUDP = new RSendUDP();
		rsendUDP.sendFile();

	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public int getLocalPort() {
		return localPort;
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
	public InetSocketAddress getReceiver() {
		return SERVER;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public boolean sendFile() {
		try {
			buff = new BufferedInputStream(new FileInputStream(getFilename()));
			long fileLength = buff.available();
			UDPSocket socket = new UDPSocket(localPort);
			int frameDataLength = socket.getSendBufferSize()-8;
			// printing initial message
			System.out.println("Sending "
					+ getFilename()
					+ " from "
					+ socket.getLocalAddress().getHostAddress()
					+ ":"
					+ socket.getLocalPort()
					+ " to "
					+ getReceiver().getAddress().getHostAddress()
					+ ":"
					+ getReceiver().getPort()
					+ (reliableMode == 0 ? " Using stop-and-wait."
							: "Using sliding window."));
			// buffer is going to be used for creating frame's data part.
			byte[] buffer = new byte[socket.getSendBufferSize()-8];
			Frame[] frames = new Frame[(int)(fileLength/(long)frameDataLength)];
			for(seqNum = 0; seqNum<((int)(fileLength/(long)frameDataLength)); seqNum++){
				System.out.println(socket.getSendBufferSize());
				buff.read(buffer, 0, (socket.getSendBufferSize()-8));
				frames[seqNum] = new Frame();
				frames[seqNum].setFrame(ByteBuffer.allocate(8).putInt(seqNum).array(), buffer);
			}
			
			while (LFS < ((int)(fileLength/(long)frameDataLength))) {
			
				for(seqNum = LFS; (seqNum-LFS) < windowSize; seqNum++){
					DatagramPacket packet = new DatagramPacket(frames[seqNum].getframe(),
							frames[seqNum].getframe().length, getReceiver());
					System.out.println(packet.getData());
					socket.send(packet);
					Thread ackreceiver = new ACKreceiverThread(socket);
					ackreceiver.start();
				}
			}
			return true;
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
	public boolean setLocalPort(int portNumber) {
		localPort = portNumber;
		return false;
	}

	@Override
	public boolean setMode(int mode) {
		reliableMode = mode;
		return true;
	}

	@Override
	public boolean setModeParameter(long size) {
		windowSize = size;
		return true;
	}

	@Override
	public boolean setReceiver(InetSocketAddress receiverIP) {
		SERVER = receiverIP;
		return true;
	}

	@Override
	public boolean setTimeout(long time) {
		timeout = time;
		return true;
	}
}
