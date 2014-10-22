/**
 * ͨ���̳߳ء�
 * �̳߳ز����������ƣ��Ա������١��̳߳���Ҫ֧�ּ�ؼ�����
 * 
 */
package com.hzjbbis.fk.common.threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.EventQueue;
import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.events.event.EventHandleTimeoutAlarm;
import com.hzjbbis.fk.common.events.event.KillThreadEvent;
import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.State;

/**
 * @author bao
 * 2008��04��09 11��22
 */
public class ThreadPool implements ITimerFunctor{
	//��̬����
	private static final Logger log = Logger.getLogger(ThreadPool.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	
	/**
	 * �̳߳����֣��Ա���ģ���ظ��̳߳ص�ִ��״������̬map��ά��Ψһ���̳߳����֡�
	 */
	private String name = "threadpool";
	private static int poolSeq = 1;
	private int timeoutAlarm = 2;		//�¼�����ʱ�䳬ʱ���棬�����ݱ��ֳ���2�룬��Ҫ�澯��
	
	private int minSize = 1;		//�̳߳���С����
	private int maxSize = 20;		//�̳߳�������
	private int timeout = 200;		//���������������timeout�¼���û��ȡ�������������ݣ��򴥷�����ִ���¼���
	
	//�����ڲ�״̬
	private volatile State state = new State();
	private IEventHandler executer = null; //�̳߳ص�ִ�к���
	private List<WorkThread> works = Collections.synchronizedList( new ArrayList<WorkThread>() );
	private int threadPriority = Thread.NORM_PRIORITY;
	private EventQueue eventQueue;
	
	/**
	 * ���캯��
	 * @param exec �������̵߳�ִ�ж���
	 * @param queue: �¼�����
	 */
	public ThreadPool(IEventHandler exec,EventQueue queue){
		executer = exec;
		eventQueue = queue;
	}
	
	public boolean start(){
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		
		forkThreads(minSize);
		while( works.size()< minSize ){
			Thread.yield();
			try{
				Thread.sleep(100);
			}catch(Exception exp){}
		}
		//������ʱ����
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,30));	//��ʱ��0��ÿ30�붨ʱ��
		state = State.RUNNING;
		if( log.isDebugEnabled() )
			log.debug("�̳߳ء�"+name+"�������ɹ���,size="+minSize);
		return true;
	}
	
	public void stop(){
		state = State.STOPPING;
		synchronized(works){
			for(WorkThread work: works){
				work.interrupt();
			}
		}
		int cnt = 40;
		while(cnt-->0 && works.size()>0 ){
			Thread.yield();
			try{
				Thread.sleep(50);
			}
			catch(Exception e){}
			if( cnt< 20 )
				continue;
			synchronized(works){
				for(WorkThread work: works){
					work.interrupt();
				}
			}
		}
		if( log.isDebugEnabled() )
			log.debug("�̳߳ء�"+name+"��ֹͣ��,�����߳���="+works.size());
		works.clear();
		state = State.STOPPED;
	}
	
	public String profile(){
		StringBuffer sb = new StringBuffer(256);
		sb.append("<threadpool name=\"").append(name).append("\">");
			sb.append("<minSize value=\"").append(minSize).append("\"/>");
			sb.append("<maxSize value=\"").append(maxSize).append("\"/>");
			sb.append("<size value=\"").append(size()).append("\"/>");
			sb.append("<timeoutAlarm value=\"").append(timeoutAlarm).append("\"/>");
			sb.append("<works>").append(this.toString()).append("</works>");
		sb.append("</threadpool>");
		return sb.toString();
	}
	
	public boolean isActive(){
		return state.isActive();
	}
	
	public int size(){
		return works.size();
	}
	
	public void onTimer(int id){
		if( 0 == id ){
			ArrayList<WorkThread> list = new ArrayList<WorkThread>(works);
			int count =0;
			for(WorkThread work: list){
				if( work.checkTimeout() ){
					//��ʱ��
					count++;
				}
			}
		}
	}
	
	private void forkThreads(int delta) {
		if (delta == 0)
			return;

		if (delta > 0) {
			//���ܳ������ֵ
			int maxDelta = this.maxSize - works.size();
			delta = Math.min(maxDelta, delta);
			if( log.isDebugEnabled() && 1 ==  delta )
				log.debug("�����̳߳ش�С(+1)");
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//����С��1
			delta = -delta;
			int n = works.size() - minSize;		//���������ٵ��߳���
			delta = Math.min(delta, n);
			if( log.isDebugEnabled() && -1 == delta )
				log.debug("�����̳߳ش�С(-1)");
			for (; delta > 0; delta--) {
				try{
					eventQueue.addFirst(new KillThreadEvent());
				}catch(Exception exp){
					log.error(exp.getLocalizedMessage());
				}
			}
		}
	}
	
	private void justThreadSize(){
		int n = eventQueue.size();
		if( n> 1000 ){
			forkThreads(1);
		}
		else if( n< 2 ){
			forkThreads(-1);
		}
	}
	
	private class WorkThread extends Thread{
		long beginTime;
		boolean busy = false;		//�ж�ʵ���Ƿ��ڹ���״̬
		IEvent currentEvent = null;
		public WorkThread(){
			super(ThreadPool.this.name+"."+poolSeq++);
			super.start();
		}

		public void run() {
			synchronized(works){
				works.add(this);
			}
			this.setPriority(threadPriority);
			int count=0;		//ÿ����1000���¼�����̳߳��Ƿ���Ҫ����
			log.info("threadpool.work running:"+this.getName());
			while( !ThreadPool.this.state.isStopping() && !ThreadPool.this.state.isStopped() ){
				try{
					busy = false;
					currentEvent = ThreadPool.this.eventQueue.take();
					if( null == currentEvent )		//JDK����ȱ�ݣ�Object.wait()����ͻȻ����
						continue;
					if( currentEvent.getType() == EventType.SYS_KILLTHREAD )
						break;
					processEvent(currentEvent);
					//���������¼����������̫�࣬�����̡߳����Ϊ0�������߳�
					count++;
					if( count>500 ){
						justThreadSize();
						count = 0;
					}
				}catch(Exception exp){
					//�̱߳��жϡ�����Ƿ���Ҫ�ر�
					continue;
				}
			}
			synchronized(works){
				works.remove(this);
			}
			log.info("�̳߳صĹ����߳��˳�:"+this.getName());
		}
		
		/**
		 * �¼�������̡���Ҫ����¼�����ʱ����������
		 * @param event
		 */
		private void processEvent(IEvent event){
			beginTime = System.currentTimeMillis();
			busy = true;
			executer.handleEvent(event);
			long endTime = System.currentTimeMillis();
			if( endTime-beginTime > timeoutAlarm*1000 ){
				//�¼�����ʱ�澯
				EventHandleTimeoutAlarm ev = new EventHandleTimeoutAlarm(event);
				ev.setBeginTime(beginTime);
				ev.setEndTime(endTime);
				ev.setThreadName(WorkThread.this.getName());
				GlobalEventHandler.postEvent(ev);
			}
		}
		
		public boolean checkTimeout(){
			if( !busy )
				return false;
			long endTime = System.currentTimeMillis();
			if( endTime-beginTime > timeoutAlarm*1000 ){
				//�¼�����ʱ�澯
				EventHandleTimeoutAlarm ev = new EventHandleTimeoutAlarm(currentEvent);
				ev.setBeginTime(beginTime);
				ev.setEndTime(endTime);
				ev.setStackTraceElement(this.getStackTrace());
				tracer.trace(ev);
				GlobalEventHandler.postEvent(ev);
				this.interrupt();
			}
			return true;
		}
		
		public String toString(){
			String busyStatus = "idle";
			if( busy ){
				long timeConsume = System.currentTimeMillis()-beginTime;
				busyStatus = "��ǰ����ʱ��(����):"+timeConsume;
			}
			return "["+getName()+","+ busyStatus + "];";
		}
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	public int getTimeoutAlarm() {
		return timeoutAlarm;
	}

	public void setTimeoutAlarm(int timeoutAlarm) {
		this.timeoutAlarm = timeoutAlarm;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getThreadPriority() {
		return threadPriority;
	}

	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(256);
		sb.append(name);
		try{
			for(WorkThread work: works){
				sb.append(work);
			}
		}catch(Exception e){
			return "";
		}
		return sb.toString();
	}
}
