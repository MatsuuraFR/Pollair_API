package application;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Tools.JsonResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;



import controller.PersonneController;
import model.Personne;
import model.PersonneRowMapper;



@Component
public class ScheduledTasks {

    @Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	@Scheduled(fixedRate = 86400000) // Suppression après 24 heures
	public ResponseEntity<Object> CleanSession() {
        ArrayList<Personne> Person = new ArrayList<Personne>();
        String query = "SELECT * FROM Personne WHERE DtCrea<=DATE_SUB(NOW(), INTERVAL 1 DAY)";
        String queryDelTask = "DELETE * FROM Task WHERE IdPersonne = ?";
        String queryDelPerson = "DELETE * FROM Personne WHERE IdPersonne = ?";

        try {
			Person = jdbcTemplate.query(query, new PersonneRowMapper());
			for(int i = 0; i < Person.size() ; i++){
				Person = jdbcTemplate.query(queryDelTask, new PersonneRowMapper());
				Person = jdbcTemplate.query(queryDelPerson);
			}
			return JsonResponse.generateResponse("OK",  HttpStatus.OK, Person);
		}catch(EmptyResultDataAccessException e) {
			Person = null;
			return JsonResponse.generateResponse("ID inconnu", 0, null);
		}
		log.info("La session est desormais nettoyé");
	}
}
