import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Mandlebrot implements KeyListener{
	private BufferedImage image;
	private JFrame window;
	private JLabel label;
	
	private double hmin = -1.5;
	private double hmax =  1.5;
	private double vmin = -1.5;
	private double vmax =  1.5;
	private double zoom = 1;
	
	private int width;
	private int height;
	private int maxIterations;
	
	public Mandlebrot(int width, int height) {
		this.width = width;
		this.height = height;
		
		window = new JFrame();
		label = new JLabel();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		maxIterations = 100;
		
		label.setIcon(new ImageIcon(image));
		window.add(label);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addKeyListener(this);
		window.setVisible(true);
		
		generateMandlebrotImage();
	}
	
	private void generateMandlebrotImage() {
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				Color c = calculateMandlebrotPixelColor(j, i);
				image.setRGB(j, i, c.getRGB());
			}
		}

		label.setIcon(new ImageIcon(image));
		window.pack();
	}
	
	private Color calculateMandlebrotPixelColor(int x, int y) {
		double a = map(x, 0, width, hmin, hmax);
		double b = map(y, 0, height, vmin, vmax);
		
		double ca = a;
		double cb = b;
		
		int n = 0;

		while(n < maxIterations) {
			double aa = a * a - b * b;
			double bb = 2 * a * b;
			a = aa + ca;
			b = bb + cb;
			
			if(a + b > 16) {
				break;
			}
			
			n++;
		}
		
		return new Color(0.5f * (float)Math.sin(0.1 * n + 0) + 0.5f,
		                 0.5f * (float)Math.sin(0.1 * n + 2) + 0.5f, 
				         0.5f * (float)Math.sin(0.1 * n + 4) + 0.5f, 
				         1.0f);
	}
	
	public static void main(String[] args) {
		new Mandlebrot(1200,1000);
	}
	
	public double map(double val, double min1, double max1, double min2, double max2) {
		double pctg = (val - min1) / (max1 - min1);
		return (pctg * (max2 - min2)) + min2;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {	
		if(e.getKeyCode() == KeyEvent.VK_UP) {
			vmin -= 0.1 * zoom;
			vmax -= 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			vmin += 0.1 * zoom;
			vmax += 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			hmin -= 0.1 * zoom;
			hmax -= 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			hmin += 0.1 * zoom;
			hmax += 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_Z) {
			maxIterations /= 2;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_X) {
			maxIterations *= 2;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_COMMA) {
			zoom *= 1.05;
			hmin -= 0.1 * zoom;
			hmax += 0.1 * zoom;
			vmin -= 0.1 * zoom;
			vmax += 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
			zoom *= 0.95;
			hmin += 0.1 * zoom;
			hmax -= 0.1 * zoom;
			vmin += 0.1 * zoom;
			vmax -= 0.1 * zoom;
			generateMandlebrotImage();
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
		System.out.println(zoom);
	}

	@Override
	public void keyReleased(KeyEvent e) {	
	}
}
