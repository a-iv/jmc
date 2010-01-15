/*
  Copyright (c) 2000, Al Sutton (al@alsutton.com)
  All rights reserved.
  Redistribution and use in source and binary forms, with or without modification, are permitted
  provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions
  and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of
  conditions and the following disclaimer in the documentation and/or other materials provided with
  the distribution.

  Neither the name of Al Sutton nor the names of its contributors may be used to endorse or promote
  products derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/



/**
 * Title:        meConnector.java
 * Description:  Class for getting a connection to the server for j2me
 */
package jmc.connection;
import javax.microedition.io.*;
import java.io.*;
import util.Datas;

public class meConnector extends Thread{
  /**
   * The connection to the jabber server
   */
  private StreamConnection connection = null;
  private CommunicationManager _cm = null;
  private int _port;
	private String _hostname;
  /**
   * Constructor
   * @author Gabriele Bianchi 
   * Modified  04/01/2006
   * @param hostname The host to connect to
   * @param port The port to connect to
   */

  public meConnector( String hostname, int port, CommunicationManager cm ) 
  {
    _hostname = hostname;
    _cm = cm;
    _port = port;
  }
  
  /**
   * Modified by Gabriele Bianchi 04/01/2006
   */
  public void run() {
    StringBuffer connectorStringBuffer;
    if (Datas.isSSL)
	connectorStringBuffer = new StringBuffer( "ssl://");
    else
    	connectorStringBuffer = new StringBuffer( "socket://");
    connectorStringBuffer.append( _hostname );
    connectorStringBuffer.append( ":" );
    connectorStringBuffer.append( _port );
    connectorStringBuffer.append( "" );

    String connectorString = connectorStringBuffer.toString().trim();
    System.out.println(connectorString);
   try {
    if (Datas.isSSL)
	connection = (SecureConnection) Connector.open( connectorString );
    else
    	connection = (StreamConnection) Connector.open( connectorString );
    _cm.notifyConnect(connection, this.openInputStream(), this.openOutputStream());
	}catch (Exception e){e.printStackTrace();
         System.out.println("Connessione non riuscita:"+ e.getMessage());
		 _cm.notifyNoConnectionOn("I can't connect, server is unreachable");
    	}
    
    return;
  }

  /**
   * Method to return the input stream of the connection
   *
   * @return InputStream
   */

  public InputStream openInputStream() throws IOException
  {
    return connection.openInputStream();
  }

  /**
   * Method to return the output stream of the connection
   *
   * @return OutputStream
   */

  public OutputStream openOutputStream()  throws IOException
  {
    return connection.openOutputStream();
  }
  
  /** 
   *Method to return  the connection
   *@return StreamConnection
   */
  
 
  public StreamConnection getConnection () {
    return connection;
  }
}
