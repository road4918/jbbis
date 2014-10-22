/**
 * 具体业务处理实现。
 */
package com.hzjbbis.fk.bp.businessprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.db.bizprocess.MasterDbService;
import com.hzjbbis.db.managertu.ManageRtu;
import com.hzjbbis.fk.bp.msgqueue.BPMessageQueue;
import com.hzjbbis.fk.bp.processor.HostCommandHandler;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.abstra.BaseModule;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.KillThreadMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.fk.utils.State;

/**
 * @author bhw
 *
 */
public class BProcessor extends BaseModule{
	private static final Logger log = Logger.getLogger(BProcessor.class);
	private static int poolSeq = 1;
	//可配置属性
	private String name = "bp";	
	private int minSize = 5;			//线程池最小个数
	private int maxSize = 20;			//线程池最大个数
	
	//引用的外部功能模块
	private AsyncService asycService;	//spring 配置实现。
	private MasterDbService masterDbService;  //spring 配置实现。
	private ManageRtu manageRtu;		//spring 配置实现。
	private HostCommandHandler hostCommandHandler=new HostCommandHandler();//主站请求数据处理类
	private BPMessageQueue msgQueue;	//spring 配置实现。
	
	//对象内部状态
	private volatile State state = new State();	
	private List<WorkThread> works = Collections.synchronizedList( new ArrayList<WorkThread>() );
	private int threadPriority = Thread.NORM_PRIORITY;
	private final IMessage killThread = new KillThreadMessage();

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return state.isActive();
	}

	public boolean start() {
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
		state = State.RUNNING;
		if( log.isDebugEnabled() )
			log.debug("线程池【"+name+"】启动成功。,size="+minSize);
		return true;
	}

	public void setAsycService(AsyncService asycService) {
		this.asycService = asycService;
	}

	public void setManageRtu(ManageRtu manageRtu) {
		this.manageRtu = manageRtu;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public void stop() {
		state = State.STOPPING;
		for (int i=0; i<works.size() ; i++ ) {
			msgQueue.offer(this.killThread);
		}
		
		synchronized(works){
			for(WorkThread work: works){
				work.interrupt();
			}
		}
		int cnt = 100;
		while(cnt-->0 && works.size()>0 ){
			Thread.yield();
			try{
				Thread.sleep(50);
			}
			catch(Exception e){}
			if( cnt< 20 )
				continue;
		}
		if( log.isDebugEnabled() )
			log.debug("线程池【"+name+"】停止。,僵死线程数="+works.size());
		works.clear();
		//把AsyncService数据回收
		for(MessageZj msg : this.asycService.revokeEventQueue() )
			msgQueue.offer(msg);
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}
	
	
	private void forkThreads(int delta) {
		if (delta == 0)
			return;

		if (delta > 0) {
			//不能超过最大值
			int maxDelta = this.maxSize - works.size();
			delta = Math.min(maxDelta, delta);
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//不能小于1
			delta = -delta;
			int n = works.size() - minSize;		//最多允许减少的线程数
			delta = Math.min(delta, n);
			for (; delta > 0; delta--) {
				msgQueue.offer(this.killThread);
			}
		}
	}
	
	private void justThreadSize(){
		int n = msgQueue.size();
		if( n> 1000 ){
			if( log.isDebugEnabled() )
				log.debug("调整线程池大小(+1)");
			forkThreads(1);
		}
		else if( n< 2 ){
			if( log.isDebugEnabled() )
				log.debug("调整线程池大小(-1)");
			forkThreads(-1);
		}
	}
	
	private class WorkThread extends Thread{
		long beginTime;
		boolean busy = false;		//判断实现是否处于工作状态
		IMessage currentMessage = null;
		IEvent curEvent = null;
		public WorkThread(){
			super(name+"."+poolSeq++);
			super.start();
		}

		public void run() {
			synchronized(works){
				works.add(this);
			}
			this.setPriority(threadPriority);
			int count=0;		//每处理1000个事件检测线程池是否需要调整
			log.info("threadpool.work running:"+this.getName());
			while( !BProcessor.this.state.isStopping() && !BProcessor.this.state.isStopped() ){
				try{
					busy = false;
					currentMessage = msgQueue.take();
					if( null == currentMessage ){		//如果数据库连接异常，返回NULL。
						Thread.sleep(100);
						continue;
					}
					
					log.info("处理上行报文:"+currentMessage.getRawPacketString());
					if( currentMessage.getMessageType() == MessageType.MSG_KILLTHREAD )
						break;
					else if ( currentMessage.getMessageType() == MessageType.MSG_GATE ){
						MessageGate mgate = (MessageGate)currentMessage;
						if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
							MessageZj zjmsg = mgate.getInnerMessage();
							handleZjMessage(zjmsg);
						}
					}
					else if( currentMessage.getMessageType() == MessageType.MSG_ZJ ){
						handleZjMessage((MessageZj)currentMessage);
					}
					//检测队列中事件个数。如果太多，增加线程。如果为0，减少线程
					count++;
					if( count>500 ){
						justThreadSize();
						count = 0;
					}
				}catch(Exception exp){
					log.error("业务处理器报文处理线程处理浙规报文出错", exp);
					continue;
				}
			}
			synchronized(works){
				works.remove(this);
			}
			log.info("线程池的工作线程退出:"+this.getName());
		}
		private void handleZjMessage(MessageZj zjmsg){
			if( log.isDebugEnabled() )
				log.debug("通信前置机上行报文:"+zjmsg);
			/** 业务处理器收到通信前置机上行报文，需要预先处理,
			 * 先检查是否存在此终端档案,存在才进行业务处理,不然只保存原始记录
			 * 任务,异常,主站请求返回处理:包括解析及回写数据库
			 * 原始报文保存
			 */
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(zjmsg.head.rtua));
			if (rtu==null){
				//找不到终端则刷新数据库档案
				boolean refreshTag=manageRtu.refreshBizRtu(zjmsg.head.rtua);
				if (!refreshTag)
					log.error("not find rtu in db:"+HexDump.toHex(zjmsg.head.rtua));
				else
					rtu=(RtuManage.getInstance().getBizRtuInCache(zjmsg.head.rtua));
			}
			if (rtu!=null){
				//任务异常
				if(zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_TASK||zjmsg.head.c_func==MessageConst.ZJ_FUNC_EXP_ALARM){
					//keep in mind that addMessage may fail if the queue is full. Check this case and put back the message.
					//Modified by bhw 2009-01-26 14:40
					if( !asycService.addMessage(zjmsg) )
						msgQueue.offer(zjmsg);
				}
				else if (zjmsg.head.c_func==MessageConst.ZJ_FUNC_RELAY||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_CUR
						||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_ROBJ||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_OBJ
						||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_PROG){//主站请求返回
					hostCommandHandler.handleExpNormalMsg(manageRtu,masterDbService,zjmsg);
				}
			}	
			//原始报文存数据库					
//    		if(zjmsg.isExceptionPacket()){//判断是否为异常返回
//    			byte code=zjmsg.getErrorCode();//取错误码
//    			if (code==ErrorCode.MST_SEND_FAILURE)//内部自定义报文不保存数据库
//    				return;
//    		}
//    		asycService.log2Db(zjmsg);	
		}
		
		
		
		
		public String toString(){
			String busyStatus = "idle";
			if( busy ){
				long timeConsume = System.currentTimeMillis()-beginTime;
				busyStatus = "当前处理时间(毫秒):"+timeConsume;
			}
			return "["+getName()+","+ busyStatus + "];";
		}
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
