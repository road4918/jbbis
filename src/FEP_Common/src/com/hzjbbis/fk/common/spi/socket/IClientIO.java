/**
 * �첽TCPͨѶ�Ŀͻ���socket����д�ӿ�
 */
package com.hzjbbis.fk.common.spi.socket;

import com.hzjbbis.fk.exception.SocketClientCloseException;

/**
 * @author bao
 * 2008-05-29 16:45
 */
public interface IClientIO {

	/**
	 * ��SocketIoThread��⵽�������¼�������øýӿڡ�
	 * @param client
	 * @return true if all data received, false socket��������������û�ж�ȡ��
	 */
	boolean onReceive(IServerSideChannel client) throws SocketClientCloseException;
	
	/**
	 * ��SocketIoThread��⵽���Է��������¼������øýӿڡ�
	 * @param client
	 * @return true�����ȫ�����ݷ�����ϣ�false����������û�з�����ϡ�
	 */
	boolean onSend(IServerSideChannel client)throws SocketClientCloseException;
}
