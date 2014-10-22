package com.hzjbbis.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.DuplicatedRecordException;
import com.hzjbbis.exception.DbUnavailableException;
import com.ibatis.dao.client.DaoException;

/**
 * 提供数据库相关的一些辅助方法
 * @author 张文亮
 */
public class DbUtil {

    private static final Log log = LogFactory.getLog(DbUtil.class);
    private static final DbConfig conf = DbConfig.getInstance();
    
    /**
     * 检查 DAO 异常，然后抛出分类后的异常
     * @param ex DAO 异常
     */
    public static void checkException(DaoException ex) {
        try{
        	log.error("ibatis sql error",ex);		//原样记录 by yangdh 2007-12-17 
        }catch(Exception e){
        	//
        }
    	SQLException cause = getNestedSQLException(ex);
        if (cause == null) {
            throw ex;
        }
        
        int errorCode = cause.getErrorCode();
        String sqlState = cause.getSQLState();
        if (conf.getNotUniqueError().match(errorCode, sqlState)) {
            throw new DuplicatedRecordException("Record is duplicated", ex);
        }
        else if (conf.getNetworkError().match(errorCode, sqlState)) {
            DbMonitor.getInstance().setOnline(false);
            try{
            	log.warn("Database is unavailable!");
            }catch(Exception e){
            	//
            }
            throw new DbUnavailableException("Database is unavailable", ex);
        }
        else if (conf.getFatalError().match(errorCode, sqlState)) {
//            if (pingDb()) {
//                throw ex;
//            }
//            else {
                DbMonitor.getInstance().setOnline(false);
                try{
                	log.warn("Database is unavailable!");
                }catch(Exception e){
                	//
                }
                throw new DbUnavailableException("Database is unavailable", ex);
//            }
        }
        else {
            throw ex;
        }
    }
    
    /**
     * ping 数据库。检查数据库是否能够正常访问
     * @return true - 正常，false - 不正常
     */
    public static boolean pingDb() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName(conf.getDriver());
            conn = DriverManager.getConnection(conf.getUrl(),
                    conf.getUsername(), conf.getPassword());
            stmt = conn.prepareStatement(conf.getPingQuery());
            rs = stmt.executeQuery();
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            if (log.isDebugEnabled()) {
                log.debug("ping db: OK");
            }
            return true;
        }
        catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("ping db: FAILED");
            }
            return false;
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }                
            }
            catch (Exception ex) {
                // 忽略关闭时产生的错误
            }
            try {
            	if (stmt != null) {
                    stmt.close();
                }                
            }
            catch (Exception ex) {
                // 忽略关闭时产生的错误
            }
            try {
            	if (conn != null) {
                    conn.close();
                }               
            }
            catch (Exception ex) {
                // 忽略关闭时产生的错误
            }
        }
    }
    
    /**
     * 取得嵌套的 SQLException
     * @param ex 异常
     * @return 嵌套的 SQLException。如果不是由 SQLException 引起，则返回 null
     */
    private static SQLException getNestedSQLException(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }
        
        return (SQLException) cause;
    }
}