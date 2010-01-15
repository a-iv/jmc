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


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import java.util.Vector;
import java.util.Hashtable;

import java.util.Enumeration;
/*
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
*/

/**
 *  
 *
 * JMC GUI Midlet
 * @author Gabriele Bianchi
 */
public class GuiMidlet
extends MIDlet /*implements ActionListener*/
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
//	ContactList contacts_list = null;
//	ButtonGroup status_list = null;
	Form conv_list = null;
	//ChoiceGroup static_menu = null;
	//ChoiceGroup openrooms = null;
//	ButtonGroup yesno = null;
	//ChoiceGroup options_list = null;
	//ChoiceGroup jud_list = null;
//	ButtonGroup ssl_list = null;
//	ButtonGroup avatar_list = null;

	Form mainForm = null;
//	Container conversationForm = null;
//	public TabbedPane tabbedPane = null;
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
		 if (display == null) { //thanks to Azlan
     		display = Display.getDisplay(this);
     		//display.setCurrent(getGuiIntroScreen(), getGuiOfflineMenu());
				Datas.load();
				//listener.display = display;
				//handlePushActivation();
				
     }
		 else {
			if (internal_state != OFFLINE && internal_state != WAIT_CONNECT && Datas.jid != null)
				Datas.jid.setPresence(Presence.getPresence("online")); //change user status
		 }

		/*		
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
        */
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
/*
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
		*/
		/*
		offLineMenu.addCommand(Contents.ok);
		offLineMenu.addCommand(Contents.exit);
		
		offLineMenu.setCommandListener(this);
		offLineMenu.show();
		//return res;
		 */
	}
	
	public void commandActionOfflineMenu(Command id) {
		/*
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
			/*
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
		*/
	}
	
	/**
	 * Show the main menu (online)
	 * @return
	 */
	public void getGuiOnlineMenu() {
        internal_state = ONLINE;
    	mainForm = new Form(Datas.jid.getUsername() + " (" + Datas.jid.getPresence()+")");
    	/*
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
        /*ContactList*/
    	/*
    	contacts_list = new ContactList();
        contacts_list.getStyle().setBgTransparency(0);
        contacts_list.getStyle().setBorder(Border.createEmpty());
        contacts_list.setListCellRenderer(new ContactsRenderer());
        contacts_list.setSmoothScrolling(true);
        //contacts_list.setFixedSelection(List.FIXED_LEAD); //da testare
        contacts_list.addActionListener(contacts_list);
        /*Image contacts = null;
        Image  persons[] = null;
		*/
	
		/*
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
    	/*
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
		/*
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
		*/
	}
	
	public void commandActionOnlineMenu(Command id)
	{ 
		
	}
	
}
