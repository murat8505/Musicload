import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import youtube.SongData;
import youtube.Youtube;
import youtube.YoutubeDownloader;
import youtube.YoutubeMP3;
import youtube.YoutubeVideo;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;
import com.coleman.utilities.http.HttpForm;

public class Server extends AbstractServer {
	
	private static final File MUSIC = new File(System.getProperty("user.home")
			+ "\\Desktop\\Java Server\\www\\MusicFiles\\");
	
	public Server(int port) {
		super(port);
	}
	
	protected String getContentType(File file) {
		if (file.isDirectory())
			return "text/html";
		String fileType;
		{
			int dotIndex = file.getName().lastIndexOf('.');
			fileType = dotIndex < 0 ? "" : file.getName().substring(dotIndex + 1);
		}
		if (fileType.equals("ico"))
			return "image/ico";
		if (fileType.equals("gif"))
			return "image/gif";
		if (fileType.equals("html"))
			return "text/html";
		if (fileType.equals("txt"))
			return "text/plain";
		if (fileType.equals("js"))
			return "text/javascript";
		if (fileType.equals("mp3"))
			return "audio/mp3";
		if (fileType.equals("jar"))
			return "application/java-archive";
		if (fileType.equals("css"))
			return "text/css";
		System.err.println("WARNING: Unable to find content type for '" + fileType + "'");
		return "application/octet-stream";
	}
	
	protected String getUsedContentType(File file) {
		if (file.isDirectory())
			return "text/html";
		String fileType;
		{
			int dotIndex = file.getName().indexOf('.');
			fileType = dotIndex < 0 ? "" : file.getName().substring(dotIndex + 1);
		}
		if (fileType.equals("html"))
			return "text/html";
		if (fileType.equals("txt"))
			return "text/plain";
		if (fileType.equals("js"))
			return "text/javascript";
		if (fileType.equals("jar"))
			return "application/java-archive";
		if (fileType.equals("css"))
			return "text/css";
		if (fileType.equals("gif"))
			return "image/gif";
		if (fileType.equals("png"))
			return "image/png";
		if (fileType.equals("jpg"))
			return "image/jpeg";
		if (fileType.equals("jpeg"))
			return "image/jpeg";
		return "application/octet-stream";
	}
	
	private boolean isInvisible(File file) {
		return file.getName().startsWith(".") || (file.getParentFile() != null && isInvisible(file.getParentFile()));
	}
	
	@Override
	protected void handleConnection(Socket connectionSocket) throws IOException {
		Request request = Request.readRequest(connectionSocket.getInputStream());
		if (request == null)
			return;
		if (request.getFileName().endsWith("/")) {
			forward(request.getFileName() + "index.html", connectionSocket);
			return;
		}
		String host = request.getProperties().get("Host");
		if (host == null)
			return;
		if (host.startsWith("www."))
			host = host.substring(4);
		if (host.contains(":"))
			host = host.substring(0, host.indexOf(":"));
		File serverFile = new File(getDataFile("www"), request.getFileName());
		if (request.getFileName().equals("/search.jsp")) {
			Map<String, String> postData = request.getGetProperties();// ClientUtils.parseSemicolon(new
																		// String(request.getContent()));
			if (postData.containsKey("query")) {
				String songName = postData.get("query");
				SongData song = downloadSong(songName, new YoutubeMP3());
				if (song == null) {
					ResponseBuilder response = new ResponseBuilder(404);
					response.append("Unable to download this song; try adding more info, or get a different song");
					response.writeTo(connectionSocket);
					return;
				}
				YoutubeVideo video = song.getYoutubeSource();
				forward("/song.html?song=" + ClientUtils.encode(video.getSongName()) + "&artist="
						+ ClientUtils.encode(video.getSongArtist()), connectionSocket);
				return;
			} else {
				ResponseBuilder response = new ResponseBuilder(400);
				response.writeTo(connectionSocket);
				return;
			}
		}
		if (host.equals("lemuralex13.tk") && request.getFileName().equals("/signature.png")) {
			String referer = request.getProperties().get("Referer");
			if (referer == null) {
				referer = "lemuralex13.tk:8080/signature.png";
			}
			ResponseBuilder response = new ResponseBuilder(200, "image/png");
			BufferedImage bi = new BufferedImage(750, 50, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) bi.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(255, 105, 180));
			g.setFont(new Font("Calibri", Font.BOLD, 17));
			
			g.drawString(referer, 0, 15);
			g.dispose();
			response.writeTo(connectionSocket);
			ImageIO.write(bi, "png", connectionSocket.getOutputStream());
			return;
		}
		if (host.equals("lemuralex13.tk") && request.getFileName().equals("/large.png")) {
			String referer = request.getProperties().get("Referer");
			if (referer != null && referer.contains("epicbot") && !referer.contains("98532")) {
				ResponseBuilder response = new ResponseBuilder(200, "png");
				BufferedImage bi = new BufferedImage(100000, 1, BufferedImage.TYPE_INT_ARGB);
				response.writeTo(connectionSocket);
				ImageIO.write(bi, "png", connectionSocket.getOutputStream());
			} else {
				ResponseBuilder response = new ResponseBuilder(200, "png");
				BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
				response.writeTo(connectionSocket);
				ImageIO.write(bi, "png", connectionSocket.getOutputStream());
			}
			return;
		}
		if (serverFile.exists() && !isInvisible(serverFile)) {
			if (serverFile.isDirectory()) {
				forward(request.getFileName() + "/index.html", connectionSocket);
				return;
			}
			if (!host.equals("gold2bank.com"))
				System.out.println("[" + new Date(System.currentTimeMillis()) + "] "
						+ IPMapping.getName(connectionSocket.getInetAddress().getHostAddress()) + ": " + serverFile);
			String contentType;
			if (request.getGetProperties().containsKey("contenttype")) {
				contentType = request.getGetProperties().get("contenttype");
			} else {
				contentType = getUsedContentType(serverFile);
			}
			ResponseBuilder response = new ResponseBuilder(200, contentType);
			StringBuilder header = response.getHeader();
			header.append("Content-Disposition: inline; filename=\"" + serverFile.getName() + "\"\r\n");
			FileInputStream in = new FileInputStream(serverFile);
			byte[] content = ResourceLoader.getBytes(in);
			in.close();
			if (getContentType(serverFile).startsWith("audio/")) {
				String range;
				if (request.getProperties().containsKey("Range")) {
					range = request.getProperties().get("Range").substring(6);
				} else {
					range = "-";
				}
				if (range.startsWith("-"))
					range = "0" + range;
				if (range.endsWith("-"))
					range = range + content.length;
				header.append("Content-Range: bytes " + range + "\r\n");
				header.append("Accept-Ranges: bytes\r\n");
			}
			if (contentType.startsWith("text/")) {
				String contentstr = new String(content);
				contentstr = contentstr.replace("%clientip%", connectionSocket.getInetAddress().getHostAddress());
				for (Entry<String, String> param : request.getGetProperties().entrySet()) {
					contentstr = contentstr.replace("%?" + param.getKey() + "%", ClientUtils.decode(param.getValue()));
				}
				for (Entry<String, String> param : request.getGetProperties().entrySet()) {
					contentstr = contentstr.replace("%?=" + param.getKey() + "%", param.getValue());
				}
				if (contentstr.contains("%serveronline%")) {
					contentstr = contentstr.replace("%serveronline%", String.valueOf(isMCServerRunning()));
				}
				if (request.getFileName().equals("/song.html")) {
					Map<String, String> postData = request.getGetProperties(); // String(request.getContent()));
					if (postData.containsKey("song") && postData.containsKey("artist")) {
						String artist = postData.get("artist");
						String song = postData.get("song");
						File file = new File(MUSIC, artist + "/" + song + ".mp3");
						if (file.exists()) {
							try {
								MP3File audio = new MP3File(file);
								Tag tag = audio.getTag();
								if (tag.hasField(FieldKey.LYRICS)) {
									String lyrics = tag.getFirst(FieldKey.LYRICS);
									lyrics = lyrics.replace("\n", "<br />");
									contentstr = contentstr.replace("%lyrics%", lyrics);
								} else {
									String lyrics = "No lyrics available";
									lyrics = lyrics.replace("\n", "<br />");
									contentstr = contentstr.replace("%lyrics%", lyrics);
								}
							} catch (TagException e) {
								e.printStackTrace();
							} catch (ReadOnlyFileException e) {
								e.printStackTrace();
							} catch (InvalidAudioFrameException e) {
								e.printStackTrace();
							}
						} else {
							System.err.println("Couldnt find " + file);
						}
					}
				}
				// contentstr = contentstr.replaceAll("%\\?.*?%", "INVALID");
				content = contentstr.getBytes();
			}
			header.append("Content-Length: " + content.length + "\r\n");
			response.writeTo(connectionSocket);
			connectionSocket.getOutputStream().write(content);
		} else {
			if (new File(System.getProperty("user.home") + "\\Desktop\\Minecraft\\Servers\\HTML Files"
					+ request.getFileName() + ".html").exists()) {
				forward(request.getFileName() + ".html", connectionSocket);
				return;
			}
			System.err.println("[" + new Date(System.currentTimeMillis()) + "] "
					+ IPMapping.getName(connectionSocket.getInetAddress().getHostAddress()) + ": " + request.getFileName());
			ResponseBuilder response = new ResponseBuilder(404);
			response.append("<h1>404 Not found</h1>");
			response.appendln("Unable to find file <b>" + request.getFileName() + "</b> on this server :(");
			response.writeTo(connectionSocket);
		}
	}
	
	private static void forward(String target, Socket socket) throws IOException {
		StringBuilder header = new StringBuilder("HTTP/1.0 302 Found\r\n");
		header.append("Location: " + target + "\r\n\r\n");
		socket.getOutputStream().write(header.toString().getBytes());
	}
	
	public static boolean isMCServerRunning() {
		try {
			Socket s = new Socket();
			s.setSoTimeout(100);
			s.connect(new InetSocketAddress(InetAddress.getLocalHost(), 25565));
			s.close();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return false;
	}

	public static SongData downloadSong(String songName, YoutubeDownloader dl) throws IOException {
		return downloadSong(songName, dl);
	}
	public static SongData downloadSong(String songName, YoutubeDownloader dl, File target) throws IOException {
		YoutubeVideo video = Youtube.searchVideo(songName);
		if(video == null) throw new IllegalArgumentException("NO SONG");
		File folder = new File(MUSIC, video.getSongArtist());
		File audio = new File(folder, video.getSongName()+".mp3");
		
		final SongData song;
		if (target.exists() || audio.exists()) {
			song = new SongData();
		} else {
			song = dl.downloadSong(null, video);
			long startTimeCode = System.currentTimeMillis();
			target.getParentFile().mkdirs();
			target.createNewFile();
			FileOutputStream out = new FileOutputStream(target);
			try {
				out.write(song.getSongBytes());
			} catch (IOException e) {
				out.close();
				throw e;
			}
			out.close();
			System.out.println("Took "+(System.currentTimeMillis()-startTimeCode)/1000.0+" seconds to save video");
		}
		song.setArtist(video.getSongArtist());
		song.setSongName(video.getSongName());
		song.setYoutubeSource(video);
		long startTimeCode = System.currentTimeMillis();
		try {
			Runtime.getRuntime().exec("ffmpeg -i \"" + target.getAbsolutePath() + "\" -acodec libmp3lame -aq 4 \""+audio.getAbsolutePath()+"\"").waitFor();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Took "+(System.currentTimeMillis()-startTimeCode)/1000.0+" seconds to convert video to audio");
		try {
			setMP3Tags(audio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return song;
	}
	
	public static void setMP3Tags(File file) throws Exception {
		MP3File audio = new MP3File(file);
		String title = file.getName().substring(0, file.getName().lastIndexOf("."));
		String artist = file.getParentFile().getName();
		Tag tag = new ID3v23Tag();
		audio.setTag(tag);
		tag.setField(FieldKey.TITLE, title);
		tag.setField(FieldKey.ARTIST, artist);
		String shortTitle = title.replaceAll("\\(.*?\\)", "").replaceAll("[.*?]", "").trim();
		String shortArtist = artist.replaceAll("\\(.*?\\)", "").replaceAll("[.*?]", "").trim();
		Client httpClient = new Client();
		httpClient
				.setUserAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		addAlbumArtwork(tag, httpClient, shortTitle, shortArtist);
		addLyrics(tag, httpClient, shortTitle, shortArtist);
		audio.commit();
	}
	
	public static void addLyrics(Tag tag, Client httpClient, String title, String artist) throws KeyNotFoundException,
			FieldDataInvalidException {
		String keyword = artist + " " + title;
		String site = "http://www.metrolyrics.com/api/v1/multisearch/all/X-API-KEY/196f657a46afb63ce3fd2015b9ed781280337ea7?find="
				+ ClientUtils.encode(keyword) + "&limitall=50&extracategories=body";
		String callback = new String(httpClient.readSite(site));
		Matcher matcher = Pattern.compile(
				"\\{\"i\":.*?,\"u\":\"(.*?)\",\"p\":\"(.*?)<br \\\\/>(.*?)\",\"w\":.*?,\"q\":\".*?\"\\}").matcher(
				callback);
		if (matcher.find()) {
			String matchUrl = "http://www.metrolyrics.com/" + matcher.group(1);
			String matchArtist = matcher.group(2).replaceAll("<.*?>", "").replace("\\/", "/").trim();
			
			String matchName = matcher.group(3).replaceAll("<.*?>", "").replace("\\/", "/").replace("\u00c3(c)", "é")
					.trim();
			
			String callback2 = new String(httpClient.readSite(matchUrl));
			Matcher lineMatcher = Pattern.compile("<span class='line line-s' id='line_\\d*?'>(.*?)</span>").matcher(
					callback2);
			
			StringBuilder lyrics = new StringBuilder();
			System.out.println("-loaded " + matchName + " by " + matchArtist + " lyrics");
			while (lineMatcher.find()) {
				String line_enc = lineMatcher.group(1);
				String line = StringEscapeUtils.unescapeHtml4(line_enc).replace("Ã©", "é").replaceAll("<.*?>", "");
				lyrics.append(line).append("\n");
			}
			tag.setField(FieldKey.LYRICS, lyrics.toString());
		} else {
			System.err.println("-unable to load lyrics for " + keyword);
		}
	}
	
	public static void addAlbumArtwork(Tag tag, Client httpClient, String title, String artist)
			throws FieldDataInvalidException {
		String site = "http://www.albumart.org/index.php?skey=" + ClientUtils.encode(title + " by " + artist)
				+ "&itempage=1&newsearch=1&searchindex=Music";
		HttpForm form = new HttpForm(site, "GET");
		String callback = new String(httpClient.httpForm(form));
		Matcher matcher = Pattern
				.compile(
						"<a href=\".*?\" title=\".*?\" target=\"_blank\" onmouseover=\"window\\.status=''; return true\" onmouseout=\"window\\.status=''; return true\"><img src=\"(.*?)\" border=\"0\" class=\"image_border\" width=\"160px\" height=\"160px\" alt=\"\"/></a>")
				.matcher(callback);
		if (matcher.find()) {
			String artworkLink = matcher.group(1).replaceAll("\\._.*?_\\.", ".");
			byte[] imageBytes = httpClient.readSite(artworkLink);
			
			Artwork artwork = ArtworkFactory.getNew();
			artwork.setBinaryData(imageBytes);
			artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(imageBytes));
			artwork.setDescription("Album artwork");
			artwork.setPictureType(0x03);
			tag.setField(artwork);
			System.out.println("-loaded artwork [" + artworkLink + "]");
		} else {
			System.err.println("-unable to find artwork");
		}
		
	}
	
	static {
		MUSIC.mkdirs();
	}
}