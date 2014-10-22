package com.hzjbbis.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ���ݿ�����������ڼ������ݿ��Ƿ����
 */
public class DbMonitor {

    private static final Log log = LogFactory.getLog(DbMonitor.class);
    
    // ���ݿ��¼�
    //private static final Event DB_AVAILABLE = new Event(EventType.DB_AVAILABLE, null);
    //private static final Event DB_UNAVAILABLE = new Event(EventType.DB_UNAVAILABLE, null);
    
    /** ���� */
    private static DbMonitor instance = null;
    
    /** ���ݿ��Ƿ����߿��� */
    private boolean online;
    /** ������ݿ�״̬�ļ��ʱ�䣨���룩 */
    private int pingInterval;
    
    private DbMonitor() {
        online = true;
        pingInterval = DbConfig.getInstance().getPingInterval();
    }
    
    /**
     * ȡ�����ݿ������
     * @return ���ݿ������ʵ��
     */
    public static DbMonitor getInstance() {
        if (instance == null) {
            synchronized (DbMonitor.class) {
                if (instance == null) {
                    instance = new DbMonitor();
                }
            }
        }
        
        return instance;
    }

    /**
     * @return Returns the online.
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * @param online The online to set.
     */
    public void setOnline(boolean online) {
        if (this.online != online) {
            this.online = online;
            if (online == false) {
                startMonitor();
            }
            fireDbStatusChanged(online);
        }
    }
    
    /**
     * �������ݿ�����״̬�ѱ仯�¼�
     * @param online �仯���״̬
     */
    private void fireDbStatusChanged(boolean online) {
        log.warn("The database became " + (online ? "available" : "unavailable"));
        if (online) {
            //ModuleControler.getControler().fireEvent(null, DB_AVAILABLE);
        }
        else {
            //ModuleControler.getControler().fireEvent(null, DB_UNAVAILABLE);
        }
    }
    
    /**
     * �������ݿ�����߳�
     */
    private void startMonitor() {
        new Monitor().start();
    }
    
    /**
     * ���ݿ�����̡߳���ʱ������ݿ�״̬��һ�����ݿ��Ϊ���ã����߳���ֹ
     * @author ������
     */
    private class Monitor extends Thread {

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run() {
            log.debug("Start database monitor");
            while (true) {
                try {
                    Thread.sleep(pingInterval);
                    if (DbUtil.pingDb()) {
                        DbMonitor.this.setOnline(true);
                        break;
                    }
                }
                catch (InterruptedException e) {
                    // ��������
                }                
            }
            log.debug("Database monitor stopped");
        }        
    }
}
