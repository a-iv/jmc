/**
 * MicroJabber, jabber for light java devices. Copyright (C) 2004, Gregoire Athanase
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with 
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, 
 * Suite 330, Boston, MA 02111-1307 USA.
 */


package threads;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.microedition.io.Connector;
import javax.microedition.io.PushRegistry;
import com.sun.lwuit.Dialog;
import util.Contents;
import jmc.GuiMidlet;

/**
 * PushThread
 * Register/Receive SMS
 *
 * @author Gabriele Bianchi
 */
public class PushThread extends Thread
{
	private String text;
	private String conn;
	private boolean register = false;
	private GuiMidlet midlet;

	public PushThread(String _conn, GuiMidlet _midlet)
	{
		conn = _conn;
		midlet = _midlet;
	}
	public PushThread(GuiMidlet _midlet)
	{
		register = true;
		midlet = _midlet;
	}

	public void run()
	{
		
		try
		{
			if (register) 
			{
				PushRegistry.registerConnection("sms://:5444", midlet.getClass().getName(), "*");
				Dialog.show("", Contents.done, null, Dialog.TYPE_CONFIRMATION,null, 3000);
				
			}
			else //show the arrived sms
			{
				MessageConnection con = (MessageConnection)Connector.open(conn);
				TextMessage msg;
				msg = (TextMessage)con.receive();
				text = msg.getPayloadText();
				Dialog.show("SMS", text, null, Dialog.TYPE_CONFIRMATION,null, 4000);
				
				
			}

		}
		catch (Exception e)
		{
			//midlet.display.setCurrent(Contents.noPhone, midlet.getGuiOnlineMenu());
			Dialog.show("", Contents.noPhone, null, Dialog.TYPE_ERROR,null, 3000);
			
			return;
		}

	}


}