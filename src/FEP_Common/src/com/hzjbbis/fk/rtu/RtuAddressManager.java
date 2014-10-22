/**
 * �����ն�RTU��ͨ�ŵ�IP��PORT��ַ�� ip:port portΪHex�ַ���.
 * RtuAddress�������ڼ�¼���С����б�����־��
 */
package com.hzjbbis.fk.rtu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hbao
 * 2008-06-12 20:05
 */
public class RtuAddressManager {
	private static final Map<Integer,RtuAddress> map = Collections.synchronizedMap(new HashMap<Integer,RtuAddress>(5120*10+7));

	public static void put(int rtu,String peerAddr ){
		RtuAddress addr = map.get(rtu);
		if( null != addr )
			addr.setPeerAddr(peerAddr);
		else{
			addr = new RtuAddress(peerAddr);
			map.put(rtu, addr);
		}
	}

	public static RtuAddress get(int rtua){
		return map.get(rtua);
	}
	
}
