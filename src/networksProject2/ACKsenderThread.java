package networksProject2;

import edu.utulsa.unet.UDPSocket;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class ACKsenderThread extends Thread {
	private UDPSocket socket;
	private DatagramPacket packet;
	private int seqNum;

	public ACKsenderThread(UDPSocket soc, DatagramPacket pac, int seqN) {
		System.out.println("initializing ack sender thread.");
		socket = soc;
		packet = pac;
		seqNum = seqN;
	}

	public void run() {
		System.out.println("sending ack : "+seqNum);
		try {
			byte[] ack = ByteBuffer.allocate(4).putInt(seqNum)
					.array();
			DatagramPacket sendACK = new DatagramPacket(ack, ack.length,
					packet.getAddress(), packet.getPort());
			socket.send(sendACK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
