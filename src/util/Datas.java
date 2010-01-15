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

package util;

import jabber.roster.*;
import java.util.*;
import org.bouncycastle.crypto.digests.SHA1Digest;

public class Datas {

   // stream connection parameters
   
    public static Jid jid;
    //private static String servername;
    //private static String username;
    //private static String resource;
    private static String password; // clear text, should be escaped?
    private static String sessionId;
    
   // other fields
    public static Vector conversations;
    public static Hashtable rosters;  // rosters, key= jid without resource
    public static RosterClassification rosterClasses; // rosters ordered by group (family, friends..)
    public static WriterThread writerThread;
    

   // ....... initialisation ..........
    public static void load() {
        // init the variables from RMS...
        jid = new Jid("greg@w9f05952/mobile");
        password = "a";
        
        conversations = new Vector();
        rosters = new Hashtable();
        rosterClasses = new RosterClassification();
        
        Vector lists = rosterClasses.getLists();
        RosterList family = new RosterList("Family");
        
        Roster yohann = registerRoster("yohann", new Jid("yohann@hotmail.com"));
        Roster paris = registerRoster("päris", new Jid("paris13@g.gr"));
        
        Roster gregmail = registerRoster("greg mail", new Jid("gregoireathanase@t-systems.com"));
        
        Roster matt = registerRoster("matt", new Jid("matt@w9f05952"));

        family.rosters.addElement(yohann);
        family.rosters.addElement(paris);
        
        RosterList friends = new RosterList("Friends");
        friends.rosters.addElement(gregmail);
        friends.rosters.addElement(matt);

        lists.addElement(family);
        lists.addElement(friends);
        lists.addElement(new RosterList("work"));
    }
    
   // ....... fields accessors ........
    /**
     * Computes the password in conformance with JEP-0078
     * @return digest password
     */
    public static String getDigestPassword() {
        return Util.sha1(sessionId + password);
    }

    public static void setPassword(String _val) {
        // *** escape the password?
        password = _val;
    }

    public static void setSessionId(String _val) {
        sessionId = _val;
    }
    
   // ............ methods ..............
    // this method may be temporary (for debug uses only)
    private static Roster registerRoster(String _name, Jid _jid) {
        Roster res = new Roster(_name, _jid);
        rosters.put(_jid.getLittleJid(), res);
        return res;
    }
    
}

