package model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TaskRowMapper implements RowMapper<Task>{
	@Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		
		Task task = new Task();
		
		task.setIdTask(rs.getInt("IdTask"));
		task.setFilename(rs.getString("filename"));
		task.setFk_idetat(rs.getInt("FK_IdEtat"));
		task.setDtLaunched(rs.getDate("DtLaunched"));
		task.setDtModif(rs.getDate("DtModif"));
		task.setFK_IdPersonne(rs.getInt("FK_IdPersonne"));
		
		return task;
	}
}
