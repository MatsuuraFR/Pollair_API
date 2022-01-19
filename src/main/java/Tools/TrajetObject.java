package Tools;

import org.json.JSONArray;
import org.json.JSONObject;

public class TrajetObject {
	private String from;
	private String os;
	private JSONObject raw_trip;
	private JSONObject raw_section;
	private JSONObject cleaned_trip;
	private JSONObject cleaned_section;
	private JSONArray locations;
	
	
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public JSONObject getRaw_trip() {
		return raw_trip;
	}
	public void setRaw_trip(JSONObject raw_trip) {
		this.raw_trip = raw_trip;
	}
	public JSONObject getRaw_section() {
		return raw_section;
	}
	public void setRaw_section(JSONObject raw_section) {
		this.raw_section = raw_section;
	}
	public JSONObject getCleaned_trip() {
		return cleaned_trip;
	}
	public void setCleaned_trip(JSONObject cleaned_trip) {
		this.cleaned_trip = cleaned_trip;
	}
	public JSONObject getCleaned_section() {
		return cleaned_section;
	}
	public void setCleaned_section(JSONObject cleaned_section) {
		this.cleaned_section = cleaned_section;
	}
	public JSONArray getLocations() {
		return locations;
	}
	public void setLocations(JSONArray locations) {
		this.locations = locations;
	}
	
	
	public void putLocation(JSONObject loc) {
		this.locations.put(loc);
	}
	
	
}
