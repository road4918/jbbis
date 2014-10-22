/**
 * ���ģ��ͻ��ˣ��ļ����䴦��
 */
package com.hzjbbis.fk.monitor.client.biz;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.hzjbbis.fk.monitor.biz.HandleFile;
import com.hzjbbis.fk.monitor.exception.MonitorHandleException;
import com.hzjbbis.fk.utils.PathUtil;

/**
 * @author hbao
 *	���������ļ���һ��һ����������ϡ�������־�ļ�����Ҫ�ָ����ء�
 */
public class ClientHandleFile {
	private static final ClientHandleFile handleFile = new ClientHandleFile();
	private static String tmpPath = ".";
	public static final ClientHandleFile getHandleFile(){
		return handleFile;
	}
	//������ʱ�ļ����Ŀ¼��
	static {
		String root = PathUtil.getRootPath(ClientHandleFile.class);
		File f = new File(root);
		if( "bin".equals(f.getName()) || "classes".equals(f.getName())){
			f = f.getParentFile();
			root = f.getPath();
		}
		if( "plugins".equalsIgnoreCase(f.getName()) ){
			f = f.getParentFile();
			root = f.getPath();
		}
		tmpPath = root + File.separator + "tmp";
		f = new File(tmpPath);
		if( ! f.exists() )
			f.mkdir();
		tmpPath += File.separator;
		System.out.println("tmp="+tmpPath);
	}

	/**
	 * �ӷ�������ȡ�ļ���ÿ������ȡ1M��
	 * @param inputBody�����������Ӧ��ı����塣���и�ʽpath+0+long + �ļ������� 
	 * path�ַ�������0��β
	 * @return��������ȡ���ݵ�body��null��ʾ�ļ���ȡ��ϡ�
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
				break;
			}
		}
		if( position < 0 || null == btPath )
			throw new MonitorHandleException("��ع�����ȡ�ļ��쳣������Ƿ���");
		//���ص��ļ����ŵ���ʱĿ¼����ǰ����Ŀ¼/tmp/
		path = path.replace('\\',File.separatorChar);
		path = path.replace('/', File.separatorChar);
		//��⣬����������·������ҪԤ�Ƚ������·����
		if( path.lastIndexOf(File.separatorChar)> 0 ){
			int index1 = path.lastIndexOf(File.separatorChar);
			String subDir = path.substring(0, index1);
			File subFile = new File(tmpPath+subDir);
			if( !subFile.exists() )
				subFile.mkdirs();
		}
		path = tmpPath + path;
		
		int offset = inputBody.position();			//���ݿ�ʼ��ȡ��λ��
		int dataLen = inputBody.remaining();		//���ݳ���
		if( 0 == dataLen )
			return null;
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
			if( dataLen < HandleFile.MAX_BLOCK )
				return null;			//�ļ���ȡ���
			//���������ȡ��������
			inputBody.clear();
			inputBody.put(btPath);	inputBody.put((byte)0);
			inputBody.putLong(position);
			inputBody.flip();
			return inputBody;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
	/**
	 * ��������ϴ��ļ���
	 * @param inputBody���������еı����塣���и�ʽpath+0+long(offset)+������
	 * path�ַ�������0��β
	 * @return��null��ʾû�к����ϴ���
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
				inputBody.limit(inputBody.position());
				inputBody.rewind();
				break;
			}
		}
		if( position < 0 )
			throw new MonitorHandleException("��ع����ϴ��ļ������쳣������Ƿ���");
		//���ص��ļ����ŵ���ʱĿ¼����ǰ����Ŀ¼/tmp/
		path = path.replace('\\',File.separatorChar);
		path = path.replace('/', File.separatorChar);
		path = tmpPath + path;
		
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"r");
			if( position> raf.length() ){
				raf.close();
				throw new MonitorHandleException("��ع���д�ļ������쳣�����ļ���ʼλ��>�ļ�ʵ�ʳ��ȡ�");
			}
			int len = (int)(raf.length()-position);
			if( len<=0 ){
				return null;	//�ļ��ϴ����
			}
			int toRead = HandleFile.MAX_BLOCK;
			toRead = Math.min(len, toRead);
			ByteBuffer body = ByteBuffer.allocate(inputBody.remaining()+toRead);
			
			raf.seek(position);
			body.put(inputBody);
			inputBody.rewind();
			raf.read(body.array(), inputBody.limit(), toRead);
			body.position(0);
			raf.close();
			return body;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
}
