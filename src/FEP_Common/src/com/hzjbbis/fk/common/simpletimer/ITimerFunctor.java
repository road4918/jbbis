/**
 * ��ʱ��ִ�еĺ�������
 */
package com.hzjbbis.fk.common.simpletimer;

/**
 * @author bhw
 *
 */
public interface ITimerFunctor {
	/**
	 * ��ʱ�������ĺ�����
	 * ÿ��������������ʱ������ID�������֡�
	 * @param id���ĸ���ʱ����������
	 */
	void onTimer(int id);
}
