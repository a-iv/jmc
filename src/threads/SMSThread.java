/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */


package threads;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.microedition.io.Connector;

import util.Contents;
import jmc.GuiMidlet;

/**
 * SMSThread
 * Send a SMS
 *
 * @author Gabriele Bianchi
 */
public class SMSThread extends Thread
{
	private String text;
	private String number;
	private GuiMidlet midlet;

	public SMSThread(String _number, GuiMidlet _midlet)
	{
		number = "sms://" + _number + ":5444";
		midlet = _midlet;
	}

	public void setText(String _text)
	{
		text = _text;
	}

	public void run()
	{
		try
		{
			
			MessageConnection conn = (MessageConnection)Connector.open(number);
			TextMessage msg = (TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);
			msg.setPayloadText(text);
			conn.send(msg);
			//Dialog.show("", Contents.done, null, Dialog.TYPE_CONFIRMATION, null, 3000);
			//midlet.display.setCurrent(Contents.done, midlet.getGuiRosterItem());

		}
		catch (Exception e)
		{
			//Dialog.show("", Contents.noPhone, null, Dialog.TYPE_ERROR, null, 3000);
			return;
		}

	}

}