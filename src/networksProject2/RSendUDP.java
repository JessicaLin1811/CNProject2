package networksProject2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

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

	public RSendUDP() {
		PORT = 32456;
		SERVER = new InetSocketAddress("localhost", PORT);
		localPort = 12987;
		reliableMode = 0;
		windowSize = 256;
		filename = "tu.txt";
		timeout = 1000;
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
			byte[] buffer = ("Hello World- or rather Mauricio saying hello through UDP")
					.getBytes();
			UDPSocket socket = new UDPSocket(localPort);
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
					getReceiver());
			socket.send(packet);
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
			Thread ackreceiver = new ACKreceiverThread(socket);
			ackreceiver.start();
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
