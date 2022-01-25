package model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Task")
public class Task {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int IdTask;
	
	private String filename;
	private int fk_idetat;
	private Date DtLaunched;
	private Date DtModif;
	private int FK_IdPersonne;
	
	
	public Task() {
		
	}

	public Task(int idTask, String filename, int fk_idetat, Date dtLaunched, Date dtModif, int fK_IdPersonne) {
		super();
		IdTask = idTask;
		this.filename = filename;
		this.fk_idetat = fk_idetat;
		DtLaunched = dtLaunched;
		DtModif = dtModif;
		FK_IdPersonne = fK_IdPersonne;
	}

	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public int getFk_idetat() {
		return fk_idetat;
	}


	public void setFk_idetat(int fk_idetat) {
		this.fk_idetat = fk_idetat;
	}


	public Date getDtLaunched() {
		return DtLaunched;
	}


	public void setDtLaunched(Date dtLaunched) {
		DtLaunched = dtLaunched;
	}


	public Date getDtModif() {
		return DtModif;
	}


	public void setDtModif(Date dtModif) {
		DtModif = dtModif;
	}


	public int getFK_IdPersonne() {
		return FK_IdPersonne;
	}


	public void setFK_IdPersonne(int fK_IdPersonne) {
		FK_IdPersonne = fK_IdPersonne;
	}


	public int getIdTask() {
		return IdTask;
	}


	public void setIdTask(int idTask) {
		IdTask = idTask;
	}
	
	
}
