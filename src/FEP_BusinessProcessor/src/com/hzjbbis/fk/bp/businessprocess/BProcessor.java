/**
 * ����ҵ����ʵ�֡�
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
	//����������
	private String name = "bp";	
	private int minSize = 5;			//�̳߳���С����
	private int maxSize = 20;			//�̳߳�������
	
	//���õ��ⲿ����ģ��
	private AsyncService asycService;	//spring ����ʵ�֡�
	private MasterDbService masterDbService;  //spring ����ʵ�֡�
	private ManageRtu manageRtu;		//spring ����ʵ�֡�
	private HostCommandHandler hostCommandHandler=new HostCommandHandler();//��վ�������ݴ�����
	private BPMessageQueue msgQueue;	//spring ����ʵ�֡�
	
	//�����ڲ�״̬
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
			log.debug("�̳߳ء�"+name+"�������ɹ���,size="+minSize);
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
			log.debug("�̳߳ء�"+name+"��ֹͣ��,�����߳���="+works.size());
		works.clear();
		//��AsyncService���ݻ���
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
			//���ܳ������ֵ
			int maxDelta = this.maxSize - works.size();
			delta = Math.min(maxDelta, delta);
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//����С��1
			delta = -delta;
			int n = works.size() - minSize;		//���������ٵ��߳���
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
				log.debug("�����̳߳ش�С(+1)");
			forkThreads(1);
		}
		else if( n< 2 ){
			if( log.isDebugEnabled() )
				log.debug("�����̳߳ش�С(-1)");
			forkThreads(-1);
		}
	}
	
	private class WorkThread extends Thread{
		long beginTime;
		boolean busy = false;		//�ж�ʵ���Ƿ��ڹ���״̬
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
			int count=0;		//ÿ����1000���¼�����̳߳��Ƿ���Ҫ����
			log.info("threadpool.work running:"+this.getName());
			while( !BProcessor.this.state.isStopping() && !BProcessor.this.state.isStopped() ){
				try{
					busy = false;
					currentMessage = msgQueue.take();
					if( null == currentMessage ){		//������ݿ������쳣������NULL��
						Thread.sleep(100);
						continue;
					}
					
					log.info("�������б���:"+currentMessage.getRawPacketString());
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
					//���������¼����������̫�࣬�����̡߳����Ϊ0�������߳�
					count++;
					if( count>500 ){
						justThreadSize();
						count = 0;
					}
				}catch(Exception exp){
					log.error("ҵ���������Ĵ����̴߳�����汨�ĳ���", exp);
					continue;
				}
			}
			synchronized(works){
				works.remove(this);
			}
			log.info("�̳߳صĹ����߳��˳�:"+this.getName());
		}
		private void handleZjMessage(MessageZj zjmsg){
			if( log.isDebugEnabled() )
				log.debug("ͨ��ǰ�û����б���:"+zjmsg);
			/** ҵ�������յ�ͨ��ǰ�û����б��ģ���ҪԤ�ȴ���,
			 * �ȼ���Ƿ���ڴ��ն˵���,���ڲŽ���ҵ����,��Ȼֻ����ԭʼ��¼
			 * ����,�쳣,��վ���󷵻ش���:������������д���ݿ�
			 * ԭʼ���ı���
			 */
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(zjmsg.head.rtua));
			if (rtu==null){
				//�Ҳ����ն���ˢ�����ݿ⵵��
				boolean refreshTag=manageRtu.refreshBizRtu(zjmsg.head.rtua);
				if (!refreshTag)
					log.error("not find rtu in db:"+HexDump.toHex(zjmsg.head.rtua));
				else
					rtu=(RtuManage.getInstance().getBizRtuInCache(zjmsg.head.rtua));
			}
			if (rtu!=null){
				//�����쳣
				if(zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_TASK||zjmsg.head.c_func==MessageConst.ZJ_FUNC_EXP_ALARM){
					//keep in mind that addMessage may fail if the queue is full. Check this case and put back the message.
					//Modified by bhw 2009-01-26 14:40
					if( !asycService.addMessage(zjmsg) )
						msgQueue.offer(zjmsg);
				}
				else if (zjmsg.head.c_func==MessageConst.ZJ_FUNC_RELAY||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_CUR
						||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_ROBJ||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_OBJ
						||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_PROG){//��վ���󷵻�
					hostCommandHandler.handleExpNormalMsg(manageRtu,masterDbService,zjmsg);
				}
			}	
			//ԭʼ���Ĵ����ݿ�					
//    		if(zjmsg.isExceptionPacket()){//�ж��Ƿ�Ϊ�쳣����
//    			byte code=zjmsg.getErrorCode();//ȡ������
//    			if (code==ErrorCode.MST_SEND_FAILURE)//�ڲ��Զ��屨�Ĳ��������ݿ�
//    				return;
//    		}
//    		asycService.log2Db(zjmsg);	
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

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
