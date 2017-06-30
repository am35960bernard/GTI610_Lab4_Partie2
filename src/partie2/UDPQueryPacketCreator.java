package partie2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class UDPQueryPacketCreator {
	int standardStartNbrOfBytes = 16;
	int PosStartOfDomain = 12;
	int nbrOfBytesInQuerry = 0;
	String[] domainParts;
	
	public UDPQueryPacketCreator(){	
	}

	private void evaluateDomainName(String domainName){
		
		int nbrOfAdditionalBytes = 0;

		
		domainName = domainName.replaceAll("\\s+","");
		
		nbrOfAdditionalBytes = domainName.length() + 2;
		
		domainParts = domainName.split("\\."); 

		nbrOfBytesInQuerry = standardStartNbrOfBytes + nbrOfAdditionalBytes;
		
		
	}
	
	public void CreateQueryPacket(String domainName){
		
		DatagramSocket sendsocket = null;
		int destPort = 53; 
	    InetAddress address = null;
	    
	    evaluateDomainName(domainName);
	    
		try {
			address = InetAddress.getLocalHost();
			System.out.println("localhost: " + address);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} 
	    String destIp = address.getHostAddress() ;	
		UDPSender udps = new UDPSender(destIp, destPort, sendsocket);


		byte[] EnteteEtQuestion = new byte[nbrOfBytesInQuerry];

		
		int randomInt1 = (int)(Math.random() * 256);
		int randomInt2 = (int)(Math.random() * 256);
		byte randomByte1 = (byte) randomInt1 ;
		byte randomByte2 = (byte) randomInt2 ;

		// ID xxxx xxxx  xxxx xxxx
		EnteteEtQuestion[0] = (byte)randomByte1;//xxxx xxxx
		EnteteEtQuestion[1] = (byte)randomByte2;//xxxx xxxx		
		
		// QR + opcode + AA + TC + RD --> 0 0000 0 0 1
		EnteteEtQuestion[2] = (byte) 0x01;//0000 0001
		
		// RA + Z + RCODE --> 0 000 0000
		EnteteEtQuestion[3] = (byte) 0x00;//0000 0000

		//Question
		EnteteEtQuestion[4] = (byte) 0x00; //0000 0000
		EnteteEtQuestion[5] = (byte) 0x01; //0000 0001
		
		//Answer RRs
		EnteteEtQuestion[6] = (byte) 0x00; //0000 0000	
		EnteteEtQuestion[7] = (byte) 0x00; //0000 0000
		
		//Authority RRs
		EnteteEtQuestion[8] = (byte) 0x00; //0000 0000
		EnteteEtQuestion[9] = (byte) 0x00; //0000 0000
		
		//Additional RRs:
		EnteteEtQuestion[10] = (byte) 0x00; //0000 0000
		EnteteEtQuestion[11] = (byte) 0x00; //0000 0000	


		
		int counterPosOfDomain = PosStartOfDomain;
		for (int i = 0; i < domainParts.length; i++) {
			
			String word = domainParts[i] ;
						
			EnteteEtQuestion[counterPosOfDomain] = (byte) word.length(); 	
	

			for (int j = 0; j < word.length(); j++) {
				
				counterPosOfDomain ++;
				
				byte b = (byte)((byte)word.charAt(j)& 0xFF);
				EnteteEtQuestion[counterPosOfDomain] = (byte) b; 
	    
			}
			
			counterPosOfDomain ++;
			
		}
		
		
		EnteteEtQuestion[counterPosOfDomain] = (byte) 0x00;//0000 0000
		counterPosOfDomain ++;
	
	
	
		//Type :A	
		EnteteEtQuestion[counterPosOfDomain] = (byte) 0x00;//0000 0000
		counterPosOfDomain ++;	
		
	
		EnteteEtQuestion[counterPosOfDomain] = (byte) 0x01;//0000 0001
		counterPosOfDomain ++;	
		
	
		//Class :IN		
		EnteteEtQuestion[counterPosOfDomain] = (byte) 0x00;//0000 0000
		counterPosOfDomain ++;	
		
	
		EnteteEtQuestion[counterPosOfDomain] = (byte) 0x01;//0000 0001
		counterPosOfDomain ++;		
	
				
		try {
		
			udps.SendPacketNow(new DatagramPacket(EnteteEtQuestion, EnteteEtQuestion.length));
		} catch (IOException e) {			
			e.printStackTrace();
		}
				
	}
	
}
