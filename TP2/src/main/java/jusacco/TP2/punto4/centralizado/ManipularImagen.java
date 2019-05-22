package jusacco.TP2.punto4.centralizado;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import jusacco.TP2.punto4.distributedImproved.Imagen;

public class ManipularImagen {
	
	BufferedImage image;
	int cantPartes;
	
	public ManipularImagen(BufferedImage image, int cantidad) throws IOException {
		this.image = image;
		this.cantPartes = (int) Math.sqrt(cantidad);
	}
	
	public ArrayList<BufferedImage> cortar(){
		int width = image.getWidth();
		int height = image.getHeight();
		ArrayList<BufferedImage> arrayImagen = new ArrayList<BufferedImage>();
		
		for (int i = 0; i < cantPartes; i++) {
			for (int j = 0; j < cantPartes; j++) {
				BufferedImage parte = image.getSubimage(j*(width/cantPartes),i*(height/cantPartes),width/cantPartes,height/cantPartes);
				arrayImagen.add(parte);
			}
		}
		return arrayImagen;
	}
	
	public BufferedImage unirImagen(ArrayList<BufferedImage> partes) {
		/////////////////////////////////////
		int width = image.getWidth()-cantPartes*2;//Resto lo px q pierdo pq no es exacto el calculo
		int height = image.getHeight()-cantPartes*2;
		BufferedImage imgFinal = new BufferedImage(width, height, image.getType());
		Graphics g = imgFinal.getGraphics();
		
		int part = 0;
		for (int j = 0; j < cantPartes; j++) {
			for (int j2 = 0; j2 < cantPartes; j2++) {
				g.drawImage(partes.get(part),j2*(width/cantPartes),j*(height/cantPartes),null);
				part++;
			}
		}
		return imgFinal;
	}
}
