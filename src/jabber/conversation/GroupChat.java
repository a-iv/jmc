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

import jabber.conversation.*;
import jabber.roster.*;

public class GroupChat extends Chat {

  RosterList rosters;  // the people we are talking with
  
  public GroupChat(RosterList _rosters, String _stanzaType, String _threadId) {
    super(_rosters.name, _stanzaType, _threadId);
    rosters = _rosters;
  }
  
  public void broadcast(Message _message) {
      Roster roster;
      for (int i=0; i<rosters.rosters.size(); i++) {
          roster = (Roster) rosters.rosters.elementAt(i);
          //mw.sendMessageToRoster(_message, roster, this);
      }
  }
  
}
