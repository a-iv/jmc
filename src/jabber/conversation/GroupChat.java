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

package jabber.conversation;

//import jabber.conversation.*;
//import jabber.roster.Jid;
//import jabber.roster.RosterList;
import java.util.Vector;
import util.Datas;

/**
 * 
 *MultiChat class 
 *
 */
public class GroupChat extends Chat {

  //public RosterList rosters; 
  public Vector jids;
  public String nick;
  /*
  public GroupChat(RosterList _rosters, String _stanzaType, String _threadId) {
    super(_rosters.name, _stanzaType, _threadId);
    rosters = _rosters;
  }*/
  public GroupChat(Vector _jids, String _name, String _stanzaType, String _threadId, String _nick) {
    super(_name, _stanzaType, _threadId);
    jids = _jids;
    nick = _nick;
  }
  
  /**
   * Modified by Gabriele Bianchi 04/01/2006
  
  public void broadcast(Message _message) {
      Jid roster;
      for (int i=0; i<rosters.rosters.size(); i++) {
          roster = (Jid) rosters.rosters.elementAt(i);
          //mw.sendMessageToRoster(_message, roster, this);
      }
  }
 */
    /**
     * Send message to members
     * @param Message
     * @author Gabriele Bianchi
     */
    public void broadcast(Message _message) {
        //mw.sendMessageToRoster(_message, roster, this);
	//for (int i=0; i<jids.size(); i++) {
        	StringBuffer res = new StringBuffer("<message type='").append(stanzaType).append("' from = '").append(Datas.jid.getFullJid()).append("' to ='").append(name).append("'>").append(_message.getTextAsXML());
        	if (!threadId.equals("")) {
         	   res.append("<thread>").append(threadId).append("</thread>");
       		}
        	res.append("</message>");

        	Datas.writerThread.write(res.toString());
	//}
    }  
  
}
