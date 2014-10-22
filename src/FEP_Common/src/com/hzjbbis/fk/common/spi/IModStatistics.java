/**
 * 通信模块的统计信息
 */
package com.hzjbbis.fk.common.spi;

/**
 * @author bhw
 *
 */
public interface IModStatistics {
	/**
	 * 下面定义通信模块统计信息
	 */
	long getLastReceiveTime();
	long getLastSendTime();
	long getTotalRecvMessages();
	long getTotalSendMessages();
	int getMsgRecvPerMinute();
	int getMsgSendPerMinute();
}
