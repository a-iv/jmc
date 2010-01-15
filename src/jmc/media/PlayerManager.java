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
package jmc.media;

import jmc.GuiMidlet;
import javax.microedition.lcdui.Item;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import util.Contents;
/*import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.CommandListener;*/
import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.MediaComponent;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;


/**
 * @author Gabriele Bianchi 
 *
 * Player manager for audio/video
 * 
 */
class PlayerManager implements Runnable, ActionListener, PlayerListener {

	
	Form form;
  	
  	Player player;
  	String locator;
  	//Display display;
	GuiMidlet midlet;
    	boolean is_image;

	public PlayerManager(String _locator, GuiMidlet _midlet, boolean isImage) {
		locator = _locator;
		midlet = _midlet;
		//display = midlet.display;
        	is_image = isImage;
		form = new Form("Player Manager");
		form.setLayout(new BorderLayout());
		form.setCommandListener(this);
       		if (is_image)
            	form.addCommand(Contents.back);
        	else
				form.addCommand(Contents.stop);
		//form.addCommand(Contents.pause);
		//form.addCommand(Contents.play);
	} 

	public void run() {

   	 try {
      // since we are loading data over the network, a delay can be
      // expected
           
            Dialog.show("", Contents.done, null, Dialog.TYPE_INFO,null, 3000);
			//bisogna richiamare getguiconv?

        StreamConnection fconn = (StreamConnection)Connector.open(locator);
        InputStream is = fconn.openInputStream();
        String type = "";
  			
        if (is_image) {
            try {
                form.addComponent(BorderLayout.CENTER, new Label(Contents.displayImage(is)));
                form.show();
            }catch (NullPointerException e) {
                form.removeAll(); // clears the form of any previous controls
                Dialog.show("", Contents.noPhone, Dialog.TYPE_ERROR,null, "Ok","");
               // midlet.getGuiUpdateConversation(0);
                //bisogna richiamare getguiconv?
            }
        }
        else {  
		int count = 1;
		boolean markable = true;
                if (locator.endsWith("wav") ) {
                        type = "audio/x-wav";
			markable = false;
		}
                else if (locator.endsWith("mp3"))
                        type = "audio/mpeg";
                else if (locator.endsWith("mid"))
                        type = "audio/mid";
                else if ( locator.endsWith("mpg") || locator.endsWith("mpeg")) {
                        type = "video/mpeg";      
			count = -1; //play forever
		}
	
            player = Manager.createPlayer(is, type);
            player.addPlayerListener(this);
    		

      		player.setLoopCount(count); // play count times
      		player.prefetch(); // prefetch
      		player.realize(); // realize

		    
            MediaComponent media = new MediaComponent(player);
            form.addComponent(BorderLayout.CENTER, media);
            form.show();
            player.start();
      		//player.start(); // and start
      		
            }
        } catch(Exception e) {
      		System.err.println(e.getMessage());
			
                form.removeAll(); // clears the form of any previous controls
                
                Dialog.show("", Contents.noPhone, null, Dialog.TYPE_ERROR,null, 3000);
				//bisogna richiamare getguiconv?
    			}
  	}
/*
	public void commandAction(Command command, Displayable disp) {
        if(disp instanceof Form) {
      // if showing form, means the media is being played
      // and the user is trying to stop or pause the player
			
            try {
                if(command == Contents.stop) { // if stopping the media play
                    player.close(); // close the player
                    
                    display.setCurrent(midlet.getGuiConversation()); // return to gui
                
                } else if (command == Contents.back) {
                    form.deleteAll(); // clears the form of any previous controls
                    display.setCurrent(midlet.getGuiConversation()); // return to gui
                } 
            }catch(Exception e) {
                System.err.println(e);
                form.deleteAll(); // clears the form of any previous controls
                midlet.getGuiConversation());
            }
        }
  	}
*/
	/** 
	 * Handle player events 
	 */
  	public void playerUpdate(Player player, String event, Object eventData) {

    // if the event is that the player has started, show the form
    // but only if the event data indicates that the event relates to newly
    // stated player, as the STARTED event is fired even if a player is
    // restarted. Note that eventData indicates the time at which the start
    // event is fired.
    		if ((event.equals(PlayerListener.STARTED)) && new Long(0L).equals((Long)eventData)) {

      // see if we can show a video control, depending on whether the media
      // is a video or not
      			VideoControl vc = null;
      			if((vc = (VideoControl)player.getControl("VideoControl")) != null) {
        			try {
						vc.initDisplayMode(vc.USE_GUI_PRIMITIVE, null);
						//form.addComponent(videoDisp);
						vc.setDisplayLocation(0,0);
						vc.setDisplayFullScreen(true);
						vc.setVisible(true);
					} catch (MediaException e) {
						// TODO Auto-generated catch block
						
					}
      			}

      			form.show();
    		} else if(event.equals(PlayerListener.CLOSED)) {

      			form.removeAll(); // clears the form of any previous controls
    		}
  	}

	public void actionPerformed(ActionEvent arg0) {
		Command command = arg0.getCommand();
        try {
            if(command == Contents.stop) { // if stopping the media play
                player.close(); // close the player
                
                midlet.getGuiConversation(midlet.tabbedPane.getSelectedIndex()); // return to gui
            
            } else if (command == Contents.back) {
            	
                midlet.getGuiConversation(midlet.tabbedPane.getSelectedIndex()); // return to gui
                form.removeAll(); // clears the form of any previous controls
            } 
        }catch(Exception e) {
            System.err.println(e);
            form.removeAll(); // clears the form of any previous controls
            midlet.getGuiUpdateConversation(-1);
        }
		
	}
	
}
