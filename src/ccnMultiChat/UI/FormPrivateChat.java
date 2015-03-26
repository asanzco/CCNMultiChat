package ccnMultiChat.UI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import ccnMultiChat.Core.UserList;

public class FormPrivateChat extends FormBase {

	private static final long serialVersionUID = 1L;

	private PanelChat _chat;

	public FormPrivateChat(String username, String chatUsername, boolean recieved) throws MalformedContentNameStringException {
		super(username);
				
		String room = FormMain._mainRoomName + "/privatechat/";
		if(recieved) {
			room += chatUsername + username; 
		} else {
			room += username + chatUsername;
		}
		_chat = new PanelChat(room, new UserList());
		
    	initForm();
         
        setTitle("Chat privado (" + chatUsername + ")");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // close output stream  - this will cause listen() to stop and exit
		addWindowListener(
	        new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
		        	close();
		        }
		    }
		);
        
    }
    
    private void initForm() throws MalformedContentNameStringException {
    	JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        panel.add(_chat);

        setContentPane(panel);

    }
    
    @Override
    protected void close() {
    	try {
			stop();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    	super.close();
    }
    
    public void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
    	_chat.start();
    }
    
    public void stop() throws IOException {
    	_chat.stop();
    }
    
}

