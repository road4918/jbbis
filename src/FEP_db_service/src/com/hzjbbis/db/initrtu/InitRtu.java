/**
 * ��ʼ������RTU����
 * ͨ��ǰ�û���ҵ����������Ҫ���ص������ǲ�һ���ġ������Ҫ�ֱ���ء�
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
			log.error("��ʼ���ն�ͨ�Ų����쳣��"+exp.getLocalizedMessage(),exp);
			return null;
		}
	}

	public void setComRtuDao(ComRtuDao comRtuDao) {
		this.comRtuDao = comRtuDao;
	}

}
