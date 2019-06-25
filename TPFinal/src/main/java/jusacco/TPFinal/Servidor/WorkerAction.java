package jusacco.TPFinal.Servidor;

import java.rmi.RemoteException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;

public class WorkerAction implements IWorkerAction{
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	ArrayList<String> workerToRemove;
	Map<String,LocalTime> workersLastPing = new HashMap<String,LocalTime>();
	Logger log = LoggerFactory.getLogger(WorkerAction.class);
	private Connection queueConnection;
	private Channel queueChannel;
	private String queueTrabajo = "queueTrabajo";
	
	public WorkerAction(ArrayList<String> listaWorkers, ArrayList<String> listaTrabajos, Map<String, LocalTime> workersLastPing, Channel queueChannel, Connection queueConnection) {
		MDC.put("log.name", WorkerAction.class.getSimpleName().toString());
		this.listaWorkers = listaWorkers;
		this.listaTrabajos = listaTrabajos;
		this.workersLastPing = workersLastPing;
		this.queueChannel = queueChannel;
		this.queueConnection = queueConnection;
	}

	@Override
	public void helloServer(String worker) throws RemoteException {
		synchronized (listaTrabajos) {
			if(!listaWorkers.contains(worker)) {
				this.listaWorkers.add(worker);
				log.debug("Registrando nuevo worker: "+worker);
			}
		}
		synchronized (workersLastPing) {
			workersLastPing.put(worker,LocalTime.now());
		}
	}

	@Override
	public Mensaje giveWorkToDo(String worker, ArrayList<String> realizedWorks) throws RemoteException {
		String result = null;
		if(listaTrabajos.size() == 0){
			return new Mensaje("empty");
		}
    	result = diferenciaListas(realizedWorks);
	    if(result.length() < 1) {
	    	return new Mensaje("");
	    }else {
			log.info(worker+": Trabajos realizados: "+realizedWorks.toString()+" | Trabajos para realizar:"+listaTrabajos.toString());
			log.info(worker+": Delegando tarea ["+result+"]");
			return obtenerMensaje(result);
	    }
	}
	
	
	private synchronized Mensaje obtenerMensaje(String trabajo) {
		try {
			GetResponse gr;
			while((gr = this.queueChannel.basicGet(this.queueTrabajo, false)) == null){Thread.sleep(200);}
			Mensaje msg = Mensaje.getMensaje(gr.getBody());
			if((msg.getName()+":"+msg.getIpCliente()).contentEquals(trabajo)) {
				//this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
				this.queueChannel.basicRecover();
				return msg;
			}else{
				msg = null;
				System.gc();
				Thread.sleep(100);
				return obtenerMensaje(trabajo);
			}
		}catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	
	public String diferenciaListas(ArrayList<String> realizedWorks) {
		Set<String> a;
		try {
			synchronized (listaTrabajos) {
				a = new HashSet<>(listaTrabajos);
			}
		}catch (Exception e) {
			log.error("Error: "+e.getMessage());
			return "";
		}
		String result = "";
	    Set<String> b = new HashSet<>(realizedWorks);
	    for (String el: a) {
	      if (!b.contains(el)) {
	    	 result = el;
	         break;
	      }
	    }
	    return result;
	}

	@Override
	public void checkStatus() throws RemoteException {
		
	}
}
