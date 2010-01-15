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



import util.Datas;
import util.Contents;
import jabber.subscription.*;
import java.util.Vector;

//import javax.microedition.lcdui.Button;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;

/*import javax.microedition.lcdui.Container;
import javax.microedition.lcdui.Dialog;
import javax.microedition.lcdui.Label;
import javax.microedition.lcdui.TextArea;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.events.ActionEvent;
import javax.microedition.lcdui.events.ActionListener;
import javax.microedition.lcdui.layouts.BoxLayout;
import javax.microedition.lcdui.layouts.GridLayout;
import javax.microedition.lcdui.plaf.Border;
*/
/**
 * Screen for the gateway registration
 * @author Gabriele Bianchi
 *
 */
public class GatewayForm extends Form /*implements ActionListener*/
{
	private GuiMidlet midlet;
	private Vector services;
/*
	public TextArea gateway = new TextArea("", 64); // "Gateway hostname:",
	public TextArea address = new TextArea("ex: myuser@hotmail.com", 1, 64, TextArea.EMAILADDR); // "User:",
	public TextArea password = new TextArea("", 1, 32, TextArea.PASSWORD); // "Password:",
*/	
	//public ButtonGroup choice = new ButtonGroup();//("Choose IM protocol", ChoiceGroup.EXCLUSIVE, Contents.gtwChoices, null);
	
	public GatewayForm(GuiMidlet _midlet)
	{
		super("Gateway");
		midlet = _midlet;
		services = Datas.server_services;
		//this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		configureFirstScreen();
		
	}
	/**
	 * Configure screen
	 */
	public void configureRegistration(String trans) {
/*
		
		this.removeAll();
		
		this.removeCommand(Contents.select);
		this.addComponent(new MyLabel("Gateway"));
		this.addComponent(gateway);
		gateway.setText(trans);
		this.addComponent(new MyLabel("username"));
		this.addComponent(address);
		this.addComponent(new MyLabel("password"));
		this.addComponent(password);
		if (trans.equals("")) {
			MyTextArea t = new MyTextArea("Cannot detect the gateway hostname. Go to 'Back'->'Server info'", 3, 100);
			
			this.addComponent(t);
		}
		this.addCommand(Contents.register);
		this.addCommand(Contents.unregister);
		//this.addCommand(Contents.back);
		this.setCommandListener(this);
		*/
			
	}
	/**
	 * Configure screen
	 */
	public void configureFirstScreen() {
        /*
		//this.append(choice);
		Container cont = new Container();
		int elementWidth = 0;
		ButtonActionListener action = new ButtonActionListener(this);
		
		for (int i=0; i< Contents.gtwChoices.length; i++) {
			//TODO: add image
			Button b = new Button(Contents.gtwChoices[i]); // ,img
			b.getStyle().setBgTransparency(0);
			b.getStyle().setBorder(Border.createLineBorder(1));
			b.setAlignment(Label.CENTER);
			b.setTextPosition(Label.TOP);
			
	        b.addActionListener(action);
	        elementWidth = Math.max(b.getPreferredW(), elementWidth);
	        cont.addComponent(b);
		}
		int cols = 2;//width / elementWidth;
	    int rows = Contents.gtwChoices.length / cols;
	    cont.setLayout(new GridLayout(rows, cols));
	    this.addComponent(cont);
		//this.append(address);
		MyTextArea t = new MyTextArea(Contents.explainGtw,0,100);
		t.setEnabled(true);
		t.setFocusable(true);
		this.addComponent(t);
		this.addCommand(Contents.select);
	
		this.addCommand(Contents.back);
		this.setCommandListener(this);
		
		*/
	}

	
	/**
	 * Check if a gateway exists
	 * @param serv
	 */
	public static boolean existGateway(Vector serv)
	{
		//check if Gateway exists
		for (int j = 0; j < serv.size(); j++)
		{
			String[] s = ((String[])serv.elementAt(j));
			//String lab = ((StringItem)serv.elementAt(j)).getLabel();

			if (s[1].indexOf("yahoo") != -1 || s[1].toLowerCase().indexOf("msn") != -1 || s[1].toLowerCase().indexOf("aim") != -1 || s[1].toLowerCase().indexOf("icq") != -1)
			{
				//Datas.gateways.addElement(s);
				return true;
			}
			if (s[0] != null && (s[0].toLowerCase().indexOf("yahoo") != -1 || s[0].toLowerCase().indexOf("msn") != -1 || s[0].toLowerCase().indexOf("aol") != -1 || s[0].toLowerCase().indexOf("icq") != -1))
			{
				//Datas.gateways.addElement(s);
				return true;
			}

		}
		return false;
	}
	/**
	 * Check if a single transport exists
	 * @param serv
	 */
	 public static String existTransport(Vector serv, String trans) 
	 {
	 
	 	for (int j = 0; j < serv.size(); j++)
		{
			String s[] = ((String[])serv.elementAt(j));
			//String lab = ((StringItem)serv.elementAt(j)).getLabel();

			if (s[1].toLowerCase().indexOf(trans) != -1 )
			{
				Datas.gateways.addElement(s[1]);
				return s[1];
			}
			if (s[0] != null && s[0].toLowerCase().indexOf(trans) != -1 )
			{
				Datas.gateways.addElement(s[0]);
				return s[0];
			}
			

		}
		return "";
	 }
	/*
	public void actionPerformed(ActionEvent arg0) {
		Command cmd = arg0.getCommand();
		if (cmd == Contents.back)
		{
			midlet.getGuiOtherOptions();

		}
		else if (cmd == Contents.select) 
		{
			Button b;
			try {
				b = (Button)this.getFocused();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return;
			}
			
			String t = b.getText();
        	if (t.equals(Contents.gtwChoices[0])) { //MSN
				this.configureRegistration(existTransport(services,"msn"));
			}else if (t.equals(Contents.gtwChoices[1])) {
				this.configureRegistration(existTransport(services,"aim"));
			}else if (t.equals(Contents.gtwChoices[2])) {
				this.configureRegistration(existTransport(services,"icq"));
			}else if (t.equals(Contents.gtwChoices[3])) {
				this.configureRegistration(existTransport(services,"yahoo"));
			}
		}
		else if (cmd == Contents.register)
		{
			String gtw = gateway.getText();
			//boolean go = false;
			if (gtw.equals("")) {
				this.show();
				return;
			}
			
			for (int i = 0; i < services.size(); i++)
			{
				String lab[] = ((String[])services.elementAt(i));
			
				if (gateway.getText().equals(lab[1]))
				{
	
					Subscribe.registerGateway(address.getText(), password.getText(), gtw);
						
					Dialog.show("", Contents.done, null, Dialog.TYPE_CONFIRMATION,null, 3000);
					
					midlet.getGuiOtherOptions();
					return;
				}

			}

			Dialog.show("", Contents.noGtw, null, Dialog.TYPE_ERROR,null, 3000);
			return;

		}
		else if (cmd == Contents.unregister)
		{
			String gtw = gateway.getText();
		
			if (gtw.equals(""))
			{
				this.show();
				return;
			}
			//StringItem item;
			for (int i = 0; i < services.size(); i++)
			{
				String[] item = (String[])services.elementAt(i);
				if (gateway.getText().equals(item[1]))
				{

					Subscribe.unregisterGateway(gtw);
					Dialog.show("", Contents.done, null, Dialog.TYPE_CONFIRMATION,null, 3000);
					
					midlet.getGuiOtherOptions();
					return;
				}

			}
			Dialog.show("", Contents.noGtw, null, Dialog.TYPE_ERROR,null, 3000);
			//midlet.display.setCurrent(Contents.noGtw, this);
			return;

		}
		
	}
	
	private class ButtonActionListener implements ActionListener {
		private GatewayForm form;
		public ButtonActionListener(GatewayForm f) {
			form = f;
		}
        public void actionPerformed(ActionEvent evt) {
        	Button b = (Button)evt.getSource();
        	String t = b.getText();
        	if (t.equals(Contents.gtwChoices[0])) { //MSN
				form.configureRegistration(existTransport(services,"msn"));
			}else if (t.equals(Contents.gtwChoices[1])) {
				form.configureRegistration(existTransport(services,"aim"));
			}else if (t.equals(Contents.gtwChoices[2])) {
				form.configureRegistration(existTransport(services,"icq"));
			}else if (t.equals(Contents.gtwChoices[3])) {
				form.configureRegistration(existTransport(services,"yahoo"));
			}
        	
        }
	}
	
*/

}
