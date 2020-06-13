import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;


public class ChatClient {
	Map<String, Socket> clientList = new Hashtable<String,Socket>();

	//Allows threads to send and recieve messages
	public class MessageHandler implements Runnable {
		Map<String, Socket> clients;
		DataOutputStream output;
		DataInputStream input;
		Socket cSocket;
		String address, type;
		int port;

		public MessageHandler(DataOutputStream output, DataInputStream input, Socket socket, String type, Map<String,Socket> clients) {
			this.output = output;
			this.input = input;
			this.cSocket = socket;
			this.type = type;
			this.clients = clients;
		}

		@Override
		public void run() {
			//Determines which thread type we need to create
			if(type.equals("send")) {
				send();
			} else if (type.equals("receive")) {
				receive();
			}
			
			try{
				cSocket.close();
			} catch(IOException e){
				System.out.println(e.getMessage());
			}


		}

		
		public void send() {
			try {
				BufferedReader reader= new BufferedReader(new InputStreamReader(System.in));
				// input the message from standard input
				String message;
				while((message = reader.readLine()) != null) {
					output.writeUTF( message );
				}	
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

		public void receive() {
			try {
				// input the message from standard input
				String message;
				while((message = input.readUTF())!= null) {
					System.out.println(message);
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}




	}


	public void runClient(int port, String address) {
		try {
			BufferedReader name= new BufferedReader(new InputStreamReader(System.in));
			Socket cSocket= new Socket( address, port );
			//Init data output/input streams
			DataOutputStream output = new DataOutputStream( cSocket.getOutputStream() );
			DataInputStream input = new DataInputStream( cSocket.getInputStream() );	
		//	System.out.println("Waiting for request from server...");
		//	System.out.println(input.readUTF());
			output.writeUTF(name.readLine());
			//System.out.println("Sending name to server...");
			//Init MessageHandler
			MessageHandler send = new MessageHandler(output, input, cSocket, "send", clientList);
			MessageHandler receive = new MessageHandler(output, input, cSocket,"receive", clientList);
			//Init threads
			Thread senderThread = new Thread(send);
			Thread receiverThread = new Thread(receive);
			//Start threads
			senderThread.start();
			receiverThread.start();
		} catch(EOFException eof) {
			System.out.println("EOF encountered; other side shut down");
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public static void main(String[] args) {
		ChatClient messenger = new ChatClient();
		//Default address
		String address = "localhost";
		if(args.length == 1) {
			int port = Integer.valueOf(args[0]);
			messenger.runClient(port,address);
		}
	}

}
