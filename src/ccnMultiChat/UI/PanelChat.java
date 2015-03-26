package ccnMultiChat.UI;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import ccnMultiChat.Core.ChatNet;
import ccnMultiChat.Core.UserList;
import ccnMultiChat.Core.ChatNet.CCNChatCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PanelChat extends JPanel implements ActionListener, CCNChatCallback{
	
	private static final long serialVersionUID = 1L;
	
	protected JTextArea  _messagePane;
    private JTextField _typedText;
    
    private ChatNet _chat;
    
    public PanelChat(String room, UserList list) throws MalformedContentNameStringException{
    	
    	_chat = new ChatNet(this, room, list);
    	
    	// User interface
    	_messagePane = new JTextArea(20,32);
    	_typedText   = new JTextField(32);	
        initPanel();
        
    }

    private void initPanel() {
    	
        setLayout(new BorderLayout());
        
        _messagePane.setEditable(false);
        _messagePane.setLineWrap(true);
        _typedText.addActionListener(this);
        _typedText.requestFocusInWindow();

       add(new JScrollPane(_messagePane), BorderLayout.CENTER);
       add(_typedText, BorderLayout.SOUTH);
       
    }
    
    protected void checkControls(boolean value) {
    	_typedText.setEnabled(value);
    }
    
    public void actionPerformed(ActionEvent e) {
		try {
			String newText = _typedText.getText();
			if ((null != newText) && (newText.length() > 0))
				_chat.sendMessage(newText);

		} catch (Exception e1) {
			System.err.println("Exception saving our input: " + e1.getClass().getName() + ": " + e1.getMessage());
			e1.printStackTrace();
			recvMessage("Exception saving our input: " + e1.getClass().getName() + ": " + e1.getMessage());
		}
        _typedText.setText("");
        _typedText.requestFocusInWindow();
	}

	public void recvMessage(String message) {
		_messagePane.insert(message, _messagePane.getText().length());
        _messagePane.setCaretPosition(_messagePane.getText().length());
	}
	
	protected void stop() throws IOException {
		_chat.shutdown();
	}
	
	public void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
		_chat.listen();
	}

}