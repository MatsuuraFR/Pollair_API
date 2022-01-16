package model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PersonneRowMapper implements RowMapper<Personne> {
	
	@Override
    public Personne mapRow(ResultSet rs, int rowNum) throws SQLException {
		Personne personne = new Personne();
		
		personne.setIdPersonne(rs.getInt("IdPersonne"));
		personne.setIdLogin(rs.getString("IdLogin"));
		personne.setDtCrea(rs.getDate("DtCrea"));
		personne.setDtModif(rs.getDate("DtModif"));
		
		return personne;
	}
}
