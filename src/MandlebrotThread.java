public class MandlebrotThread extends Thread{
	public byte[] pixels;
	
	public volatile double hmin = -1.5;
	public volatile double hmax =  1.5;
	public volatile double vmin = -1.5;
	public volatile double vmax =  1.5;
	
	public int pixelSize;
	public int pixelOffset;
	
	private int windowWidth;
	private int windowHeight;
	private int x;
	private int y;
	private int width;
	private int height;
	
	public volatile int maxIterations = 128;
	
	public volatile boolean running = true;
	public volatile boolean update = false;
	
	public MandlebrotThread(int ww, int wh, int x, int y, int w, int h, byte[] p) {
		this.windowWidth = ww;
		this.windowHeight = wh;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.pixelSize = width * height * 4;
		this.pixels = new byte[pixelSize];
		this.pixelOffset = (y * ww * 4) + (x * 4);
	}
	
	private byte[] calculateMandlebrotPixelColor(int x, int y) {
		double a = map(x, 0, windowWidth, hmin, hmax);
		double b = map(y, 0, windowHeight, vmin, vmax);
		
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
	public void run() {
		while(running) {
			if(update) {
				int ctr = 0;
				for(int i = y; i < y + height; i++) {
					for(int j = x; j < x + width; j++) {
						byte[] px = calculateMandlebrotPixelColor(j, i);
						pixels[ctr++] = px[3];
						pixels[ctr++] = px[2];
						pixels[ctr++] = px[1];
						pixels[ctr++] = px[0];
					}
				}
				update = false;
			}else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
