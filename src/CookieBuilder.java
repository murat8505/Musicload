public class CookieBuilder {
	private StringBuilder cookieString = new StringBuilder();
	
	public void addCookie(String cookieName, String cookieValue, String domain, String path, String expiration) {
		cookieString.append("Set-Cookie: ");
		cookieString.append(cookieName).append("=").append(cookieValue);
		if (domain != null)
			cookieString.append("; Domain=").append(domain);
		if (path != null)
			cookieString.append("; Path=").append(path);
		if (expiration != null)
			cookieString.append("; Expires=").append(expiration);
		cookieString.append("\r\n");
	}
	
	public String toString() {
		return cookieString.toString();
	}
}