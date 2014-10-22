package com.hzjbbis.db.batch.dao.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.db.batch.dao.IBatchDao;
import com.hzjbbis.fk.tracelog.TraceLog;


/**
 * 思路：
 * 	  1）每个业务逻辑Service包含多个带Key的DAO对象。
 * 	  2）业务逻辑在线程池中执行报文处理，然后把结果放入到相应DAO对象；
 * 	  3）在业务逻辑线程中，必须定时（delay秒）调用add(null);
 * @author bhw
 * 2008-10-22 23:49
 */
public class JdbcBatchDao implements IBatchDao {
	private static final Logger log = Logger.getLogger(JdbcBatchDao.class);
	//数据库操作跟踪日志
	private static final TraceLog tracer = TraceLog.getTracer("jdbcBatchdao");
	//可配置属性
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	private DataSource dataSource;						//用于DbMonitor
	private String sql,sqlAlt,additiveSql;
	private Object additiveParameter;
	private int key = 0;
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
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
	}

	private int[] batchUpdateByPojo(String sqlStr,List<Object> pojoList){
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoList.toArray());
		int[] updateCounts = simpleJdbcTemplate.batchUpdate(sqlStr,batch);
		if( null != this.additiveSql )
			simpleJdbcTemplate.update(this.additiveSql);
		return updateCounts;
	}

	private int[] batchUpdateByParams(String sqlStr,List<Object[]> arrayList){
		int[] updateCounts =  simpleJdbcTemplate.batchUpdate(sqlStr,arrayList);
		if( null != this.additiveSql )
			simpleJdbcTemplate.update(this.additiveSql);
		return updateCounts;
	}

	private void _doBatchUpdate(){
		if( log.isDebugEnabled())
			log.debug("开始执行Dao，key="+key+",sql="+sql);
		int[] result = null;
		long time0 = System.currentTimeMillis();
		if( objList.size()>0 ){
			result = batchUpdateByPojo( sql,objList );
			long timeTake = System.currentTimeMillis() - time0;
			if( timeTake> 2000 )
				tracer.trace("batchUpdate takes(milliseconds):"+timeTake);
			if (log.isDebugEnabled())
				log.debug("key="+key+",成功条数="+ result.length +",花费毫秒="+timeTake);

			if( null != sqlAlt ){
				List<Object> listAlt = new ArrayList<Object>();
				for(int i=0; i<result.length; i++ ){
					if( result[i]<=0 ){
						listAlt.add(objList.get(i));
					}
				}
				if( listAlt.size()>0 )
					batchUpdateByPojo(sqlAlt,listAlt);
			}
			lastIoTime = System.currentTimeMillis();
		}
		else if( paramArrayList.size()>0 ){
			result = batchUpdateByParams(sql,paramArrayList );
			long timeTake = System.currentTimeMillis() - time0;
			if (log.isDebugEnabled())
				log.debug("key="+key+",成功条数="+ result.length +",花费毫秒="+timeTake);

			if( null != sqlAlt ){
				List<Object[]> listAlt = new ArrayList<Object[]>();
				for(int i=0; i<result.length; i++ ){
					if( result[i]<=0 ){
						listAlt.add(paramArrayList.get(i));
					}
				}
				if( listAlt.size()>0 )
					batchUpdateByParams(sqlAlt,listAlt);
			}
			lastIoTime = System.currentTimeMillis();
		}
	}
	
	public void batchUpdate() {
		if( executing )
			return;
		synchronized(batchDaoLock){
			//数据库不可用，则直接退出批量保存。
			DbMonitor dm = DbMonitor.getMonitor(dataSource);
			if( null != dm && !dm.isAvailable() )
				return;
			if( null != executeThreadName ){
				log.error("BatchDao[key="+key+"] has already been executed by : "+ executeThreadName);
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
				//语法错误，必须纠正。正确情况下，系统不应该打印此错误信息
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

	public boolean add(Object pojo) {
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

	public Object getAdditiveParameter() {
		return additiveParameter;
	}
}
