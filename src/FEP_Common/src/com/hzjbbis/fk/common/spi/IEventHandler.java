/**
 * 线程池所管理的工作单元接口。
 */
package com.hzjbbis.fk.common.spi;

/**
 * @author bhw
 * 2008-03-18 
 */
public interface IEventHandler{
	/**
	 * 提供事件处理函数
	 */
	void handleEvent(IEvent event);
	
}
