/**
 * <p>XMPP parser for light Java devices.
 * It can handle XMPP streams.</p>
 *
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

import util.ExceptionListener;
import util.Util;
import threads.ReaderThreadListener;


public class Parser implements ReaderThreadListener {
 // fields 
  ParserListener listener;
  ExceptionListener exceptionListener;

  StringBuffer buffer; // character input buffer
  int internal_state; // state of the finite state machine
  boolean after_prolog; // Indicate if we have already read the xml prolog
  char code; // character code being read
  Character ch; // character being read
  Node node; // node being read
  String attribute; // Last attribute name

  int string_reader_target; // Target of the string reader
  static final int NODE_NAME = 0;
  static final int ATTRIBUTE_NAME = 1;
  static final int ATTRIBUTE_VALUE = 2;
  static final int NODE_TEXT = 3;

  /**
   * Used when string_reader_state is READ_STRING1 or READ_STRING2
   * Tells if the next character is marked as special, such as " in \"
   */
  boolean special_char = false;

  // State of the automatic string reader
  int string_reader_state = DEACTIVATED;

  final static int DEACTIVATED = 0;
  final static int READ_TEXT = 1;
  final static int READ_IDENT = 2;
  final static int READ_STRING1 = 3; // single quote string
  final static int READ_STRING2 = 4; // double quote string
  final static int READ_BLANK = 5;  // space, tabs..


 // standard methods / constructor 
  /**
   * Create a new Parser
   */
  public Parser(ParserListener _listener,
                ExceptionListener _exceptionListener) {
    listener = _listener;
    exceptionListener = _exceptionListener;
    
    internal_state = 0;
    after_prolog = false;
    string_reader_state = DEACTIVATED;
    buffer = new StringBuffer();
    node = null; // parent node of the current node
  }

  /**
   * Reads the given character
   */
  public void read(Object reader, int _code) {
    boolean eated; // tells if the character has been eated
    
    code = (char) _code; 
    
    ch = new Character(code);

    
    eated = false;
    do {
      switch (string_reader_state) {
        case DEACTIVATED:
          eated = read_standard();
          break;
        case READ_TEXT:
          eated = read_text();
          break;
        case READ_IDENT:
          eated = read_ident();
          break;
        case READ_STRING1:
        case READ_STRING2:
          eated = read_string();
          break;
        case READ_BLANK:
          eated = read_blank();
          break;
      }
    }
    while (!eated);
  }
  
  
  /**
   * 
   * @return boolean
   */
  public boolean read_standard() {
    boolean eated = true;

    // tells if the char is a letter: a-zA-Z
    boolean isLetter;

    isLetter = ((code >= 'a') && (code <= 'z')) |
        ((code >= 'A') && (code <= 'Z'));

    // tells if the char is a blank: space, tab, return
    boolean isBlank;

    isBlank = (code == ' ') |
        (code == '\n') |
        (code == '\t') |
        (code == '\r');

    switch (internal_state) {
      case 0:
        if (code == '<') {
          internal_state = 1;
        }
        else if (isBlank) {
          listener.blank();
        }
        break;
      case 1:
        if (code == '?') {
          if (!after_prolog) {
            //System.out.println("begin prolog");
            node = new Node(node);
            internal_state = 2;
          }
        }
        else if (code == '/') {
          //System.out.println("end element");
          internal_state = 19;
        }
        else if (isLetter) {
          //System.out.println("begin element");
          node = new Node(node);
          after_prolog = true;
          internal_state = 2;
          eated = false;
        }
        break;
      case 2:
        string_reader_state = READ_IDENT;
        string_reader_target = NODE_NAME;
        internal_state = 3;
        break;
      case 3:
        string_reader_state = READ_BLANK;
        internal_state = 17;
        break;
      case 4:
        string_reader_state = READ_IDENT;
        string_reader_target = ATTRIBUTE_NAME;
        internal_state = 18;
        break;
        // no case 5!!!
      case 6:
        if (code == '=') {
          internal_state = 7;
        }
        else {
          internal_state = 3;
        }
        break;
      case 7:
        string_reader_state = READ_BLANK;
        internal_state = 8;
        break;
      case 8:
        if (code == '\'') {
          string_reader_state = READ_STRING1;
        }
        else if (code == '\"') {
          string_reader_state = READ_STRING2;
        }
        else {
          string_reader_state = READ_IDENT;
        }
        string_reader_target = ATTRIBUTE_VALUE;
        internal_state = 3;
        break;
        // no case 9, 10, 11
      case 12:
        if (code == '>') {
          //System.out.println("end prolog");
          listener.prologEnd(node);
          node = node.parent;
          internal_state = 13;
        }
        break;
      case 13:
        string_reader_state = READ_TEXT;
        string_reader_target = NODE_TEXT;
        internal_state = 0;
        break;
      case 14:
        if (code == '>') {
          //System.out.println("end basic element");
          listener.nodeEnd(node);
          node = node.parent;
          internal_state = 0;
        }
        break;
        // no case 15, 16
      case 17:
        if (code == '?') {
          internal_state = 12;
        }
        else if (code == '>') {
          listener.nodeStart(node);
          internal_state = 13;
        }
        else if (code == '/') {
          internal_state = 14;
        }
        else {
          internal_state = 4;
          eated = false;
        }
        break;
      case 18:
        string_reader_state = READ_BLANK;
        internal_state = 6;
        break;
      case 19:
        string_reader_state = READ_IDENT;
        string_reader_target = NODE_NAME;
        // we could check that the name are correct
        internal_state = 20;
        break;
      case 20:
        if (code == '>') {
          //System.out.println("end element");
          listener.nodeEnd(node);
          node = node.parent;
          internal_state = 0;
        }
        break;
    }

    if ( (string_reader_state == READ_IDENT) |
        (string_reader_state == READ_TEXT) |
        (string_reader_state == READ_BLANK)) {
      eated = false;
    }

    return eated;
  }
 /**
  * 
  * @return boolean
  */
  public boolean read_text() {
    boolean eated = false;

    if (code == '<') {
      // end of text
      //System.out.println("text:" + buffer.toString());
      read_map(); // map the read text
      buffer = new StringBuffer();

      string_reader_state = DEACTIVATED;
    }
    else {
      // constructs the text...
      buffer.append(code);
      eated = true;
    }
    return eated;
  }

  /**
   * 
   * @return boolean
   */
  public boolean read_ident() {
    boolean eated = false;

    if (code == ' ' | code == '\n' | code == '\t' | code == '='
        | code == '>' | code == '/') {
      // end of ident
      //System.out.println("ident:" + buffer.toString());
      read_map(); // map the read text
      buffer = new StringBuffer();
      string_reader_state = DEACTIVATED;
    }
    else {
      // constructs the ident...
      buffer.append(code);
      eated = true;
    }
    return eated;
  }

  /**
   * 
   * @return boolean
   */
  public boolean read_string() {
    if (special_char) {
      // NOT TESTED
      // we are reading a special char, such as " in \"
      // constructs the string...
      buffer.append(code);
      special_char = false;
    } else if (code == '\\') {
      // special char will follow
      special_char = true;
    } else if ( ( (code == '\'') && (string_reader_state == READ_STRING1)) |
             ( (code == '\"') && (string_reader_state == READ_STRING2))) {
      // end of the string
      //System.out.println("string:" + buffer.toString());
      read_map(); // map the read text
      buffer = new StringBuffer();

      string_reader_state = DEACTIVATED;
    } else {
      // we are still in the string, normal char
      // constructs the string...
      buffer.append(code);
    }
    return true;
  }

  /**
   * 
   * @return boolean
   */
  public boolean read_blank() {
    boolean eated = false;
    // \n and \r may not be needed.
    if (code == ' ' | code == '\n' | code == '\t' | code == '\r') {
      // we are still in the blank
      eated = true;
    } else {
      
      string_reader_state = DEACTIVATED;
    }
    return eated;
  }

  /**
   * Maps the current string value to the corresponding node attribute.
   */
  public void read_map() {
    String str = serverToUnicode(buffer.toString());
    switch (string_reader_target) {
      case NODE_NAME:
        node.name = str;
        break;
      case NODE_TEXT:
        if (node != null) {
          node.text = Util.unescapeCDATA(str);
        }
        break;
      case ATTRIBUTE_NAME:
        attribute = str; // save the attribute name
        // attribute has no value yet
        node.attributes.put(str, "");
        break;
      case ATTRIBUTE_VALUE:
        node.attributes.put(attribute, str);
        break;
    }
  }
  
 /**
  * Unicode Support
  * © 2003, 2004 Vidar Holen
	* www.vidarholen.net
  *
  */
  public String serverToUnicode(String s) {
        StringBuffer sb=new StringBuffer();
        char[] a=s.toCharArray();
        char t;
        int cnt;
        for(int i=0; i<a.length; i++) {
            if(a[i]<0x80) {
                sb.append(a[i]);
            } else if(a[i]>0xEF) {
                return s; //with 16-bit chars, we don't bother with more
            } else {
                t=a[i]; 
                if((t|0xE0)==t) cnt=2; 
                else if((t|0xC0)==t) cnt=1;
                else return s;
                t=(char)(t&(((1<<(6-cnt))-1)));
                for(int j=0; j<cnt; j++) {
                    i++;
                    if(i==a.length || a[i]>0xBF) return s;
                    int k=t;
                    t=(char)((t<<6)|(a[i]&0x3f));
                }
                sb.append(t);
            }
        }
        return sb.toString();
    }

}
