package jusacco.TP1.punto3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ThreadServer implements Runnable{
	Socket client;
	BufferedReader inputChannel;
	PrintWriter outputChannel;
	ArrayList<Mensaje> liMensajes;
	
	public ThreadServer (Socket client) {
		this.client = client;
		try {
			this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getMensajes() {
		String respuesta = "";
		for (Mensaje mensaje : liMensajes) {
			respuesta += mensaje.toString()+"\n----\n";
		}
		return respuesta;
	}
	
	public String getMensajes(String from) {
		String respuesta = "";
		for (Mensaje mensaje : liMensajes) {
			if (mensaje.to == from)
				respuesta += mensaje.toString()+"\n----\n";
		}
		return respuesta;
	}
	
	public void addMensaje(String from, String to, String msg){
		liMensajes.add(new Mensaje(from,to,msg));
	}
	
	public void run() {
		String msgCli;
		String user;
		String dest;
		String msg;
		try {
			user = this.inputChannel.readLine();
			System.out.println("Se conecto "+user+" desde "+client.getInetAddress()+":"+client.getPort());
			msgCli = this.inputChannel.readLine();
			switch(msgCli) {
				case "enviar": 
					this.outputChannel.println("\nIngrese el destino:");
					dest = this.inputChannel.readLine();
					System.out.println("exploto");
					this.outputChannel.println("\nIngrese el mensaje a enviar:");
					msg = this.inputChannel.readLine();
					addMensaje(user,dest,msg);
					this.outputChannel.println("\nMensaje enviado!");
					break;
				case "leer":
					this.outputChannel.println(getMensajes(user));
					break;
			}
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
