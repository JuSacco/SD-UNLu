package jusacco.TP1.punto4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
	final String ARCHIVO = "c:/archivo1.txt";
	
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
	
	public void getMensajes(String from) throws IOException {
		String respuesta = "";
		String ack = "";
		ArrayList<Mensaje> li2 = new ArrayList<>();
		li2.addAll(liMensajes);
		Mensaje m = null;
		for (Mensaje mensaje : liMensajes) {
			if (mensaje.to.contentEquals(from)) {
				respuesta = mensaje.toString();
				this.outputChannel.println(respuesta);
				this.outputChannel.println("..END");
				ack = this.inputChannel.readLine();
				if (ack.contentEquals("recibido")) {
					m = mensaje;
					li2.remove(m);
				}
			}
		}
		if (respuesta.isEmpty()) {
			respuesta = "No tienes mensajes nuevos\n";
			this.outputChannel.print(respuesta);
		}
		this.outputChannel.println("termine");
		this.liMensajes = li2;
	}
	
	public void addMensaje(String from, String to, String msg){
		this.liMensajes.add(new Mensaje(from,to,msg));
		try {
			this.grabarCambios();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Mensaje splitMsg(String s) {
		String[] sv = s.split("-");
		Mensaje m = new Mensaje(sv[0],sv[1],sv[2]);
		return m;
	}
	
	public void grabarCambios() throws IOException {
		String s = "";
		File file = new File(ARCHIVO);
		FileWriter fw = new FileWriter(file, false);
		for (Mensaje mensaje : liMensajes) {
			s = mensaje.from+"-"+mensaje.to+"-"+mensaje.msg+"\r\n";
			fw.write(s);
		}
		fw.write(".END\r\n");
		fw.close();
	}
	
	public void levantarMensajes(){
		try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
			String s;
			while ((s = br.readLine()) != null) {
				if(!s.contentEquals(".END"))
					this.liMensajes.add(splitMsg(s));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void enviarMensaje(String us) {
		String dest = "";
		String msg = "";
		String user = us;
		try {
			this.outputChannel.println("Ingrese el destino:");
			this.outputChannel.flush();
			while(dest.equalsIgnoreCase("")) {
				dest = this.inputChannel.readLine();	
			}
			System.out.println("Destino: "+dest);
			this.outputChannel.println("Ingrese el mensaje a enviar:");
			this.outputChannel.flush();
			while(msg.equalsIgnoreCase("")) {
				msg = this.inputChannel.readLine();	
			}
			System.out.println("Mensaje: "+msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		addMensaje(user,dest,msg);
		this.outputChannel.println("\nMensaje enviado!");
	}
	
	public void run() {
		String msgCli;
		String user;
		boolean salir = false;
		try {
			this.liMensajes = new ArrayList<Mensaje>();
			System.out.println("Levantando mensajes...");
			levantarMensajes();
			user = this.inputChannel.readLine();
			System.out.println("Se conecto "+user+" desde "+client.getInetAddress()+":"+client.getPort());
			msgCli = this.inputChannel.readLine();
			while (!salir) {
				this.outputChannel.flush();
				switch(msgCli) {
					case "enviar": 
						enviarMensaje(user);
						msgCli = "";
						break;
					case "leer":
						this.getMensajes(user);
						this.grabarCambios();
						msgCli = "";
						break;
					case "salir":
						salir = true;
						this.client.close();
						break;
					case "":
						msgCli = this.inputChannel.readLine();
				}
			}
			System.out.println("Cerrando la conexion con el cliente conectado desde puerto efimero: "+client.getPort());
			
			this.client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
