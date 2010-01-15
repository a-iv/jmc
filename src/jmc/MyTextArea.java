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
package jmc;

import com.sun.lwuit.TextArea;
import com.sun.lwuit.plaf.Border;

public class MyTextArea extends TextArea {

	public MyTextArea() {
		
	}

	public MyTextArea(String text) {
		super(text);
		this.getStyle().setBgTransparency(0);
        this.getStyle().setBorder(Border.createEmpty());
	}

	public MyTextArea(int rows, int columns) {
		super(rows, columns);
		this.getStyle().setBgTransparency(0);
		this.setGrowByContent(true);
		this.setEditable(false);
		this.getStyle().setBorder(Border.createEmpty());
		this.setEnabled(false);
	}

	public MyTextArea(String text, int maxSize) {
		super(text, maxSize);
		
	}

	public MyTextArea(int rows, int columns, int constraint) {
		super(rows, columns, constraint);
		
	}

	public MyTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		this.getStyle().setBgTransparency(0);
		this.setGrowByContent(true);
		this.setEditable(false);
		this.getStyle().setBorder(Border.createEmpty());
		this.setEnabled(false); 
		
	}

	public MyTextArea(String text, int rows, int columns, int constraint) {
		super(text, rows, columns, constraint);
		this.getStyle().setBgTransparency(0);
		this.setGrowByContent(true);
		this.setEditable(false);
		this.getStyle().setBorder(Border.createEmpty());
		this.setEnabled(false);
	}
	
	//TODO potremmo creare pressedKey

}
