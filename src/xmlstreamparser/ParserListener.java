package xmlstreamparser;

/**
 * <p>Provides hooks for the parser</p>
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

/**
 * XML Parser interface
 */
public interface ParserListener {

    /**
     * Called when the xml prolog has been read. The prolog
     * is the <?xml version='1.0'?> like string.
     * Node's name and attributes (with values) are mapped.
     *
     * @param _node Contains all infos about the prolog.
     */
    void prologEnd(Node _node);

    /**
     * Called when the parser starts a new node <node>...</node>,
     * except for nodes like <node/> which only call the nodeEnd method.
     * Node's name and attributes (with values) are mapped,
     * contrary to text and children nodes (reported later with the 
     * nodeEnd method).
     *
     * @param _node Contains some infos about the starting node.
     */
    void nodeStart(Node _node);

    /**
     * Called whenever the parser finishes a node.
     * Node's name and attributes (with values) are mapped, 
     * in addition to text and children nodes.
     *
     * @param _node Contains all infos about the ending node.
     */
    void nodeEnd(Node _node);

    /**
     * Called whenever the parser reads a "useless" blank character
     * between two nodes. Such characters serve generally for 
     * formatting purposes.
     */
    void blank();

}
