package jusacco.TP2.punto4.distributedImproved;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/*
 * https://github.com/gjaszfalvi/sobel/blob/master/Sobel.java
 */

public class FiltroSobel {
	BufferedImage image;
	
		public FiltroSobel(BufferedImage imagen) {
			this.image = imagen;
		}
		
	public BufferedImage aplicarFiltro() {
		    
		    // the sobel matrix in two 2D arrays
		    int[][] sx = {{-1,0,1},{-1,0,1},{-1,0,1}};
		    int[][] sy = {{-1,-1,-1},{0,0,0},{1,1,1}};
		    
		    // a sobel template 2D array for calculation
		    int[][] sob;
		        
	        // get image width and height
	        int width = image.getWidth();
	        int height = image.getHeight();
	        /*
	         * Creo img nueva unos px mas grande
	         */
	        BufferedImage imgNueva = new BufferedImage(width+2, height+2, image.getType());
	        Graphics gImage = imgNueva.getGraphics();
	        gImage.drawImage(image,1,1,width,height,null);
	        
	        width++;
	        height++;
	        	        
	        // at first need to greyscale and populate sob[][] array
	        sob = new int[width][height];
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                int pixel = imgNueva.getRGB(x, y);
	                int a = (pixel >> 24) & 0xff;
	                int r = (pixel >> 16) & 0xff;
	                int g = (pixel >> 8) & 0xff;
	                int b = pixel & 0xff;
	                
	                // calculate average
	                int avg = (r+g+b)/3;
	                
	                sob[x][y] = avg;
	                // replace RGB value with average
	                pixel = (avg << 24) | (avg << 16) | (avg << 8) | avg;
	                imgNueva.setRGB(x, y, pixel);
	            }
	        }
	        
	        // sobel calculation
	        for (int y = 1; y < height-1; y++) {
	            for (int x = 1; x < width-1; x++) {
	                int px = (sx[0][0] * sob[x-1][y-1]) + (sx[0][1] * sob[x][y-1]) +
	                (sx[0][2] * sob[x+1][y-1]) + (sx[1][0] * sob[x-1][y]) + 
	                (sx[1][1] * sob[x][y]) + (sx[1][2] * sob[x+1][y]) + 
	                (sx[2][0] * sob[x-1][y+1]) + (sx[2][1] * sob[x][y+1]) + 
	                (sx[2][2] * sob[x+1][y+1]);
	 
	                int py = (sy[0][0] * sob[x-1][y-1]) + (sy[0][1] * sob[x][y-1]) +
	                (sy[0][2] * sob[x+1][y-1]) + (sy[1][0] * sob[x-1][y]) + 
	                (sy[1][1] * sob[x][y]) + (sy[1][2] * sob[x+1][y]) + 
	                (sy[2][0] * sob[x-1][y+1]) + (sy[2][1] * sob[x][y+1]) + 
	                (sy[2][2] * sob[x+1][y+1]);
	                
	                int pixel = (int) Math.sqrt((px * px) + (py * py));

	                if (pixel>255) {
	                    pixel = 255;
	                } else if (pixel<0) {
	                    pixel = 0;
	                }
	                
	                Color pix = new Color(pixel,pixel,pixel);
	                imgNueva.setRGB(x, y, pix.getRGB());
	            }
	        }
	        //Recorto la img
	        imgNueva = imgNueva.getSubimage(2,2,width-3,height-3);

	        return imgNueva;
	    }
	}


