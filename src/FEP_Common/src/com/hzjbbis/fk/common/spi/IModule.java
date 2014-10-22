// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi 
// Source File Name:   IModule.java

package com.hzjbbis.fk.common.spi;

/**
 * �ļ���ʧ�󣬷���������ġ�
 * ���ӿ���Ҫ֧�ֿɼ�ع����ģ�鶨�塣
 * @author bhw
 *
 */

public interface IModule extends IModStatistics,IProfile
{

    public static final String MODULE_TYPE_SOCKET_SERVER = "socketServer";
    public static final String MODULE_TYPE_SOCKET_CLIENT = "socketClient";
    public static final String MODULE_TYPE_MESSAGE_QUEUE = "messageQueue";
    public static final String MODULE_TYPE_EVENT_HOOK = "eventHook";
    public static final String MODULE_TYPE_GPRS_CLIENT = "gprsClient";
    public static final String MODULE_TYPE_UMS_CLIENT = "umsClient";
    public static final String MODULE_TYPE_DB_SERVICE = "dbService";
    public static final String MODULE_TYPE_BP = "businessProcessor";
    public static final String MODULE_TYPE_CONTAINER = "moduleContainer";

    public String getModuleType();

    public String getName();

    public boolean start();

    public void stop();

    public boolean isActive();

	/**
	 * ��������Ӧ��ͨ�ŷ�ʽ�� 
	 * 		01:����; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 		05:����; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 * @return
	 */
	String getTxfs();
}
