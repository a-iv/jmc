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

public class MyMidlet
extends MIDlet
implements CommandListener, ConversationListener, PresenceListener {
    
   // fields
    CommunicationManager cm;
    
    Hashtable commands;  // register the commands
    // *** i could use another hashtable, to register an object with the command. (the corresponding conversation, roster, ...)
    
    Display display;
    
    // to remember the *displayed* state
    int internal_state;
    final static int OFFLINE         = 0;
    final static int ONLINE          = 1;
    final static int ROSTER          = 2;
    final static int CONVERSATION    = 3;
    final static int MANAGE_CONTACTS = 4;
    final static int PARAMS          = 5;
    final static int WAIT_CONNECT    = 6;
    final static int WAIT_DISCONNECT = 7;
    final static int ROSTER_DETAILS  = 8;
    final static int ROSTER_LIST     = 9;
    
    // to remember which roster/conversation is displayed
    Conversation currentConversation;
    RosterList currentRosterList;
    Roster currentRoster;
    
    Hashtable infopool; // contains highly dynamic data. i.e: TextFields in forms...
     // I could save the textfields for each chat, so it does not get cleared when i go to the main menu.
     // TextFields:
     // username
     // password
     
     
   // standard methods & constructor
    public MyMidlet() {
        Datas.load();
        cm = new CommunicationManager(this);
        
        infopool = new Hashtable();
        commands = new Hashtable();
    }
    
    public void startApp() {
        display = Display.getDisplay(this);
        
        display.setCurrent(getGuiSplashScreen(), getGuiOfflineMenu());
    }
    
    public void pauseApp() {}
    
    public void destroyApp(boolean unconditional) {}
    
   // gui 
    public Displayable getGuiOfflineMenu() {
        Form res = new Form("offline");
        
        res.addCommand(registerCommand("connect", "Connect", Command.OK, 0));
        res.addCommand(registerCommand("contacts", "Contacts", Command.SCREEN, 2));
        res.addCommand(registerCommand("options", "Options", Command.SCREEN, 3));
        res.addCommand(registerCommand("exit", "Exit", Command.EXIT, 1));
        
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionOfflineMenu(String id) {
        if (id.equals("connect")) {
            display.setCurrent(getGuiWaitConnect());
            internal_state = WAIT_CONNECT;
            cm.connect();
        } else if (id.equals("contacts")) {
            display.setCurrent(getGuiManageContacts());
            internal_state = MANAGE_CONTACTS;
        } else if (id.equals("options")) {
            display.setCurrent(getGuiParams());
            internal_state = PARAMS;
        } else if (id.equals("exit")) {
            notifyDestroyed();
        }
    }
    
    public Displayable getGuiOnlineMenu() {
        Displayable res = new Form("online");
        
        // need to synchronize operations? ***
        Vector conversations = Datas.conversations;
        Conversation conv;
        for (int i=0; i<conversations.size(); i++) {
            conv = (Conversation) conversations.elementAt(i);
            res.addCommand(registerCommand("conversation "+i, conv.name, Command.SCREEN, 3));
        }
        
        // need to synchronize operations? ***
        Vector lists = Datas.rosterClasses.getLists();
        RosterList list;
        for (int i=0; i<lists.size(); i++) {
            list = (RosterList) lists.elementAt(i);
            res.addCommand(registerCommand("list " + i, list.name, Command.SCREEN, 7+i));
        }

        res.addCommand(registerCommand("manage", "Manage", Command.SCREEN, 2));
        res.addCommand(registerCommand("disconnect", "Disconnect", Command.EXIT, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionOnlineMenu(String id) {
        if (id.equals("disconnect")) {
            // disconnecting...
            //display.setCurrent(new Alert("OFFLINE", "You are now offline", null, Alert.CONFIRMATION), getGuiOffline());
            cm.terminateStream();
            display.setCurrent(getGuiOfflineMenu());
            internal_state = OFFLINE;
        } else if (id.equals("manage")) {
            display.setCurrent(getGuiManageContacts());
            internal_state = MANAGE_CONTACTS;
        } else if (id.startsWith("conversation")) {
            // ID of the conversation
            int conversationId = Integer.parseInt(id.substring(13));
            Vector conversations = Datas.conversations;
            currentConversation = (Conversation) conversations.elementAt(conversationId);
            display.setCurrent(getGuiConversation());
            internal_state = CONVERSATION;
        }/* else if (id.startsWith("roster")) {
            // ID of the roster
            rosterId = Integer.parseInt(id.substring(7));
            display.setCurrent(getGuiRoster());
            internal_state = ROSTER;
        }*/ else if (id.startsWith("list")) {
            int listId = Integer.parseInt(id.substring(5));
            currentRosterList = (RosterList) Datas.rosterClasses.getLists().elementAt(listId);
            display.setCurrent(getGuiRosterList());
            internal_state = ROSTER_LIST;
        }
    }
    
    public Displayable getGuiRosterList() {
        Displayable res = new Form(currentRosterList.name);
        
        Vector rosters = currentRosterList.rosters;
        Roster roster;
        for (int i=0; i<rosters.size(); i++) {
            roster = (Roster) rosters.elementAt(i);
            res.addCommand(registerCommand("roster " + i, roster.name, Command.SCREEN, 7+i));
        }
        
        res.addCommand(registerCommand("cancel", "Cancel", Command.EXIT, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionRosterList(String id) {
        if (id.equals("cancel")) {
            display.setCurrent(getGuiOnlineMenu());
            internal_state = ONLINE;
        } else if (id.startsWith("roster")) {
            int rosterId = Integer.parseInt(id.substring(7));
            currentRoster = (Roster) currentRosterList.rosters.elementAt(rosterId);
            display.setCurrent(getGuiRoster());
            internal_state = ROSTER;
        }
    }
    
    public Displayable getGuiRoster() {
        Form res = new Form(currentRoster.name);
        
        res.addCommand(registerCommand("chat", "send message", Command.OK, 3));
        res.addCommand(registerCommand("details", "Details", Command.SCREEN, 2));
        res.addCommand(registerCommand("cancel", "Cancel", Command.EXIT, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionRoster(String id) {
        if (id.equals("cancel")) {
            display.setCurrent(getGuiRosterList());
            internal_state = ROSTER_LIST;
        } else if (id.equals("details")) {
            display.setCurrent(getGuiRosterDetails());
            internal_state = ROSTER_DETAILS;
        } else if (id.equals("chat")) {
            // sets up a new conversation ***
            currentConversation = new SingleChat(currentRoster, "chat", "");
            Vector conversations = Datas.conversations;
            conversations.addElement(currentConversation);
            display.setCurrent(getGuiConversation());
            internal_state = CONVERSATION;
        }
    }
    
    public Displayable getGuiRosterDetails() {
        Form res = new Form(currentRoster.name);
        TextField jid = new TextField("jid", currentRoster.jid.getLittleJid(), 32, TextField.ANY);
        infopool.put("jid", jid);
        res.append(jid);
        
        res.addCommand(registerCommand("ok", "ok", Command.OK, 3));
        res.addCommand(registerCommand("cancel", "Cancel", Command.CANCEL, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionRosterDetails(String id) {
        if (id.equals("ok")) {
            TextField jid = (TextField) infopool.remove("jid");
            boolean changes = true; // test if there are changes
            if (changes) {
                currentRoster.jid = new Jid(jid.getString());
                display.setCurrent(new Alert("OK", "Changes Saved", null, AlertType.CONFIRMATION), getGuiRoster());
            } else {
                display.setCurrent(getGuiRoster());
            }
            internal_state = ROSTER;
        } else if (id.equals("cancel")) {
            display.setCurrent(getGuiRoster());
            internal_state = ROSTER;
        } 
    }
    
    public Displayable getGuiConversation() {
        boolean canAnswer = currentConversation.canAnswer();
        
        Form res = new Form(currentConversation.name);
        Vector msgs = currentConversation.messages;
        Message msg;
        for (int i=0; i<msgs.size(); i++) {
            msg = (Message) msgs.elementAt(i);
            res.append(msg.getText());
        }
        if (canAnswer) {
            TextField tf = new TextField(">", "", 64, TextField.ANY);
            infopool.put("text2send", tf);
            res.append(tf);
            res.addCommand(registerCommand("send", "send", Command.OK, 0));
        }
        
        res.addCommand(registerCommand("back", "back", Command.EXIT, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionConversation(String id) {
        if (id.equals("back")) {
            display.setCurrent(getGuiOnlineMenu());
            internal_state = ONLINE;
        } else if (id.equals("send")) {
            TextField tf = (TextField) infopool.remove("text2send");
            Message msg = new Message("", tf.getString());
            ((Chat) currentConversation).appendFromMe(msg);
            display.setCurrent(getGuiConversation());
        }
    }
    
    public Displayable getGuiManageContacts() {
        Displayable res = new Form("contacts");
        
        res.addCommand(registerCommand("exit", "Exit", Command.EXIT, 1));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionManageContacts(String id) {
        // ***
        if (id.equals("exit")) {
            display.setCurrent(getGuiOfflineMenu());
            internal_state = OFFLINE;
        }
    }
    
    public Displayable getGuiParams() {
        Form res = new Form("params");
        
        TextField jid = new TextField("im-address", Datas.jid.getFullJid(), 32, TextField.ANY);
        TextField password = new TextField("password", "", 32, TextField.PASSWORD);
        infopool.put("jid", jid);
        infopool.put("password", password);
        res.append(jid);
        res.append(password);
        res.addCommand(registerCommand("cancel", "Cancel", Command.CANCEL, 0));
        res.addCommand(registerCommand("ok", "Ok", Command.OK, 0));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionParams(String id) {
        if (id.equals("ok")) {
            TextField jid = (TextField) infopool.remove("jid");
            TextField password = (TextField) infopool.remove("password");
            Datas.jid = new Jid(jid.getString());
            Datas.setPassword(password.getString());
            display.setCurrent(new Alert("OK", "Changes Saved", null, AlertType.CONFIRMATION), getGuiOfflineMenu());
            internal_state = OFFLINE;
        } else if (id.equals("cancel")) {
            display.setCurrent(getGuiOfflineMenu());
            internal_state = OFFLINE;
        }
    }
    
    public Alert getGuiSplashScreen() {
        Image logo = null;
        
        try {
            logo = Image.createImage("/splash.png");
        }
        catch (java.io.IOException e) {
        }
        
        Alert res = new Alert("LBS-IM", "Greg Inc.", logo, null);
        res.setTimeout(Alert.FOREVER);
        return res;
    }
    
    public Displayable getGuiWaitConnect() {
        Form res = new Form("Connecting");
        res.append("Please wait...");
        res.addCommand(registerCommand("cancel", "Cancel", Command.CANCEL, 0));
        res.setCommandListener(this);
        
        return res;
    }
    
    public void commandActionWaitConnect(String id) {
        if (id.equals("cancel")) {
            cm.disconnect(); // make a new cm?
            display.setCurrent(getGuiOfflineMenu());
            internal_state = OFFLINE;
        }
    }
    
   // command action 
    public void commandAction(Command _c, Displayable _d) {
        String id = (String) commands.get(_c);
        commands.clear();
        
        switch (internal_state) {
            case OFFLINE: commandActionOfflineMenu(id); break;
            case ONLINE : commandActionOnlineMenu(id); break;
            case ROSTER : commandActionRoster(id); break;
            case CONVERSATION : commandActionConversation(id); break;
            case MANAGE_CONTACTS : commandActionManageContacts(id); break;
            case PARAMS : commandActionParams(id); break;
            case WAIT_CONNECT : commandActionWaitConnect(id); break;
            case ROSTER_LIST : commandActionRosterList(id); break;
            case ROSTER_DETAILS : commandActionRosterDetails(id); break;
        }
        
        // another SWITCH internal_state ***
        
    }
    
   // events from the CommunicationManager
    public void connectedEvent() {
        display.setCurrent(getGuiOnlineMenu());
        internal_state = ONLINE;
    }
    
    public void disconnectedEvent() {
        //display.setCurrent(getGuiOfflineMenu());
        display.setCurrent(new Alert("DISCONNECTED", "You have been disconnected", null, AlertType.CONFIRMATION), getGuiOfflineMenu());
        internal_state = OFFLINE;
    }
    
   // ConversationListener
    public void notifyError(Conversation _conversation, Message _errorMessage) {
        // should insert&display the error in the conversation history?
        String text = _errorMessage.getText();
        Alert a = new Alert("Error", text, null, AlertType.INFO);
        a.setTimeout(Alert.FOREVER);
        display.setCurrent(a);
    }
    
    public void newConversationEvent(Conversation _conv) {
        System.out.println("NEW_CONVERSATION_EVENT");
        //String title = "from " + _conv.name;
        System.out.println("name= " + _conv.name);

        if (internal_state == ONLINE) {
            // update the online menu
            display = Display.getDisplay(this);
            display.setCurrent(getGuiOnlineMenu());
        } else if ((internal_state == CONVERSATION) && 
            (_conv == currentConversation)) {
            // update if its the current conversation
            display = Display.getDisplay(this);
            display.setCurrent(getGuiConversation());
        } else {
            // send an alert
            display.setCurrent(new Alert("New Message", "go in main menu", null, AlertType.INFO));
        }
        
        System.out.println("end of event");
    }
    
    public void newMessageEvent(Conversation _conv) {
        // need to update the display?
        
        System.out.println("NEW_MESSAGE_EVENT");
        String title = "from " + _conv.name;
        
        Message lastMessage = (Message) _conv.messages.lastElement();
        String text = lastMessage.getText();
        
        if ((internal_state == CONVERSATION) && 
            (_conv == currentConversation)) {
            // update if its the current conversation
            display.setCurrent(getGuiConversation());
        } else {
            // send an alert
            display.setCurrent(new Alert(title, text, null, AlertType.INFO));
        }
    }
    
   // PresenceListener
    public void notifyPresence(Roster _roster, int _presence) {
        // need to update the display?
        if ((internal_state == ROSTER) && (_roster == currentRoster)) {
            display.setCurrent(getGuiRoster());
        } else {
            // send an alert
            String title = _roster.jid.getLittleJid();
            String body = _roster.name + Presence.getPresence(_presence);
            display.setCurrent(new Alert(title, body, null, AlertType.INFO));
        }
    }
    
   // other methods 
    public Command registerCommand(String _id, String _label, int _commandType, int _priority) {
        Command res = new Command(_label, _commandType, _priority);
        commands.put(res, _id);
        return res;
    }
    
}
