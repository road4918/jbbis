/**
 * ȫ���¼��������������¼���Ĭ�ϴ���Ĭ�ϴ���һ�����д��־��
 * ���ĳ���¼���Ҫ�ر�������Ҫ��д��Ӧ�Ĺ��ӡ�
 */
package com.hzjbbis.fk.common.events;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IEventHandler;
import com.hzjbbis.fk.common.spi.IEventHook;
import com.hzjbbis.fk.common.spi.IEventPump;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.State;

/**
 * @author bao
 * 2008-05-27 14:24
 */
@SuppressWarnings("unchecked")
public final class GlobalEventHandler implements IEventPump {
	private final EventQueue queue = new EventQueue();
	/**
	 * ȫ���¼������ӡ���Щ���ӵ�ʵ���࣬ͨ�����÷�ʽȷ����ס�ĸ��¼�Դ���Լ���Щ�����¼���
	 */
	private static final Object hookLock = new Object();
	private static ArrayList<IEventHook> [] hooks = (ArrayList<IEventHook>[])Array.newInstance(ArrayList.class,512);

	/**
	 * ͨ����̷�ʽ����¼��Լ�������������ģʽֻ�ʺ��ڿ��ٴ���ģʽ���¼�����ϵͳ��ʱ����
	 */
	private static ArrayList<IEventHandler> [] handlers;
	
	private volatile State state = State.STOPPED;
	private static Logger log = Logger.getLogger(GlobalEventHandler.class);
	private static TraceLog tracer = TraceLog.getTracer();

	private static GlobalEventHandler gHandler = null;
	private EventPumpThread pump = null; 

	static {
		gHandler = getInstance();
	}

	private GlobalEventHandler(){
		pump = new EventPumpThread();
		pump.start();
	}
	
	public static final GlobalEventHandler getInstance(){
		if( null == gHandler )
			gHandler = new GlobalEventHandler();
		return gHandler;
	}
	
	public void destroy(){
		state = State.STOPPING;
		pump.interrupt();
	}
	/**
	 * ȫ���¼�����������ע�ṳ�ӣ����ض�EventType�¼��������Ӵ���
	 * @param hook
	 */
	@SuppressWarnings("unchecked")
	public static final void registerHook(IEventHook hook,EventType type){
		synchronized(hookLock){
			if( null == hooks ){
				hooks = new ArrayList [128];
				Arrays.fill(hooks,null);
			}
			if( hooks.length < EventType.getMaxIndex() ){
				ArrayList<IEventHook> [] temp = new ArrayList [EventType.getMaxIndex()];
				Arrays.fill(temp, null);
				System.arraycopy(hooks, 0, temp, 0, hooks.length);
				hooks = temp;
			}
			if( null == hooks[type.toInt()]){
				hooks[type.toInt()] = new ArrayList<IEventHook>();
			}
			hooks[type.toInt()].add(hook);
		}
	}

	/**
	 * ��̷�ʽ�����ĳ���¼��Ĵ���
	 * @param handler
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	public static final void registerHandler(IEventHandler handler,EventType type){
		synchronized(hookLock){
			if( null == handlers ){
				handlers = new ArrayList [128];
				Arrays.fill(handlers,null);
			}
			if( handlers.length < EventType.getMaxIndex() ){
				ArrayList<IEventHandler> [] temp = new ArrayList [EventType.getMaxIndex()];
				Arrays.fill(temp, null);
				System.arraycopy(handlers, 0, temp, 0, handlers.length);
				handlers = temp;
			}
			if( null == handlers[type.toInt()]){
				handlers[type.toInt()] = new ArrayList<IEventHandler>();
			}
			handlers[type.toInt()].add(handler);
		}
	}
	
	/**
	 * ȫ���¼�����������ɾ��ע��Ĺ���
	 * @param hook
	 */
	public static final void deregisterHook(IEventHook hook,EventType type){
		synchronized(hookLock){
			try{
				hooks[type.toInt()].remove(hook);
			}catch(Exception e){
				log.error("deregisterHook: "+e.getLocalizedMessage(),e);
			}
		}
	}

	public static final void deregisterHandler(IEventHandler handler,EventType type){
		synchronized(hookLock){
			try{
				handlers[type.toInt()].remove(handler);
			}catch(Exception e){
				log.error("deregisterHandler: "+e.getLocalizedMessage(),e);
			}
		}
	}
	
	public final void handleEvent(IEvent e) {
		if( EventType.SYS_MEMORY_PROFILE == e.getType() ){
			log.info("profile�¼�: "+e);
		}
		else
			log.debug("ȫ���¼�������:"+e );
	}

	public static void postEvent(IEvent e){
		gHandler.post(e);
	}
	
	public final void post(IEvent e) {
		boolean hooked = false;
		ArrayList<IEventHook> list = hooks[e.getType().toInt()];
		if( null != list && list.size()>0 ){
			try{
				for(IEventHook hook: list){
					hook.postEvent(e);
				}
				hooked = true;
			}catch(Exception exp){
				log.error("hook.postEvent:"+exp.getLocalizedMessage(),exp);
			}
		}

		//����¼��Ƿ���Ҫ��Handler����
		if( null != handlers && handlers.length> e.getType().toInt() ){
			ArrayList<IEventHandler> arHandlers = handlers[e.getType().toInt()];
			if( null != arHandlers && arHandlers.size()>0 ){
				hooked = true;
				for( IEventHandler handler: arHandlers ){
					try{
						handler.handleEvent(e);
					}catch(Exception exp){
						log.error("handler.handleEvent: "+exp.getLocalizedMessage(),exp);
					}
				}
			}
		}
		
		//û��ע��Ĺ��ӣ���ȫ���¼��������Լ�����
		if( ! hooked ){
			try{
				queue.offer(e);
			}catch(Exception exp){
				String info = "ȫ���¼����в���ʧ�ܡ�event="+e.toString();
				tracer.trace(info,exp);
				log.error(info,exp);
			}
		}
	}
	
	private class EventPumpThread extends Thread{
		public EventPumpThread(){
			super("GlobalEventPumpThread");
			this.setDaemon(true);
		}
		public void run(){
			state = com.hzjbbis.fk.utils.State.RUNNING;
			log.info("Global event handler thread running");
			long pre = System.currentTimeMillis();
			int cnt = 0;
			while( state != com.hzjbbis.fk.utils.State.STOPPING ){
				try{
					IEvent e = queue.take();
					if( null == e ){
						//��ѭ�����
						cnt++;
						if( cnt >= 20 ){
							long now = System.currentTimeMillis();
							if( Math.abs(now-pre)<1000 ){
								log.warn("��⵽��ѭ����");
							}
							pre = System.currentTimeMillis();
							cnt = 0;
						}
						continue;
					}
					handleEvent(e);
				}
				catch(Exception e){
					log.warn("Global event handler event pump catch exception:"+e.getLocalizedMessage());
				}
			}
			state = com.hzjbbis.fk.utils.State.STOPPED;
		}
	}
}
