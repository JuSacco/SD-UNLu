package jusacco.TPFinal.Servidor.Tools;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ImageStacker {
	String path;
	ArrayList<BufferedImage> liSamples;
	private final Logger log = LoggerFactory.getLogger(ImageStacker.class);
	
	public void aplicarFiltroMedian(ArrayList<BufferedImage> liImg) {
		LocalTime initTime = LocalTime.now();
		log.info("initTime aplicarFiltoMedian:"+initTime.toString());
		log.info("Cantidad de imagenes:\t"+liImg.size());
		// Generate a new BufferedImage without the tourist
		BufferedImage newImage = new BufferedImage(liImg.get(0).getWidth(), liImg.get(0).getHeight(),
				BufferedImage.TYPE_INT_RGB);

		// Apply median filter to other liImg that do contain the pesky tourist
		for (int x = 0; x < liImg.get(0).getWidth(); x++) {
			for (int y = 0; y < liImg.get(0).getHeight(); y++) {
				List<Integer> RGB = new ArrayList<Integer>();
				int median;
				for (int i = 0; i < liImg.size(); i++) {
					RGB.add(liImg.get(i).getRGB(x, y));

				}
				Collections.sort(RGB);
				if (RGB.size() % 2 == 0) {
					median = (RGB.get((RGB.size() / 2)) + RGB.get((RGB.size() / 2 - 1))) / 2;
				} else {
					median = RGB.get(RGB.size() / 2);
				}

				newImage.setRGB(x, y, median);
			}
		}

		// Display the final image
		File outputFile = new File("./Imagenes procesadas/render-"+liImg.size()+" median-"+LocalTime.now().toString().replace(':', '.')+".png");
		try {
			ImageIO.write(newImage, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("endTime aplicarFilto:"+LocalTime.now().toString());
	    log.info("Delta time aplicarFilto:"+Duration.between(initTime, LocalTime.now()));
	}

	
	
	public static BufferedImage aplicarFiltroStack(ArrayList<BufferedImage> liImg) {
		BufferedImage newImage = new BufferedImage( liImg.get(0).getWidth(),  liImg.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < liImg.get(0).getWidth(); x++) {
			for (int y = 0; y < liImg.get(0).getHeight(); y++) {
				List<Integer> red = new ArrayList<Integer>();
				List<Integer> green = new ArrayList<Integer>();
				List<Integer> blue = new ArrayList<Integer>();

				for (int i = 0; i <  liImg.size(); i++) {
					int argb =  liImg.get(i).getRGB(x, y);
					int r = (argb >> 16) & 0xFF;
					int g = (argb >> 8) & 0xFF;
					int b = (argb >> 0) & 0xFF;

					red.add(r);
					green.add(g);
					blue.add(b);
				}

				int finalR = meanList(red);
				int finalG = meanList(green);
				int finalB = meanList(blue);

				int rgb = ((finalR & 0x0ff) << 16) | ((finalG & 0x0ff) << 8) | (finalB & 0x0ff);

				newImage.setRGB(x, y, rgb);

			}
		}
		File outputFile = new File("./Imagenes procesadas/render-"+liImg.size()+" stacks-"+LocalTime.now().toString().replace(':', '.')+".png");
		try {
			ImageIO.write(newImage, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return newImage;
	}
	
	public static int meanList(List<Integer> l) {
		int sumList = 0;
		for (int i = 0; i < l.size(); i++) {
			sumList += l.get(i);
		}

		return sumList / l.size();
	}

	public ArrayList<BufferedImage> loadImages(String path){
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for (String str : getFiles(path)) {
			try {
				BufferedImage originalImage = ImageIO.read(new File(path + str));
				images.add(originalImage);

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		return images;
	}
	
	
    public ArrayList<String> getFiles( String path ) {
        File f = new File( path );
        if ( f.isDirectory()) {
            ArrayList<String> res   = new ArrayList<String>();
            File[] arr_content = f.listFiles();
            int size = arr_content.length;
            for ( int i = 0; i < size; i ++ ) {
                if ( arr_content[ i ].isFile( ))
                res.add( arr_content[ i ].getName());
            }
            return res;
        } else {
        	f.mkdir();  
            return null;      	
        }
    }
	
}