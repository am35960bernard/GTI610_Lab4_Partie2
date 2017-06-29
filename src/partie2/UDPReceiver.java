package partie2;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Cette classe permet la reception d'un paquet UDP sur le port de reception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui ecoute en permanance pour ne pas affecter le
 * deroulement du programme
 * 
 * @author Max
 *
 */

public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP 
	 * --------------------------
	 * En-tete (12 octects) 
	 * Question : l'adresse demande 
	 * Reponse : l'adresse IP
	 * Autorite :
	 * info sur le serveur d'autorite 
	 * Additionnel : information supplementaire
	 */

	/**
	 * Definition de l'En-tete d'un Packet UDP
	 * --------------------------------------- 
	 * Identifiant Parametres 
	 * QDcount
	 * Ancount
	 * NScount 
	 * ARcount
	 * 
	 * L'identifiant est un entier permettant d'identifier la requete. 
	 * parametres contient les champs suivant : 
	 * 		QR (1 bit) : indique si le message est une question (0) ou une reponse (1). 
	 * 		OPCODE (4 bits) : type de la requete (0000 pour une requete simple). 
	 * 		AA (1 bit) : le serveur qui a fourni la reponse a-t-il autorite sur le domaine? 
	 * 		TC (1 bit) : indique si le message est tronque.
	 *		RD (1 bit) : demande d'une requete recursive. 
	 * 		RA (1 bit) : indique que le serveur peut faire une demande recursive. 
	 *		UNUSED, AD, CD (1 bit chacun) : non utilises. 
	 * 		RCODE (4 bits) : code de retour.
	 *                       0 : OK, 1 : erreur sur le format de la requete,
	 *                       2: probleme du serveur, 3 : nom de domaine non trouve (valide seulement si AA), 
	 *                       4 : requete non supportee, 5 : le serveur refuse de repondre (raisons de securite ou autres).
	 * QDCount : nombre de questions. 
	 * ANCount, NSCount, ARCount : nombre dentrees dans les champs �Reponse�, Autorite,  Additionnel.
	 */

	protected final static int BUF_SIZE = 1024;
	protected String SERVER_DNS = null;//serveur de redirection (ip)
	protected int portRedirect = 53; // port  de redirection (par defaut)
	protected int port; // port de reception
	private String adrIP = null; //bind ip d'ecoute
	private String domainName = "";
	private String DNSFile = null;
	private boolean RedirectionSeulement = false;
	
	private class ClientInfo { //quick container
		public InetAddress client_ip = null;
		@SuppressWarnings("unused")
		public int client_port = 0;
	};
	private HashMap<Integer, ClientInfo> Clients = new HashMap<>();
	
	private boolean stop = false;

	
	public UDPReceiver() {
	}

	public UDPReceiver(String SERVER_DNS, int Port) {
		this.SERVER_DNS = SERVER_DNS;
		this.port = Port;
	}
	
	
	public void setport(int p) {
		this.port = p;
	}

	public void setRedirectionSeulement(boolean b) {
		this.RedirectionSeulement = b;
	}

	public String gethostNameFromPacket() {
		return domainName;
	}

	public String getAdrIP() {
		return adrIP;
	}

	@SuppressWarnings("unused")
	private void setAdrIP(String ip) {
		adrIP = ip;
	}

	public String getSERVER_DNS() {
		return SERVER_DNS;
	}

	public void setSERVER_DNS(String server_dns) {
		this.SERVER_DNS = server_dns;
	}


	public void setDNSFile(String filename) {
		DNSFile = filename;
	}

	public void run() {
		try {
			DatagramSocket serveur = new DatagramSocket(this.port); // *Creation d'un socket UDP

			
			AnswerRecorder answerRecord = new AnswerRecorder(DNSFile);
			QueryFinder queryfind = new QueryFinder(this.DNSFile);
			
			
			// *Boucle infinie de recpetion
			while (!this.stop) {
				byte[] buff = new byte[0xFF];
				DatagramPacket paquetRecu = new DatagramPacket(buff,buff.length);
				//System.out.println("Serveur DNS  "+serveur.getLocalAddress()+"  en attente sur le port: "+ serveur.getLocalPort());
				System.out.println("Serveur DNS  en attente sur le port: "+ serveur.getLocalPort());
				// *Reception d'un paquet UDP via le socket
				serveur.receive(paquetRecu);
				
				System.out.println("paquet recu du  "+paquetRecu.getAddress()+"  du port: "+ paquetRecu.getPort());
				

				// *Creation d'un DataInputStream ou ByteArrayInputStream pour
				// manipuler les bytes du paquet

				ByteArrayInputStream TabInputStream = new ByteArrayInputStream (paquetRecu.getData());
				
				
				
				System.out.println("\n"+ buff.toString());


					// *Lecture du Query Domain name, a partir du 13 byte
					// *Sauvegarde du Query Domain name
					// *Savoir si c est une Query ou une Answer
					// *Connaitre le ID de la Query

					byte byteRead;
					int curseurPos = 0;
					int nbrCarNextPartOfName = -1;
					int posStartNextPartOfName = 12;
					int queryStartDomainName = 12;
					int isAnswer = -1;
					boolean endOfPartOfName = false;
					int startOfAnswer = -1;
					short AnswerRecordCount = 0;
					short idPaqet = 0;
					int AfterNameBeforeQueryEnd = 5;
					int AfterAnswerStartBeforeIp = 12;
					List<String> listOfDomainIpForAquery = new ArrayList<String>();
					String domainName = "";
					int compteurDomainIp = 0;
	    			int startPosDomainIp = 0;
	    			int EndPosDomainIp = 0;
					
					while(( byteRead = (byte) TabInputStream.read())!= -1) {

						if(curseurPos == 0){
							
							idPaqet = (short)( (byteRead) << 8);
								
						}else if(curseurPos == 1){
							idPaqet =  (short) (idPaqet | (byteRead));
							
						}
						
						if(curseurPos == 2){
							
							byte mostSignificantBit = (byte) ((byteRead) >> 7);

							if(mostSignificantBit == 0){
								isAnswer = 0;
							}else{
								isAnswer = 1;
							}
						}
						
						if(curseurPos == 6 && isAnswer == 1){
							AnswerRecordCount = (short)( (byteRead) << 8);
							
						}else if(curseurPos == 7 && isAnswer == 1){
							AnswerRecordCount =  (short) (AnswerRecordCount | (byteRead));
						}

						if(curseurPos >= queryStartDomainName && endOfPartOfName == false){	    	
					    	if (byteRead != 0) { 			
								if (curseurPos == (nbrCarNextPartOfName + posStartNextPartOfName + 1)) {
									posStartNextPartOfName = curseurPos; 
									nbrCarNextPartOfName = (int)byteRead;
									
									if(posStartNextPartOfName != queryStartDomainName){
										domainName += ".";
									}
								}else{
									domainName += (char) byteRead;
								}
							}else{
								
								endOfPartOfName = true;
								if(isAnswer == 1){
									startOfAnswer = curseurPos + AfterNameBeforeQueryEnd;
									
								}
					    	}
					    	
						}
						
				    	if(isAnswer == 1 && startOfAnswer != -1){
				    		if(compteurDomainIp < AnswerRecordCount){
				    			
			    				startPosDomainIp = (startOfAnswer + AfterAnswerStartBeforeIp) + (16 * compteurDomainIp);
			    				EndPosDomainIp = (startOfAnswer + AfterAnswerStartBeforeIp + 4) + (16 * compteurDomainIp);
				    		}
				    		if(curseurPos >= startPosDomainIp && curseurPos < EndPosDomainIp){
				    			
				    			if(curseurPos == startPosDomainIp){
				    				
				    				listOfDomainIpForAquery.add(compteurDomainIp, "");
				    			}else{
				    				
				    				String concatIp = listOfDomainIpForAquery.get(compteurDomainIp) + ".";
				    				listOfDomainIpForAquery.set( compteurDomainIp, concatIp);
				    				
				    								    				
				    			}
				    			
				    			String concatIp = listOfDomainIpForAquery.get(compteurDomainIp) + (byteRead & 0xff);
				    			listOfDomainIpForAquery.set( compteurDomainIp, concatIp);
				    			
				    			if(curseurPos == (EndPosDomainIp - 1) && compteurDomainIp < AnswerRecordCount){
				    				compteurDomainIp++;
				    			}
				    		}

				    	}

						curseurPos++;
					}
					
					
					TabInputStream.reset();
					curseurPos = 0;
					System.out.println("->isAnswer: " + isAnswer);
					
					 for (int i=0; i < listOfDomainIpForAquery.size(); i++){
						 
						 System.out.println("listOfDomainIpForAquery: " + i + " :" + listOfDomainIpForAquery.get(i));
						 

					 }

				 
					// *Sauvegarde de l'adresse, du port et de l'identifiant de la requete
					ClientInfo clientInformation = new ClientInfo();
					clientInformation.client_ip = paquetRecu.getAddress();
					clientInformation.client_port = paquetRecu.getPort();
					Clients.put((int) idPaqet, clientInformation);					 
					
					if(isAnswer == 0){// ****** Dans le cas d'un paquet requete *****	
						

	
						System.out.println("Debut de la recherche DNS");
						// *Si le mode est redirection seulement
						if(RedirectionSeulement == true){
							System.out.println("le mode est redirection seulement");
							// *Rediriger le paquet vers le serveur DNS
							UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect,serveur);		 
							UDPOut.SendPacketNow(paquetRecu); 

						}else{// *Sinon
							
						// *Rechercher l'adresse IP associe au Query Domain name
						// dans le fichier de correspondance de ce serveur					

							List<String> adresseIpRecherche = queryfind.StartResearch(domainName);
							
						// *Si la correspondance n'est pas trouvee
							if(adresseIpRecherche.isEmpty()){
								System.out.println("correspondance non trouvee, donc redirection vers un serveur DNS");
								// *Rediriger le paquet vers le serveur DNS
								UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect,serveur);		 
								UDPOut.SendPacketNow(paquetRecu); 
							
							}else{// *Sinon
								
								System.out.println("Correspondance trouvee dans le fichier DNS !");
								
								UDPAnswerPacketCreator customAnswer = UDPAnswerPacketCreator.getInstance();
								
								// *Creer le paquet de reponse a l'aide du UDPAnswerPaquetCreator
								byte[] byteAnswer  = customAnswer.CreateAnswerPacket(paquetRecu.getData(),adresseIpRecherche);
																
								DatagramPacket paquetAnswer = new DatagramPacket(byteAnswer,byteAnswer.length);
								// *Placer ce paquet dans le socket
								// *Envoyer le paquet
								UDPSender UDPAnswer = new UDPSender(clientInformation.client_ip,53,serveur);		 
								UDPAnswer.SendPacketNow(paquetAnswer);

							}
						}
					}else if(isAnswer == 1){// ****** Dans le cas d'un paquet reponse *****				
						// *Ajouter la ou les correspondance(s) dans le fichier DNS
						// si elles ne y sont pas deja
						
						List<String> adresseIpDansFichier= queryfind.StartResearch(domainName);
						if(adresseIpDansFichier.isEmpty()){
							System.out.println("Ecriture Dans le fichier DNS");
							for (int i=0; i < listOfDomainIpForAquery.size(); i++){
								 
								 answerRecord.StartRecord(domainName, listOfDomainIpForAquery.get(i));
							 }
						}
						
					}
			}
//			serveur.close(); //closing server
		} catch (Exception e) {
			System.err.println("Probleme a l'execution :");
			e.printStackTrace(System.err);
		}
	}
}
