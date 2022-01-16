package model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class EvenementRowMapper implements RowMapper<Evenement> {
	
	@Override
    public Evenement mapRow(ResultSet rs, int rowNum) throws SQLException {
		Evenement event = new Evenement();

        event.setIdEvenement(rs.getInt("idevenement"));
        event.setUid(rs.getInt("uid"));
        event.setLien(rs.getString("lien"));
        event.setInformation_Pratiques(rs.getString("informations_pratiques"));
        event.setImage(rs.getString("image"));
        event.setLangue(rs.getString("langue"));
        event.setTitre(rs.getString("titre"));
        event.setDescription(rs.getString("description"));
        event.setMots_cles(rs.getString("mots_cles"));
        event.setLatlong(rs.getString("latlon"));
        event.setPlace(rs.getString("place"));
        event.setAdresse(rs.getString("adresse"));
        event.setDepartement(rs.getString("departement"));
        event.setRegion(rs.getString("region"));
        event.setVille(rs.getString("ville"));
        event.setDateDebut(rs.getDate("dateDebut"));
        event.setDateFin(rs.getDate("dateFin"));
        event.setTimeTable(rs.getString("timeTable"));
        event.setTarif(rs.getString("tarif"));
        event.setDateMiseAJour(rs.getDate("dateMiseAJour"));
        return event;
    }
}
