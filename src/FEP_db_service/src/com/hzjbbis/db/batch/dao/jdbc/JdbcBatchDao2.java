package com.hzjbbis.db.batch.dao.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.db.batch.dao.IBatchDao;
import com.hzjbbis.db.batch.dao.jdbc.springwrap.NamedParameterUtils2;
import com.hzjbbis.fk.tracelog.TraceLog;


/**
 * 思路：
 * 	  1）每个业务逻辑Service包含多个带Key的DAO对象。
 * 	  2）业务逻辑在线程池中执行报文处理，然后把结果放入到相应DAO对象；
 * 	  3）在业务逻辑线程中，必须定时（delay秒）调用add(null);
 * @author bhw
 * 2008-10-22 23:49
 */
public class JdbcBatchDao2 implements IBatchDao {
	private static final Logger log = Logger.getLogger(JdbcBatchDao2.class);
	private static final TraceLog tracer = TraceLog.getTracer("jdbcBatchdao2");
	//可配置属性
	private BatchSimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	private DataSource dataSource;
	private String sql,sqlAlt,additiveSql;
	private Object additiveParameter;		//用于附加SQL的参数输入。
	private int key;
	private int batchSize = 2000;
	private long delay = 5000;		//不足批量，最迟5秒必须保存
	//内部属性
	private List<Object> objList = new ArrayList<Object>();
	private List<Object[]> paramArrayList = new ArrayList<Object[]>();
	private Object batchDaoLock = new Object();
	private long lastIoTime = System.currentTimeMillis();
	private String executeThreadName = null;
	private boolean executing = false;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new BatchSimpleJdbcTemplate(dataSource);
	}

	private int[] batchUpdateByPojo(String sqlStr,List<Object> pojoList){
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoList.toArray());
		int[] updateCounts ;
		if( null != this.additiveSql ){
			if( null != this.additiveParameter ){
				final SqlParameterSource sqlParaSource = new BeanPropertySqlParameterSource(additiveParameter);
				String sqlToUse = NamedParameterUtils2.substituteNamedParameters(additiveSql, sqlParaSource );
				updateCounts = simpleJdbcTemplate.batchUpdate(sqlStr,batch, sqlToUse);
			}
			else
				updateCounts = simpleJdbcTemplate.batchUpdate(sqlStr,batch, additiveSql);
		}
		else
			updateCounts = simpleJdbcTemplate.batchUpdate(sqlStr,batch);
		return updateCounts;
	}

	private int[] batchUpdateByParams(String sqlStr,List<Object[]> arrayList){
		int[] updateCounts;
		if( null != this.additiveSql )
			updateCounts =  simpleJdbcTemplate.batchUpdate(sqlStr,arrayList,additiveSql);
		else
			updateCounts =  simpleJdbcTemplate.batchUpdate(sqlStr,arrayList);
		return updateCounts;
	}

	private void _doBatchUpdate(){
		if( log.isInfoEnabled() )
			log.info("开始执行Dao，key="+key+",sql="+sql);
		int[] result = null;
		long time0 = System.currentTimeMillis();
		if( objList.size()>0 ){
			result = batchUpdateByPojo(sql,objList);
			if( log.isInfoEnabled() ){
/*				int failCount = 0;
				for( int i=0;i<result.length; i++ ){
					if( result[i] == 0 )
						failCount++;
				}
*/				long timeTake = System.currentTimeMillis() - time0;
				log.info("key="+key+",成功条数="+ result.length +",花费毫秒="+timeTake);
			}
			if( null != sqlAlt ){
				//in case insert failed, then update(sqlAlt) should be executed 
/*				List<Object> listAlt = new ArrayList<Object>();
				for(int i=0; i<result.length; i++ ){
					if( result[i] == 0 ){
						listAlt.add(objList.get(i));
					}
				}
				if( listAlt.size()>0 )
					batchUpdateByPojo(sqlAlt,listAlt);
*/			}
			lastIoTime = System.currentTimeMillis();
		}
		else if( paramArrayList.size()>0 ){
			result = batchUpdateByParams(sql,paramArrayList );
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				log.info("key="+key+",成功条数="+result.length+",花费毫秒="+timeTake);
			}
			lastIoTime = System.currentTimeMillis();
		}
	}
	
	public void batchUpdate() {
		if( executing )
			return;
		synchronized(batchDaoLock){
			DbMonitor dm = DbMonitor.getMonitor(dataSource);
			if( null != dm && !dm.isAvailable() )
				return;
			if( null != executeThreadName ){
				log.error("BatchDao2[key="+key+"] has already been executed by : "+ executeThreadName);
			}
			executeThreadName = Thread.currentThread().getName();
			boolean success = false;
			try{
				executing = true;
				_doBatchUpdate();
				success = true;
			}
			catch(CannotGetJdbcConnectionException e){
				//数据库连接异常，通知监控模块
				if( null != dm )
					dm.setAvailable(false);
			}
			catch(BadSqlGrammarException e){
				tracer.trace(e.getLocalizedMessage(),e);
			}
			catch(Exception e){
				tracer.trace("batch dao exception:"+e.getLocalizedMessage(),e);
				log.warn("batch dao exception:"+e.getLocalizedMessage(),e);
			}
			finally{
				executing = false;
				if( success ){
					this.objList.clear();
					this.paramArrayList.clear();
				}
			}
			executeThreadName = null;
		}
	}

	public int getKey() {
		return key;
	}

	public void setKey(int k){
		key = k;
	}

	public boolean add(Object pojo){
		if( null != pojo ){
			synchronized(batchDaoLock){
				int above = size()- batchSize;
				if( above > batchSize || above > 3000 ){
					tracer.trace("batchDao can not add object,size="+size()+",batchSize="+batchSize);
					return false;
				}
				objList.add(pojo);
			}
			if( size()>= batchSize )
				batchUpdate();
		}
		else{
			delayExec();
		}
		return true;
	}

	/**
	 * Object[] params,代表的是表的一行数据。其顺序按照配置定义。
	 */
	public boolean add(Object[] params){
		if( null != params ){
			synchronized(batchDaoLock){
				int above = size()- batchSize;
				if( above > batchSize || above > 3000 ){
					tracer.trace("batchDao can not add object,size="+size()+",batchSize="+batchSize);
					return false;
				}
				paramArrayList.add(params);
			}
			if( size()>= batchSize )
				batchUpdate();
		}
		else{
			delayExec();
		}
		return true;
	}

	public void setSql(String sql) {
		this.sql = sql.trim();
	}

	public void setSqlAlt(String sqlAlt){
		this.sqlAlt = sqlAlt.trim();
	}
	
	public void setAdditiveSql(String adSql ){
		adSql = StringUtils.strip(adSql);
		adSql = StringUtils.remove(adSql, '\t');
		adSql = StringUtils.replaceChars(adSql, '\n', ' ');
		this.additiveSql = adSql;
	}
	
	public int size(){
		return Math.max(objList.size(), paramArrayList.size());
	}
	
	public void setBatchSize(int batchSize){
		this.batchSize = batchSize;
	}
	
	public long getLastIoTime(){
		return lastIoTime;
	}
	
	public void setDelaySecond(int delaySec){
		delay = delaySec*1000;
	}
	
	public long getDelayMilliSeconds(){
		return delay;
	}
	
	public boolean hasDelayData(){
		DbMonitor dm = DbMonitor.getMonitor(dataSource);
		boolean result = System.currentTimeMillis() - lastIoTime >= delay && size()>0 ;
		if( null != dm )
			result = result && dm.isAvailable();
		return result;
	}
	
	private void delayExec(){
		if( hasDelayData() ){
			batchUpdate();
		}
	}

	public void setAdditiveParameter(Object additiveParameter) {
		this.additiveParameter = additiveParameter;
	}
}
