package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerMaster {
		String ip;
		int port;
		ArrayList<String[]> otherMasters;
		ArrayList<String[]> connMasters;
		ArrayList<MasterIndex> peerData;
		private final Logger log = LoggerFactory.getLogger(Config.class);

		public ServerMaster (String ip,int port) {
			this.port = port;
			this.ip = ip;
			this.otherMasters = new ArrayList<String[]>();
			this.connMasters = new ArrayList<String[]>();
			this.peerData = new ArrayList<MasterIndex>();
			registrarConfig(this.ip,this.port);
			try {
				ServerSocket ss = new ServerSocket (this.port);
				boolean esta = false;
				log.info("[SERVER MASTER-"+port+"]: Listo para servir");
				for(String s[] : this.otherMasters) {
					if(s.length>0) {
						esta = available(s[0],Integer.parseInt(s[1]));
					}
					if (esta) {
						log.info("[SERVER MASTER-"+port+"]: El servidor "+s[0]+":"+s[1]+" esta ON. Registrandolo.");
						this.connMasters.add(s);
						esta = false;
					}else {
						log.info("[SERVER MASTER-"+port+"]: El servidor "+s[0]+":"+s[1]+" esta OFF.");
					}
				}
				int counter = 0;
				while (true) {
					Socket client = ss.accept();
					BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
					String msg = inputChannel.readLine();
					log.debug(msg);
					if(msg.contains("imMaster")) {
						String[] strPort = msg.split(":");
						log.info("[SERVER MASTER-"+port+"]: Me pingueo "+client.getInetAddress().getHostAddress()+" Corriendo en "+strPort[1]);
						log.info("[SERVER MASTER-"+port+"]: Registrando en Masters conectados");
						String newIp = client.getInetAddress().getHostAddress();
						String newPort = strPort[1];
						String [] newServer = {newIp,newPort};
						if(!newIp.isEmpty() && !newPort.isEmpty()) {
							newServer[0] = newIp;
							newServer[1] = newPort;
							this.connMasters.add(newServer);
							client.close();
						}
					}
					if(msg.contains("imClientPinging")){
						client.close();
					}
					if(msg.contains("actualizarMaster")) {
						String [] m = msg.split("=");
						m = m[1].split(":");
						actualizarByObj(m[0], m[1], client);
						client.close();
					}
					if(msg.contains("serverPortOn=")) {
						String[] strParced = msg.split("=");
						counter++;
						log.info("[SERVER MASTER-"+port+"]: Conexion con el cliente nro: "+counter);
						log.info("[SERVER MASTER-"+port+"]: Info Cliente: "+client.getInetAddress()+":"+client.getPort());
						//Pedir informacion y registrarlo.
						requestClientData(client,strParced[1]);
						String cli = client.getInetAddress().getHostName()+":"+strParced[1];
						this.peerData.add(new MasterIndex(cli, null));
						// THREAD
						Master m = new Master (ip,this.port,client,this.peerData,this.connMasters);
						Thread tMaster = new Thread (m);
						tMaster.start();	
					}
				}
			} catch (IOException e) {
				log.info("[SERVER MASTER-"+port+"]: Socket on port "+port+" is used ");
			}
		}

		public void registrarConfig(String ip, int port) {
			Config c = new Config("./config.txt");
			this.otherMasters = c.doConfig(ip, port);
		}
		public void addPeer(String peer, ArrayList<Archivo> peerSharedData) {
			boolean esta = false;
			for(MasterIndex m : this.peerData) {
				if(m.owner.contentEquals(peer)) {
					log.debug("Ya tenia agregado el peer. Agregando la lista.");
					m.liArchivo = peerSharedData;
					esta = true;
				}
			}
			if(!esta)
				this.peerData.add(new MasterIndex(peer,peerSharedData));
		}
		
		private boolean available(String ip,int port) {
		    try (Socket ignored = new Socket(ip, port)) {
				PrintWriter outputChannel = new PrintWriter (ignored.getOutputStream(), true);
				outputChannel.println("imMaster:"+this.port);
				ignored.close();
		        return true;
		    } catch (IOException ignored) {
		        return false;
		    }
		}

		private void requestClientData(Socket client, String peerPort) {
			try {				
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
				ArrayList<Archivo> store = new ArrayList<Archivo>();
				String msg;
				boolean salir = false;
				String data = client.getInetAddress().getHostName()+":"+peerPort;
				log.debug("[SERVER MASTER-"+port+"]: RequestClientData(): Info Cliente: "+data);
				while(!salir){
					if((msg = inputChannel.readLine()) != null) {
						if(!msg.contentEquals(".END")) {
							store.add(new Archivo(msg));
						}else {
							salir = true;
							addPeer(data,store);
							log.debug("[SERVER MASTER-"+port+"]: requestClientData(end). Info Sv Peer: "+data);
							log.debug("[SERVER MASTER-"+port+"]: Mis peer y sus archivos:");
							for (MasterIndex m : this.peerData) {
								log.debug("[SERVER MASTER-"+this.port+"]: Owner: "+m.owner);
								if(m.liArchivo != null) {
									for (Archivo ma : m.liArchivo) {
										log.debug("[SERVER MASTER-"+this.port+"]: \tArchivo: "+ma.getName());
									}
								}else {
									//Se borrara el reg? 
									m = null;
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void actualizarByObj(String ip, String port, Socket master) {
			try {
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (master.getInputStream()));
	//			PrintReader outputChannel = new PrintWriter (master.getOutputStream(), true);
				ArrayList<Archivo> store = new ArrayList<Archivo>();
				String data = master.getInetAddress().getHostName()+":";
				log.info("[SERVER MASTER-"+this.port+"]: RequestClientData(): Info Cliente: "+master.getInetAddress().getHostName()+":"+master.getPort());
				String msgFromServer;
				boolean salir = false;
				while(!salir) {
					msgFromServer = inputChannel.readLine();
					log.debug("msg: "+msgFromServer);
					if(msgFromServer.contains("sending")){
						try {
							ArrayList<MasterIndex> returnMessage = null;
							ObjectInputStream is = new ObjectInputStream(master.getInputStream());
							log.debug("[SERVER MASTER-"+this.port+"]: ObjectInputStream : "+is.toString());
							returnMessage = (ArrayList<MasterIndex>) is.readObject();
							
							log.info("[SERVER MASTER-"+this.port+"]: Recibiendo contenido actualizado");
							
							//DEBUG ANTES
							log.debug("[SERVER MASTER-"+this.port+"]: Mis archivos antes de actualizar:");
							for (MasterIndex m : this.peerData) {
								log.debug("[SERVER MASTER-"+this.port+"]: Owner: "+m.owner);
								for (Archivo ma : m.liArchivo) {
									log.debug("[SERVER MASTER-"+this.port+"]: \t Archivo: "+ma.getName());
								}
							}
							
							for(MasterIndex mi : returnMessage){
								for(MasterIndex myMi : this.peerData) {
									if(mi.owner.contentEquals(myMi.owner)) {
										myMi.liArchivo.removeAll(mi.liArchivo);
										myMi.liArchivo.addAll(mi.liArchivo);
									}
								}
							}
							this.peerData = returnMessage;
							
							//DEBUG DESPUES
							log.debug("[SERVER MASTER-"+this.port+"]: Mis archivos despues de actualizar:");
							for (MasterIndex m : this.peerData) {
								log.debug("[SERVER MASTER-"+this.port+"]: Owner: "+m.owner);
								for (Archivo ma : m.liArchivo) {
									log.debug("[SERVER MASTER-"+this.port+"]: \t Archivo: "+ma.getName());
								}
							}
							salir = true;
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
		}
		
		public static void main(int args) {
			new ServerMaster("127.0.0.1", args);
		}
		
}
