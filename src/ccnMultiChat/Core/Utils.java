package ccnMultiChat.Core;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ccnx.ccn.config.UserConfiguration;

public class Utils {
	
	public static void setUsername(String username) {
		if (!UserConfiguration.userName().equals(username)) {
			UserConfiguration.setUserName(username);
			
			Map<String, String> newenv = new HashMap<String, String>();
		    newenv.put("CCNX_USER_NAME", username);
		    newenv.put("CCNX_DIR", "/var/tmp/" + username + "/.ccnx");
			Utils.setEnv(newenv);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void setEnv(Map<String, String> newenv) {
	  try {
	        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	        theEnvironmentField.setAccessible(true);
	        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	        env.putAll(newenv);
	        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	        theCaseInsensitiveEnvironmentField.setAccessible(true);
	        Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
	        cienv.putAll(newenv);
	    } catch (NoSuchFieldException e) {
	      try {
	        Class[] classes = Collections.class.getDeclaredClasses();
	        Map<String, String> env = System.getenv();
	        for(Class cl : classes) {
	            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	                Field field = cl.getDeclaredField("m");
	                field.setAccessible(true);
	                Object obj = field.get(env);
	                Map<String, String> map = (Map<String, String>) obj;
	                map.clear();
	                map.putAll(newenv);
	            }
	        }
	      } catch (Exception e2) {
	        e2.printStackTrace();
	      }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    } 
	}
	
	protected static Timestamp now() { 
		return new Timestamp(System.currentTimeMillis()); 
	}

}
