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
//import jabber.roster.*;
import util.Util;
import util.Datas;
import jabber.roster.Jid;

public class Message {
	
	private boolean processed = false;
	private boolean smile_processed = false;
	public String subject;  // the subject of the message, "" if no subject
	public String body;  // the body of the message, "" if no body
	public String from;
	
	public Message(String _subject, String _body) {
		subject = _subject;
		body = _body;
		from = Datas.jid.getUsername();
	}
	public Message(String _subject, String _body, String _from) {
		subject = _subject;
		body = _body;
		from = _from;
	}
	public Message(Node _node) {
		subject = "";

		if (_node.getChild("subject") != null) {
			subject = _node.getChild("subject").text;
		}
		body = "";

		if (_node.getChild("body") != null) {			
			body = _node.getChild("body").text;
			
		}
		if (_node.getValue("from") != null)
			from = _node.getValue("from");
	}
	
	/**
	 * Returns the subject and the body, separated by a newline
	 * @return String
	 */
	public String getText() {
		StringBuffer res = new StringBuffer();
		if (subject.equals("")) {
			if (!smile_processed)
			{
				body = setSmiles(body);
				smile_processed = true;
			}
			if (!processed) {
				body = setLink(body);
				processed = true;
			}

			res.append(Jid.getUsername(from)).append(">").append(body).append("\n");
		} else {
			if (body.equals("")) {
				res.append(Jid.getUsername(from)).append(">").append(subject);
			} else {
				if (!smile_processed)
				{
					body = setSmiles(body);
					smile_processed = true;
				}
				res.append(Jid.getUsername(from)).append(">").append(subject).append("\n").append(body).append("\n");
			}
		}
		//String xml = res.toString();
		
		return res.toString();
	}
	/**
	 * Returns the subject and the body, separated by a newline (with the nick of the author)
	 * @return String
	 */
	public String getTextNick() {
		StringBuffer res = new StringBuffer();
		int i;
		String jid;
		if ((i = from.indexOf("/")) != -1) 
		 	jid = from.substring(i+1, from.length());
		else //chat service message
			jid = Jid.getUsername(from);
		if (subject.equals("")) {
			if (!smile_processed)
			{
				body = setSmiles(body);
				smile_processed = true;
			}
			if (!processed) {
				body = setLink(body);
				processed = true;
			}
			res.append(jid).append(">").append(body).append("\n");
		} else {
			if (body.equals("")) {
				res.append(jid).append(">").append(subject);
			} else {
				if (!smile_processed)
				{
					body = setSmiles(body);
					smile_processed = true;
				}
				res.append(jid).append(">").append(subject).append("\n").append(body).append("\n");
			}
		}
		
		
		return res.toString();
	}
	
	/**
	 * returns the subject and the body in "xml"
	 * example: 
	 * &gt;subject&lt;pollution&gt;/subject&lt;
	 * &gt;body&lt;pollution in frankfurt today!&gt;/body&lt;
	 *
	 */
	public String getTextAsXML() {
		StringBuffer res = new StringBuffer();
		if (!subject.equals("")) {
			res.append("<subject>").append(Util.escapeCDATA(subject)).append("</subject>");
		}
		
		if (!body.equals("")) {
			res.append("<body>").append(Util.escapeCDATA(body)).append("</body>");
					
		}
		//check if there is an url
		String xml = res.toString();
		/*int i = -1;
		if ((i = xml.indexOf("http://")) != -1 ) {
			int end = xml.indexOf(" ", i);
			if (end == -1)
				end = xml.indexOf("</body>");
			xml = xml.substring(0, i) + "<x xmlns='jabber:x:oob'><url>" + xml.substring(i, end) +"</url></x>" + xml.substring(end, xml.length());
				
		}*/
		return xml;
	}
	/**
	 * Find smiles and replace them with codes  (1 smile for message supported)
	 */
	public static String setSmiles(String body) {
		int i =-1;//check for smiles
		

		if ((i=body.indexOf(":)")) != -1 || (i=body.indexOf(":-)")) != -1) {
			body = body.substring(0, i)+ "1smile1 "+ body.substring(body.indexOf(")",i)+1, body.length());
		
		}

		else if ((i = body.indexOf(":(")) != -1 || (i = body.indexOf(":-(")) != -1)
		{
			body = body.substring(0, i) + "1smile2 " + body.substring(body.indexOf("(", i) + 1, body.length());
	
		}
		else if ((i = body.indexOf(":D")) != -1)
		{
			body = body.substring(0, i) + "1smile3 " + body.substring(body.indexOf("D", i) + 1, body.length());

		}
		else if ((i = body.indexOf(":P")) != -1)
		{
			body = body.substring(0, i) + "1smile4 " + body.substring(body.indexOf("P", i) + 1, body.length());

		}
		return body;
		
	}
	/**
	 * Find links in the body
         */	
	public static String setLink(String body) {
		int i = -1;
		if ((i = body.indexOf("http://")) != -1 || (i = body.indexOf("https://")) != -1) {
			int end = body.indexOf(" ", i);
			if (end == -1)
				end = body.length();
			body = body.substring(0, i) + "+url+" + body.substring(i, end) +"-url-" + body.substring(end, body.length());
				
		}
		return body;
	}

	public void addError(String error_text) {
		body = body + "\n" + error_text;
	}	

}
