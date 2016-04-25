import java.net.*;
import java.io.*;
import java.util.*;

public class ChatDaemon implements Runnable
{
	static final int maxUsers=20;
	static final int maxRooms=20;
	static final int portNumber=1666;
	static int numUsers=0;
	static int numRooms=0;
	ChatServer[] user;
	int [][] rooms; 
	Thread me;

	MessageQueue shrMsg;

	public static void main(String[] argv)
	{
		ChatDaemon cd=new ChatDaemon();
	}

	public ChatDaemon()
	{
		shrMsg=new MessageQueue();
		user=new ChatServer[maxUsers];
		rooms=new int[maxRooms][maxUsers];
		me=new Thread(this);
		me.start();

		try
		{
			listenLoop();
		}
		catch(IOException e)
		{
			System.out.println("Abnormal socket condition:"+e);
		}
	}

	public void listenLoop() throws IOException
	{
		ServerSocket ss=new ServerSocket(portNumber);
		Socket chatSocket=null;
		System.out.println("Chat Server listening for connections..");
		while(true)
		{
			chatSocket=ss.accept();
			ChatServer ct=allocServerThread(chatSocket);
			synchronized(System.out)
			{
				System.out.println("Allocated new chat server");
			}
		}
	}

	ChatServer allocServerThread(Socket s)
	{
		if(numUsers<maxUsers)
		{
			ChatServer ct=new ChatServer(s,this);
			user[numUsers++]=ct;
			return ct;
		}
		else
			return null;
	}

	public void run()
	{
		sendMessageLoop();
	}

	public void sendMessageLoop()
	{
		while (true)
		{
			String mes=shrMsg.get();

			if (mes.charAt(0)=='m') // message
			{
				StringTokenizer t= new StringTokenizer(mes);
				t.nextToken(); // to move across command
				int roomid;
				try {
					roomid=Integer.parseInt(t.nextToken());
					for (int i=1;i<=rooms[roomid][0];i++)
						if (user[rooms[roomid][i]].alive())
							user[rooms[roomid][i]].nextMsg.put(mes);
				} catch (Exception e) 
				{
					e.printStackTrace();
					System.exit(-1);
				}
			}
			else if (mes.charAt(0)!='d')
			{
				for (int i=0;i<numUsers;i++)
					if (user[i].alive())
					{
						user[i].nextMsg.put(mes);
					}
			}
		}
	}

	synchronized public String getUsers(int room){
		ArrayList<Integer> roomids = new ArrayList<Integer>();
		for(int i = 1; i < rooms[room][0] + 1; i++){
			roomids.add(rooms[room][i]);
		}
		String results = "";
		for (int i = 0; i < roomids.size(); i++){
			results += user[roomids.get(i)].userName + " ";
		}
		return results;
	}
	
	synchronized public void createNewRoom(String mes)
	{
		StringTokenizer t= new StringTokenizer(mes);
		t.nextToken(); 
		if (numRooms==maxRooms)
			return;
		String roomName=t.nextToken();
		String host=t.nextToken();
		String invMessage="v "+host+" "+numRooms+" "+roomName;
		int j=0;
		while (t.hasMoreTokens())
		{
			String nextUser=t.nextToken();
			for (int i=0;i<numUsers;i++)
			{
				if (user[i].alive() && user[i].userName.equals(nextUser))
				{
					rooms[numRooms][++j]=i;
					user[i].nextMsg.put(invMessage);
				}
			}
			rooms[numRooms][0]=j;
		}
		numRooms++;
	}
}
