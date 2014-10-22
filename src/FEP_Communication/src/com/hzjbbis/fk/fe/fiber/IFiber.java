/**
 * 纤程接口定义。
 * 用于多线程调度执行某些任务，如：多个UMS连接，采用纤程技术，使得少数线程可以管理多个连接操作。
 */
package com.hzjbbis.fk.fe.fiber;

/**
 * @author bhw
 * 2008-10-28 23:48
 */
public interface IFiber {

	void runOnce();		//被调度执行的函数
	
	/**
	 * 是否以纤程方式运行。
	 * @param isFiber
	 */
	void setFiber(boolean isFiber );
	boolean isFiber();
}
