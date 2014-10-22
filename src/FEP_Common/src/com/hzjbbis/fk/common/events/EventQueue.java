package com.hzjbbis.fk.common.events;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.exception.EventQueueFullException;
import com.hzjbbis.fk.exception.EventQueueLockedException;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * <p>Title: Java Socket Server with NIO support </p>
 * <p>Description:ʵ���¼����� </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author bhw
 * @version 1.0
 */

public class EventQueue	implements Serializable 
{
	private static final long serialVersionUID = 200603141443L;
	private static final TraceLog tracer = TraceLog.getTracer();
	
	private static final int DEFAULT_QUEUE_SIZE = 1024;
	private int capacity = 1024*100;
	private final Object lock = new Object();

	private IEvent[] events ;
	private int first = 0;
	private int last = 0;
	private int size = 0;
	private int waiting = 0;
	
	private boolean writable = true;
	private boolean readable = true; 

	/**
	 * Construct a new, empty <code>Queue</code> with the specified initial
	 * capacity.
	 */
	public EventQueue(int initialCapacity) {
		events = new IEvent[initialCapacity];
	}

	/**
	 * construct a new queue with default initial capacity
	 */

	public EventQueue() {
		events = new IEvent[DEFAULT_QUEUE_SIZE];
	}

	/**
	 * �Ӷ��г����Ƴ�����Ԫ��.
	 */
	public void clear() {
		synchronized(lock){
			Arrays.fill(events, null);
			first = 0;
			last = 0;
			size = 0;
			lock.notifyAll();
		}
	}
	
	/**
	 * �������Ƴ��˶��е�ͷ��������˶��в������κ�Ԫ�أ���һֱ�ȴ���
	 * ע��take ��poll�Ĳ��졣
	 * @return
	 */
	public IEvent take() throws InterruptedException{
		synchronized(lock){
			IEvent e=null;
			waiting++;
			while( null == (e=poll()) ){
				lock.wait();
			}
			waiting--;
			return e;
		}
	}
	
	/**
	 * ��ָ����Ԫ����ӵ����е�β�������б�Ҫ����ȴ��ռ��ÿ��á�
	 * ע��put��offer�Ĳ��졣
	 * @param evt
	 * �׳���
	 * 		InterruptedException - ����ڵȴ�ʱ�жϡ�
	 * 		NullPointerException - ���ָ����Ԫ��Ϊ null��
	 */
	public void put( IEvent evt) throws InterruptedException{
		if( null == evt )
			throw new NullPointerException();
		throw new RuntimeException("��δʵ�ָù��ܡ�");
	}
	
	/**
	 * Inserts the given element at the beginning of this queue
	 * @param evt
	 * @return
	 */
	public boolean addFirst(IEvent evt) throws EventQueueLockedException,EventQueueFullException{
		if( null == evt )
			return false;
		synchronized(lock){
			if( evt.getType() != EventType.SYS_KILLTHREAD  && !writable )
				throw new EventQueueLockedException("Invalid offer while eventQueue disable put into.");
			if (size == events.length) {
				// expand queue
				if( size>= capacity ){
					//����������������ֵ�����ܲ��뵽������.
					String info = "����������������ֵ�����ܲ��뵽�����С�size="+size;
					tracer.trace(info);
					throw new EventQueueFullException(info);
				}
				final int oldLen = events.length;
				IEvent[] newEvents = new IEvent[oldLen * 2];

				if (first < last) {
					System.arraycopy(events, first, newEvents, 0, last - first);
				} else {
					System.arraycopy(events, first, newEvents, 0, oldLen - first);
					System.arraycopy(events, 0, newEvents, oldLen - first, last);
				}

				first = 0;
				last = oldLen;
				events = newEvents;
			}

			//����ͷ��
			if( --first < 0 ){
				first = events.length-1;
			}
			events[first] = evt;
			size++;

			if (waiting > 0) {
				lock.notifyAll();
			}
			return true;
		}
	}

	/**
	 * �������Ƴ��˶��е�ͷ������˶���Ϊ�գ��򷵻� null��
	 * @return
	 */
	public IEvent poll() {
		synchronized(lock){
			if (size == 0 ) {
				return null;
			}
			if( !readable ){
				//���ͷ���¼���killThread��������
				if( events[first].getType() != EventType.SYS_KILLTHREAD )
					return null;
			}
			IEvent event = events[first];
			events[first] = null;
			first++;

			if (first == events.length) {
				first = 0;
			}

			size--;
			return event;
		}
	}
	
	/**
	 * Removes at most the given number of available elements from this queue and adds them 
	 * into the given collection��
	 * ��Ҫ�ȴ�timeout millisecondsʱ��
	 * @return the number of elements transferred.
	 */
	public int drainTo(Collection<IEvent> c,int maxElements,long timeout) {
		if( timeout<0 )
			timeout = 0;
		synchronized(lock){
			long mark = System.currentTimeMillis();
			if( maxElements<=0 )
				maxElements = size;
			for(int i=0;i<maxElements; i++){
				while (size == 0) {
					if( System.currentTimeMillis()-mark >= timeout )
						return i;
					try{
						if( timeout>0 )
							lock.wait(timeout);
					}
					catch(Exception e){}
				}
				c.add(events[first]);
				events[first] = null;
				first++;
				if (first == events.length) {
					first = 0;
				}
				size--;
			}
			return maxElements;
		}
	}
	
	/**
	 * ������ܣ��ڶ���β������ָ����Ԫ�أ���������������������ء�
	 * @param o Ҫ��ӵ�Ԫ��
	 * @return ���������˶������Ԫ�أ��򷵻� true�����򷵻� false��
	 * �׳��� NullPointerException - ���ָ����Ԫ��Ϊ null��
	 */
	public boolean offer(IEvent evt) throws EventQueueLockedException,EventQueueFullException,NullPointerException
	{
		if( null == evt )
			throw new NullPointerException();
		synchronized(lock){
			if( evt.getType() != EventType.SYS_KILLTHREAD  && ! writable )
				throw new EventQueueLockedException("Invalid offer while eventQueue disable put into.");
			if (size == events.length) {
				// expand queue
				if( size>= capacity ){
					//����������������ֵ�����ܲ��뵽������.
					String info = "����������������ֵ�����ܲ��뵽�����С�size="+size;
					tracer.trace(info);
					throw new EventQueueFullException(info);
				}
				final int oldLen = events.length;
				IEvent[] newEvents = new IEvent[oldLen * 2];

				if (first < last) {
					System.arraycopy(events, first, newEvents, 0, last - first);
				} else {
					System.arraycopy(events, first, newEvents, 0, oldLen - first);
					System.arraycopy(events, 0, newEvents, oldLen - first, last);
				}

				first = 0;
				last = oldLen;
				events = newEvents;
			}

			events[last++] = evt;

			if (last == events.length) {
				last = 0;
			}

			size++;

			if (waiting > 0) {
				lock.notifyAll();
			}
			return true;
		}
	}


	/**
	 * Returns <code>true</code> if the queue is empty.
	 */
	public boolean isEmpty() {
		return (size == 0);
	}

	/**
	 * Returns the number of elements in the queue.
	 */
	public int size() {
		return size;
	}
	
	public void setCapacity(int capacity){
		this.capacity = capacity;
	}
	
	public int capacity(){
		return capacity;
	}
	
	/**
	 * false,������Ӳ���Ԫ�ص����У�����SYS_KILLTHREAD�¼���
	 * true,�������Ԫ�ص�����
	 */
	public void enableOffer(boolean putable){
		synchronized(lock){
			this.writable = putable;
		}
	}
	
	public boolean enableOffer(){
		return writable;
	}
	
	/**
	 * 
	 */
	public void enableTake(boolean takable){
		synchronized(lock){
			readable = takable;
			if( takable && size>0 )
				lock.notifyAll();
		}
	}
	
	public boolean enableTake(){
		return readable;
	}
	
	public void lockQueue(){
		enableOffer(false);
		enableTake(false);
	}
	
	public void unlockQueue(){
		enableTake(true);
		enableOffer(true);
	}
	
}