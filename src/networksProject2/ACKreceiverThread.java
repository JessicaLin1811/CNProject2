package networksProject2;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import edu.utulsa.unet.UDPSocket;

public class ACKreceiverThread extends Thread {
	private UDPSocket socket;
	private int a;
	private RSendUDP sender;

	public ACKreceiverThread(UDPSocket soc, RSendUDP s) {
		socket = soc;
		sender = s;
	}

	public void run() {
		try {
			byte[] ack = new byte[1024];
			DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
			while (true) {
				socket.receive(ackPacket);
				System.out.println("ACK received."
						+ new String(ackPacket.getData(), 0, ackPacket
								.getLength()));
				int ackNum = ByteBuffer.wrap(ackPacket.getData()).getInt();
				if((ackNum >= sender.getLeft())&&(ackNum <= sender.getRight())){
					sender.setLeft(ackNum);
					sender.getFrames().get(ackNum).cancelTimer();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
