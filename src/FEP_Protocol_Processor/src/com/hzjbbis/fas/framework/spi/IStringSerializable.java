package com.hzjbbis.fas.framework.spi;

import java.io.Serializable;

/**
 * ֧�ְѶ������л���string�����Լ���string�������л���ʵ�ʶ���
 * ������Ҫʵ��һ��������object
 * @author bhw
 *
 */
public interface IStringSerializable extends Serializable {
	String serialzeToString()throws Exception;
	IStringSerializable deserializeFromString(String buffer)throws Exception;
}
