import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

public class ItemPanel extends JPanel {
	private static final long serialVersionUID = -2104991772995031145L;
	private JProgressBar progressBar;
	private JLabel videoLabel;
	private ImagePanel albumArtworkPanel;
	private JLabel songLabel;
	private JLabel artistLabel;
	private SongInfo info;
	private ImagePanel closePanel;

	/**
	 * Create the panel.
	 */
	public ItemPanel(String videoTitle, final LoadThread loadThread) {
		setLayout(null);
		setPreferredSize(new Dimension(410, 57));

		closePanel = new ImagePanel(ItemPanel.class.getResourceAsStream("resources/close.png"));

		closePanel.setBounds(378, 3, 22, 22);
		closePanel.setToolTipText("Cancel Download");
		closePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (loadThread != null) {
					loadThread.kill();
				}
				DownloadGUI.instance.searchPanel.revalidate();
			}
		});
		closePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(closePanel);

		albumArtworkPanel = new ImagePanel();
		albumArtworkPanel.setBounds(1, 1, 53, 53);
		albumArtworkPanel.setVisible(false);
		add(albumArtworkPanel);

		songLabel = new JLabel("Song title");
		songLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		songLabel.setBounds(89, 6, 331, 25);
		songLabel.setVisible(false);

		add(songLabel);

		artistLabel = new JLabel("By: Some Artist");
		artistLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		artistLabel.setBounds(120, 24, 282, 25);
		artistLabel.setVisible(false);
		add(artistLabel);

		progressBar = new JProgressBar();
		progressBar.setBounds(6, 29, 396, 20);
		progressBar.setStringPainted(true);
		add(progressBar);

		videoLabel = new JLabel(videoTitle);
		videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		videoLabel.setBounds(6, 6, 414, 16);
		add(videoLabel);
		// this.setBorder(BorderFactory.createLineBorder(Color.black));
	}

	public void completeInfo(SongInfo info) {
		this.info = info;
		videoLabel.setVisible(false);
		progressBar.setVisible(false);
		artistLabel.setVisible(true);
		closePanel.setVisible(false);
		songLabel.setVisible(true);
		albumArtworkPanel.setVisible(true);

		setArtist(info.getArtist());
		setTitle(info.getTitle());
		setAlbumArtwork(info.getAlbumArtwork());
		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseClicked(MouseEvent event) {
				try {
					DownloadGUI.instance.changePanel(new ResultPanel(ItemPanel.this, ItemPanel.this.info));
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
		});
	}

	public void setTitle(String title) {
		songLabel.setText(title);
	}

	public void setArtist(String artist) {
		artistLabel.setText("By " + artist);
	}

	public void setAlbumArtwork(byte[] artwork) {
		albumArtworkPanel.setImage(artwork);
	}

	public void setProgressMin(int min) {
		progressBar.setMinimum(min);
	}

	public void setProgress(int value) {
		progressBar.setValue(value);
	}

	public void setProgressMax(int max) {
		progressBar.setMaximum(max);
	}

	public void setProgressIndeterminate(boolean value) {
		progressBar.setIndeterminate(value);
	}

	public void setProgressText(String text) {
		progressBar.setString(text);
	}

	public Color col(int r, int g, int b) {
		return new Color(Math.min(255, Math.max(r, 0)), Math.min(255, Math.max(g, 0)), Math.min(255, Math.max(b, 0)));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Component[] components = this.getParent().getComponents();
		int id = 0;
		for (Component c : components) {
			id++;
			if (c == this)
				break;
		}

		id = components.length - id;
		g.setColor(this.getParent().getBackground());
		if (id % 2 == 0)
			g.setColor(col(g.getColor().getRed() + 30, g.getColor().getGreen() + 30, g.getColor().getBlue() + 30));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	public SongInfo getInfo() {
		return info;
	}

	public void setInfo(SongInfo info) {
		this.info = info;
	}
}
