package jusacco.TP2.punto3;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/*
 * @Propose: Balanceador tipo proxy, se asigna las tareas a los nodos que menos clientes estan atendiendo
 * @Authors: vFacundo, jusacco
 */
public class Balanceador{
	
	static ArrayList<String> listaNodos;
	static HashMap<String, Integer> nodosCarga = new LinkedHashMap<String, Integer>();
	static Logger log = LoggerFactory.getLogger(Balanceador.class);
	/*
	 * Constantes:
	 * 	SIN_CARGA = 0 No esta atendiendo a ningun cliente
		NORMAL = 3 Entre 0 y 3 esta atendiendo clientes de manera normal
		ALERTA = 4 Cuando llega a 4 clientes entra en estado de alerta
		CRITICO = 5 Estado critico 5 clientes. No se aceptan mas conexiones
		PUERTO_LOCAL = 9000 Puerto donde corre el balanceador
	*/
	static final int SIN_CARGA = 0;
	static final int NORMAL = 3;
	static final int ALERTA = 4;
	static final int CRITICO = 5;
	static final int PUERTO_LOCAL = 9000;
	
	/*
	 * @Propose: Ordeno la lista de carga de menor a mayor..
	 */
	public static HashMap<String, Integer> sortByValue(Map<String, Integer> map) {
	    List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
	    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
	        public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
	            return (m1.getValue()).compareTo(m2.getValue());
	        }
	    });
	    HashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
	    for (Map.Entry<String, Integer> entry : list) {
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	}
	
	/*
	 * @Propose: Leo el archivo de config del disco y lo guardo en una lista
	 */
	@SuppressWarnings("unchecked")
	public static int cargarNodos() {
		Gson gson = new Gson();
		HashMap<String, ArrayList<String>> config;
		try {
			config = gson.fromJson(new FileReader("nodosDinamicos.json"), HashMap.class);
			listaNodos = config.get("nodos");

		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			log.info("Error Archivo Config!");
		} 
		return listaNodos.size();
	}

	/*
	 * @Propose: Crear nuevo nodo, thread
	 */
	public static boolean nuevoNodo() {
		boolean resultado = false;
		String direccion;
		if(nodosCarga.size()<listaNodos.size()) {
			synchronized (listaNodos) {
				direccion = listaNodos.get(nodosCarga.size());
			}
			int puerto = partirDireccion(direccion);
			NodoService n1 = new NodoService(puerto);
			Thread thN1 = new Thread(n1);
			thN1.start();
			nodosCarga.put(direccion,SIN_CARGA);//Cada vez que creo un nodo lo pongo en la lista
			MDC.put("log.name",Balanceador.class.getSimpleName().toString());
			log.info("Cree nuevo NODO"+" port: "+puerto+" ,listaNODOS: "+nodosCarga);
			resultado = true;
		}
		return resultado;
	}

	/*
	 * @Propose: Recibe IP:Puerto, return solo puerto
	 */
	public static int partirDireccion(String direccion) {
		String[] parts = direccion.split(":");
		int result = Integer.parseInt(parts[1]);
		return result;
	}
	
	/*
	 * @Propose: detener un nodo.
	 */
	public static void detenerNodo(String direccion) {
		String[] parts = direccion.split(":");
		try {
			Registry registro = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
			IControl controlNodo = (IControl) registro.lookup("Control");
			controlNodo.detenerSv();
			synchronized (nodosCarga) {
				nodosCarga.remove(direccion);				
			}
		} catch (NumberFormatException | RemoteException | NotBoundException e) {
			log.info("No fue posible detener el nodo!: "+direccion);
		}
	}
	
	/*
	 * @Propose: Asignar una tarea.
	 * @Steps: Ordeno nodosCarga. Calculo su carga. Asigno tarea
	 * @Comments: String direccion: Primer Direccion, es decir, nodo con menos carga.
	 */	
	public synchronized static String asignarTarea() {
		nodosCarga = sortByValue(nodosCarga);
		Map.Entry<String, Integer>entry = nodosCarga.entrySet().iterator().next();
		String direccion = entry.getKey();
		int tareas = nodosCarga.get(direccion);
		if(tareas<=NORMAL) {
			tareas = nodosCarga.get(direccion);//Sumo 1 tarea
			tareas++;
			nodosCarga.put(direccion, tareas);
			log.info("Le asigne una tarea al NODO: "+direccion+" esta realizando: "+tareas+" tareas!");
		}else if((tareas>=ALERTA) && (tareas<=CRITICO)) {
			boolean crearNodo = nuevoNodo();
			if(crearNodo) {
				direccion = asignarTarea();
			}else if(tareas<CRITICO){
				tareas++;
				nodosCarga.put(direccion, tareas);
				log.info("Le asigne una tarea al NODO: "+direccion+" esta realizando: "+tareas+" tareas! ESTADO CRITICO");
			}else {
				direccion = "127.0.0.1:"+PUERTO_LOCAL;
			}
		}
		return direccion;
	}
	
	/*
	 * @Propose: Terminar una tarea que fue asignada.
	 */	
	public synchronized static void terminarTarea(String direccion) {
		int tareas;
		synchronized(nodosCarga) {
			tareas = nodosCarga.get(direccion);
		}
		tareas--;
		nodosCarga.put(direccion, tareas);
		log.info("Tarea FINALIZADA nodo: "+direccion+" ,TAREAS RESTANTES: "+tareas);
		if((tareas==0)&&(nodosCarga.size()>2)) {//Si el nodo no tiene tareas y tengo mas de 2 nodos lo detengo
			detenerNodo(direccion);
		}
	}
	
	/*
	 * @Propose: Atender un cliente.
	 * @Steps: Obtener direccion del nodo. Si la red esta completa -> Espero hasta que se desocupe un nodo.
	 */	
	public static String atenderCliente() {
		String direccion = null;
		try {
			direccion = asignarTarea();
			int port = partirDireccion(direccion);
			if(port == PUERTO_LOCAL)
				log.info("RED COMPLETA! - CLIENTE en Lista de Espera");
			while (port==PUERTO_LOCAL) {
				Thread.sleep(5000);
				direccion = asignarTarea();
				port = partirDireccion(direccion);
			}
		} catch (InterruptedException e) {
			log.info(e.getMessage());
		}
		return direccion;
	}

	
	
	public static void main() throws RemoteException, NotBoundException{

		Remote remote = UnicastRemoteObject.exportObject(new ITarea() {
			int cant = 0;
			@Override
			public int[] restar(int[] v1, int[] v2) {
				Registry registry;
				int[] resultado = null;
				try {
					String direccion = atenderCliente();
					String[] parts = direccion.split(":");
					registry = LocateRegistry.getRegistry(parts[0],partirDireccion(direccion));
					ITarea nodoStub = (ITarea) registry.lookup("Tarea");
					resultado = nodoStub.restar(v1, v2);
					terminarTarea(direccion);
				} catch (NotBoundException | RemoteException e) {
					log.info(e.getMessage());
				}
				return resultado;
			}

			@Override
			public int[] sumar(int[] v1, int[] v2) throws RemoteException {
				Registry registry;
				int[] resultado = null;
				try {
					String direccion = atenderCliente();
					String[] parts = direccion.split(":");
					registry = LocateRegistry.getRegistry(parts[0],partirDireccion(direccion));
					ITarea nodoStub = (ITarea) registry.lookup("Tarea");
					resultado = nodoStub.sumar(v1, v2);
					terminarTarea(direccion);
				} catch (NotBoundException | RemoteException e) {
					log.info(e.getMessage());
				}
				return resultado;
			}
			
		},0);
		
		if(cargarNodos()>=2) {
			nuevoNodo();
			nuevoNodo();
			Registry registry = LocateRegistry.createRegistry(PUERTO_LOCAL);
			registry.rebind("Tarea", remote);
			log.info("Balanceador RMI Iniciado!");
		};
	}
}
