package Tools;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

public class JsonResponse {
	
	/*
	public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			
	    	map.put("status", status.value());
	        map.put("message", message);
	        map.put("data", responseObj);

	        
		}catch( Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		
		return new ResponseEntity<Object>(map,status);
    }
	*/
	
	public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
		return generateResponse(message,status.value(), responseObj);
    }
	
	
	public static ResponseEntity<Object> generateResponse(String message, int status, Object responseObj) {
		Map<String, Object> map = new HashMap<String, Object>();
		ResponseEntity response;
		try {
			
	    	map.put("status", status);
	        map.put("message", message);
	        map.put("data", responseObj);
	        
	        response = new ResponseEntity<Object>(map,HttpStatus.OK);
		}catch( Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			response = new ResponseEntity<Object>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		//return new ResponseEntity<Object>(map,);
		return response;
    }
}
