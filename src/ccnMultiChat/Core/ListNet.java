package ccnMultiChat.Core;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;

import org.ccnx.ccn.config.UserConfiguration;

public class ListNet {

	private DefaultListModel _model;
	
	private UserList _list;
	
	private Timer _timer;
	private static final int TIMER_TIME = 1;
	
	public DefaultListModel getModel() { return _model; }
	  
	public ListNet(UserList list) {
		_list = list;
		_model = new DefaultListModel();
		_timer = new Timer();
	}
	
	private void addNewUser(String username) {
		_model.addElement(username);
	}
	
	private void removeUser(String username) {
		_model.removeElement(username);
	}
	
	private class UserChecker extends TimerTask {
		@Override
		public void run() {
			for (Enumeration<String> e = getElements(); e.hasMoreElements();) {
				String username = e.nextElement();
				if(!_list.getFriendlyNames().contains(username)) {
					removeUser(username);
				}
			}
			for(String username : _list.getFriendlyNames()) {
				if(!username.equals(UserConfiguration.userName())) {
					if(!_model.contains(username)) {
						addNewUser(username);
					}
				}
			}
		}		
	}

	@SuppressWarnings("unchecked")
	private Enumeration<String> getElements() {
		return ((Enumeration<String>)_model.elements());
	}
	
	public void shutdown() {
		// Stop timer
		_timer.cancel();
	}

	public void listen() {
		// Start timer to check new users
		_timer.schedule(new UserChecker(), TIMER_TIME * 1000, TIMER_TIME * 1000);
	}
	
}
