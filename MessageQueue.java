import java.util.*;

public class MessageQueue
{
	Queue<String> messages = new LinkedList<String>();

	synchronized String get()
	{
		if (messages.peek() == null)
			return "d";
		else
			return messages.remove();
	}

	synchronized void put(String s)
	{
		messages.add(s);
	}
}
