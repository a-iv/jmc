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

import jabber.presence.Presence;
import jabber.roster.Jid;
import java.util.Vector;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import javax.microedition.rms.InvalidRecordIDException;
import threads.IWriterThread;

/**
 * Storage class 
 * Modified by Gabriele Bianchi 04/01/2006
 */
public class Datas {

   // stream connection parameters
   
    public static Jid jid;
	  public static String hostname;
    public static String server_name;
	  public static String subdomain = null;
    public static int port;
    public static boolean isSSL = false;
    public static boolean isHTTP = false;
    public static boolean noData = true;
    public static String avatarFile = null;
    private static String password; 
    private static String sessionId;
	
    
   // other fields
    public static Vector conversations;
    public static Vector rooms;
    public static Vector server_services;
    public static Vector gateways;
    public static Hashtable multichat;
    public static Contents images;
    public static Hashtable roster;  // rosters, key= jid without resource
    public static Vector roster_vector;
   // public static RosterClassification rosterClasses; 
    public static IWriterThread writerThread;
    public static boolean readRoster = false;

	public static int HTTP_PORT = 80;
	public static int TCP_PORT = 5222;
	public static int SSL_PORT = 5223;
	public static String customPort = TCP_PORT+"";

   /**
    * initialization ..........
    */
    public static void load() {
        // init the variables from RMS...
	conversations = new Vector(1, 1);
        roster = new Hashtable(2);
	multichat = new Hashtable(1);
	server_services = new Vector(1,1);
	Datas.gateways = new Vector(3, 1);
	images = new Contents();
	port = TCP_PORT;
	boolean default_flag = false;
	RecordStore rs = null;
	try {
		rs = RecordStore.openRecordStore("MyOptions",true);
	}catch (RecordStoreException ex) {
		jid = new Jid("username@localhost/JabberMix");
         	password = "password";
	 	server_name = "localhost";
	 	
		
		return;
		
	}
	
	//get record in order
	byte b[] = null;
	byte b1[] = null;
	byte b2[] = null;
	byte b3[] = null;
	byte b4[] = null;
	byte b5[] = null;
	byte b6[] = null;
	byte b7[] = null;
	
	try {
		b = rs.getRecord(1);
		b1 = rs.getRecord(2);
		b2 = rs.getRecord(3);
		b3 = rs.getRecord(4);
		b4 = rs.getRecord(5);
		b5 = rs.getRecord(6);
		b6 = rs.getRecord(7);
		b7 = rs.getRecord(8);
	}catch (RecordStoreNotOpenException e) {
		default_flag = true;
	}catch (InvalidRecordIDException e1) {
		default_flag = true;
	}catch (RecordStoreException e2) {
		default_flag = true;
	}
	if (default_flag) {//default options
		jid = new Jid("username@localhost/JabberMix");
         	password = "password";
	 	server_name = "localhost";
	 	
		
	}
	else { //the records exist
		jid = new Jid(new String(b, 0, b.length));
		
		password = new String(b1, 0, b1.length);
	
		server_name = new String(b2, 0, b2.length);
		
		String ssl = new String(b3, 0, b3.length);
		
		String mail = new String(b4, 0, b4.length);

		subdomain = new String(b5, 0, b5.length);

		if (subdomain.equals("null"))
		{

			subdomain = null;
			hostname = server_name;
		}
		else
			hostname = subdomain;
		if (ssl.toLowerCase().equals("yes")) {
			isSSL = true;
			port = SSL_PORT; ;
		}
		else if (ssl.toLowerCase().equals("http")) {
			isHTTP = true;
			port = HTTP_PORT;
		}
		else {
			isSSL = false; 
			port = TCP_PORT;
		}
		
		if (mail.equals("null"))
		{
			jid.setMail("");
			
		}
		else
			jid.setMail(mail);
		
		noData = false; //data set 
			
		//Set AVATAR
		
		avatarFile = new String(b6, 0, b6.length);
		if (avatarFile != null && avatarFile.equals("null"))
			avatarFile = null;
		setJidAvatar();
		
		customPort = new String(b7, 0, b7.length);
		if (customPort != null && !customPort.equals(""))
			port = Integer.parseInt(customPort);
		
	}
        try{
		rs.closeRecordStore();
	}catch (RecordStoreException exc1){
		System.out.println(exc1.getMessage());
	}


    }
    
    /**
     * set jid AVATAR
     * 
     */
    public static void setJidAvatar() {
    	try {
		InputStream in = null;
		Object o = new Object();
		if (avatarFile == null)
			return;
		in = o.getClass().getResourceAsStream(avatarFile);
		
		if (in.available() > 8000) {
			System.out.println("Error: avatar too big");
			//TODO lanciare alert all'utente
		}else{
			
			byte[] data = new byte[in.available()]; 
			in.read(data);
			jid.setAvatar(data);
		}
	}catch(Exception e) {System.out.println("cannot load avatar");}
    }
   
    /**
     * Computes the password in conformance with JEP-0078
     * @return String (digest password)
     */
    public static String getDigestPassword() {
        return Util.sha1(sessionId + password);
    }
    /**
     * Gets plain password
     * @return String 
     */
    public static String getPassword(){
    	return password;
    }
    /**
     * Sets the password 
     * @param String 
     */
    public static void setPassword(String _val) {
        // *** escape the password?
        password = _val;
    }
    /**
     * Sets the session id 
     * @param String 
     */
    public static void setSessionId(String _val) {
        sessionId = _val;
    }
    
    /**
     * Inserts in Datas a new roster item
     * Modified by Gabriele Bianchi 04/01/2006
     * @param _jid
     */
    public static void registerRoster(Jid _jid) {

        roster.put(_jid.getLittleJid(), _jid);
	
        return;
    }
	/**
	 * Get all roster items
	 * @param update
	 */
	public static Vector createRosterVector(boolean update)
	{
		if (!update)
			return roster_vector;
		Enumeration contacts = Datas.roster.elements();
		roster_vector = new Vector();
		while (contacts.hasMoreElements())
		{	
				roster_vector.addElement((Jid)contacts.nextElement());
		}

		return roster_vector;
	}
	/**
	 * Get online roster items
	 * @param update
	 */
	public static Vector createOnlineRosterVector(boolean update)
	{
		if (!update)
			return roster_vector;
		Enumeration contacts = Datas.roster.elements();
		roster_vector = new Vector();
		while (contacts.hasMoreElements())
		{
			Jid temp = (Jid)contacts.nextElement();
			if (!Presence.getPresence("unavailable").equals(temp.getPresence()))
				roster_vector.addElement(temp);
		}

		return roster_vector;
	}
    
    /**
     *Save user jid info in persistent storage (RMS)
     *@author Gabriele Bianchi
     *@return boolean
     */
    public static boolean saveRecord() {
	RecordStore rs = null;
	try {
		rs = RecordStore.openRecordStore("MyOptions",true);
	}catch (RecordStoreException ex) {
		return false;
	}
	//insert
	try {
	if (rs.getNumRecords() == 0) {
		
		byte bytes[] = jid.getFullJid().getBytes();
		try {
			rs.addRecord(bytes,0,bytes.length);
			bytes = password.getBytes();
			rs.addRecord(bytes,0,bytes.length);
			bytes = server_name.getBytes();
			rs.addRecord(bytes,0,bytes.length);
			if (isSSL) {
				bytes = "yes".getBytes();
				port = SSL_PORT;
			}
			else if (isHTTP) {
				bytes = "http".getBytes();
				port = HTTP_PORT;
			}
			else {
				bytes = "no".getBytes();
				port = TCP_PORT;
			}
			rs.addRecord(bytes,0,bytes.length);
			if (jid.getMail() == null || jid.getMail().equals(""))
				bytes = "null".getBytes();
			else
				bytes = jid.getMail().getBytes();
			rs.addRecord(bytes,0,bytes.length);
			if (subdomain == null)
				bytes = "null".getBytes();
			else
				bytes = subdomain.getBytes();
			rs.addRecord(bytes, 0, bytes.length);
			if (Datas.avatarFile == null)
				bytes = "null".getBytes();
			else
				bytes = Datas.avatarFile.getBytes(); //AVATAR
			rs.addRecord(bytes, 0, bytes.length);
			
			if (Datas.customPort.equals("")) {
			  customPort = String.valueOf(port); //custom port
			}
			else 
				port = Integer.parseInt(customPort);
			bytes = customPort.getBytes(); //custom port
			rs.addRecord(bytes, 0, bytes.length);
			
			noData = false; //data set
		}catch (RecordStoreException exc2){
			System.out.println(exc2.getMessage());
			try{
				rs.closeRecordStore();
			}catch (RecordStoreException exc1){
				System.out.println(exc1.getMessage());
				return false;
			}
			return false;
		}
		
		
		
	}
	//update
	else {
		byte bytes[] = jid.getFullJid().getBytes();
		try {
			rs.setRecord(1,bytes,0,bytes.length);
			bytes = password.getBytes();
			rs.setRecord(2,bytes,0,bytes.length);
			bytes = server_name.getBytes();
			rs.setRecord(3,bytes,0,bytes.length);
			if (isSSL){
				bytes = "yes".getBytes();
				port = SSL_PORT;
			}
			else if (isHTTP) {
				bytes = "http".getBytes();
				port = HTTP_PORT;
			}
			else {
				bytes = "no".getBytes();
				port = TCP_PORT;
			}
			rs.setRecord(4,bytes,0,bytes.length);
			if (jid.getMail() == null || jid.getMail().equals(""))
				bytes = "null".getBytes();
			else
				bytes = jid.getMail().getBytes();
			rs.setRecord(5,bytes,0,bytes.length);
			if (subdomain == null)
				bytes = "null".getBytes();
			else
				bytes = subdomain.getBytes();
			rs.setRecord(6, bytes, 0, bytes.length);
			
			//AVATAR
			if (avatarFile == null)
				bytes = "null".getBytes();
			else
				bytes = avatarFile.getBytes();
			rs.setRecord(7, bytes, 0, bytes.length);
			
			if (customPort.equals("")) {
				//bytes = "null".getBytes();
				customPort = String.valueOf(port);
			}
			else 
				port = Integer.parseInt(customPort);
			bytes = customPort.getBytes();
			rs.setRecord(8, bytes, 0, bytes.length);
			
			noData = false; //data set
		}catch (RecordStoreException exc1){
			System.out.println(exc1.getMessage());
			try{
			rs.closeRecordStore();
			}catch (RecordStoreException exc2){
				System.out.println(exc2.getMessage());
				return false;
			}
			return false;
		}
	}
	}catch (RecordStoreNotOpenException exc3){
	
	try{
		rs.closeRecordStore();
	}catch (RecordStoreException exc1){return false;
	}
	return false;
	}
	try{
		rs.closeRecordStore();
	}catch (RecordStoreException exc1){return false;
	}
	return true;
    }

	public static boolean isGateway(String gtw)
	{
		if (gateways.size() == 0) 
		{
			if (gtw.indexOf("@") == -1 && !gtw.equals(hostname)) //weak condition!!!
				return true; 
			else
				return false;

		}

		for (int i = 0; i < gateways.size(); i++)
		{
			if (gtw.equals((String)gateways.elementAt(i)))
				return true;
		}
		return false;
	}

}

