package jusacco.TP2.punto4.distributedImproved;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;


public class Imagen implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] byteImage;
	private int nroImg;

	public Imagen(BufferedImage image,int nro) {
		this.byteImage = buffImgToByteArr(image);
		this.nroImg = nro;
	}
	
	public Imagen(BufferedImage image) {
		this.byteImage = buffImgToByteArr(image);
	}
	
	public void setByteImage(byte[] data) {
		this.byteImage = data;
	}
	
	public BufferedImage getImage() {
		ByteArrayInputStream bis = new ByteArrayInputStream(this.byteImage);
		try {
			return ImageIO.read(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int getNroImage() {
		return this.nroImg;
	}
	
	public static byte[] imagenToByteArr(Imagen img) {
    	try {
        	ByteArrayOutputStream bs= new ByteArrayOutputStream();
        	ObjectOutputStream os = new ObjectOutputStream (bs);
			os.writeObject(img);
	    	os.close();
	    	byte[] bytes =  bs.toByteArray(); // devuelve byte[]
	    	return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}  
	}
	
	public static byte[] buffImgToByteArr(BufferedImage img) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedImage bImage = img;
		try {
			ImageIO.write(bImage, "jpg", bos);
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static BufferedImage ByteArrToBuffImg(byte[] data) {
		Imagen i = ByteArrToImagenObj(data);
		return i.getImage();
	}

	public static Imagen ByteArrToImagenObj(byte[] data) {
		try {
			ByteArrayInputStream bs = new ByteArrayInputStream(data); 
			ObjectInputStream is = new ObjectInputStream(bs);
			Imagen img = (Imagen)is.readObject();
			is.close();
			return img;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean persistImg(String src) {
		try {
			File file = new File(src);
		    ByteArrayInputStream bis = new ByteArrayInputStream(this.byteImage);
		    BufferedImage bImage = ImageIO.read(bis);
			ImageIO.write((RenderedImage)bImage ,"jpg",file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
