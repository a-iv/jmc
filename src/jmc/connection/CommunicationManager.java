/**
 * Class responsible for communication with the Jabber server.
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
package jmc.connection;

import util.*;
import xmlstreamparser.*;
import jmc.*;
import javax.microedition.io.*;
import java.io.*;
import threads.*;

/**
 * Connector Manager class
 * Modified by Gabriele Bianchi 04/01/2006
 */
public class CommunicationManager 
implements ParserListener, ExceptionListener/*, ConnectionListener */ {
	// fields
	MidletEventListener midlet;
	
	// connections & streams: save them for closing in "disconnect"
	StreamConnection inConn;  
	
	InputStream is;

	OutputStream os;

	meConnector cinit; 
	CharacterReaderThread readerThread;
	IWriterThread writerThread;
	//HttpBindThread httpThread;
	StanzaReader stanzaReader; // send him the stanzas
	int type_of_connection;
	// constructor
	public CommunicationManager( MidletEventListener _midlet) {
		midlet = _midlet;
	}
	
	/**
	 * Makes connection
	 * Modified by Gabriele Bianchi 04/01/2006	
	 */
	public void connect(int state) {
		// *** should ensure it is disconnected...
		cinit = new meConnector(Datas.server_name, Datas.port, this);
		type_of_connection = state;
		
		
		cinit.start();
	}
	
	public void httpConnect() {
		//Parser parser = new Parser(this, this);
		stanzaReader = new StanzaReader(this, midlet, type_of_connection);
		writerThread = new HttpBindThread(stanzaReader, midlet);

		Datas.writerThread = writerThread;
	}
	
	/**
	 * Notifies connection to midlet
	 * Modified by Gabriele Bianchi 05/01/2006
	 */
	public void notifyConnect(StreamConnection _inConn, InputStream _is, OutputStream _os) {
		inConn = _inConn;
		//outConn = _inConn;
		is = _is;
		os = _os;
		System.out.println("Connessione effettuata");
		try {
			
			
			Parser parser = new Parser(this, this);
			
			//TODO: discriminare se vuole httpBind!! e creare l'obj httpbindthread
			
			readerThread = new CharacterReaderThread(is, parser, this);
			        
			
			writerThread = new WriterThread(os, this);
			writerThread.start();
			Datas.writerThread = writerThread; // Datas is already initialized

			/*String server;
			if (Datas.subdomain != null)
			{
				server = Datas.subdomain;
				
			}
			else
				server = Datas.server_name;*/
			// stream start
			writerThread.write("<?xml version='1.0'?>" +
					"<stream:stream to=" + "'" + Datas.hostname + "'" +
					" xmlns='jabber:client'" +
			" xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
			
			
			
			stanzaReader = new StanzaReader(this, midlet, type_of_connection);
			readerThread.start();
	
		}
		catch (Exception e) {
			System.err.println("Stream Init Error " + e);
		}
	}
	
	public void terminateStream() {
		writerThread.write("</stream:stream>");
		disconnect();
	}
	/**
	 * Disconnect
	 */
	public void disconnect() {
		try {
			// unlock the WriterThread and finish it
			
			writerThread.terminate();
			
			// closes the streams
			if (is != null)
			{
				//writerThread.write("</stream:stream>");
				is.close();
				os.close();
			}
			
			// closes the connection
			if (inConn != null)
				inConn.close();
	
		} catch (IOException e) {
		}
	}
	
	public void prologEnd(Node _node) {
		System.out.println("<< PROLOG: " + _node.toString());
		
		// I don't care about the prolog, should I?
	}
	/**
	 * @param Node
	 */
	public void nodeStart(Node _node) {
		
		
		if (_node.parent == null) {
			// document entity (stream) starts...
			System.out.println("<< DOCUMENT ENTITY start: " + _node.toString());
			if (_node.name.equals("stream:stream")) {
				Datas.setSessionId(_node.getValue("id"));
				// IGNORED attributes: xmlns, xmlns:stream, from
				if (_node.getValue("version") == null) {
					//NO SASL
					writerThread.write(
							"<iq id='s1' type='get'><query xmlns='jabber:iq:auth'>" +
							"<username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username>" +
						"</query></iq>");
					
				}
				
				
			
				/*	
				 */
				/* writerThread.write("<iq id='s1' to='"+Datas.hostname+"'"
					+ " type=\"set\" id=\"sess_1\">"
					+ "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>");
				*/
				 // *** should appear later?
				 //midlet.connectedEvent();
			}
			else {
				disconnect();
				midlet.disconnectedEvent();
				//throw new IllegalStateException("<stream:stream> expected");
			}
		}
	}
	/**
	 * @param Node
	 */
	public void nodeEnd(Node _node) {
		if (_node.parent == null) {
			// end of stream
			System.out.println("<< DOCUMENT ENTITY end: " + _node.toString());
			if (_node.name.equals("stream:stream")) {
				disconnect();
				midlet.disconnectedEvent();
			}
			else {
				throw new IllegalStateException();
			}
		}
		else if (_node.parent.parent == null) {
			// it's a stanza
			System.out.println("<< stanza: " + _node.toString());
			
			stanzaReader.read(_node);
		}
		else if (_node.parent.parent.parent == null) {
			// it's a stanza
			System.out.println("<< long stanza: " + _node.toString());
			
			stanzaReader.read(_node);
		}
		else if (_node.name != null && (_node.name.equals("message") || _node.name.equals("presence") || _node.name.equals("iq"))) {
			System.out.println("<< stanza error: " + _node.toString());
			try {
				//if (_node.name.equals("message"))
				stanzaReader.read(_node);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}else if(_node.name.equals("stream:features") && stanzaReader.internalstate == 1){
			try {
				System.out.println("<< stream:features: " + _node.toString());
				stanzaReader.read(_node);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
		}
		//else
		//	System.out.println("<< generic stanza error: " + _node.toString());
		
	}
	/**
	 * Sends out a keep alive space character.
	 */
	public void blank() {
		System.out.println("blank");
		
		// every time???
		writerThread.write(" ");
	}
	
	
	/**
	 * Exceptions handling
	 * @param Exception
	 */
	public void reportException(Thread _t, Exception _e) {
		System.err.println("Error " + _e);
		/*Alert a = new Alert("Error", _e.toString(), null, AlertType.ERROR);
		 Display.getDisplay(midlet).setCurrent(a);*/
	}
	/**
	 * Exceptions handling
 	 * Modified by Gabriele Bianchi 05/01/2006
	 * @param Exception
 	 */
	public void reportException(Exception _e) {
		System.err.println("Error " + _e);
		terminateStream();
		midlet.unauthorizedEvent("Error: " + _e.getMessage());
	}
	/**
	 * @param Exception
 	 * @author Gabriele Bianchi 
 	 */
	public void reportRegistrationError(Exception _e, boolean disconnect) {
		System.err.println("Error " + _e);
		if (disconnect)
			terminateStream();
		midlet.registrationFailure(_e, disconnect);
		
	}
	public void notifyNoConnectionOn(String reason){
		midlet.unauthorizedEvent(reason);
	}
}
