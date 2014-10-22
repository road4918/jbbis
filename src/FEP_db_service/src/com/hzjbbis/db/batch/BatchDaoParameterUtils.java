/**
 * 为批量插入的DAO的附加SQL，提供附加参数功能。
 */
package com.hzjbbis.db.batch;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.db.batch.dao.IBatchDao;

/**
 * @author bhw
 *
 */
public class BatchDaoParameterUtils {
	private static BatchDaoParameterUtils instance;
	private List<IBatchDao> batchDaoList = new ArrayList<IBatchDao>();
	private Object additiveParameter;

	private BatchDaoParameterUtils(){
		instance = this;
	}
	
	public static BatchDaoParameterUtils getInstance(){
		if( null == instance )
			instance = new BatchDaoParameterUtils();
		return instance;
	}
	
	public void setBatchDaoList(List<IBatchDao> batchDaoList) {
		this.batchDaoList = batchDaoList;
		if( null != additiveParameter ){
			for(IBatchDao dao: batchDaoList )
				dao.setAdditiveParameter(additiveParameter);
		}
	}
	
	public void setAdditiveParameter(Object additiveParameter){
		this.additiveParameter = additiveParameter;
		for(IBatchDao dao: batchDaoList )
			dao.setAdditiveParameter(additiveParameter);
	}
}
