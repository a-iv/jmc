                *** microJabber ***

Content:

I.   Disclaimer
II.  MicroJabber
III.  Change Log



*********************************
* I. Disclaimer                 *
*********************************

MicroJabber, jabber for light java devices. Copyright (C) 2004, Gregoire Athanase

This library is free software; you can redistribute it and/or modify it under the 
terms of the GNU Lesser General Public License as published by the Free Software 
Foundation; either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with 
this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, 
Suite 330, Boston, MA 02111-1307 USA.


**********************************
* II. MicroJabber                *
**********************************
Project : MicroJabber, jabber for light java devices.
Author  : Gregoire Athanase
Licence : LGPL

Project hosted at sourceforge, http://sourceforge.net/projects/micro-jabber/
Project Home, http://micro-jabber.sourceforge.net/


microJabber is a library bringing Jabber technologies to light java devices.
The package "microJabber" contains 
 * an xmpp compliant parser (for xml streams) in directory ./xmlStreamParser
 * usefull libraries for Jabber identifiers, messages, etc. in directory ./jabber
 * a library for SHA encryption from the bouncycastle (www.bouncycastle.org) in directory ./org
 * classes for TCP connection management (./CommunicationManager.java & ./CommunicationIniter.java) 
 * utility classes (reader & writer threads...) directory ./util
 * two midlet examples ./BasicMidlet.java & ./MyMidlet.java


This package is intended for developpers only, since it cannot connect to public
Jabber servers. Indeed, the communication is still operated using TCP, and suffering
subsequent connection problems with many mobile phones. The connection is made up of
two simplex connections, and needs some kind of server side proxy to initiate.
An improvement of this would be to use HTTP instead of TCP.


**********************************
* IV. Change Log                 *
**********************************

Version 1.0:
  added a basic midlet (not tested!)