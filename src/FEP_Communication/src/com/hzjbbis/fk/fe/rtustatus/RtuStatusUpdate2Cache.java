/**
 * �ն˹������µ����ݿ⡣
 * �������ִ�еĵ��ȣ���Spring����Quartz��ʵ�֡�
 */
package com.hzjbbis.fk.fe.rtustatus;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.fe.filecache.RtuStatusCache;
import com.hzjbbis.fk.model.RtuManage;

/**
 * @author bhw
 *
 */
public class RtuStatusUpdate2Cache {
	private static final Logger log = Logger.getLogger(RtuStatusUpdate2Cache.class);
	//���µ����ݿ��Dao����
	/**
	 * ���ڸ��µ������ļ���Ϊ�˷�ֹ���ݶ�ʧ�������Ƶ�Ƚϸߡ�
	 */
	public void update2Cache(){
		log.info("���ڸ����ն�״̬��Ϣ�������ļ���");
		RtuStatusCache.save2File(RtuManage.getInstance().getAllComRtu());
	}
}
