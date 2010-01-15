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

package jabber.conversation;

import xmlstreamparser.*;
import jabber.roster.*;
import util.*;

public class Message {
    
    public String subject;  // the subject of the message, "" if no subject
    public String body;  // the body of the message, "" if no body
    
    public Message(String _subject, String _body) {
        subject = _subject;
        body = _body;
    }
    
    public Message(Node _node) {
        subject = "";
        //System.out.println("v");
        if (_node.getChild("subject") != null) {
            subject = _node.getChild("subject").text;
        }
        body = "";
        //System.out.println("w");
        if (_node.getChild("body") != null) {
            body = _node.getChild("body").text;
        }
    }
    
    /**
    * returns the subject and the body, separated by a newline
    */
    public String getText() {
        String res = "";
        if (subject.equals("")) {
            res = body;
        } else {
            if (body.equals("")) {
                res = subject;
            } else {
                res = subject + "\n" + body;
            }
        }
        return res;
    }
    
    /**
    * returns the subject and the body in "xml"
    * example: 
    * &gt;subject&lt;pollution&gt;/subject&lt;
    * &gt;body&lt;pollution in frankfurt today!&gt;/body&lt;
    *
    */
    public String getTextAsXML() {
        String res = "";
        if (!subject.equals("")) {
            res = "<subject>"+Util.escapeCDATA(subject)+"</subject>";
        }
        if (!body.equals("")) {
            res = res + "<body>"+Util.escapeCDATA(body)+"</body>";
        }
        return res;
    }
    
}
