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
 */

package jabber.conversation;

import java.util.Vector;
import util.Datas;
import jabber.conversation.GroupChat;
import jabber.roster.Jid;

/**
 * Multi-chat management class
 * @author Gabriele Bianchi
 */
public class ChatHelper {

	/**
	 * Join a room chat
	 * @param nick
	 * @param chatRoom
	 * @param chatServer
	 */
	public static void groupChatJoin(String nick, String chatRoom, String chatServer) {
 
         	/*String sXML = "<iq type=\'get\' id=\'" + ID + "\' to=\'" + chatRoom + "@" + chatServer + "\'>"
                       + "<query xmlns=\'jabber:iq:conference\'/></iq>"; //changed to iq:conference
 		*/
		String sXML = "<presence from='" + Datas.jid.getFullJid() + "' to='" + chatRoom + "@" + chatServer + "/" + nick + "'></presence>";
        	 Datas.writerThread.write(sXML);
	}

	/**
	 * Chat Service discovery 
	 * @param chatServer
	 */
	public static void serviceRequest(String chatServer) {
		String res = "<iq from='"+Datas.jid.getFullJid()+"' id='discoRooms' to='"+chatServer+"' type='get'><query xmlns='http://jabber.org/protocol/disco#items'/></iq>";
		Datas.writerThread.write(res);
	}

	/**
	 * Create a new multichat object
	 * @param jids
	 * @param name
	 * @param nick
	 * @return
	 */
	public static GroupChat createChat(Vector jids, String name, String nick) {
		GroupChat chat = new GroupChat(jids, name, "groupchat", "", nick);
		Datas.multichat.put(name, chat);
		return chat;
		
	}

	/**
	 * Add a multichat member
	 * @param name
	 * @param jid
	 */
	public static void addMember(String name, String jid) {
		if (jid.indexOf('@') == -1)
			jid += "@"+Datas.hostname;
		GroupChat chat = (GroupChat)Datas.multichat.remove(Jid.getLittleJid(name));
		chat.jids.addElement(jid); //PATCH 2008
		Datas.multichat.put(chat.name, chat);
	}

	/**
	 * Delete a multichat member
	 * @param name
	 * @param jid
	 */
	public static void deleteMember(String name, String jid) {

		GroupChat chat = (GroupChat)Datas.multichat.remove(Jid.getLittleJid(name));
		//?
		chat.jids.removeElement(jid);
		Datas.multichat.put(chat.name, chat);
	}
	/**
	 * invite a new multichat member
	 * @param room
	 * @param jid
	 */
	public static void inviteContact (String jid, String room) {
		String res ="<message from=\'"+Datas.jid.getFullJid()+"\' to=\'"+room+"\'><x xmlns=\'http://jabber.org/protocol/muc#user\'><invite to=\'"+jid+"\'>"+
    				"<reason>"+
        			"Hey "+Jid.getLittleJid(jid)+", Come in chat with me!"+
      				"</reason>"+
    				"</invite>"+
  				"</x>"+
				"</message>";
		Datas.writerThread.write(res);
	}
}
