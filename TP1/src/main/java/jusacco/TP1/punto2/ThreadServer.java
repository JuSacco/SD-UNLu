package jusacco.TP1.punto2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadServer implements Runnable{
	Socket client;
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	
	public ThreadServer (Socket client) {
		this.client = client;
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void run() {
		System.out.println("Cliente conectado al servidor en "+client.getInetAddress()+":"+client.getPort());
		String msg;
		try {
			msg = this.inputChannel.readLine();
			System.out.println("Cliente: "+msg);
			msg+=" (devuelto por el server)";
			this.outputChannel.println(msg);
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				// TODO: handle exception
			}
			System.out.println("Cerrando la conexion con el cliente conectado desde puerto efimero: "+client.getPort());
			this.client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
