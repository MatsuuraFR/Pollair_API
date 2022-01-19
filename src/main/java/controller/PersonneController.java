package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import Tools.JsonResponse;
import Tools.RandomString;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;


import model.Personne;
import model.PersonneRepository;
import model.PersonneRowMapper;
import service.FilesStorageService;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping(path = "/personne")
public class PersonneController {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	FilesStorageService storageService;
	
	/**
	 * 
	 * @param idLogin
	 * @return
	 */
	@GetMapping("/{idLogin}")
	public ResponseEntity<Object> getPersonneFromIdLogin(@PathVariable("idLogin") String idLogin) {
		String query = "SELECT * FROM Personne WHERE idLogin = ?";	
		Personne personne;
		
		try {
			personne = jdbcTemplate.queryForObject(query, new PersonneRowMapper(), idLogin);
			return JsonResponse.generateResponse("OK",  HttpStatus.OK, personne);
		}catch(EmptyResultDataAccessException e) {
			personne = null;
			return JsonResponse.generateResponse("ID inconnu", 0, null);
		}
		//return new Gson().toJson(personne);
		
	}
	
	@GetMapping("/createpersonne")
	public ResponseEntity<Object> createPersonne(){
		
		String queryCheckId = "SELECT * FROM Personne WHERE idLogin = ?";
		String queryInsert = "INSERT INTO Personne (IdLogin,DtCrea,DtModif) VALUES (?,NOW(),NOW());";
		
		//1 - creation d'un id unique
		RandomString randomStringInstance = new RandomString();
		
		boolean unique = false;
		int triedloop = 0;
		String idCreated = "";
		Personne personne = null;
		
		while(!unique && triedloop < 20) {
			idCreated = randomStringInstance.nextString();
			//idCreated = "nNJTKTnT";
			try {
				personne = jdbcTemplate.queryForObject(queryCheckId, new PersonneRowMapper(), idCreated);
				triedloop++;
			}catch(EmptyResultDataAccessException e) {
				unique = true;
			}
			
			/*
			if(personne == null) {
				unique = true;
			}else {
				triedloop++;
			}
			*/
		}
		
		if(personne == null) {
			int codeRetour = 0;
			try {
				codeRetour = jdbcTemplate.update(queryInsert, idCreated);
				return JsonResponse.generateResponse("OK",  HttpStatus.OK, idCreated);
			}catch(Exception e) {
				//System.out.println(e);
				return JsonResponse.generateResponse("Une erreur est survenue à la création de l'id: Insert failed",  HttpStatus.INTERNAL_SERVER_ERROR, null);
			}
		}else {
			return JsonResponse.generateResponse("Une erreur est survenue à la création de l'id: Attempt check failed",  HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
		
		//return null;//debug
	}
	
	/*
	 * iOS
	 * http://localhost:8080/personne/testreadjson/ios/pMlmNQWo/yohann-2022-01-12.2022-01-12.timeline
	 */
	@GetMapping("/testreadjson/{typeOS}/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> readJson(@PathVariable String filename,@PathVariable("idLogin") String idLogin, @PathVariable("typeOS") String typeOS){
		
		JSONArray jsonObject = null;
		
		ArrayList<JSONObject> trajetsRaw = new ArrayList<JSONObject>();
		ArrayList<JSONObject> trajetsClean = new ArrayList<JSONObject>();
		HashMap<String,JSONObject> placesRaw = new HashMap<String,JSONObject>();
		HashMap<String,JSONObject> placesClean = new HashMap<String,JSONObject>();
		
		
		Resource jsonFile = storageService.load(filename,idLogin, "timeline");
		try (Reader reader = new InputStreamReader(jsonFile.getInputStream())) {
             String jsonString = FileCopyUtils.copyToString(reader);
             
             jsonObject = new JSONArray(jsonString);
             
             //JSONArray obj = new JSONArray(jsonString);
             //System.out.println(obj.getJSONObject(0).getJSONObject("metadata").getString("key"));
             
             //return JsonResponse.generateResponse("ok", 200, obj.get(0).toString());
        } catch (IOException e) {
        	System.err.println(e);
        	return JsonResponse.generateResponse("Erreur lors de la lecture du fichier", 0, null);
        }catch(Exception e) {
        	System.err.println(e);
        	return JsonResponse.generateResponse("Erreur interne", 0, null);
        }
		
		switch(typeOS) {
		case "ios": {
			//System.out.println("jsonObject.length(): "+jsonObject.length());//debug
			
			for(int i=0; i < jsonObject.length();i++) {
				
				//segmentation
				switch(getJsonMetadataKey(jsonObject.getJSONObject(i))) {
					case "segmentation/raw_trip": {
						trajetsRaw.add(jsonObject.getJSONObject(i));
					}
					break;
					case "segmentation/raw_place" : {
						placesRaw.put(jsonObject.getJSONObject(i).getJSONObject("_id").getString("$oid"),jsonObject.getJSONObject(i));
					}
					break;
					case "analysis/cleaned_trip" : {
						trajetsClean.add(jsonObject.getJSONObject(i));
					}
					break;
					case "analysis/cleaned_place" : {
						placesClean.put(jsonObject.getJSONObject(i).getJSONObject("_id").getString("$oid"),jsonObject.getJSONObject(i));
					}
					break;
				}
				
				/*
				if(getJsonMetadataKey(jsonObject.getJSONObject(i)).equals("segmentation/raw_trip")) {
					trajets.add(jsonObject.getJSONObject(i));
				}
				*/
			}
			
			//DEBUG
			System.out.println(
					"trajetsRaw length: "+trajetsRaw.size() + " | " + 
					"placesRaw length: " + placesRaw.size() + " | " + 
					"trajetsClean length: "+trajetsClean.size() + " | " + 
					"placesClean length: " + placesClean.size() + " | "
		    );//debug
			System.out.println(placesRaw.get("61de2329b198a6cbdbbb2b52"));//debug
			
			//FIN DEBUG
			
		}
		break;
		case "android" : {
			return JsonResponse.generateResponse("OS non implémenté", 0, null);
		}
		//break;
		default : {
			return JsonResponse.generateResponse("OS non implémenté", 0, null);
		}
		//break;
		}
		
		
		
		return null; //debug
		
		//return JsonResponse.generateResponse("ok", 200, null);
	}
	
	private String getJsonMetadataKey(JSONObject jso) {
		return jso.getJSONObject("metadata").getString("key");
	}
	
}
