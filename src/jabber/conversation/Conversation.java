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

import java.util.Vector;

import javax.microedition.lcdui.Image;
import xmlstreamparser.*;
import jabber.roster.Jid;
/**
 * Conversation class
 *
 *
 */
public class Conversation {
  
  public Vector messages;
  public String name;  // pretty string for display: nickname, JID, groupname, subject...
  public boolean isMulti;
  public String composing;
  public Image avatar; //AVATAR
  
  public Conversation(String _name) {
    messages = new Vector(2,1);
    name = _name;
    isMulti = false;
    composing = "";
  }
  
  /**
   * New message received
   * @param _message
   */
  public void appendToMe(Message _message) {
    messages.addElement(_message);
  }

  /**
   * Tests if the conversation matches the given message stanza
   * Modified by Gabriele Bianchi
   * @param Node
   */
  public boolean match(Node _node) {

     
      if (Jid.getLittleJid(_node.getValue("from")).equals(Jid.getLittleJid(name)))
      	return true; 
      return false;
  }
  
  /**
   * Not used
   * @return
   */
  public boolean canAnswer() {
	
      	return false;
  }
  
}
