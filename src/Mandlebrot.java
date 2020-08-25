import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Mandlebrot implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
	enum Mode {
		SINGLE_THREAD, MULTI_THREAD
	}
	
	private Mode mode;
	
	private BufferedImage image;
	private JFrame window;
	private JLabel label;
	private JLabel textLabel;
	
	private MandlebrotThread[] threads;
	
	private byte[] pixels;
	
	private long frameStart = 0;
	private float frameTime = 0;
	
	private double hmin = -1.5;
	private double hmax =  1.5;
	private double vmin = -1.5;
	private double vmax =  1.5;
	
	private int width;
	private int height;
	private int maxIterations;
	
	private int mouseX;
	private int mouseY;
	
	private int maxThreads = 8;
	
	public Mandlebrot(int width, int height) {
		this.width = width;
		this.height = height;
		
		mode = Mode.SINGLE_THREAD;
		
		window = new JFrame();
		label = new JLabel();
		textLabel = new JLabel();
		textLabel.setOpaque(true);
		textLabel.setBackground(Color.WHITE);
		textLabel.setBounds(0, 0, 300, 75);
		label.add(textLabel);
		
		image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		
		pixels = new byte[width * height * 4];
		
		maxIterations = 128;
		
		threads = new MandlebrotThread[maxThreads];
		int tsq = (int)Math.sqrt(maxThreads);
		for(int i = 0; i < maxThreads; i++) {
			int xpos = 0;
			int ypos = i * (height / maxThreads);
			int wid = width;
			int hei = height / maxThreads;
			
			threads[i] = new MandlebrotThread(width, height, xpos, ypos, wid, hei, pixels);
			
		}
		
		label.setIcon(new ImageIcon(image));
		window.add(label);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addKeyListener(this);
		window.addMouseListener(this);
		window.addMouseMotionListener(this);
		window.addMouseWheelListener(this);
		window.setResizable(false);
		window.setVisible(true);
		window.pack();
		
		for(int i = 0; i < maxThreads; i++) {
			threads[i].start();
		}
		
		generateMandlebrotImage();
	}
	
	private void generateMandlebrotImage() {
		frameStart = System.nanoTime();
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				byte[] px = calculateMandlebrotPixelColor(j, i);
				pixels[i * width * 4 + j * 4 + 0] = px[3];
				pixels[i * width * 4 + j * 4 + 1] = px[2];
				pixels[i * width * 4 + j * 4 + 2] = px[1];
				pixels[i * width * 4 + j * 4 + 3] = px[0];
			}
		}
	
		byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		System.arraycopy(pixels, 0, imgData, 0, pixels.length);
		label.setIcon(new ImageIcon(image));
		
		frameTime = (float)((System.nanoTime() - frameStart) / 1000000000.0);
		textLabel.setText("<html>Press F1 to view controlls<br>Iterations: " + 
		                  maxIterations + "<br>Calc Time: " + frameTime + " seconds<br>Mode: " + 
				          mode + " </html>");		
		window.pack();
	}
	
	private void generateMandlebrotImageThreaded() {
		frameStart = System.nanoTime();
		
		for(int i = 0; i < maxThreads; i++) {
			threads[i].vmin = vmin;
			threads[i].vmax = vmax;
			threads[i].hmin = hmin;
			threads[i].hmax = hmax;
			threads[i].maxIterations = maxIterations;
			threads[i].update = true;
		}
		for(int i = 0; i < maxThreads; i++) {
			while(threads[i].update == true);
			System.arraycopy(threads[i].pixels, 0, pixels, threads[i].pixelOffset, threads[i].pixelSize);
		}
	
		byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		System.arraycopy(pixels, 0, imgData, 0, pixels.length);
		label.setIcon(new ImageIcon(image));
		
		frameTime = (float)((System.nanoTime() - frameStart) / 1000000000.0);
		textLabel.setText("<html>Press F1 to view controlls<br>Iterations: " + 
		                  maxIterations + "<br>Calc Time: " + frameTime + " seconds<br>Mode: " + 
				          mode + " </html>");		
		window.pack();
	}
	
	private byte[] calculateMandlebrotPixelColor(int x, int y) {
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
		
		double rd = 0.5f * Math.sin(0.1 * n + 0) + 0.5f;
		double gr = 0.5f * Math.sin(0.1 * n + 2) + 0.5f;
		double bl = 0.5f * Math.sin(0.1 * n + 4) + 0.5f;
		
		return new byte[] {
			(byte)(rd * 255),
			(byte)(gr * 255), 
			(byte)(bl * 255), 
			(byte)255
		};
	}
	
	public double map(double val, double min1, double max1, double min2, double max2) {
		double pctg = (val - min1) / (max1 - min1);
		return (pctg * (max2 - min2)) + min2;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		boolean update = false;
		if(e.getKeyCode() == KeyEvent.VK_UP) {
			double pct = (vmax - vmin) * 0.1;
			vmin -= pct;
			vmax -= pct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			double pct = (vmax - vmin) * 0.1;
			vmin += pct;
			vmax += pct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			double pct = (hmax - hmin) * 0.1;
			hmin -= pct;
			hmax -= pct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			double pct = (hmax - hmin) * 0.1;
			hmin += pct;
			hmax += pct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_Z) {
			maxIterations /= 2;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_X) {
			maxIterations *= 2;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_COMMA) {
			double hpct = (hmax - hmin) * 0.1;
			double vpct = (vmax - vmin) * 0.1;
			hmin -= hpct;
			hmax += hpct;
			vmin -= vpct;
			vmax += vpct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
			double hpct = (hmax - hmin) * 0.1;
			double vpct = (vmax - vmin) * 0.1;
			hmin += hpct;
			hmax -= hpct;
			vmin += vpct;
			vmax -= vpct;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
		else if(e.getKeyCode() == KeyEvent.VK_1) {
			mode = Mode.SINGLE_THREAD;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_2) {
			mode = Mode.MULTI_THREAD;
			update = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_F1) {
			JOptionPane.showMessageDialog(null, "1: Single Thread Mode\n"
					                          + "2: Multi Thread Mode\n"
					                          + ", or mouse scroll down: Zoom Out\n"
					                          + ". or mouse scroll up: Zoom In\n"
					                          + "arrow keys or drag mouse: Move Image\n"
					                          + "z or left click: Increase Iterations\n"
					                          + "x or right click: Decrease Iterations\n"
					                          + "esc: Close Program");
		}
		if(update) {
			switch(mode) {
				case SINGLE_THREAD:{
					generateMandlebrotImage();
					break;
				}
				case MULTI_THREAD:{
					generateMandlebrotImageThreaded();
					break;
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double xdif = e.getX() - mouseX;
		double ydif = e.getY() - mouseY;
		mouseX = e.getX();
		mouseY = e.getY();
		double pct = (vmax - vmin) * 0.002;
		vmin -= ydif * pct;
		vmax -= ydif * pct;
		hmin -= xdif * pct;
		hmax -= xdif * pct;
	
		switch(mode) {
			case SINGLE_THREAD:{
				generateMandlebrotImage();
				break;
			}
			case MULTI_THREAD:{
				generateMandlebrotImageThreaded();
				break;
			}
		}
		
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double hpct = (hmax - hmin) * 0.1 * e.getWheelRotation();
		double vpct = (vmax - vmin) * 0.1 * e.getWheelRotation();
		hmin -= hpct;
		hmax += hpct;
		vmin -= vpct;
		vmax += vpct;
		switch(mode) {
			case SINGLE_THREAD:{
				generateMandlebrotImage();
				break;
			}
			case MULTI_THREAD:{
				generateMandlebrotImageThreaded();
				break;
			}
		}		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			mouseX = e.getX();
			mouseY = e.getY();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			maxIterations *= 2;
		}else if(e.getButton() == MouseEvent.BUTTON3) {
			maxIterations /= 2;
		}
		switch(mode) {
			case SINGLE_THREAD:{
				generateMandlebrotImage();
				break;
			}
			case MULTI_THREAD:{
				generateMandlebrotImageThreaded();
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {	
	}

	@Override
	public void mouseMoved(MouseEvent e) {	
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	public static void main(String[] args) {
		new Mandlebrot(1920, 1080);
	}
}
