/**
 * ��TimerSchedulerʹ�á�
 */
package com.hzjbbis.fk.common.simpletimer;

/**
 * @author bhw
 *
 */
public class TimerData {
	//���ò���
	private ITimerFunctor functor;
	private int id=0;
	private long period = 60*1000;	//������룩

	public TimerData(){}
	
	public TimerData(ITimerFunctor src,int id, long period ){
		this.functor = src;
		this.id = id;
		this.period = period*1000;
	}
	
	//��TimerShcedulerʹ�õĲ���
	private long lastActivate = System.currentTimeMillis();

	public ITimerFunctor getFunctor() {
		return functor;
	}

	public void setFunctor(ITimerFunctor functor) {
		this.functor = functor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period*1000;
	}
	
	/**
	 * ���ʱ������
	 */
	public void activate(){
		lastActivate = System.currentTimeMillis();
		functor.onTimer(id);
	}
	
	/**
	 * ���ر���ʱ�����봥����ʣ��ĺ�������
	 * @return
	 */
	public long distance(){
		return lastActivate+period - System.currentTimeMillis();
	}
}
