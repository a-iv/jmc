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


package threads;

import java.io.*;
import util.ExceptionListener;

/**
 *Writer Thread
 *Writes bytes to an outputstream
 *
 *@author Gregoire Athanase, modified by Gabriele Bianchi
 */
public final class WriterThread extends Thread implements IWriterThread{
    
    private OutputStream writer;
    private ExceptionListener exListener;
    private StringBuffer buffer;   // output buffer
    private boolean exit;

    
    public WriterThread(OutputStream _writer, ExceptionListener _exListener) {
        writer = _writer;
        exListener = _exListener;
        buffer = new StringBuffer();
        
    }
    
    /**
     * Sends a string in the output stream.
     * @param _s String to send to the output stream
     */
    public synchronized void write(String _s) {
        // the synchronization blocks the append while the send() method is executed
        buffer.append(_s);
        notify();  // wakes up the thread, we've something to send
    }
    
    /**
     * Tells the thread to terminate as soon as possible. 
     */
    public synchronized void terminate() {
        // the synchronization makes it possible to wake up the thread 
        exit = true;
        notify(); // wakes up the thread.
    }
    
    /**
     * Waits for data and sends it. Waits for exit too.
     * if there is a problem, should we note that we sent some chars? ***
     * the "writer.write()" could be nonsynchronized
     */
    private synchronized void execute() throws IOException {
        exit = false;
        int alive = 0;
        while (!exit) {
            
            // the synchronization blocks the append while this method is executed
            if (buffer.length() == 0) {
                
                try {               
                    if (alive > 180) {//3 min of inactivity
                    	writer.write(unicodeToServer(" "));
                    	writer.flush();
                    	alive = 0;
                    	continue;
                    }  	
                    wait(1000);  // waits for something to send. timeout avoids possible? deadlocks
                    alive++;
                } catch (InterruptedException e) {
                    // when does it happen?
                    System.out.println("[INTERRUPTED EXCEPTION]");
                }
                
            } else {
                String s = buffer.toString();
                writer.write(unicodeToServer(s));
                
		System.out.println(">> WRITE: " + s);
                
                writer.flush();
                alive = 0;
                buffer = new StringBuffer(1); // empty buffer
            }
            
        }
    }
    /**
     * Run Thread
     */
    public void run() {
        try {
            execute(); // if empty buffer: waits, else sends it
        }
        catch (IOException e) {
            exListener.reportException(this, e);
        }
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
