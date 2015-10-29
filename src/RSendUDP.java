import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI {
	private InetSocketAddress SERVER;
	private int PORT;
	private int localPort;
	private int reliableMode;
	private UDPSocket socket;
	private long windowSize;
	private String filename;
	private long timeout;
	private BufferedInputStream buff;
	private int left; // last ack received
	private int right; // last frame sent
	private FQueue mQueue;
	// HashMap to store the frames.Integer is the sequence number
	private HashMap<Integer, Frame> frames;
	private int frameDataLength;
	private long time;
	private int lastFrameSeq;
	private long totalFileLength;
	private long mtu;

	public RSendUDP() {
		totalFileLength = 0;
		PORT = 32456;
//		SERVER = new InetSocketAddress("129.244.55.185", PORT);
		SERVER = new InetSocketAddress("localhost", PORT);
		localPort = 12987;
		reliableMode = 0;
		windowSize = 256;
		// filename = "TestVideo2.mov";
		filename = "";
		timeout = 1500;
		left = 0;
		right = 0;
		mQueue = new FQueue();
		frames = new HashMap<Integer, Frame>();
		time = 0;
		lastFrameSeq = Integer.MAX_VALUE;
		try {
			socket = new UDPSocket(localPort);
			mtu = socket.getSendBufferSize();
			frameDataLength = socket.getSendBufferSize() - 4;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
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
		try {
			buff = new BufferedInputStream(new FileInputStream(getFilename()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		time = System.currentTimeMillis();
		// Start the ack receiver thread
		Thread ackreceiver = new ACKreceiverThread(socket, this);
		ackreceiver.start();

		for (int i = 0; i < (windowSize / frameDataLength); i++) {
			Frame currF = read(i);
			if(currF == null){
				break;
			}
			DatagramPacket packet = new DatagramPacket(currF.getframe(),
					currF.getframe().length, getReceiver());
			callSend(packet, currF, false);
			right++;
			currF.startTimer(this);
		}

		while (true) {
			if (!mQueue.isEmpty()) {
				// get message from mQueue
				Message currM = mQueue.poll();
				// if the message is not from timer, then it's from the
				// receiver, check and move left and right pointers.
				if (!currM.isFromeTimer()) {
					if (currM.getSeqNum() >= left && currM.getSeqNum() < right) {
						for (int i = left; i <= currM.getSeqNum(); i++) {
							frames.get(i).cancelTimer();
							frames.remove(i);
						}
						left = currM.getSeqNum() + 1;
						for (int i = right; i < (left + (windowSize / frameDataLength)); i++) {
							Frame currF = read(i);
							if(currF == null){
								break;
							}
							DatagramPacket packet = new DatagramPacket(
									currF.getframe(), currF.getframe().length,
									getReceiver());
							callSend(packet, currF, false);
							right++;
						}
					}
					if(currM.getSeqNum() >= (lastFrameSeq-1)){
						time = System.currentTimeMillis() - time;
						System.out.println("Successfully transferred "+filename+" ( "+totalFileLength+" bytes) in "+(time/1000)+" seconds.");
						try {
							buff.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				// if it is from the timer, then resent the frame.
				else {
					Frame currF = frames.get(currM.getSeqNum());
					DatagramPacket packet = new DatagramPacket(
							currF.getframe(), currF.getframe().length,
							getReceiver());
					System.out.println("Message "+currM.getSeqNum()+" time-out.");
					callSend(packet, currF, true);
				}
			}else{
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void callSend(DatagramPacket packet, Frame currF, boolean isFromTimer) {
		// send packet, start timer, and print
		try {
			socket.send(packet);
			System.out.println("Message "+ GlobalFunction.convertByteArrayToInt(packet.getData())
					+ " sent with "
					+ (packet.getLength()-4)
					+ " bytes of actual Data "
					+ (isFromTimer ? "(From timer)." : ".")
					);
			currF.startTimer(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FQueue getmQueue() {
		return mQueue;
	}

	public void setmQueue(FQueue mQueue) {
		this.mQueue = mQueue;
	}

	public Frame read(int seqNum) {
		try {

			long fileLength = buff.available();
			byte[] buffer = new byte[(int) (fileLength < frameDataLength ? fileLength
					: frameDataLength)];
			if (seqNum == 0) {
				totalFileLength = fileLength;
				frames.put(
						seqNum,
						new Frame(GlobalFunction.convertIntToByteArray(seqNum),
								GlobalFunction
										.convertLongToByteArray(fileLength),
								timeout));
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
						+ " with "
						+ fileLength
						+ " bytes "
						+ (reliableMode == 0 ? " Using stop-and-wait."
								: " Using sliding window."));
				return frames.get(0);
			}
			if(fileLength == 0){
				lastFrameSeq = seqNum;
				return null;
			}
			buff.read(buffer, 0,
					(int) (fileLength < frameDataLength ? fileLength
							: frameDataLength));
			frames.put(seqNum,
					new Frame(GlobalFunction.convertIntToByteArray(seqNum),
							buffer, timeout));
			return frames.get(seqNum);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		sender.setMode(1);
		sender.setModeParameter(600);
		sender.setTimeout(1000);
		sender.setFilename("tu.txt");
		sender.setLocalPort(23456);
		sender.setReceiver(new InetSocketAddress(("127.0.0.1"),32456));
		sender.sendFile();
	}
}
