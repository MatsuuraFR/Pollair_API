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
	
	//public static String GetEtatFile = "select Ts.FK_IdEtat,Et.nomEtat from task as Ts,etat as Et where Ts.FK_idEtat = Et.IdEtat AND Fk_IdPersonne= ? and Filename = ? order by DtLaunched desc limit 1;";
	public static String GetEtatFile = "select * from task as Ts, personne as Pr Where Pr.IdLogin = ? and Filename = ? order by DtLaunched desc limit 1;";
	
	//WFS tempo (TODO class attitré)
	//public static String GetIndicesByDate = "https://opendata.atmo-na.org/geoserver/ind_nouvelle_aquitaine/wfs?FILTER=%3Cfes%3AFilter%20xmlns%3Afes%3D%22http%3A%2F%2Fwww.opengis.net%2Ffes%2F2.0%22%3E%20%3Cfes%3APropertyIsEqualTo%3E%20%20%3Cfes%3AValueReference%3Eind_nouvelle_aquitaine%3Adate_ech%3C%2Ffes%3AValueReference%3E%20%20%3Cfes%3ALiteral%3E2022-01-23%3C%2Ffes%3ALiteral%3E%20%3C%2Ffes%3APropertyIsEqualTo%3E%3C%2Ffes%3AFilter%3E&REQUEST=GetFeature&RESULTTYPE=results&OUTPUTFORMAT=application%2Fjson&VERSION=2.0.0&TYPENAMES=ind_nouvelle_aquitaine%3Aind_nouvelle_aquitaine&SERVICE=WFS&STARTINDEX=0";
	public static String GetIndicesByDate1 = "https://opendata.atmo-na.org/geoserver/ind_nouvelle_aquitaine/wfs?FILTER=%3Cfes%3AFilter%20xmlns%3Afes%3D%22http%3A%2F%2Fwww.opengis.net%2Ffes%2F2.0%22%3E%20%3Cfes%3APropertyIsEqualTo%3E%20%20%3Cfes%3AValueReference%3Eind_nouvelle_aquitaine%3Adate_ech%3C%2Ffes%3AValueReference%3E%20%20%3Cfes%3ALiteral%3E";
	public static String GetIndicesByDate2 = "%3C%2Ffes%3ALiteral%3E%20%3C%2Ffes%3APropertyIsEqualTo%3E%3C%2Ffes%3AFilter%3E&REQUEST=GetFeature&RESULTTYPE=results&OUTPUTFORMAT=application%2Fjson&VERSION=2.0.0&TYPENAMES=ind_nouvelle_aquitaine%3Aind_nouvelle_aquitaine&SERVICE=WFS&STARTINDEX=0";
	
}
