package Tools;

import org.json.JSONArray;
import org.json.JSONObject;

public class TrajetsFileTemplate {
	private JSONArray cleaned_place ;
	
	private JSONArray trajets;

	public JSONArray getCleaned_place() {
		return cleaned_place;
	}

	public void setCleaned_place(JSONArray cleaned_place) {
		this.cleaned_place = cleaned_place;
	}

	public JSONArray getTrajets() {
		return trajets;
	}

	public void setTrajets(JSONArray trajets) {
		this.trajets = trajets;
	}
	
	
	public void putCleaned_place(JSONObject place) {
		this.cleaned_place.put(place);
	}
	
	public void putTrajet(JSONObject trajet) {
		this.trajets.put(trajet);
	}
	
}
