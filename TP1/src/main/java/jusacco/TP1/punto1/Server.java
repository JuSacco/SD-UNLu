package jusacco.TP1.punto1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
		int port;
	    ServerSocket ss;
	    Socket cs;
	    String msg;
		BufferedReader inputChannel;
		PrintWriter outputChannel;

	    public Server(int port){
			this.port = port;
			try {
				this.ss = new ServerSocket (this.port);
				System.out.println("Servidor corriendo en el puerto: "+port);
				int counter = 0;
				while (true) {
					this.cs = ss.accept();
					counter++;
					System.out.println("Cliente Nro:"+counter);
					this.inputChannel = new BufferedReader(new InputStreamReader(cs.getInputStream()));
					this.outputChannel = new PrintWriter (this.cs.getOutputStream(), true);
					this.msg = this.inputChannel.readLine();
					System.out.println("Cliente: "+this.msg);
					this.msg+=" (respuesta del servidor)";
					this.outputChannel.println(msg);
				}
			}catch (IOException e) {
				System.out.println("Socket en el puerto "+port+" esta siendo usado");
			}
	    }
	    
		public static void main(String[] args){
			Server s = new Server(9000);
		}
}
