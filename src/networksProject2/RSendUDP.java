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
	private long LAR; // last ack received
	private long LFS; // last frame sent
	private long CFS; // current frame sending
	private int seqNum;
	
	public RSendUDP() {
		PORT = 32456;
		SERVER = new InetSocketAddress("localhost", PORT);
		localPort = 12987;
		reliableMode = 0;
		windowSize = 256;
		filename = "tu.txt";
		timeout = 1000;
		LAR = 0;
		LFS = LAR + windowSize;
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
			UDPSocket socket = new UDPSocket(localPort);
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
			byte[] buffer = new byte[socket.getSendBufferSize()];
			Frame[] frames = new Frame[buff.available()/(socket.getSendBufferSize()-8)];
			for(int i = 0; i<frames.length; i++){
				buff.read(buffer, 0, socket.getSendBufferSize()-8);
				frames[i].setFrame(ByteBuffer.allocate(8).putInt(seqNum).array(), buffer);
				seqNum++;
			}
			
	
			while (LFS < buff.available()/(socket.getSendBufferSize()-8)) {
				for(int i = 0; i<4; i++){
					DatagramPacket packet = new DatagramPacket(frames[(int)LFS+i].getframe(),
							frames[(int)LFS+i].getframe().length, getReceiver());
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
