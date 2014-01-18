import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ResponseBuilder {
	private StringBuilder header;
	private StringBuilder body;
	
	public ResponseBuilder(int returnCode) {
		this(returnCode, null);
	}
	
	public ResponseBuilder(int returnCode, String contentType) {
		body = new StringBuilder();
		header = new StringBuilder("HTTP/1.0 ");
		switch (returnCode) {
			case 200:
				header.append("200 OK");
				break;
			case 400:
				header.append("400 Bad Request");
				break;
			case 403:
				header.append("403 Forbidden");
				break;
			case 404:
				header.append("404 Not Found");
				break;
			case 405:
				header.append("405 Method Not Allowed");
			case 500:
				header.append("500 Internal Server Error");
				break;
			case 501:
				header.append("501 Not Implemented");
				break;
			default:
				header.append(returnCode).append(" Error");
		}
		header.append("\r\n");
		header.append("Connection: close\r\n");
		header.append("Server: Alexs Server\r\n");
		if (contentType != null)
			header.append("Content-Type: " + contentType + "\r\n");
	}
	
	public void append(String str) {
		body.append(str);
	}
	
	public void appendln(String str) {
		body.append(str).append("\n");
	}
	
	public StringBuilder getHeader() {
		return header;
	}
	
	public void setContent(byte[] bytes) {
		setContent(new String(bytes));
	}
	
	public void setContent(String str) {
		this.body = new StringBuilder(str);
	}
	
	public ResponseBuilder addCookies(CookieBuilder cookieBuilder) {
		header.append(cookieBuilder.toString());
		return this;
	}
	
	public String toString() {
		return header.toString() + "\r\n" + body;
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write((header.toString() + "\r\n").getBytes());
		out.write(this.body.toString().getBytes());
	}
	
	public void writeTo(Socket socket) throws IOException {
		writeTo(socket.getOutputStream());
	}
}
