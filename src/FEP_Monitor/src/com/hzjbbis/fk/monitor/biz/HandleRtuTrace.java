package com.hzjbbis.fk.monitor.biz;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.spi.IEventHook;
import com.hzjbbis.fk.monitor.biz.eventtrace.EventTracer;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;
import com.hzjbbis.fk.utils.HexDump;
/**
 * ���ǵ�ϵͳ���ܣ�ֻ��ά��һ��rtuTrace����
 * @author hbao
 *
 */
public class HandleRtuTrace {
	private static final Logger log = Logger.getLogger(HandleRtuTrace.class);
	private static final HandleRtuTrace handleRtuTrace = new HandleRtuTrace();
	private static EventTracer tracer = null;
	public static final HandleRtuTrace getHandleRtuTrace(){
		return handleRtuTrace;
	}

	/**
	 * ����RTU�¼����١�
	 * @param event
	 * @param inputBody�����������ʽ��Int����RTUA����������
	 * @return
	 */
	public boolean startTraceRtu(ReceiveMessageEvent event,ByteBuffer inputBody){
		int count = inputBody.remaining()/4;
		if( count == 0 )
			return false;
		int[]rtus  = new int[count];
		for(int i=0; i<count; i++){
			rtus[i] = inputBody.getInt();
			log.info("���٣�RTUA="+HexDump.toHex(rtus[i]));
		}
		//������Ҫ����tracer
		if( null == tracer ){
			synchronized(this){
				tracer = new EventTracer(event.getClient());
				//�������¼�������ע��eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(tracer);
				}
			}
		}
		else
			tracer.addClient(event.getClient());
		tracer.traceRtus(rtus);
		return true;
	}
	
	public boolean stopTrace(ReceiveMessageEvent event){
		if( null == tracer )
			return false;
		synchronized(this){
			int monitorCount = tracer.removeClient(event.getClient());
			if( 0 == monitorCount ){
				//�������¼�������ȡ��eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(null);
				}
			}
		}
		return true;
	}
	
	public void onClientClose(ClientCloseEvent event){
		if( null == tracer )
			return;
		synchronized(this){
			int monitorCount = tracer.removeClient(event.getClient());
			if( 0 == monitorCount ){
				//�������¼�������ȡ��eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(null);
				}
				tracer = null;
			}
		}
	}

}
