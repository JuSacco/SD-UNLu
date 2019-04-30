package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Scanner;



public class Master implements Runnable{
	private final Logger log = LoggerFactory.getLogger(Config.class);
	String ip;
	int port;
    String msg;
	Socket client;
	ArrayList<MasterIndex> peerData;
	ArrayList<String[]> otherMasters;
    
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	
	
	public Master (String ip, int port, Socket client) {
		this.ip = ip;
		this.port = port;		
		this.client = client;
		this.peerData = new ArrayList<MasterIndex>();
		//actualizarOtrosMaestros();
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Master(String ip, int port, Socket client, ArrayList<MasterIndex> peerData, ArrayList<String[]> otherMasters) {
		this.peerData = new ArrayList<MasterIndex>();
		this.otherMasters = new ArrayList<String[]>();
		this.ip = ip;
		this.port = port;		
		this.client = client;
		this.peerData = peerData;
		this.otherMasters = otherMasters;
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	
	/*
	 * Trying to deprecate this method, will be remplazed by receivePeerData()
	 */
	private void actualizar() {
		try {
			String msg;
			ArrayList<Archivo> store = new ArrayList<Archivo>();
			boolean salir = false;
			String data = this.client.getLocalAddress()+":";
			msg = this.inputChannel.readLine();
			for (String str : msg.split("//+//")) {
				log.info(str);
				store.add(new Archivo(str));
			}
			for(MasterIndex m : this.peerData) {
				log.info("Owner is: "+m.owner);
			}
			while(!salir){
				boolean existe = false;
				if((msg = inputChannel.readLine()) != null) {
					if(msg.contains("=")) {
						String[] msgParced = msg.split("=");
						if(msgParced[0].contentEquals("serverPortOn")) {
							data += msgParced[1];
						}
					}else {
						if(!msg.contentEquals(".END")) {
							for(MasterIndex m : this.peerData) {
								if(m.owner.contentEquals(data)) {
									for(Archivo a : m.liArchivo) {
										if(a.getName().contentEquals(msg)) {
											existe = true;
										}
									}									
								}
							}
							if(!existe) {
								log.info("-Master- Guardando archivo: "+msg);
								store.add(new Archivo(msg));
							}
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
	/*
	 * TODO: Search on peerData the owner and overwrite this liArchivo by store (new files)
	 */
	private void receivePeerData() {
		try {
			String msg;
			ArrayList<Archivo> store = new ArrayList<Archivo>();
			boolean salir = false;
			String data = this.client.getLocalAddress()+":";
			msg = this.inputChannel.readLine();
			for (String str : msg.split("//+//")) {
				log.info(str);
				store.add(new Archivo(str));
			}
			for(MasterIndex m : this.peerData) {
				log.info("Owner is: "+m.owner);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/*
	 * TODO: Test all the cases, first update work but next updates ??? 
	 */
	private void actualizarOtrosMaestros() {
		ArrayList<MasterIndex> toSend = null;
		Socket sock = null;
		if(!this.otherMasters.isEmpty()) {
			for(String[] str : this.otherMasters) {
				try {
					sock = new Socket(str[0],Integer.valueOf(str[1]));
					PrintWriter outChannel = new PrintWriter (sock.getOutputStream(), true);
					outChannel.println("actualizarMasterObj="+this.ip+":"+this.port);
					outChannel.println("sending");
			        outChannel.flush();
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
		String msg;
		String[] msgParced;
		boolean end = false;
		try {
			System.out.println("Debug: Mi cliente es: "+this.client);
			while(!end && this.client.isConnected()) {
				if ((msg = this.inputChannel.readLine()) != null) {
					log.info("-Master- Recibi:"+msg);
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
						String data = client.getLocalAddress()+":"+client.getLocalPort();
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
						String s = this.client.getLocalAddress()+":"+this.client.getLocalPort();
						s = s.substring(1);
						for (MasterIndex entry : this.peerData) {
							if(entry.getOwner().contentEquals(s)) {
								this.peerData.remove(entry);
							}
						}
						log.info("Cliente"+client.toString()+" dado de baja.");
						this.peerData.contains(new MasterIndex(s, null));
						this.client.close();
						end = true;
						break;
					case "actualizarMaster":
						actualizar();
						actualizarOtrosMaestros();
						break;
					case "peerData":
						receivePeerData();
					default:
						break;
					}
				}			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
