package networksProject2;

public class GlobalFunction {
	public static final int iMyheader = 4;
	
	public static String getModeName(int mode) {
		return mode == 0 ? "Stop-And-Wait" : "Sliding Window";
	}
	
    public static byte[] convertIntToByteArray(int n) {
    	byte[] array = new byte[4];
    	for (int i = 0; i < 4; i++) {    		
    		array[i] = (byte)(n & 0xff);
    		n = n >> 8;    		
    	}
    	return array;
    }
    
    public static int convertByteArrayToInt(byte[] array) {
    	int n = 0;
    	for (int i = 0; i < 4; i++) {
    		int tmp = array[i];
    		tmp = tmp & 0xff;
    		tmp = tmp << (i*8);
    		n += tmp; 
    	}
    	
    	return n;
    }
    
    public static byte[] convertLongToByteArray(long n){
    	byte[] array = new byte[8];
    	for (int i = 0; i < 8; i++) {    		
    		array[i] = (byte)(n & 0xff);
    		n = n >> 8;    		
    	}
    	return array;
    }
    
    public static int convertByteArrayToLong(byte[] array){
    	int n = 0;
    	for (int i = 0; i < 8; i++) {
    		int tmp = array[i];
    		tmp = tmp & 0xff;
    		tmp = tmp << (i*8);
    		n += tmp; 
    	}
    	
    	return n;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long n = 1799000000;
		byte[] array = convertLongToByteArray(n);
		System.out.println(convertByteArrayToLong(array));
		
		byte[] w = new byte[0];

	}

}
