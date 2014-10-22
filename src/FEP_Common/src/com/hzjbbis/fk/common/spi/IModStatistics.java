/**
 * ͨ��ģ���ͳ����Ϣ
 */
package com.hzjbbis.fk.common.spi;

/**
 * @author bhw
 *
 */
public interface IModStatistics {
	/**
	 * ���涨��ͨ��ģ��ͳ����Ϣ
	 */
	long getLastReceiveTime();
	long getLastSendTime();
	long getTotalRecvMessages();
	long getTotalSendMessages();
	int getMsgRecvPerMinute();
	int getMsgSendPerMinute();
}
