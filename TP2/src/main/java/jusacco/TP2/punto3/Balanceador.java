package jusacco.TP2.punto3;

import java.rmi.NotBoundException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/*	Carga:
 * 		Vacio = 0 trabajos.
 * 		Nnormal = 1-3 trabajos.
 * 		Alerta = 3-6 trabajos.
 * 		Critico = 6-8 trabajos. 
 */



public class Balanceador implements IClientServices{
	private String ip;
	private int port;
	private Map<String, ArrayList<String>> estadoNodos;
	private ArrayList<String[]> activeServer;
	private final Logger log = LoggerFactory.getLogger(Balanceador.class);
	private Registry serverRMI;
	private ArrayList<Registry> services;
	
	private Balanceador(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.services = new ArrayList<Registry>();
		this.estadoNodos = new HashMap<String, ArrayList<String>>();
		MDC.put("log.name", Balanceador.class.getSimpleName().toString()+"-"+this.port);
		init();
		createRMI();
		loadServices();
	}
	
	
	private void createRMI() {
		try {
			this.serverRMI = LocateRegistry.createRegistry(9000);
			System.out.println("Servidor RMI levantado en el puerto 9000");
			
			IClientServices serverStub = (IClientServices) UnicastRemoteObject.exportObject(this, 8000);
			System.out.println("Objetos correctamente publicados a traves de IRemota");
			
			this.serverRMI.rebind("realizarTarea", serverStub);
			System.out.println("Se ha bindeado el nombre con el servicio");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void loadServices() {
		try {
			for(String[] str : this.activeServer) {
				this.services.add(LocateRegistry.getRegistry(str[0],Integer.valueOf(str[1])));
			}
			System.out.println("Lista de servicios disponibles: ");
			int i = 0;
			for(Registry r : this.services) {
				String[] services = r.list();
				for (String service : services) {
					System.out.println(i+"."+service);
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void init() {
		this.estadoNodos.put("vacio", new ArrayList<String>());
		this.estadoNodos.put("normal", new ArrayList<String>());
		this.estadoNodos.put("alerta", new ArrayList<String>());
		this.estadoNodos.put("critico", new ArrayList<String>());
		this.activeServer = new ArrayList<String[]>();
		firstRun();
	}
	
	//De entrada levanto 2 nodos
	private void firstRun() {
		String ipPort = "localhost:6000";
		ArrayList<String> value = new ArrayList<String>();
		NodoServicio ns = new NodoServicio(ipPort.split(":")[0],Integer.valueOf(ipPort.split(":")[1]));
		Thread nsThread = new Thread(ns);
		value = this.estadoNodos.get("vacio");
		value.add(ipPort);
		this.activeServer.add(ipPort.split(":"));
		

		ipPort = "localhost:7000";
		NodoServicio ns1 = new NodoServicio(ipPort.split(":")[0],Integer.valueOf(ipPort.split(":")[1]));
		Thread nsThread1 = new Thread(ns1);
		value.add(ipPort);
		this.estadoNodos.put("vacio", value);
		this.activeServer.add(ipPort.split(":"));
		
		nsThread.run();
		nsThread1.run();
	}


	@Override
	// TODO Debo checkear los nodos; Asignarle la tarea; y actualizar estadoNodos + checkear si debo crear un nuevo nodo.
	public String realizarTarea(String name) throws RemoteException {
		log.info("[Balanceador]: Recibi una peticion de realizarTarea. Asignando nodo");
		ILoadBalancerServices nodoAsignado = asignarNodo();
		
		//TODO asignar trabajo al nodo
		if(nodoAsignado != null) {
			
			String resultado = nodoAsignado.realizarTarea(name);
			//TODO Sacar trabajo a el nodo
			updEstadoNodos();
			showEstadoNodos();
			return resultado;
		}else {
			return "";
		}
	}

	private ILoadBalancerServices getInterfaceByIpPort(String ipPort) {
		for(Registry r : this.services) {
			try {
				if(r.list()[0].equals(ipPort)) {
					return (ILoadBalancerServices) r.lookup(r.list()[0]);
				}
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void updEstadoNodos() {
		ArrayList<String> vacios = new ArrayList<String>();
		ArrayList<String> normal = new ArrayList<String>();
		ArrayList<String> alerta = new ArrayList<String>();
		ArrayList<String> critico = new ArrayList<String>();
		
		try {
			for(String[] svActivos : this.activeServer) {
				ILoadBalancerServices aux = getInterfaceByIpPort(svActivos[0]+":"+svActivos[1]);
				if(aux != null) {
					int activeClients = aux.getActiveClients();
					if(activeClients < 1) {
						log.info("[Balanceador]: "+svActivos[0]+":"+svActivos[1]+" Atendiendo "+activeClients+" Clientes");
						vacios.add(svActivos[0]+":"+svActivos[1]);
					}
					if(activeClients >= 1 && activeClients <= 3) {
						log.info("[Balanceador]: "+svActivos[0]+":"+svActivos[1]+" Atendiendo "+activeClients+" Clientes");
						normal.add(svActivos[0]+":"+svActivos[1]);
					}	
					if(activeClients >= 3 && activeClients <= 6) {
						log.info("[Balanceador]: "+svActivos[0]+":"+svActivos[1]+" Atendiendo "+activeClients+" Clientes");
						alerta.add(svActivos[0]+":"+svActivos[1]);
					}
					if(activeClients >= 6 && activeClients <= 8) {
						log.info("[Balanceador]: "+svActivos[0]+":"+svActivos[1]+" Atendiendo "+activeClients+" Clientes");
						critico.add(svActivos[0]+":"+svActivos[1]);
					}
				}
			}
			this.estadoNodos.put("vacios",vacios);
			this.estadoNodos.put("normal",normal);
			this.estadoNodos.put("alerta",alerta);
			this.estadoNodos.put("critico",critico);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void showEstadoNodos() {
		log.info("[Balanceador]Estado nodos: Vacios: ");
		for(String str : this.estadoNodos.get("vacio")) {
			log.info("\t"+str);
		}
		log.info("[Balanceador]Estado nodos: Normal: ");
		for(String str : this.estadoNodos.get("normal")) {
			log.info("\t"+str);
		}
		log.info("[Balanceador]Estado nodos: Alerta: ");
		for(String str : this.estadoNodos.get("alerta")) {
			log.info("\t"+str);
		}
		log.info("[Balanceador]Estado nodos: Critico: ");
		for(String str : this.estadoNodos.get("critico")) {
			log.info("\t"+str);
		}
	}
	
	
	private ILoadBalancerServices asignarNodo() {
		updEstadoNodos();
		
		if(this.estadoNodos.get("vacios").size()>0){
			ILoadBalancerServices cli = getInterfaceByIpPort(this.estadoNodos.get("vacios").get(0));
			return cli;
		}
		if(this.estadoNodos.get("normal").size()>0){
			ILoadBalancerServices cli = getInterfaceByIpPort(this.estadoNodos.get("normal").get(0));
			return cli;
		}
		if(this.estadoNodos.get("alerta").size()>0){
			ILoadBalancerServices cli = getInterfaceByIpPort(this.estadoNodos.get("alerta").get(0));
			return cli;
		}
		if(this.estadoNodos.get("critico").size()>0){
			ILoadBalancerServices cli = getInterfaceByIpPort(this.estadoNodos.get("critico").get(0));
			return cli;
		}
		return null;
	}
	
	

	public static void main(String ip,int port) {
		new Balanceador(ip,port);
	}


}
