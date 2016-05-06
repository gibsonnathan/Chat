import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer implements Runnable
{
	MessageQueue nextMsg;
	Socket socket;
	ChatDaemon daemon;

	BufferedReader input;
	PrintStream output;

	String userName;
	boolean running;

	Database database;

	boolean alive()
	{
		return running;
	}

	public ChatServer(Socket s,ChatDaemon d)
	{
		database = new Database();
		nextMsg = new MessageQueue();
		userName = "Anonymous";
		running = false;
		socket = s;
		daemon = d;

		try
		{
			input=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output=new PrintStream(socket.getOutputStream(),true);
			running=true;
			Thread writeThread=new Thread(this);
			writeThread.start();
			Thread readThread=new Thread(new ReadThread());
			readThread.start();
		}
		catch(IOException e)
		{
			System.out.println("Abnormal chat server socket condition 1:"+e);;
		}
	}

	public void run()
	{
		writeLoop();
	}
	
	class ReadThread implements Runnable
	{
		public void run()
		{
			readLoop();
		}
	}
			

	void readLoop()
	{
		try
		{
			
			while(running)
			{
				String line=input.readLine();
				if(line == null){
					continue;
				}
				System.out.println("Server Received: "+line);

				switch(line.charAt(0))
				{
					case 'i': // login
						login(line);
						break;
					case 'o': // logout
						logout(line);
						break;
					case 'm': // new message
						message(line);
						break;
					case 'v': // invite
						invite(line);
						break;
					case 'x':
						get_users(line); //users in room
						break;
					case 'p':
						validateUser(line); //check if user/pass is valid
						break;
					case 'a':
						addUser(line);
						break;
					default:
				}
			}
		}catch (IOException e) {}
		try
		{		
			input.close();
			output.close();
			socket.close();
		}
		catch (IOException e) {}
		System.out.println(userName+ " logged off...server thread exiting");
	}
		
	void writeLoop()
	{
		while(running)
		{
			String s=nextMsg.get();
			if (s.charAt(0)!='d')
			{
				System.out.println("Server sent :"+s);
				output.println(s);
			}
		}
	}

	void addUser(String line){
		StringTokenizer t = new StringTokenizer(line);
		t.nextToken();
		String user = t.nextToken();
		String pass = t.nextToken();
		database.addUser(user, pass);
	}

	void validateUser(String line){
		StringTokenizer t = new StringTokenizer(line);
		t.nextToken();
		String user = t.nextToken();
		String pass = t.nextToken();
		System.out.println(user + " " + pass);
		boolean result = database.valid(user, pass);
		nextMsg.put("p " + user + " " + pass + " " + result);
	}

	void login(String line){
		daemon.shrMsg.put(line);
		userName=line.substring(2);
		String userListMsg="u";
		for(int i=0;i<daemon.numUsers;i++)
			if (daemon.user[i].alive())
				userListMsg+= " "+daemon.user[i].userName;
		nextMsg.put(userListMsg);
	}

	void logout(String line){
		daemon.shrMsg.put(line);
		running = false;
	}

	void message(String line){
		daemon.shrMsg.put(line);
	}

	void invite(String line){
		daemon.createNewRoom(line);
	}

	void get_users(String line){
		String[] message = line.split(" "); 
		String result = daemon.getUsers(Integer.parseInt(message[1]));
		result = "x " + result;
		nextMsg.put(result);
	}

}
