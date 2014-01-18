import java.util.HashMap;
import java.util.Map;

public class IPMapping {
	protected static Map<String, String> ipToNameMap = new HashMap<String, String>();
	static {
		ipToNameMap.put("72.23.193.150", "Karry");
		ipToNameMap.put("72.23.198.69", "Me");
	}
	
	public static String getName(String ip) {
		String name = ipToNameMap.get(ip);
		if (name == null)
			return ip;
		return name;
	}
}
