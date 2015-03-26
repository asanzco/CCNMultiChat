package ccnMultiChat.Core;

import java.io.IOException;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.UserConfiguration;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.content.CCNStringObject;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.profiles.security.KeyProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;

public class UserListNet {
	
	private static final int WAIT_TIME_FOR_FRIENDLY_NAME = 2500;
	
	private UserList _userList;
	
	private String _namespaceStr;
	
	private CCNHandle _readHandle;
	private CCNHandle _writeHandle;
	
	private CCNStringObject _readNameString;
	private CCNStringObject _writeNameString;
	
    private ContentName _friendlyNameNamespace;
    
	public UserListNet(UserList userList, String namespace) throws MalformedContentNameStringException {
		_userList = userList; 
		_namespaceStr = namespace;
	}
	
	public String getFriendlyName(PublisherPublicKeyDigest digest) throws ContentDecodingException, IOException, MalformedContentNameStringException {
		
		String name = _userList.getNameFromHash(digest);
		
		if (name.equals("")) {
			// Its not in the hashMap.. So, try and read the user's friendly name from the ContentName and then add it to the hashMap....
			String userNameStr = _namespaceStr + "/members/";  
			_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(userNameStr), digest);
						
			try {
				_readHandle = CCNHandle.getHandle();
				_readNameString = new CCNStringObject(_friendlyNameNamespace, (String)null, SaveType.RAW,  _readHandle);
			} catch (Exception e) {
			}
			
			_readNameString.update(WAIT_TIME_FOR_FRIENDLY_NAME); // for now, I am just waiting for 2.5 secs.. Otherwise, I might have to update in background and have a callback						
			
			if (_readNameString.available()) {	 
				if (digest.equals(_readNameString.getContentPublisher())) {
					_userList.addNameToHash(_readNameString.getContentPublisher(), _readNameString.string());
					name = _readNameString.string();
				}
			}
		}
		
		if (name.equals(""))
			Log.info("We DON'T have an entry in our hash for this " + digest);
		
		return name;
		
	}
	
	public void shutdown() {
		
	}
	
	public void listen(CCNHandle readHandler, CCNHandle writeHandler, PublisherPublicKeyDigest digest) throws MalformedContentNameStringException, ConfigurationException, IOException {

		_readHandle = readHandler;
		_writeHandle = writeHandler;
				
		// Publish the user's friendly name under a new ContentName
		String friendlyNameNamespaceStr = _namespaceStr + "/members/";  
		_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(friendlyNameNamespaceStr), digest);	
		Log.info("**** Friendly Namespace is " + _friendlyNameNamespace);
	
		//read the string here.....	
		_readNameString = new CCNStringObject(_friendlyNameNamespace, (String)null, SaveType.RAW, _readHandle);
		_readNameString.updateInBackground(true);
	
		String publishedNameStr = UserConfiguration.userName();
		Log.info("*****I am adding my own friendly name as " + publishedNameStr);
		_writeNameString = new CCNStringObject(_friendlyNameNamespace, publishedNameStr, SaveType.RAW,  _writeHandle);
		_writeNameString.save();
		
		_userList.addNameToHash(_writeNameString.getContentPublisher(), _writeNameString.string());	
	
	}
		
}
