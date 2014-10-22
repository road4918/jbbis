/**
 * ����JDBC���ṩ�洢���̻��ߺ����ķ�װ��
 */
package com.hzjbbis.db.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.hzjbbis.db.resultmap.ResultMapper;

/**
 * @author bhw
 *
 */
public class DbProcedure {
	private static final Logger log = Logger.getLogger(DbProcedure.class);
	//����������
	private DataSource dataSource;
	private List<ProcParam> outParams = new ArrayList<ProcParam>();
	private List<ProcParam> inParams = new ArrayList<ProcParam>();
	private String callString;
	//�ڲ�����
	private boolean initialized = false;
	private final Object lock = new Object();
	
	/**
	 * ȱʡ���캯������Ҫ�������ԣ�Ȼ�����initialize()���г�ʼ����
	 */
	public DbProcedure(){}
	
	public DbProcedure(DataSource ds,String sqlConfig){
		dataSource = ds;
		callString = sqlConfig;
		checkInitialize();
	}
	
	private void initialize(String sqlConfig){
		// sql example:
		//{call comm_service.update_command_status(#id,jdbcType=NUMERIC,mode=IN#, #status,jdbcType=VARCHAR,mode=IN#, #errcode,jdbcType=VARCHAR,mode=IN#,#messageCount,jdbcType=NUMERIC,mode=IN#)}
		try{
			callString = compile(sqlConfig);			
			if( log.isDebugEnabled() )
				log.debug("call string="+callString);
		}catch(Exception e){
			log.error("sql�������:"+sqlConfig,e);
		}
	}
	
	private void checkInitialize(){
		synchronized(lock){
			if( !initialized ){
				initialized=true;
				initialize(callString);
			}
		}
	}
	
	private boolean isPrimitive(Object obj){
		Class<?> clz = obj.getClass();
		if( clz.isPrimitive() )
			return true;
		if( clz == String.class || clz == Integer.class || clz == Long.class || clz == Short.class
				|| clz == Character.class || clz == Byte.class || clz == Double.class || clz == Float.class )
			return true;
		if( obj instanceof Date )
			return true;
		return false;
	}
	
	public Object executeFunction(Object... args) throws SQLException{	
		checkInitialize();
		long time = System.currentTimeMillis();
		Connection con = DataSourceUtils.getConnection(dataSource);
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeFunctionȡ����ʱ��="+(time1-time));
		try{
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "�洢������Ҫ��������="+inParams.size()+",ʵ�ʴ������="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				if( isBean )
					pin.setInputValueByBean(procStmt, args[0]);
				else
					pin.setInputValue(procStmt, args[i]);
			}
			procStmt.execute();
			Object ret = procStmt.getObject(1);
			procStmt.close();
			return ret;
		}
		catch(SQLException e){
			log.error("�洢����ִ�д���:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	/**
	 * ִ�д洢���̡�����Ҫ����ֵ�������
	 * @param args
	 * @return ִ�гɹ�����ʧ�ܡ�
	 * @throws SQLException
	 */
	public boolean execute(Object... args) throws SQLException{
		checkInitialize();
		long time = System.currentTimeMillis();	
		Connection con = DataSourceUtils.getConnection(dataSource);		
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeȡ����ʱ��="+(time1-time));
		try{
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "�洢������Ҫ��������="+inParams.size()+",ʵ�ʴ������="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				if( isBean )
					pin.setInputValueByBean(procStmt, args[0]);
				else
					pin.setInputValue(procStmt, args[i]);
			}
			boolean ret = procStmt.execute();
			procStmt.close();
			return ret;
		}
		catch(SQLException e){
			log.error("�洢����ִ�д���:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
			if (log.isDebugEnabled())
				log.debug("�洢����ִ�����");
		}
	}

	/**
	 * ִ�д洢���̡�
	 * @param args
	 * @return �洢���̷���һ����¼��
	 * @throws SQLException
	 */
	private ResultSet executeResultSet(Object... args) throws SQLException{			
		checkInitialize();
		long time = System.currentTimeMillis();	
		Connection con = DataSourceUtils.getConnection(dataSource);
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeResultSetȡ����ʱ��="+(time1-time));
		try{
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "�洢������Ҫ��������="+inParams.size()+",ʵ�ʴ������="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				pin.setInputValue(procStmt, args[i]);
			}
			procStmt.execute();
			ResultSet rs = procStmt.getResultSet();
			return rs;
		}
		catch(SQLException e){
			log.error("�洢����ִ�д���:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	/**
	 * ִ�д洢���̡�
	 * @param args
	 * @return �洢���̷���һ����¼��
	 * @throws SQLException
	 */
	public List<?> executeList(ResultMapper<?>rm,Object... args) throws SQLException{
		ResultSet rs = executeResultSet(args);
		return rm.mapAllRows(rs);
	}

	public int executeFunctionInt(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String ){
			Long lv = Long.parseLong((String)ret);
			return lv.intValue();
		}
		else if( ret instanceof Integer )
			return (Integer)ret;
		else if( ret instanceof Long )
			return ((Long)ret).intValue();
		else if( ret instanceof Short )
			return (Short)ret;
		else
			throw new RuntimeException("�������Ͳ���ת����int");
	}
	
	public long executeFunctionLong(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String )
			return Long.parseLong((String)ret);
		else if( ret instanceof Integer )
			return (Integer)ret;
		else if( ret instanceof Long )
			return (Long)ret;
		else if( ret instanceof Short )
			return (Short)ret;
		else
			throw new RuntimeException("�������Ͳ���ת����long");
	}
	
	public String executeFunctionString(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String )
			return (String)ret;
		else
			return ret.toString();
	}
	
	private String compile(String sqlConfig){
		//����Ibatis���洢���̵�����
		sqlConfig = StringUtils.strip(sqlConfig);
		int index = sqlConfig.indexOf("{");
		if( index>0 )
			sqlConfig = sqlConfig.substring(index);
		index = sqlConfig.indexOf("#");
		if( index>0 ){
			int lp = sqlConfig.indexOf("(");
			int rp = sqlConfig.indexOf(")");
			StringBuffer sb = new StringBuffer();
			//��������߲���
			String lsql = sqlConfig.substring(0, lp+1);
			String sparam = sqlConfig.substring(lp+1,rp);
			//�������롢�������
			int iq = lsql.indexOf("?");
			int pindex = 1;
			if( iq >0 ){
				//��������,֧�ָ�ʽ?#NUMERIC= ����?:#NUMERIC
				sb.append("{?");
				int ieq = lsql.indexOf("=");
				sb.append(lsql.substring(ieq));
				String jtype = lsql.substring(iq+1,ieq);
				int escapteIndex = 0;
				for( escapteIndex=0;escapteIndex<jtype.length();escapteIndex++){
					char c = lsql.charAt(escapteIndex);
					if( c != ':' && c != '#' ){
						jtype = jtype.substring(escapteIndex);
						break;
					}
				}
				ProcParam retParam = new ProcParam("",jtype,"OUT",pindex++);
				outParams.add(retParam);
			}
			else
				sb.append(lsql);
			
			String[] parts = sparam.split(",");
			for( int i=0; i<parts.length; i +=3 ){
				String name = parts[i].substring(1);	//exp: #id
				String jtype = parts[i+1].substring("jdbcType=".length());
				String pmode = parts[i+2].substring(0,parts[i+2].length()-1);
				ProcParam param = new ProcParam(name,jtype,pmode,pindex++);
				if( param.getParamMode() == ProcParam.MODE_IN ){
					if( inParams.size()>0 )
						sb.append(",?");
					else
						sb.append("?");
					inParams.add(param);
				}
				else if( param.getParamMode() == ProcParam.MODE_OUT){
					outParams.add(param);
				}
				else if( param.getParamMode() == ProcParam.MODE_INOUT){
					if( inParams.size()>0 )
						sb.append(",?");
					else
						sb.append("?");
					inParams.add(param);
					ProcParam paramOut = new ProcParam(name,jtype,pmode,param.getParamIndex());
					outParams.add(paramOut);
				}
				else{
					String errinfo = "Ibatis��ʽ�洢�������ô���mode="+pmode;
					log.error(errinfo);
					throw new RuntimeException(errinfo);
				}
			}
			//�����������
			sb.append(")}");
			return sb.toString();
		}
		return sqlConfig;
	}

	public void setOutParams(List<ProcParam> outParams) {
		this.outParams = outParams;
	}

	public void setInParams(List<ProcParam> inParams) {
		this.inParams = inParams;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setCallString(String callString) {
		this.callString = callString;
	}
	
	
}
