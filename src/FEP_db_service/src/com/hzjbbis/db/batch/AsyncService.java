/**
 * 数据库异步保存逻辑模块接口。
 * 异步批量保存数据库，需要线程池支持。
 * 思路：
 * 	  1）异步业务逻辑Service，首先把消息对象转换为事件对象放入队列，确保业务处理快速。
 * 	  2）业务消息的处理，通过接口，使得service能够多线程处理业务逻辑。
 * 	  3）特殊设计－系统流量控制：如果业务处理器不断接收消息，导致数据库处理不过来，那么
 * 		 系统将崩溃。因此需要控制最大队列。当达到最大值，需要等待批量处理完毕。
 * 	  3）业务逻辑的处理结果，一般是把一个消息，生成多个DAO对象调用。
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
	//可配置属性
	private int maxQueueSize = DEFAULT_QUEUE_SIZE;
	private int minThreadSize = 4;
	private int maxThreadSize = 20;
	private int delaySecond = 5;		//如果插入的数量不足批次，最大延迟秒数
	private String name = "batchService";
	private List<IBatchDao> daoList;
	private Map<EventType,BaseBpEventHandler> bpHandlerMap;

	//内部属性
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
	 * 需要异步处理的消息对象。
	 * 如果数据库连接异常（数据库不可用），那么不能增加新的消息。
	 * @param msg
	 * @return：true:允许处理消息； false：数据库不可用或者service繁忙。
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
	 * 当需要更新终端对象时。
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
				//容量不足情况下，尝试加载缓存文件中的原始报文。
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
			log.error("数据保存到DAO错误，对象对应的KEY找不到DAO。key="+key);
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
			// 定时检测是否需要触发延迟保存。
			for(IBatchDao dao: daoList ){
				if( dao.hasDelayData() ){
					//需要把事件放入到EventHook来执行。
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
