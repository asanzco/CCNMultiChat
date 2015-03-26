package ccnMultiChat.UI;

import java.awt.Dimension;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ccnMultiChat.Core.ListNet;
import ccnMultiChat.Core.UserList;

public class PanelUserList extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JList _list;
	
	private ListNet _listNet;
    
	public PanelUserList(UserList list){
		
		_listNet = new ListNet(list);
		_list = new JList(_listNet.getModel());
		
		initPanel();
		
	}
	
	private void initPanel() {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(200, 0));
        setMinimumSize(new Dimension(200, 0));
        setMaximumSize(new Dimension(200, 10000));
        
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane list = new JScrollPane(_list);     
        add(list);
       
	}
	
	public String getSelectedUser() {
		
		String username = "";
		
		if(null != _list.getSelectedValue()) 
			username = (String)_list.getSelectedValue();
		
		return username;
		
	}
	
	public String getRandomUser() {
		
		String username = "";
		
		int users = _list.getModel().getSize();
		
		if(users > 0) {
			Random rn = new Random();
			int user = rn.nextInt() % users;
			_list.setSelectedIndex(user);
			username = getSelectedUser();
		}
		
		return username;
		
	}
	
	protected void checkControls(boolean value) {
    	_list.setEnabled(value);
    }
	
	protected void start(){
		_listNet.listen();
	}
	
	protected void stop(){
		_listNet.shutdown();
	}

}
