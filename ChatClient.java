import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ChatClient extends JFrame implements Runnable 
{
	Socket socket;
	
	Room currentRoom=null;
	DefaultListModel myRooms = new DefaultListModel();
	DefaultListModel onlineUsers = new DefaultListModel();
	DefaultListModel currentUsersModel = new DefaultListModel();
	
	String myPassword;

	BufferedReader input;
	PrintStream output;

	JTextArea txtMessages;
	JList lstRooms;
	JList lstUsers;
	JList lstCurrent;
	JTextField txtSend;
	JTextField userNameTxt = new JTextField(10);
	JPasswordField passwordTxt = new JPasswordField(10);
	JTextField portNumberTxt = new JTextField(10);
	JTextField IPAddressTxt = new JTextField(10);
	JPasswordField signUpPasswordTxt = new JPasswordField(10);	
	JLabel lbl4 = new JLabel("");
	JPanel logInPanel = new JPanel();

	boolean active;
	
	public static void main(String[] argv) throws IOException
	{
		InetAddress clientAddr = InetAddress.getLocalHost();


		ChatClient cc = new ChatClient(clientAddr); 
		cc.pack();
		cc.setLocation(15,1);
		cc.setVisible(true);
	}

	public ChatClient(InetAddress adx) throws IOException
	{

		super("Chat Client");
	
		int portNumber=1666;
		boolean loop = true;

		System.out.println("Connected....starting GUI...");

		Object[] options = {"Sign In","Sign up","Exit"};
		
		JLabel lbl1 = new JLabel("Room Messages");
		JLabel lbl2 = new JLabel("Your Rooms");
		JLabel lbl3 = new JLabel("Online Users");
		JLabel lbl5 = new JLabel("Users in Room");
		JLabel lbl6 = new JLabel("");
		JLabel lbl7 = new JLabel("IP Address");
		JLabel lbl8 = new JLabel("Port Number");
		JLabel lbl9 = new JLabel("");
		JLabel lbl10 = new JLabel("");
		
		JButton signUpbtn = new JButton("Sign Up");
		JButton logInbtn = new JButton("Log In");
		JButton exitbtn = new JButton("Exit");
	
		
		logInPanel.setLayout(new GridLayout(5,3));
		logInPanel.add(new JLabel("Username:"));
		logInPanel.add(userNameTxt);
		logInPanel.add(Box.createHorizontalStrut(15));
		logInPanel.add(new JLabel("Password: "));	
		logInPanel.add(passwordTxt);
		logInPanel.add(lbl9);
		logInPanel.add(lbl7);
		logInPanel.add(IPAddressTxt);
		logInPanel.add(lbl10);
		logInPanel.add(lbl8);
		logInPanel.add(portNumberTxt);
		logInPanel.add(lbl6);
		logInPanel.add(logInbtn);
		logInPanel.add(signUpbtn);
		logInPanel.add(exitbtn);
					
		lbl4.setVisible(false);
		txtMessages = new JTextArea();
		txtMessages.setEditable(false);
		txtMessages.setLineWrap(true);
		txtMessages.setWrapStyleWord(true);

		JScrollPane sclMessages = new JScrollPane(txtMessages);
		sclMessages.setPreferredSize(new Dimension(250, 250));
		
		lstRooms=new JList(myRooms);
		lstRooms.addListSelectionListener(new ListListener());
		JScrollPane sclRooms = new JScrollPane(lstRooms);
		sclRooms.setPreferredSize(new Dimension(100, 250));

		lstUsers=new JList(onlineUsers);
		JScrollPane sclUsers = new JScrollPane(lstUsers);
		sclUsers.setPreferredSize(new Dimension(100, 250));
		
		
        lstCurrent = new JList(currentUsersModel);
        JScrollPane sclCurrent = new JScrollPane(lstCurrent);
        sclCurrent.setPreferredSize(new Dimension(100,250));

		txtSend = new JTextField(20);
		JButton btnSend = new JButton("Send");
		JButton btnCreate = new JButton("Create Room");
		JButton btnInstruction = new JButton("Instructions");
		btnInstruction.setActionCommand("Instruction");		
		logInbtn.setActionCommand("Log In");
		exitbtn.setActionCommand("Exit");
		signUpbtn.setActionCommand("Sign Up");
		btnSend.setActionCommand("Send");
		btnCreate.setActionCommand("Create");
		MyActionListener listener=new MyActionListener();
		btnSend.addActionListener(listener);
		btnCreate.addActionListener(listener);
		btnInstruction.addActionListener(listener);
		logInbtn.addActionListener(listener);
		signUpbtn.addActionListener(listener);
		exitbtn.addActionListener(listener);

		GriddedPanel mainPanel = new GriddedPanel();
		mainPanel.setBorder( new EmptyBorder( new Insets( 2, 2, 2, 2 ) ) );
        mainPanel.addComponent(lbl1,        1,0,5,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
        mainPanel.addComponent(lbl2,        1,6,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
        mainPanel.addComponent(lbl3,        1,7,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
        mainPanel.addComponent(lbl4,        0,0,1,1, GridBagConstraints.WEST,GridBagConstraints.NONE);
		mainPanel.addComponent(lbl5,        1,5,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE);
        mainPanel.addComponent(sclMessages, 2,0,5,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
        mainPanel.addComponent(sclRooms,    2,6,1,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
        mainPanel.addComponent(sclUsers,    2,7,1,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
        mainPanel.addComponent(sclCurrent,  2,5,1,3,GridBagConstraints.WEST,GridBagConstraints.BOTH);
        mainPanel.addComponent(txtSend,     5,0,5,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
        mainPanel.addComponent(btnSend,     5,5,1,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
        mainPanel.addComponent(btnCreate,   5,6,1,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
		mainPanel.addComponent(btnInstruction,   5,7,1,1,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL);
        getContentPane().add( BorderLayout.CENTER, mainPanel );
		WindowListener wndCloser = new WindowAdapter()
		{
			public void	windowClosing(WindowEvent e)
			{
				JFrame f=(JFrame)e.getSource();
				f.dispose();
				active=false;
				output.println("o "+userNameTxt.getText());
				System.exit(1);
			}
		};


		addWindowListener( wndCloser );
		pack();
		setLocation(500,250);
		setVisible(true);
		
		JOptionPane.showOptionDialog(null,logInPanel,"Chat Client",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
		
		do{
			try{
				if(!IPAddressTxt.getText().equals("")){	
					adx = InetAddress.getByName(IPAddressTxt.getText());
				}
				if (!portNumberTxt.getText().equals("")){
					portNumber = Integer.parseInt(portNumberTxt.getText());
				}
				break;
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"Invalid input. Please try again", "Chat Client", JOptionPane.ERROR_MESSAGE);
				
				
				JOptionPane.showOptionDialog(null,logInPanel,"Chat Client",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
			}
		}

		while(true);
		
		try
		{
			socket=new Socket(adx,portNumber);
			input =new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output=new PrintStream(socket.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("Could not connect to the server...exiting");;
			System.exit(-1);
		}
		

		while( (passwordTxt.getPassword().equals("") || userNameTxt.getText().equals("")))
		{
			JOptionPane.showMessageDialog(null,"Please enter a username and password ","Chat Client",JOptionPane.ERROR_MESSAGE);
			JOptionPane.showOptionDialog(null,logInPanel,"Chat Client",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
	
		}

		while(true){

			output.println("p " + userNameTxt.getText() + " " + passwordTxt.getText());

			String line=input.readLine();
			if (line==null)
				continue;

			System.out.println("Client Received:"+line);
			StringTokenizer t = new StringTokenizer(line);
			t.nextToken();
			String user = t.nextToken();
			String pass = t.nextToken();
			String result = t.nextToken();

			if(userNameTxt.getText().equals(user) && passwordTxt.getText().equals(pass) && result.equals("true")){
				break;
			}else{
				JOptionPane.showMessageDialog(null,"Please enter a username and password ","Chat Client",JOptionPane.ERROR_MESSAGE);
				JOptionPane.showOptionDialog(null,logInPanel,"Chat Client",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
			}

		}

		System.out.println("Connecting to Chat Server at "+ adx + "...");
		lbl4.setText("Currently connected as: " + userNameTxt.getText());
		lbl4.setVisible(true);		
		setTitle("Chat Client - "+userNameTxt.getText()+" currently not in any room");
		output.println("i "+userNameTxt.getText());
		active=true;
		Thread readThread=new Thread(this);
		readThread.start();
	}

	public void run()
	{
		try
		{
			readLoop();
			input.close();
			output.close();
			socket.close();
			System.exit(0);
		}
		catch(IOException e)
		{
			System.out.println("Abnormal chat client socket condition:"+e);;
		}

	}

	public void readLoop() throws IOException
	{
		while(active)
		{
			String line=input.readLine();
			if (line==null)
				continue;
			System.out.println("Client Received:"+line);
			StringTokenizer t;
			switch(line.charAt(0))
			{ 
				case 'i': // a user logged in
					if (!userNameTxt.getText().equals(line.substring(2)))
					{
						JOptionPane.showMessageDialog(null,line.substring(2) + " logged in","Chat Client - " + userNameTxt.getText(),JOptionPane.INFORMATION_MESSAGE);
						onlineUsers.addElement(line.substring(2));
					}
					break;
				case 'o': // a user logged out
					for(int i=0;i<onlineUsers.size();i++)
						if (((String)onlineUsers.elementAt(i)).equals(line.substring(2)))
						{
							onlineUsers.removeElementAt(i);
							break;
						}
					JOptionPane.showMessageDialog(null,line.substring(2) + " logged out","Chat Client - " + userNameTxt.getText(),JOptionPane.INFORMATION_MESSAGE);
					break;
				case 'm': // new message
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					int roomid=Integer.parseInt(t.nextToken());
					for(int i=0;i<myRooms.size();i++)
						if (((Room)myRooms.elementAt(i)).roomId == roomid)
						{
							((Room)myRooms.elementAt(i)).message+= "\n"+t.nextToken("");
							break;
						}
					if (currentRoom!=null)
						txtMessages.setText(currentRoom.message);
					break;
				case 'v': // invitation
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					String host=t.nextToken();
					Room newRoom=new Room();
					newRoom.roomId=Integer.parseInt(t.nextToken());
					newRoom.roomName=t.nextToken();
					newRoom.message=host+" created the room\n";
					if (!host.equals(userNameTxt.getText()))
					{
						int n = JOptionPane.showConfirmDialog(null,host+" has invited you to the room with name \""+newRoom.roomName+"\". Do you want to enter?","Chat Client - "+userNameTxt.getText(),JOptionPane.YES_NO_OPTION);
						if (n!=0)
							continue;
					}
					myRooms.addElement(newRoom);
					break;
				case 'u': // user list
					t= new StringTokenizer(line);
					t.nextToken(); // to move across command
					while (t.hasMoreTokens())
						onlineUsers.addElement(t.nextToken());
					break;
				case 'x':
					currentUsersModel.clear();
					t= new StringTokenizer(line);
					t.nextToken();
					while(t.hasMoreTokens()){
						currentUsersModel.addElement(t.nextToken());
					}
					break;
				default:
			}
		}
	}

	class ListListener implements ListSelectionListener
	{
		public void	valueChanged(ListSelectionEvent	e)	
		{
			
			if(!e.getValueIsAdjusting()){
				System.out.println("ListListener called");
				Room roomToGo=(Room)lstRooms.getSelectedValue();
				currentRoom=roomToGo;
				output.println("x " + currentRoom.roomId);
				txtMessages.setText(roomToGo.message);
				setTitle("Chat Client - "+ userNameTxt.getText()+" in " + currentRoom.roomName);
			}
		}
	}


	class Room
	{
		int roomId;
		String roomName;
		String message="";
		public String toString()
		{
			return roomName;
		}
	}
	
	
	class MyActionListener implements ActionListener
	{
		public void	actionPerformed(ActionEvent	e)	
		{
			Window w = SwingUtilities.getWindowAncestor(userNameTxt);
			JPanel signUpPanel = new JPanel();
			
			if(e.getActionCommand().equals("Send"))
			{
				if (currentRoom==null)
				{
					JOptionPane.showMessageDialog(null,"You are currently not in any room. Create one or wait for another user to invite you.","Chat Client - "+userNameTxt.getText(),JOptionPane.INFORMATION_MESSAGE);					
					return;
				}
				output.println("m "+currentRoom.roomId+" "+userNameTxt.getText()+": "+txtSend.getText());
				System.out.println("Client sent: m "+currentRoom.roomId+" "+txtSend.getText());
				txtSend.setText("");
			}
			else if(e.getActionCommand().equals("Create"))
			{
				int[] selections=lstUsers.getSelectedIndices();

				if (selections.length==0)
				{
					JOptionPane.showMessageDialog(null,"Please select users in the room using Ctrl key.","Chat Client - "+userNameTxt.getText(),JOptionPane.ERROR_MESSAGE);					
					return;
				}
				String inviteMessage="";
				for(int i=0;i<selections.length;i++){
					inviteMessage += " "+(String)onlineUsers.elementAt(selections[i]);
					
				}
				String roomName = JOptionPane.showInputDialog(null,"Enter name of the room:","Chat Client - "+userNameTxt.getText(),JOptionPane.QUESTION_MESSAGE);
				if (roomName==null)
					return;

				if (roomName=="")
					roomName="Unnamed";

				inviteMessage = "v "+roomName+" "+userNameTxt.getText()+inviteMessage;				
				output.println(inviteMessage);
				System.out.println("Client sent: "+inviteMessage);
			}
			else if(e.getActionCommand().equals("Instruction")){
				JOptionPane.showMessageDialog(null,"Use 'ctrl' key to select multiple users to add to a chat.","Chat Client - "+userNameTxt.getText(),JOptionPane.QUESTION_MESSAGE);					
					return;
			}
			else if(e.getActionCommand().equals("Log In")){
				w.setVisible(false);
				
			}
			else if(e.getActionCommand().equals("Sign Up")){
				JOptionPane.showMessageDialog(null,"Your username has been created.","Chat Client",JOptionPane.INFORMATION_MESSAGE);
			}
			else if(e.getActionCommand().equals("Exit")){
				System.exit(0);
			}
		}
	}
}




