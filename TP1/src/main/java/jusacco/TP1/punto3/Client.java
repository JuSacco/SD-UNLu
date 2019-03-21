package jusacco.TP1.punto3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	String user;
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	Socket s;
	
	public Client (String serverIp, int serverPort) {
		try {
			Scanner sc = new Scanner(System.in);
			this.s = new Socket (serverIp, serverPort);
			System.out.println("Cliente conectado al servidor en "+serverIp+":"+Integer.toString(serverPort));
			System.out.println("Configurando canales de I/O");
			this.inputChannel = new BufferedReader (new InputStreamReader (this.s.getInputStream()));
			this.outputChannel = new PrintWriter (this.s.getOutputStream(), true);
			
			System.out.println("Ingrese su usuario: ");
			this.user = sc.nextLine();
			outputChannel.println(this.user);
			int opt;
			boolean salir = false;
			while (!salir) {
				System.out.println("Ingrese que desea hacer: ");
				System.out.println("1- Enviar mensaje\n2- Ver casilla de mensajes\n3- Salir ");
				opt = sc.nextInt();
				switch(opt) {
					case 1: 
						enviarMensaje();
						break;
					case 2: 
						recuperarMensaje();
						break;
					case 3: 
						salir = true;
						outputChannel.println("enviar");
						break;
				}
			}
			s.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void recuperarMensaje() throws IOException {
		outputChannel.println("leer");
		String rta = "";
		String cadena = "";
		while(!rta.contentEquals(".END")) {
			rta = this.inputChannel.readLine();
			if(!rta.contentEquals(".END"))
				cadena += rta+"\n";
		}
		System.out.println(cadena);
	}

	private void enviarMensaje() throws IOException {
		String dest = "";
		String msg = "";
		String rta = "";
		Scanner sc = new Scanner(System.in);
		outputChannel.println("enviar");
		outputChannel.flush();
		//Ingrese el destino
		rta = this.inputChannel.readLine();
		System.out.println("Server: "+rta);
		dest = sc.nextLine();
		outputChannel.println(dest);
		outputChannel.flush();
		//Mensaje
		rta = this.inputChannel.readLine();
		System.out.println("Server: "+rta);
		msg = sc.nextLine();
		outputChannel.println(msg);
		outputChannel.flush();
		//Todo salio bien?
		rta = this.inputChannel.readLine();
		System.out.println("Server: "+rta);
	}

	public static void main(String[] args){
		Client c = new Client("localhost",9000);
	}
}
