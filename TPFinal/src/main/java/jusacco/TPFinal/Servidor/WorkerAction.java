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

public class WorkerAction implements IWorkerAction{
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	ArrayList<String> workerToRemove;
	Map<String,LocalTime> workersLastPing = new HashMap<String,LocalTime>();
	Logger log = LoggerFactory.getLogger(WorkerAction.class);
	
	public WorkerAction(ArrayList<String> listaWorkers, ArrayList<String> listaTrabajos, Map<String, LocalTime> workersLastPing) {
		MDC.put("log.name", WorkerAction.class.getSimpleName().toString());
		this.listaWorkers = listaWorkers;
		this.listaTrabajos = listaTrabajos;
		this.workersLastPing = workersLastPing;
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
	public String giveWorkToDo(String worker, ArrayList<String> realizedWorks) throws RemoteException {
		String result = null;
		if(listaTrabajos.size() == 0){
			return "empty";
		}
    	result = diferenciaListas(realizedWorks);
	    if(result.length() < 1) {
	    	return "";
	    }else {
			log.info(worker+": Trabajos realizados: "+realizedWorks.toString()+" | Trabajos para realizar:"+listaTrabajos.toString());
			log.info(worker+": Delegando tarea ["+result+"]");
			return result;
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
