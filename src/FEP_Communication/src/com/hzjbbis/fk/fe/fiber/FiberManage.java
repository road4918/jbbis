/**
 * �˳̹�����һ���̳߳�������ִ�ж���˳̡�
 */
package com.hzjbbis.fk.fe.fiber;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.abstra.BaseModule;
import com.hzjbbis.fk.utils.State;

/**
 * @author bhw
 * 2008-10-28 23:58
 */
public class FiberManage extends BaseModule{
	//��̬����
	private static final Logger log = Logger.getLogger(FiberManage.class);
	private static int threadSeq = 1;
	//���������ԡ�
	private int minThreadSize = 2;
	private int maxThreadSize = 10;
	//�����һ�����������ƹ����̣߳����ŵ�runOnce���С�
	private List<IFiber> runOnce = new LinkedList<IFiber>();
	//�����̳߳أ�fibersά��һ���˳̳ء�
	private List<IFiber> fibers = new ArrayList<IFiber>();
	
	//�ڲ�����
	private volatile State state = new State();
	private List<WorkThread> works = new LinkedList<WorkThread>();
	
	private static final FiberManage instance = new FiberManage();
	
	private FiberManage(){}
	
	public static final FiberManage getInstance(){
		return instance;
	}
	
	public void scheduleRunOnce(IFiber fiber){
		synchronized(runOnce){
			runOnce.add(fiber);
		}
	}
	
	/**
	 * ��������������ִ�е��˳�����
	 * @param fiber
	 */
	public void schedule(IFiber fiber){
		synchronized(fibers){
			fibers.add(fiber);
		}
	}
	
	@Override
	public String getName() {
		return "FiberManage";
	}
	@Override
	public boolean isActive() {
		return state.isRunning();
	}
	@Override
	public boolean start() {
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		
		forkThreads( this.minThreadSize );
		while( works.size()< this.minThreadSize ){
			Thread.yield();
			try{
				Thread.sleep(100);
			}catch(Exception exp){}
		}
		state = State.RUNNING;
		if( log.isDebugEnabled() )
			log.debug("�˳̳ع�������"+getName()+"�������ɹ���,size="+ this.minThreadSize );
		return true;
	}
	
	@Override
	public void stop() {
		state = State.STOPPING;
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
			synchronized(works){
				for(WorkThread work: works){
					work.interrupt();
				}
			}
		}
		if( log.isDebugEnabled() )
			log.debug("�̳߳ء�"+getName()+"��ֹͣ��,�����߳���="+works.size());
		works.clear();
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_CONTAINER;
	}

	private void forkThreads(int delta) {
		if (delta == 0)
			return;

		if (delta > 0) {
			//���ܳ������ֵ
			int maxDelta = this.maxThreadSize - works.size();
			delta = Math.min(maxDelta, delta);
			if( log.isDebugEnabled() && delta>0 )
				log.debug("�����̳߳ش�С(+"+delta+")");
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//����С��1
			delta = -delta;
			int n = works.size() - this.minThreadSize;		//���������ٵ��߳���
			delta = Math.min(delta, n);
			if( log.isDebugEnabled() && delta>0 )
				log.debug("�����̳߳ش�С(-"+delta+")");
			for (; delta > 0; delta--) {
				runOnce.add(new KillThreadFiber());
			}
		}
	}
	
	private void justThreadSize(){
		int size = works.size()>0 ? works.size() : 1;
		int n = fibers.size() / size;
		if( n> 2 ){
			forkThreads(1);
		}
		else if( n == 0 ){
			forkThreads(-1);
		}
	}

	private IFiber nextFiber(){
		synchronized( fibers ){
			if( fibers.size()>0 )
				return fibers.remove(0);
			else
				return null;
		}
	}
	
	private class WorkThread extends Thread{
		public WorkThread(){
			super(FiberManage.this.getName()+"."+threadSeq++);
			super.start();
		}
		public void run() {
			synchronized(works){
				works.add(this);
			}
			log.info("FiberManage.work running:"+this.getName());
			int count = 0;
			while( !FiberManage.this.state.isStopping() && !FiberManage.this.state.isStopped() ){
				try{
					IFiber fiber = null;
					//1. ��ִ��һ��������
					synchronized(runOnce){
						if( runOnce.size()>0 )
							fiber = runOnce.remove(0);
					}
					if( null != fiber ){
						try{
							fiber.runOnce();
						}
						catch(KillThreadException kill){
							break;
						}
						catch(Exception e){
							log.warn("unhandled exception:"+e.getLocalizedMessage(),e);
						}
					}
					
					//2. ִ�������Ե��˳̹���
					fiber = nextFiber();
					if( null == fiber ){
						Thread.sleep(50);
						continue;
					}
					try{
						fiber.runOnce();
					}
					catch(Exception e){
						log.warn("�˳�ִ���쳣fiber exception:"+e.getLocalizedMessage(),e);
					}
					finally{
						schedule(fiber);
					}
					//��������ִ�д�����ÿ50�Σ�����Ƿ���Ҫ�����̡߳�
					count++;
					if( count>50 ){
						justThreadSize();
						count = 0;
					}
				}catch(InterruptedException exp){
					//�̱߳��жϡ�����Ƿ���Ҫ�ر�
					continue;
				}
				catch( Exception e){
					log.warn("catch unhandled exception:"+e.getLocalizedMessage(),e);
					continue;
				}
			}
			synchronized(works){
				works.remove(this);
			}
			log.info("�˳̳صĹ����߳��˳�:"+this.getName());
		}
	}
	
	private class KillThreadException extends RuntimeException {
		private static final long serialVersionUID = -4810948231187690635L;

		public KillThreadException(){}
	}
	
	private class KillThreadFiber implements IFiber {

		public boolean isFiber() {
			return true;
		}

		public void runOnce() {
			throw new KillThreadException();
		}

		public void setFiber(boolean isFiber) {
		}
		
	}

	public final void setMinThreadSize(int minThreadSize) {
		this.minThreadSize = minThreadSize;
	}
	public final void setMaxThreadSize(int maxThreadSize) {
		this.maxThreadSize = maxThreadSize;
	}
	public final void setRunOnce(List<IFiber> runOnce) {
		this.runOnce = runOnce;
	}
	public final void setFibers(List<IFiber> fibers) {
		this.fibers = fibers;
	}

}
