package networksProject2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP implements edu.utulsa.unet.RReceiveUDPI {
	private int PORT;
	private int reliableMode;
	private long windowSize;
	private String filename;
	private BufferedOutputStream buff;
	private int LFR;
	private int LAF;

	public RReceiveUDP() {
		PORT = 32456;
		reliableMode = 0;
		windowSize = 256;
		filename = "tuout.txt";
	}

	public static void main(String[] args) {
		RReceiveUDP rreceiveUDP = new RReceiveUDP();
		rreceiveUDP.receiveFile();
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
			buff = new BufferedOutputStream(new FileOutputStream(getFilename()));
			byte[] buffer = new byte[11];
			UDPSocket socket = new UDPSocket(PORT);
			System.out.println("Receiving "
					+ getFilename()
					+ " on "
					+ socket.getLocalAddress().getHostAddress()
					+ ":"
					+ socket.getLocalPort()
					+ (reliableMode == 0 ? " Using stop-and-wait."
							: "Using sliding window."));
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			InetAddress client = packet.getAddress();
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
			Thread acksender = new ACKsenderThread(socket, packet);
			acksender.start();
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
		return true;
	}

	@Override
	public boolean setModeParameter(long ws) {
		windowSize = ws;
		return true;
	}

}
