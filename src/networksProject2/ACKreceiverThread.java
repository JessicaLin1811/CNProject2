package networksProject2;

import java.net.DatagramPacket;

import edu.utulsa.unet.UDPSocket;

public class ACKreceiverThread extends Thread {
	private UDPSocket socket;
	
	public ACKreceiverThread(UDPSocket soc){
		socket = soc;
	}
	
	public void run() {
		try {
			byte[] ack = new byte[1024];
			DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
			socket.receive(ackPacket);
			System.out
					.println("ACK received."
							+ new String(ackPacket.getData(), 0, ackPacket
									.getLength()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
