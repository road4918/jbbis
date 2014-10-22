package com.hzjbbis.fas.protocol.conf;

import java.util.List;


/**
 *@filename	IItemParser.java
 *@auther	netice
 *@date		2007-3-15
 *@version	1.0
 *TODO
 */
public interface IItemParser {
	/**
	 * ����
	 * @param data		������
	 * @param pos		������ʼλ��
	 * @param para		����
	 * @param result	�������(���ز���)
	 * @param no       ·��
	 * @return			��������
	 */
	public abstract int parse(byte[] data,int pos,Object para,Long cmdId,List result);
	
	/**
	 * ��֡
	 * @param frame		֡������
	 * @param pos		��ʼ���λ��
	 * @param para      ����
	 * @return          ��֡����
	 */
	public abstract int construct(byte[] frame,int pos,Object para,int no);
}
