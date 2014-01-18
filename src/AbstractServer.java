import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import utilities.database.Database;
import utilities.database.FileDatabase;
import utilities.Session;

public abstract class AbstractServer {
	protected int port;
	
	private FileDatabase<String, Object> dataBase;
	private PortForwarder portForwarder1;
	
	public AbstractServer(int port) {
		this.portForwarder1 = new PortForwarder(port);
		portForwarder1.register();
		this.port = port;
		dataBase = new FileDatabase<String, Object>(getDataFile("database.dat"));
		dataBase.readFromFile();
		dataBase.addDefault("sessions", new Database<UUID, Session>());
		dataBase.addDefault("support", new ArrayList<InetAddress>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dataBase.writeToFile();
				AbstractServer.this.terminate();
			}
		});
	}
	
	private ServerSocket serversocket;
	private ThreadPoolExecutor threadHandler;
	
	public void launch() {
		
		int cpus = Runtime.getRuntime().availableProcessors();
		int maxThreads = cpus * 64 * 4;
		maxThreads = (maxThreads > 0 ? maxThreads : 1);
		
		try {
			serversocket = new ServerSocket(port, maxThreads);
		} catch (BindException e) {
			System.err.println("Server already running on port " + port);
			return;
		} catch (IOException e) {
			System.err.println("Error!");
			e.printStackTrace();
			return;
		}
		threadHandler = new ThreadPoolExecutor(maxThreads, maxThreads, 2, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(maxThreads, true));
		System.err.println("Server launched on port " + port);
		while (!serverTerminated) {
			try {
				final Socket connectionSocket = serversocket.accept();
				threadHandler.submit(new Thread() {
					public void run() {
						try {
							handleConnection(connectionSocket);
						} catch (SocketException e) {
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								connectionSocket.close();
							} catch (IOException e) {
							}
						}
					}
				});
			} catch (IOException e) {
				if (!serverTerminated) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected abstract void handleConnection(Socket connectionSocket) throws IOException;
	
	private boolean serverTerminated = false;
	
	public void terminate() {
		serverTerminated = true;
		if (serversocket != null) {
			try {
				serversocket.close();
			} catch (IOException e) {
			}
		}
		portForwarder1.unregister();
	}
	
	public FileDatabase<String, Object> getDatabase() {
		return dataBase;
	}
	
	public static File getDataFile(String name) {
		File file = new File(getServer(), name);
		if (!file.exists())
			makeFile(file);
		return file;
	}
	
	public static File getServer() {
		File file = new File( System.getProperty("user.home")
				+ "\\Desktop\\Java Server\\");
		if (!file.exists())
			makeFile(file);
		return file;
	}
	
	private static void makeFile(File file) {
		if (file.getParentFile() != null && !file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try {
			if (file.isDirectory())
				file.mkdirs();
			else
				file.createNewFile();
		} catch (IOException e) {
		}
	}
}
