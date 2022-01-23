package Tools;

import java.util.HashMap;
import java.util.Map;

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
	
	//22-01-2022
	//todo inverse, retour du pk par rapport à une recherche texte 
	/*
	public static HashMap<Integer, String> etats = new HashMap<Integer, String>()  {{
		put(1, "en cours");
		put(2,"complet");
		put(3,"erreur");
	}};
	*/
	
	/**
	 * init,ok,error
	 */
	public static HashMap<String, Integer> etats = new HashMap<String, Integer>()  {{
		put("init", 1);
		put("ok",2);
		put("error",3);
	}};
	
	
	public static String SelectPersonById = "SELECT * FROM Personne WHERE idLogin = ?";
	
	
	public static String InsertTaskInitialize = "INSERT INTO Task (Filename,FK_IdEtat, DtLaunched, DtModif,FK_IdPersonne) VALUES ( ? ,1,NOW(),NOW(), ? );";
	public static String UpdateEtatTask = "UPDATE Task Set FK_IdEtat= ? Where FileName = ? AND FK_IdPersonne = ? ; ";
	
	
}
