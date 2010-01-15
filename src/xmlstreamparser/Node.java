/**
 *
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
package xmlstreamparser;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;


/**
 * Basic functions:
 * - hasAttribute= attributes.containsKey("attribute name");
 * - Get attribute value= attributes.get("attribute name");
 * - Add attribute= attributes.put("attribute name", "attribute value");
 * - Get child= children.get(i);
 * - Add child= children.add(child);
 * Note that string values must not be null: empty strings are used instead.
 */

public class Node {
    
    /**
     * Parent node. It is null if there is no parent (main node)
     */
    public Node parent;
    
    /**
     * Node name
     * ex: <stream:stream ...> "stream:stream", or <iq ...> "iq"
     */
    public String name;
    
    /**
     * Node attributes. key = attribute name, value = attribute value.
     * ex: <... to="greg@w9f05952" ...> key="to" value="greg@w9f05952"
     * ex: <... novalue ...> key="novalue" value=empty string.
     */
    public Hashtable attributes;
    
    /**
     * Node text. It is an empty string if there is no text.
     * ex: <bal>mytext jflf  </bal> text="mytext jflf  "
     * ex: <bal></bal> text=empty string
     */
    public String text;
    
    /**
     * Children nodes, in order of appearance in the xml document.
     */
    public Vector children;
    
    /**
     * Constructor. Values (such as name, attributes, text, children..) are
     * given later with basic operations. ex: node.name="stream:stream";
     *
     * @param _parent Parent node. Is null if the node has no parent (main node).
     */
    public Node(Node _parent) {
        parent = _parent;
        name = "";
        attributes = new Hashtable(4);
        text = "";
        children = new Vector(3,1);
        
        if (parent != null) {
            parent.children.addElement(this);
        }
    }
    /**
     * Constructor/Converter 
     * @param _node 
     *
     */
    public Node(HttpNode _node) {
    	
    	
    	name = _node.name;
    	text = _node.value;
    	attributes = _node.attributes;
		children = new Vector(3, 1);
    	setChildren(_node.childs);

	}
    
    /**
     * Copy the children nodes 
     *
     */
    private void setChildren(Vector http) {
		//System.out.println("vettore figli: "+http);
    	if (http != null) {
    		for (int i=0; i<http.size(); i++) 
    			children.addElement(new Node((HttpNode)http.elementAt(i)));//RICORSIONE!!
    	}	
    	
    }
    /**
     * Returns the value of an attribute given its name.
     *
     * @return value of the given attribute. Returns null if there is no
     * such attribute. Returns an empty string if the attribute has no value.
     */
    public String getValue(String _attributeName) {
        return (String) attributes.get(_attributeName);
    }
    
    /**
     * Returns a child node given its name (node name). Returns null if 
     * there is no such child. If several children have the same name,
     * this method returns the first matching child.
     */
    public Node getChild(String _nodeName) {
        int i = 0;
        boolean found = false;
        Node tmp = null;
        while ( (i < children.size()) && !found) {
            tmp = (Node) children.elementAt(i);
            if (tmp.name.equals(_nodeName)) {
                found = true;
            }
            i++;
        }
        if (!found) {
            tmp = null;
        }
        return tmp;
    }

    /**
     * Returns a child node given its name (node name) and given attribute value. Returns null if 
     * there is no such child. If several children have the same name and attribute,
     * this method returns the first matching child.
     * @author Gabriele Bianchi
     * @param String String String
     * @return Node
     */
    public Node getChild(String _nodeName, String _attrName, String _attrVal) {
        int i = 0;
        boolean found = false;
        Node tmp = null;
        while ( (i < children.size()) && !found) {
            tmp = (Node) children.elementAt(i);
            if (tmp.name.equals(_nodeName) && tmp.getValue(_attrName).equals(_attrVal)) {
                found = true;
            }
            i++;
        }
        if (!found) {
            tmp = null;
        }
        return tmp;
    }    
    /**
     * @author Gabriele Bianchi 
     * @return Vector
     */
    public Vector getChildren() {
		return children;
    }
    
    // This code is optional, since the application may not need it.
   
    /**
     * @return String
    */ 
    public String toString() {
        // used to lookup attributes in the hashtable
        String value;
        String key;

        // to increase speed, we use a temporary string buffer. Inited with 32 chars capacity.
        // For compile memory consumption, on my config: 
        // without this method=1145B, with(String version)=1830B, with(StringBuffer version)=1846B.
        StringBuffer buffer = new StringBuffer(64);

        buffer.append('<');
        buffer.append(name);

        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement(); // name of the attribute
            buffer.append(' ');
            buffer.append(key);
            value = (String) attributes.get(key);
            if (value != "") {
                // attribute has a value
                buffer.append("='");
                buffer.append(value);
                buffer.append("'");
            }
        }
        buffer.append('>');
        buffer.append(text);
        
        Node child;
        for (int i = 0; i < children.size(); i++) {
            child = (Node) children.elementAt(i);
            buffer.append(child.toString());
        }
        buffer.append("</");
        buffer.append(name);
        buffer.append('>');
        return buffer.toString();
    }
    
    
}
