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
	Mensaje msg;
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	Logger log = LoggerFactory.getLogger(ThreadServer.class);
	Channel queueChannel;
	Connection queueConnection;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	volatile BufferedImage respuesta;
	
	public ThreadServer(Mensaje msg, ArrayList<String> listaWorkers, BufferedImage respuesta, Channel queueChannel, Connection queueConnection, ArrayList<String> listaTrabajos) {
		this.msg = msg;
		this.listaWorkers = listaWorkers;
		this.respuesta = respuesta;
		this.queueChannel = queueChannel;
		this.queueConnection = queueConnection;
		this.listaTrabajos = listaTrabajos;
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
	
	@Override
	public void run() {
		boolean salir = false;
		ArrayList<BufferedImage> renderedImages = new ArrayList<BufferedImage>();
		LocalTime initTime = LocalTime.now();
		int intentos = 0;
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
			GetResponse grMsg;
			this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
			if(porSamples) {//Entonces cada worker hace X cantidad de samples
				Map<String,Integer> workers = new HashMap<String,Integer>();
				ArrayList<String> finishedWorkers = new ArrayList<String>();
				while(!salir) {
					try {
						//For obtain DeliveryTag
						if(this.queueChannel.messageCount(this.queueTerminados) > 0) {
							while((grMsg = this.queueChannel.basicGet(this.queueTerminados, false)) == null) {Thread.sleep(100);}
							byte[] data = grMsg.getBody();
				    		Mensaje m = Mensaje.getMensaje(data);
				    		if(m.getName().contentEquals(msg.getName()) && m.getIpCliente().contentEquals(msg.getIpCliente())) {
								this.queueChannel.basicAck(grMsg.getEnvelope().getDeliveryTag(), false);//Doy ack porque es el msg que quiero
				    			renderedImages.add(Imagen.ByteArrToBuffImg(m.bufferedImg));
				    			if(workers.containsKey(m.from)) {
				    				int count = workers.get(m.from);
				    				workers.put(m.from, count + 1);
				    			}else {
				    				workers.put(m.from, 1);
				    			}
				    			log.info("---");
					    		for (String key: workers.keySet()) {
					    			log.info("Worker: " + key + " Render count:" + workers.get(key) + " @ " + msg.getName()+":"+msg.ipCliente);
					    		    if((workers.get(key) == msg.cantidadSamples) && !(finishedWorkers.contains(key))) {
					    		    	finishedWorkers.add(key);
					    		    }
					    		}
				    			log.info("---");
				    		}
				    		if(renderedImages.size()> 0){
				    			log.debug("finishedWorkers.size():"+finishedWorkers.size()+"\t listaWorkers.size()"+listaWorkers.size());
				    			if(finishedWorkers.size() == listaWorkers.size()) {
					    			log.debug("renderedImages.size():"+renderedImages.size()+"\t msg.cantidadSamples * finishedWorkers.size()"+msg.cantidadSamples * finishedWorkers.size());
				    				if (renderedImages.size() == (msg.cantidadSamples * finishedWorkers.size())){
						    			salir = true;
										log.info("Termine: "+msg.getName());
						    			this.queueChannel.basicGet(this.queueTrabajo, true);
						    			log.debug("Tengo que borrar "+Mensaje.getMensaje(gr.getBody()).name);
						    			this.queueChannel.basicAck(gr.getEnvelope().getDeliveryTag(), false);
				    				}else {
					    				log.error("Hay workers con problemas... Re-intentando "+intentos+"/20");
					    				intentos++;
					    				if (intentos > 19) {
					    					ArrayList<String> nodosError = diferenciaListas(finishedWorkers, listaWorkers);
					    					log.error("Parece que hubo un problema con el nodo: "+nodosError.toString());
					    					for(String str : nodosError) {
					    						for(String strWrk : listaWorkers) {
					    							if(strWrk.equals(str)) {
					    								listaWorkers.remove(str);
					    								log.error("Eliminando al nodo "+str);
					    							}
					    						}
					    					}
					    					break;
					    				}
				    				}
				    			}
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
			log.info("Cantidad de imagenes:\t"+renderedImages.size());
			this.respuesta = ImageStacker.aplicarFiltroStack(renderedImages);
			log.info("endTime aplicarFilto:\t"+LocalTime.now().toString());
		    log.info("Delta time aplicarFilto:\t"+Duration.between(initTime, LocalTime.now()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
