/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package util;

import java.util.Hashtable;
import com.sun.lwuit.Image;
import com.sun.lwuit.Command;
import java.io.InputStream;

/**
 *Class for GUI contents like images
 *
 *@author Gabriele Bianchi
 */
public class Contents {

	public static Hashtable images;
	public static Hashtable presence; //dictionary: key=protocol presence name, value=customizable presence name
	
	public static String help_text = "Connect: connect to your favourite Jabber server.\n"
				  +"User settings: save your userid, password and e-mail.\n"
				  +"If you are not registered to the server, you will be registrated during the first connection attempt.\n"
				  +"Credits: author info";
	public static String chatWarn = "(warning: rooms discovery needs a lot of memory)";
	public static String credits = "JabberMixClient is created by Gabriele Bianchi (gabriele.bianchi@gmail.com)";
	//Gui Commands
	public static Command ok = new Command("Ok", 1);
	public static Command send = new Command("Send", 1);
	public static Command accept = new Command("Accept",1);
	public static Command back = new Command("Back", 0);
	public static Command exit = new Command("Exit", 0);
	public static Command disc = new Command("Actions", 0);
	public static Command deny = new Command("Deny", 0);
	public static Command history = new Command("History",  2);
	//public static Command active = new Command("Open chat", 2);
	public static Command delete = new Command("Close chat",  3);
	public static Command stop = new Command("Stop",  0);
	public static Command register = new Command("Register",  1);
	public static Command unregister = new Command("Unregister",  2);
	public static Command chat = new Command("Chat",  1);//NUOVO
	public static Command info = new Command("Info",  2);//NUOVO
	public static Command invite = new Command("Invite",  3);//NUOVO


	public static Command select = new Command("Select",  1);
	//other alert strings
	public static String help = new String(help_text);
	public static String noPhone = "Your device doesn't support this feature!";
	public static String noData = "Set your configuration settings first!"; //conf data set alert
	public static String noJud = "Your Jabber server doesn't support it!";
	public static String done = "Your request has been sent!";
	public static String noSavedPhone = "Phone number is not saved for this contact";
	public static String noGtw = "Gateway name is incorrect";
	public static String failGtw  = "Registration failed";
	public static String subs = "You aren't subscribed to this user, sending the request now.."; //NUOVO
	

	public static String[] offlineChoices =  new String[] {"Connect", "User settings", "Help", "Credits"};
	public static String[] rosterChoices = new String[] {"Change jid", "Delete", "PhoneCall!"};
	public static String[] judChoices = new String[] {"Register", "Search a user"};
	public static String[] sslChoices = new String[] {"Unsecure connection", "SSL connection (port 5223)", "Http connection (port 80)"};
	public static String[] optionsChoices = new String[] { "Disconnect", "Add Contact", "MSN/AIM/ICQ/Yahoo", "Join Multichat", "Change Status", "Accept incoming Wake-up sms","Server Info", "Search users" };
	public static String[] hide = new String[] { "Hide offline", "Show all" };
	static public String[] string_presence = { "offline", "online", "away", "busy", "unsubscribed" };
	static public String[] mystring_presence = { "offline", "online", "away", "busy"};
	static public String[] online_choices = { "Send message", "Try to subscribe", "Wake-up with SMS"};
	static public String[] ws_choices = { "Weather Forecast", "Stock Quotes", "Free SMS" };
	public static String[] gtwChoices = new String[] {"MSN Messenger", "AIM", "ICQ", "Yahoo"};
	public static String subsc_form = "Subscription request";
	public static String invit_form = "Chat Invitation";
	public static String offline_form = "Offline Menu";
	public static String wait_form = "Connecting";
	public static String options_form = "Other Options";
	public static String settings_form = "Settings";
	public static String errorCode = "Operation not executed! ";
	public static String jud_success = "You have been registered to Jud!";
	public static String jud_search = "Jud Search response: ";
    public static String jud_nores = "No results.";
	public static String new_convers = "New conversation from ";	
	public static String choose_status = "Choose status";
	public static String saved = "Data saved";
	public static String emptyParams = "Empty parameters";
	public static String jid_sintax_error = "No changes: Jid sintax not correct";
	public static String no_changes = "No changes";
	public static String composing = " is typing..";
	public static String inactive = " has closed chat..";
	public static String noRoster = "Sorry, there aren't contacts: go to 'options'->'add contact'";
	public static String explainGtw = "You can chat with your contacts from different IM protocols. Select your preferite and insert your credentials.";
	
	public Contents() {
		images = new Hashtable(15);
		images.put("online","/online.png");
		images.put("offline","/offline.png");
		images.put("away","/away.png");
		images.put("busy","/dnd.png");
		images.put("disconnected","/disconnected.png");
		images.put("connected","/connected.png");
		images.put("message","/message.png");
		images.put("unsubscribed", "/question_mark.png");
		images.put("1smile1","/smile.png");
		images.put("1smile2","/sad.png");
		images.put("1smile3", "/riso.png");
		images.put("1smile4", "/prr.png");
		images.put("logo", "/jmc_back.png");
		images.put("choice", "/choice.png");
		images.put("icon", "/icon.png");
		images.put("settings", "/profile.png");
		images.put("add", "/add.png");
		images.put("invite", "/invite.png");
		images.put("msn", "/msn.png");
		images.put("wake", "/wake.png");
		images.put("presence", "/onlineMenu.png");
		images.put("jmcAvatar", "/jmc.png");

		presence = new Hashtable(5);
		presence.put("online", string_presence[1]);
		presence.put("unavailable", string_presence[0]);
		presence.put("dnd", string_presence[3]);
		presence.put("unsubscribed", string_presence[4]);
		presence.put("away", string_presence[2]);

		//help.setTimeout(9000);
		

	}
	/**
	 *
	 *@param name
   *@return String
	 */
	public static String getImage(String name)
	{
		return (String)images.get(name);
	
	}
	/**
	 * Get Image object
	 *@param name
   *@return Image
	 */
	public static Image displayImage(String name) {
		Image image;
		try {
			image = Image.createImage(getImage(name));
		}catch (java.io.IOException e) {
			return null;
		}
		return image;
	
	}
  /**
	 * Get Image from stream
	 *@param name
   *@return Image
	 */
	public static Image displayImage(InputStream stream) {
		Image image;
		try {
			image = Image.createImage(stream);
		}catch (java.io.IOException e) {
			return null;
		}
		return image;
	
	}
}
