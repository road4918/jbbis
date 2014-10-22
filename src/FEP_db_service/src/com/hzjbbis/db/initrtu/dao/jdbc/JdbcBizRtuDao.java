package com.hzjbbis.db.initrtu.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.initrtu.dao.BizRtuDao;
import com.hzjbbis.db.resultmap.ResultMapper;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuAlertCode;
import com.hzjbbis.fk.model.RtuAlertCodeArg;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.SysConfig;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskTemplate;
import com.hzjbbis.fk.model.TaskTemplateItem;

public class JdbcBizRtuDao implements BizRtuDao {
	//可配置属性
	private String sqlLoadRtu;
	private ResultMapper<BizRtu> mapperLoadRtu;
	
	private String sqlLoadMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperLoadMeasurePoints;

	private String sqlLoadAlertCode;
	private ResultMapper<RtuAlertCode> mapperLoadAlertCode;
	
	private String sqlLoadAlertCodeArgs; 
	private ResultMapper<RtuAlertCodeArg> mapperLoadAlertCodeArgs;

	private String sqlLoadRtuTask;
	private ResultMapper<RtuTask> mapperLoadRtuTask;
	
	private String sqlLoadTaskDbConfig;
	private ResultMapper<TaskDbConfig> mapperLoadTaskDbConfig;
	
	private String sqlLoadTaskTemplate;
	private ResultMapper<TaskTemplate> mapperLoadTaskTemplate;
	private String sqlLoadTaskTemplateItem;
	private ResultMapper<TaskTemplateItem> mapperLoadTaskTemplateItem;
	
	private String sqlLoadSysConfig;
	private ResultMapper<SysConfig> mapperLoadSysConfig;

	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public List<BizRtu> loadBizRtu() {
		ParameterizedRowMapper<BizRtu> rowMap = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtu, rowMap);
	}

	public List<MeasuredPoint> loadMeasuredPoints() {
		ParameterizedRowMapper<MeasuredPoint> rowMap = new ParameterizedRowMapper<MeasuredPoint>(){
			public MeasuredPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadMeasurePoints.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMeasurePoints, rowMap);
	}

	private List<RtuAlertCodeArg> loadRtuAlertCodeArgs() {
		ParameterizedRowMapper<RtuAlertCodeArg> rowMap = new ParameterizedRowMapper<RtuAlertCodeArg>(){
			public RtuAlertCodeArg mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadAlertCodeArgs.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadAlertCodeArgs, rowMap);
	}

	public List<RtuAlertCode> loadRtuAlertCodes() {
		ParameterizedRowMapper<RtuAlertCode> rowMap = new ParameterizedRowMapper<RtuAlertCode>(){
			public RtuAlertCode mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadAlertCode.mapOneRow(rs);
			}
		};
		List<RtuAlertCode> alertCodeList = this.simpleJdbcTemplate.query(sqlLoadAlertCode, rowMap);
		HashMap<String,RtuAlertCode> map = new HashMap<String,RtuAlertCode>();
		for( RtuAlertCode acode: alertCodeList){
			map.put(acode.getCode(), acode);
		}
		List<RtuAlertCodeArg> args = loadRtuAlertCodeArgs();
		for( RtuAlertCodeArg arg : args ){
			RtuAlertCode acode = map.get(arg.getCode());
			if( null != acode )
				acode.getArgs().add(arg.getSjx());
		}
		map.clear();
		args.clear();
		return alertCodeList;
	}

	public List<RtuTask> loadRtuTasks() {
		ParameterizedRowMapper<RtuTask> rowMap = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtuTask.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtuTask, rowMap);
	}

	public List<TaskDbConfig> loadTaskDbConfig() {
		ParameterizedRowMapper<TaskDbConfig> rowMap = new ParameterizedRowMapper<TaskDbConfig>(){
			public TaskDbConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskDbConfig.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadTaskDbConfig, rowMap);
	}

	public List<TaskTemplate> loadTaskTemplate() {
		ParameterizedRowMapper<TaskTemplate> rowMap = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskTemplate.mapOneRow(rs);
			}
		};
		List<TaskTemplate> taskTemps = this.simpleJdbcTemplate.query(sqlLoadTaskTemplate, rowMap);
		Map<String,TaskTemplate> map = new HashMap<String,TaskTemplate>();
		for(TaskTemplate tt: taskTemps ){
			map.put(tt.getTaskTemplateID(), tt);
		}
		List<TaskTemplateItem> taskTempItems = loadTaskTemplateItem();
		for( TaskTemplateItem ttItem: taskTempItems ){
			TaskTemplate tt = map.get(ttItem.getTaskTemplateID());
			if( null != tt )
				tt.addDataCode(ttItem.getCode());
		}
		return taskTemps;
	}

	public void setMapperLoadSysConfig(ResultMapper<SysConfig> mapperLoadSysConfig) {
		this.mapperLoadSysConfig = mapperLoadSysConfig;
	}

	public void setSqlLoadSysConfig(String sqlLoadSysConfig) {
		this.sqlLoadSysConfig = sqlLoadSysConfig;
	}

	private List<TaskTemplateItem> loadTaskTemplateItem() {
		ParameterizedRowMapper<TaskTemplateItem> rowMap = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskTemplateItem.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadTaskTemplateItem, rowMap);
	}
	
	public List<SysConfig> loadSysConfig() {
		ParameterizedRowMapper<SysConfig> rowMap = new ParameterizedRowMapper<SysConfig>(){
			public SysConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadSysConfig.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadSysConfig, rowMap);
	}

	public void setSqlLoadRtu(String sqlLoadRtu) {
		this.sqlLoadRtu = sqlLoadRtu;
	}

	public void setSqlLoadMeasurePoints(String sqlLoadMeasurePoints) {
		this.sqlLoadMeasurePoints = sqlLoadMeasurePoints;
	}

	public void setSqlLoadAlertCode(String sqlLoadAlertCode) {
		this.sqlLoadAlertCode = sqlLoadAlertCode;
	}

	public void setSqlLoadRtuTask(String sqlLoadRtuTask) {
		this.sqlLoadRtuTask = sqlLoadRtuTask;
	}

	public void setSqlLoadTaskDbConfig(String sqlLoadTaskDbConfig) {
		this.sqlLoadTaskDbConfig = sqlLoadTaskDbConfig;
	}

	public void setSqlLoadTaskTemplate(String sqlLoadTaskTemplate) {
		this.sqlLoadTaskTemplate = sqlLoadTaskTemplate;
	}

	public void setSqlLoadTaskTemplateItem(String sqlLoadTaskTemplateItem) {
		this.sqlLoadTaskTemplateItem = sqlLoadTaskTemplateItem;
	}

	public void setSqlLoadAlertCodeArgs(String sqlLoadAlertCodeArgs) {
		this.sqlLoadAlertCodeArgs = sqlLoadAlertCodeArgs;
	}

	public void setMapperLoadRtu(ResultMapper<BizRtu> mapperLoadRtu) {
		this.mapperLoadRtu = mapperLoadRtu;
	}

	public void setMapperLoadMeasurePoints(
			ResultMapper<MeasuredPoint> mapperLoadMeasurePoints) {
		this.mapperLoadMeasurePoints = mapperLoadMeasurePoints;
	}

	public void setMapperLoadAlertCode(
			ResultMapper<RtuAlertCode> mapperLoadAlertCode) {
		this.mapperLoadAlertCode = mapperLoadAlertCode;
	}

	public void setMapperLoadAlertCodeArgs(
			ResultMapper<RtuAlertCodeArg> mapperLoadAlertCodeArgs) {
		this.mapperLoadAlertCodeArgs = mapperLoadAlertCodeArgs;
	}

	public void setMapperLoadRtuTask(ResultMapper<RtuTask> mapperLoadRtuTask) {
		this.mapperLoadRtuTask = mapperLoadRtuTask;
	}

	public void setMapperLoadTaskDbConfig(
			ResultMapper<TaskDbConfig> mapperLoadTaskDbConfig) {
		this.mapperLoadTaskDbConfig = mapperLoadTaskDbConfig;
	}

	public void setMapperLoadTaskTemplate(
			ResultMapper<TaskTemplate> mapperLoadTaskTemplate) {
		this.mapperLoadTaskTemplate = mapperLoadTaskTemplate;
	}

	public void setMapperLoadTaskTemplateItem(
			ResultMapper<TaskTemplateItem> mapperLoadTaskTemplateItem) {
		this.mapperLoadTaskTemplateItem = mapperLoadTaskTemplateItem;
	}

}
