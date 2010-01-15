/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package jmc;

import jmc.connection.CommunicationManager;
import jmc.media.MediaManager;
import threads.SMSThread;
import threads.PushThread;
import javax.microedition.io.PushRegistry;
import jabber.conversation.*;
import jabber.roster.Jid;
import jabber.roster.Jud;
import jabber.presence.Presence;
import util.Datas;
import util.Contents;
import jabber.subscription.*;


import javax.microedition.midlet.MIDlet;
import java.util.Vector;
import java.util.Hashtable;

import java.util.Enumeration;
import com.sun.lwuit.Button;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;

//import com.sun.lwuit.animations.CommonTransitions;
//import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
//import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import com.sun.lwuit.Dialog;

/**
 *  
 *
 * JMC GUI Midlet
 * @author Gabriele Bianchi
 */
public class GuiMidlet
extends MIDlet implements ActionListener
/*implements CommandListener, ItemCommandListener*/ {
	
	// fields
	public CommunicationManager cm;
	private MidletEventListener listener;
	
	public Display display;
	
	// to remember the *displayed* state
	public int internal_state;
	boolean history = false;
	public final static int OFFLINE         = 0;
	public final static int ONLINE          = 1;
	public final static int ROSTER          = 2;
	public final static int CONVERSATION    = 3;
	public final static int SUBSCRIPTION    = 4;	
	public final static int PARAMS          = 5;
	public final static int WAIT_CONNECT    = 6;
	public final static int WAIT_DISCONNECT = 7;
	public final static int ROSTER_DETAILS  = 8;
	public final static int MULTI_CHAT      = 9;
	public final static int INVITATION      = 10;
	public final static int OPTIONS	     = 11;
	public final static int JUD		     = 12;
	private static final int STATUS = 13;
	
	// to remember which roster/conversation is displayed
	Conversation currentConversation;
	Jid currentjid;
	
	Enumeration contacts = null;
	
	Form offLineMenu =null;	 
	Form wait_form = null;
	Form options_form = null;
	
	//Form params_form = null;
	ContactList contacts_list = null;
	ButtonGroup status_list = null;
	Form conv_list = null;
	//ChoiceGroup static_menu = null;
	//ChoiceGroup openrooms = null;
	ButtonGroup yesno = null;
	//ChoiceGroup options_list = null;
	//ChoiceGroup jud_list = null;
	ButtonGroup ssl_list = null;
	ButtonGroup avatar_list = null;

	Form mainForm = null;
	Container conversationForm = null;
	public TabbedPane tabbedPane = null;
	Hashtable infopool; // contains highly dynamic data. i.e: TextFields in forms...
	Hashtable buttonpool;
	Vector roster;
	
	
	
	public  GuiMidlet() {
	
		infopool = new Hashtable(5);
		infopool.put("hide", Contents.hide[1]);
		//font = Font.getDefaultFont();
		
		
		listener = new MidletEventListener(this);
		cm = new CommunicationManager(listener);
		
	}
	
	public void startApp() {
//		 if (display == null) { //thanks to Azlan
//     		display = Display.getDisplay(this);
//     		display.setCurrent(getGuiIntroScreen(), getGuiOfflineMenu());
//				Datas.load();
//				listener.display = display;
//				handlePushActivation();
//				
//     }
//		 else {
//			if (internal_state != OFFLINE && internal_state != WAIT_CONNECT && Datas.jid != null)
//				Datas.jid.setPresence(Presence.getPresence("online")); //change user status
//		 }
		
		try {
            Display.init(this);
            Resources r1 = Resources.open("/starTheme.res");
            UIManager.getInstance().setThemeProps(r1.getTheme(r1.getThemeResourceNames()[0]));
            getGuiIntroScreen();
            
            Datas.load();
            handlePushActivation();
           
           
            getGuiOfflineMenu();
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void pauseApp() {
		if (internal_state != OFFLINE && internal_state != WAIT_CONNECT && Datas.jid != null)
			Datas.jid.setPresence(Presence.getPresence("away")); //change user status
	}
	
	public void destroyApp(boolean unconditional) {notifyDestroyed();}
	
	/**
	 * Show the first menu (offline)
	 * @return
	 */
	public void getGuiOfflineMenu() {

		offLineMenu = new Form(Contents.offline_form);
		offLineMenu.setLayout(new BorderLayout());
      //  int width = Display.getInstance().getDisplayWidth(); //get the display width 
        Container mainContainer;
        int elementWidth = 0;

        mainContainer = new Container();
        //Image[] selectedImages = new Image[DEMOS.length];
        Image[] unselectedImages = new Image[4];
     
        //ButtonActionListener bAListner = new ButtonActionListener();
        //for (int i = 0; i < DEMOS.length; i++) {
        unselectedImages[0] = Contents.displayImage("connected");
        unselectedImages[1] = Contents.displayImage("settings");
        unselectedImages[2] = Contents.displayImage("unsubscribed");
        unselectedImages[3] = Contents.displayImage("choice");
          //  selectedImages[i] = temp;
        ButtonActionListener action = new ButtonActionListener();
        for (int i = 0; i < unselectedImages.length; i++) {
            Button b = new Button(Contents.offlineChoices[i], unselectedImages[i]);
           // b.setRolloverIcon(selectedImages[i]);
            b.setPressedIcon(unselectedImages[i].scaled( (int) (unselectedImages[i].getWidth()* 0.8), 
                                             (int)(unselectedImages[i].getHeight() *0.8) ));
            b.getStyle().setBgTransparency(0);
            b.getStyle().setBorder(Border.createLineBorder(1));
            b.setAlignment(Label.CENTER);
            b.setTextPosition(Label.BOTTOM);
            mainContainer.addComponent(b);
            b.addActionListener(action);
          //  buttonpool.put(b, Contents.offlineChoices[i]);
            elementWidth = Math.max(b.getPreferredW(), elementWidth);
        }
            
        //Calculate the number of columns for the GridLayout according to the 
        //screen width
        int cols = 2;//width / elementWidth;
        int rows = unselectedImages.length / cols;
        mainContainer.setLayout(new GridLayout(rows, cols));

        offLineMenu.addComponent(BorderLayout.CENTER, mainContainer);
		
		
		/*offLineMenu = new ChoiceGroup("OffLine", List.EXCLUSIVE, Contents.offlineChoices, null);
		offLineMenu.setFont(3, Font.getFont(font.getFace(), Font.STYLE_ITALIC, font.getSize()));
		
	
		res.append(offLineMenu);
		res.append(new util.CustomSpacer(res.getWidth(), res.getHeight()));
		*/offLineMenu.addCommand(Contents.ok);
		offLineMenu.addCommand(Contents.exit);
		
		offLineMenu.setCommandListener(this);
		offLineMenu.show();
		//return res;
	}
	
	public void commandActionOfflineMenu(Command id) {
		if (id == Contents.ok) {
			Button b = (Button)offLineMenu.getFocused();
			if (b.getText().equals(Contents.offlineChoices[0])) {
				if (Datas.noData) { //conf data set ?
					internal_state = PARAMS;
					Dialog.show("", Contents.noData, null, Dialog.TYPE_WARNING, null, 3000);
					getGuiParams();
				}else {
					getGuiWaitConnect();
					internal_state = WAIT_CONNECT;
					if (Datas.isHTTP)
						cm.httpConnect(); //HTTP
					else
						cm.connect(0); //TCP
				}
			}
		  	
			else if (b.getText().equals(Contents.offlineChoices[1])) {
				getGuiParams();
				internal_state = PARAMS;
			}
		/*	else if (offLineMenu.getSelectedIndex() == 2)
			{
				display.setCurrent(new WSForm(this));
				internal_state = OFFLINE;
			}*/ 
			else if (b.getText().equals(Contents.offlineChoices[2])) {
				
				
				Dialog.show("Help", Contents.help, "Ok", "");
				getGuiOfflineMenu();
				internal_state = OFFLINE;
			}
			else if (b.getText().equals(Contents.offlineChoices[3])) {
				
				
				Dialog.show("Credits", Contents.credits, "Ok", "");
				getGuiOfflineMenu();
				//internal_state = OFFLINE;
			} 
			else
				System.out.println("Error: choice not chosen");
		
		}
		else if (id == Contents.exit)
		{
			notifyDestroyed();
		}
	}
	
	/**
	 * Show the main menu (online)
	 * @return
	 */
	public void getGuiOnlineMenu() {
        internal_state = ONLINE;
    	mainForm = new Form(Datas.jid.getUsername() + " (" + Datas.jid.getPresence()+")");
    	mainForm.setLayout(new BorderLayout());
    	mainForm.setScrollable(false);
    	mainForm.getStyle().setBgTransparency(0);
    	tabbedPane = new TabbedPane();
		tabbedPane.addTabsListener(listener); 
		tabbedPane.getStyle().setBgTransparency(0);
    		
        //}else
        	//mainForm.removeAllCommands();
		Container contacts = new Container(new BoxLayout(BoxLayout.Y_AXIS));
		contacts.getStyle().setBorder(Border.createEmpty());
		if (Datas.jid.status_message != null && !Datas.jid.status_message.equals("")){
			Label mood = new Label(Datas.jid.status_message);
			mood.getStyle().setBgTransparency(0);
			contacts.addComponent(mood); 
		}
        /*ContactList*/ contacts_list = new ContactList();
        contacts_list.getStyle().setBgTransparency(0);
        contacts_list.getStyle().setBorder(Border.createEmpty());
        contacts_list.setListCellRenderer(new ContactsRenderer());
        contacts_list.setSmoothScrolling(true);
        //contacts_list.setFixedSelection(List.FIXED_LEAD); //da testare
        contacts_list.addActionListener(contacts_list);
        /*Image contacts = null;
        Image  persons[] = null;
		*/
	
			
		String hide = (String)infopool.get("hide");
		if (hide.equals(Contents.hide[1]))
			roster = Datas.createOnlineRosterVector(true); 
		else
			roster = Datas.createRosterVector(true);
		if (roster.size() > 0) {
			//contacts_list = new ChoiceGroup("Contacts", ChoiceGroup.EXCLUSIVE);
			for (int i=0;i< roster.size(); i++) {				
				Jid temp = (Jid)roster.elementAt(i);
				/*String username = temp.getUsername().replace('%', '@');
				contacts_list.append(username, Datas.images.displayImage(temp.getPresence()));*/
				contacts_list.addItem(temp);
			}
			
		}else if (hide.equals(Contents.hide[0]))
			contacts.addComponent(new Label(Contents.noRoster));
		
		Button b = new Button(hide);
		//TODO: cambiare lo style
		b.setAlignment(Label.CENTER); 
		b.getStyle().setBgTransparency(100);
		b.getStyle().setBorder(Border.createEmpty());
		//b.getStyle().setBgSelectionColor(bgSelectionColor);
		//b.getStyle().setFgSelectionColor(fgSelectionColor);
		b.addActionListener(new ButtonActionListener());
		contacts.addComponent(b);
		contacts.addComponent(contacts_list);
		//contacts_list.append(hide, null);
		//contacts_list.setFont(contacts_list.size() - 1, Font.getFont(font.getFace(), Font.STYLE_ITALIC, font.getSize()));
		
		//valutare come trasformare questo


		/*StringItem choiceHide = new StringItem(hide, "", Item.HYPERLINK);
		choiceHide.addCommand(Contents.select);
		choiceHide.setItemCommandListener(this);		
		res.append(choiceHide);
		*/	
		
	/*	if (tabbedPane.getTabCount() > 0) {
			tabbedPane.removeTabAt(0);
			tabbedPane.insertTab("Contacts", null, contacts, 0);
		}else*/
		tabbedPane.addTab("Contacts", contacts);
			//PER IL MOMENTO RICOSTRUIsco MAINFORM TUTTE LE VOLTE
		if (Datas.conversations.size() > 0)
		{
		
			Vector chats = Datas.conversations;
			
			for (int k = 0; k < chats.size(); k++)
			{
				Conversation c = (Conversation)chats.elementAt(k); 
				String name = c.name;
				if (name.indexOf("@") != -1) {
					name = name.substring(0, name.indexOf("@"));
				}
				tabbedPane.addTab(name, new Container());
			}

		}
		//tabbedPane.setSelectedIndex(0);	
		mainForm.addCommand(Contents.disc, 0);
		mainForm.addCommand(Contents.chat, 1);
		mainForm.addCommand(Contents.info, 2);
		 
		
		mainForm.setCommandListener(this);
		mainForm.addComponent(BorderLayout.CENTER, tabbedPane);
		//res.addCommand(Contents.select);
		
		
		mainForm.show();
	}
	
	public void commandActionOnlineMenu(Command id)
	{ 
		if (id == Contents.disc)
		{
			getGuiOtherOptions();
		} 
		else if (id == Contents.chat) { 
			try {
				currentjid = (Jid)contacts_list.getSelectedItem();
                if (currentjid == null)
                    return;
				//System.out.println("Selected user:"+currentjid.getUsername());
			}catch (Exception e) {return;}
			if (Presence.getPresence("unsubscribed").equals(currentjid.getPresence()))
				{
					// try to subscribe to the item
					internal_state = ONLINE;
					Subscribe.requestSubscription(currentjid);

					Dialog.show("",Contents.subs,null,Dialog.TYPE_CONFIRMATION, null,3000); 
					getGuiOnlineMenu();

				}
			else {
					//boolean found = false;
					Conversation c1 = null;
					Vector conversations = Datas.conversations;
					//look for an exsisting Conversation
					for (int i=0; i< conversations.size(); i++) {
						c1 = (Conversation)conversations.elementAt(i);
						if (c1.name.equals(currentjid.getUsername())) {
							
							currentConversation = c1;
							internal_state = CONVERSATION;
							//tabbedPane.setSelectedIndex(i+1);
							getGuiConversation(i+1);
							return;
						}
					}
					
					// sets up a new conversation 
					currentConversation = new SingleChat(currentjid, "chat", "");					
					conversations.addElement(currentConversation);
				  	//tabbedPane.addTab(currentConversation.name, new Container()); //pensare se fare sta cosa dentro getGuiConversation
					internal_state = CONVERSATION;
					//
					getGuiConversation(0); 
					//tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
					//System.out.println("end of Selected user:"+currentjid.getUsername());
				}
			
			
			
		}else if (id == Contents.info) {
			try {
				currentjid = (Jid)contacts_list.getSelectedItem();
                if (currentjid == null)
                    return;
			}catch (Exception e) {return;}
			if (currentjid.phone == null)
					Subscribe.getPhoneNumber(currentjid);//get phone number..
			internal_state = ROSTER;
			getGuiRosterItem();
			
		}
		
	
		return;
		
	}
	
	class ContactsRenderer extends Container implements ListCellRenderer {

        private Label name = new Label("");
        private Label status = new Label("");
        private Label pic = new Label("");
        private int display_width;
        private Label focus = new Label("");
        
        public ContactsRenderer() {
            setLayout(new BorderLayout());
            addComponent(BorderLayout.WEST, pic);
            Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));
            name.getStyle().setBgTransparency(0);
            name.getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            name.getStyle().setFgColor(0xF1F57C);
            status.getStyle().setBgTransparency(0);
            
            display_width = Display.getInstance().getDisplayWidth();
            //location.getStyle().setFgColor(0xF1F57C); TODO cambiare colore
            pic.getStyle().setBgTransparency(0);
            cnt.addComponent(name);
            cnt.addComponent(status);
            addComponent(BorderLayout.CENTER, cnt);
            
            focus.getStyle().setBgTransparency(255);
        }

        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {

            Jid person = (Jid) value;
            name.setText(person.getNickname());
            status.setText(person.getPresence());
            /*if (person.getPresence().equalsIgnoreCase(Contents.mystring_presence[3])) { //busy

            	status.getStyle().setFgColor(0xFF0000);
            }*/
            Image img = Jid.createAvatar(person.getAvatar());
            if (img.getWidth() != 32)
            	img = img.scaled(32, 32);
            pic.setIcon(img); 
            pic.setPreferredSize(new Dimension(32,32));
            this.setPreferredSize(new Dimension(display_width, 40));
            return this;
        }

        public Component getListFocusComponent(List list) {
            return focus;
        }

	
		
    }
	
	
	
	/**
	 * Show other options of the online menu
	 * @return Form
	 */
	public void getGuiOtherOptions() {
		options_form = new Form(Contents.options_form);
		internal_state = OPTIONS;
		
		
		if (infopool.containsKey("ServerInfo")) {//server info
			options_form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
			options_form.removeCommand(Contents.ok);
			infopool.remove("ServerInfo");
			if (Datas.server_services.size() > 0) {
	
				options_form.addComponent(new MyLabel("Server Services:"));
			
				Vector serv = Datas.server_services;
				
				for (int k=0;k<serv.size();k++) {
					String lab[] = ((String[])serv.elementAt(k));
					//String name = ((StringItem)serv.elementAt(k)).getText();
					System.out.println(lab[0]+" ("+lab[1]+")");
					MyTextArea l = new MyTextArea(lab[0]+" ("+lab[1]+")");
					l.setEditable(false);
					options_form.addComponent(l);
				}		
			
			}
			else
				options_form.addComponent(new MyLabel("Server Info not available"));			
			
		}
		else {
			options_form.setLayout(new BorderLayout());
	        //int width = Display.getInstance().getDisplayWidth(); //get the display width 
	        Container mainContainer;
	        int elementWidth = 0;

	        mainContainer = new Container();
	        //Image[] selectedImages = new Image[DEMOS.length];
	        //Image[] unselectedImages = new Image[3];
	        
			Image[] unselectedImages = new Image[] { Contents.displayImage("disconnected"), Contents.displayImage("add"), Contents.displayImage("msn"), Contents.displayImage("choice"), Contents.displayImage("presence"), Contents.displayImage("wake"), Contents.displayImage("message"), Contents.displayImage("invite") };
			
			ButtonActionListener action = new ButtonActionListener();
	        for (int i = 0; i < unselectedImages.length; i++) {
	            Button b = new Button(Contents.optionsChoices[i], unselectedImages[i]);
	           // b.setRolloverIcon(selectedImages[i]);
	            b.setPressedIcon(unselectedImages[i].scaled( (int) (unselectedImages[i].getWidth()* 0.8), 
	                                             (int)(unselectedImages[i].getHeight() *0.8) ));
	            b.getStyle().setBgTransparency(0);
	            b.getStyle().setBorder(Border.createLineBorder(1));
	            b.setAlignment(Label.CENTER);
	            b.setTextPosition(Label.BOTTOM);
	            mainContainer.addComponent(b);
	            b.addActionListener(action);
	          //  buttonpool.put(b, Contents.offlineChoices[i]);
	            elementWidth = Math.max(b.getPreferredW(), elementWidth);
	        }
	            
	        //Calculate the number of columns for the GridLayout according to the 
	        //screen width
	        int cols = 2;//width / elementWidth;
	        int rows = unselectedImages.length / cols;
	        mainContainer.setLayout(new GridLayout(rows, cols));

	        options_form.addComponent(BorderLayout.CENTER, mainContainer);
			
			
	        options_form.addCommand(Contents.ok);
		}
		//res.append(new util.CustomSpacer(res.getWidth(), res.getHeight()));
		
		options_form.addCommand(Contents.back);
		options_form.setCommandListener(this);
		
		options_form.show();
	}

	public void commandActionOtherOptions(Command id) {
		if (id == Contents.back){
			
			internal_state = ONLINE;
			getGuiOnlineMenu();
		}
		/*else if (infopool.containsKey("anonimousMsg")){
			String jid = ((TextField)infopool.remove("anonimousMsg")).getString();
			 //HO ELIMINATO IL SEND MESSAGE ANONIMO
			if (jid.equals("")) {
			  internal_state = ONLINE;
			  display.setCurrent(getGuiOnlineMenu());
			  return;
			}
			// sets up a new conversation	
			currentjid = new Jid(jid);
			currentConversation = new SingleChat(currentjid, "chat", "");
			Vector conversations = Datas.conversations;
			conversations.addElement(currentConversation);
			display.setCurrent(getGuiConversation());
			internal_state = CONVERSATION;
			System.gc();//garbage!!
		}*/
		else {
			Button b = (Button)options_form.getFocused();
			String text = b.getText();
			if (text.equals(Contents.optionsChoices[0])) {
				// disconnecting...
		//in offline mode, all chats are deleted
				Datas.multichat.clear();
				Datas.conversations.removeAllElements();
				Datas.conversations.trimToSize();
				Datas.server_services.removeAllElements();
				Datas.conversations.trimToSize();
				Datas.readRoster = false;
				cm.terminateStream();
				//System.gc();//garbage!
				getGuiOfflineMenu();
				internal_state = OFFLINE;
			}
			else if (text.equals(Contents.optionsChoices[1]))
			{
				//add new jabber roster item
				currentjid = null;
				getGuiRosterDetails();
				internal_state = ROSTER_DETAILS;
			}
			else if (text.equals(Contents.optionsChoices[2]))
			{
				//register to gateway
				if (GatewayForm.existGateway(Datas.server_services))
				{
					GatewayForm gtw = new GatewayForm(this);
					gtw.show();
				}
				else
				{
					internal_state = ONLINE;
					Dialog.show("", Contents.noJud, null, Dialog.TYPE_ERROR, null, 3000);
					getGuiOnlineMenu();
				}

			}
			else if (text.equals(Contents.optionsChoices[3]))
			{
				//join multichat
				internal_state = MULTI_CHAT;
				getGuiRoomList();
			}
			else if (text.equals(Contents.optionsChoices[4]))
			{
				//change status
				
				
				//infopool.put("status", status_list);
				getGuiChangeStatus();

			}
			else if (text.equals(Contents.optionsChoices[5])) //accept incoming sms
			{
				internal_state = ONLINE;
				PushThread pt = new PushThread(this);
				pt.start();
				getGuiOnlineMenu();
			//SI LEVA?? 
			}
			else if (text.equals(Contents.optionsChoices[6]))
			{
				//server info
				infopool.put("ServerInfo", "ServerInfo");
				getGuiOtherOptions();
			}
			else if (text.equals(Contents.optionsChoices[7]))
			{
				//JUD management
				//check if JUD exists
				if (infopool.containsKey("jud_add")) {
					internal_state = JUD;
					getGuiJudMenu();
					return;
				}
				Vector serv = Datas.server_services;
				
				for (int j = 0; j < serv.size(); j++)
				{
					String[] s = ((String[])serv.elementAt(j));
					String service = s[0];
					if (s[0] == null || s[0].equals(""))
						service = s[1];
					if (service.toLowerCase().indexOf("jud") != -1 || service.toLowerCase().indexOf("directory") != -1 || service.toLowerCase().indexOf("users") != -1 || service.toLowerCase().indexOf("user search") != -1)
					{ //TODO: da cambiare
						infopool.put("jud_add", s[1]);
						break;
					}
				}
				if (!infopool.containsKey("jud_add")) {
					Dialog.show("", Contents.noJud, null, Dialog.TYPE_ERROR, null, 3000);
				
					getGuiOtherOptions();
				}
				else
				{
					getGuiJudMenu();
					internal_state = JUD;
				}
			}
		}
	}

	public void getGuiChangeStatus() {
		Form status_form = new Form(Contents.choose_status);
		status_form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
	
        internal_state = STATUS;
		Image[] unselectedImages = new Image[] { Contents.displayImage("offline"), Contents.displayImage("online"), Contents.displayImage("away"), Contents.displayImage("busy")/*, Contents.displayImage("unsubscribed")*/ };	
		//status_list = new ChoiceGroup(Contents.choose_status, ChoiceGroup.EXCLUSIVE, Contents.mystring_presence, image);
		status_list = new ButtonGroup();
		for (int k = 0; k < Contents.mystring_presence.length; k++) {
			RadioButton rb = new RadioButton(Contents.mystring_presence[k], unselectedImages[k]);
			Style s = rb.getStyle();
	        s.setMargin(0, 0, 0, 0);
	       
	        rb.setPressedIcon(unselectedImages[k].scaled( (int) (unselectedImages[k].getWidth()* 0.8), 
                    (int)(unselectedImages[k].getHeight() *0.8) ));
	        rb.setAlignment(Label.LEFT);
            rb.setTextPosition(Label.RIGHT);
	        s.setBgTransparency(70);
	        status_list.add(rb);
	        status_form.addComponent(rb);
	        if (Contents.mystring_presence[k].equals(Datas.jid.getPresence())) {
				status_list.setSelected(k);
			}
		}
		status_form.addComponent(new MyLabel("Your mood"));
        TextArea mess = new TextArea(Datas.jid.status_message, 100);
        status_form.addComponent(mess);
        infopool.put("status_message", mess);
        status_form.addCommand(Contents.ok);
	
        status_form.addCommand(Contents.back);
        status_form.setCommandListener(this);
	
        status_form.show();
	}
    public void commandActionChangeStatus(Command id) {
    	String message = ((TextArea)infopool.remove("status_message")).getText();
    	if (id == Contents.ok) {
	    	int ind = status_list.getSelectedIndex();
	    	String status = Contents.mystring_presence[ind];
			
			
			//if (!status.equals(Datas.jid.getPresence())){
			Presence.changePresence(Presence.getPresence(ind), message);
				
			Datas.jid.setPresence(status, message);
		}
    	internal_state= ONLINE;
		getGuiOnlineMenu();
		
    }
	/**
	 * Show the details of a selected roster item
	 * @return
	 */
	public void getGuiRosterItem() {
		conv_list = new Form(currentjid.getUsername()/*"Group: " + currentjid.group + " Status: " + currentjid.getPresence()*/);
		String state = currentjid.status_message;
		conv_list.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		if (state != null && !state.equals("")) {
			MyTextArea t = new MyTextArea("Group: "+ currentjid.group + " Status: "+currentjid.getPresence());
			t.setEditable(false);
		    t.getStyle().setFgColor(0xF1F57C);
			conv_list.addComponent(t);
		}
		
		else state = "Group: "+ currentjid.group + " Status: "+currentjid.getPresence();
		Container info = new Container();
		info.setLayout(new GridLayout(1,2));
		info.getStyle().setBgTransparency(0);
	    Label avatar = new Label(Jid.createAvatar(currentjid.getAvatar()).scaled(32, 32));
        avatar.getStyle().setBgTransparency(0);
		if (avatar.getWidth() < 32) {
			avatar.setPreferredSize(new Dimension(32,32));
			
		}
		info.addComponent(avatar);//AVATAR
		MyTextArea t = new MyTextArea(state, 0 ,100);
        t.setEnabled(true);
		
	    t.getStyle().setFgColor(0xF1F57C);
		info.addComponent(t);
		conv_list.addComponent(info);
		//conv_list.setScrollable(false);
		
	/*
		Vector conversations = Datas.conversations;
		conv_list = new ChoiceGroup(currentjid.getUsername() + state, ChoiceGroup.EXCLUSIVE, Contents.rosterChoices, null);
		
		for (int i=0; i< conversations.size() && (Presence.getPresence("online").equals(currentjid.getPresence()) || Presence.getPresence("away").equals(currentjid.getPresence())); i++) {
			Conversation c = (Conversation)conversations.elementAt(i);
			if (c.name.equals(currentjid.getUsername())) {
				conv_list.append("Active Conversation "+(i+1), null); 
				infopool.put("convers", c);
			}
		}*/
		//TODO: valutare la possibilit� di mettere i bottoni come offlinemenu
		ButtonActionListener action = new ButtonActionListener();
        
		for (int k=0; k<Contents.rosterChoices.length; k++) {
        	Button b = new Button(Contents.rosterChoices[k]);
            b.getStyle().setBgTransparency(100);
            b.getStyle().setBorder(Border.createEmpty());
            b.addActionListener(action);
            conv_list.addComponent(b);
        }
        
		if (Presence.getPresence("unsubscribed").equals(currentjid.getPresence())){
			Button b = new Button(Contents.online_choices[1]);
            b.getStyle().setBgTransparency(100);
            b.getStyle().setBorder(Border.createEmpty());
            b.addActionListener(action);
            conv_list.addComponent(b);
			//conv_list.append(Contents.online_choices[1], null);//try to subscribe
			//conv_list.setFont(conv_list.size() - 1, Font.getFont(font.getFace(), Font.STYLE_BOLD, font.getSize()));
		}
		else if (Presence.getPresence("unavailable").equals(currentjid.getPresence())) {
			//wake-up with SMS
			Button b = new Button(Contents.online_choices[2]);
            b.getStyle().setBgTransparency(100);
            b.getStyle().setBorder(Border.createEmpty());
            b.addActionListener(action);
            conv_list.addComponent(b);
		}

		//res.append(conv_list);
		//res.append(new util.CustomSpacer(res.getWidth(), res.getHeight()));
		conv_list.addCommand(Contents.ok);
		
		conv_list.addCommand(Contents.back);
		conv_list.setCommandListener(this);
		if (currentjid.phone == null)
			currentjid.phone = "";
		conv_list.show();
	}
	
	public void commandActionRoster(Command id) {
		if (id == Contents.back) {
			currentjid = null;
			internal_state = ONLINE;
			getGuiOnlineMenu();
			
			
		} 
		else if (id == Contents.ok) {
            Button b;
            try {
                b = (Button)conv_list.getFocused();
            } catch (Exception e) {
                return;
            }

			String text = b.getText();
			if (text.equals(Contents.rosterChoices[0])) { //Change
				getGuiRosterDetails();
				internal_state = ROSTER_DETAILS;
			}
			else if (text.equals(Contents.rosterChoices[1])) { //delete
				Subscribe.removeRosterItem(currentjid);
				Datas.roster.remove(currentjid.getLittleJid());
				
				internal_state = ONLINE;
				getGuiOnlineMenu();
			}
			else if (text.equals(Contents.rosterChoices[2])) {//Phonecall
				if (currentjid.phone != null && !currentjid.phone.equals("")){
				     try {	
					if (platformRequest("tel:"+currentjid.phone)){
						cm.terminateStream();//manual close application
						System.exit(0);
					}
					Datas.jid.setPresence(Presence.getPresence("dnd"));
				     }catch(javax.microedition.io.ConnectionNotFoundException e){
				     	Dialog.show("", Contents.noPhone,null,Dialog.TYPE_ERROR,null,3000/*, getGuiRosterItem()*/);
					return;
					}
				}
				else { //set the number
					internal_state = ROSTER_DETAILS;
					Dialog.show("", Contents.noSavedPhone,null,Dialog.TYPE_WARNING,null,3000/*, getGuiRosterItem()*/);
					getGuiRosterDetails();
				}
			}
			else if (text.equals(Contents.online_choices[1]))
			{
				
					// try to subscribe to the item
					internal_state = ONLINE;
					Subscribe.requestSubscription(currentjid);
					Dialog.show("", Contents.done,null,Dialog.TYPE_CONFIRMATION,null,3000/*, getGuiRosterItem()*/);
					
					getGuiOnlineMenu();

			}
			else if (text.equals(Contents.online_choices[2]))
				{ //sms
					if (currentjid.phone != null && !currentjid.phone.equals(""))
					{
						SMSThread sms = new SMSThread(currentjid.phone, this);
						sms.setText("Hello " + currentjid.getUsername() + "! " + Datas.jid.getUsername() + " wants to chat with you..");
						sms.start();
					}
					else
					{ //set the number
						internal_state = ROSTER_DETAILS;
						Dialog.show("", Contents.noSavedPhone,null,Dialog.TYPE_WARNING,null,3000/*, getGuiRosterItem()*/);
						
						getGuiRosterDetails();
					}

			}
			
		}
	}
	
	/**
	 *
	 *Change a current roster info or create a new one
	 */
	public void getGuiRosterDetails() {
		String name = "New Contact";
		String current_Jid = "username@hostname";
		String group_Jid = "unfiled";
		String phone_num = "";
		if (currentjid != null) {
			name = currentjid.getUsername();
			current_Jid = currentjid.getFullJid();
			group_Jid = currentjid.group;
			phone_num = currentjid.phone;
		}
		Form res = new Form(name);
		res.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		TextArea jid = new TextArea(/*"jid", */current_Jid, 64);
		TextField group = new TextField(/*"group", */group_Jid, 32);
		TextField phone = new TextField(/*"phone num.",*/ phone_num, 32);
		infopool.put("jid", jid);
		infopool.put("group", group);
		infopool.put("phone", phone);
		res.addComponent(new MyLabel("jid:"));
		res.addComponent(jid);
		res.addComponent(new MyLabel("group:"));
		res.addComponent(group);
		res.addComponent(new MyLabel("phone:"));
		res.addComponent(phone);
		//res.append(new util.CustomSpacer(res.getWidth(), res.getHeight()));
		res.addCommand(Contents.ok);
		res.addCommand(Contents.back);
		res.setCommandListener(this);
		
		res.show();
	}
	
	public void commandActionRosterDetails(Command id) {
		
		internal_state = ONLINE;
		
		if (id == Contents.ok) {
			String jid = ((TextArea) infopool.remove("jid")).getText();
			String group = ((TextField) infopool.remove("group")).getText();
			String phone = ((TextField) infopool.remove("phone")).getText();
			boolean changes = false; // test if there are changes
			boolean onlyphone = false;
			boolean isNew = true;
			if (currentjid == null || !jid.equals(currentjid.getFullJid()) || !group.equals(currentjid.group) || !phone.equals(currentjid.phone)){
				if (currentjid != null && jid.equals(currentjid.getFullJid()) && group.equals(currentjid.group))
					onlyphone = true;//only phone number changes
				changes = true;	
			}
			if (currentjid != null) {
				Datas.roster.remove(currentjid.getLittleJid());
				isNew = false;
			}
			
			if (onlyphone) {

				currentjid.phone = phone;
				Subscribe.setPhoneNumber(currentjid, currentjid.phone);
				Datas.roster.put(currentjid.getLittleJid(), currentjid);
	
			}
			else if (changes) {
				Jid newjid = new Jid(jid);
				newjid.group = group;
				newjid.phone = phone;
				
				//notify to the server
				Subscribe.setNewRosterItem(newjid, isNew);
				Datas.registerRoster(newjid);
				
				if (newjid.phone != null && !newjid.phone.equals(""))
					Subscribe.setPhoneNumber(newjid, newjid.phone);
		
			} 
		} 
		
		getGuiOnlineMenu();
		
		
		
	}

	/**
	 * Show the gui for jud management
	 * @return Form
	 */
	public void getGuiJudMenu() {
		Form res = new Form("Search");
		if (infopool.containsKey("register")){
			res.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
			TextField tf = new TextField( "", 32);
			TextField tf2 = new TextField( "", 32);
			TextField tf3 = new TextField( "", 64);
			infopool.put("jud_name", tf);
			infopool.put("jud_surname", tf2);
			infopool.put("jud_mail", tf3);
			res.addComponent(new MyLabel("Name"));
			res.addComponent(tf);
			res.addComponent(new MyLabel("Surname"));
			res.addComponent(tf2);
			res.addComponent(new MyLabel("Mail"));
			res.addComponent(tf3);
		}
		else if (infopool.containsKey("search")) {
			res.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
			//TextField tf = new TextField("", 32);
			TextField tf2 = new TextField( "", 32);
			TextField tf3 = new TextField("", 32);
			TextField tf4 = new TextField( "", 32);
			TextField tf5 = new TextField("", 32);
			//infopool.put("jud_username", tf);
			infopool.put("jud_name", tf2);
			infopool.put("jud_surname", tf3);
			infopool.put("jud_nick", tf4);
			infopool.put("jud_mail", tf5);
            res.addComponent(new MyLabel("Nick"));
			res.addComponent(tf4);
			res.addComponent(new MyLabel("Name"));
			res.addComponent(tf2);
			res.addComponent(new MyLabel("Surname"));
			res.addComponent(tf3);
			//res.addComponent(new MyLabel("Username"));
			//res.addComponent(tf);
			res.addComponent(new MyLabel("Mail"));
			res.addComponent(tf5);
		}
		else {
			res.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
			Image[] img = new Image[]{Contents.displayImage("connected"), Contents.displayImage("unsubscribed")};
			ButtonActionListener action = new ButtonActionListener();
			for (int k=0; k<Contents.judChoices.length; k++) {
	        	Button b = new Button(Contents.judChoices[k], img[k]);
	            b.getStyle().setBgTransparency(100);
	            b.setTextPosition(Button.RIGHT);
	            b.addActionListener(action);
	            res.addComponent(b);
	        }
			
			if (infopool.containsKey("jud_message")) 
			{
				MyTextArea t = new MyTextArea((String)infopool.remove("jud_message"));
				
				res.addComponent(t);
			}
			
			infopool.put("jud_form", res);
		}
		
		res.addCommand(Contents.ok);
		res.addCommand(Contents.back);
		res.setCommandListener(this);
		res.show();
		
	}
	public void commandActionJud(Command id) {
		if (id == Contents.back) {
			if (infopool.remove("register") != null || infopool.remove("search") != null) {
				getGuiJudMenu(); //empty infopool??
			}
			else {
				internal_state = ONLINE;
				getGuiOnlineMenu();
			}
		}
		else if (id == Contents.ok) {
			if (infopool.remove("register") != null) { //registration
				TextField tf = (TextField) infopool.remove("jud_name");
				TextField tf2 = (TextField) infopool.remove("jud_surname");
				TextField tf3 = (TextField) infopool.remove("jud_mail");
				Datas.jid.setMail(tf3.getText());
				Jud.setRegistration(Datas.jid, tf.getText(), tf2.getText(), (String)infopool.get("jud_add"));
				infopool.put("jud_message", "Request executed");
				getGuiJudMenu();
			}
			else if (infopool.remove("search") != null) { //search
				
				Vector reg = new Vector(4,1);
				//reg.addElement(((TextField) infopool.remove("jud_username")).getText());
				reg.addElement(((TextField) infopool.remove("jud_name")).getText());
				reg.addElement(((TextField) infopool.remove("jud_surname")).getText());
				reg.addElement(((TextField) infopool.remove("jud_nick")).getText());
				reg.addElement(((TextField) infopool.remove("jud_mail")).getText());
				
				Jud.searchJid(reg, (String)infopool.get("jud_add"));
				infopool.put("jud_message", "Request executed");
				getGuiJudMenu();
			}
			else if (infopool.get("jud_form") != null){ //default menu
				Form res = (Form)infopool.remove("jud_form");
				Button b = (Button)res.getFocused(); //try catch?
				//int ind = jud_list.getSelectedIndex();
				if (b.getText().equals(Contents.judChoices[0]))
					infopool.put("register", "reg");
				else  
					infopool.put("search", "sea");
				getGuiJudMenu();
			}
		}
	}
	/**
	 * Show the gui for a chat
	 * @param tab (number of tab, -1 = get current tab, 0 = add new tab)
	 *
	 */
	public void getGuiConversation(int tab) {
	   
		try {
	    //System.out.println("getGuiConversation, tab:"+tab);
	    if (tab == -1) {
	    	//listener.keypressed = false;
	    	//tab = tabbedPane.getSelectedIndex();
	    	System.out.println("getGuiConversation, ERROR:tab="+tab);
	    	
	    }
		//boolean canAnswer = currentConversation.canAnswer();
		//mainForm.removeAllCommands();
        mainForm = new Form(Datas.jid.getUsername() + " (" + Datas.jid.getPresence()+")");
    	mainForm.setLayout(new BorderLayout());
    	mainForm.setScrollable(false);      
    	mainForm.getStyle().setBgTransparency(0);
        mainForm.addCommand(Contents.send, 0);
		mainForm.addCommand(Contents.delete, 1);
    	tabbedPane = new TabbedPane();
		
		tabbedPane.getStyle().setBgTransparency(0);
        
	    
		conversationForm = new Container();
		conversationForm.setScrollableY(true);
		conversationForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		//conversationForm.getStyle().setBgColor()
		Vector msgs = currentConversation.messages;
		Message msg;
		//if (currentConversation.isMulti || canAnswer) {
		TextArea tf = new TextArea(2,100,TextArea.ANY); //settare stile
		tf.setFocus(true);
	
		//TextBox tf = new TextBox("", ">", 128, TextArea.ANY);
		infopool.put("text2send", tf);
		conversationForm.addComponent(tf);
		
		int maxText = 11; //max texts displayed
		if (history || msgs.size() < maxText)
			maxText = msgs.size();//all texts
		
		history = false; //reset
        //int lin = 0; //links count 
		for (int i=msgs.size()-1; i>=msgs.size()-maxText; i--) {
			msg = (Message) msgs.elementAt(i);
			int j,p = -1;
	        //TODO: mettere sfondo di colore alternato
			String m;
			if (currentConversation.isMulti)
				m = msg.getTextNick(); //cambiare
			else
				m = msg.getText(); //cambiare 
			Container text = new Container(new BoxLayout(BoxLayout.X_AXIS));
			
			if (i%2 != 0) {
				text.getStyle().setBgTransparency(255);
				
			}
			text.getStyle().setBorder(Border.createEmpty()); //no border
			Label txt = new Label(m.substring(0, m.indexOf(">")+1));
			txt.getStyle().setFgColor(0xF1F57C);
			txt.getStyle().setBgTransparency(0);
			text.addComponent(txt);
			m = m.substring(m.indexOf(">")+1, m.length());
			if ((p = m.indexOf("1smile")) != -1) { //check smiles
				//conversationForm.addComponent(txt);
				if (p > 0) {
					text.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
					text.addComponent(new MyTextArea(m.substring(0, p),0,p));
				}
				Label l = new Label(Contents.displayImage(m.substring(p, p + 7)));
				l.getStyle().setBgTransparency(0);
				text.addComponent( l);
				if (p + 8 < m.length()-1)
					text.addComponent(new MyTextArea(m.substring(p + 8, m.length()),0,(m.length()-(p+8))));
			
			}
			if ((j = m.indexOf("+url+")) != -1) { //check links
				//conversationForm.addComponent(txt);
				//String name_link = "link";
        	/*	if (lin == 0) 
            			lin++;
        		else {
            			name_link += lin;
            			lin++;
        		}*/
				int k = m.indexOf("-url-");
				if (j > 0) {
					text.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
					text.addComponent(new MyTextArea(m.substring(0, j), 0,j)); 
				}
				j = j + 5;
				Button link = new Button(m.substring(j, k));
				link.getStyle().setBgTransparency(100);
				link.getStyle().setBorder(Border.createEmpty()); //no border
				link.addActionListener(new ButtonActionListener());
				text.addComponent(link); k = k + 5;
				if (k < m.length()-1)
					text.addComponent(new MyTextArea(m.substring(k, m.length()), 0, 100));
			

			}
			else if (j == -1 && p == -1) {
				
				MyTextArea t = new MyTextArea(m, 0,50);
				t.setEnabled(true);
				text.addComponent(t);
			}
			conversationForm.addComponent(text);
		}
		
		//if it is multi chat
		if (currentConversation.isMulti) {
			//conversationForm.append(new util.CustomSpacer(conversationForm.getWidth()));
		//	res.append("Room Members: ");
			
			
			Container multi = new Container();
			multi.setLayout(new BorderLayout());
			multi.addComponent(BorderLayout.NORTH,new MyLabel("Room Members:"));
			
			GroupChat chat = (GroupChat)Datas.multichat.get(currentConversation.name);
			//conversationForm.append(new util.CustomStringItem("Room Members:", /*chat.jids,*/ conversationForm.getWidth()));
			Container friends = new Container();	
			for (int j=0; j<chat.jids.size(); j++){
				
				String temp = (String)chat.jids.elementAt(j);
				if (temp.equals(currentConversation.name))
					continue;
				else if (temp.indexOf('@') != -1)
					friends.addComponent(new Label(temp.substring(0, temp.indexOf('@'))));
				else
					friends.addComponent(new Label(temp));
			}
			multi.addComponent(BorderLayout.CENTER, friends);
			//conversationForm.append(new util.CustomSpacer(conversationForm.getWidth()));
			TextField tf1 = new TextField("Invite a contact", 64);
			
			
			infopool.put("invite", tf1);

			multi.addComponent(BorderLayout.SOUTH, tf1);
			
			conversationForm.addComponent(multi);
		}
		
		Button his = new Button("Show History");
		his.getStyle().setBgTransparency(100);
		his.getStyle().setBorder(Border.createEmpty());
		his.addActionListener(new ButtonActionListener());
		conversationForm.addComponent(his);
		//his.addCommand(Contents.history);
		
		//TODO: se mettessimo un tastino send sotto la textarea?
		
		if (currentConversation.isMulti)
			mainForm.addCommand(Contents.invite,2);
        
		tabbedPane.addTab("Contacts", new Container());
		if (tab == 0) { //nuova tab da aggiungere
            if (Datas.conversations.size() > 0)
            {

                Vector chats = Datas.conversations;

                for (int k = 0; k < chats.size()-1; k++)
                {
                    Conversation c = (Conversation)chats.elementAt(k);
                    String name = c.name;
                    if (name.indexOf("@") != -1) {
                        name = name.substring(0, name.indexOf("@"));
                    }
                    tabbedPane.addTab(name, new Container());
                }

            }
			//System.out.println("add tab:"+tab);
			
		    listener.keypressed = false;
            String name = currentConversation.name;

            if (name.indexOf("@") != -1) {
                        name = name.substring(0, name.indexOf("@"));
            }
			tabbedPane.addTab(name, conversationForm);
			tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
			listener.keypressed = true;
		//	System.out.println("dopo add");
		}else {
			//System.out.println("insert tab:"+tab);
            
            if (Datas.conversations.size() > 0)
            {

                Vector chats = Datas.conversations;

                for (int k = 0; k < chats.size(); k++)
                {
                    Conversation c = (Conversation)chats.elementAt(k);
                    String name = c.name;
                    if (!name.equals(currentConversation.name)) {
                        if (name.indexOf("@") != -1) {
                            name = name.substring(0, name.indexOf("@"));
                        }
                        tabbedPane.addTab(name, new Container());
                    }else {
                        if (name.indexOf("@") != -1) {
                            name = name.substring(0, name.indexOf("@"));
                        }
                        tabbedPane.addTab(name, conversationForm);
                    }
                }

            }
			
			//tabbedPane.removeTabAt(tab);//??
			//tabbedPane.insertTab(currentConversation.name, null, conversationForm, tab); //va levata quella vecchia?
			listener.keypressed = false;
			tabbedPane.setSelectedIndex(tab);
			listener.keypressed = true;
		
		}
        tabbedPane.addTabsListener(listener);
        mainForm.addComponent(BorderLayout.CENTER, tabbedPane);
      
        mainForm.setCommandListener(this);
		mainForm.show(); //Sicuro??
		//System.out.println("dopo show");
	   }catch(OutOfMemoryError e) {
		
		Datas.multichat.clear();
		Datas.conversations.removeAllElements();
		Datas.conversations.trimToSize();
		Datas.server_services.removeAllElements();
		Datas.conversations.trimToSize();
		cm.terminateStream();
		internal_state = OFFLINE;
		getGuiOfflineMenu();
		}
		
	}

	/**
	 * Update the gui for a chat
	 * @param tab (number of tab, -1 = delete textarea, 0 = maintain textarea)
	 * 
	 */
	public void getGuiUpdateConversation(int tab)
	{
		TextArea tempItem = (TextArea)conversationForm.getComponentAt(0);
		if (tab == -1)
			tempItem.setText("");
		tempItem.setFocus(true);
		conversationForm.removeAll();
		//Container alternateForm = new Container();
		//alternateForm.setScrollableY(true);
		//alternateForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		
		
		conversationForm.addComponent(tempItem);
		//conversationForm.setScrollable(true);
		infopool.put("text2send", tempItem);
	/*	if (tab == -1)
	    	tab = tabbedPane.getSelectedIndex();
      */  	
    	Vector msgs = currentConversation.messages;
		Message msg;
		//if (currentConversation.isMulti || canAnswer) {
	/*	TextArea tf = new TextArea(2,100,TextArea.ANY); //settare stile
		tf.setFocus(true);
		infopool.put("text2send", tf);
		conversationForm.addComponent(tf);
		*/	
    		//}
    		/*if (currentConversation.avatar != null){
    			conversationForm.append(new ImageItem("", currentConversation.avatar, Item.LAYOUT_CENTER, ""));
    			 }*/
    		
    		int maxText = 11; //max texts displayed
    		if (history || msgs.size() < maxText)
    			maxText = msgs.size();//all texts
    		
    		history = false; //reset
           // int lin = 0; //links count
          //TODO PROVA di COMPOSING
        	if (!currentConversation.isMulti)
        		conversationForm.addComponent(new Label(currentConversation.composing));
    		for (int i=msgs.size()-1; i>=msgs.size()-maxText; i--) {
    			msg = (Message) msgs.elementAt(i);
    			int j,p = -1;
    	        //TODO: mettere sfondo di colore alternato
    			String m;
    			if (currentConversation.isMulti)
    				m = msg.getTextNick(); //cambiare
    			else
    				m = msg.getText(); //cambiare 
    			Container text = new Container(new BoxLayout(BoxLayout.X_AXIS));
    			
			if (i%2 != 0) {
				text.getStyle().setBgTransparency(255);
				
			}
			text.getStyle().setBorder(Border.createEmpty());
    			Label txt = new Label(m.substring(0, m.indexOf(">")+1));
    			txt.getStyle().setFgColor(0xF1F57C);
    			txt.getStyle().setBgTransparency(0);
    			text.addComponent(txt);
    			m = m.substring(m.indexOf(">")+1, m.length());
    			if ((p = m.indexOf("1smile")) != -1) { //check smiles
    			//	conversationForm.addComponent(txt);
    				if (p > 0) {
    					text.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    					text.addComponent(new MyTextArea(m.substring(0, p),0,p));
    				}
    				Label l = new Label(Contents.displayImage(m.substring(p, p + 7)));
    				l.getStyle().setBgTransparency(0);
    				text.addComponent( l);
    				if (p + 8 < m.length()-1)
    					text.addComponent(new MyTextArea(m.substring(p + 8, m.length()),0,(m.length()-(p+8))));
    			
    			}
    			if ((j = m.indexOf("+url+")) != -1) { //check links
    				//conversationForm.addComponent(txt);
    				//String name_link = "link";
            	/*	if (lin == 0) 
                			lin++;
            		else {
                			name_link += lin;
                			lin++;
            		}*/
    				text.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    				int k = m.indexOf("-url-");
    				if (j > 0) {
    					text.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    					text.addComponent(new MyTextArea(m.substring(0, j), 0,j)); 
    				}
    				j = j + 5;
    				Button link = new Button(m.substring(j, k));
    				link.getStyle().setBgTransparency(100);
    				link.getStyle().setBorder(Border.createEmpty()); //no border
    				link.addActionListener(new ButtonActionListener());
    				text.addComponent(link); k = k + 5;
    				if (k < m.length()-1)
    					text.addComponent(new MyTextArea(m.substring(k, m.length()), 0, 100));
    			

    			}
    			else if (j == -1 && p == -1) {
    				//txt.setText(m);
    				MyTextArea t = new MyTextArea(m, 0,50);
    				t.setEnabled(true);
    				text.addComponent(t);
    			}
    			conversationForm.addComponent(text);
    		}
    		
    		//if it is multi chat
    		if (currentConversation.isMulti) {
    			//conversationForm.append(new util.CustomSpacer(conversationForm.getWidth()));
    		//	res.append("Room Members: ");
    			
    			
    			Container multi = new Container();
    			multi.setLayout(new BorderLayout());
    			multi.addComponent(BorderLayout.NORTH,new MyLabel("Room Members:"));
    			
    			GroupChat chat = (GroupChat)Datas.multichat.get(currentConversation.name);
    			//conversationForm.append(new util.CustomStringItem("Room Members:", /*chat.jids,*/ conversationForm.getWidth()));
    			Container friends = new Container();	
    			for (int j=0; j<chat.jids.size(); j++){
    				
    				String temp = (String)chat.jids.elementAt(j);
    				if (temp.equals(currentConversation.name))
    					continue;
    				else if (temp.indexOf('@') != -1)
    					friends.addComponent(new Label(temp.substring(0, temp.indexOf('@'))));
    				else
    					friends.addComponent(new Label(temp));
    			}
    			multi.addComponent(BorderLayout.CENTER, friends);
    			//conversationForm.append(new util.CustomSpacer(conversationForm.getWidth()));
    			TextField tf1 = new TextField("Invite a contact", 64);
    			//mainForm.addCommand(Contents.invite);
    			
    			infopool.put("invite", tf1);

    			multi.addComponent(BorderLayout.SOUTH, tf1);
    			
    			conversationForm.addComponent(multi);
    		}
    		
    		Button his = new Button("Show History");
    		his.getStyle().setBgTransparency(100);
    		his.getStyle().setBorder(Border.createEmpty());
    		his.addActionListener(new ButtonActionListener());
    		conversationForm.addComponent(his);
    		//his.addCommand(Contents.history);
    		
    		//TODO: se mettessimo un tastino send sotto la textarea?
    		mainForm.addCommand(Contents.send, 0);
    		mainForm.addCommand(Contents.delete, 1);
    		//mainForm.addCommand(Contents.back, 2);
    		if (currentConversation.isMulti)
    			mainForm.addCommand(Contents.invite,3);
    		//mainForm.replace(conversationForm, alternateForm, null);
    		//conversationForm.removeAll();
    		//conversationForm = alternateForm;
    		mainForm.show();
    		//mainForm.setCommandListener(this);
    
    	}


	public void commandActionConversation(Command id)
	{
	  try { 
		if (id == Contents.back) {
			System.gc();//garbage!
			if (currentConversation.isMulti){
				internal_state = MULTI_CHAT;
				getGuiRoomList();
			}
			else {
				getGuiOnlineMenu();
				internal_state = ONLINE;
			}
		} else if (id == Contents.send) {
			TextArea tf = (TextArea) infopool.remove("text2send");
			if (!tf.getText().equals("")) {
				Message msg = new Message("", tf.getText());
				((Chat) currentConversation).appendFromMe(msg);
			}
			getGuiUpdateConversation(-1);
		}
		else if (id == Contents.invite ) {
			TextField tf = (TextField) infopool.remove("invite");
				
			String inv = tf.getText();
			if (inv.indexOf("@") != -1)
				ChatHelper.inviteContact(inv, currentConversation.name);
			else {
				//display.setCurrent(new Alert("Invitation error", "JID not correct", null, AlertType.ERROR), getGuiConversation());
				Dialog.show("Error", "Invitation error", null, Dialog.TYPE_ERROR,null, 3000);
				return;
			}
			//TODO: devo aggiornare?
			getGuiUpdateConversation(-1);
		}
		else if (id == Contents.history) {
			history = true;
			
			getGuiUpdateConversation(-1);
		}
		else if (id.getCommandName().indexOf("http") != -1)
		{   
			System.out.print("activate link:"+id.getCommandName());
			MediaManager.activateLink(id.getCommandName(), this);
		}
		else if (id == Contents.delete) {
			listener.keypressed = false;
			int ind = tabbedPane.getSelectedIndex();
			tabbedPane.removeTabAt(ind);
			listener.keypressed = true;
			Datas.conversations.removeElementAt(ind-1);
			internal_state = ONLINE;
			getGuiOnlineMenu();
			
		}
	  }catch(OutOfMemoryError e) {
		
		Datas.multichat.clear();
		Datas.conversations.removeAllElements();
		Datas.conversations.trimToSize();
		Datas.server_services.removeAllElements();
		Datas.conversations.trimToSize();
		cm.terminateStream();
		internal_state = OFFLINE;
		getGuiOfflineMenu();
	  }

	}
	
	/**
	 * Show the form to insert user jid information
	 * @return
	 */
	public void getGuiParams() {
		Form res = new Form(Contents.settings_form);
		res.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		
		/*String subdomain = "";
		if (Datas.subdomain != null)
			subdomain = "@"+Datas.subdomain;
			*/
		TextArea jid = new TextArea(Datas.jid.getUsername(), 32);
		TextArea server = new TextArea(Datas.server_name, 64);
		TextArea subdomain = new TextArea(Datas.subdomain, 64);
		
		TextArea password = new TextArea(Datas.getPassword(), 1,32, TextArea.PASSWORD);
		TextArea mail = new TextArea(Datas.jid.getMail(), 1,32, TextArea.EMAILADDR);
		TextArea port = new TextArea(Datas.customPort, 1,5, TextArea.NUMERIC);
		infopool.put("password", password);
		
		
		infopool.put("jid", jid);
		infopool.put("mail", mail);
		infopool.put("server", server);
		infopool.put("subdomain", subdomain);
		infopool.put("port", port);
		res.addComponent(new MyLabel("Username *"));
		res.addComponent(jid);
		res.addComponent(new MyLabel("Password *"));
		res.addComponent(password);
		res.addComponent(new MyLabel("Mail"));
		res.addComponent(mail);
		res.addComponent(new MyLabel("Jabber domain"));
		res.addComponent(subdomain);
		res.addComponent(new MyLabel("Server address *"));
		res.addComponent(server);
		res.addComponent(new MyLabel("Port"));
		res.addComponent(port);
		res.addComponent(new MyLabel("Connection type"));
		ssl_list = new ButtonGroup();
		for (int k = 0; k < Contents.sslChoices.length; k++) {
			RadioButton rb = new RadioButton(Contents.sslChoices[k]);
			Style s = rb.getStyle();
	        s.setMargin(0, 0, 0, 0);
	        s.setBgTransparency(0);
	        ssl_list.add(rb);
	        res.addComponent(rb);
		}
		
		if (Datas.isSSL)
			ssl_list.setSelected(1);
		else if (Datas.isHTTP)
			ssl_list.setSelected(2);
		else 
			ssl_list.setSelected(0);
		
		res.addComponent(new MyLabel("Your Avatar"));
		avatar_list = new ButtonGroup(); //choose AVATAR
		String[] img = new String [] {"icon", "jmcAvatar"}; //spostare in contents
		for (int k = 0; k < 2; k++) {
			RadioButton rb = new RadioButton("avatar "+(k+1), Contents.displayImage(img[k]).scaled(16, 16));
			Style s = rb.getStyle();
	        s.setMargin(0, 0, 0, 0);
	        s.setBgTransparency(0);
	        avatar_list.add(rb);
	        res.addComponent(rb);
		}
		
		if (Datas.avatarFile != null && Datas.avatarFile.indexOf("icon") == -1)
			avatar_list.setSelected(1);
		else if (Datas.avatarFile != null)
			avatar_list.setSelected(0);
		
		
		Label m = new Label("* Mandatory fields");
		m.getStyle().setBgTransparency(0);
		m.getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL));
		m.setEnabled(true);
		m.setFocusable(true);
		res.addComponent(m);
		res.addCommand(Contents.back);
		res.addCommand(Contents.ok);
		res.setCommandListener(this);
		
		res.show();
	}
	
	
	public void commandActionParams(Command id) {
		internal_state = OFFLINE;
		String alert;
		if (id == Contents.ok) {
			String jid = ((TextArea)infopool.remove("jid")).getText();
			String password = ((TextArea)infopool.remove("password")).getText();
			String mail = ((TextArea)infopool.remove("mail")).getText();
			String server = ((TextArea)infopool.remove("server")).getText();
			String subdomain = ((TextArea)infopool.remove("subdomain")).getText();
			String customPort = ((TextArea)infopool.remove("port")).getText();
			/*int ssl = 0;
			if (Datas.isSSL)
				ssl = 1;
			if (Datas.isHTTP)
				ssl = 2;*/	
			//if (!server.equals(Datas.server_name) || !password.equals(Datas.getPassword()) || !jid.equals(Datas.jid.getUsername()) || !mail.equals(Datas.jid.getMail()) || ssl != ssl_list.getSelectedIndex()) {
			if (jid.equals("") || server.equals(""))
			{
				alert = Contents.jid_sintax_error;
			}
			else {
				if (subdomain != null && !subdomain.equals(""))
				{//subdomain exists
					Datas.subdomain = subdomain;//jid.substring(jid.indexOf('@') + 1, jid.length());
					//jid = jid.substring(0, jid.indexOf('@'));
					Datas.hostname = Datas.subdomain;
					
				}
				else
				{
					Datas.hostname = server;
					Datas.subdomain = null;
				}
				Datas.jid = new Jid(jid + "@" + Datas.hostname);
				if (Datas.jid.getResource() == null)
					Datas.jid.setResource("JabberMix");
				Datas.setPassword(password);
				Datas.server_name = server;
				
				Datas.jid.setMail(mail);
				if (ssl_list.getSelectedIndex() == 1) {
					Datas.isSSL = true;
					Datas.isHTTP = false;
				}
				else if (ssl_list.getSelectedIndex() == 2){
					Datas.isSSL = false;
					Datas.isHTTP = true;
				}
				else {Datas.isSSL = false;Datas.isHTTP = false;}
				alert = Contents.saved;
				
				if (customPort != null && !customPort.equals("")) {
					Datas.customPort = customPort;
					
					Datas.port = Integer.parseInt(customPort);
				}
				//System.out.println("AVATAR:"+avatar_list.getSelectedIndex());
				if (avatar_list.getSelectedIndex() == 0) { //AVATAR
					Datas.avatarFile = Contents.getImage("icon");
					
				}
				else if (avatar_list.getSelectedIndex() == 1){
					Datas.avatarFile = Contents.getImage("jmcAvatar");
				}
				Datas.setJidAvatar(); 
				
				Datas.saveRecord();
				
				
				//empty roster, future retrieve offline
				Datas.roster.clear();
			}
		
			Dialog.show("", alert, null, Dialog.TYPE_WARNING,null, 3000);
			getGuiOfflineMenu();
			
		} else if (id == Contents.back) {
			getGuiOfflineMenu();
			
		}
	}
	
	/**
	 * Show the intro logo and author info 
	 * @return Alert
	 */
	 public void getGuiIntroScreen() {
		Image logo;
		try {
		 logo = Image.createImage("/jmc_back.png");
		}catch(java.io.IOException e){
		 logo = null;
		}	 
	 	//Alert intro = new Alert("Jabber Mix Client", " Created by Gabriele Bianchi", logo, AlertType.INFO);
	 
		Container body = new Container(new BorderLayout());
		//body.getStyle().setBgColor(0xFFFFFF,true);
		Label l =  new Label(logo);
		l.setAlignment(Label.CENTER);
		body.addComponent(BorderLayout.NORTH,l);
		MyTextArea t = new MyTextArea("Created by Gabriele Bianchi",0,100);
		//t.getStyle().setFgColor(0x0000FF,true);
		body.addComponent(BorderLayout.CENTER, t);
		Dialog.show("Jabber Mix Client", body, null, Dialog.TYPE_INFO, null, 3000);
	 }
	
	/**
	 * Wait for connecting
	 * @return
	 */
	public void getGuiWaitConnect() {
		Form wait_form = new Form(Contents.wait_form);
		wait_form.setLayout(new BorderLayout());
		//if (wait_form == null) {
		
		
			wait_form.addComponent(BorderLayout.CENTER,new MyLabel("Please wait...."));
		/*	Progress p2;
			try {
				p2 = new Progress(Image.createImage("/unfilled.png"), Image.createImage("/filled.png"));
				p2.getStyle().setBgTransparency(0);
				wait_form.addComponent(BorderLayout.CENTER, p2);
			} catch (IOException e) {
				
				
			}*/
			wait_form.addCommand(Contents.back);
			wait_form.setCommandListener(this);
			//TODO: mettere gauge
		//}
		wait_form.show();
	}
	
	public void commandActionWaitConnect(Command id) {
		if (id == Contents.back) {
			cm.disconnect(); 
			getGuiOfflineMenu();
			internal_state = OFFLINE;
		}
	}
	
	/**
	 * Show the form to accept or deny a subscription request from another contact or a multichat invitation
	 * @return Form
	 */
	public void getGuiChoose(String type) { 
		
		String text = "";
		Form subscriber = null;
		if (type.equalsIgnoreCase("subscription")) {
			subscriber = new Form(Contents.subsc_form);
			subscriber.setLayout(new BorderLayout());
			text = currentjid.getUsername()+ " wants to subscribe your presence!";
		}
		else if (type.equalsIgnoreCase("invitation")) {
			subscriber = new Form(Contents.invit_form);
			text = "You have been invited to " + (String)infopool.get("invit_room") + " from "+(String)infopool.get("invit_from");
		}
		subscriber.setLayout(new BorderLayout());
		MyTextArea t = new MyTextArea(text, 0, 100);
		
		subscriber.addComponent(BorderLayout.CENTER,t);
		
		subscriber.addCommand(Contents.accept);
		subscriber.addCommand(Contents.deny);
		subscriber.setCommandListener(this);
		subscriber.show();
	}
	
	public void commandActionSubscription(Command id) {
		if (id == Contents.accept)
		{
			Subscribe.acceptSubscription(currentjid);   
			//TODO: check the result of this request
			currentjid.setPresence("subscribed"); //??
			
		}
		else {
			Subscribe.denySubscription(currentjid);
		}
		internal_state = ((Integer)infopool.remove("internal_state")).intValue();
		
		this.setCurrentDisplay();
		
	}
	public void commandActionInvite(Command id) {

		if (id == Contents.accept) {
			Jid room = new Jid((String)infopool.remove("invit_room"));
			infopool.remove("invit_from");
			ChatHelper.groupChatJoin(Datas.jid.getUsername(), room.getUsername(), room.getServername());
			
		}
		else {
			internal_state = ((Integer)infopool.remove("invit_internal_state")).intValue();
			infopool.remove("invit_from");
			infopool.remove("invit_room");
			setCurrentDisplay();
		}
	}
	/**
	 *Display existing conversations and the form to join in a new chat room
	 * @return
	 */
	public void getGuiRoomList() {
		Form rooms = new Form("Chat Rooms");
        rooms.setLayout(new BorderLayout());
       // rooms.setScrollable(false);
        Container cont = new Container();
        cont.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		//show existing chats
		//openrooms = new ChoiceGroup("Your Active Chats", ChoiceGroup.EXCLUSIVE);
		//if (infopool.remove("newchat") != null) {
        TextField nick = new TextField(Datas.jid.getUsername(), 32);
        TextField room = new TextField(/*"Chat Room", */"", 32);
        TextField server = new TextField(/*"Chat Server",*/ "conference."+Datas.hostname, 32);
		cont.addComponent(new MyLabel("Your Nick"));
		cont.addComponent(nick);
		cont.addComponent(new MyLabel("Chat Room"));
		cont.addComponent(room);
		cont.addComponent(new MyLabel("Chat Server"));
		cont.addComponent(server);
		cont.addComponent(new MyLabel("Discover Rooms?"));
		yesno = new ButtonGroup(/*"Discover Rooms?", ChoiceGroup.EXCLUSIVE*/);
		RadioButton rb = new RadioButton("No");
		Style s = rb.getStyle();
        s.setMargin(0, 0, 0, 0);
        s.setBgTransparency(70);
        RadioButton rb1 = new RadioButton("Yes");
		Style s1 = rb1.getStyle();
        s1.setMargin(0, 0, 0, 0);
        s1.setBgTransparency(70);
        cont.addComponent(rb);
        cont.addComponent(rb1);
        yesno.add(rb);
        yesno.add(rb1);
        yesno.setSelected(0);
		
		infopool.put("nick", nick);
		infopool.put("room", room);
		infopool.put("chatserver", server);
	//	}
/*
		else { //TODO: unificare nella gestione delle chat aperte quindi in onlinemnu
			Enumeration chats = Datas.multichat.keys();
			while (chats.hasMoreElements()){
				String temp = (String)chats.nextElement();
			
				openrooms.append(temp, null); 
			}
			openrooms.append("Create/Join new chat", null);
			
			rooms.append(openrooms);
			if (Datas.rooms != null){ //rooms discovery
				//spacer?
				rooms.append(new util.CustomSpacer(rooms.getWidth()));

				rooms.append(new util.CustomStringItem("Existing rooms:", Datas.rooms, rooms.getWidth()));
				//rooms.append("Existing rooms:");
				//for(int k=0;k<Datas.rooms.size();k++)
				//	rooms.append((String)Datas.rooms.elementAt(k));
			}
			else
				rooms.append(new util.CustomSpacer(rooms.getWidth(), rooms.getHeight()));
		}
		
	*/	rooms.addComponent(BorderLayout.CENTER, cont);
		rooms.addCommand(Contents.back);
		rooms.addCommand(Contents.ok);
		rooms.setCommandListener(this);
		rooms.show();
	}

	public void commandActionRooms(Command id) {
		if (id == null) {
			internal_state = ONLINE;
			getGuiOnlineMenu();
		}
		if (id == Contents.back)
		{
			internal_state = ONLINE;
			getGuiOnlineMenu();
		}
		else if (id == Contents.ok)
		{
			String nick = ((TextField)infopool.remove("nick")).getText();
			String room = ((TextField)infopool.remove("room")).getText();
			String server = ((TextField)infopool.remove("chatserver")).getText();
			//join an existing chat
		/*	if (openrooms.getSelectedIndex() != -1 && openrooms.getSelectedIndex() < openrooms.size()-1) { //existing chat chosen
				String cname = openrooms.getString(openrooms.getSelectedIndex());
				currentConversation = (Conversation)Datas.multichat.get(cname);
				currentConversation.isMulti = true;
				internal_state = CONVERSATION;
				display.setCurrent(getGuiConversation());
			}//display form
			else if (openrooms.getSelectedIndex() != -1 && openrooms.getSelectedIndex() == openrooms.size()-1) {
				infopool.put("newchat", "newchat");
				display.setCurrent(getGuiRoomList());
			}
			*/if (!nick.equals("") && !server.equals("") && !room.equals("")) { //submit form
				
				ChatHelper.groupChatJoin(nick, room, server);
				if (yesno.getSelectedIndex() == 1)
					ChatHelper.serviceRequest(server);
				getGuiRoomList(); //????????????????
			}
			else {
				internal_state = MULTI_CHAT;
				Dialog.show("Error", Contents.emptyParams, null, Dialog.TYPE_ERROR, null, 3000);
				//display.setCurrent(new Alert("Error", "Empty parameters", null, AlertType.ERROR),getGuiRoomList());
			}
		}
	}
	


	/**
	 * General method to dispatch the right specific commandaAction method in base of internal_state
	 * @param Command
	 * @param Displayable
	 */
	public void actionPerformed(ActionEvent evt) {
		
		Command _c = evt.getCommand();
		//String id = _c.getLabel().toLowerCase();
		//commands.clear();
		
		switch (internal_state) {
		case ONLINE: commandActionOnlineMenu(_c); break;
		case OFFLINE: commandActionOfflineMenu(_c); break;
		case CONVERSATION: commandActionConversation(_c); break;
		case ROSTER: commandActionRoster(_c); break;
		case STATUS: commandActionChangeStatus(_c); break;
		case SUBSCRIPTION: commandActionSubscription(_c); break;
		case PARAMS: commandActionParams(_c); break;
		case WAIT_CONNECT: commandActionWaitConnect(_c); break;
		case ROSTER_DETAILS: commandActionRosterDetails(_c); break;
		case MULTI_CHAT: commandActionRooms(_c); break;
		case INVITATION: commandActionInvite(_c); break;
		case OPTIONS: commandActionOtherOptions(_c); break;
		case JUD: commandActionJud(_c); break;
		}
		
		
		
	}
	/**
	 * @param Command
	 * @param Item
	
	public void actionPerformed(Command _c, Item _i) {
		
		switch (internal_state) {
		case CONVERSATION : commandActionConversation(_c); break;
		case ONLINE: 
			
			if (_c == Contents.active)//command "Open chat" 
			{
				String label = _i.getLabel();
				int ind = Integer.parseInt(label) - 1;
				currentConversation = (Conversation)Datas.conversations.elementAt(ind);
				display.setCurrent(getGuiConversation());
				internal_state = CONVERSATION;
				break;
			}
			else if (_c == Contents.delete)//command "Delete chat" 
			{
				String label = _i.getLabel();
				int ind = Integer.parseInt(label) - 1;
				Datas.conversations.removeElementAt(ind);
				internal_state = ONLINE;
				display.setCurrent(getGuiOnlineMenu());
				break;
			}	//NUOVO
			else if (_c == Contents.select)	{//hide choice
				String hide = (String)infopool.get("hide");
				if (hide.equals(Contents.hide[1]))
					infopool.put("hide", Contents.hide[0]);
				else
					infopool.put("hide", Contents.hide[1]);
				display.setCurrent(getGuiOnlineMenu());
			}
			else if (_c == Contents.info){//NUOVO
				int index = contacts_list.getSelectedIndex();
				
				currentjid = (Jid)roster.elementAt(index);

				if (currentjid.phone == null)
					Subscribe.getPhoneNumber(currentjid);//get phone number..
				display.setCurrent(getGuiRosterItem());
				internal_state = ROSTER;
			}
			else if (_c == Contents.chat) {
			 	int index = contacts_list.getSelectedIndex();
				
				currentjid = (Jid)roster.elementAt(index);
			  	if (Presence.getPresence("unsubscribed").equals(currentjid.getPresence()))
				{
					// try to subscribe to the item
					internal_state = ONLINE;
					Subscribe.requestSubscription(currentjid);

					display.setCurrent(Contents.subs, getGuiOnlineMenu());

				}
				else {
					boolean found = false;
					Conversation c1 = null;
					Vector conversations = Datas.conversations;
					//look for an exsisting Conversation
					for (int i=0; i< conversations.size(); i++) {
						c1 = (Conversation)conversations.elementAt(i);
						if (c1.name.equals(currentjid.getUsername())) {
							found = true;
							break;
						}
					}
					if (found) {
						currentConversation = c1;
					} else{
					// sets up a new conversation 
						currentConversation = new SingleChat(currentjid, "chat", "");					
						conversations.addElement(currentConversation);
				  	}
					display.setCurrent(getGuiConversation());
					internal_state = CONVERSATION;
				}
		
			}
		}
	}
	
 	*/
	/**
	 *Utility method to set the display
	 *
	 */
	public void setCurrentDisplay() {
		if (internal_state == ONLINE)
			getGuiOnlineMenu();
		else if (internal_state == ROSTER || internal_state == ROSTER_DETAILS) {	
			if (infopool.containsKey("currentjid"))
				currentjid = (Jid)infopool.remove("currentjid");
			if (internal_state == ROSTER_DETAILS)
				getGuiRosterDetails();//possible problems..
			else
				getGuiRosterItem();
		}
		else if (internal_state == OPTIONS)
			getGuiOtherOptions();
		else if (internal_state == JUD)
			getGuiJudMenu();
		else
			getGuiOnlineMenu();
	}
	/**
	*  Determine if activated due to inbound connection and
	*  if so dispatch a PushProcessor to handle incoming
	*  connection(s). 
	  @return true if MIDlet was activated
	*  due to inbound connection, false otherwise
	*/
	private boolean handlePushActivation()
	{
		//  Discover if there are pending push inbound
		//  connections and if so, dispatch a
		//  PushProcessor for each one.
		String[] connections = PushRegistry.listConnections(true);
		if (connections != null && connections.length > 0)
		{
			
			PushThread pp = new PushThread(connections[0], this);
			pp.start();
			
			return (true);
		}
		return (false);
	}
	
	private class ButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
        	Button b = (Button)evt.getSource();
        	switch (internal_state) {
        		case OFFLINE:
        			commandActionOfflineMenu(Contents.ok);
        			break;
        		case ONLINE:
        			//commandActionOnlineMenu(Contents.ok);
        			String hide = (String)infopool.get("hide");
    				if (hide.equals(Contents.hide[1]))
    					infopool.put("hide", Contents.hide[0]);
    				else
    					infopool.put("hide", Contents.hide[1]);
    				getGuiOnlineMenu();
        			break;
        		case ROSTER:
        			commandActionRoster(Contents.ok);
        			break;
        		case OPTIONS:
        			commandActionOtherOptions(Contents.ok);
        			break;
        		case STATUS:
        			commandActionChangeStatus(Contents.ok);
        			break;	
        		case JUD:
        			commandActionJud(Contents.ok);
        			break;
        		case CONVERSATION:
        			
        			if (b.getText().indexOf("History") != -1)
        				commandActionConversation(Contents.history);
        			else 
        				commandActionConversation(new Command(b.getText())); //link address
        			break;
        		
        		
        			
        	}
             //currentDemo = ((Demo)(demosHash.get(evt.getSource()) ));
             //currentDemo.run(backCommand, UIDemoMIDlet.this);
        }
     }
	
	private class ContactList extends com.sun.lwuit.List implements ActionListener {
		//private GuiMidlet midlet;
		/*public  ContactList(GuiMidlet _midlet) {
			midlet = _midlet;
		}*/
		/*protected boolean  isSelectableInteraction() {
			return super.isSelectableInteraction();
		}
		protected  void fireClicked() {
			*/
		public void actionPerformed(ActionEvent evt) {
			//start conversation
			//System.out.println("ContactList - fireclicked");
			currentjid = (Jid)this.getSelectedItem();
			//System.out.println("selected user:"+currentjid.getUsername());
			  	if (Presence.getPresence("unsubscribed").equals(currentjid.getPresence()))
				{
					// try to subscribe to the item
					internal_state = ONLINE;
					Subscribe.requestSubscription(currentjid);

					Dialog.show("",Contents.subs,null,Dialog.TYPE_CONFIRMATION, null,3000); 
					getGuiOnlineMenu();

				}
				else {
					//boolean found = false;
					Conversation c1 = null;
					Vector conversations = Datas.conversations;
					//look for an exsisting Conversation
					for (int i=0; i< conversations.size(); i++) {
						c1 = (Conversation)conversations.elementAt(i);
						if (c1.name.equals(currentjid.getUsername())) {
							
							currentConversation = c1;
							//tabbedPane.setSelectedIndex(i+1);
							internal_state = CONVERSATION;
							getGuiConversation(i+1);
							return;
						}
					}
					
					// sets up a new conversation 
					currentConversation = new SingleChat(currentjid, "chat", "");					
					conversations.addElement(currentConversation);
				  	//tabbedPane.addTab(currentConversation.name, new Container()); //pensare se fare sta cosa dentro getGuiConversation
					getGuiConversation(0); 
					internal_state = CONVERSATION;
				}
			return;
		}

	}

	
}
