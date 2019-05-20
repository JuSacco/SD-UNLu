package jusacco.TP2.punto4.distributedImproved;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class Worker implements Runnable{

	Channel queueChannel;
	Connection queueConnection;
	String queueTrabajos = "queueTrabajos";
	String queueEnProceso = "queueEnProceso";
	String queueTerminados = "queueTerminados";
	Logger log = LoggerFactory.getLogger(Worker.class);
	boolean termino = false;
	
	public Worker(Channel queueChannel, Connection queueConnection) {
		this.queueChannel = queueChannel;
		this.queueConnection = queueConnection;
	}
	
	@Override
	public void run() {
		int cantProceso;
		try {
			int trabajos = (int) this.queueChannel.messageCount(this.queueTrabajos);
		    while(!this.termino) {
		    	try { 
			    	int terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
					byte[] byteImg;
					int i = 0;
					byte[] aux;
					boolean esta = false;
					synchronized (this.queueConnection) {
						byteImg = this.queueChannel.basicGet(this.queueTrabajos, false).getBody();
						cantProceso = (int) this.queueChannel.messageCount(this.queueEnProceso);
						log.debug("Worker "+Thread.currentThread().getId()+": obteniendo trabajo id:"+byteImg.hashCode());
						if(cantProceso > 0) {
							while (i < cantProceso && !esta) {
								aux = this.queueChannel.basicGet(this.queueEnProceso, false).getBody();
								if(aux.equals(byteImg)) {
									esta = true;
									log.debug("Worker "+Thread.currentThread().getId()+": Encontre la imagen en la cola queueEnProceso ");
								}
								i++;
							}
							if(!esta) {
								this.queueChannel.basicPublish("", this.queueEnProceso, MessageProperties.PERSISTENT_TEXT_PLAIN, byteImg);	
							}
						}else {
							this.queueChannel.basicPublish("", this.queueEnProceso, MessageProperties.PERSISTENT_TEXT_PLAIN, byteImg);
						}
					}
					if(cantProceso == 0 || !esta) {
						BufferedImage bImage = Imagen.ByteArrToBuffImg(byteImg);
						Imagen imgObj = Imagen.ByteArrToImagenObj(byteImg);
						FiltroSobel filtro = new FiltroSobel(bImage);
						imgObj.setByteImage(Imagen.buffImgToByteArr(filtro.aplicarFiltro()));
						synchronized (this.queueConnection) {
							this.queueChannel.basicPublish("", this.queueTerminados, MessageProperties.PERSISTENT_TEXT_PLAIN, Imagen.imagenToByteArr(imgObj));
							log.debug("Worker "+Thread.currentThread().getId()+": Publique trabajo id:"+byteImg.hashCode());
					    	terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
							log.debug("Terminados:"+terminados);
							log.debug("Trabajos:"+trabajos);
						}
					}
			    	if(terminados == trabajos){
			    		this.termino = true;
			    	}
				} catch (Exception e) {
			    	int terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
			    	trabajos = (int) this.queueChannel.messageCount(this.queueTrabajos);
					if(terminados == trabajos){
						this.termino = true;
			    	}
				}
		    }
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
