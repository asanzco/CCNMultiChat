package ccnMultiChat.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FormLogin extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private JTextField _typedText;
	
	private String _username = "";
	public String getUsername() { return _username; }
	
	public boolean isUsernameValid() { return !_username.equals(""); }	
	
	public FormLogin() { 
		super((Window)null);
		setModal(true);
		
		// User interface
    	_typedText   = new JTextField(32);
		initForm();
		
		setTitle("Login");
        setSize(400, 90);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
	
	}
	
	private void initForm() {
		
		JPanel panelY = new JPanel();
        panelY.setLayout(new BoxLayout(panelY, BoxLayout.Y_AXIS));
        panelY.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        _typedText.setMinimumSize(new Dimension(360, 25));
        _typedText.setMaximumSize(new Dimension(360, 25));
        _typedText.setPreferredSize(new Dimension(360, 25));
        _typedText.requestFocusInWindow();
        panelY.add(_typedText, BorderLayout.NORTH);
        
        JPanel panelX = new JPanel();
        panelX.setLayout(new BoxLayout(panelX, BoxLayout.X_AXIS));
        
        JButton btnCheck = new JButton("Validar");
        btnCheck.setMinimumSize(new Dimension(180, 25));
        btnCheck.setMaximumSize(new Dimension(180, 25));
        btnCheck.setPreferredSize(new Dimension(180, 25));
        btnCheck.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
                checkUsername();
            }
        });
        panelX.add(btnCheck);
        
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setMinimumSize(new Dimension(180, 25));
        btnCancel.setMaximumSize(new Dimension(180, 25));
        btnCancel.setPreferredSize(new Dimension(180, 25));
        btnCancel.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
                dispose();
            }
        }); 
        panelX.add(btnCancel);
        
        panelY.add(panelX, BorderLayout.SOUTH);
        setContentPane(panelY);
	
	}
	
	private void checkUsername() {
		
		boolean valid = true;
		String error = "";
		
		if (_typedText.getText().trim().equals("")) {
			valid = false;
			error = "Debe contener al menos 1 caracter";
		} else if (_typedText.getText().trim().contains(" ")) {
			valid = false;
			error = "No puede contener espacios en blanco";
		}
		
		if(valid) {
			_username = _typedText.getText().trim();
			dispose();
		} else {
			String text = "Introduzca un nombre válido";
			if (!error.equals("")) 
				text += ": " + error;
			Utils.showMessage(text);
		}
	}
	
}
