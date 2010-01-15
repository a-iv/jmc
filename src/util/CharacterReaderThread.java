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
 Character Reader Thread
 Reads characters from an inputstreamreader
 Notify a CharacterReaderThreadListener for every character.
 The reading is not buffered, because it would block the stream.

 @author Gregoire Athanase
 */

import java.io.*;

public final class CharacterReaderThread extends Thread {
  private ReaderThreadListener listener;
  private ExceptionListener exListener;
  private InputStreamReader reader;

  public CharacterReaderThread(InputStreamReader _reader,
                               ReaderThreadListener _listener,
                               ExceptionListener _exListener) {
    listener = _listener;
    exListener = _exListener;
    reader = _reader;
  }

  public void run() {
    try {
        int code;
        while ((code = reader.read()) != -1) {
            listener.read(this, code);
        }
    }
    catch (IOException e) {
        exListener.reportException(this, e);
    }
  }
}
