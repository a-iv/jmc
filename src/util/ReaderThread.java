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
 Reader Thread
 Reads bytes from an inputstream
 Notify a ReaderThreadListener for every byte read.

 @author Gregoire Athanase
 */

import java.io.*;

public final class ReaderThread extends Thread {
  private ReaderThreadListener listener;
  private InputStream is;

  public ReaderThread(ReaderThreadListener _listener,
                      InputStream _is) {
    listener = _listener;
    is = _is;
  }

  public void run() {
    try {
      int code;
      while ( (code = is.read()) != -1) {
        listener.read(this, code);
      }
    }
    catch (IOException e) {
      System.err.println("READ ERROR " + e);
    }
  }
}
