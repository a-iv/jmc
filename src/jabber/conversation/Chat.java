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
/**
 * Chat abstract class
 * 
 *
 */
public abstract class Chat extends Conversation {
    
    // OPAQUE xmpp attributes: only used to mirror the values in answers
    String threadId;
    String stanzaType;
    
    protected Chat(String _name, String _stanzaType, String _threadId) {
        super(_name);
        stanzaType = _stanzaType;
        threadId = _threadId;
    }
    
    /**
     * New message from the user
     * @param _message
     */
    public void appendFromMe(Message _message) {
	if (!stanzaType.equalsIgnoreCase("groupchat"))
        	messages.addElement(_message);
        broadcast(_message);
    }
    
    /**
    * Send a message
    * @param _message
    */
    protected abstract void broadcast(Message _message);
    
    public boolean canAnswer() {
        return true;
    }
}
