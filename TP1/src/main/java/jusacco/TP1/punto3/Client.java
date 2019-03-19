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
	Scanner sc;
	
	public Client (String serverIp, int serverPort) {
		try {
			sc = new Scanner(System.in);
			Socket s = new Socket (serverIp, serverPort);
			System.out.println("Cliente conectado al servidor en "+serverIp+":"+Integer.toString(serverPort));
			System.out.println("Configurando canales de I/O");
			inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
			outputChannel = new PrintWriter (s.getOutputStream(), true);
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
					case 1: enviarMensaje();break;
					case 2: recuperarMensaje();break;
					case 3:  salir = true; break;
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
		String msgFromServer = this.inputChannel.readLine();
		
	}

	private void enviarMensaje() throws IOException {
		outputChannel.println("enviar");
		//Ingrese el destino
		String msgFromServer = this.inputChannel.readLine();
		System.out.println("Server: "+msgFromServer);
		outputChannel.println(sc.nextLine());
		//Mensaje
		msgFromServer = this.inputChannel.readLine();
		System.out.println("Server: "+msgFromServer);
		outputChannel.println(sc.nextLine());
		//Todo salio bien?
		msgFromServer = this.inputChannel.readLine();
		System.out.println("Server: "+msgFromServer);
	}

	public static void main(String[] args){
		Client c = new Client("localhost",9000);
	}
}
