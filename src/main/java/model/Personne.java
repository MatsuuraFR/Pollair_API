package model;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "Personne")
public class Personne {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int IdPersonne;
	
	private String IdLogin;
	private Date DtCrea;
	private Date DtModif;
	
	public Personne() {
		
	}

	public Personne(int idPersonne, String idLogin, Date dtCrea, Date dtModif) {
		super();
		IdPersonne = idPersonne;
		IdLogin = idLogin;
		DtCrea = dtCrea;
		DtModif = dtModif;
	}

	public int getIdPersonne() {
		return IdPersonne;
	}

	public void setIdPersonne(int idPersonne) {
		IdPersonne = idPersonne;
	}

	public String getIdLogin() {
		return IdLogin;
	}

	public void setIdLogin(String idLogin) {
		IdLogin = idLogin;
	}

	public Date getDtCrea() {
		return DtCrea;
	}

	public void setDtCrea(Date dtCrea) {
		DtCrea = dtCrea;
	}

	public Date getDtModif() {
		return DtModif;
	}

	public void setDtModif(Date dtModif) {
		DtModif = dtModif;
	}
	
	
}
