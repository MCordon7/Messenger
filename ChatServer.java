import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class ChatServer {
	Map<Integer, ClientInfo> clientList = new Hashtable<Integer,ClientInfo>();

	//Keeps track of all client data
	public class ClientInfo {
		Socket cSocket;
		DataOutputStream output;
		DataInputStream input;
		String name;

		public ClientInfo(Socket s, DataOutputStream o, DataInputStream i, String n) {
			this.cSocket = s;
			this.output = o;
			this.input = i;
			this.name = n;
		}
	}

	//Allows threads to send and recieve messages
	public class MessageHandler implements Runnable {
		DataOutputStream output;
		DataInputStream input;
		Socket cSocket;
		String address;
		int port;
		boolean newClient = true;

		public MessageHandler(DataOutputStream output, DataInputStream input, Socket socket) {
			this.output = output;
			this.input = input;
			this.cSocket = socket;
		}

		@Override
		public void run() {
			//Determines which thread type we need to create
			try {
				// input the message from standard input
				String message;
				if(newClient){
				String name = input.readUTF();
				clientList.put(cSocket.getPort(), new ClientInfo(cSocket, output, input, name));
				newClient = false;
				}
				Entry<Integer,ClientInfo> send;
				Iterator<Entry<Integer,ClientInfo>> nameIter = clientList.entrySet().iterator();
				while((message = input.readUTF())!= null) {
					//Resets iteration of names
					nameIter = clientList.entrySet().iterator();
					while(nameIter.hasNext()) {
						send = nameIter.next();
						//Looks through table of clientList, if the port matches we don't have to send
						//cSocket = original sender's name; send.getValue().cSocket = everyone else
						if(send.getValue().cSocket != cSocket) {
							send.getValue().output.writeUTF(clientList.get(cSocket.getPort()).name + ": " + message);
						} 

					}
				}
			} catch(EOFException eof) {
				//If a client disconnects, we can remove them from the list
				clientList.remove(cSocket.getPort());
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public void runServer(int port) {
		try {
			//Init Sockets
			ServerSocket sSocket= new ServerSocket( port );
			while(true) {

				Socket cSocket= sSocket.accept();
				//Init data output/input streams
				DataOutputStream output= new DataOutputStream( cSocket.getOutputStream() );
				DataInputStream input= new DataInputStream( cSocket.getInputStream() );	
				//output.writeUTF("What is your name?");
				MessageHandler receive = new MessageHandler(output, input, cSocket);
				//Init threads
				Thread receiverThread = new Thread(receive);
				//Start threads
				receiverThread.start();
			}

		} catch(EOFException eof) {
			System.out.println("EOF encountered; other side shut down");
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public static void main(String[] args) {
		ChatServer messenger = new ChatServer();
		if(args.length == 1) {
			int port = Integer.valueOf(args[0]);
			messenger.runServer(port);
		}
	}

}
