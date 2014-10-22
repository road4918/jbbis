/**
 * 当某个DAO已经插入部分数据对象，个数不足一个批次。
 * 那么最多延迟一定时间后，就需要保存，防止无限等待。
 */
package com.hzjbbis.db.batch.event;

import com.hzjbbis.db.batch.dao.IBatchDao;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author bhw
 * 2008-10-23
 */
public class BpBatchDelayEvent implements IEvent {
	private static final EventType type = EventType.BP_BATCH_DELAY;
	private IBatchDao dao;

	public BpBatchDelayEvent( IBatchDao dao ){
		this.dao = dao;
	}
	
	public IMessage getMessage() {
		return null;
	}

	public Object getSource() {
		return null;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public IBatchDao getDao() {
		return dao;
	}

	public void setDao(IBatchDao dao) {
		this.dao = dao;
	}

}
