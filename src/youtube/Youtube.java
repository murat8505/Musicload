package youtube;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;
import com.coleman.utilities.http.HttpForm;

public class Youtube {
	public static YoutubeVideo searchVideo(String search) {
		if(search.startsWith("youtube.com"))
			search = "www." + search;
		if(search.startsWith("www."))
			search = "http://" + search;
		
		String gdataURL = null;
		boolean isSpecific = false;
		if(search.startsWith("http://www.youtube.com") || search.startsWith("https://www.youtube.com")) {
			Matcher m = Pattern.compile("\\?v=(.*?)($|&)").matcher(search);
			if(m.find()) {
				System.out.println(m.group(2));
				gdataURL = "https://gdata.youtube.com/feeds/api/videos/" + m.group(1) + "?v=2&alt=json";
				isSpecific = true;
			}
		}
		ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
		long startTimeCode = System.currentTimeMillis();
		if(!isSpecific) {
			search = ClientUtils.encode(search.replace(' ', '+'));
			Client c = new Client();
			c.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			String url = "https://gdata.youtube.com/feeds/api/videos?v=2";
			url+="&key=AIzaSyABOH8EWlzcCR3OqzELJAhxYBdU-ncC7yk";
			url+="&category=Music";
			url+="&orderby=relevance";
			url+="&max-results=5";
			url+="&alt=json";
			url+="&q=" + search;
			System.out.println(url);
			
			HttpForm form = new HttpForm(url, "GET");
			String callback = new String(c.httpForm(form));
			JSONObject json = new JSONObject(new JSONTokener(callback));
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for(int i=0;i<entries.length();i++) {
				JSONObject entry = entries.getJSONObject(i);
				JSONObject mediagroup = entry.getJSONObject("media$group");
				String title = mediagroup.getJSONObject("media$title").getString("$t");
				String videoid = mediagroup.getJSONObject("yt$videoid").getString("$t");
				YoutubeVideo video = new YoutubeVideo("http://www.youtube.com/watch?v=" + videoid, videoid, null, null, title);
				videos.add(video);
			}
		} else {
			Client c = new Client();
			c.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			String url = gdataURL;
			System.out.println(url);
			
			HttpForm form = new HttpForm(url, "GET");
			String callback = new String(c.httpForm(form));
			JSONObject json = new JSONObject(new JSONTokener(callback));
			JSONObject entry = json.getJSONObject("entry");
			JSONObject mediagroup = entry.getJSONObject("media$group");
			String title = mediagroup.getJSONObject("media$title").getString("$t");
			String videoid = mediagroup.getJSONObject("yt$videoid").getString("$t");
			YoutubeVideo video = new YoutubeVideo("http://www.youtube.com/watch?v=" + videoid, videoid, null, null, title);
			videos.add(video);
		}
		System.out.println("Took "+(System.currentTimeMillis()-startTimeCode)/1000.0+" seconds to find video");
		return videos.get(0);
	}
}
