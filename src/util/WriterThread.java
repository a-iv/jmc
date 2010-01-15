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

/**
Writer Thread
Writes bytes to an outputstream

@author Gregoire Athanase
*/

import java.io.*;

public final class WriterThread extends Thread {
    private OutputStreamWriter writer;
    private ExceptionListener exListener;
    private StringBuffer buffer;   // output buffer
    private boolean exit;
    
    public WriterThread(OutputStreamWriter _writer, ExceptionListener _exListener) {
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
    * Tells the thread to terminate as soon as possible. NEEDED???
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
        while (!exit) {
            
            // the synchronization blocks the append while this method is executed
            if (buffer.length() == 0) {
                //System.out.println("[...]");
                try {
                    wait(1000);  // waits for something to send. timeout avoids possible? deadlocks
                } catch (InterruptedException e) {
                    // when does it happen?
                    System.out.println("[INTERRUPTED EXCEPTION]");
                }
                
            } else {
                String s = buffer.toString();
                System.out.println(">> WRITE: " + s);
                writer.write(s);
                writer.flush();
                buffer = new StringBuffer(); // empty buffer
            }
            
        }
    }
    
    public void run() {
        try {
            execute(); // if empty buffer: waits, else sends it
        }
        catch (IOException e) {
            exListener.reportException(this, e);
        }
    }
    
}
