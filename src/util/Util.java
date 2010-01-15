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

import xmlstreamparser.*;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import org.bouncycastle.crypto.digests.SHA1Digest;
import java.io.*;
import java.util.*;


public class Util {
    
    /**
     * Returns a SHA1 digest of the given string, in hex values lowercase.
     */
    public static String sha1(String _str) {
        String res;
        SHA1Digest digest = new SHA1Digest();
        String tmp = _str;
        byte in[] = tmp.getBytes();
        digest.update(in, 0, in.length);
        byte out[] = new byte[20];
        digest.doFinal(out, 0);
        
        // builds the hex string (lowercase)
        res = "";
        tmp = ""; // tmp = two characters to append to the hex string
        for (int i = 0; i < 20; i++) {
            int unsigned = out[i];
            if (out[i] < 0) {
                unsigned += 256;
            }
            tmp = Integer.toHexString(unsigned);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            res = res + tmp;
        }
        
        return res;
    }
    
    
    /**
     * Escapes the given string, for xml CDATA.
     */
    public static String escapeCDATA(String _str) {
        String escapeSource = "<>&'\"";
        String escapeDest[] = {"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};
        char ch;
        int pos;
        String res = "";
        for (int i=0; i<_str.length(); i++) {
            ch = _str.charAt(i);
            pos = escapeSource.indexOf(ch);
            if (pos!=-1) {
                res += escapeDest[pos];
            } else {
                res += ch;
            }
        }
        return res;
    }
    
    /**
     * Unescapes the given string, from an xml CDATA.
     */
    public static String unescapeCDATA(String _str) {
        String escapeSource = "<>&'\"";
        String escapeDest[] = {"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};
        char ch;
        int pos;  // position of the next amp '&' operator
        String res = "";
        
        while ((pos = _str.indexOf('&')) != -1) {
            // found a '&' character
            // take the string until the '&'
            res += _str.substring(0,pos);
            _str = _str.substring(pos);
            
            // unescape the character
            int i=0;
            boolean found=false;
            do {
                if (_str.startsWith(escapeDest[i])) {
                    found=true;
                } else {
                    i++;
                }
            } while (!found && (i<escapeDest.length));
            if (found) {
                res += escapeSource.charAt(i);
                _str = _str.substring(escapeDest[i].length());
            } else {
                // ERROR ***
                System.err.println("Parsing error: wrong escape character");
                _str = _str.substring(1);
            }
        }
        res += _str;
        return res;
    } 
    
    
}
