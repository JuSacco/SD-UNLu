package jusacco.TPFinal.Servidor;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;


public class Mensaje implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	byte[] blend;
	byte[] bufferedImg;
	int cantidadSamples = 0;
	int tiempoLimite = 0;
	int frameToRender = 1;
	String from;
	String ipCliente;
	int nroRender;
	boolean highEnd;
	
	//Mensaje simple solo string de Servidor -> worker
	public Mensaje(String name) {
		this.name = name;
	}
	
	
	//Mensaje enviado por el cliente
	public Mensaje(byte[] blend, String name, int cantidadSamples, int frameToRender, String ipCliente, boolean highEnd){
		this.blend = blend;
		this.cantidadSamples = cantidadSamples;
		this.frameToRender = frameToRender;
		this.name = name;
		this.ipCliente = ipCliente;
		this.highEnd = highEnd;
	}
	
	//Mensaje enviado por el cliente
	public Mensaje(byte[] blend,int tiempoLimite, String name, int frameToRender, String ipCliente, boolean highEnd){
		this.blend = blend;
		this.tiempoLimite = tiempoLimite;
		this.frameToRender = frameToRender;
		this.name = name;
		this.ipCliente = ipCliente;
		this.highEnd = highEnd;
	}
	
	//Mensaje armado por el worker
	public Mensaje(BufferedImage bufferedImg, String name, String from, String ipCliente,int nroRender){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImg, "png", outputStream);
			this.bufferedImg = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.name = name;
		this.from = from;
		this.ipCliente = ipCliente;
		this.nroRender = nroRender;
	}
	
	
	public byte[] getBytes(){
		try {
        	ByteArrayOutputStream bs= new ByteArrayOutputStream();
        	ObjectOutputStream os = new ObjectOutputStream (bs);
			os.writeObject(this);
	    	os.close();
	    	return  bs.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}  
	}
	
	public static Mensaje getMensaje(byte[] bData) {
		try {
			ByteArrayInputStream bs = new ByteArrayInputStream(bData); 
			ObjectInputStream is = new ObjectInputStream(bs);
			Mensaje msg = (Mensaje)is.readObject();
			is.close();
			return msg;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getBlend() {
		return blend;
	}

	public void setBlend(byte[] blend) {
		this.blend = blend;
	}

	public int getCantidadSamples() {
		return cantidadSamples;
	}

	public void setCantidadSamples(int cantidadSamples) {
		this.cantidadSamples = cantidadSamples;
	}

	public int getTiempoLimite() {
		return tiempoLimite;
	}

	public void setTiempoLimite(int tiempoLimite) {
		this.tiempoLimite = tiempoLimite;
	}

	public int getFrameToRender() {
		return frameToRender;
	}

	public void setFrameToRender(int frameToRender) {
		this.frameToRender = frameToRender;
	}

	public String getIpCliente() {
		return ipCliente;
	}

	public void setIpCliente(String ipCliente) {
		this.ipCliente = ipCliente;
	}
	public boolean getHighEnd() {
		return highEnd;
	}
}
