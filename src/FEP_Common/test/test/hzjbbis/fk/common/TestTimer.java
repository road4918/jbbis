package test.hzjbbis.fk.common;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.simpletimer.ITimerFunctor;
import com.hzjbbis.fk.common.simpletimer.TimerData;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;

public class TestTimer {
	private static final Logger log = Logger.getLogger(TestTimer.class); 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimerScheduler scheduler = TimerScheduler.getScheduler();
		ITimerFunctor functor = new ITimerFunctor(){
			public void onTimer(int id) {
				log.debug("定时器["+id+"]触发了");
			}
			
		};
		TimerData timer1 = new TimerData();
		timer1.setFunctor(functor);
		timer1.setId(1);
		timer1.setPeriod(1);
		scheduler.addTimer(timer1);

		TimerData timer2 = new TimerData();
		timer2.setFunctor(functor);
		timer2.setId(2);
		timer2.setPeriod(5);
		scheduler.addTimer(timer2);
		try{
			Thread.sleep(100*1000);
		}catch(Exception e){}
	}

}
