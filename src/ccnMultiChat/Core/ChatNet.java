package ccnMultiChat.Core;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.UserConfiguration;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.ErrorStateException;
import org.ccnx.ccn.io.content.CCNStringObject;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.io.content.ContentGoneException;
import org.ccnx.ccn.io.content.ContentNotReadyException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;

public final class ChatNet {
	
	private final CCNChatCallback _callback;
	private final ContentName _namespace;
    
	private Timestamp _lastUpdate;
	private boolean _finished = false;
	
	private static final long CYCLE_TIME = 100;
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

	private CCNHandle _readHandle;
	private CCNHandle _writeHandle;
	
	// Separate read and write libraries so we will read our own updates,
	// and don't have to treat our inputs differently than others.
	private CCNStringObject _readString;
	private CCNStringObject _writeString;
	 
	// this is where we store the friendly name of the users.
	private UserListNet _listnet;
	private UserList _list;
	
	public interface CCNChatCallback {
		public void recvMessage(String message);
	}
	
	public static class Message {
		
		private boolean _auto = false;
 		private boolean _show = true;
 		private int _nonce;
 		
 		private boolean _isLogin = false;
 		private boolean _isLogout = false;
 		
 		private String _message;
 		
 		public boolean getAuto() { return _auto; }
 		public void setAuto(boolean auto) { _auto = auto; }
 		
 		public boolean getShow() { return _show; }
 		public void setShow(boolean show) { _show = show; }
 		
 		public boolean getIsLogin() { return _isLogin; }
 		public void setIsLogin(boolean isLogin) { _isLogin = isLogin; }
 		
 		public boolean getIsLogout() { return _isLogout; }
 		public void setIsLogout(boolean isLogout) { _isLogout = isLogout; }

 		public String getMessage() { return _message; }
 		public void setMessage(String message) { _message = message; }
 		
 		public int getNonce() { return _nonce; }
 		public void setNonce(int nonce) { _nonce =  nonce; }
 		
 		private static String boolToString(boolean b) {
 			if (b) return "1"; else return "0";
 		}
 		
 		private static boolean stringToBool(String s) {
 			if (s.equals("1")) return true; else return false;
 		}
 		
 		private static String intToString(int i) {
 			return String.format("%06d", i);
 		}
 		
 		private static int stringToInt(String s) {
 			return Integer.parseInt(s);
 		}
 		
 		public String toString() {
 			return boolToString(getShow()) + boolToString(getAuto()) + boolToString(getIsLogin()) + boolToString(getIsLogout()) + intToString(getNonce()) + getMessage();
 		}
 		
 		public static Message toMessage(String s) {
 			
 			Message m = new Message();
 			
 			m.setShow(stringToBool(s.substring(0, 1)));
 			m.setAuto(stringToBool(s.substring(1, 2)));
 			m.setIsLogin(stringToBool(s.substring(2, 3)));
 			m.setIsLogout(stringToBool(s.substring(3, 4)));
 			m.setNonce(stringToInt(s.substring(4, 10)));
 			m.setMessage(s.substring(10));
 			
 			return m;
 			
 		}
 		
 		public Message() {
 			_nonce = (new Random()).nextInt(999999);
 		}
 
	}
	
	public ChatNet(CCNChatCallback callback, String namespace, UserList list) throws MalformedContentNameStringException {
    	_callback = callback;
    	_list = list;
    	_namespace = ContentName.fromURI(namespace);
       	_listnet = new UserListNet(list, namespace);
    }
	
	private synchronized void sendMessage(Message message) throws ContentEncodingException, IOException {
		if (null != _writeHandle)
			_writeString.save(message.toString());
	}
	
	public synchronized void sendMessage(String message) throws ContentEncodingException, IOException {
		Message m = new Message();
		m.setMessage(message);
		sendMessage(m);
	}
	
	private void showMessage(PublisherPublicKeyDigest publisher, String sender, Timestamp time, Message message) {
		try {
			if (message.getIsLogin() && !publisher.equals(_writeString.getContentPublisher())) {
				Thread t = new Thread(new Runnable() {
					public void run() {
						Message m = new Message();
						m.setShow(false);
						try {
							Thread.sleep((new Random()).nextInt(2000) + 300);
							sendMessage(m);
						} catch (ContentEncodingException e) {
						} catch (IOException e) {
						} catch (InterruptedException e) {
						}
					}
				});
				t.start();
			}
		} catch (IOException e) {
		}
		
		if (message.getIsLogout()) {
			_list.removeNameFromHash(publisher);
		}
		
		String text;
		if (message.getAuto()) {
			text = "[" + DATE_FORMAT.format(time) + "] " + message.getMessage() + "\n";
		} else {
			text = "[" + DATE_FORMAT.format(time) + "] " + sender + ": " + message.getMessage() + "\n";
		}
		
		_callback.recvMessage(text);
	}
	
	private void showMessage(PublisherPublicKeyDigest publisher, String sender, Timestamp time, String message) {
		
		Message m = Message.toMessage(message);
		if (!m.getShow()) 
			return; 
		showMessage(publisher, sender, time, m);
		
	}
	
	private void showMessage(PublisherPublicKeyDigest publisher, Timestamp time, String message) {
		// Start with key fingerprints. Move up to user names.
		showMessage(publisher, publisher.shortFingerprint().substring(0, 8), time, message);
	}
	
	public void shutdown() throws ContentEncodingException, IOException {
		sendLogout();		
		_finished = true;
		if (null != _readString) 
			_readString.cancelInterest();
	}
	
	private void sendLogout() throws ContentEncodingException, IOException {
		Message m = new Message();
		m.setAuto(true);
		m.setIsLogout(true);
		m.setMessage(UserConfiguration.userName() + " ha abandonado la sala");
		sendMessage(m);
	}
	
	private void sendLogin() throws ContentEncodingException, IOException {
		Message m = new Message();
		m.setAuto(true);
		m.setIsLogin(true);
		m.setMessage(UserConfiguration.userName() + " ha entrado en la sala");
		sendMessage(m);
	}
		
	public void listen() throws ConfigurationException, MalformedContentNameStringException, IOException {

		_readHandle = CCNHandle.getHandle();
		_writeHandle = CCNHandle.open();

		_readString = new CCNStringObject(_namespace, (String)null, SaveType.RAW, _readHandle);
		_readString.updateInBackground(true); 
	
		_writeString = new CCNStringObject(_namespace, (String)null, SaveType.RAW, _writeHandle);
		sendLogin();
		
		_listnet.listen(_readHandle, _writeHandle, _writeString.getContentPublisher());

		// Need to do synchronization for updates that come in while we're processing last one.
		new Thread() {
			public void run() {
				while (!_finished) {
					try {
						synchronized(_readString) {
							_readString.wait(CYCLE_TIME);
							//_readString.waitForData(CYCLE_TIME);
						}
					} catch (InterruptedException e) {
					}
				
					try {
						if (_readString.isSaved()) {
							Timestamp thisUpdate = _readString.getVersion();
							if ((null == _lastUpdate) || thisUpdate.after(_lastUpdate)) {
								Log.info("Got an update: " + _readString.getVersion());
								_lastUpdate = thisUpdate;	
								
								//lookup friendly name to display for this user.....
								String userFriendlyName = _listnet.getFriendlyName(_readString.getContentPublisher());
								if (userFriendlyName.equals("")) {
									showMessage(_readString.getContentPublisher(), thisUpdate, _readString.string());
								} else {
									showMessage(_readString.getContentPublisher(), userFriendlyName, thisUpdate, _readString.string());	
								}
							}
						}
					} catch (ContentDecodingException e) {
						e.printStackTrace();
					} catch (MalformedContentNameStringException e) {
						e.printStackTrace();
					} catch (ContentNotReadyException e) {
						e.printStackTrace();
					} catch (ContentGoneException e) {
						e.printStackTrace();
					} catch (ErrorStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
  
}
