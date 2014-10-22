/**
 * ÿ���߳�ά��һ��Selector��ÿ��SocketChannelֻ�ܹ�����ĳ��Thread��
 * 
 * ���������ն˷�����˵�������������ȼ���ߣ���˷����¼����ȣ�
 * ����������ǰ�û�Ӧ����˵�����������������ȼ��ߣ���˶��¼����ȡ�
 * �����Ҫ���ж���д���ȼ����ƻ��ơ�
 *    ���ƻ��ƣ�ͨ��interestOps��������ڷ���û����ɣ�������һ���������Ͷ��ٴΣ����������
 *    		  ����ֻ�ܿ�����client���������С�
 */
package com.hzjbbis.fk.sockserver.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.EventQueue;
import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.common.spi.socket.IClientIO;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.exception.SocketClientCloseException;
import com.hzjbbis.fk.sockclient.async.event.ClientConnectedEvent;
import com.hzjbbis.fk.sockserver.event.AcceptEvent;
import com.hzjbbis.fk.sockserver.event.ClientCloseEvent;
import com.hzjbbis.fk.sockserver.event.ClientWriteReqEvent;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * @author bao
 * 2008-05-28 22:23
 */
public class SocketIoThread extends Thread {
	private static final Logger log = Logger.getLogger(SocketIoThread.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	private volatile com.hzjbbis.fk.utils.State state = com.hzjbbis.fk.utils.State.STOPPED;
	private IClientIO ioHandler;
	private Selector selector;
	//ÿ���߳�ά��SocketClient�б�ʹ�ö���߳̾��⹤����
	private List<IServerSideChannel> list = Collections.synchronizedList(new LinkedList<IServerSideChannel>());
	private EventQueue queue = new EventQueue();
	
	public SocketIoThread(int port,IClientIO ioHandler,int index){
		super("io-"+port+"-"+index);
		this.ioHandler = ioHandler;
		super.start();
	}
	
	public void stopThread(){
		state = com.hzjbbis.fk.utils.State.STOPPING;
		this.interrupt();
	}
	
	public boolean isRunning(){
		return state == com.hzjbbis.fk.utils.State.RUNNING;
	}
	
	public void acceptClient(IServerSideChannel client){
		client.setIoThread(this);
		list.add(client);

		//�¼�ϵͳ��˵���¼��ǲ��ܶ����ġ����ǿ��ǵ�ϵͳ���������У������ṩ1���еĲ��볢��ʱ�䡣
		//�¼�����Ӧ���Ǹ�Ч�ġ����ʧ�ܣ�����Ҫ������־���Ա�����¼����д�С��
		IEvent event = new AcceptEvent(client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//�¼��������ʧ�ܣ��Ƿǳ����ص����顣
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void addConnectedClient(IServerSideChannel client){
		client.setIoThread(this);
		list.add(client);

		IEvent event = new ClientConnectedEvent(client.getServer(),client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//�¼��������ʧ�ܣ��Ƿǳ����ص����顣
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void closeClientRequest(IServerSideChannel client){
		list.remove(client);
		
		IEvent event = new ClientCloseEvent(client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//�¼��������ʧ�ܣ��Ƿǳ����ص����顣
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void clientWriteRequest(IServerSideChannel client){
		try{
			queue.offer(new ClientWriteReqEvent(client));
			selector.wakeup();
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public int getClientSize(){
		return list.size();
	}
	
	public void run(){
		try{
			selector = Selector.open();
		}
		catch(IOException e){
			log.error("Selector.open()",e);
			return;
		}
		state = com.hzjbbis.fk.utils.State.RUNNING;
		while( state != com.hzjbbis.fk.utils.State.STOPPING ){
			try{
				//JDK����ȱ�ݣ�selector()���ܼ�ⲻ��Ѹ�٣���˲���selector(100)���벻Ҫ�޸�������롣
				int n = selector.select(100);
				if( state == com.hzjbbis.fk.utils.State.STOPPING )
					break;
				//�ȴ����첽�ر�socket client���¼���
				if( handleEvent() )
					continue;
				
				Set<SelectionKey> set = selector.selectedKeys();
				if( null== set )
					continue;
				if( 0==n && set.size()<=0 )
					continue;
				Iterator<SelectionKey> it = set.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					it.remove();
					IServerSideChannel client = (IServerSideChannel)key.attachment();
					if( null == client ){
						key.cancel();	key.attach(null);
						log.warn("null==key.attachment()");
						continue;
					}
					if( !key.isValid() ){
						key.cancel();	key.attach(null);
						log.warn("!key.isValid()");
						continue;
					}
					if( key.isReadable() ){		//�����Ķ�ȡ���ݡ�
						client.setLastReadTime() ;
						try{
							//JDKȱ�ݣ���������ȡ�������ɽ�����Ϣ�����У�����Է����������ݷ��͵�socket��������
							//��ôselect�����⵽�ɶ��¼����⽫���²������ݲ��ܼ�ʱ����������������"����"���ݡ�
							//��ð취�ǣ�����һ�ζ�ȡsocket�����ڲ��¼���
							//�ڷ�����������£���ȡ�ڲ��¼������ͺ����ɣ����Ͳ���������£��ٴε���onRecieve��
							//�����Ƿ�ȫ����ȡ��ϡ�����������ȼ��ߣ���ô��ȡ������Ϣ��Ӧ�ðѿ���Ȩ�������͡�
							boolean readDone = ioHandler.onReceive(client);
							client.getServer().setLastReceiveTime(System.currentTimeMillis());
							//���readDone=true����ʾ�����ȣ����Լ�������false����ʾ����Ȩ����д��
							if( readDone ){
								ioHandler.onReceive(client);		//�ٴζ�����ֹ����
								//�������Ϣ��Ҫ���ͣ���ȷ�����ϣ��ټ�������
								if( client.sendQueueSize()>1 ){
									int interest = key.interestOps();
									interest &= ~SelectionKey.OP_READ;
									interest |= SelectionKey.OP_WRITE;
									key.interestOps( interest );
								}
							}
							else{
								int interest = key.interestOps();
								interest &= ~SelectionKey.OP_READ;
								interest |= SelectionKey.OP_WRITE;
								key.interestOps( interest );
							}
						}catch(SocketClientCloseException cce){
							//�Է��ر�Socket���ӡ�
							client.getServer().removeClient(client);
							key.cancel();
							client.close();
							list.remove(client);
							GlobalEventHandler.postEvent(new ClientCloseEvent(client));
							continue;
						}
						catch(Exception exp){
							//��ȡ�����쳣
							log.warn("server.getIoHandler().onReceive(client):"+exp.getLocalizedMessage(),exp);
						}
					}
					if( key.isWritable() ){
						//���������У����Է��͡��������ݷ�����ϣ���ȡ��д
						try{
							doWrite(client);
						}catch(SocketClientCloseException cce){
							//�Է��ر�Socket���ӡ�
							client.getServer().removeClient(client);
							key.cancel();
							client.close();
							list.remove(client);
							GlobalEventHandler.postEvent(new ClientCloseEvent(client));
							if(log.isDebugEnabled())
								log.debug("server["+client.getServer().getPort()+"]doWrite exp. client closed.",cce);
							continue;
						}
						catch(Exception exp){
							//д�����쳣
							log.warn("server.getIoHandler().onSend(client):"+exp.getLocalizedMessage(),exp);
						}
					}
				}
			}
			catch(Exception e){
				log.error("select(500) exception:"+e,e);
			}
		}
		try{
			selector.close();
		}
		catch(IOException e){
			log.error("Selector.close()",e);
		}
		finally{
			selector = null;
			state = com.hzjbbis.fk.utils.State.STOPPED;
		}
	}
	
	private void doWrite(IServerSideChannel client) throws SocketClientCloseException{
		client.setLastIoTime();
		boolean finishWrite = false;
		finishWrite = ioHandler.onSend(client);
		SelectionKey key = client.getChannel().keyFor(selector);
		int interest = key.interestOps();

		//д���ȼ����ƻ���
		if( finishWrite && client.sendQueueSize()==0 ){
			client.setLastingWrite(0);
			interest &= ~SelectionKey.OP_WRITE;		//ȥ����д���
			interest |= SelectionKey.OP_READ;
			client.getServer().setLastSendTime(System.currentTimeMillis());
			//���ȫ�����ݷ�����ϣ�����Ҫ��һ�����ݡ���ֹsocket������������©��select���ܲ����⵽��
			ioHandler.onReceive(client);
		}
		else
			client.setLastingWrite(client.getLastingWrite()+1);
		if( client.getLastingWrite() > client.getServer().getWriteFirstCount() ){
			interest |= SelectionKey.OP_READ;
			client.setLastingWrite(0);
		}
		key.interestOps(interest);
	}
	
	private boolean handleEvent(){
		IEvent ee = null;
		boolean processed = false;
		while( null !=( ee=queue.poll()) ){
			processed = true;
			if( ee.getType() == EventType.ACCEPTCLIENT ){
				//�����������ܿͻ������Ӻ���Ҫע�ᡰ����
				try{
					AcceptEvent e = (AcceptEvent) ee;
					IServerSideChannel client = e.getClient();
					client.getChannel().configureBlocking(false);
					client.getChannel().register(selector,SelectionKey.OP_READ, client);
					//SocketServer�Ѿ����ù�����Ҫ�ٴε��á�GlobalEventHandler.postEvent(ee);
				}catch(Exception exp){
					log.error("accept client�����쳣:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENTCLOSE ){
				try{//ǿ�ƹر��¼�
					ClientCloseEvent e = (ClientCloseEvent)ee;
					IServerSideChannel client = e.getClient();
					if( null == client.getChannel() )
						continue;
					SelectionKey key = client.getChannel().keyFor(selector);
					if( null != key )
						key.cancel();
					client.close();
					//socket client�ر��¼�
					GlobalEventHandler.postEvent(ee);
				}catch(Exception exp){
					log.error("�ⲿ���ùر�client�����쳣:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENT_WRITE_REQ ){
				try{
					ClientWriteReqEvent e = (ClientWriteReqEvent)ee;
					IServerSideChannel client = e.getClient();
					//if client is closed.
					if( null == client.getChannel() )
						continue;
					SelectionKey key = client.getChannel().keyFor(selector);
					int interest = key.interestOps();
					interest |= SelectionKey.OP_WRITE;		//���ӿ�д���
					key.interestOps(interest);
				}catch(Exception exp){
					log.error("clientд�������쳣:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENT_CONNECTED ){
				ClientConnectedEvent e = (ClientConnectedEvent)ee;
				IServerSideChannel client = (IServerSideChannel)e.getClient();
				try{
					client.getChannel().configureBlocking(false);
					client.getChannel().register(selector,SelectionKey.OP_READ, client);
				}catch(Exception exp){
					log.error("connected client�����쳣:"+exp.getLocalizedMessage(),exp);
				}
			}
		}
		return processed;
	}
}
