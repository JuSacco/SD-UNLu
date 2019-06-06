package jusacco.TPFinal.Servidor;

import java.rmi.RemoteException;
import java.util.ArrayList;

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
	public String giveWorkToDo(String worker) throws RemoteException {
		while(this.listaTrabajos.size() == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.listaTrabajos.get(0);
	}
}
