package jusacco.TPFinal.Servidor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerAction implements IWorkerAction{
	ArrayList<String> listaWorkers;
	ArrayList<String> listaTrabajos;
	Logger log = LoggerFactory.getLogger(WorkerAction.class);
	
	public WorkerAction(ArrayList<String> listaWorkers, ArrayList<String> listaTrabajos) {
		this.listaWorkers = listaWorkers;
		this.listaTrabajos = listaTrabajos;
	}

	@Override
	public void helloServer(String worker) throws RemoteException {
		this.listaWorkers.add(worker);
		log.debug("Se conecto el worker "+worker);
	}

	@Override
	public String giveWorkToDo(String worker, ArrayList<String> realizedWorks) throws RemoteException {
		String result = "";
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
		    if(!result.isEmpty()) {
		    	salir = true;
		    }else {
		    	a = new HashSet<>(listaTrabajos);
		    }
		}
		return result;
	}
}
