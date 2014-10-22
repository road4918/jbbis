/**
 * ���ݿ��첽�����߼�ģ��ӿڡ�
 * �첽�����������ݿ⣬��Ҫ�̳߳�֧�֡�
 * ˼·��
 * 	  1���첽ҵ���߼�Service�����Ȱ���Ϣ����ת��Ϊ�¼����������У�ȷ��ҵ������١�
 * 	  2��ҵ����Ϣ�Ĵ���ͨ���ӿڣ�ʹ��service�ܹ����̴߳���ҵ���߼���
 * 	  3��������ƣ�ϵͳ�������ƣ����ҵ���������Ͻ�����Ϣ���������ݿ⴦����������ô
 * 		 ϵͳ�������������Ҫ���������С����ﵽ���ֵ����Ҫ�ȴ�����������ϡ�
 * 	  3��ҵ���߼��Ĵ�������һ���ǰ�һ����Ϣ�����ɶ��DAO������á�
 */
package com.hzjbbis.db.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.dao.IBatchDao;
import com.hzjbbis.db.batch.event.BpBatchDelayEvent;
import com.hzjbbis.db.batch.event.BpExpAlarmEvent;
import com.hzjbbis.db.batch.event.BpLog2DbEvent;
import com.hzjbbis.db.batch.event.BpReadTaskEvent;
import com.hzjbbis.db.batch.event.FeUpdateRtuStatus;
import com.hzjbbis.db.batch.event.adapt.BatchDelayHandler;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.events.EventQueue;
import com.hzjbbis.fk.common.queue.CacheQueue;
import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.abstra.BaseModule;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.tracelog.TraceLog;
/**
 * @author bhw
 * 2008-10-22
 */
public class AsyncService extends BaseModule implements ITimerFunctor{
	protected static final Logger log = Logger.getLogger(AsyncService.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	
	private static final int DEFAULT_QUEUE_SIZE = 10000; 
	//����������
	private int maxQueueSize = DEFAULT_QUEUE_SIZE;
	private int minThreadSize = 4;
	private int maxThreadSize = 20;
	private int delaySecond = 5;		//�������������������Σ�����ӳ�����
	private String name = "batchService";
	private List<IBatchDao> daoList;
	private Map<EventType,BaseBpEventHandler> bpHandlerMap;

	//�ڲ�����
	private EventQueue queue = new EventQueue(maxQueueSize);
	private BasicEventHook eventHook;
	private BatchDelayHandler batchDelayHandler = new BatchDelayHandler();
	private Map<Integer,IBatchDao> daoMap = new HashMap<Integer,IBatchDao>(127);
	private CacheQueue msgLogCacheQueue = null;

	public void init(){
		if( null == eventHook ){
			eventHook = new BasicEventHook();
			if( ! eventHook.isActive() ){
				eventHook.setMinSize(minThreadSize);
				eventHook.setMaxSize(maxThreadSize);
				eventHook.setName(name);
				eventHook.setQueue(queue);
			}
		}
		if( null == msgLogCacheQueue ){
			msgLogCacheQueue = new CacheQueue();
			msgLogCacheQueue.setKey("rawmsg");
			msgLogCacheQueue.setMaxFileSize(100);
			msgLogCacheQueue.setFileCount(20);
			msgLogCacheQueue.setMaxSize(100);
			msgLogCacheQueue.setMinSize(10);
		}
		for( EventType type: bpHandlerMap.keySet()){
			eventHook.addHandler(type, bpHandlerMap.get(type));
		}
		eventHook.addHandler(batchDelayHandler.type(), batchDelayHandler);
		eventHook.start();
	}

	public boolean isActive(){
		return null != eventHook && eventHook.isActive() && FasSystem.getFasSystem().isDbAvailable();
	}

	public String getName() {
		return name;
	}

	public boolean start() {
		init();
		for(IBatchDao dao: daoList ){
			dao.setDelaySecond(this.delaySecond);
		}
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,this.delaySecond));
		return true;
	}

	public void stop() {
		TimerScheduler.getScheduler().removeTimer(this, 0);
		if( null != eventHook ){
			eventHook.stop();
		}
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_DB_SERVICE;
	}

	/**
	 * ��Ҫ�첽�������Ϣ����
	 * ������ݿ������쳣�����ݿⲻ���ã�����ô���������µ���Ϣ��
	 * @param msg
	 * @return��true:��������Ϣ�� false�����ݿⲻ���û���service��æ��
	 */
	public boolean addMessage(MessageZj msg){
		if( queue.size()>= maxQueueSize )
			return false;
		
		IEvent event;
		if( msg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK )
			event = new BpReadTaskEvent(this,msg);
		else if( msg.head.c_func == MessageConst.ZJ_FUNC_EXP_ALARM )
			event = new BpExpAlarmEvent(this,msg);
		else
			return false;
		try{
			queue.offer(event);
		}catch(Exception exp){
			tracer.trace(exp.getLocalizedMessage(), exp);
			return false;
		}
		return true;
	}
	
	/**
	 * ����Ҫ�����ն˶���ʱ��
	 * @param rtu
	 * @return
	 */
	public boolean addRtu(Object rtu){
		if( queue.size()>= maxQueueSize )
			return false;
		IEvent event = new FeUpdateRtuStatus(this,rtu);
		try{
			queue.offer(event);
		}catch(Exception exp){
			tracer.trace(exp.getLocalizedMessage(), exp);
		}
		return true;
	}
	
	public boolean log2Db(MessageZj msg){
		if( queue.size()>= maxQueueSize ){
			msgLogCacheQueue.offer(msg);
			return true;
		}
		try{
			queue.offer(new BpLog2DbEvent(this,msg));
			if( queue.size()*2 < queue.capacity() ){
				//������������£����Լ��ػ����ļ��е�ԭʼ���ġ�
				for(int i=0; i<10; i++ ){
					msg = (MessageZj)msgLogCacheQueue.poll();
					if( null == msg )
						break;
					queue.offer(new BpLog2DbEvent(this,msg));
				}
			}
			
		}catch(Exception exp){
			tracer.trace(exp.getLocalizedMessage(), exp);
		}
		return true;
	}
	
	public void addToDao(Object pojo,int key){
		IBatchDao dao = daoMap.get(key);
		if( null == dao ){
			log.error("���ݱ��浽DAO���󣬶����Ӧ��KEY�Ҳ���DAO��key="+key);
			return;
		}
		dao.add(pojo);
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		if( this.maxQueueSize> queue.capacity() )
			queue.setCapacity(maxQueueSize);
	}

	public void setDaoList(List<IBatchDao> list) {
		this.daoList = list;
		for(IBatchDao dao: daoList ){
			daoMap.put(dao.getKey(), dao);
		}
	}

	public void setBpHandlerMap(Map<EventType, BaseBpEventHandler> handlers) {
		this.bpHandlerMap = handlers;
		for(BaseBpEventHandler handler: bpHandlerMap.values() ){
			handler.setService(this);
		}
	}

	public void setEventHook(BasicEventHook eventHook) {
		this.eventHook = eventHook;
	}

	public void setMinThreadSize(int minThreadSize) {
		this.minThreadSize = minThreadSize;
	}

	public void setMaxThreadSize(int maxThreadSize) {
		this.maxThreadSize = maxThreadSize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void onTimer(int id) {
		if( 0 == id ){
			// ��ʱ����Ƿ���Ҫ�����ӳٱ��档
			for(IBatchDao dao: daoList ){
				if( dao.hasDelayData() ){
					//��Ҫ���¼����뵽EventHook��ִ�С�
					try{
						queue.offer(new BpBatchDelayEvent(dao));
					}catch(Exception exp){
						tracer.trace(exp.getLocalizedMessage(), exp);
					}
				}
			}
		}
	}

	public void setDelaySecond(int delaySecond) {
		if( delaySecond<=1 )
			delaySecond = 5;
		this.delaySecond = delaySecond;
	}

	@Override
	public String toString() {
		return "AsyncService";
	}
	
	public Collection<MessageZj> revokeEventQueue(){
		boolean takable = queue.enableTake();
		queue.enableTake(false);
		List<IEvent> events = new LinkedList<IEvent>();
		List<MessageZj> msgs = new ArrayList<MessageZj>();
		queue.drainTo(events, queue.size(), 0);
		for(IEvent ev: events ){
			if( null != ev.getMessage() && ev.getMessage().getMessageType() == MessageType.MSG_ZJ ){
				msgs.add((MessageZj)ev.getMessage());
			}
		}
		queue.enableTake(takable);
		if( null != msgLogCacheQueue )
			msgLogCacheQueue.asyncSaveQueue();
		return msgs;
	}
}
