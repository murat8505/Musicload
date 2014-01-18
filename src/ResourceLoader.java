import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceLoader {
	public static void readResource(String resourcePath, OutputStream target) {
		InputStream input = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
		try {
			transferStream(input, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] readResource(String resourcePath) {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		readResource(resourcePath, o);
		return o.toByteArray();
	}
	
	public static void transferStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[10240];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	}

	public static byte[] getBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transferStream(in, out);
		return out.toByteArray();
	}
	public static byte[] getBytes(InputStream in, int start, int end) throws IOException {
		byte[] bytes = getBytes(in);
		byte[] trimmed = new byte[end - start + 1];
		System.arraycopy(bytes, start, trimmed, 0, start - end + 1);
		return trimmed;
	}
}
