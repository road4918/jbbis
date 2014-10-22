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
 * �ṩ���ݿ���ص�һЩ��������
 * @author ������
 */
public class DbUtil {

    private static final Log log = LogFactory.getLog(DbUtil.class);
    private static final DbConfig conf = DbConfig.getInstance();
    
    /**
     * ��� DAO �쳣��Ȼ���׳��������쳣
     * @param ex DAO �쳣
     */
    public static void checkException(DaoException ex) {
        try{
        	log.error("ibatis sql error",ex);		//ԭ����¼ by yangdh 2007-12-17 
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
     * ping ���ݿ⡣������ݿ��Ƿ��ܹ���������
     * @return true - ������false - ������
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
                // ���Թر�ʱ�����Ĵ���
            }
            try {
            	if (stmt != null) {
                    stmt.close();
                }                
            }
            catch (Exception ex) {
                // ���Թر�ʱ�����Ĵ���
            }
            try {
            	if (conn != null) {
                    conn.close();
                }               
            }
            catch (Exception ex) {
                // ���Թر�ʱ�����Ĵ���
            }
        }
    }
    
    /**
     * ȡ��Ƕ�׵� SQLException
     * @param ex �쳣
     * @return Ƕ�׵� SQLException����������� SQLException �����򷵻� null
     */
    private static SQLException getNestedSQLException(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }
        
        return (SQLException) cause;
    }
}