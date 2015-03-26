package ccnMultiChat.UI;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Utils {
	
	private static class TimedMessage extends Thread {
		
		private static class MessageTimer extends Timer {
			
			private static class MessageTimerListener implements ActionListener {

				private int _timeLeft;
				private JLabel _label;
				
				public MessageTimerListener(int time, JLabel label) {
					_timeLeft = time;
					_label = label;
				}
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (_timeLeft > 0) {
						_timeLeft--;
					} else {
						((Timer)e.getSource()).stop();
						Window win = SwingUtilities.getWindowAncestor(_label);
						win.setVisible(false);
					}
				}
				
			}
						
			private static final long serialVersionUID = 1L;
			
			public MessageTimer(int delay, int time, JLabel label) {
				super(delay, new MessageTimerListener(time, label));
			}
			
			@Override
			public void start() {
				setInitialDelay(0);
				super.start();
			}
			
		}
		
		private final JLabel _label;
		
		public TimedMessage(String text) {
			_label = new JLabel(text);
		}
		
		@Override
		public void run() {
			(new MessageTimer(1000, 2, _label)).start();
			JOptionPane.showMessageDialog(null, _label);
		}
	}
	
	public static void showTimedMessage(String text) {
		(new TimedMessage(text)).start();
	}
	
	public static void showMessage(String text) {
		JOptionPane.showMessageDialog(null, text);
	}
	
	public static boolean showConfirmationMessage(String text) {
		return (JOptionPane.showConfirmDialog(null, text, "Petición recibida", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
	}
	
}
