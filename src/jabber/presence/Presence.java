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

package jabber.presence;


import util.Datas;
import util.Util;
import util.Contents;
import util.Base64;
//import com.twmacinta.util.MD5;

/**
 * Presence management class
 * @author Gabriele Bianchi 
 * Modified 04/01/2006
 */
public class Presence {

   /* public static final int OFFLINE = 0;
    public static final int ONLINE  = 1;
    public static final int AWAY    = 2;
    public static final int BUSY    = 3;    public static final int UNSUBSCRIBED = 4;    */
    
   static public String[]  string_presence = {"unavailable", "online", "away", "dnd", "unsubscribed"};
    
    /**
     * Send presence information
     * @param _presence
     */
    public static void changePresence(String _presence, String message) {
    	String send;
		if (message.equals(""))
		{
			if (_presence.equals("online"))
				send = "<presence/>";
			else
				send = "<presence from=\'" + Datas.jid.getFullJid() + "\'><show>" + _presence + "</show></presence>";
		}
		else
		{
			
				send = "<presence from=\'" + Datas.jid.getFullJid() + "\'><show>" + _presence + "</show><status>"+message+"</status></presence>";
		}
		//possibilità di inserire priority
    	Datas.writerThread.write(send); 
	}
    
	/**
	* Returns the protocol string corresponding to the presence value.
	* 
	* @param String
	*/
	public static String getPresence(String _presence) {
	return (String)Contents.presence.get(_presence);
	}
	/**
     * Returns the string corresponding to the presence value.
     * 
     * @param int
     */
	public static String getPresence(int _presence)
	{
		return string_presence[_presence];
	}
	/**
	* Create initial presence with avatar
	*
	*/
	public static String sendFirstPresence(byte[] image) {
		String send = "<presence/>";
		if (image != null) {
			try {
				String img = Util.sha1(image); //AVATAR
				send="<presence><x xmlns='vcard-temp:x:update'><photo>"+img+"</photo></x></presence>";
				Datas.jid.avatarHash = img;
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return send;//Datas.writerThread.write(send); 
	}
	/**
	* Create initial vcard with avatar
	*
	*/
	public static String sendFirstVCard(byte[] image) {
		String send = "";
		if (image != null) {
			try {
				String img = Base64.encode(image); //AVATAR
				send="<iq from='"+ Datas.jid.getFullJid() +"' type='set' id='vc1'><vCard xmlns='vcard-temp'><PHOTO><TYPE>image/png</TYPE><BINVAL>"+img+"</BINVAL></PHOTO></vCard></iq>";
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return send;//Datas.writerThread.write(send); 
	}
	/**
	* Get vcard with avatar
	*
	*/
	public static void getVCard( String to) {
		String send = "<iq from='"+ Datas.jid.getFullJid() +"' to='"+to+"' type='get' id='vc2'><vCard xmlns='vcard-temp'/></iq>";
		
		Datas.writerThread.write(send); 
		return;
	}
}
