/**
 * �����ļ�
 */
package com.hzjbbis.fk.monitor.biz;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.hzjbbis.fk.monitor.exception.MonitorHandleException;

/**
 * @author hbao
 *	���������ļ���һ��һ����������ϡ�������־�ļ�����Ҫ�ָ����ء�
 */
public class HandleFile {
	public static final int MAX_BLOCK = 1024*1024;
	private static final HandleFile handleFile = new HandleFile();
	public static final HandleFile getHandleFile(){
		return handleFile;
	}

	/**
	 * ��ȡ�ļ���ÿ������ȡ1M��
	 * @param inputBody���������еı����塣���и�ʽpath+0+long  
	 * path�ַ�������0��β
	 * @return��
	 */
	public ByteBuffer getFile(ByteBuffer inputBody){
		String path="";
		byte[] btPath = null;
		long position = -1;
		int index = 0;
		for(index=0; index<inputBody.limit(); index++ ){
			if( 0 == inputBody.get(index) ){
				btPath = new byte[index];
				inputBody.get(btPath);
				path = new String(btPath);
				inputBody.get();		//skip 0
				position = inputBody.getLong();
				inputBody.rewind();
				break;
			}
		}
		if( -1 == position || null == btPath )
			throw new MonitorHandleException("��ع�����ȡ�ļ��쳣������Ƿ���");
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"r");
			int len = (int)(raf.length()-position);
			if( len<=0 ){
				return inputBody;
			}
			int toRead = MAX_BLOCK;
			toRead = Math.min(len, toRead);
			ByteBuffer body = ByteBuffer.allocate(inputBody.remaining()+toRead);
			raf.seek(position);
			body.put(inputBody);
			inputBody.rewind();
			raf.read(body.array(), inputBody.remaining(), toRead);
			body.position(0);
			raf.close();
			return body;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
	/**
	 * д�ļ���
	 * @param inputBody���������еı����塣���и�ʽpath+0+long(offset)+������
	 * path�ַ�������0��β
	 * @return��
	 */
	public ByteBuffer putFile(ByteBuffer inputBody){
		String path="";
		byte[] btPath = null;
		long position = -1;
		int index = 0;
		for(index=0; index<inputBody.limit(); index++ ){
			if( 0 == inputBody.get(index) ){
				btPath = new byte[index];
				inputBody.get(btPath);
				path = new String(btPath);
				inputBody.get();		//skip 0
				position = inputBody.getLong();
				break;
			}
		}
		int offset = inputBody.position();
		int dataLen = inputBody.remaining();
		if( -1 == position || null == btPath)
			throw new MonitorHandleException("��ع���д�ļ������쳣������Ƿ���");
		
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"rwd");
			if( position> raf.length() ){
				raf.close();
				throw new MonitorHandleException("��ع���д�ļ������쳣��д�ļ���ʼλ��>�ļ�ʵ�ʳ��ȡ�");
			}
			
			raf.setLength(position);
			raf.seek(position);
			raf.write(inputBody.array(), offset, dataLen);
			raf.close();
			position += dataLen;
			
			inputBody.clear();
			inputBody.put(btPath);	inputBody.put((byte)0);
			inputBody.putLong(position);
			inputBody.flip();
			return inputBody;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
}
