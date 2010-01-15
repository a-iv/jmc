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
package jmc;

import jabber.conversation.*;
import jabber.roster.Jid;
import jabber.JabberListener;
import jabber.presence.*;
import util.Datas;
import util.Contents;
import java.util.Hashtable;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.SelectionListener;

/**
 * Midlet events manager
 * @author Gabriele Bianchi
 */
public class MidletEventListener implements JabberListener, SelectionListener
{
	private GuiMidlet midlet;
	//public Display display; 
	//private int internal_state;
	private Hashtable infopool;
	public boolean keypressed = true;
	

	final static int OFFLINE         = 0;
	final static int ONLINE          = 1;
	final static int ROSTER          = 2;
	final static int CONVERSATION    = 3;
	final static int SUBSCRIPTION    = 4;	
	final static int PARAMS          = 5;
	final static int WAIT_CONNECT    = 6;
	final static int WAIT_DISCONNECT = 7;
	final static int ROSTER_DETAILS  = 8;
	final static int MULTI_CHAT      = 9;
	final static int INVITATION      = 10;
	final static int OPTIONS	 = 11;
	final static int JUD		 = 12;

	public MidletEventListener(GuiMidlet _midlet) {
		midlet = _midlet;
		infopool = midlet.infopool;
		
	}


	/**
	 * Event method invoked by CommunicationManager when connection is established
	 * 
	 */
	public void connectedEvent() {
		midlet.getGuiOnlineMenu();
		midlet.internal_state = ONLINE;
	}
	/**
	 * @param reason
	 */
	public void unauthorizedEvent(String reason) {

		//display.setCurrent(new Alert("DISCONNECTED", reason, Datas.images.displayImage("disconnected"), AlertType.INFO),midlet.getGuiOfflineMenu());
		
		midlet.getGuiOfflineMenu();
		Dialog.show("DISCONNECTED", reason, null, Dialog.TYPE_WARNING,null, 3000);
		Datas.readRoster = false;
		midlet.internal_state = OFFLINE;
	}
	/**
	 * Event method invoked by CommunicationManager when disconnection occurs
	 * 
	 */
	public void disconnectedEvent() {
		//display.setCurrent(midlet.getGuiOfflineMenu());
		
		midlet.internal_state = OFFLINE;
		
		midlet.getGuiOfflineMenu();
		Dialog.show("DISCONNECTED", "You have been disconnected", null, Dialog.TYPE_WARNING,Datas.images.displayImage("disconnected"), 3000);
		Datas.readRoster = false;
		midlet.internal_state = OFFLINE;
	}
	
	/**
	 * ConversationListener method for the chat notification error
	 *@param Conversation
	 *@param Message
	 */
	public void notifyError(Conversation _conversation, Message _errorMessage) {
		// should insert&display the error in the conversation history?
		String text = _errorMessage.getText();
		//Alert a = new Alert("Error", text, null, AlertType.INFO);
		//a.setTimeout(Alert.FOREVER);
		//display.setCurrent(a);
		Dialog.show("Error", text, null, Dialog.TYPE_ERROR,null, 3000);
		
	}
	/**
	 * Error in registration to server
	 *@param Exception
	 *
	 */
	public void registrationFailure(Exception e, boolean offline) {

		if (offline)
		{
			//display.setCurrent(new Alert("DISCONNECTED", e.getMessage(), , AlertType.INFO), midlet.getGuiOfflineMenu());
			Dialog.show("DISCONNECTED", e.getMessage(), null, Dialog.TYPE_WARNING,Datas.images.displayImage("disconnected"), 3000);
			
			midlet.internal_state = OFFLINE;
			midlet.getGuiOfflineMenu();
		}
		else
			Dialog.show("", Contents.failGtw, null, Dialog.TYPE_ERROR,null, 3000);
		
			//display.setCurrent(Contents.failGtw);

	} 
	/**
	 * ConversationListener method for a new chat start notification 
	 *@param Conversation
	 *
	 */
	public void newConversationEvent(Conversation _conv) {
	
		System.out.println("NEW_CONVERSATION_EVENT name= " + _conv.name);
		//AlertType info = AlertType.INFO;
		if (midlet.internal_state == ONLINE) {
			// update the online menu
			midlet.getGuiOnlineMenu();
			//display = Display.getDisplay(midlet);
			//display.setCurrent(new Alert("New Message", Contents.new_convers + _conv.name, Datas.images.displayImage("message"), info), midlet.getGuiOnlineMenu());
			Dialog.show("New Message", Contents.new_convers + _conv.name, null, Dialog.TYPE_INFO,Datas.images.displayImage("message"), 3000);
			
			
		} else if ((midlet.internal_state == CONVERSATION) && 
				(_conv == midlet.currentConversation)) {
			// update if its the current conversation
			//display = Display.getDisplay(midlet);
			midlet.getGuiUpdateConversation(0);
		} else if (midlet.internal_state == MULTI_CHAT || midlet.internal_state == INVITATION) {
			//infopool.put("multichat", _conv.name);
			midlet.currentConversation = _conv;
			midlet.internal_state = CONVERSATION;
			midlet.getGuiConversation(0);
		}else if (midlet.internal_state == ROSTER) {
			Dialog.show("New Message", Contents.new_convers + _conv.name, null, Dialog.TYPE_INFO,Datas.images.displayImage("message"), 3000);
			//display.setCurrent(new Alert("New Message", Contents.new_convers + _conv.name, Datas.images.displayImage("message"), info));
		}
		else {
			// send an alert
			midlet.tabbedPane.addTab(_conv.name, new Container());
           
            //midlet.getGuiConversation(midlet.tabbedPane.getSelectedIndex());
            
			//display.setCurrent(new Alert("New Message", Contents.new_convers + _conv.name, Datas.images.displayImage("message"), info));
			Dialog.show("New Message", Contents.new_convers + _conv.name, null, Dialog.TYPE_INFO,Datas.images.displayImage("message"), 3000);
			
			//info.playSound(display); //come emetto un suono??
		}
		
		System.out.println("end of event");
	}
	/**
	 * ConversationListener method for a new message arrival from the server
	 *@param Conversation
	 *
	 */
	public void newMessageEvent(Conversation _conv, int tab) {
	
		
		System.out.println("NEW_MESSAGE_EVENT");
		String title = "from " + _conv.name;
	    
		Message lastMessage = (Message) _conv.messages.lastElement();
		String text = lastMessage.getText();
		
		if ((midlet.internal_state == CONVERSATION) && 
				(_conv == midlet.currentConversation)) {
			// update if its the current conversation
					midlet.getGuiUpdateConversation(0);
		} else {
			// send an alert
			if (tab > 0) { //TODO: da testare l'aggiunta di un'icona all'arrivo del msg sulla tab
				//midlet.tabbedPane.removeTabAt(tab);
                String name = _conv.name;
                if (name.indexOf("@") != -1)
                    name= name.substring(0, name.indexOf("@"));
                midlet.tabbedPane.setTabTitle(name+" (m)", null, tab);
				//midlet.tabbedPane.insertTab(_conv.name, Datas.images.displayImage("message"), new Container(), tab);
			//	midlet.tabbedPane.setTabTitle(_conv.name, Datas.images.displayImage("message"), tab);
			}else if (tab == -1) {
				//server message
				Dialog.show(title, text, "Ok", "");
			}
		//	AlertType info = AlertType.INFO;
			//display.setCurrent(new Alert(title, text, Datas.images.displayImage("message"), info));
			//info.playSound(display); 
		}
	}
	/**
	 * ConversationListener method for a new message arrival from the server
	 *@param Conversation
	 *
	 */
	public void newComposingEvent(Conversation _conv) {
	
		
		System.out.println("NEW_Composing_EVENT");
		//String title = "from " + _conv.name;
	    
		//Message lastMessage = (Message) _conv.messages.lastElement();
		//String text = lastMessage.getText();
		
		if ((midlet.internal_state == CONVERSATION) && 
				(_conv == midlet.currentConversation)) {
			// update if its the current conversation
					midlet.getGuiUpdateConversation(0);
		} 
	}
	/**
	 * ConversationListener method for a new invitation arrival from a jid
	 *@param from, room
	 *
	 */
	public void newInvitationEvent(String from, String room) {
		System.out.println("NEW_INVITATION_EVENT");
		
		infopool.put("invit_from", from);
		infopool.put("invit_room", room);
		infopool.put("invit_internal_state", new Integer(midlet.internal_state));
		midlet.internal_state = INVITATION;
		midlet.getGuiChoose("invitation");
	} 
	/**
	 * PresenceListener method for presence notifications 
	 *@param Jid
	 *@param String
	 */
	public void notifyPresence(Jid _roster, String _presence) {
		// need to update the display?
		
		if (_presence.equals("online") || _presence.equals("unavailable") || _presence.equals("away") || _presence.equals("dnd")) {
			if (_presence.equals("unavailable") && Datas.isGateway(_roster.getServername()))
			{
				if (_roster.getUsername() == null)
				{

					//Dialog.show(title, body, cmds)Contents.failGtw);
					Datas.roster.remove(_roster.getLittleJid());
				}
				else
				{
					Jid rost = (Jid)Datas.roster.get(_roster.getLittleJid());
					if (rost != null)
						rost.setPresence(Presence.getPresence(_presence));
					return;
				}
			}
			else if ((midlet.internal_state == ROSTER) && (_roster.getLittleJid().equals(midlet.currentjid.getLittleJid()))) {
				if (_roster.status_message.equals(""))
					midlet.currentjid.setPresence(Presence.getPresence(_presence));
				else
					midlet.currentjid.setPresence(Presence.getPresence(_presence), _roster.status_message);
				midlet.getGuiRosterItem();
				
			} 
			else if (midlet.internal_state == ONLINE)  {
				Jid rost = (Jid)Datas.roster.get(_roster.getLittleJid());
				if (rost != null)
				{
					if (_roster.status_message.equals(""))
						rost.setPresence(Presence.getPresence(_presence));
					else
						rost.setPresence(Presence.getPresence(_presence), _roster.status_message);
				}
				//String title = _roster.getLittleJid();
				//String body = _roster.getUsername() + " is " + Presence.getPresence(_presence);
				midlet.getGuiOnlineMenu();
			}
			else {
				// send an alert
			
				String real_presence = Presence.getPresence(_presence);
				//String title = _roster.getLittleJid();
				//String body = _roster.getUsername() + " is " + real_presence;
				//disattivato alert di cambio presence
				//display.setCurrent(new Alert(title, body, Datas.images.displayImage(real_presence), AlertType.INFO));
				Jid rost = (Jid)Datas.roster.get(_roster.getLittleJid());
				if (rost != null)
				{
					if (_roster.status_message.equals(""))
						rost.setPresence(real_presence);
					else
						rost.setPresence(real_presence, _roster.status_message);
				}
			}
		}
		else if (_presence.equals("unsubscribed")) {
			if (Datas.isGateway(_roster.getServername())) {
				return;//send an alert?
			}
			if (Datas.roster.get(_roster.getLittleJid()) == null)  {
				_roster.setPresence(Presence.getPresence("unsubscribed"));
				Datas.registerRoster(_roster);
			}
			midlet.internal_state = ONLINE;
			Dialog.show(_roster.getLittleJid()+" added!", "Subscription not accepted/pending.", null, Dialog.TYPE_INFO,null, 3000);
			midlet.getGuiOnlineMenu();
			
		}
		else if (_presence.equals("subscribed")) {
			if ((midlet.currentjid = (Jid)Datas.roster.get(_roster.getLittleJid())) == null)  {
				Datas.registerRoster(_roster);
				midlet.currentjid = _roster;
			}
			else {
				midlet.currentjid.setPresence("subscribed");
			}
			
			midlet.internal_state = ROSTER;
			Dialog.show("",_roster.getUsername()+" subscribed!", null, Dialog.TYPE_CONFIRMATION,null, 3000);
			
			//display.setCurrent(new Alert(_roster.getUsername()+" subscribed!", "Changes Saved", null, AlertType.CONFIRMATION), midlet.getGuiRosterItem());
		}
		else if (_presence.equals("subscribe")) {
			
			
			//save the state
			if (midlet.currentjid != null && !midlet.currentjid.getLittleJid().equals(_roster.getLittleJid()))
				infopool.put("currentjid", new Jid(midlet.currentjid.getFullJid(), midlet.currentjid.getPresence()));
			infopool.put("internal_state", new Integer(midlet.internal_state));
			midlet.internal_state = SUBSCRIPTION;
			midlet.currentjid = _roster;
			midlet.getGuiChoose("subscription");
		}
		
		
	}

	/**
	 * RosterListener method for the reading of the roster items list
	 *@param Jid
	 *@param String
	 */
	public void notifyRoster() {
		//if (midlet.internal_state == ONLINE) 
		midlet.internal_state = ONLINE;
		//display.setCurrent(new Alert("", "Reading Roster...", Contents.displayImage("connected"), AlertType.INFO), midlet.getGuiOnlineMenu());
		
		midlet.getGuiOnlineMenu();
		Dialog.show("","Reading Roster...", null, Dialog.TYPE_INFO,Contents.displayImage("connected"), 3000);
		
	}
	/**
	 * RosterListener method for alerting a new jud search response event
	 *
	 *@param String
	 */
	public void notifyJudInfo(String info) {
		
		infopool.put("jud_message", info);
		if (midlet.internal_state != JUD)
			Dialog.show("Search user",new Label(info), new Command[]{Contents.ok}, Dialog.TYPE_INFO,null);
		
			//display.setCurrent(new Alert("Jud alert", info, null,AlertType.INFO));
		else 
			midlet.getGuiJudMenu();
	}
	/**
	 * Notify an error in presence management
	 */
	public void notifyPresenceError(String code) {
		//Alert al = new Alert("Error!", code, null, AlertType.INFO);
		System.out.println("notifyPresenceError"+code);
		/*if (midlet.internal_state == MULTI_CHAT)
			display.setCurrent(al, midlet.getGuiRoomList());
		else if (midlet.internal_state == CONVERSATION)
			display.setCurrent(al, midlet.getGuiConversation());
		else
		{
			midlet.internal_state = ONLINE;
			display.setCurrent(al, midlet.getGuiOnlineMenu());
		}*/
		
	}
	/**
	 * Refresh screen
	 * 
	 */
	public void updateScreen()
	{
		midlet.setCurrentDisplay();
	}


	public void selectionChanged(int old, int newtab) {
		
		//System.out.println("old:"+old+" new:"+newtab+" pressed:"+keypressed);
		if (old != newtab) {
			if (newtab == 0) {
				
				if (keypressed) {
					midlet.internal_state = ONLINE;
					midlet.getGuiOnlineMenu();
					System.gc();
				}
			}
			else {
				
				
				if (keypressed) {
					midlet.internal_state = CONVERSATION;
					midlet.currentConversation = (Conversation)Datas.conversations.elementAt(newtab-1);
					midlet.getGuiConversation(newtab);
				}
			}
			
		}
		
/*		System.out.println("oldtab:"+old+" newtab:"+newtab);
		if (old != newtab) {
			if (newtab == 0)
				midlet.getGuiOnlineMenu();
			else {
				midlet.tabbedPane.setSelectedIndex(newtab);
				midlet.getGuiConversation(newtab);
			}
		}
	*/	
	}
}
