package youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coleman.utilities.http.Client;

public class YoutubeVideo {
	protected String videoUrl;
	protected String videoId;
	protected String songArtist;
	protected String songName;
	protected String videoTitle;
	public YoutubeVideo(String videoUrl, String videoId, String songArtist, String songName, String videoTitle) {
		this.videoId=videoId;
		this.videoUrl=videoUrl;
		this.songArtist=songArtist;
		this.songName=songName;
		this.videoTitle=videoTitle;
	}
	public YoutubeVideo(String videoUrl, String songArtist, String songName) {
		this.videoUrl=videoUrl;
		this.videoId = videoUrl.substring(videoUrl.indexOf("?v=")+3);
		this.videoId = videoId.substring(0, videoId.indexOf("&") > 0 ? videoId.indexOf("&") : videoId.length());
		this.songArtist=songArtist;
		this.songName=songName;
	}
	public YoutubeVideo(String videoUrl) {
		this.videoUrl = videoUrl;
		this.videoId = videoUrl.substring(videoUrl.indexOf("?v=")+3);
		this.videoId = videoId.substring(0, videoId.indexOf("&") > 0 ? videoId.indexOf("&") : videoId.length());
		Client c = new Client();
		String html = new String(c.readSite(videoUrl));
		Pattern artistPattern = Pattern
				.compile("<span class=\"metadata-info-title\">\nArtist<br />\n    </span>\n      (<.*?>)?(.*?)(<.*?>)?\n  </span>");
		Pattern songNamePattern = Pattern.compile("<span class=\"metadata-info(-title)?\">\n?Buy \"(.*?)\"");
		Pattern videoTitlePattern = Pattern.compile("<span id=\"eow-title\" class=\".*?\" dir=\"ltr\">\n    (.*?)");
		Matcher artistMatcher = artistPattern.matcher(html);
		Matcher songNameMatcher = songNamePattern.matcher(html);
		Matcher videoTitleMatcher = videoTitlePattern.matcher(html);
		if (!songNameMatcher.find()) {
			//return;
			throw new IllegalArgumentException("Video URL " + videoUrl + "is not a song; no name found");
		}
		this.songName = songNameMatcher.group(2).replace("&#39;", "'").replace("Karaoke", "").replace(" - ", "")
				.replace("Live", "").replace("()", "").trim();
		this.songName = songName.replace("Ã©", "é");
		this.songName = songName.replace("/", "-").replace("\\", "-");
		if (!artistMatcher.find()) {
			this.songArtist = "Unknown";
		} else
			this.songArtist = artistMatcher.group(2).replace("&#39;", "'").replaceAll("\\(.*?\\)", "");
		this.songArtist = songArtist.replace("/", "-").replace("\\", "-");
		int ftIndex = this.songArtist.indexOf("ft.");
		if (ftIndex == -1)
			ftIndex = this.songArtist.indexOf("featuring");
		if (ftIndex == -1)
			ftIndex = this.songArtist.indexOf("Featuring");
		if (ftIndex == -1)
			ftIndex = this.songArtist.indexOf("Feat.");
		if (ftIndex == -1)
			ftIndex = this.songArtist.indexOf("feat.");
		if (ftIndex != -1)
			this.songArtist = this.songArtist.substring(0, ftIndex);
		this.songArtist = this.songArtist.trim();
		if (videoTitleMatcher.find())
			this.videoTitle = videoTitleMatcher.group(1).replace("&#39;", "'");
	}
	
	public String getSongArtist() {
		return songArtist;
	}
	
	public String getSongName() {
		return songName;
	}
	
	public String getVideoTitle() {
		return videoTitle;
	}
	
	public String getVideoUrl() {
		return videoUrl;
	}
	
	public String getVideoId() {
		return videoId;
	}
	
	public String toString() {
		return songName + " by " + songArtist + " [" + videoUrl + "]";
	}
}
