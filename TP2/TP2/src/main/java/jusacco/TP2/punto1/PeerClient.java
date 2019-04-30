package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerClient implements Runnable {
	String peerIp;
	int peerServerPort;
	String directory;
	String backUpMasterIp;
	int backUpMasterport;
	private final Logger log = LoggerFactory.getLogger(Config.class);
	Socket connMaestro;
	ArrayList<Archivo> liArchivos;
	private Scanner sc; 
	
	public PeerClient(Socket connMaestro, int peerServerPort, ArrayList<Archivo> liArchivos, String directory) {
		this.connMaestro = connMaestro;
		this.peerServerPort = peerServerPort;
		this.directory = directory;
		log.info("Cargando mis archivos para compartir...");
		this.liArchivos = liArchivos;
		givePeerData();
	}
	
	private void menuDescargar() {
		ArrayList<String> response;
		int index = 0;
		String read;
		boolean termino = false;
		System.out.println("Buscar: ");
		read = sc.nextLine();
		response = buscar(read);
		if (response != null) {
			String[] parced = null;
			System.out.println("Seleccione el nro indice de el que desea descargar o X para salir");
			for (String res : response) {
				System.out.println("res:"+res);
				parced = res.split("//@t//");
				System.out.println(index+". "+parced[1]+"(on "+parced[0]+")");
				index++;
				parced = null;
			}
			index=0;
			System.out.println("Ingrese el indice de el arhivo a descargar: ");
			read = sc.nextLine();
			while(!termino) {
				if(read.matches("\\d+")){
					if ((Integer.valueOf(read) >= 0)&&(Integer.valueOf(read) <= response.size())){
						termino = true;
						index = 0;
						String sel = response.get(Integer.valueOf(read));
						parced = sel.split("//@t//");
						Archivo a = descargar(parced[0].split(":"), parced[1]);	
						log.info("Prueba de que funciono: "+a.getName()+"\nContent: "+a.getContent());
						guardar(a);

					}
				
				}else {
					if(read.toLowerCase().contentEquals("x")) {
						termino = true;
					}else {
						System.out.println("Error!\nIngrese un numero valido o X para salir:");
						read = sc.nextLine();
					}
				}
			}
		}
	}
	private void menu() {
		sc = new Scanner(System.in);
		String opt;
		boolean salir = false;
		while (!salir) {
				System.out.println("========================\n"
						+ "1.Buscar archivo\n"
						+ "2.Ver mis archivos\n"
						+ "3.Agregar archivo\n"
						+ "\n"
						+ "4.Salir\n"
						+ "========================");
				opt = sc.nextLine();
				if(connMaestro.getInetAddress() != null) {
					switch (opt) {
						case "1": 
							menuDescargar();
							givePeerData();
							break;
						case "2":
								break;
						case "3":
								//addArchivo()
							break;
						case "4": 
							salir = true;
							try {
								PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
								outputChannel.println("cerrarConn");
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						
						break;
						default: System.out.println("Opcion invalida.");
					}
			}else {
			log.error("Fallo el servidor master.");
			try {
				finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		}
	}

	//TODO CADA VEZ QUE GUARDO UN ARCHIVO LO DEBO INFORMAR A EL NODO MAESTRO.
	
	private void guardar(Archivo a) {
		this.liArchivos.add(a);
		File file = new File(this.directory+"/"+a.name);
		if(!file.exists()) {
			try (FileOutputStream stream = new FileOutputStream(this.directory+"/"+a.name)) {
			    try {
					stream.write(a.getContent());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else {
			log.error("Ya existe un archivo con ese nombre. Renombrando...");
			try (FileOutputStream stream = new FileOutputStream(this.directory+"/"+"Copy "+a.name)) {
			    try {
					stream.write(a.getContent());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		PrintWriter outputChannel;
		try {
			outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
			outputChannel.println("actualizarMaster");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Archivo descargar(String[]connData,String archivo) {
		try {
			//String original "/127.0.0.1", lo convierto a "127.0.0.1"
			connData[0] = connData[0].substring(1);
			Socket s = new Socket (connData[0], Integer.valueOf(connData[1]));
			log.info("Estableciendo conexion con: "+connData[0]+":"+connData[1]);
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (s.getOutputStream(), true);

			log.info("Conexion establecida. Enviando peticion de descarga de: "+archivo);
			outputChannel.println("descargar="+archivo);
			String msgFromServer= inputChannel.readLine();
			if(msgFromServer.contains("error")) {
				log.info("Algo salió mal. No se pudo descargar");
			    s.close();
			}else if(msgFromServer.contains("sending")){
				try {
				    Archivo returnMessage = null;
					ObjectInputStream is = new ObjectInputStream(s.getInputStream());
					returnMessage = (Archivo) is.readObject();
				    log.info("Recibiendo contenido");
				    System.out.println("Archivo que descargue es:" + returnMessage.getName());
				    is.close();
				    return returnMessage;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}else {
				log.info("Algo salió realmente mal.");
			    s.close();
			}				
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<String> buscar(String query) {
		ArrayList<String> response = new ArrayList<String>();
		boolean termino = false;
		try {
			log.info("Cliente conectado al master.Buscando archivos...");
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.connMaestro.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);

			outputChannel.println("buscar="+query);
			String msgFromServer = null;
			while(!termino) {
				if((msgFromServer = inputChannel.readLine()) != null && !msgFromServer.contentEquals(".END")) {
					if (msgFromServer.contentEquals("no existe") || response == null) {
						log.info("La busqueda '"+query+"' no produjo resultados");
						termino = true;
						return null;
					}else {
						if(msgFromServer.contains("//@t//"))
						response.add(msgFromServer);
						log.info("Server responde: "+msgFromServer);
					}
				}else {
					termino = true;
					return response;
				}
			}				
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	private void givePeerData() {
		try {
			PrintWriter outputChannel = new PrintWriter (this.connMaestro.getOutputStream(), true);
			String msg =""; 
			log.info("Cliente conectado al master("+this.connMaestro+"). Enviando archivos disponibles");
			if(!this.liArchivos.isEmpty()){
				outputChannel.println("peerDataOn="+this.peerServerPort);
				for (Archivo archivo : this.liArchivos) {
					msg = msg+archivo.getName()+"//+//";
				}
				outputChannel.println(msg);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		menu();
	}
	
}
