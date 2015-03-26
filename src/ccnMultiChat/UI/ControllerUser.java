package ccnMultiChat.UI;

import java.io.IOException;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import ccnMultiChat.Core.UserList;
import ccnMultiChat.Core.UserNet;
import ccnMultiChat.Core.UserNet.CCNUserCallback;

public class ControllerUser implements CCNUserCallback {

	private UserNet _userNet;
	
	public ControllerUser(String room, UserList list) {
		_userNet = new UserNet(this, room, list);
	}
	
	public void start() throws MalformedContentNameStringException, ConfigurationException, IOException {
		_userNet.listen();
	}
	
	public void stop() {
		_userNet.shutdown();
	}

	@Override
	public boolean showPetition(String petition) {
		return Utils.showConfirmationMessage(petition);
	}
	
	@Override
	public void showMessage(String text) {
		Utils.showMessage(text);
	}

	@Override
	public void openPrivateChat(String user1, String user2, boolean recieved) {
		try {
			FormPrivateChat panel = new FormPrivateChat(user1, user2, recieved);
			panel.start();
		} catch (MalformedContentNameStringException e) {
			Utils.showMessage("Error al iniciar el chat privado");
		} catch (ConfigurationException e) {
			Utils.showMessage("Error al iniciar el chat privado");
		} catch (IOException e) {
			Utils.showMessage("Error al iniciar el chat privado");
		}
	}
	
	public void sendPrivateChatPetition(String user1, String user2) {
		Utils.showTimedMessage("Enviando solucitud de chat privado a " + user2 + "...");
		try {
			_userNet.sendPetitionChat(user1, user2);
		} catch (MalformedContentNameStringException e) {
			Utils.showMessage("Error al enviar solicitud de chat privado a " + user2);
			e.printStackTrace();
		} catch (IOException e) {
			Utils.showMessage("Error al enviar solicitud de chat privado a " + user2);
			e.printStackTrace();
		}
	}
	
}
