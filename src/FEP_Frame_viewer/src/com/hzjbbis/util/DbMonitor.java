package com.hzjbbis.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 数据库监视器。用于监视数据库是否可用
 */
public class DbMonitor {

    private static final Log log = LogFactory.getLog(DbMonitor.class);
    
    // 数据库事件
    //private static final Event DB_AVAILABLE = new Event(EventType.DB_AVAILABLE, null);
    //private static final Event DB_UNAVAILABLE = new Event(EventType.DB_UNAVAILABLE, null);
    
    /** 单例 */
    private static DbMonitor instance = null;
    
    /** 数据库是否在线可用 */
    private boolean online;
    /** 检查数据库状态的间隔时间（毫秒） */
    private int pingInterval;
    
    private DbMonitor() {
        online = true;
        pingInterval = DbConfig.getInstance().getPingInterval();
    }
    
    /**
     * 取得数据库监视器
     * @return 数据库监视器实例
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
     * 触发数据库在线状态已变化事件
     * @param online 变化后的状态
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
     * 启动数据库监视线程
     */
    private void startMonitor() {
        new Monitor().start();
    }
    
    /**
     * 数据库监视线程。定时检查数据库状态，一旦数据库变为可用，则线程终止
     * @author 张文亮
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
                    // 继续监视
                }                
            }
            log.debug("Database monitor stopped");
        }        
    }
}
