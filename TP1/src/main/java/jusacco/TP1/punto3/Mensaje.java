package jusacco.TP1.punto3;

public class Mensaje {
	String from;
	String to;
	String msg;
	
	public Mensaje (String from, String to, String msg) {
		this.from = from;
		this.to = to;
		this.msg = msg;
	}
	
	@Override
	public String toString (){
        return "Mensaje de: "+this.from+"\n Hacia: "+this.to+"\n"+this.msg;
    }
}
