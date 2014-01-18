package youtube;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.DownloadProgressHandler;

public class YoutubeMP3 extends YoutubeDownloader {
	private static final int __AM = 65521;
	private int cc(String a) {
		int c = 1, b = 0, d, e;
		for (e = 0; e < a.length(); e++) {
			d = a.charAt(e);
			c = (c + d) % __AM;
			b = (b + c) % __AM;
		}
		return b << 16 | c;
	}
	@Override
	public String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video) {
		String url = "http://www.youtube-mp3.org/a/pushItem/";
		url += "?item=http%3A//www.youtube.com/watch%3Fv%3D"
				+ video.getVideoId();
		url += "&el=na";
		url += "&bf=false";
		url += "&r=" + System.currentTimeMillis();
		byte[] b = c.readSite(url);
		if(b == null) {
			if(dph != null) {
				dph.downloadFailed("Invalid conversion protocol, FIX THIS ALEX!");
				return null;
			}
		}
		String status = null;
		String h = null;
		while (h == null) {
			String callback = new String(
					c.readSite("http://www.youtube-mp3.org/a/itemInfo/"
							+ "?video_id=" + video.getVideoId()
							+ "&ac=www&t=grp&r=" + System.currentTimeMillis()));
			if(callback.equals("pushItemYTError();"))
				throw new IllegalArgumentException("Video is not allowed to be pirated");
			callback = callback.substring(7);
			JSONObject json = new JSONObject(new JSONTokener(callback));
			status = json.getString("status");
			String pf = json.getString("pf");
			if (status.equals("serving")) {
				h = json.getString("h");
			} else
				try {
					System.out.println("Pinging...");
					System.out.println(new String(c.readSite(pf)));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		// http://www.youtube-mp3.org/get?ab=128&video_id=AZrbW9UT1vE&h=49084a7a143ae3c5136f4e48ae410e9a&r=1386556301051.1457915441
		long a = System.currentTimeMillis();
		String r = a + "." + cc(video.getVideoId() + a);
		String downloadLink = "http://www.youtube-mp3.org/get?ab=128&video_id="
				+ video.getVideoId() + "&h=" + h + "&r="
				+ r;
		return downloadLink;
	}

}
