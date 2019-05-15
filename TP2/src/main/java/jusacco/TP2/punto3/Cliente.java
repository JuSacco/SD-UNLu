package jusacco.TP2.punto3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Cliente {
	Logger log = LoggerFactory.getLogger(Cliente.class);
	String ip;
	String clientName;
	int port;
	Scanner sc = new Scanner(System.in);
	public Cliente(String ip, int port,String name) {
		this.ip= ip;
		this.port = port;
		this.clientName = name;
		this.startClient();
	}
	private void startClient() {
		try {
			Registry clientRMI = LocateRegistry.getRegistry(this.ip, this.port);
			
			System.out.println("Lista de servicios disponibles: ");
			String[] services = clientRMI.list();
			int i = 0;
			for (String service : services) {
				System.out.println(i+"."+service);
				i++;
			}
			IClientServices clientStub = (IClientServices) clientRMI.lookup(services[0]);
			System.out.println(clientStub.realizarTarea(this.clientName));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	public static void main(String[] args) {
		int thread = (int) Thread.currentThread().getId();
		String logname = Cliente.class.getSimpleName().toString()+"-"+thread;
		System.setProperty("log.name",logname);
		new Cliente (args[0],Integer.valueOf(args[1]),args[2]);
	}

}