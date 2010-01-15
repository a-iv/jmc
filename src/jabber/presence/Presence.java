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

package jabber.presence;

import jabber.roster.*;

public class Presence {

    public static final int OFFLINE = 0;
    public static final int ONLINE  = 1;
    
    
    static String[] string_presence = {"offline", "online"};
    
    /**
     * return the presence value (OFFLINE, ONLINE..) of a string, 
     * for example "OFFLINE" or "Online". The test is not case sensitive.
     */
    public static int getPresence(String _presence) {
        int res = OFFLINE;
        _presence.toLowerCase(); // non case sensitive test
        
        int i=0;
        boolean found=false;
        while ((i<string_presence.length) && !found) {
            if (_presence.equals(string_presence[i])) {
                found = true;
            } else {
                i++;
            }
        }

        if (found) {
            res = i;
        } else {
            res = ONLINE; // ??? should I throw an error?
        }
        
        return res;
    }
    
    /**
     * Returns the string corresponding to the presence value.
     * example: getPresence(OFFLINE) => "offline"
     */
    public static String getPresence(int _presence) {
        return string_presence[_presence];
    }
}
