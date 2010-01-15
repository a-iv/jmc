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

import java.util.*;
import xmlstreamparser.*;

public class Conversation {
  
  public Vector messages;
  public String name;  // pretty string for display: nickname, JID, groupname, subject...
  
  public Conversation(String _name) {
    messages = new Vector();
    name = _name;
  }
  
  /**
   * New message received
   */
  public void appendToMe(Message _message) {
    messages.addElement(_message);
  }

  /**
   * Tests if the conversation matches the given message stanza
   */
  public boolean match(Node _node) {
      return false;
  }
  
  public boolean canAnswer() {
      return false;
  }
  
}
