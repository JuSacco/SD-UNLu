package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Master implements Runnable{
	private final Logger log = LoggerFactory.getLogger(Config.class);
	String ip;
	int port;
    String msg;
	Socket client;
	ArrayList<MasterIndex> peerData;
	ArrayList<String[]> connMasters;
    
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	
	
	public Master (String ip, int port, Socket client) {
		this.ip = ip;
		this.port = port;		
		this.client = client;
		this.peerData = new ArrayList<MasterIndex>();
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Master(String ip, int port, Socket client, ArrayList<MasterIndex> peerData, ArrayList<String[]> connMasters) {
		this.peerData = new ArrayList<MasterIndex>();
		this.connMasters = new ArrayList<String[]>();
		this.ip = ip;
		this.port = port;		
		this.client = client;
		this.peerData = peerData;
		this.connMasters = connMasters;
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void addPeer(String peer, ArrayList<Archivo> peerSharedData) {
		this.peerData.add(new MasterIndex(peer,peerSharedData));
	}


	public ArrayList<String> lookupMultipleArchivo(String name) {
		ArrayList<String> resultado = new ArrayList<String>();
		for (MasterIndex entry : this.peerData) {
			for(Archivo arch : entry.getLiArchivo()) {
				if(arch.getName().contains(name)) {
					resultado.add(entry.getOwner()+"//@t//"+arch.getName());
				}
			}
		}
		return resultado;
	}

	private void actualizar(String port) {
		try {
			String msg;
			ArrayList<Archivo> store = new ArrayList<Archivo>();
			boolean salir = false;
			String data = this.client.getInetAddress().getHostAddress()+":"+port;
			log.debug("Cliente: "+data);
			while(!salir){
				boolean existe = false;
				if((msg = inputChannel.readLine()) != null) {
					log.debug("Recibi (86)"+msg);
					if(!msg.contains(".END")) {
						for(MasterIndex m : this.peerData) {
							log.debug("Owner "+m.owner);
							log.debug("data "+data);
							if(m.liArchivo != null) {
								log.debug("liArchivo "+m.liArchivo.size());
								if(m.owner.contentEquals(data) && m.liArchivo.size()>0) {
									for(Archivo a : m.liArchivo) {
										existe = a.getName().contentEquals(msg);
									}									
								}
							}else {
								m.liArchivo = new ArrayList<Archivo>();
							}
						}
						if(!existe) {
							log.info("[MASTER-"+this.port+"] Guardando archivo: "+msg+" de "+data);
							store.add(new Archivo(msg));
						}
					}else {
						salir = true;
						addPeer(data,store);
						log.info("Terminé de ejecutar requestClientData. Servidor peer corriendo en: "+data);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void actualizarOtrosMaestros() {
		ArrayList<MasterIndex> toSend = null;
		Socket sock = null;
		if(!this.connMasters.isEmpty()) {
			for(String[] str : this.connMasters) {
				try {
					sock = new Socket(str[0],Integer.valueOf(str[1]));
					PrintWriter outChannel = new PrintWriter (sock.getOutputStream(), true);
					outChannel.println("actualizarMaster="+this.ip+":"+this.port);
					Thread.sleep(200);
					outChannel.println("sending");
					ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
					toSend = this.peerData;
					os.writeObject(toSend);	      
				} catch (Exception e) {
					log.error("Hubo un error al actualizar Master "+str[0]+":"+str[1]);
					log.error("Error: "+e.getMessage());
					sock = null;
				}
				
			}
		}
	}
	
	@Override
	public void run() {
		String msg = "";
		String[] msgParced;
		boolean end = false;
		try {
			while(!end && this.client.isConnected()) {
				msg = this.inputChannel.readLine();
				if(msg.isEmpty()) {
					log.info("Recibi null");
				}else {
					if(msg.split("=").length > 0) {
							log.info("[MASTER-"+this.port+"] Recibi:"+msg);
							msgParced = msg.split("=");
							switch (msgParced[0]) {
							case "buscar":
								if(msgParced.length >=1) {
									ArrayList<String> busqueda = lookupMultipleArchivo(msgParced[1]);
									this.outputChannel.flush();
									if (!(busqueda.isEmpty())) {
										for(String m : busqueda) {
											this.outputChannel.println(m);
										}
										this.outputChannel.println(".END");
									}else {
										this.outputChannel.println("no existe");
									}
									msgParced = null;
								}
								break;
							case "addPeer":
								ArrayList<Archivo> store = new ArrayList<Archivo>();
								boolean salir = false;
								String data = client.getLocalAddress()+":"+client.getPort();
								while(((msg = this.inputChannel.readLine()) != null)&&(!salir)){
									if(!msg.contentEquals(".END")) {
										store.add(new Archivo(msg));
									}else {
										salir = true;
										System.out.println("Data:"+data);
										addPeer(data,store);
										log.info("Terminé de ejecutar addPeer");
									}
								}
								
								break;
							case "cerrarConn":
								log.info("Cerrando conexion");
								String s = this.client.getLocalAddress()+":"+this.client.getPort();
								s = s.substring(1);
								for (MasterIndex entry : this.peerData) {
									if(entry.getOwner().contentEquals(s)) {
										this.peerData.remove(entry);
									}
								}
								log.info("Cliente"+client.toString()+" dado de baja.");
								log.debug("peerData despues de dar de baja:");
								for (MasterIndex entry : this.peerData) {
									log.debug("Owner:"+entry.owner);
									for (Archivo archivo : entry.liArchivo) {
										log.debug("Archivo: "+archivo.getName());
									}
								}
								this.peerData.contains(new MasterIndex(s, null));
								
								this.client.close();
								end = true;
								break;
							case "serverPortOn":
								actualizar(msgParced[1]);
								actualizarOtrosMaestros();
								break;
							case "actualizarMasterObj":
								//String parcedData[] = msgParced[1].split(":");
								//actualizarByObj(parcedData[0],parcedData[1]);
							default:
								break;
						}	
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
