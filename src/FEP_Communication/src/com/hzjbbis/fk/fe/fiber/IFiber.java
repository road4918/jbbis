/**
 * �˳̽ӿڶ��塣
 * ���ڶ��̵߳���ִ��ĳЩ�����磺���UMS���ӣ������˳̼�����ʹ�������߳̿��Թ��������Ӳ�����
 */
package com.hzjbbis.fk.fe.fiber;

/**
 * @author bhw
 * 2008-10-28 23:48
 */
public interface IFiber {

	void runOnce();		//������ִ�еĺ���
	
	/**
	 * �Ƿ����˳̷�ʽ���С�
	 * @param isFiber
	 */
	void setFiber(boolean isFiber );
	boolean isFiber();
}
