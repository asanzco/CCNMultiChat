package ccnMultiChat.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import ccnMultiChat.Core.UserList;
import ccnMultiChat.Core.Utils;

public class FormMain extends FormBase {
	
	private static final long serialVersionUID = 1L;
	
	private UserList _userlist;
	private ControllerUser _user;
	
	private PanelChat _chat;
	private PanelUserList _list;
	
	private JButton _btnSeleccionar;
	private JButton _btnAzar;
	
	protected static final String _mainRoomName = "ccnx:/CCNMultiChat";
    
    public FormMain(String title) throws MalformedContentNameStringException {
    	super();
    	
    	_userlist = new UserList();
    	_user = new ControllerUser(_mainRoomName, _userlist);
    	
    	_chat = new PanelChat(_mainRoomName, _userlist);	
    	_list = new PanelUserList(_userlist);

        initForm();
        
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        setSize(500, 400);
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
		
		checkControls();
		login();
		
    }

    private void initForm() {
    	
    	JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        _chat.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        panel.add(_chat);
        
        JPanel panelList = new JPanel();
        panelList.setLayout(new BoxLayout(panelList, BoxLayout.Y_AXIS));
        panelList.add(_list);
        
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMinimumSize(new Dimension(200, 50));
        p.setMaximumSize(new Dimension(200, 50));
        p.setPreferredSize(new Dimension(200, 50));
        
        _btnSeleccionar = new JButton("Hablar con...");
        _btnSeleccionar.setToolTipText("Hablar con el jugador que escojas de la lista");
        _btnSeleccionar.setMinimumSize(new Dimension(200, 25));
        _btnSeleccionar.setMaximumSize(new Dimension(200, 25));
        _btnSeleccionar.setPreferredSize(new Dimension(200, 25));
        _btnSeleccionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	openPrivateChat(true);
            }
        });
        
        _btnAzar = new JButton("Elegir al azar");
        _btnAzar.setToolTipText("Se escogerá una persona al azar con la que hablar");
        _btnAzar.setMinimumSize(new Dimension(200, 25));
        _btnAzar.setMaximumSize(new Dimension(200, 25));
        _btnAzar.setPreferredSize(new Dimension(200, 25));
        _btnAzar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	openPrivateChat(false);
            }
        });
        
        p.add(_btnSeleccionar, BorderLayout.NORTH);
        p.add(_btnAzar, BorderLayout.SOUTH);
        
        panelList.add(p);
        panel.add(panelList);

        setContentPane(panel);

    }
    
    private void openPrivateChat(boolean selected) {
    	
    	String user;
    	if(selected) {
    		user = _list.getSelectedUser();
    	} else {
    		user = _list.getRandomUser();
    	}
    	
    	if(null != user && user != "") {
    		_user.sendPrivateChatPetition(getUsername(), user);
    	} else if(selected) {
    		ccnMultiChat.UI.Utils.showMessage("Seleccione un usuario válido");
    	} else {
    		ccnMultiChat.UI.Utils.showMessage("No se ha obtendio un usuario válido");
    	}
    
    }
    
    @Override
    protected void checkControls() {
    	super.checkControls();
    	boolean valid = isUsernameValid();

		_btnSeleccionar.setEnabled(valid);
		_btnAzar.setEnabled(valid);
		
		_chat.checkControls(valid);
		_list.checkControls(valid);
		
    	if (valid) {
			try {
				start(getUsername());
			} catch (ConfigurationException e) {
				e.printStackTrace();
			} catch (MalformedContentNameStringException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
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
    
    public void start(String username) throws ConfigurationException, MalformedContentNameStringException, IOException {
    	Utils.setUsername(username);
    	_user.start();
    	_chat.start();
    	_list.start();
    }
    
    public void stop() throws IOException{
    	_user.stop();
    	_chat.stop();
    	_list.stop();    	
    }
    
}