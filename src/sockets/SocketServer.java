package sockets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import bridge.DataStream;

/**
 * A simple server that handled socket. One user at a time, but could be modified
 * to handle more, if that makes sense for your application.
 * 
 * @author platisd
 */
public class SocketServer implements Runnable, DataStream{
	private ServerSocket server;
	private int serverPort = 8088; //the default port this server instance will listen to
	private Socket socket;	
	private PrintWriter out;
	private boolean socketInitialized = false; //if this is false, it means that there is not connection atm
	private BlockingQueue<String> socketData = new PriorityBlockingQueue<String>(); //FIFO data structure to save the incoming data


	/**
	 * The default constructor of the SocketServer. The server uses the default port
	 * (serverPort) to listen for incoming connections.
	 */
	public SocketServer() {
	}
	
	/**
	 * Constructor of the SocketServer in case you want to provide a specific
	 * port for the server to listen for incoming connections.
	 * @param the port number for the server to listen for connections.
	 */
	public SocketServer(int serverPort){
		this.serverPort = serverPort;
	}

	public void write(String s){
		if (isConnected()) out.println(s);
	}

	/**
	 * Indicates whether a user is connected to the socket or not.
	 * @return true if a user has been connected to the socket, false otherwise.
	 */
	public boolean isConnected(){
		return socketInitialized;
	}

	public String read(){
		try {
			return socketData.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void run(){ //this will run in parallel to the main thread
		try {
			System.out.println("Opened port " + serverPort + " and waiting");
			server = new ServerSocket(serverPort); //initialize a new connection (if port already in use an error will be thrown)
			while (true) {
				socketInitialized = false;
				socket = server.accept(); //wait until a user is connected
				socketInitialized = true;
				System.out.println("Got a user connection!");
				out = new PrintWriter(socket.getOutputStream(), true); //out will now write to the particular socket
				write("Hi, you are connected"); //welcome message sent to the connected client
				BufferedReader reader = new BufferedReader(new InputStreamReader((socket.getInputStream())));
				String input;
				while ((input = reader.readLine()) != null) {
					//System.out.println("Bridge received from socket: " + input); //uncomment if you want to print whatever is being received
					//write("You wrote: " + input); //uncomment if you want to echo back to the client whatever is being received
					socketData.put(input); //save the data in order to be transmitted to the serial port
				}
			}
		} catch (BindException e) {
			System.out.println("Port already in use. Start another server with a different"
					+ " port using the setPort(port) command");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
