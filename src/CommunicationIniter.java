/**
 * The CommunicationIniter inits the input/output streams
 * from/to the server.<br>
 * Use: launch the thread, it will call the <var>connect</var> method
 * passing the input/output streams as parameters.<br>
 * It uses 2 tcp connections: one for input, one for output,
 * because of serious flaws in mobile phones tcp sockets.
 * The input connection (from the server) is on port 5131.
 * The output connection (to the server) is on port 5132.
 * A special proxy is needed to bind the client to the server.<br>
 *
 * Proxy use:<br>
 * <li>1. connect the input and output streams</li>
 * <li>2. copy the raw 32 first bytes from the input to the output.</li>
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

import javax.microedition.io.*;
import java.io.*;

public class CommunicationIniter
    extends Thread {
  CommunicationManager cm;

  public CommunicationIniter(CommunicationManager _cm) {
    cm = _cm;
  }

  public void run() {
    StreamConnection inConn; // input stream connection
    StreamConnection outConn; // output stream connection
    
    InputStream is; // from the server
    OutputStream os; // to the server
    
    try {
      
      inConn = (StreamConnection) Connector.open("socket://w9f05952:5132",
                                                 Connector.READ);
      is = inConn.openInputStream();

      outConn = (StreamConnection) Connector.open("socket://w9f05952:5131",
                                                  Connector.WRITE);
      os = outConn.openOutputStream();

      // proxy init
      // should add client version
      byte code[] = new byte[32];
      for (int i = 0; i < 32; i++) {
        code[i] = (byte) is.read();
      }
      
      //System.out.print(code);
      os.write(code);
      os.flush();
      
      // sends the result to the communication manager
      cm.notifyConnect(inConn, outConn, is, os);
    }
    catch (java.io.IOException e) {
      cm.reportException(this, e);
    }
  }
}
