package controller;

import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Evenement;
import model.EvenementRepository;
import model.EvenementRowMapper;

@RestController
@RequestMapping(path = "/evenement")
public class EvenementController {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
    @GetMapping("/all")
    public String all() {
        List<Evenement> events = jdbcTemplate.query("SELECT * FROM EVENEMENT", new EvenementRowMapper());
    	return new Gson().toJson(events);
    }

    @GetMapping("/{id}")
    public String getOne(@PathVariable("id") int id) {
		String query = "SELECT * FROM evenement WHERE idevenement = ?";
		Evenement event = jdbcTemplate.queryForObject(query, new EvenementRowMapper(), id);
        return new Gson().toJson(event);
    }
    
    @GetMapping("/getByDate")
    public String getByDate(@RequestParam("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date date){
		String query = "select * from evenement WHERE datedebut < ? AND datefin > ?";
		List<Evenement> events = jdbcTemplate.query(query, new EvenementRowMapper(), date, date);
        return new Gson().toJson(events); 
    }
	
}

