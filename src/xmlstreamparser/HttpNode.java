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

package xmlstreamparser;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Http node
 *
 */
public class HttpNode {

	public Vector childs;
	public Hashtable attributes;
	public String name;
	public String value;
	private InputStream is = null;
	private String buff;
	private boolean ignoreNS = true;
	int pos = 0;

	public HttpNode()
	{
		super();
		attributes = new Hashtable();
		childs = new Vector();
	}

	public int getNextCharacter() throws Exception
	{
		int i = -1;
		int j = is.read();
		if(j==-1)
			throw new Exception("InputStreamReader cannot read data");
		{
			j &= 0xff;
			boolean flag = false;
			switch(j >> 4)
			{
			case 8: // '\b'
			case 9: // '\t'
			case 10: // '\n'
			case 11: // '\013'
			default:
				break;

			case 0: // '\0'
			case 1: // '\001'
			case 2: // '\002'
			case 3: // '\003'
			case 4: // '\004'
			case 5: // '\005'
			case 6: // '\006'
			case 7: // '\007'
				i = j;
				break;

			case 12: // '\f'
			case 13: // '\r'
				i = j & 0x1f;
				i <<= 6;
				int k = is.read();
				if((k & 0xc0) != 128)
					throw new Exception("Bad UTF-8 Encoding encountered");
				i += k & 0x3f;
				break;

			case 14: // '\016'
				i = j & 0xf;
				i <<= 6;
				int l = is.read();
				if((l & 0xc0) != 128)
					throw new Exception("Bad UTF-8 Encoding encountered");
				i += l & 0x3f;
				i <<= 6;
				l = is.read();
				if((l & 0xc0) != 128)
					throw new Exception("Bad UTF-8 Encoding encountered");
				i += l & 0x3f;
				break;
			}
		}
		return i;
	}

	private char nextChar() throws Exception
	{
		try {
			if(pos>buff.length()-1)
			{
				int ccc;
				boolean ok = true;
				do
				{
					ok = true;
					ccc = getNextCharacter();
					if(ccc==10)
						ok = false;
				}while(!ok);
				buff += (char)ccc;

			}
			char ch = buff.charAt(pos++);

			if(ch==-1)
			{
				System.out.println("conn closed");
				throw new Exception("Conn closed");
			}
			return ch;
		} catch (Exception e) {
			
			throw new Exception("nextChar buff = "+buff+", pos = "+pos+": "+e.toString());
		}
	}
	
	public void parse(String stBuff, InputStream s) throws Exception
	{
	    	pos = 0;
		is = s;
		buff = stBuff;
		try {
			char ch = nextChar();
			if(ch!='<')
				return;
			if(nextChar()=='?')
			{
				//Read header
				while(nextChar()!='>')
				{

				}
				while(nextChar()!='>')
				{

				}
				nextChar();
			}
			else
				pos--;
			String n = "";
			n += nextChar();
			while((ch = nextChar())!='>')
			{
				n+=ch;
			}
//			System.out.println("name = "+n);
			boolean hasEnd = false;
			if(n.charAt(n.length()-1)=='/')
			{
				hasEnd = true;
				n = n.substring(0, n.length()-1);
			}
			if(n.indexOf(' ')!=-1)
			{
//				System.out.println("has attr");
				String attrs = n.substring(n.indexOf(' '), n.length()).trim()+' ';
				n = n.substring(0, n.indexOf(' '));
				for(; attrs.length()>1;)
				{
					String aName = attrs.substring(0, attrs.indexOf('='));
					attrs = attrs.substring(attrs.indexOf('=')+1, attrs.length());
					char b = attrs.charAt(0);
					String aValue = attrs.substring(1, attrs.indexOf(b, 1));
					attrs = attrs.substring(attrs.indexOf(b, 1)+2, attrs.length());
	//				System.out.println(n+":"+aName+" = "+aValue);
					if (ignoreNS && aName.indexOf(":")>0) {
						aName = aName.substring(aName.indexOf(":")+1);
					}
					attributes.put(aName, aValue);
				}
			}
			name = n;
			if (ignoreNS && n.indexOf(":")>0) {
				name = n.substring(n.indexOf(":")+1);
			}
			if(!hasEnd)
			{
				ch = nextChar();
				if(ch=='<')
				{
					pos--;
					while((ch = nextChar())=='<')
					{
						if(nextChar()=='/')
						{
							while((ch = nextChar())!='>')
							{

							}
							break;
						}
						pos--;
						HttpNode x = new HttpNode();
						x.parse("<"+nextChar(), s);
						childs.addElement(x);
					}
				}
				else
				{
					value = ""+ch;
					while((ch = nextChar())!='<')
						value+=ch;
					while((ch = nextChar())!='>')
					{

					}

				}
			}
		
		} catch (Exception e) {
			
			throw new Exception("Could not read: "+e);
		}
	}

	public String getName() {
		if(name==null)
			return "";
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		if(value==null)
			return "";
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public boolean hasChild(String n)
	{
		for(int i = 0; i<childs.size(); i++)
		{
			HttpNode x = (HttpNode) childs.elementAt(i);
			if(x.getName().equals(n)) //PATCH 2008 thanks..
				return true;
		}
		return false;
	}

	public boolean hasValueOfChild(String n)
	{
		for(int i = 0; i<childs.size(); i++)
		{
			HttpNode x = (HttpNode) childs.elementAt(i);
			if(x.getValue().equals(n))
				return true;
		}
		return false;
	}

	public String childValue(String n)
	{
		for(int i = 0; i<childs.size(); i++)
		{
			HttpNode x = (HttpNode) childs.elementAt(i);
			if(x.getName().equals(n))
				return x.getValue();
		}
		return "";
	}

	public HttpNode child(String n)
	{
		for(int i = 0; i<childs.size(); i++)
		{
			HttpNode x = (HttpNode) childs.elementAt(i);
			if(x.getName().equals(n))
				return x;
		}
		return new HttpNode();
	}

	public String getAttr(String a)
	{
		String val = (String) attributes.get(a);
		if(val==null)
			return "";
		return val;
	}
	public String toString(int level)
	{
		String pr = "";
		for(int i=0; i<level; i++)
			pr+="\t";
		String res = getName();
		for(Enumeration e = attributes.keys(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			res += (" "+name+"="+attributes.get(name));
		}
		res+=" == "+getValue();
		for(int i = 0; i<childs.size(); i++)
		{
			res += ("\n"+pr+"\t"+((HttpNode) childs.elementAt(i)).toString(level+1));
		}
		return pr+res;
	}
	public String toString()
	{
		return toString(0);
	}
	/**
	 * @return Returns the childs.
	 */
	public Vector getChilds() {
		return childs;
	}
	/**
	 * @param childs The childs to set.
	 */
	public void setChilds(Vector childs) {
		this.childs = childs;
	}

}