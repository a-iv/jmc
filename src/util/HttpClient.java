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
package util;

import javax.microedition.io.*;
import java.io.IOException;
import java.io.InputStream;


public class HttpClient
{
	/**
	 * Send params with a get request
	 * @param url
	 */
	public static String getViaHttpConnection(String url) throws IOException {
         HttpConnection c = null;
         InputStream is = null;
         int rc;
		 String result = null;
         try {
             c = (HttpConnection)Connector.open(url);

             
             rc = c.getResponseCode();
             if (rc != HttpConnection.HTTP_OK) {
                 throw new IOException("HTTP response code: " + rc);
             }

             is = c.openInputStream();
			 result = new String();
           
             int ch;
             while ((ch = is.read()) != -1) {
				 result += (char)ch;
             }
             
         } catch (ClassCastException e) {
             throw new IllegalArgumentException("Not an HTTP URL");
         } finally {
             if (is != null)
                 is.close();
             if (c != null)
                 c.close();
         }
		 return Util.unescapeCDATA(result);
     }

	 /**
	  * Send params with a post request
	  * @param url, data, request
	 
	 public static String postViaHttpConnection(String url, String data, Hashtable request) throws IOException
	{
		HttpConnection c = null;
		InputStream is = null;
		OutputStream os = null;
		int rc;
		String result = new String();
		try
		{
			c = (HttpConnection)Connector.open(url);

			// Set the request method and headers
			c.setRequestMethod(HttpConnection.POST);

			if (request != null && request.size() > 0)
			{
				if (request.containsKey("Content-Type"))
					c.setRequestProperty("Content-Type", (String)request.get("Content-Type"));
				
					
			}
			//data = Util.escapeCDATA(data);

			c.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
			
			// Getting the output stream may flush the headers
			os = c.openOutputStream();
			
			os.write(data.getBytes());
			System.out.println(">> " + data + " len:" + Integer.toString(data.getBytes().length));
			os.flush();         
			rc = c.getResponseCode();
			if (rc != HttpConnection.HTTP_OK)
			{
				throw new IOException("HTTP response code: " + rc);
			}

			is = c.openInputStream();

			// Get the ContentType
			String type = c.getType();
			
			int ch;
			while ((ch = is.read()) != -1)
				{
					result += (char)ch;
				}
			
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException("Not an HTTP URL");
		}
		finally
		{
			if (is != null)
				is.close();
			if (os != null)
				os.close();
			if (c != null)
				c.close();
		}
		return Util.unescapeCDATA(result);
	} */

 }