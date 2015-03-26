package ccnMultiChat.UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class FormBase extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JMenuItem _login;
	
	private String _username = "";
	public String getUsername() { return _username; }
	
	public boolean isUsernameValid() { return (null != _username && !_username.equals("")); }
		
	public FormBase() {
		this(null);
	}

    public FormBase(String username) {
    	if (null != username && !username.equals(""))
    		_username = username;

    	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    	
    	initMenu();
    	checkLogin();
    }
    
    private void initMenu() {
    	    	
    	// File menu
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        //Login
        _login = new JMenuItem("Login");
        _login.setMnemonic(KeyEvent.VK_L);
        _login.setToolTipText("Log into the application");
        _login.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent event) {
        		login();
            }
        });
        file.add(_login);
        
        //Exit
        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_E);
        exit.setToolTipText("Exit application");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
               close();
            }
        });
        file.add(exit);
        
        // menubar
        JMenuBar menubar = new JMenuBar();
        menubar.add(file);
        setJMenuBar(menubar);
        
    }
    
    protected void login() {
    	
    	FormLogin log = new FormLogin();

    	if(log.isUsernameValid()) {
    		_username = log.getUsername();
    	} else {
    		Utils.showMessage("No ha iniciado sesión, no podrá usar la aplicación hasta que inicie sesión");
    	}
    	
    	checkControls();
	
    }
    
    private void checkLogin() {
    	_login.setEnabled(!isUsernameValid());
    }
    
    protected void checkControls() {
    	checkLogin();
    }
    
    protected void close() {
    	dispose();
    }

}
