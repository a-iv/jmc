/**
 *
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
package jmc;

import xmlstreamparser.*;
import util.Datas;
import util.Util;
import util.Base64;
import util.ExceptionListener;
import jabber.conversation.*;
import jabber.JabberListener;
import jabber.roster.Jid;
import jabber.roster.Jud;
import jabber.presence.*;
import jabber.subscription.*;
import java.util.Vector;
import util.Contents;
import com.twmacinta.util.MD5;

/**
 * Class for reading incoming stanzas
 */
public class StanzaReader {

	private ExceptionListener exceptionListener;
	private JabberListener jabberListener;
	
	
	// common attributes for stanzas
	protected String stanzaId;
	protected String stanzaType;
	protected String stanzaFrom;
	protected String stanzaTo;
	
	public int internalstate;
	protected final int WAIT_LOGIN_PARAMS = 0;
	protected final int WAIT_LOGIN_RESULT = 1;
	protected final int WAIT_SESSION = 2;
	protected final int WAIT_ROSTER = 3;
	protected final int CONNECTION_COMPLETED = 4;
	protected final int REGISTRATION = 5;
	//protected final int REGISTRATION_RESULT = 5;
	
	public StanzaReader(ExceptionListener _exceptionListener, JabberListener _jabberListener, int state) {
		exceptionListener = _exceptionListener;
		jabberListener = _jabberListener;
		
		
		internalstate = state;//registration(4) or login (0) 
	}
	/**
	 * Read the Node objet in argument
	 *@param Node
	 */	
	public void read(Node _node) {
		// common attributes for stanzas:
		stanzaId = _node.getValue("id");
		stanzaType = _node.getValue("type");
		stanzaFrom = _node.getValue("from");
		stanzaTo = _node.getValue("to");
		//System.out.println("ReadStanza: Read()");
		if (_node.name.equals("iq")) {
			readIq(_node);
		} else if (_node.name.equals("presence")) {
			readPresence(_node);
		} else if (_node.name.equals("message")) {
			readMessage(_node);
		} else if (_node.name.equals("stream:error")) {
			// unrecoverable error
			
			exceptionListener.reportException(new Exception("Stream Error " + _node.text));
		}
//		else if (_node.name.equals("stream:stream")) {
//			Node feat = _node.getChild("stream:features");
//			if (feat != null && feat.getChild("bind") != null) {
//				Datas.writerThread.write("<iq type=\"set\" id=\"bind3\">"
//						+ "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">"
//						+ "<resource>" + Datas.jid.getResource() + "</resource></bind></iq>");
//				System.out.println("Binding resource");
//				internalstate = WAIT_SESSION;
//			}else {
//				//TODO che si fa?
//			}
//		}
		else if (_node.name.equals("stream:features")) {
			if (_node.getChild("bind") != null && internalstate == WAIT_LOGIN_RESULT) {
				Datas.writerThread.write("<iq type=\"set\" id=\"bind3\">"
						+ "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">"
						+ "<resource>" + Datas.jid.getResource() + "</resource></bind></iq>");
				System.out.println("Binding resource");
				internalstate = WAIT_SESSION;
				return;
			}
			boolean found = false;
			if (_node.getChild("mechanisms") != null) { 
				try {
					Vector mec =  _node.getChild("mechanisms").getChildren();
					for (int j=0; j<mec.size(); j++) {
						if (((Node)mec.elementAt(j)).text.equals("PLAIN")){
							found = true;
							break;
						}
					}
				}catch(Exception e) {}
			}
			if (found) {
			// PLAIN authorization 
				System.out.println("Using plain authorization");
				String resp = "\0" + Datas.jid.getUsername() + "\0" + Datas.getPassword();
				Datas.writerThread.write("<auth id='sasl2' xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">"
					+ MD5.toBase64(resp.getBytes()) + "</auth>");
				internalstate = WAIT_LOGIN_RESULT;
			}
			else {
				//try with old auth 
				Datas.writerThread.write(
						"<iq id='s1' type='get'><query xmlns='jabber:iq:auth'>" +
						"<username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username>" +
					"</query></iq>");
				internalstate = WAIT_LOGIN_PARAMS;
			}		
			
		}
		else if (_node.name.equals("success")) {
			
			//va rinviato un altro intro con features
			Datas.writerThread.write("<stream:stream to=" + "'" + Datas.hostname + "'" +
					" xmlns='jabber:client'" +
			" xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
			
			
		}
		else if (_node.name.equals("failure")) {
			//perhaps user not registered, try it!
			Datas.writerThread.write("<iq type='set' id='reg1'><query xmlns='jabber:iq:register'><username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username><password>" + Datas.getPassword() + "</password><email>" + Datas.jid.getMail() + "</email></query></iq>");
				//exceptionListener.reportException(new Exception("Unauthorized"));
			internalstate = REGISTRATION;
			return;
		}
	}
	/**
	 * Reads an iq stanza and answers to the server 
	 * Modified by Gabriele Bianchi 04/01/2006
	 * @param _node
	 */
	protected void readIq(Node _node) {
		//System.out.println("+readIQ+");
		
	       StringBuffer res = new StringBuffer(0);
	       String pass = "";
		
	       if (stanzaType.equals("error")) {  //error response

			if (stanzaId.equals("discoitem1")) {
				
				System.out.println("Discovery error");
			}
			else if (internalstate == WAIT_LOGIN_RESULT || internalstate == WAIT_LOGIN_PARAMS) {	
			//perhaps user not registered, try it!
				Datas.writerThread.write("<iq type='set' id='reg1'><query xmlns='jabber:iq:register'><username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username><password>" + Datas.getPassword() + "</password><email>" + Datas.jid.getMail() + "</email></query></iq>");
				//exceptionListener.reportException(new Exception("Unauthorized"));
				internalstate = REGISTRATION;
				return;
			}
			else if (internalstate == REGISTRATION) {
				Node error = _node.getChild("error");
				String exception = "";
				if (error.children.size()>0) {
					Node errorType = (Node)error.children.elementAt(0);
					 exception = errorType.name;
				}
				else exception = error.text;
				exceptionListener.reportRegistrationError(new Exception("Registration failed: "+exception), true);
				return;
			}
			else if (stanzaId.equals("jud_reg")){//jud registration error
				System.out.print("Error in Jud registration");
				exceptionListener.reportRegistrationError(new Exception("Jud Registration failed"), false);
				return;
			}
			else if (stanzaId.equals("regGateway"))
			{//Gateway registration error
				System.out.print("Error in Gateway registration");
				exceptionListener.reportRegistrationError(new Exception("Gateway Registration failed"), false);
				return;
			}
			else if (stanzaId.equals("getNum"))
			{
				System.out.print("Error in getting phone number");
				return;
			}
			else if (stanzaId.equals("vc1")) {
				System.out.print("Error in setting vcard");
				
				return;
			}
			else if (stanzaId.equals("vc2")) {
				System.out.print("Error in getting vcard");
				
				return;
			}
			else if (stanzaId.equals("bind3")) {
				System.out.print("Error in binding");
				jabberListener.unauthorizedEvent("Cannot bind to the server");
				//TODO: riavviare la login senza sasl
				return;
			}
			else if (stanzaId.equals("sess_1")) {
				System.out.print("Error in session");
				jabberListener.unauthorizedEvent("Cannot open session with the server");
				return;
			}
			else
			{
				try {
					Node error = _node.getChild("error");
					String code = Contents.errorCode;
					if (error != null && error.getChildren() != null && !error.getChildren().isEmpty() && error.getChildren().firstElement() != null)
						code = ((Node)error.getChildren().firstElement()).name + " (error code:" + error.getValue("code") + ")";
					else if (error != null)
						code = error.text;
					jabberListener.notifyPresenceError(code);
				}catch (Exception e) {}
				return;
			}
			
	       }

	      else if (stanzaType.equals("result")) { //ok response

		if (stanzaId.equals("discoitem1")) {
		 	Node query = _node.getChild("query");
			Vector items = query.getChildren();
			if (query != null && items != null && items.size() > 0) { //get items
				for (int i = 0; i < items.size(); i++) {
					Node item = (Node)items.elementAt(i);
					if (item.name.equals("item")) {
						 if (item.getValue("name") != null)
							Datas.server_services.addElement(new String[]{item.getValue("name"), item.getValue("jid")});
						else
							Datas.server_services.addElement(new String[]{"", item.getValue("jid")});
					}
				}
			}
			//jabberListener.updateScreen();
			
		}//rooms discovery
		else if (stanzaId.equals("discoRooms")) {
		 	Node query = _node.getChild("query");
			Vector items = query.getChildren();
			if (query != null && items != null && items.size() > 0) { //get items
				Datas.rooms = new Vector(2);
				for (int i = 0; i < items.size() && i < 10; i++) {//display max 12 rooms
					Node item = (Node)items.elementAt(i);
					if (item.name.equals("item")) {
						Datas.rooms.addElement(item.getValue("jid"));
					}
				}
			
			}
			return;
		
		}
		else if (stanzaId.equals("reg1"))
		{
			
			//TODO:forse va cambiato qui
			res.append("<iq id='s1' type='get'><query xmlns='jabber:iq:auth' ><username>").append(Util.escapeCDATA(Datas.jid.getUsername())).append("</username></query></iq>");
			internalstate = WAIT_LOGIN_PARAMS;


		} //response for multichat request info
		/*else if (stanzaType.equals("result") && stanzaId.equals("discoMultichat")) {
			;
		}*/
		else if (stanzaId.equals("getNum"))
		{
			if (_node.getChild("query") != null)
			{
				Node n = (Node)_node.getChild("query").getChildren().firstElement();
				if (n.getChildren().size() == 0)
				{
					return;
				}
				n = (Node)n.getChildren().firstElement();
				String jid = n.getValue("user");
				Jid j = (Jid)Datas.roster.get(Jid.getLittleJid(jid));
				if (j == null)
					return;
				j.phone = n.text;
				//Datas.roster.put(Jid.getLittleJid(jid), j);
			}
			return;
		}
		else if (stanzaId.equals("setNum"))
		{
			System.out.println("Phone number saved");
			return;
		}
		else if (stanzaId.equals("roster_2"))
		{
			System.out.println("Contact deleted");
			return;
		}
		else if (stanzaId.equals("vc1")) {
			//AVATAR set
				
			return;
		}
		else if (stanzaId.equals("vc2")) {
			System.out.print("getting vcard");
			Node vcard =_node.getChild("vCard");
			Jid user  = (Jid)Datas.roster.get(Jid.getLittleJid(stanzaFrom));
			if (user != null && vcard != null && vcard.getChild("PHOTO") != null) {
				Node binval = vcard.getChild("PHOTO").getChild("BINVAL");
				try {
					if (binval != null && binval.text != null) {
						
						byte[] img = Base64.decode(binval.text);
						if (img != null) {
							user.setAvatar(img);
							//System.out.println("AVATAR settato");
						}
					}
				}catch(Exception e) {System.out.println("AVATAR error:"+e.getMessage());}
					
			}
			return;
		}
		else if (internalstate == WAIT_LOGIN_PARAMS)
		{
			//System.out.println(_node.getChild("query").getChild("digest"));
			if (_node.getChild("query").getChild("digest") == null)
			{
				//exceptionListener.reportException(new Exception("Unsecure Server"));
				pass = "<password>" + Datas.getPassword() + "</password>";
			}
			else
				pass = "<digest>" + Datas.getDigestPassword() + "</digest>";
			String resourceNode = "";
			if (_node.getChild("query").getChild("resource") != null)
			{
				resourceNode = "<resource>" + Util.escapeCDATA(Datas.jid.getResource()) + "</resource>";
			} // else forget about the resource

			res.append("<iq id='s2' type='set'><query xmlns='jabber:iq:auth'><username>");

			res.append(Util.escapeCDATA(Datas.jid.getUsername())).append("</username>");
			res.append(resourceNode).append(pass).append("</query></iq>");
		   	internalstate = WAIT_LOGIN_RESULT;
			
		}
		//else if (internalstate == REGISTRATION) {
		//	res += "<iq type='set' id='reg1'><query xmlns='jabber:iq:register'><username>"+Datas.jid.getUsername() +"</username><password>" +Datas.getPassword()+"</password><email>" +Datas.jid.getMail()+ "</email></query></iq>";
		//TODO: se le cose vanno bene come faccio a fare login???			
		//}
		else if (internalstate == WAIT_LOGIN_RESULT)
		{
			/*if (stanzaType.equals("error")) {
				exceptionListener.reportException(new Exception("Stanza IQ Error"));
			}*/
			//old auth
			// we are connected
			res.append("<iq id='s3' type='get'><query xmlns='jabber:iq:roster'/></iq>");

			internalstate = WAIT_ROSTER;

			res.append("<iq type='get' from='").append(Datas.jid.getFullJid()).append("' to='").append(Datas.hostname).append("' id='discoitem1'><query xmlns='http://jabber.org/protocol/disco#items'/></iq>");
		
			// midlet.connectedEvent(); ***
		}
		else if (internalstate == WAIT_SESSION)
		{
			//TODO: aggiungere settaggio resource in caso sia diversa
			Node bind=_node.getChild("bind");
			if ( bind !=  null && bind.getChild("jid") != null) {
				String j = bind.getChild("jid").text;
				if (j != null && !j.equals("") && !Datas.jid.getResource().equals(j.substring(j.indexOf("/")+1)))
					Datas.jid.setResource(j.substring(j.indexOf("/")+1)); //update resource
			}
			res.append("<iq to=\""
					+ Datas.hostname
					+ "\" type=\"set\" id=\"sess_1\">"
					+ "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>");
			//System.out.println("Opening session");
			internalstate = WAIT_ROSTER;
		}
		else if (stanzaId.equals("sess_1")) {
			//System.out.println("Session Open!");
			res.append("<iq id='s3' type='get'><query xmlns='jabber:iq:roster'/></iq>");

			internalstate = WAIT_ROSTER;

			res.append("<iq type='get' from='").append(Datas.jid.getFullJid()).append("' to='").append(Datas.hostname).append("' id='discoitem1'><query xmlns='http://jabber.org/protocol/disco#items'/></iq>");

		}
		else if (stanzaId.equals("s3"))
		{
			
			internalstate = CONNECTION_COMPLETED;
			//send AVATAR
			byte[] img = Datas.jid.getAvatar();
			Datas.jid.setPresence(Presence.getPresence("online"));
            jabberListener.connectedEvent();
			// sends the presence
			if (img != null) {
				try {
					//[] img = Util.imageToByte(avatar, avatar.getWidth(), avatar.getHeight());
					res.append(Presence.sendFirstVCard(img));
					res.append(Presence.sendFirstPresence(img));
				}catch (Exception e) {res.append("<presence/>");}
			}
			else
				res.append("<presence/>");
			
			
			//Read roster	
			readRoster(_node);

		}
		else if (stanzaId.equals("jud_reg"))
		{ //jud registration success
			System.out.println("Success: jud registration");
			//alert to midlet
			jabberListener.notifyJudInfo(Contents.jud_success);
			return;
		}
		else if (stanzaId.equals("jud_search"))
		{ //jud search success
			
			//alert to midlet
			String info = Jud.getJidfromResponse(_node);
			System.out.println("Success: jud search:" + info);
			jabberListener.notifyJudInfo(Contents.jud_search + info);
			return;
		}
		else if (stanzaId.equals("regGateway")) //gateway registration
		{
			System.out.println("Success: gateway registration");
		}
		else if (stanzaId.equals("unregGateway")) //gateway registration
		{
			System.out.println("Success: gateway unregistered");
		}
		else if (internalstate == CONNECTION_COMPLETED)
		{
			;
		}
		
	   }
	   else if (stanzaType.equals("set"))
	   {
		   if (_node.getChild("query") != null && _node.getChild("query").getValue("xmlns") != null)
		   {
			   if (_node.getChild("query").getValue("xmlns").equals("jabber:iq:roster"))
					readRoster(_node);

		   }
	   }
	   
	   if (res.length() > 0) {
			Datas.writerThread.write(res.toString());
			
	   }
	}
	/**
	 * Reads a presence node and notify to midlet
	 * Modified by Gabriele Bianchi 04/01/2006
	 * @param _node
	 */
	protected void readPresence(Node _node) {
		//System.out.println("+readPRESENCE+");
		Node x;
		Node status;
		// default stanza type if not specified.
		if (stanzaType == null) {

			if (_node.getChild("show") == null)
				stanzaType = "online";
			else if (_node.getChild("show").text.equals("xa"))
				stanzaType = "away";
			else
			{
				stanzaType = _node.getChild("show").text;
				//System.out.println(_node.getChild("show").text);
			}
			//check if is a chat presence..
			if ((x = _node.getChild("x", "xmlns", "http://jabber.org/protocol/muc#user")) != null)
			{
				stanzaType = "groupchat";
				Vector children = x.getChildren();
				Vector partners = new Vector(1);
				for (int i = 0; i < children.size(); i++)
				{
					Node child = (Node)children.elementAt(i);
					if (child.name.equals("item"))
					{
						if (child.getValue("jid") == null)
						{

							//partners.addElement(Jid.getLittleJid(stanzaFrom));
							partners.addElement(stanzaFrom.substring(stanzaFrom.indexOf("/") + 1, stanzaFrom.length()));
							break;
						}
						String temp = new String(child.getValue("jid"));
						partners.addElement(temp);
					}
				}
				String littleFrom = stanzaFrom;
				String nick = Datas.jid.getLittleJid();//?
				if (stanzaFrom.indexOf("/") != -1)
				{
					littleFrom = stanzaFrom.substring(0, stanzaFrom.indexOf("/"));
					nick = stanzaFrom.substring(stanzaFrom.indexOf("/") + 1, stanzaFrom.length());
				}
				if (Datas.multichat.get(littleFrom) != null)
				{ //already exists
					GroupChat update = (GroupChat)Datas.multichat.remove(littleFrom);
					if (!update.jids.contains(partners.firstElement()))
						update.jids.addElement(partners.firstElement());
					Datas.multichat.put(littleFrom, update);
					//TODO: reload display!!!
					
				//	jabberListener.updateScreen();//pericolo
					return;
				}
               
				//presenceListener.notifyPresence(new Jid(stanzaFrom), stanzaType);
				Conversation conversation = ChatHelper.createChat(partners, littleFrom, nick);
				conversation.appendToMe(new Message("", "Added to this room"));
				conversation.isMulti = true;
				Datas.conversations.addElement(conversation);
				jabberListener.newConversationEvent(conversation);
				return;
			}
	
			
		}
		if (stanzaType.equals("error")) {
			// error! 
			Node error = _node.getChild("error");
			String code = Contents.errorCode;
			if (error != null  && error.getChildren() != null && !error.getChildren().isEmpty())
				code = ((Node)error.getChildren().firstElement()).name +" (error code:"+ error.getValue("code")+")";
			else if (error != null)
			{
				if (error.getValue("code") != null && error.getValue("code").equals("407"))
					code = "Register to the gateway first";
				else if (error.getValue("code") != null && error.getValue("code").startsWith("5"))
				{
					code = "Jabber Server Error: "+ error.text;
				}
				else
					code = error.text;
			}
			jabberListener.notifyPresenceError(code);
			return;
		} else if (stanzaType.equals("online") || stanzaType.equals("unavailable") || stanzaType.equals("away") || stanzaType.equals("dnd")) {
			
			if (stanzaFrom.indexOf("@") == -1) //not a user
				return;
			//check if is my presence
			if (stanzaFrom.equals(Datas.jid.getFullJid()))
				return; //skip

			//check if is group chat presence signal..			
			if ((x = _node.getChild("x", "xmlns","http://jabber.org/protocol/muc#user")) != null) {
				Vector conversations = Datas.conversations;
		
			// finds out the conversation
				int i=0;
				boolean found = false;
				Conversation conversation = null;
				while ((i<conversations.size()) && !found) {
					conversation = (Conversation) conversations.elementAt(i);
					found = conversation.match(_node);
					i++;
				}
				if (found) {
					Node item = x.getChild("item");
					String usr;
					if (item == null || item.getValue("jid") == null && stanzaFrom.indexOf('/') != -1)
					{
						usr = stanzaFrom.substring(stanzaFrom.indexOf('/') + 1, stanzaFrom.length());
					}
					else
						usr = item.getValue("jid");

					conversation.appendToMe(new Message("", usr + " is " + stanzaType, Jid.getLittleJid(stanzaFrom)));
					jabberListener.newMessageEvent(conversation, i);
					if (stanzaType.equals("unavailable"))
						ChatHelper.deleteMember(stanzaFrom, usr);
					else
						ChatHelper.addMember(stanzaFrom, usr);
					return;
				}
			}//avatar management
			else if ((x = _node.getChild("x", "xmlns","vcard-temp:x:update")) != null) {
				try {
					Node hash = x.getChild("photo"); //AVATAR
					if (hash != null && hash.text != null && !hash.text.equals("")) {
						//byte[] image = hash.text.getBytes();
						Jid rost = (Jid)Datas.roster.get(Jid.getLittleJid(stanzaFrom));
						if (rost != null) {
							if (rost.avatarHash == null) {//No avatar yet
								rost.avatarHash = hash.text;
								//ask for avatar
								Presence.getVCard(rost.getLittleJid());
							}
							else if (!rost.avatarHash.equals(hash.text))
								Presence.getVCard(rost.getLittleJid());//update avatar
						}
						
					}
				}catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			Jid rost = new Jid(stanzaFrom);
			if (_node.getChild("status") != null)
			{
				status = _node.getChild("status");
				rost.setPresence(Presence.getPresence(stanzaType), status.text);
			}
			else
				rost.setPresence(Presence.getPresence(stanzaType));

			jabberListener.notifyPresence(rost, stanzaType);
			
		} else if (stanzaType.equals("subscribe")) {
		 	// roster wants to subscribe for my presence events
				 Jid rost = new Jid(stanzaFrom);
				 if (stanzaFrom.indexOf("@") == -1 || Datas.isGateway(rost.getServername())) //service subscription
				 {
					 Subscribe.acceptSubscription(rost);
					 return;
				 }


				 jabberListener.notifyPresence(rost, stanzaType);
		 	
		 } else if (stanzaType.equals("subscribed")) {
		 	// roster granted my "subscribe wish"
		 	Jid rost = new Jid(stanzaFrom);
			jabberListener.notifyPresence(rost, stanzaType);
		 } else if (stanzaType.equals("unsubscribe")) {
		 	// roster wants to unsubscribe for my presence events
		 } else if (stanzaType.equals("unsubscribed")) {
		 	// roster unsubscribed me for his presence events
		 	// or my "subscribe wish" was declined
		 	Jid rost = new Jid(stanzaFrom);
			jabberListener.notifyPresence(rost, stanzaType);
		 } else if (stanzaType.equals("probe")) {
		 	// roster/server probes my presence
		 }
		
	}
	
	/**
	 * Reads a roster stanza, saves info in Datas and notify to midlet
	 * @author Gabriele Bianchi 
	 * @param _node
	 */
	protected void readRoster(Node _node) {
		System.out.println("+readRoster+");
		Node root = _node.getChild("query");
		Vector children = root.getChildren();
		for (int i=0; i<children.size();i++) {
			Node child = (Node)children.elementAt(i);
			
			
			
			Jid newjid = new Jid(child.getValue("jid"));
			if (child.getValue("name") != null) {//nick
				newjid.setNickname(child.getValue("name"));
			}
			//check group
			if (child.getChild("group") != null)
				newjid.group = child.getChild("group").text;
			if (newjid.getUsername() == newjid.getServername())
				continue;
			String subs = child.getValue("subscription");
			if (Datas.roster.get(newjid.getLittleJid()) != null &&  subs != null) {
				
				if (subs.equals("none") || subs.equals("from")){
					((Jid)Datas.roster.get(newjid.getLittleJid())).setPresence(Presence.getPresence("unsubscribed"));
				}
				else if (subs.equals("remove"))
				{
					Datas.roster.remove(newjid.getLittleJid());
					Vector conversations = Datas.conversations;
					for (int k=0; k<conversations.size(); k++) {
						Conversation c = (Conversation) conversations.elementAt(k);
						if (c.name.equals(newjid.getUsername())) {
							conversations.removeElementAt(k);
							break;
						}
					}
					
				}
			/*	else if (subs.equals("to") && child.getValue("ask") == null && Datas.isGateway(newjid.getServername())) {
					//TODO: AUTO SUBSCRIPTION FOR TRANSPORTS DA TESTARE
					System.out.println("is gateway:"+newjid.getServername());
					Subscribe.acceptSubscription(newjid);
					
				}*/
				return;
			}
			else {
				Datas.registerRoster(newjid);
				if (subs != null && (subs.equals("none") || subs.equals("from"))){
					newjid.setPresence(Presence.getPresence("unsubscribed"));
				}
			/*	else if (subs != null && subs.equals("to") && child.getValue("ask") == null && Datas.isGateway(newjid.getServername())) {
					//TODO: AUTO SUBSCRIPTION FOR TRANSPORTS DA TESTARE
					//System.out.println("is gateway:"+newjid.getServername());
					Subscribe.acceptSubscription(newjid);
					
				}*/
				
			}
		}
		
		if (!Datas.readRoster)
		{
			Datas.readRoster = true;
			jabberListener.notifyRoster();
		}
	}
	/**
	 * Reads a message stanza and notify to midlet
	 * Modified by Gabriele Bianchi 17/01/2006
	 * @param _node
	 */
	protected void readMessage(Node _node) {
		//System.out.println("+readMESSAGE+");
		
		Message message = new Message(_node); // takes the subject&body of the message
		String threadId = "";
		Conversation conversation = null; // conversation corresponding to the message
		Jid rosterFrom;
		
		
		
		if (_node.getChild("thread") != null) {
			threadId = _node.getChild("thread").text;
		} else {
			// _node has no "thread" child: server message?
			if (message.from.equalsIgnoreCase(Datas.hostname) ||  Datas.isGateway(message.from))
			{
				System.out.println("server message");
				conversation = new Conversation(message.from);
				conversation.appendToMe(message);
				jabberListener.newMessageEvent(conversation, -1);
				return;
			}
			
		}		
		//groupchat invitation management
		if ((stanzaType == null || stanzaType.equals("normal")) && _node.getChild("x", "xmlns", "jabber:x:conference") != null) {
			String jidfrom;
			String room;
			//check if server uses new MUC protocol
			if (_node.getChild("x", "xmlns", "http://jabber.org/protocol/muc#user") != null) {
				Node invite = _node.getChild("x", "xmlns", "http://jabber.org/protocol/muc#user");
				if (invite.getChild("invite") != null) {
					jidfrom = invite.getChild("invite").getValue("from");
					room = stanzaFrom;
					jabberListener.newInvitationEvent(jidfrom, room);
					return;
				}
			}//check if server uses old protocol
			else if (_node.getChild("body") != null && _node.getChild("body").text.startsWith("You have been invited")) {

				Node invite = _node.getChild("x", "xmlns", "jabber:x:conference");
				jidfrom = stanzaFrom;
				room = invite.getValue("jid");
				jabberListener.newInvitationEvent(jidfrom, room);
				return;
			}			
		}
		//Composing management
		else if (stanzaType.equals("chat") && _node.getChild("composing", "xmlns", "http://jabber.org/protocol/chatstates") != null) {
			
			Vector conversations = Datas.conversations;
			Conversation convers = null;
			// finds out the conversation, if already exists
			
			for(int i=0; i<conversations.size(); i++) {
				convers = (Conversation) conversations.elementAt(i);
				if (convers.match(_node)) {
					convers.composing = Jid.getUsername(convers.name) + Contents.composing;
					jabberListener.newComposingEvent(convers);
					break;
				}
				
			}
				
			
				
			 
			return;
		}
		else if (stanzaType.equals("chat") && _node.getChild("inactive", "xmlns", "http://jabber.org/protocol/chatstates") != null) {
			Vector conversations = Datas.conversations;
			Conversation convers = null;
			// finds out the conversation, if already exists
			
			for(int i=0; i<conversations.size(); i++) {
				convers = (Conversation) conversations.elementAt(i);
				if (convers.match(_node)) {
					convers.composing = Jid.getUsername(convers.name) + Contents.inactive;
					jabberListener.newComposingEvent(convers);
					break;
				}
				
			}
			
			return;
		}
		else if (stanzaType.equals("chat") && _node.getChild("x", "xmlns", "jabber:x:event") != null &&  _node.getChild("body") == null) {
			Node x = _node.getChild("x", "xmlns", "jabber:x:event");
			if (x.getChild("composing") != null) {
				Vector conversations = Datas.conversations;
				Conversation convers = null;
			
				for(int i=0; i<conversations.size(); i++) {
					convers = (Conversation) conversations.elementAt(i);
					if (convers.match(_node)) {
						convers.composing = Jid.getUsername(convers.name) + Contents.composing;
						jabberListener.newComposingEvent(convers);
						break;
					}
					
				}
				
				
				return;
			}
		}
		
		Vector conversations = Datas.conversations;
		
		// finds out the conversation, if already exists
		int i=0;
		boolean found=false;
		while ((i<conversations.size()) && !found) {
			conversation = (Conversation) conversations.elementAt(i);
			found = conversation.match(_node);
			i++;
		}
		
		// default stanza type if not specified.
		if (stanzaType == null) {
			stanzaType = "normal";
		}
		
		if (found == false) { // no conversation with this roster is running

			if (stanzaType.equals("error")) {
				message.addError(_node.getChild("error").text);
				jabberListener.notifyError(null, message);
			}
			else if (stanzaType.equals("normal") 
					|| stanzaType.equals("chat") ) {
				rosterFrom = (Jid) Datas.roster.get(Jid.getLittleJid(stanzaFrom));

				if (rosterFrom == null) {
					// the roster is not known
					Jid newjid = new Jid(stanzaFrom, "online");
					rosterFrom = newjid;
					Datas.registerRoster(newjid);
					
				}
				// normal: default message type. reply expected. no history.
				// chat: peer to peer communication. with history.

				SingleChat chat = new SingleChat(rosterFrom, stanzaType, threadId);

				chat.appendToMe(message);
				// registers this new conversation
				conversations.addElement(chat);

				jabberListener.newConversationEvent(chat);
			}
			else if (stanzaType.equals("groupchat") && (stanzaFrom.indexOf("/") == -1)) {
				System.out.println("My message");
			    return;
				/*Conversation conversation = ChatHelper.createChat(partners, littleFrom, nick);
				conversation.appendToMe(new Message("", "Added to this room"));
				conversation.isMulti = true;
				Datas.conversations.addElement(conversation);
				jabberListener.newConversationEvent(conversation);
				return;*/
			}
			else if (stanzaType.equals("headline")) {
				rosterFrom = (Jid) Datas.roster.get(stanzaFrom);
				if (rosterFrom == null) {
					// the roster is not known
					Jid newjid = new Jid(stanzaFrom);
					rosterFrom = newjid;
					if (stanzaFrom.indexOf("@") != -1) //is user
					{
						
						Datas.registerRoster(newjid);
						
					}
				
				}
				// no reply expected. (e.g. news, ads...)
				conversation = new Conversation(rosterFrom.getUsername());
				conversation.appendToMe(message);
				// registers this new conversation
				
				jabberListener.newMessageEvent(conversation, 0);
			}
			
		} else {
			// conversation already exists
			if (stanzaType.equals("error")) {
				// should find the message in the conversation, append the error to it and
				// display the error msg ***
				message.addError(_node.getChild("error").text);
				jabberListener.notifyError(conversation, message);
			} else {
				if (message.body.equals("") && message.subject.equals(""))
					return; //taglia i messaggi vuoti, come ad esempio gli eventi MSN ("composing..")

				conversation.appendToMe(message);
				conversation.composing = "";
				jabberListener.newMessageEvent(conversation, i);
			}
		}
	}

	public void setRosterState() {
		internalstate = WAIT_ROSTER;
	}
	
}
