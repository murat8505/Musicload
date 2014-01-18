import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;


public class Lyrics {
	public static Client c = new Client();
	public static String search(Track track) throws EchoNestException {
		return search(track.getTitle(), track.getArtistName());
	}
	public static String search(String song, String artist) {
		song = ClientUtils.encode(song);
		artist = ClientUtils.encode(artist);
		String url = String.format("http://lyricalizer.ac3xx.com/?song=%s&artist=%s", song, artist);
		byte[] bytes = c.readSite(url);
		if(bytes == null) {
			return null;
		}
		String callback = new String(bytes).replace("’", "'");
		if(callback.equals("No lyrics found.\n")) {
			return null;
		}
		System.out.println(callback);
		return callback;
		//System.out.println("LYRICS: " + callback);
		//Pattern p = Pattern.compile("<Lyric>(.*?)</Lyric>", Pattern.DOTALL);
		//Matcher m = p.matcher(callback);
		//if(m.find()) {
		//	return m.group(1).replace("’", "'");
		//}
		//return null;
	}
}
