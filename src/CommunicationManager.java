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

import util.*;
import jabber.conversation.*;
import jabber.roster.*;
import xmlstreamparser.*;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import org.bouncycastle.crypto.digests.SHA1Digest;
import java.io.*;
import java.util.*;


public class CommunicationManager 
implements ParserListener, ExceptionListener/*, ConnectionListener */ {
   // fields
    MyMidlet midlet;

    // connections & streams: save them for closing in "disconnect"
    Connection inConn;  // Server to ReaderThread
    InputStream is;
    Connection outConn; // WriterThread to the server
    OutputStream os;
    
    // save the threads, for thread exception handling ??
    CommunicationIniter cinit; // init the connections with proxy
    CharacterReaderThread readerThread;
    WriterThread writerThread;

    StanzaReader stanzaReader; // send him the stanzas
    
   // constructor
    public CommunicationManager(MyMidlet _midlet) {
        midlet = _midlet;
    }
    
    public void connect() {
        // *** should ensure it is disconnected...
        cinit = new CommunicationIniter(this);
        cinit.start();
    }
    
   // hooks methods
    public void notifyConnect(Connection _inConn, Connection _outConn, InputStream _is, OutputStream _os) {
        inConn = _inConn;
        outConn = _outConn;
        is = _is;
        os = _os;
        try {
            Object inputLock = new Object();
            Object outputLock = inputLock;
            
            // launches the reader thread
            InputStreamReader reader = new LockedInputStreamReader(is, inputLock);
            Parser parser = new Parser(this, this);
            readerThread = new CharacterReaderThread(reader, parser, this);
            //System.out.println("parser created");
            
            // launches the writer thread
            OutputStreamWriter writer = new LockedOutputStreamWriter(os, outputLock);
            writerThread = new WriterThread(writer, this);
            writerThread.start();
            Datas.writerThread = writerThread; // Datas is already initialized
            //System.out.println("writer launched");
            
            // stream start
            writerThread.write("<?xml version='1.0'?>" +
            "<stream:stream to=" + "'" + Datas.jid.getServername() + "'" +
            " xmlns='jabber:client'" +
            " xmlns:stream='http://etherx.jabber.org/streams'>");
            
            // launches the reader
            stanzaReader = new StanzaReader(this, midlet, midlet);
            readerThread.start();
            //parser.start();
            System.out.println("reader launched");
        }
        catch (Exception e) {
            System.err.println("Stream Init Error " + e);
        }
    }
    
    public void terminateStream() {
        writerThread.write("</stream:stream>");
        disconnect();
    }
    
    public void disconnect() {
        try {
            // unlock the WriterThread and finish it
            writerThread.terminate();
            
            // closes the streams
            is.close();
            os.close();
            
            // closes the connection(s)
            inConn.close();
            outConn.close();
        } catch (IOException e) {
        }
    }
    
    public void prologEnd(Node _node) {
        System.out.println("<< PROLOG: " + _node.toString());
        
        // I don't care about the prolog, should I?
    }
    
    public void nodeStart(Node _node) {
        /*Alert a = new Alert("Communication", "Node Start", null,
        AlertType.CONFIRMATION);
        Display.getDisplay(midlet).setCurrent(a);*/
        
        if (_node.parent == null) {
            // document entity (stream) starts...
            System.out.println("<< DOCUMENT ENTITY start: " + _node.toString());
            if (_node.name.equals("stream:stream")) {
                Datas.setSessionId(_node.getValue("id"));
                // IGNORED attributes: xmlns, xmlns:stream, from
                
                // reply: query authentication fields
                writerThread.write(
                "<iq id='s1' type='get'><query xmlns='jabber:iq:auth'>" +
                    "<username>" + Util.escapeCDATA(Datas.jid.getUsername()) + "</username>" +
                "</query></iq>");
                
                midlet.connectedEvent(); // *** should appear later?
            }
            else {
                throw new IllegalStateException("<stream:stream> expected");
            }
        }
    }
    
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
        
    }
    
    public void blank() {
        System.out.println("blank");
        
        // send out a keep alive space character. every time???
        writerThread.write(" ");
    }
    
   // Exceptions handling
    public void reportException(Thread _t, Exception _e) {
        System.err.println("Error " + _e);
        /*Alert a = new Alert("Error", _e.toString(), null, AlertType.ERROR);
        Display.getDisplay(midlet).setCurrent(a);*/
    }

    public void reportException(Exception _e) {
        System.err.println("Error " + _e);
    }
    
}

class LockedInputStreamReader extends InputStreamReader {
    public LockedInputStreamReader(InputStream _is, Object _lock) {
        super(_is);
        lock = _lock;
    }
}

class LockedOutputStreamWriter extends OutputStreamWriter {
    public LockedOutputStreamWriter(OutputStream _os, Object _lock) {
        super(_os);
        lock = _lock;
    }
}

