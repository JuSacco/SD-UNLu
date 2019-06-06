package jusacco.TPFinal.Servidor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	Logger log = LoggerFactory.getLogger(ThreadServer.class);
	Channel queueChannel;
	Connection queueConnection;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	volatile BufferedImage respuesta;
	
	public ThreadServer(Mensaje msg, ArrayList<String> listaWorkers, BufferedImage respuesta, Channel queueChannel, Connection queueConnection) {
		this.msg = msg;
		this.listaWorkers = listaWorkers;
		this.respuesta = respuesta;
		this.queueChannel = queueChannel;
		this.queueConnection = queueConnection;
	}
	
	public BufferedImage getRespuesta() {
		return this.respuesta;
	}
	
	@Override
	public void run() {
		  boolean salir = false;
			ArrayList<BufferedImage> renderedImages = new ArrayList<BufferedImage>();
			LocalTime initTime = LocalTime.now();
			boolean porSamples = msg.cantidadSamples > 0;
			if(porSamples)
				log.info("Obteniendo trabajo. Modalidad: Por samples. Cantidad: "+msg.cantidadSamples);
			else
				log.info("Obteniendo trabajo. Modalidad: Por tiempo. Cantidad: "+msg.tiempoLimite+" segundos");
			log.info("Tiempo inicio:\t"+initTime.toString());
			try {
				this.queueChannel.basicPublish("", this.queueTrabajo, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
				//For obtain DeliveryTag
				GetResponse gr = this.queueChannel.basicGet(this.queueTrabajo, false);
				this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
				if(porSamples) {//Entonces cada worker hace X cantidad de samples
					Map<String,Integer> workers = new HashMap<String,Integer>();
					while(!salir) {
						try {
							byte[] data = this.queueChannel.basicGet(this.queueTerminados, false).getBody();
				    		Mensaje m = Mensaje.getMensaje(data);
				    		if(m.name.contentEquals(msg.name)) {
				    			renderedImages.add(Imagen.ByteArrToBuffImg(m.bufferedImg));
				    			if(workers.containsKey(m.from)) {
				    				int count = workers.get(m.from);
				    				workers.put(m.from, count + 1);
				    			}else {
				    				workers.put(m.from, 1);
				    			}
				    		}
				    		for (String key: workers.keySet()) {
				    		    System.out.println("Worker : " + key);
				    		    System.out.println("Render : " + workers.get(key));
				    		    if(workers.get(key) == msg.cantidadSamples) {
				    		    	workers.remove(key);
				    		    }
				    		}
				    		if(renderedImages.size()>0 && workers.isEmpty()) {
				    			salir = true;
				    			this.queueChannel.basicGet(this.queueTrabajo, true);
				    			//this.queueChannel.basicAck(gr.getEnvelope().getDeliveryTag(), false);
				    		}
						}catch (Exception e) {
							
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
				log.info("Cantidad de imagenes:\t"+renderedImages.size());
				this.respuesta = ImageStacker.aplicarFiltroStack(renderedImages);
				log.info("endTime aplicarFilto:\t"+LocalTime.now().toString());
			    log.info("Delta time aplicarFilto:\t"+Duration.between(initTime, LocalTime.now()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}


}
