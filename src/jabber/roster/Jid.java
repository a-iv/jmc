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

package jabber.roster;

/**
 * Jabber ID. It has one of the following form: 
 * - servername
 * - username@servername
 * - username@servername/resource 
 */


public class Jid {

    private String servername;
    private String username;
    private String resource;

   // ....... constructor .............
    /**
     * Constructs a Jid, given its string representation
     * example: "myserver" or "username@myserver" or "username@myserver/resource"
     */
    public Jid(String _jid) {
        int at = _jid.indexOf('@');
        int slash = _jid.indexOf('/', at);

        if (at==-1) {
            username = null;
        } else {
            username = _jid.substring(0,at);
        }

        if (slash==-1) {
            // no resource
            servername = _jid.substring(at+1);
            resource = null;
        } else {
            // resource
            servername = _jid.substring(at+1,slash);
            resource = _jid.substring(slash+1);
        }
    }
    
   // ....... fields accessors ........
    public String getServername() {
        return servername;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String _val) {
        if (_val=="") {
            resource = null;
        } else {
            resource = _val;
        }
    }
    
    /**
     * returns the little jid, that is without the eventual resource information
     */
    public String getLittleJid() {
        String res = "";
        if (username != null) {
            res += username + "@";
        }
        res += servername;
        return res;
    }
    
    /**
     * returns the full jid, that is with the eventual resource information
     */
    public String getFullJid() {
        String res = getLittleJid();
        if (resource!=null) {
            res += "/" + resource;
        }
        return res;
    }

   // ...... static methods ...........
    /**
     * returns the jid without the eventual resource.
     */
    public static String getLittleJid(String _jid) {
        String res;
        int at = _jid.indexOf('@');
        int slash = _jid.indexOf('/', at); 
        
        if (slash == -1) {
            res = _jid;
        } else {
            res = _jid.substring(0, slash);
        }
        return res;
    }
}

