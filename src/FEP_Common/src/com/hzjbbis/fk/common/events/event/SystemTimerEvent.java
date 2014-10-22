/**
 * ϵͳ����ʱ���¼������δ���ģʽ��
 */
package com.hzjbbis.fk.common.events.event;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.message.IMessage;

/**
 * @author hbao
 *
 */
public class SystemTimerEvent implements IEvent {
	//��̬����
	private static final Logger log = Logger.getLogger(SystemTimerEvent.class);
	private static final EventType type = EventType.SYS_TIMER;
	//�ӳ�ʱ����С���¼�������ǰ�档
	private static final ArrayList<SystemTimerEvent> events = new ArrayList<SystemTimerEvent>(128);
	static{
		final SysTimerThread timerThread = new SysTimerThread();
		timerThread.start();
	}

	//��ʱ���¼����ԡ�
	protected String name = "";
	protected IMessage message = null;
	protected Object source = null;
	protected int delay = 1;		//�ӳ�delay�롣
	private long beginTime = System.currentTimeMillis();

	public SystemTimerEvent(String name, Object source,IMessage msg, int delay ){
		this.name = name;
		this.source = source;
		this.message = msg;
		this.delay = delay*1000 ;
	}
	
	public IMessage getMessage() {
		return message;
	}

	public Object getSource() {
		return source;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
		source = src;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void schedule(SystemTimerEvent event){
		if( null == event )
			return;
		long now = System.currentTimeMillis();
		event.beginTime = now;
		synchronized(events){
			//����delay����˳�������¼���
			for(int i=0; i<events.size(); i++ ){
				SystemTimerEvent e = events.get(i);
				long remain = e.beginTime + e.delay - now;
				if( event.delay < remain ){
					events.add(i, event);
					events.notifyAll();
//					log.debug("timer event:"+event.name+",scheduled,delay="+event.delay);
					return;
				}
			}
			events.add(event);
			events.notifyAll();
//			log.debug("timer event:"+event.name+",scheduled,delay="+event.delay);
		}
	}
	
	/**
	 * ϵͳ��ʱ���̡߳�
	 * @author hbao
	 *
	 */
	static class SysTimerThread extends Thread{
		public SysTimerThread(){
			super("SysTimerThread");
			this.setDaemon(true);
		}
		
		/**
		 * ����events��ȷ����ʱ�¼��ܹ����͵�GlobalEventHandler��
		 */
		@Override
		public void run() {
			int cnt = 0;		//�����ѭ��������ģʽʹ�ã����������У�Ҳ��Ӱ��Ч�ʡ�
			long checkTime = 0;
			log.info("ϵͳ��ʱ���ػ��߳̿�ʼ����...");
			while(true){
				synchronized(events){
					try{
						if( 0 == checkTime )
							checkTime = System.currentTimeMillis();
						dealList();
						cnt++;
					}
					catch(InterruptedException e){
					}
					catch(Exception e){
						log.warn(e.getLocalizedMessage(),e);
					}
					if( cnt> 1024 ){
						cnt = 0;
						long now = System.currentTimeMillis();
						if( now-checkTime<200 ){
							log.error("ϵͳ��ʱ�����ƽ�����ѭ����");
						}
						checkTime = now;
					}
				}
			}
		}
		
		private void dealList() throws InterruptedException{
			while( events.size() == 0 )
				events.wait(1000);
			long now = System.currentTimeMillis();
			SystemTimerEvent ev = events.get(0);
			//ȡ��һ���¼��������ǰʱ�� �� �¼���ʼʱ�䣬
			long dif = now-ev.beginTime;
			if( dif >= ev.delay ){
				events.remove(0);
				GlobalEventHandler.postEvent(ev);
				return;
			}
			if( dif<0 )
				return;
			if( dif< 100 )
				dif = 100;
			events.wait(dif);
		}
	}
}
