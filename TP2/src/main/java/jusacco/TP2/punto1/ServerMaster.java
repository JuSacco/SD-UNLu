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

import ch.qos.logback.core.net.server.Client;

public class ServerMaster {
		String ip;
		int port;
		ArrayList<String[]> otherMasters;
		ArrayList<MasterIndex> peerData;
		private final Logger log = LoggerFactory.getLogger(Config.class);

		public ServerMaster (String ip,int port) {
			this.port = port;
			this.ip = ip;
			this.otherMasters = new ArrayList<String[]>();
			this.peerData = new ArrayList<MasterIndex>();
			registrarConfig(this.ip,this.port);
			try {
				ServerSocket ss = new ServerSocket (this.port);
				log.info("Server master en puerto: "+port);
				log.info("Server master ("+port+") conoce a:");
				for(String s[] : this.otherMasters) {
					log.info(s[0]+":"+s[1]);
				}
				int counter = 0;
				while (true) {
					Socket client = ss.accept();
					counter++;
					log.info("Conexion con el cliente nro: "+counter);
					log.info("Mi cliente es: "+client.getInetAddress()+":"+client.getLocalPort());
					//Pedir informacion y registrarlo.
					requestClientData(client);
					// THREAD
					Master m = new Master (ip,this.port,client,this.peerData,this.otherMasters);
					Thread tMaster = new Thread (m);
					tMaster.start();
				}
			} catch (IOException e) {
				System.out.println(" Socket on port "+port+" is used ");
			}
		}

		public void registrarConfig(String ip, int port) {
			Config c = new Config("./config.txt");
			this.otherMasters = c.doConfig(ip, port);
		}
		public void addPeer(String peer, ArrayList<Archivo> peerSharedData) {
			this.peerData.add(new MasterIndex(peer,peerSharedData));
		}
	/*	
		private void actualizarByObj(String ip, String port) {
			try {
				log.info("Actualizar Lista: Estableciendo conexion con: "+ip+":"+port);
				Socket s = new Socket (ip, Integer.valueOf(port));
				ArrayList<MasterIndex> returnMessage = null;
				log.info("Espero mensaje de: "+s.getInetAddress()+":"+s.getPort()+"...");
				ObjectInputStream is = new ObjectInputStream(s.getInputStream());
				log.info("ObjectInputStream : "+is.toString());
				while ((returnMessage = (ArrayList<MasterIndex>) is.readObject()) != null);
				log.info("Recibiendo contenido actualizado");
				this.peerData = returnMessage;
				log.info("Mis nuevos archivos son:");
				for (MasterIndex m : returnMessage) {
					log.info("Owner: "+m.owner);
					for (Archivo ma : m.liArchivo) {
						log.info("Archivo: "+ma.getName());
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
		*/
		private void requestClientData(Socket client) {
			try {
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
				String msg;
				ArrayList<Archivo> store = new ArrayList<Archivo>();
				boolean salir = false;
				String data = client.getLocalAddress()+":";
				while(!salir){
					if((msg = inputChannel.readLine()) != null) {
						if(msg.contains("=")) {
							String[] msgParced = msg.split("=");
							if(msgParced[0].contentEquals("serverPortOn")) {
								data += msgParced[1];
							}
							if(msgParced[0].contains("actualizarMasterObj")) {
								log.info("Recibi: "+msg+" de "+client.getInetAddress()+":"+client.getLocalPort());
								String parcedData[] = msgParced[1].split(":");
								//actualizarByObj(parcedData[0],parcedData[1]);
								String msgFromServer= inputChannel.readLine();
								if(msgFromServer.contains("error")) {
									log.info("Algo salió mal. No se pudo descargar");
								    client.close();
								}
								if(msgFromServer.contains("sending")){
									try {
										ArrayList<MasterIndex> returnMessage = null;
										ObjectInputStream is = new ObjectInputStream(client.getInputStream());
										log.info("ObjectInputStream : "+is.toString());
										returnMessage = (ArrayList<MasterIndex>) is.readObject();
										log.info("Recibiendo contenido actualizado");
										this.peerData = returnMessage;
										log.info("Mis nuevos archivos son:");
										for (MasterIndex m : returnMessage) {
											log.info("Owner: "+m.owner);
											for (Archivo ma : m.liArchivo) {
												log.info("Archivo: "+ma.getName());
											}
										}
									} catch (ClassNotFoundException e) {
										e.printStackTrace();
									} catch (NumberFormatException | IOException e) {
										e.printStackTrace();
									}
								}
							}
						}else {
							if(!msg.contentEquals(".END")) {
								store.add(new Archivo(msg));
							}else {
								salir = true;
								System.out.println("Data:"+data);
								addPeer(data,store);
								log.info("Terminé de ejecutar requestClientData. Servidor peer corriendo en: "+data);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public static void main(int args) {
			new ServerMaster("localhost", args);
		}
		
}
