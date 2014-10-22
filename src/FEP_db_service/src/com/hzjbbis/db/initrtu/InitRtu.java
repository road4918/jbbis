/**
 * 初始化加载RTU对象。
 * 通信前置机与业务处理器所需要加载的属性是不一样的。因此需要分别加载。
 */
package com.hzjbbis.db.initrtu;

import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.db.initrtu.dao.ComRtuDao;
import com.hzjbbis.fk.model.ComRtu;

/**
 * @author bhw
 *
 */
public class InitRtu {
	private static final Logger log = Logger.getLogger(InitRtu.class);
	private ComRtuDao comRtuDao;

	public List<ComRtu> loadComRtu(){
		try{
			return comRtuDao.loadComRtu();
		}catch(Exception exp){
			log.error("初始化终端通信参数异常："+exp.getLocalizedMessage(),exp);
			return null;
		}
	}

	public void setComRtuDao(ComRtuDao comRtuDao) {
		this.comRtuDao = comRtuDao;
	}

}
