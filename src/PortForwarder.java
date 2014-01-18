import java.net.InetAddress;
import java.net.UnknownHostException;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;
import org.teleal.cling.support.model.PortMapping.Protocol;

public class PortForwarder {
	protected UpnpService upnpService;
	protected PortMapping portMapping;
	
	static {
		
	}
	
	public PortForwarder(int port) {
		this(port, port);
	}
	
	public PortForwarder(int externalPort, int internalPort) {
		portMapping = new PortMapping();
		portMapping.setInternalPort(new UnsignedIntegerTwoBytes(externalPort));
		portMapping.setExternalPort(new UnsignedIntegerTwoBytes(internalPort));
		portMapping.setProtocol(Protocol.TCP);
		try {
			portMapping.setInternalClient(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (upnpService != null)
					unregister();
			}
		});
	}
	
	public void register() {
		if (upnpService != null)
			throw new IllegalStateException(
					"This port forwarder already has a service running, cancel it with unregister first");
		upnpService = new UpnpServiceImpl(new PortMappingListener(portMapping));
		upnpService.getControlPoint().search();
	}
	
	public void unregister() {
		upnpService.shutdown();
		upnpService.getRouter().shutdown();
		upnpService = null;
	}
	
	public UpnpService getUpnpService() {
		return upnpService;
	}
	
	public PortMapping getPortMapping() {
		return portMapping;
	}
	
	public void setExternalPort(int externalPort) {
		portMapping.setExternalPort(new UnsignedIntegerTwoBytes(externalPort));
	}
	
	public void setInternalPort(int internalPort) {
		portMapping.setInternalPort(new UnsignedIntegerTwoBytes(internalPort));
	}
	
	public int getInternalPort() {
		return portMapping.getInternalPort().getValue().intValue();
	}
	
	public int getExternalPort() {
		return portMapping.getExternalPort().getValue().intValue();
	}
}
