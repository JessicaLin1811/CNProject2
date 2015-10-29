import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import edu.utulsa.unet.UDPSocket;

public class ACKreceiverThread extends Thread {
	private UDPSocket socket;
	private RSendUDP sender;

	public ACKreceiverThread(UDPSocket soc, RSendUDP s) {
		socket = soc;
		sender = s;
	}

	public void run() {
		try {
			byte[] ack = new byte[4];
			DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
			while (true) {
				socket.receive(ackPacket);
				int ackNum = GlobalFunction.convertByteArrayToInt(ackPacket
						.getData());
				System.out.println("Message " + ackNum+" acknowledged.");
				sender.getmQueue().offer(new Message(ackNum, false));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
