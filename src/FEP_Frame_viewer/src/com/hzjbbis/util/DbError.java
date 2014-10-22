package com.hzjbbis.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 数据库错误
 * @author 张文亮
 */
public class DbError {

    /** 用来分隔多个错误代码或SQL状态码的分隔符 */
    private static final String TOKEN_DELIM = ",";
    /** 用来指定错误代码范围的分隔符 */
    private static final String RANGE_DELIM = "-";
    
    /** 错误代码集合 */
    private Set errorCodes;
    /** SQL 状态码集合 */
    private Set sqlStates;
    
    /**
     * 构造一个数据库错误对象
     * @param errorCode 错误代码。可以用 "," 分隔多个错误代码，如果指定氛围，
     *                  则两个错误代码之间用 "-" 分隔
     * @param sqlState SQL 状态码。可以用 "," 分隔多个 SQL 状态码
     */
    public DbError(String errorCode, String sqlState) {
        parseErrorCode(errorCode);
        parseSqlState(sqlState);
    }
    
    /**
     * 判断是否匹配指定的错误代码
     * @param errorCode 错误代码
     * @return true - 匹配，false - 不匹配
     */
    public boolean match(int errorCode) {
        if (errorCodes == null) {
            return false;
        }
        
        for (Iterator it = errorCodes.iterator(); it.hasNext();) {
            IntRange range = (IntRange) it.next();
            if (range.contains(errorCode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否匹配指定的 SQL 状态
     * @param sqlState SQL 状态
     * @return true - 匹配，false - 不匹配
     */
    public boolean match(String sqlState) {
        if (sqlStates == null) {
            return false;
        }
        
        return sqlStates.contains(sqlState);
    }
    
    /**
     * 判断是否匹配指定的错误代码或 SQL 状态
     * @param errorCode 错误代码
     * @param sqlState SQL 状态
     * @return true - 匹配，false - 不匹配
     */
    public boolean match(int errorCode, String sqlState) {
        return match(errorCode) || match(sqlState);
    }
    
    /**
     * 解析错误代码
     * @param errorCode 错误代码。可以用 "," 分隔多个错误代码，如果指定氛围，
     *                  则两个错误代码之间用 "-" 分隔
     */
    private void parseErrorCode(String errorCode) {
        if (errorCode == null) {
            return;
        }
        String s = errorCode.trim();
        if (s.length() == 0) {
            return;
        }
        
        StringTokenizer st = new StringTokenizer(s, TOKEN_DELIM);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (token.length() > 0) {
                IntRange range = null;
                int pos = token.indexOf(RANGE_DELIM);
                if (pos > 0) {
                    int min = Integer.parseInt(s.substring(0, pos));
                    int max = Integer.parseInt(s.substring(pos + 1));
                    range = new IntRange(min, max);
                }
                else {
                    int val = Integer.parseInt(token);
                    range = new IntRange(val, val);
                }
                
                if (errorCodes == null) {
                    errorCodes = new HashSet();
                }
                errorCodes.add(range);
            }
        }
    }
    
    /**
     * 解析 SQL 状态码
     * @param sqlState SQL 状态码。可以用 "," 分隔多个 SQL 状态码
     */
    private void parseSqlState(String sqlState) {
        if (sqlState == null) {
            return;
        }
        String s = sqlState.trim();
        if (s.length() == 0) {
            return;
        }
        
        StringTokenizer st = new StringTokenizer(s, TOKEN_DELIM);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (token.length() > 0) {
                if (sqlStates == null) {
                    sqlStates = new HashSet();
                }
                sqlStates.add(token);
            }
        }
    }
}
