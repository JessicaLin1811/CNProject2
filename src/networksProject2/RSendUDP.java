package networksProject2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Timer;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI {
	private InetSocketAddress SERVER;
	private int PORT;
	private int localPort;
	private int reliableMode;
	UDPSocket socket;
	private long windowSize;
	private String filename;
	private long timeout;
	private BufferedInputStream buff;
	private int left; // last ack received
	private int right; // last frame sent
	private FQueue mQueue;
	// HashMap to store the frames.Integer is the sequence number
	private HashMap<Integer, Frame> frames;
	int frameDataLength;

	public RSendUDP() {
		PORT = 32456;
		SERVER = new InetSocketAddress("localhost", PORT);
		localPort = 12987;
		reliableMode = 0;
		windowSize = 256;
		filename = "tu.txt";
		timeout = 1000;
		left = 0;
		right = 0;
		mQueue = new FQueue();
		frames = new HashMap<Integer, Frame>();
		try {
			socket = new UDPSocket(localPort);
			frameDataLength = socket.getSendBufferSize() - 4;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		for(int i = 0; i < (windowSize/frameDataLength); i++){
			readAndOffer();
		}
		
		sendFile();
	}

	public HashMap<Integer, Frame> getFrames() {
		return frames;
	}

	public void setFrames(HashMap<Integer, Frame> frames) {
		this.frames = frames;
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
		// Start the ack receiver thread
		Thread ackreceiver = new ACKreceiverThread(socket, this);
		ackreceiver.start();

		while (!mQueue.isEmpty()) {
			Message currM = mQueue.poll();
			Frame currF = frames.get(currM.getSeqNum());
			DatagramPacket packet = new DatagramPacket(currF.getframe(),
					currF.getframe().length, getReceiver());
			try {
				socket.send(packet);
				currF.startTimer(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return true;

	}

	public FQueue getmQueue() {
		return mQueue;
	}

	public void setmQueue(FQueue mQueue) {
		this.mQueue = mQueue;
	}

	public void readAndOffer() {
		try {
			buff = new BufferedInputStream(new FileInputStream(getFilename()));
			long fileLength = buff.available();
			System.out.println("MTU = " + socket.getSendBufferSize());
			byte[] buffer = new byte[frameDataLength];
			frameDataLength = (int) (fileLength < frameDataLength ? fileLength
					: frameDataLength);
			buff.read(buffer, right * frameDataLength, frameDataLength);
			frames.put(right, new Frame(ByteBuffer.allocate(4).putInt(right)
					.array(), buffer, timeout));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mQueue.offer(new Message(right, false));
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
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

	public static void main(String[] args) {
		RSendUDP sender = new RSendUDP();
	}
}
