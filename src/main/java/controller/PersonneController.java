package controller;

import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import Tools.JsonResponse;
import Tools.RandomString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Personne;
import model.PersonneRepository;
import model.PersonneRowMapper;

@RestController
@RequestMapping(path = "/personne")
public class PersonneController {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
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
}
