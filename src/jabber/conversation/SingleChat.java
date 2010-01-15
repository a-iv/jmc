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

import xmlstreamparser.*;
import jabber.roster.*;

public class SingleChat extends Chat {
    Roster roster; // the person we are talking with
    
    public SingleChat(Roster _roster, String _stanzaType, String _threadId) {
        super(_roster.name, _stanzaType, _threadId);
        roster = _roster;
    }
    
    public void broadcast(Message _message) {
        //mw.sendMessageToRoster(_message, roster, this);
        String res = "<message type='"+stanzaType+"' "+
                     "from ='greg@w9f05952/mobile' "+
                     "to ='"+roster.jid.getFullJid()+"'>"+
                     _message.getTextAsXML();
        if (!threadId.equals("")) {
            res = res + "<thread>" + threadId + "</thread>";
        }
        res = res + "</message>";

        util.Datas.writerThread.write(res);
    }
    
    /**
     * Tests if the conversation matches the given message stanza
     */
    public boolean match(Node _node) {
        boolean res = false;
        if (Jid.getLittleJid(_node.getValue("from")).equals(roster.jid.getLittleJid())) {
            // should also test the threadID? stanzaType?
            res = true;
        }
        return res;
    }
    
}
