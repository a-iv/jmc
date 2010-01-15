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
package threads;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Date;

import jabber.presence.*;
import xmlstreamparser.*;
import util.Datas;
import jmc.StanzaReader;
import jmc.MidletEventListener;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.twmacinta.util.MD5;

/**
 * Class for transmitting stanzas over http-binding (JEP 124)
 * @author G.Bianchi
 * 
 */
public class HttpBindThread extends Thread implements IWriterThread{

	public static int DEFAULT_WAIT = 30;

	private String httpburl; // url of the HTTB Binding gateway

	private int wait = DEFAULT_WAIT; // max seconds to keep the connection

	private int polling = 5; // max seconds for polling
				
	protected boolean statusSet = false;
	private HttpConnection[] conn = new HttpConnection[2]; //allow exactly 2 connections

	private int defaultConn = 0;

	private long rid = -1; // request id

	private String sid = null; // session id

	private Thread secondThread = null;
	
	private boolean ended = false; // Network error flag

	private boolean busy = false; // Indicates if someone reads packet

	private boolean terminated = false; // Indicates if someone closed

	private StanzaReader stanzaReader;

	private MidletEventListener eventsListener;
	
	private String buffer = "";	
	
	/**
	 * Constructor
	 */
	public HttpBindThread(StanzaReader _stanzaReader, MidletEventListener events) {
		stanzaReader = _stanzaReader;
		eventsListener = events;
		start();
	}

	/**
	 * Run Method of the thread
	 *
	 * 
	 */
	public void run() {
		
		
		String user = Datas.jid.getLittleJid();
		
		String domain = Datas.hostname;
		
		String addr = Datas.server_name + ":"
				+ Datas.port;

		httpburl = "http://"+addr+"/http-bind/";
		
		System.out.println("Connecting..."+httpburl);
		
		try {
			/* Starts session with jabber server */
			initSession(addr, domain, user, Datas.getPassword(),
					"JabberMix", Presence.getPresence(1));
					
		} catch (Exception e) {
			// If any exception throws - terminate connection
			System.out.println(e.getMessage());
			
			ended = true;
		}
		if (ended) {
			
			terminate();
			return;
		}
		System.out.println("Successfully connected with "
				+ addr);

		// Calculates string representation of initial user status
	

		if (!ended) {
			stanzaReader.setRosterState();
			//Requests roster items 
			writeWithBody("<iq id=\"s3\" type=\"get\">"+ "<query xmlns=\"jabber:iq:roster\"/></iq>");
			readStanzas(0);

		//	writeWithBody("<iq type=\"get\" from=\""+Datas.jid.getFullJid()+"\" to=\""+Datas.hostname+"\" id=\"discoitem1\"><query xmlns=\"http://jabber.org/protocol/disco#items\"/></iq>");
		//	readStanzas(0);

		
		}

	
		defaultConn = 1;
		statusSet = false;
             
                Date last = null;
		while (!ended) {
			// Main loop
			try {
				if (!terminated && !busy) {
					
					Date now = new Date();

					
					if (last != null && (now.getTime() - last.getTime() < (polling*1000))) {
						sleep(((polling * 1000) - (now.getTime() - last.getTime())));
						
					}
					
					
					last = new Date();
					System.out.println("sending [" + last.toString() + "]: "+buffer);
				
					writeWithBody(buffer, 0);
					buffer = "";
					
					readStanzas(0);
					System.out.println("receiving  [" + new Date().toString() + "]");
					

				}
			} catch (Exception e) {
				ended = true;
				
			}
		}
	//	System.out.println("Run: loop ended");

	}



	/**
	 * Read stanzas
	 * @param connIdx
	 */
	private synchronized void readStanzas(int connIdx) {
		HttpNode n = readResponse(connIdx);
		if (n == null)
			return;
		System.out.println("readStanzas: " + n.getChilds());
		
		int s = n.getChilds().size();
		for (int i=0; i<s; i++) {
			
			
			stanzaReader.read(new Node((HttpNode) n.getChilds().elementAt(i)));
		}
	}



	/**
	 * reads the next stanza from the given connection
	 * @param httpconn
	 * @return HttpNode 
	 */
	private HttpNode readStanza(int connIdx) {
		HttpNode n = readResponse(connIdx);
		int s = n.getChilds().size();
		if (s > 1) {
			
			return (HttpNode) n.getChilds().firstElement();
		} else if (s < 1) {
			//discard empty stanza
			return new HttpNode();
		}
		return n;
	}

	/**
	 * reads the next non-empty stanza (blocking)
	 *
	 * @return HttpNode
	 */
	protected HttpNode readStanza() {
		//use default connection
		return readStanza(defaultConn);
	}

	/**
	 * reads the returned response with the body element
	 *
	 * @return HttpNode
	 */
	private HttpNode readResponse(int connIdx) {
		busy = true;
	
		HttpNode x = new HttpNode();
		if (ended) {
		
			return null;
		}
		do {
			if (!ended) {
				InputStream is = null;
				try {
					HttpConnection httpconn = conn[connIdx];
					int rc = ((HttpConnection)httpconn).getResponseCode();
					if (rc == 404)
						return null;
				      
					if (rc != 200)
					{
						is = httpconn.openInputStream();
						int code;
						while ((code = is.read()) != -1) 
							System.out.print((char)code);
						throw new Exception("Exception response code: " + rc);
					}
					is = httpconn.openInputStream();
        					
					x.parse("", is);
		
				} catch (Exception e) {
			
					System.out.println("Exception reading response: " + e.getMessage());
					ended = true;
				} finally {
					try {
						if (is != null)
							is.close();
					} catch (IOException e) {
						
					}
				}
				busy = false;
			}
		} while (x.getName().equals("") && !ended);
	

		return x;
	}

	/**
	 * Initialize the session
	 *
	 *
	 */
	protected void initSession(String addr, String domain, String user,
			String pass, String resource, String Status) throws Exception {
		try {
			
			System.out.println("Opening first stream");
		
			generateRequestId(); // create rid

			writeStream("<body content=\"text/xml; charset=utf-8\" to=\""
					+ domain
					+ "\" hold=\"1\" wait=\""
					+ wait
					+ "\" rid=\""
					+ rid
					+ "\" "
					+ "xml:lang=\"en\" "
					+ ""
					+ "route=\"xmpp:" + addr + "\" " 
					+ "xmlns=\"http://jabber.org/protocol/httpbind\" " 		
					+"/>", 0);

			HttpNode x = readResponse(0);
			
			if (!x.getName().equals("body"))
				eventsListener.unauthorizedEvent("Body element missing!");
			sid = x.getAttr("sid");
			System.out.println("Session creation response received: "+x.toString(0));
			if (sid == null || sid.length() == 0) {
				eventsListener.unauthorizedEvent("Session ID not given!");
				throw new Exception("Session ID not given!");
			}
			if (x.getAttr("requests") == null || x.getAttr("requests").equals("1")) {
				eventsListener.unauthorizedEvent("Server supports only polling behaviour!");
				throw new Exception("Server supports only polling behaviour!");
			}
			
			int tmpW = Integer.parseInt(x.getAttr("wait"));
			if (tmpW < wait)
				wait = tmpW;
				
			
			if (x.getAttr("polling") != null) {
				polling =  Integer.parseInt(x.getAttr("polling"));
				polling++;
			}
			
			if (x.getAttr("inactivity").length()>0) {
			 int inact=Integer.parseInt(x.getAttr("inactivity"));
			 if (inact < wait)
			 wait = inact;
		   }

			System.out.println("Authenticating: "+x.toString());
			
			x = x.child("features");
			Authenticate(x, user, pass, domain);
			
			writeWithBody("<iq type=\"set\" id=\"bind_1\">"
					+ "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">"
					+ "<resource>" + resource + "</resource></bind></iq>");
			System.out.println("Binding resource");
			x = readStanza();
			if (x.getAttr("type").equals("error")) {
				eventsListener.unauthorizedEvent("Error binding resource");
				throw new Exception("Error binding resource");
			}
			writeWithBody("<iq to=\""
					+ domain
					+ "\" type=\"set\" id=\"sess_1\">"
					+ "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>");
			System.out.println("Opening session");
			x = readStanza();
			if (x.getAttr("type").equals("error")) {
				eventsListener.unauthorizedEvent("Error opening session");
				throw new Exception("Error opening session");
			}
			System.out.println("Session Open!");
		} catch (Exception e) {
			
			eventsListener.unauthorizedEvent(e.getMessage());
			throw new Exception(e.getMessage());
		}
	}


	
    /**
     * Tells the thread to terminate as soon as possible. 
     */
	public synchronized void terminate()
	{

		
		wait = DEFAULT_WAIT;
		rid = -1;
		sid = null;
		ended = true;
		terminated = true;
		defaultConn = 0;

		//close open connections
		for (int i = 0; i < conn.length; i++)
		{
			try
			{
				if (conn[i] != null)
					conn[i].close();
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		eventsListener.disconnectedEvent();

		System.out.println("Terminated!");
    }
	
	/**
	 * Implements the interface method
	 * @param _s
	 */
	public synchronized void write(final String _s)
	{
		System.out.println("Writing");
		
		while (secondThread != null && secondThread.isAlive())
		{
			try
			{
				sleep(200);
			}
			catch (InterruptedException e)
			{
			}
		
		}
		secondThread = new Thread()
		{
			public void run()
			{
				writeWithBody(_s, 1);
				readStanzas(defaultConn);
			}
		};
		secondThread.start();
	}

	/**
	 * writes the message to the outputsream of the given connection
	 * @param mess
	 * @param conn
	 */
	private void writeWithBody(String mess, int connIdx)  {
		writeStream("<body rid=\"" + (++rid) + "\" sid=\"" + sid + "\" "
				+ "xmlns=\"http://jabber.org/protocol/httpbind\">" + mess
				+ "</body>", connIdx);
	}

	/*
	 * Add the body tag to the message
	 *
	 * 
	 */
	protected void writeWithBody(final String mess) {
		//default connection to use when called from outside
		if (defaultConn == 1) {
			
			while (secondThread != null && secondThread.isAlive()) {
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
				
			}
			secondThread = new Thread() {
				public void run() {
					writeWithBody(mess, defaultConn);
					readStanzas(defaultConn);
				}
			};
			secondThread.start();
		} else {
			writeWithBody(mess, defaultConn);
		}
	}

	/**
	 * sends the message, but does not autonmatically include the enclosing body
	 * element.
	 *
	 * @param mess
	 */
	private void writeStream(String mess, int connIdx) {
		if (ended) {
			terminate();
			return;
		}
		OutputStream out = null;
		try {
			byte[] bout = unicodeToServer(mess);
			HttpConnection httpconn = (HttpConnection) Connector.open(httpburl);
			conn[connIdx] = httpconn;
			
			if (!httpburl.startsWith("https://")) {
				
				httpconn.setRequestProperty("User-Agent",
						"Profile/MIDP-2.0 Configuration/CLDC-1.1");
				
			}
			httpconn.setRequestMethod("POST");
			httpconn.setRequestProperty("Content-Length", ""
					+ bout.length);
			out = httpconn.openOutputStream();
			if (out != null) {
				out.write(bout);
				
			}
			System.out.println("writtenToAir ["+connIdx+"]: " + mess);
		} catch (Exception e) {
			System.out.println("Exception found: " + e.getMessage());
			ended = true;
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				
			}
		}
	}

	/**
	 * generates a random rid with max. 10 digits. 
	 * (9007199254740991 limit)
	 *
	 * @return
	 */
	private long generateRequestId() {
		String strRid = "";
		Random r = new Random();
		for (int i = 0; i < 10; i++)
			strRid += "" + r.nextInt(10);
		rid = Long.parseLong(strRid);
	
		return rid;
	}
	
	/**
	 * authenticates the user with the most appropriate mechanism
	 * @param x	features
	 * @param user
	 * @param pass
	 * @param domain
	 * @throws Exception
	 */
	protected void Authenticate(HttpNode x, String user, String pass, String domain) throws Exception {



		if (x.child("mechanisms").hasValueOfChild("PLAIN")) {
				// PLAIN authorization 
				System.out.println("Using plain authorization");
				String resp = "\0" + user + "\0" + pass;
				writeWithBody("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">"
						+ MD5.toBase64(resp.getBytes()) + "</auth>");
				System.out.println("Starting PLAIN authorization");
				x = readStanza();
				if (x.getName().equals("failure"))
					throw new Exception("PLAIN authorization error");
		}
		else throw new Exception("Only PLAIN authorization supported");
	}
	
   /**
    * Unicode Support
    * © 2003, 2004 Vidar Holen
    * www.vidarholen.net
    *
    */
    public byte[] unicodeToServer(String s) {
        byte[] b=new byte[strlen(s)];
        char[] a=s.toCharArray();
        int j=0;
        for(int i=0; i<a.length; i++) {
            if(a[i]<0x80) {
                b[j]=(byte)(a[i]);
                j+=1;
            } else if(a[i]<0x800) {
                b[j]=(byte)(0xC0 | (a[i]>>6));
                b[j+1]=(byte)(0x80 | (a[i]&0x3F));
                j+=2;
            } else {
                b[j]=(byte)(0xE0 | (a[i]>>12));
                b[j+1]=(byte)(0x80 | ((a[i]>>6)&0x3F));
                b[j+2]=(byte)(0x80 | (a[i]&0x3F));
                j+=3;
            }
        }

        return b;
    }
    
    /**
     * Find length in bytes of a string, akin to strlen vs wcslen
     *
     * © 2003, 2004 Vidar Holen
 		 * www.vidarholen.net
     */
    public static int strlen(String s) {
        int n=0;
        char[] a=s.toCharArray();
        for(int i=0; i<a.length; i++) {
            if(a[i]<0x80) n++;
            else if(a[i]<0x800) n+=2;
            else n+=3;
        }
        return n;
    }

	

}