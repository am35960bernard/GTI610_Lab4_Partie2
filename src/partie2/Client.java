package partie2;



public class Client {

	public Client() {

	}

	public static void main(String[] args) {
		UDPQueryPacketCreator uneRequete = new UDPQueryPacketCreator();
		uneRequete.CreateQueryPacket("soleil.ca");
		
	}



}
