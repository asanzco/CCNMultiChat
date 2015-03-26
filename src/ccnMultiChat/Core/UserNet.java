package ccnMultiChat.Core;

import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;

import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.UserConfiguration;
import org.ccnx.ccn.io.CCNWriter;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class UserNet {
	
	public interface CCNUserCallback {
		public boolean showPetition(String petition);
		public void showMessage(String text);
		public void openPrivateChat(String user1, String user2, boolean recieved);
	}
	
	private static class Message {
		
		public enum MESSAGEKIND{
	        PRIVATECHAT(0),
	        UNKNOWN(-1);

	        private final int _value;

	        private MESSAGEKIND(final int newValue) {
	            _value = newValue;
	        }

	        public final int getValue() { return _value; }
	        
	        public static MESSAGEKIND getKind(int value) {
	        	switch (value) {
	        	case 0 : 
	        		return PRIVATECHAT;
	        	default :
	        		return UNKNOWN;
	        	}
	        }
	        
	    }
		
		private final MESSAGEKIND _kind;
		private final String _message;
		
		public MESSAGEKIND getMessageKind() { return _kind; }
		public String getMessage() { return _message; }
		
		public Message(String message, MESSAGEKIND kind) {
			_message = message;
			_kind = kind;
		}
		
		public String toString() {
			return String.valueOf(getMessageKind().getValue()) + getMessage();
		}
		
		public static Message getMessage(String s) {
			return new Message(s.substring(2), MESSAGEKIND.getKind(Integer.valueOf(s.substring(1, 2))));
		}
	}
	
	private class InterestHandler implements CCNInterestHandler {

		protected ContentName _prefix;
		protected CCNHandle _handle;
		
		protected String _username;
		
		public InterestHandler(String ccnxURI, String username) throws MalformedContentNameStringException {
			_username = username;
			_prefix = ContentName.fromURI(ccnxURI + "/" + _username);
		}
		
		public void launch() throws ConfigurationException, IOException {
			_handle = CCNHandle.open();
			_handle.registerFilter(_prefix, this);
		}
		
		public void shutdown() {
			if (null != _handle)
				_handle.unregisterFilter(_prefix, this);
		}
		
		@Override
		public boolean handleInterest(Interest interest) {
			
			System.out.println("RECIEVED NEW INTEREST: " + interest);
			
			// Test to see if we need to respond to it.
			if (!_prefix.isPrefixOf(interest.name())) {
				System.out.println(">Unexpected: got an interest not matching our prefix (which is " + _prefix + ")");
				return false;
			}
			
			if (SegmentationProfile.isSegment(interest.name()) && !SegmentationProfile.isFirstSegment(interest.name())) {
				System.out.println(">Got an interest for something other than a first segment, ignoring " + interest.name() + ".");
				return false;
			} 
			
			boolean response = false;
			
			Message m = Message.getMessage(interest.name().toString().substring(_prefix.toString().length()));
			switch (m.getMessageKind()) {
			case PRIVATECHAT : 
				String user1 = _username;
				String user2 = m.getMessage();
				boolean result = _callback.showPetition(user2 + " quiere iniciar una conversación privada");
				try {
					CCNWriter cw = new CCNWriter(_handle);
					cw.addOutstandingInterest(interest);
					cw.put(interest.getContentName(), String.valueOf(result));
					cw.close();
					if (result) 
						_callback.openPrivateChat(user1, user2, true);
					response = true;
				} catch (IOException e) {
					_callback.showMessage("Error al enviar respuesta");
				} catch (SignatureException e) {
					_callback.showMessage("Error al enviar respuesta");
				} catch (MalformedContentNameStringException e) {
					_callback.showMessage("Error al enviar respuesta");
				}
				
			}
			
			return response;
			
		}
		
	}

	private class ContentHandler implements CCNContentHandler {

		protected HashMap<Interest, Message> _petitions = new HashMap<Interest, Message>();
		protected CCNHandle _handle;
		
		protected String _room;
		protected String _username;
		
		protected ContentName _namespace;
		
		public ContentHandler(String room, String username) {
			_room = room;
			_username = username;
		}
		
		public void launch() throws MalformedContentNameStringException, ConfigurationException, IOException {
			_namespace = ContentName.fromURI(_room);
			_handle = CCNHandle.getHandle();
		}
		
		public void shutdown() {
			
		}
		
		@Override
		public Interest handleContent(ContentObject contentObject, Interest interest) {
			
			System.out.println("RECIEVED INTEREST RESPONSE: " + interest.name());
			if(_petitions.containsKey(interest)) {
				Message m = _petitions.get(interest);
				_petitions.remove(interest);
				
				switch (m.getMessageKind()) {
				case PRIVATECHAT:
					String user1 = _username;
					String aux = interest.name().toString().substring(_namespace.toString().length() + 1);
					String user2 = aux.substring(0, aux.indexOf("/"));
					if(Boolean.valueOf(new String(contentObject.content()))) {
						_callback.openPrivateChat(user1, user2, false);
					} else {
						_callback.showMessage(user2 + " ha cancelado la petición de chat privado.");
					}
					break;
				default:
					break;
				}
			}
			
			return null;
		}
		
		public void registerInterest(Message message, String user) throws MalformedContentNameStringException, IOException {
			
			Interest interest = new Interest(_room + "/" + user + "/" + message.toString());
			interest.answerOriginKind(0);
						
			System.out.println("REGISTERING NEW INTEREST: " + interest.name());
			
			synchronized (_petitions) {
				if(!_petitions.containsKey(interest))
					_petitions.put(interest, message);
			}
						
			_handle.expressInterest(interest, this);
			
		}
		
	}
	
	private final CCNUserCallback _callback;
	private String _room;
	private UserList _list;
	
	private String _username;

	private InterestHandler _interestHandler;
	private ContentHandler _contentHandler;
	
	public UserNet(CCNUserCallback callback, String room, UserList list) {
		_callback = callback;
		_room = room + "/ControllerUsers";
		_list = list;
	}
	
	public void shutdown() {
		if (null != _interestHandler) 
			_interestHandler.shutdown();
		if (null != _contentHandler) 
			_contentHandler.shutdown();
	}
	
	public void listen() throws MalformedContentNameStringException, ConfigurationException, IOException {
		_username = UserConfiguration.userName();
		_interestHandler = new InterestHandler(_room, _username);
		_interestHandler.launch();		
		_contentHandler = new ContentHandler(_room, _username);
		_contentHandler.launch();
	}
	
	public void sendPetitionChat(final String user1, final String user2) throws MalformedContentNameStringException, IOException {
		_contentHandler.registerInterest(new Message(user1, Message.MESSAGEKIND.PRIVATECHAT), user2);
	}
	
}
