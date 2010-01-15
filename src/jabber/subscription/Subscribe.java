/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package jabber.subscription;

import util.Datas;

import jabber.roster.Jid;
import java.util.Vector;
import java.util.Enumeration;
/**
 * Class for subscription management
 * @author Gabriele Bianchi
 *
 */
public class Subscribe {

	/**
	 * Create/Update contact
	 * @param _jid
	 * @param group
	 */
	public static void setNewRosterItem(Jid _jid, boolean isNew) {
		String res;
		
	
			res = "<iq from=\'" + Datas.jid.getFullJid() + "\' type='set' id='Set_roster'><query xmlns='jabber:iq:roster'><item jid=\'" + _jid.getFullJid() + "\' name=\'" + _jid.getUsername() + "\'><group>" + _jid.group + "</group></item></query></iq>";

			

		if (isNew)
			res += "<presence from=\'" + Datas.jid.getFullJid() + "\' to=\'" + _jid.getFullJid() + "\' type='subscribe'/>";
		Datas.writerThread.write(res);
	}

	/**
	 * Remove a contact
	 * @param _jid
	 */
	public static void removeRosterItem(Jid _jid) {
		String res =  "<iq from=\'"+ Datas.jid.getFullJid() + "\' type=\'set\' id=\'roster_2\'><query xmlns=\'jabber:iq:roster\'><item jid=\'"+_jid.getFullJid()+"\' subscription=\'remove\'></item></query></iq>";
		Datas.writerThread.write(res);
	}

	/**
	 * Remove some contacts
	 * @param items
	 */
	public static void removeRoster(Vector items)
	{
		String res = "<iq from=\'" + Datas.jid.getFullJid() + "\' type=\'set\' id=\'roster_remove\'><query xmlns=\'jabber:iq:roster\'>";
		for (int i = 0; i < items.size(); i++)
		{
			Datas.roster.remove(items.elementAt(i));
			res += "<item jid=\'" + items.elementAt(i) + "\' subscription=\'remove\'></item>";
		}
		res += "</query></iq>";
		Datas.writerThread.write(res);
	}
	
	/**
	 * Request a subscription to a contact
	 * @param _jid
	 */
	public static void requestSubscription(Jid _jid){
		String res;
		
		res = "<presence from=\'" + Datas.jid.getFullJid() + "\' to=\'" + _jid.getFullJid() + "\' type='subscribe'/>";
	

		Datas.writerThread.write(res);
	}

	/**
	 * Deny a subscription request from a contact
	 * @param _jid
	 */
	public static void denySubscription(Jid _jid) {
		String res = "<presence from=\'"+Datas.jid.getFullJid()+"\' to=\'"+_jid.getFullJid()+"\' type='unsubscribed'/>";
		Datas.writerThread.write(res);
	}
	
	/**
	 * Accept a subscription request from a contact
	 * @param _jid
	 */
	public static void acceptSubscription(Jid _jid) {
		String res = "<presence from=\'"+Datas.jid.getFullJid()+"\' to=\'"+_jid.getFullJid()+"\' type='subscribed'/>";
		Datas.writerThread.write(res);
	}


	/**
	 * Set a contact phone number
	 * @param _jid, num
	 */
	public static void setPhoneNumber(Jid _jid, String num) {
		String res;
		String username;
		if (Datas.isGateway(_jid.getServername()) && _jid.getUsername().indexOf("%") != -1)
			username = _jid.getUsername().substring(0, _jid.getUsername().indexOf("%"));
		else
			username = _jid.getUsername();
		res = "<iq type='set' id='setNum' ><query xmlns='jabber:iq:private'><" + username + "phone xmlns='" + username + "phone:number'><num user='" + _jid.getFullJid() + "'>" + num + "</num></" + username + "phone></query></iq>";
		Datas.writerThread.write(res);
	}
	/**
	 * Get a contact phone number
	 * @param _jid, num
	 */
	public static void getPhoneNumber(Jid _jid) {
		String res;
		String username;
		if (Datas.isGateway(_jid.getServername()) && _jid.getUsername().indexOf("%") != -1)
			username = _jid.getUsername().substring(0, _jid.getUsername().indexOf("%"));
		else
			username = _jid.getUsername();

		res = "<iq type='get' id='getNum' ><query xmlns='jabber:iq:private'><" + username + "phone xmlns='" + username + "phone:number'></" + username + "phone></query></iq>";
		Datas.writerThread.write(res);
	}
	/**
	 * Register to other protocol gateway
	 */
	public static void registerGateway(String user, String passw, String gateway)
	{
		String res = "<iq type=\'set\' from=\'" + Datas.jid.getFullJid() + "\' to=\'" + gateway + "\' id='regGateway'><query xmlns='jabber:iq:register'><username>" + user + "</username><password>" + passw + "</password></query></iq>";
		Datas.writerThread.write(res);
	}

	/**
	 * Unregister to other protocol gateway
	 */
	public static void unregisterGateway(String gateway)
	{
		String res = "<iq type=\'set\' from=\'" + Datas.jid.getFullJid() + "\' to=\'" + gateway + "\' id='unregGateway'><query xmlns='jabber:iq:register'><remove/></query></iq>";
		Datas.writerThread.write(res);
		Vector items = new Vector();
		Enumeration contacts = Datas.roster.keys();
		while (contacts.hasMoreElements())
		{
			Jid jid = new Jid((String)contacts.nextElement());
			if (jid.getServername().equals(gateway))
				items.addElement(jid.getLittleJid());
		}

		removeRoster(items);
	}

}
