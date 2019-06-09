package jusacco.TPFinal.Servidor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class WorkerAction implements IWorkerAction{
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	Logger log = LoggerFactory.getLogger(WorkerAction.class);
	
	public WorkerAction(ArrayList<String> listaWorkers, ArrayList<String> listaTrabajos) {
		MDC.put("log.name", WorkerAction.class.getSimpleName().toString());
		this.listaWorkers = listaWorkers;
		this.listaTrabajos = listaTrabajos;
	}

	@Override
	public void helloServer(String worker) throws RemoteException {
		synchronized (listaTrabajos) {
			if(!listaWorkers.contains(worker))
				this.listaWorkers.add(worker);
			else
				log.debug("Ya estaba registrado el worker");
			log.debug("Se conecto el worker "+worker);
		}
	}

	@Override
	public String giveWorkToDo(String worker, ArrayList<String> realizedWorks) throws RemoteException {
		String result = null;
		boolean salir = false;
		while(!salir) {
	    	result = diferenciaListas(realizedWorks);
		    if(result.length() > 0)
		    	salir = true;
		}
		log.debug(worker+": realizedWorks"+realizedWorks.toString()+"| listaTrabajos"+listaTrabajos.toString());
		log.debug(worker+": Delegando tarea["+result+"]");
		log.debug("Result lenght: "+result.length());
		return result;
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
}
