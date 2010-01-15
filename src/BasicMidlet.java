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

import xmlstreamparser.*;
import jabber.conversation.*;
import jabber.roster.*;
import jabber.presence.*;
import util.*;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import org.bouncycastle.crypto.digests.SHA1Digest;
import java.io.*;
import java.util.*;

public class BasicMidlet
extends MIDlet
implements CommandListener, ConversationListener, PresenceListener {
    
   // fields
    CommunicationManager cm;
    
    Display display;

    // to remember the *displayed* state
    int internal_state;
    final static int OFFLINE         = 0;
    final static int ONLINE          = 1;
    final static int WAIT_CONNECT    = 6;
    
   // standard methods & constructor
    public BasicMidlet() {
        cm = new CommunicationManager(this);
        internal_state = WAIT_CONNECT;
        cm.connect();
    }
    
    public void startApp() {
        display = Display.getDisplay(this);
    }
    
    public void pauseApp() {}
    
    public void destroyApp(boolean unconditional) {}

   // events from the CommunicationManager
    public void connectedEvent() {
        display.setCurrent(new Alert("CONNECTED", "Connection Established", null, AlertType.CONFIRMATION));
        internal_state = ONLINE;
    }
    
    public void disconnectedEvent() {
        display.setCurrent(new Alert("DISCONNECTED", "You have been disconnected", null, AlertType.CONFIRMATION));
        internal_state = OFFLINE;
    }
    
   // ConversationListener
    public void notifyError(Conversation _conversation, Message _errorMessage) {
        String text = _errorMessage.getText();
        display.setCurrent(new Alert("Conversation Error", text, null, AlertType.INFO));
    }
    
    public void newConversationEvent(Conversation _conv) {
        display.setCurrent(new Alert("New Conversation", null, AlertType.INFO));
    }
    
    public void newMessageEvent(Conversation _conv) {
        String title = "from " + _conv.name;
        
        Message lastMessage = (Message) _conv.messages.lastElement();
        String text = lastMessage.getText();
        
        display.setCurrent(new Alert(title, text, null, AlertType.INFO));
    }
    
   // PresenceListener
    public void notifyPresence(Roster _roster, int _presence) {
        String title = _roster.jid.getLittleJid();
        String body = _roster.name + Presence.getPresence(_presence);
        display.setCurrent(new Alert(title, body, null, AlertType.INFO));
    }
    
}
