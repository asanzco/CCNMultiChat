package ccnMultiChat.Core;

import java.util.Collection;
import java.util.HashMap;

import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;

public class UserList {

	private HashMap<PublisherPublicKeyDigest, String> _friendlyNameToDigestHash;
	
	public UserList() {
		_friendlyNameToDigestHash = new HashMap<PublisherPublicKeyDigest, String>();
	}
	
	public Collection<String> getFriendlyNames() {
    	return _friendlyNameToDigestHash.values();
    }
	
	public boolean addNameToHash(PublisherPublicKeyDigest digest, String friendlyName) {
		boolean result;
		
		synchronized (_friendlyNameToDigestHash) {
			result = !_friendlyNameToDigestHash.containsKey(digest);
			if (result)
				_friendlyNameToDigestHash.put(digest, friendlyName);
		}
		
		return result;
	}
	
	public boolean removeNameFromHash(PublisherPublicKeyDigest digest) {
		boolean result;
		
		synchronized (_friendlyNameToDigestHash) {
			result = _friendlyNameToDigestHash.containsKey(digest);
			if (result)
				_friendlyNameToDigestHash.remove(digest);
		}
		
		return result;
	}
	
	public String getNameFromHash(PublisherPublicKeyDigest digest) {
		String resultado = "";
		if(_friendlyNameToDigestHash.containsKey(digest))
			resultado = _friendlyNameToDigestHash.get(digest);
		return resultado;
	}
	
}
