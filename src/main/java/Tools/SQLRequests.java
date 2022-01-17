package Tools;

import java.util.HashMap;

/**
 * simple sql request storage for the project (simplified resource bundle/resx)
 * @author Documents
 *
 */
public class SQLRequests {
	/*
	public static HashMap<String,String> SqlRequestAccess = null;
	
	private SQLRequests() {
		
	}
	
	private static void initializeSingleton() {
		SqlRequestAccess = new HashMap<String,String>();
		
		SqlRequestAccess.put("", "");
	}
	
	public static String getSqlRequest(String requestName) {
		if(SqlRequestAccess == null) {
			initializeSingleton();
		}
		
		return
	}
	*/
	
	public static String SelectPersonById = "SELECT * FROM Personne WHERE idLogin = ?";
	
	
}
