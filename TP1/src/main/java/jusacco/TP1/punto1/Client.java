package jusacco.TP1.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	public Client (String serverIp, int serverPort) {
		try {
			Socket s = new Socket (serverIp, serverPort);
			System.out.println("Cliente conectado al servidor en "+serverIp+":"+Integer.toString(serverPort));
			System.out.println("Configurando canales de I/O");
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (s.getOutputStream(), true);
			
			outputChannel.println("Este mensaje es enviado hacia el servidor");
			
			String msgFromServer= inputChannel.readLine();
			System.out.println("Server: "+msgFromServer);
			
			s.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Client c = new Client("localhost",9000);
	}
}
