package ccnMultiChat.Application;

import javax.swing.SwingUtilities;

import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import ccnMultiChat.UI.FormMain;

public class Chat {
		
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Thread() {
        	public void run() {
    			try {
    				new FormMain("Chat");
    			} catch (MalformedContentNameStringException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
        });
    }
}
