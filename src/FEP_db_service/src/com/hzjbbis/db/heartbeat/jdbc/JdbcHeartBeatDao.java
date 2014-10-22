package com.hzjbbis.db.heartbeat.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.heartbeat.HeartBeat;
import com.hzjbbis.db.heartbeat.HeartBeatDao;
import com.hzjbbis.db.heartbeat.HeartBeatLog;


public class JdbcHeartBeatDao implements HeartBeatDao{
	
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private DataSource dataSource;

	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);	
		this.dataSource = dataSource;
	}
	/**
	 * 每周末更新掉上个weektag的数据
	 */
	public boolean doInit(int weekNo, int weekNum) throws SQLException
	{
		StringBuffer sb = new StringBuffer();
		sb.append("update heartbeatmessage set weekofyear = ").append(weekNo);
		sb.append(",heartbeattime = ").append(System.currentTimeMillis());
		for (int i = 1; i < 36; i++) {
			sb.append(",d").append(i).append("=0");
		}// end for
		sb.append(" where weektag = ").append(weekNo % weekNum);
		int r = simpleJdbcTemplate.update(sb.toString());
		if(r == 0) return false;
		else return true;
	}
	/**
	 * 获取更新日志数据
	 */
	@SuppressWarnings("unchecked")
	public List<HeartBeatLog> getLogResult(int weekNO) throws SQLException{
		StringBuffer sb = new StringBuffer();
		sb.append("select id,issuccess from log where weekno = ").append(weekNO);
		RowProcess	rp = new RowProcess();
		List<HeartBeatLog> list = simpleJdbcTemplate.query(sb.toString(), rp);
		return list;
	}
	/**
	 * 更新日志文件
	 */
	public void updateLogResult(boolean b,long curTime,int id,int nInitWeekNo) throws SQLException{
		StringBuffer sql = new StringBuffer();
		sql.append("update log set issuccess=?, startime = ?,endtime = ? where id = ? and weekno = ?");
		Object[] object = {b,curTime,System.currentTimeMillis(),id,nInitWeekNo};
		simpleJdbcTemplate.update(sql.toString(), object);
	}
	/**
	 * 
	 */
	public void insertLogResult(int nInitWeekNo, long curTime) throws SQLException{
		StringBuffer sql = new StringBuffer();
		sql.append("insert into log (`weekno`,`issuccess`,`startime`,`endtime`) value(?, ?, ?, ?);");
		Object[] object = {nInitWeekNo,1,curTime,System.currentTimeMillis()};
		simpleJdbcTemplate.update(sql.toString(), object);
	}
	/**
	 * 更新心跳
	 */
	public int[] batchUpdate(final List<HeartBeat> orHeartBeats,int columnIndex){
		StringBuffer sb = new StringBuffer();
		sb.append("update heartbeatmessage set deptCode = ? , weekOfYear = ?,heartBeatTime = ?,");
		sb.append("d").append(columnIndex).append("=").append("d").append(columnIndex).append("|(?)");
		sb.append(" where rtua=? and weektag=?");
//		SqlParameterSource[] sqlParameter = SqlParameterSourceUtils.createBatch(orHeartBeats.toArray());
//		int[] rs = simpleJdbcTemplate.batchUpdate(sb.toString(), sqlParameter);
//		return rs;
		 JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); 
         int[] i = jdbcTemplate.batchUpdate(sb.toString(),new BatchPreparedStatementSetter(){  
	         public int getBatchSize() { 
	      // TODO Auto-generated method stub 
	        	 return orHeartBeats.size(); 
	         } 
		      public void setValues(PreparedStatement arg0, int arg1) throws SQLException { 
		      // TODO Auto-generated method stub 
		    	  HeartBeat heartBeat =(HeartBeat) orHeartBeats.get(arg1); 
		    	  arg0.setString(1, heartBeat.getDeptCode());
		    	  arg0.setInt(2, heartBeat.getWeekOfYear()); 
		    	  arg0.setString(3, heartBeat.getValueOrigin()); 
		    	  arg0.setLong(4, heartBeat.getValue());
		    	  arg0.setString(5, heartBeat.getRtua());
		    	  arg0.setInt(6, heartBeat.getWeekTag());
		      } 
         });
		return i;
	}
	/**
	 * 插入心跳
	 */
	public void batchInsert(final List<HeartBeat> insertHeartBeats,
			int columnIndex){
		StringBuffer sb = new StringBuffer();
		sb.append("insert into heartbeatmessage(rtua,deptCode,weekOfYear");
		sb.append(",weektag,heartBeatTime,d").append(columnIndex);
		sb.append(") values(?,?,?,?,?,?)");
//		SqlParameterSource[] sqlParameter = SqlParameterSourceUtils.createBatch(insertHeartBeats.toArray());
//		simpleJdbcTemplate.batchUpdate(sb.toString(), sqlParameter);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); 
        jdbcTemplate.batchUpdate(sb.toString(),new BatchPreparedStatementSetter(){  
	         public int getBatchSize() { 
	      // TODO Auto-generated method stub 
	        	 return insertHeartBeats.size(); 
	         } 
		      public void setValues(PreparedStatement arg0, int arg1) throws SQLException { 
		      // TODO Auto-generated method stub 
		    	  HeartBeat heartBeat =(HeartBeat) insertHeartBeats.get(arg1); 
		    	  arg0.setString(1, heartBeat.getRtua());
		    	  arg0.setString(2, heartBeat.getDeptCode());
		    	  arg0.setInt(3, heartBeat.getWeekOfYear()); 
		    	  arg0.setInt(4, heartBeat.getWeekTag());
		    	  arg0.setString(5, heartBeat.getValueOrigin()); 
		    	  arg0.setLong(6, heartBeat.getValue());  
		      } 
        });
		
	}
	
	public class RowProcess implements ParameterizedRowMapper<HeartBeatLog>{
		public HeartBeatLog mapRow(ResultSet rs, int rowNum) throws SQLException {
			// TODO Auto-generated method stub
			HeartBeatLog hlog = new HeartBeatLog();
			hlog.setId(rs.getInt(1));
			hlog.setIssuccess(rs.getBoolean(2));
			return hlog;
		}
		
	}
}


