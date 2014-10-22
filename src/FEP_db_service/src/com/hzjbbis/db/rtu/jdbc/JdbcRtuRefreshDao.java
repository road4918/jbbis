package com.hzjbbis.db.rtu.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.resultmap.ResultMapper;
import com.hzjbbis.db.rtu.RtuRefreshDao;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.TaskTemplate;
import com.hzjbbis.fk.model.TaskTemplateItem;

public class JdbcRtuRefreshDao implements RtuRefreshDao {
	//配置属性
	private String sqlGetRtu;
	private ResultMapper<BizRtu> mapperGetRtu;
	private String sqlGetMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperGetMeasurePoints;
	
	private String sqlGetRtuTask;
	private ResultMapper<RtuTask> mapperGetRtuTask;
	
	private String sqlGetTaskTemplate;
	private ResultMapper<TaskTemplate> mapperGetTaskTemplate;
	
	private String sqlGetTaskTemplateItem;
	private ResultMapper<TaskTemplateItem> mapperGetTaskTemplateItem;
	
	private SimpleJdbcTemplate simpleJdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public List<MeasuredPoint> getMeasurePoints(String zdjh) {
		ParameterizedRowMapper<MeasuredPoint> rm = new ParameterizedRowMapper<MeasuredPoint>(){
			public MeasuredPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetMeasurePoints.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetMeasurePoints, rm, zdjh);
	}

	public BizRtu getRtu(String zdjh) {
		ParameterizedRowMapper<BizRtu> rm = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtu.mapOneRow(rs);
			}
		};
		BizRtu rtu = simpleJdbcTemplate.queryForObject(this.sqlGetRtu, rm, zdjh);
		//依次加载终端测量点，终端任务列表
		List<MeasuredPoint> mps = getMeasurePoints(zdjh);
		for( MeasuredPoint mp: mps )
			rtu.addMeasuredPoint(mp);
		
		List<RtuTask> tasks = getRtuTasks(zdjh);
		for( RtuTask task: tasks )
			rtu.addRtuTask(task);
		return rtu;
	}

	public BizRtu getRtu(int rtua) {
		ParameterizedRowMapper<BizRtu> rm = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtu.mapOneRow(rs);
			}
		};
		BizRtu rtu = simpleJdbcTemplate.queryForObject(this.sqlGetRtu, rm, rtua);
		//依次加载终端测量点，终端任务列表
		List<MeasuredPoint> mps = getMeasurePoints(rtu.getRtuId());
		for( MeasuredPoint mp: mps )
			rtu.addMeasuredPoint(mp);
		
		List<RtuTask> tasks = getRtuTasks(rtu.getRtuId());
		for( RtuTask task: tasks )
			rtu.addRtuTask(task);
		return rtu;
	}

	public List<RtuTask> getRtuTasks(String zdjh) {
		ParameterizedRowMapper<RtuTask> rm = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuTask.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetRtuTask, rm, zdjh);
	}

	public TaskTemplate getTaskTemplate(String templID) {
		ParameterizedRowMapper<TaskTemplate> rm = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetTaskTemplate.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.queryForObject(this.sqlGetTaskTemplate, rm, templID);
	}

	public List<TaskTemplateItem> getTaskTemplateItems(String templID) {
		ParameterizedRowMapper<TaskTemplateItem> rm = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetTaskTemplateItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetTaskTemplateItem, rm, templID);
	}

	public void setSqlGetRtu(String sqlGetRtu) {
		this.sqlGetRtu = sqlGetRtu;
	}

	public void setMapperGetRtu(ResultMapper<BizRtu> mapperGetRtu) {
		this.mapperGetRtu = mapperGetRtu;
	}

	public void setSqlGetMeasurePoints(String sqlGetMeasurePoints) {
		this.sqlGetMeasurePoints = sqlGetMeasurePoints;
	}

	public void setMapperGetMeasurePoints(
			ResultMapper<MeasuredPoint> mapperGetMeasurePoints) {
		this.mapperGetMeasurePoints = mapperGetMeasurePoints;
	}

	public void setSqlGetRtuTask(String sqlGetRtuTask) {
		this.sqlGetRtuTask = sqlGetRtuTask;
	}

	public void setMapperGetRtuTask(ResultMapper<RtuTask> mapperGetRtuTask) {
		this.mapperGetRtuTask = mapperGetRtuTask;
	}

	public void setSqlGetTaskTemplate(String sqlGetTaskTemplate) {
		this.sqlGetTaskTemplate = sqlGetTaskTemplate;
	}

	public void setMapperGetTaskTemplate(
			ResultMapper<TaskTemplate> mapperGetTaskTemplate) {
		this.mapperGetTaskTemplate = mapperGetTaskTemplate;
	}

	public void setSqlGetTaskTemplateItem(String sqlGetTaskTemplateItem) {
		this.sqlGetTaskTemplateItem = sqlGetTaskTemplateItem;
	}

	public void setMapperGetTaskTemplateItem(
			ResultMapper<TaskTemplateItem> mapperGetTaskTemplateItem) {
		this.mapperGetTaskTemplateItem = mapperGetTaskTemplateItem;
	}

	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}

}
