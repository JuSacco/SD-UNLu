package jusacco.TP2.punto1;

import java.io.Serializable;
import java.util.ArrayList;

public class MasterIndex implements Serializable{
	private static final long serialVersionUID = 1L;
	private String owner;
	private ArrayList<Archivo> liArchivo;
	
	public MasterIndex(String owner, ArrayList<Archivo> peerSharedData) {
		this.owner = owner;
		this.liArchivo = peerSharedData;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public ArrayList<Archivo> getLiArchivo() {
		return liArchivo;
	}
	public void setLiArchivo(ArrayList<Archivo> liArchivo) {
		this.liArchivo = liArchivo;
	}
	
	public ArrayList<Archivo> mergeLiArchivo(ArrayList<Archivo> li) {
		ArrayList<Archivo> result = new ArrayList<>(li);
		ArrayList<Archivo> thisResult = new ArrayList<>(this.liArchivo);
		result.removeAll(thisResult);
		thisResult.addAll(result);
		return thisResult;
	}
}
