package jusacco.TP1.punto4;

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
        return "Mensaje de: "+this.from+"\nHacia: "+this.to+"\n"+this.msg;
    }
}
