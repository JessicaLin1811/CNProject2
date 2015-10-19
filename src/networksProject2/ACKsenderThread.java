package networksProject2;

import edu.utulsa.unet.UDPSocket;
import java.net.DatagramPacket;

public class ACKsenderThread extends Thread {
	private UDPSocket socket;
	private DatagramPacket packet;

	public ACKsenderThread(UDPSocket soc, DatagramPacket dp) {
		socket = soc;
		packet = dp;
	}

	public void run() {
		try {
			byte[] ack = ("ACK").getBytes();
			DatagramPacket sendACK = new DatagramPacket(ack, ack.length,
					packet.getAddress(), packet.getPort());
			socket.send(sendACK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
