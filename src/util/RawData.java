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


import java.io.*;
import javax.microedition.io.*;

public class RawData {
    
   // ......... fields ..................
    byte[] data = null;
    int offset  = 0;
    int length  = 0;
    
   // ........ constructors .............
    public RawData() {
    }
    
    public RawData(byte[] _data, int _offset, int _length) {
        data = _data;
        offset = _offset;
        length = _length;
    }
    
    /**
     * Constructs a RawData from a HTTP connection.
     */
    public static void getWithHttp(RawDataListener _listener, String _url) {
        HttpGetThread thread = new HttpGetThread(_listener, _url);
        thread.start();
    }
    
   // ........ field accessors ..........
    public byte[] getData() {
        return data;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public int getLength() {
        return length;
    }
    
}


class HttpGetThread extends Thread {

    RawDataListener listener;
    String url;
    
    public HttpGetThread(RawDataListener _listener, String _url) {
        listener = _listener;
        url = _url;
    }

    public void run() {
        RawData res = new RawData();
        HttpConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpConnection)Connector.open(url);
            
            // Getting the InputStream will open the connection
            // and read the HTTP headers. They are stored until
            // requested.
            is = connection.openInputStream();
            
            // Get the ContentType
            String type = connection.getType();
            
            // Get the length and process the data
            int len = (int)connection.getLength();
            if (len > 0) {
                res.data = new byte[len];
                res.offset = 0;
                res.length = is.read(res.data);
            } else {
                res = null;  // cannot process the data
                //int ch;
                //while ((ch = is.read()) != -1) {
                // ***
                //}
            }
        } catch (IOException e) {
            res = null;
        } finally {
            listener.notifyRawData(res);
            //listener.notify();
            try {
                if (is != null)
                    is.close();
                if (connection != null)
                    connection.close();
            } catch (IOException e) {
                System.err.println("ERROR when closing the HTTP connection");
            }
        }
    }
}

