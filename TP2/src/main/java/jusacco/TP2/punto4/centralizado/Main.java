package jusacco.TP2.punto4.centralizado;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
	Logger log = LoggerFactory.getLogger(Main.class);
	
	public void singleThConCortes(String src, int cant) {
		try {
			int CANT_CORTES = cant;
			LocalTime initTime = LocalTime.now();
			BufferedImage image;
			File file = null;
			file = new File(src);
			image = ImageIO.read(file);
			log.info("Calculando imagen (Con cortes): "+file.getName());
			
			ManipularImagen imgHandler = new ManipularImagen(image,CANT_CORTES); 
			ArrayList<BufferedImage> arrayImg = imgHandler.cortar();
		    ArrayList<BufferedImage> arrayImgConFiltro = new ArrayList<BufferedImage>(); 
			
		    for (BufferedImage img : arrayImg) {
				FiltroSobel filtro = new FiltroSobel(img);
				arrayImgConFiltro.add(filtro.aplicarFiltro());
			}
		    
		    BufferedImage resultado = imgHandler.unirImagen(arrayImgConFiltro);

		    LocalTime endTime = LocalTime.now();
		    log.info("Init time:"+initTime.toString());
		    log.info("End time:"+endTime.toString());
		    log.info("Delta time:"+Duration.between(initTime, endTime));
			
	        // write image
	        try {
	            file = new File("image_singleThread_conCorte_sobel.jpg");
	            ImageIO.write(resultado, "jpg", file);
	        } catch (IOException e) {
	            System.err.println(e);
	        }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void singleThSinCortes(String src) {
		try {
			LocalTime initTime = LocalTime.now();
			BufferedImage image;
			File file = null;
			file = new File(src);
			image = ImageIO.read(file);
			log.info("Calculando imagen (Sin cortes): "+file.getName());
			
			FiltroSobel filtro = new FiltroSobel(image);
			
		    BufferedImage resultado = filtro.aplicarFiltro();

		    LocalTime endTime = LocalTime.now();
		    log.info("Init time:"+initTime.toString());
		    log.info("End time:"+endTime.toString());
		    log.info("Delta time:"+Duration.between(initTime, endTime));
			
	        // write image
	        try {
	            file = new File("image_singleThread_sinCorte_sobel.jpg");
	            ImageIO.write(resultado, "jpg", file);
	        } catch (IOException e) {
	            System.err.println(e);
	        }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		Main m = new Main();
		//m.singleThConCortes("./repositorioImagenes/audi.jpg",1500);
		m.singleThSinCortes("./repositorioImagenes/audi.jpg");
	}

}
