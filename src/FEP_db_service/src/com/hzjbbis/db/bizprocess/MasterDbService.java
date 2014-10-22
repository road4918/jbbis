package com.hzjbbis.db.bizprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.procedure.DbProcedure;
import com.hzjbbis.db.resultmap.ResultMapper;
import com.hzjbbis.fk.model.RtuCmdItem;

/**
 * ҵ���������õ���վ���ݿ���صĲ�����
 * @author bhw
 *
 */
public class MasterDbService {
	private static final Logger log = Logger.getLogger(MasterDbService.class);
	//Spring���õ�����
	private DataSource dataSource;
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String insertCommandResult;			//�����������
	private String insertRtuComdMag;			//�ն˲��������������
	private DbProcedure funcGetRtuCommandSeq;	//�����ݿ��ȡ�ն�����������š�
	private DbProcedure procUpdateCommandStatus;//������վ��������״̬
	private DbProcedure procUpdateParamResult;	//������վ��������Ĳ������ý��
	private DbProcedure procPostCreateRtuAlert;	//�쳣����洢����
	private DbProcedure procPostCreateRtuData;	//�������洢����
	private String sqlGetRtuComdItem;			//�ն˲������������ѯ
	private ResultMapper<RtuCmdItem> mapperGetRtuComdItem;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
	}

	public int insertCommandResult(Object obj){
		BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
		return simpleJdbcTemplate.update(this.insertCommandResult, ps);
	}

	public void setInsertCommandResult(String insertCommandResult) {
		this.insertCommandResult = insertCommandResult;
	}

	public void insertRtuComdMag(Object obj){
		try{
			BeanPropertySqlParameterSource ps = new BeanPropertySqlParameterSource(obj);
			simpleJdbcTemplate.update(this.insertRtuComdMag, ps);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public List<RtuCmdItem> getRtuComdItem(String zdljdz,int mlxh) {
		ParameterizedRowMapper<RtuCmdItem> rm = new ParameterizedRowMapper<RtuCmdItem>(){
			public RtuCmdItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperGetRtuComdItem.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(this.sqlGetRtuComdItem, rm, zdljdz,mlxh);
	}

	public int getRtuCommandSeq(String strRTUA){
		try{
			return this.funcGetRtuCommandSeq.executeFunctionInt(strRTUA);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		return 1;
	}

	public void procUpdateCommandStatus(Object param){
		try{
			procUpdateCommandStatus.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void procUpdateParamResult(Object param){
		try{
			procUpdateParamResult.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	public void procPostCreateRtuAlert(Object param){
		try{
			procPostCreateRtuAlert.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	public void procPostCreateRtuData(Object param){
		try{
			procPostCreateRtuData.execute(param);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public void setFuncGetRtuCommandSeq(DbProcedure funcGetRtuCommandSeq) {
		this.funcGetRtuCommandSeq = funcGetRtuCommandSeq;
	}

	public void setProcUpdateCommandStatus(DbProcedure procUpdateCommandStatus) {
		this.procUpdateCommandStatus = procUpdateCommandStatus;
	}

	public void setProcUpdateParamResult(DbProcedure procUpdateParamResult) {
		this.procUpdateParamResult = procUpdateParamResult;
	}

	public void setInsertRtuComdMag(String insertRtuComdMag) {
		this.insertRtuComdMag = insertRtuComdMag;
	}

	public void setSqlGetRtuComdItem(String sqlGetRtuComdItem) {
		this.sqlGetRtuComdItem = sqlGetRtuComdItem;
	}

	public void setMapperGetRtuComdItem(
			ResultMapper<RtuCmdItem> mapperGetRtuComdItem) {
		this.mapperGetRtuComdItem = mapperGetRtuComdItem;
	}

	public void setProcPostCreateRtuAlert(DbProcedure procPostCreateRtuAlert) {
		this.procPostCreateRtuAlert = procPostCreateRtuAlert;
	}

	public void setProcPostCreateRtuData(DbProcedure procPostCreateRtuData) {
		this.procPostCreateRtuData = procPostCreateRtuData;
	}


}
