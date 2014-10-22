/**
 * 纤程管理。以一个线程池来调度执行多个纤程。
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
	//静态属性
	private static final Logger log = Logger.getLogger(FiberManage.class);
	private static int threadSeq = 1;
	//可配置属性。
	private int minThreadSize = 2;
	private int maxThreadSize = 10;
	//如果是一次性任务（类似工作线程），放到runOnce队列。
	private List<IFiber> runOnce = new LinkedList<IFiber>();
	//类是线程池，fibers维护一个纤程池。
	private List<IFiber> fibers = new ArrayList<IFiber>();
	
	//内部属性
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
	 * 增加永久周期性执行的纤程任务。
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
			log.debug("纤程池管理器【"+getName()+"】启动成功。,size="+ this.minThreadSize );
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
			log.debug("线程池【"+getName()+"】停止。,僵死线程数="+works.size());
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
			//不能超过最大值
			int maxDelta = this.maxThreadSize - works.size();
			delta = Math.min(maxDelta, delta);
			if( log.isDebugEnabled() && delta>0 )
				log.debug("调整线程池大小(+"+delta+")");
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//不能小于1
			delta = -delta;
			int n = works.size() - this.minThreadSize;		//最多允许减少的线程数
			delta = Math.min(delta, n);
			if( log.isDebugEnabled() && delta>0 )
				log.debug("调整线程池大小(-"+delta+")");
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
					//1. 先执行一次性任务
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
					
					//2. 执行周期性的纤程工作
					fiber = nextFiber();
					if( null == fiber ){
						Thread.sleep(50);
						continue;
					}
					try{
						fiber.runOnce();
					}
					catch(Exception e){
						log.warn("纤程执行异常fiber exception:"+e.getLocalizedMessage(),e);
					}
					finally{
						schedule(fiber);
					}
					//检测队列中执行次数。每50次，检测是否需要增加线程。
					count++;
					if( count>50 ){
						justThreadSize();
						count = 0;
					}
				}catch(InterruptedException exp){
					//线程被中断。检查是否需要关闭
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
			log.info("纤程池的工作线程退出:"+this.getName());
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
