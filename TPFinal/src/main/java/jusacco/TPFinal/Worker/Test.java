package jusacco.TPFinal.Worker;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

import jusacco.TPFinal.Servidor.Mensaje;

public class Test {

	private ConnectionFactory queueConnectionFactory;
	private Connection queueConnection;
	private Channel queueChannel;
	String queueUser = "admin";
	String queuePwd = "admin";
	String queueIp = "localhost";
	private String queuePrueba = "prueba";
	
	public Test() {

    	//QUEUE RELATED
    			try {
    				// [STEP 0] - FACTORIA DE CONEXION
    				this.queueConnectionFactory = new ConnectionFactory();
    				this.queueConnectionFactory.setHost(this.queueIp);
    				this.queueConnectionFactory.setPort(5672);
    				this.queueConnectionFactory.setUsername(this.queueUser);
    				this.queueConnectionFactory.setPassword(this.queuePwd);
    				// [STEP 1] - QueueConnection
    				this.queueConnection = this.queueConnectionFactory.newConnection();
    				// [STEP 2] - ChannelConnection
    				this.queueChannel = this.queueConnection.createChannel();
    				// [STEP 3] - Create the queues
				    this.queueChannel.queueDelete(this.queuePrueba);
    				this.queueChannel.queueDeclare(this.queuePrueba, true, false, false, null);
    			} catch (IOException e) {
    				e.printStackTrace();
    			} catch (TimeoutException e) {
    			}

    	try {
			String example = "1";
			byte[] bytes = example.getBytes();
			this.queueChannel.basicPublish("", this.queuePrueba, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);

			example = "2";
			bytes = example.getBytes();
			this.queueChannel.basicPublish("", this.queuePrueba, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);

			example = "3";
			bytes = example.getBytes();
			this.queueChannel.basicPublish("", this.queuePrueba, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
			
			example = "4";
			bytes = example.getBytes();
			this.queueChannel.basicPublish("", this.queuePrueba, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
			
			example = "5";
			bytes = example.getBytes();
			this.queueChannel.basicPublish("", this.queuePrueba, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    		
	}
	
	@SuppressWarnings("unchecked")
	public void consumidor() {
		boolean salir = false;
		Map<String,Long> trabajosCola = new HashMap<String,Long>();
		try {
			while(!salir) {
				Thread.sleep(200);
				GetResponse gr;
				String trabajo = "4";
				System.out.println("La cola tiene: "+this.queueChannel.messageCount(this.queuePrueba)+" mensajes");
				while((gr = this.queueChannel.basicGet(this.queuePrueba, false)) == null){Thread.sleep(100);}
				String msg = new String(gr.getBody());
				System.out.println("msg: "+msg+"  |  trabajo: "+trabajo );//TODO
				trabajosCola.put(msg, (Long) gr.getEnvelope().getDeliveryTag());
				if(msg.equals(trabajo)) {
					this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), true, true);
					Thread.sleep(200);
					System.out.println("Encontre el trabajo.Dando NACK. La cola tiene ahora: "+this.queueChannel.messageCount(this.queuePrueba)+" mensajes");
					salir = true;
				}else {
					//consumidor();
				}
			}
			for (String key: trabajosCola.keySet()) {
				System.out.println("Key:"+key+" DeliveryTag:"+trabajosCola.get(key));
    		}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String obtenerMensaje(String trabajo) {
		try {
			Thread.sleep(50);
			GetResponse gr;
			System.out.println("La cola tiene: "+this.queueChannel.messageCount(this.queuePrueba)+" mensajes");
			while((gr = this.queueChannel.basicGet(this.queuePrueba, false)) == null){Thread.sleep(100);}
			String msg = new String(gr.getBody());
			System.out.println("Mensaje: "+msg+"  |  trabajo: "+trabajo );
			if(msg.equals(trabajo)) {
				this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), true, true);
				Thread.sleep(50);
				System.out.println("Encontre el trabajo. Dando NACK. La cola tiene ahora: "+this.queueChannel.messageCount(this.queuePrueba)+" mensajes");
				return msg;
			}else {
				return obtenerMensaje(trabajo);
			}
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
    public static void main(String... args) {
    	
    	Test t = new Test();
    	t.obtenerMensaje("3");
    	
/*
    	ArrayList<String> listaWorkers = new ArrayList<String>();
    	ArrayList<String> realizedWorks = new ArrayList<String>();
    	ArrayList<String> listaTrabajos = new ArrayList<String>();
    	
    	listaWorkers.add("worker1");
    	
    	realizedWorks.add("lamparafocovidrio.blend:192.168.0.108");
    	realizedWorks.add("lamparafocovidrio.blend:192.168.0.110");
        
        listaTrabajos.add("lamparafocovidrio.blend:192.168.0.110");

        
        //System.out.println("Result is null:"+giveWorkToDo(listaWorkers.get(0),realizedWorks,listaTrabajos));
 * 
 * 
 */
    	
    }

	public String giveWorkToDo(String worker, ArrayList<String> realizedWorks, ArrayList<String> listaTrabajos, ArrayList<String> listaWorkers) throws RemoteException {
		String result = null;
		boolean salir = false;
		Set<String> a = new HashSet<>(listaTrabajos);
	    Set<String> b = new HashSet<>(listaWorkers);
		while(!salir) {
		    for (String el: a) {
		      if (!b.contains(el)) {
		    	 result = el;
		         break;
		      }
		    }
		    if(result != null) {
		    	salir = true;
		    }else {
		    	a = new HashSet<>(listaTrabajos); //update
		    }
		}
		return result;
	}

}
