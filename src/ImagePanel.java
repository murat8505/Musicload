import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 6355616917210554481L;
	private BufferedImage image;
	private boolean async = true;

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isAsync() {
		return async;
	}

	public ImagePanel() {
		this.setBackground(new Color(0, 0, 0, 0));
	}

	public ImagePanel(InputStream in) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(in);
	}

	public ImagePanel(BufferedImage image) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(image);
	}

	public ImagePanel(byte[] bytes) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(bytes);
	}

	public void setImage(BufferedImage image) {
		BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		convertedImg.getGraphics().drawImage(image, 0, 0, null);

		this.image = convertedImg;
		this.repaint();
	}

	public void setImage(byte[] bytes) {
		if (bytes != null) {
			InputStream in = new ByteArrayInputStream(bytes);
			setImage(in);
		}
	}

	public void setImage(final InputStream in) {
		Thread th = new Thread() {
			public void run() {
				try {
					setImage(ImageIO.read(in));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (java.lang.OutOfMemoryError e) {
					Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
					Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
					for (Thread t : threadArray) {
						System.out.println(t.toString() + ":");
						for (StackTraceElement ste : t.getStackTrace()) {
							System.out.println("\t" + ste.toString());
						}
					}
				}
			}
		};
		th.start();
		while (!async && th.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
		}
	}

}