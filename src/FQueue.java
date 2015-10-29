import java.util.*;

public class FQueue {
	private LinkedList<Message> queue;
	private Object lock;
	
	public FQueue() {
		lock =  new Object();
		queue = new LinkedList<Message>();
	}
	
	public void offer(Message m) {
		synchronized(lock) {
			queue.add(m);
		}
	}
	
	public Message poll(){
		synchronized(lock){
			return queue.removeFirst();
			
		}
	}
	
	public boolean isEmpty(){
		synchronized(lock){
			return queue.isEmpty();
		}
	}
	
	



}
