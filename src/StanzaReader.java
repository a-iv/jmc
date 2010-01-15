/**
 * The StanzaReader reads Stanzas, and answers them if necessary
 * (as method result).
 * Note: "stanza" refers to first depth xml nodes.
 *
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

import xmlstreamparser.*;
import util.*;
import jabber.conversation.*;
import jabber.roster.*;
import jabber.presence.*;

import javax.microedition.io.*;
import java.io.*;
import java.util.*;


public class StanzaReader {
    ExceptionListener exceptionListener;
    ConversationListener conversationListener;
    PresenceListener presenceListener;
    
    // common attributes for stanzas
    protected String stanzaId;
    protected String stanzaType;
    protected String stanzaFrom;
    protected String stanzaTo;
    
    protected int internalstate;
    protected final int WAIT_LOGIN_PARAMS = 0;
    protected final int WAIT_LOGIN_RESULT = 1;
    protected final int CONNECTION_COMPLETED = 2;
    
    public StanzaReader(ExceptionListener _exceptionListener, 
                        ConversationListener _conversationListener,
                        PresenceListener _presenceListener) {
        exceptionListener = _exceptionListener;
        conversationListener = _conversationListener;
        presenceListener = _presenceListener;
        
        internalstate = 0;
    }
    
    public void read(Node _node) {
        // common attributes for stanzas:
        stanzaId = _node.getValue("id");
        stanzaType = _node.getValue("type");
        stanzaFrom = _node.getValue("from");
        stanzaTo = _node.getValue("to");
        
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
    }
    
    protected void readIq(Node _node) {
        System.out.println("+readIQ+");
        
        String res = "";

        if (stanzaType.equals("error")) {
            exceptionListener.reportException(new Exception("Stanza IQ Error"));
        }
        
        if (internalstate == WAIT_LOGIN_PARAMS) {
            if (_node.getChild("query").getChild("digest") == null) {
                exceptionListener.reportException(new Exception("Unsecure Server"));
            }
            
            String resourceNode = "";
            if (_node.getChild("query").getChild("resource") != null) {
                resourceNode = "<resource>" + Util.escapeCDATA(Datas.jid.getResource()) +"</resource>";
            } // else forget about the resource
            
            res += "<iq id='s2' type='set'>" +
            "<query xmlns='jabber:iq:auth'>" +
            "<username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username>" +
            resourceNode +
            "<digest>" + Datas.getDigestPassword() + "</digest>" +
            "</query>" +
            "</iq>";
            
            internalstate = WAIT_LOGIN_RESULT;
        }
        else if (internalstate == WAIT_LOGIN_RESULT) {
            if (stanzaType.equals("error")) {
                exceptionListener.reportException(new Exception("Stanza IQ Error"));
            }
            // we are connected
            
            res += "<presence/>"; // sends the presence
            internalstate = CONNECTION_COMPLETED;
            
            // midlet.connectedEvent(); ***
        }
        else if (internalstate == CONNECTION_COMPLETED) {

        }
        
        if (res != "") {
            Datas.writerThread.write(res);
        }
    }
    
    protected void readPresence(Node _node) {
        System.out.println("+readPRESENCE+");
        
        // default stanza type if not specified.
        if (stanzaType == null) {
            stanzaType = "online";
        }
        
        // ignored children: show (away, dnd, xa..), status
        
        
        if (stanzaType == "error") {
            // error! should do something here ***
        } else if (stanzaType == "online") {
            // roster is online
        } else if (stanzaType == "unavailable") {
            // roster is unavailable
        } else if (stanzaType == "subscribe") {
            // roster wants to subscribe for my presence events
        } else if (stanzaType == "subscribed") {
            // roster granted my "subscribe wish"
        } else if (stanzaType == "unsubscribe") {
            // roster wants to unsubscribe for my presence events
        } else if (stanzaType == "unsubscribed") {
            // roster unsubscribed me for his presence events
            // or my "subscribe wish" was declined
        } else if (stanzaType == "probe") {
            // roster/server probes my presence
        }
    }
    
    protected void readMessage(Node _node) {
        System.out.println("+readMESSAGE+");
        
        Message message = new Message(_node); // takes the subject&body of the message
        String threadId = "";
        Conversation conversation = null; // conversation corresponding to the message
        Roster rosterFrom;
        
        try {
            threadId = _node.getChild("thread").text;
        } catch (NullPointerException e) {
            // _node has no "thread" child
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
            
            // gets the calling roster, creates one if necessary
            //rosterFrom = null; // rosterManager.getByJID(stanzaFrom); ***
            rosterFrom = (Roster) Datas.rosters.get(stanzaFrom);
            if (rosterFrom == null) {
                // the roster is not known
                rosterFrom = new Roster(new Jid(stanzaFrom));
                Datas.rosters.put(stanzaFrom, rosterFrom);
            }
            
            if (stanzaType.equals("error")) {
                conversationListener.notifyError(null, message);
            }
            else if (stanzaType.equals("normal") 
                || stanzaType.equals("chat") 
                || stanzaType.equals("groupchat")) {
                    // normal: default message type. reply expected. no history.
                    // chat: peer to peer communication. with history.
                    // groupchat: many to many communication.
                    //System.out.println("G");
                    SingleChat chat = new SingleChat(rosterFrom, stanzaType, threadId);
                    chat.appendToMe(message);
                    // registers this new conversation
                    conversations.addElement(chat);
                    conversationListener.newConversationEvent(chat);
                }
                else if (stanzaType.equals("headline")) {
                    // no reply expected. (e.g. news, ads...)
                    conversation = new Conversation(rosterFrom.name);
                    conversation.appendToMe(message);
                    // registers this new conversation
                    conversations.addElement(conversation);
                    conversationListener.newConversationEvent(conversation);
                }
                
        } else {
            // conversation already exists
            if (stanzaType.equals("error")) {
                // should find the message in the conversation, append the error to it and
                // display the error msg ***
                conversationListener.notifyError(conversation, message);
            } else {
                conversation.appendToMe(message);
                //conversationListener.newMessageEvent(conversation);
                conversationListener.newMessageEvent(conversation);
            }
        }
    }
    
}
