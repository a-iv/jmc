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

//import com.sun.lwuit.Dialog;

import jmc.GuiMidlet;
import util.Contents;

/**
 * 
 *
 * Media manager class
 * @author Gabriele Bianchi 
 */
public class MediaManager {

	
    /**
     * Activate a link during a conversation
     * @param link
     * @param midlet
    */
	public static void activateLink(String link, GuiMidlet midlet) {
		//System.out.println("Active link!");
		boolean is_image = false;
        PlayerManager manager;
		if (link.endsWith("wav") || link.endsWith("mpg") || link.endsWith("mpeg") || link.endsWith("mid") || link.endsWith("mp3")) {
		//if it is a video or audio
		    
			manager = new PlayerManager(link, midlet, is_image);
	    	//form.setCommandListener(manager);
			
    		Thread runner = new Thread(manager);
    		runner.start();
		}
   		else if (link.endsWith("jpg") || link.endsWith("jpeg") || link.endsWith("gif") || link.endsWith("png")) {
    		is_image = true;
    		manager = new PlayerManager(link, midlet, is_image);
    		//form.setCommandListener(manager);
		
    		Thread runner = new Thread(manager);
    		runner.start();
        
    	}
		else {//send to browser 
			try {
				if (midlet.platformRequest(link)){
					midlet.cm.terminateStream();//manual close application
					midlet.destroyApp(true);
				}
			}catch(javax.microedition.io.ConnectionNotFoundException e){
				
				//midlet.display.setCurrent(Contents.noPhone, midlet.getGuiConversation());
				//Dialog.show("", Contents.noPhone, null, Dialog.TYPE_ERROR,null, 3000);
				//bisogna richiamare getguiconv?
				return;
			}
		}
	}

	/**
	 * Play sound
	 * @param link
	 * @param midlet

	public static void playSound (String file, String type) throws Exception {
        
		InputStream is = StartMidlet.class.getResourceAsStream(file);
      		
		Player p = Manager.createPlayer(is,type);
		p.setLoopCount(1); // play once	
        
		p.realize();
		p.prefetch();
			//Play
		p.start();
		return;
	}
		*/


}
