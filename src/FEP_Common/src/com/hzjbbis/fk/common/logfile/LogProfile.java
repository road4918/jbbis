/**
 * ��Ҫ��;������ϵͳ��Profile��Ϣ��¼��־����ֹ������־��ˢ������profile��Ϣ�Ҳ�����
 * ע�������log4j.properties�ļ��У���LogProfileר�����������һ���ļ���
 */
package com.hzjbbis.fk.common.logfile;

import org.apache.log4j.Logger;

/**
 * @author hbao
 *
 */
public class LogProfile {
	private static final Logger log = Logger.getLogger(LogProfile.class);
	public static final Logger getLog(){
		return log;
	}
}
