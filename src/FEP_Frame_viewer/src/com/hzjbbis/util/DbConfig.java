package com.hzjbbis.util;

/**
 * 前置数据库相关配置
 * @author 张文亮
 */
public class DbConfig {

    /** 单例 */
    private static DbConfig instance;
    /** 数据库配置属性文件名 */
    private static final String CONFIG_FILE = "/database.properties";
    // 属性名常量
    private static final String PROP_DRIVER = "front.db.driver";
    private static final String PROP_URL = "front.db.url";
    private static final String PROP_USERNAME = "front.db.username";
    private static final String PROP_PASSWORD = "front.db.password";
    private static final String PROP_PING_INTERVAL = "front.db.ping.interval";
    private static final String PROP_PING_QUERY = "front.db.ping.query";
    private static final String PROP_ERROR_NOTUNIQUE_ERRORCODE = "front.db.error.notUnique.errorCode";
    private static final String PROP_ERROR_NOTUNIQUE_SQLSTATE = "front.db.error.notUnique.sqlState";
    private static final String PROP_ERROR_NETWORK_ERRORCODE = "front.db.error.network.errorCode";
    private static final String PROP_ERROR_NETWORK_SQLSTATE = "front.db.error.network.sqlState";
    private static final String PROP_ERROR_FATAL_ERRORCODE = "front.db.error.fatal.errorCode";
    private static final String PROP_ERROR_FATAL_SQLSTATE = "front.db.error.fatal.sqlState";
    
    /** JDBC 驱动类名 */
    private String driver;
    /** JDBC URL */
    private String url;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** ping 间隔（毫秒） */
    private int pingInterval;
    /** ping 使用的查询语句 */
    private String pingQuery;
    /** 数据库错误: 记录不唯一 */
    private DbError notUniqueError;
    /** 数据库错误: 网络错误 */
    private DbError networkError;
    /** 数据库错误: 严重错误 */
    private DbError fatalError;
    
    /**
     * 取得数据库配置实例
     * @return 数据库配置实例
     */
    public static DbConfig getInstance() {
        if (instance == null) {
            synchronized (DbConfig.class) {
                if (instance == null) {
                    instance = new DbConfig();
                }
            }
        }
        
        return instance;
    }
    
    private DbConfig() {
        FileBasedProperties props = new FileBasedProperties(CONFIG_FILE);
        driver = props.getProperty(PROP_DRIVER);
        url = props.getProperty(PROP_URL);
        username = props.getProperty(PROP_USERNAME);
        password = props.getProperty(PROP_PASSWORD);        
        pingInterval = props.getInt(PROP_PING_INTERVAL, 0);
        pingQuery = props.getProperty(PROP_PING_QUERY);
        
        notUniqueError = new DbError(props.getProperty(PROP_ERROR_NOTUNIQUE_ERRORCODE),
                props.getProperty(PROP_ERROR_NOTUNIQUE_SQLSTATE));
        networkError = new DbError(props.getProperty(PROP_ERROR_NETWORK_ERRORCODE),
                props.getProperty(PROP_ERROR_NETWORK_SQLSTATE));
        fatalError = new DbError(props.getProperty(PROP_ERROR_FATAL_ERRORCODE),
                props.getProperty(PROP_ERROR_FATAL_SQLSTATE));
    }
    
    /**
     * @return Returns the driver.
     */
    public String getDriver() {
        return driver;
    }
    /**
     * @param driver The driver to set.
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }
    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @return Returns the pingInterval.
     */
    public int getPingInterval() {
        return pingInterval;
    }
    /**
     * @param pingInterval The pingInterval to set.
     */
    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }
    /**
     * @return Returns the pingQuery.
     */
    public String getPingQuery() {
        return pingQuery;
    }
    /**
     * @param pingQuery The pingQuery to set.
     */
    public void setPingQuery(String pingQuery) {
        this.pingQuery = pingQuery;
    }
    /**
     * @return Returns the notUniqueError.
     */
    public DbError getNotUniqueError() {
        return notUniqueError;
    }
    /**
     * @param notUniqueError The notUniqueError to set.
     */
    public void setNotUniqueError(DbError notUniqueError) {
        this.notUniqueError = notUniqueError;
    }
    /**
     * @return Returns the networkError.
     */
    public DbError getNetworkError() {
        return networkError;
    }
    /**
     * @param networkError The networkError to set.
     */
    public void setNetworkError(DbError networkError) {
        this.networkError = networkError;
    }
    /**
     * @return Returns the fatalError.
     */
    public DbError getFatalError() {
        return fatalError;
    }
    /**
     * @param fatalError The fatalError to set.
     */
    public void setFatalError(DbError fatalError) {
        this.fatalError = fatalError;
    }
}
