package partie2;

import java.util.Scanner;

public class Client {

	public Client() {

	}

	public static void main(String[] args) {
		UDPQueryPacketCreator uneRequete = new UDPQueryPacketCreator();
		
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		
		
		System.out.println("Nom de domaine s.v.p.");
		
		String scannDomainName = scanner.nextLine();
		
		uneRequete.CreateQueryPacket(scannDomainName);
		
	}



}
