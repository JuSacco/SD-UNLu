package jusacco.TPFinal.Servidor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

import jusacco.TPFinal.Cliente.Imagen;
import jusacco.TPFinal.Servidor.Tools.ImageStacker;

public class ThreadServer implements Runnable {
	private final int TIME_OUT = 100;
	Mensaje msg;
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	Map<String, LocalTime> workersLastPing;
	Logger log = LoggerFactory.getLogger(ThreadServer.class);
	Channel queueChannel;
	Connection queueConnection;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	volatile BufferedImage respuesta;
	
	public ThreadServer(Mensaje msg, ArrayList<String> listaWorkers, BufferedImage respuesta, Channel queueChannel, Connection queueConnection, ArrayList<String> listaTrabajos, Map<String, LocalTime> workersLastPing) {
		this.msg = msg;
		this.listaWorkers = listaWorkers;
		this.respuesta = respuesta;
		this.queueChannel = queueChannel;
		this.queueConnection = queueConnection;
		this.listaTrabajos = listaTrabajos;
		this.workersLastPing = workersLastPing;
	}
	
	public BufferedImage getRespuesta() {
		return this.respuesta;
	}
	
	public ArrayList<String> diferenciaListas(ArrayList<String> a, ArrayList<String> b) {
		Set<String> setA = new HashSet<>(a);
	    Set<String> setB = new HashSet<>(b);
		ArrayList<String> result = new ArrayList<String>();
	    for (String el: setA) {
	      if (!setB.contains(el)) {
	    	 result.add(el);
	      }
	    }
	    return result;
	}
	
	private Mensaje obtenerMensaje(Mensaje msgCli) {
		try {
			GetResponse gr;
			while((gr = this.queueChannel.basicGet(this.queueTerminados, false)) == null){Thread.sleep(500);}
			Mensaje msg = Mensaje.getMensaje(gr.getBody());
			log.info("Mensaje leido: "+msg.getName()+":"+msg.getIpCliente()+"  |  Buscando trabajo: "+msgCli.getName()+":"+msgCli.ipCliente +" | La cola tiene: "+this.queueChannel.messageCount(this.queueTerminados)+" mensajes");
			if((msg.getName()+":"+msg.getIpCliente()).contentEquals(msgCli.getName()+":"+msgCli.ipCliente)) {
				this.queueChannel.basicAck(gr.getEnvelope().getDeliveryTag(), false);
				this.queueChannel.basicRecover();
				Thread.sleep(100);
				log.info("Encontre el mensaje:\n\t\t\t\tFrom:"+msg.from+"\n\t\t\t\tName:"+msg.name+":"+msg.ipCliente+"\n\t\t\t\tNro.Render:"+msg.nroRender+"\n\t\t\tLa cola tiene ahora: "+this.queueChannel.messageCount(this.queueTrabajo)+" mensajes");
				return msg;
			}else {
				Thread.sleep(200);
				this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
				return obtenerMensaje(msgCli);
			}
		}catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}
	
	@Override
	public void run() {
		boolean salir = false;
		ArrayList<String> nodosFaltantes = new ArrayList<String>();
		ArrayList<BufferedImage> renderedImages = new ArrayList<BufferedImage>();
		LocalTime initTime = LocalTime.now();
		boolean porSamples = msg.cantidadSamples > 0;
		if(porSamples)
			log.info("Trabajando en: "+msg.getName()+" Modalidad: Por samples. Cantidad: "+msg.cantidadSamples+" Samples");
		else
			log.info("Trabajando en: "+msg.getName()+" Modalidad: Por tiempo. Cantidad: "+msg.tiempoLimite+" segundos");
		log.info("Tiempo inicio:\t"+initTime.toString());
		try {
			this.queueChannel.basicPublish("", this.queueTrabajo, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
			this.listaTrabajos.add(msg.getName()+":"+msg.ipCliente);
			//For obtain DeliveryTag
			GetResponse gr = this.queueChannel.basicGet(this.queueTrabajo, false);
			this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
			if(porSamples) {//Entonces cada worker hace X cantidad de samples
				Map<String,Integer> workers = new HashMap<String,Integer>();
				ArrayList<String> finishedWorkers = new ArrayList<String>();
				while(!salir) {
					try {
						Mensaje msgTerminado = obtenerMensaje(msg);		
		    			renderedImages.add(Imagen.ByteArrToBuffImg(msgTerminado.bufferedImg));
						if(workers.containsKey(msgTerminado.from)) {
		    				int count = workers.get(msgTerminado.from);
		    				workers.put(msgTerminado.from, count + 1);
			    		}else {
			    			workers.put(msgTerminado.from, 1);
			    		}
		    			log.info("---");
		    			log.info("Worker: " + msgTerminado.from + " Render count:" + workers.get(msgTerminado.from) + " @ " + msg.getName()+":"+msg.ipCliente);
		    			if((workers.get(msgTerminado.from) >= msg.cantidadSamples) && !(finishedWorkers.contains(msgTerminado.from))) {
		    		    	finishedWorkers.add(msgTerminado.from);
		    		    }
		    			log.info("---");
		    			log.debug("Cantidad workers que finalizaron: "+finishedWorkers.size()+"\t\t Cantidad de workers total: "+listaWorkers.size());
		    			log.debug("Cantidad de imagenes: "+renderedImages.size()+"\t\t Cantidad esperada: "+msg.cantidadSamples * listaWorkers.size());
					
	    		    	nodosFaltantes = diferenciaListas(listaWorkers, finishedWorkers);
						Thread.sleep(1000);
    					log.debug("Nodos sin terminar: "+nodosFaltantes.toString());
					
						if(renderedImages.size()> 0){
		    				if (nodosFaltantes.size() == 0){
				    			salir = true;
								log.info("###Termine: "+msg.getName()+"###");
				    			this.queueChannel.basicAck(gr.getEnvelope().getDeliveryTag(), false);
		    				}
			    		}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else {//Cada worker opera X cantidad de tiempo
				try {
					Thread.sleep(msg.tiempoLimite * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
				synchronized (this.queueConnection) {
			    	for (int i = 0; i < terminados; i++) {
			    		byte[] data = this.queueChannel.basicGet(this.queueTerminados, false).getBody();
			    		Mensaje m = Mensaje.getMensaje(data);
			    		if(m.name.contentEquals(msg.name))
			    			renderedImages.add(Imagen.ByteArrToBuffImg(m.bufferedImg));
					}
				}
			}
			this.queueChannel.basicAck(gr.getEnvelope().getDeliveryTag(),false);//No esta borrando el trabajo
			log.info("Cantidad de imagenes procesadas:\t\t"+renderedImages.size());
			this.respuesta = ImageStacker.aplicarFiltroStack(renderedImages);
		    log.info("Tiempo tardado:\t\t"+Duration.between(initTime, LocalTime.now()).toMinutes()+" minutos.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
