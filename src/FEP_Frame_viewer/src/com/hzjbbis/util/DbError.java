package com.hzjbbis.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * ���ݿ����
 * @author ������
 */
public class DbError {

    /** �����ָ������������SQL״̬��ķָ��� */
    private static final String TOKEN_DELIM = ",";
    /** ����ָ��������뷶Χ�ķָ��� */
    private static final String RANGE_DELIM = "-";
    
    /** ������뼯�� */
    private Set errorCodes;
    /** SQL ״̬�뼯�� */
    private Set sqlStates;
    
    /**
     * ����һ�����ݿ�������
     * @param errorCode ������롣������ "," �ָ����������룬���ָ����Χ��
     *                  �������������֮���� "-" �ָ�
     * @param sqlState SQL ״̬�롣������ "," �ָ���� SQL ״̬��
     */
    public DbError(String errorCode, String sqlState) {
        parseErrorCode(errorCode);
        parseSqlState(sqlState);
    }
    
    /**
     * �ж��Ƿ�ƥ��ָ���Ĵ������
     * @param errorCode �������
     * @return true - ƥ�䣬false - ��ƥ��
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
     * �ж��Ƿ�ƥ��ָ���� SQL ״̬
     * @param sqlState SQL ״̬
     * @return true - ƥ�䣬false - ��ƥ��
     */
    public boolean match(String sqlState) {
        if (sqlStates == null) {
            return false;
        }
        
        return sqlStates.contains(sqlState);
    }
    
    /**
     * �ж��Ƿ�ƥ��ָ���Ĵ������� SQL ״̬
     * @param errorCode �������
     * @param sqlState SQL ״̬
     * @return true - ƥ�䣬false - ��ƥ��
     */
    public boolean match(int errorCode, String sqlState) {
        return match(errorCode) || match(sqlState);
    }
    
    /**
     * �����������
     * @param errorCode ������롣������ "," �ָ����������룬���ָ����Χ��
     *                  �������������֮���� "-" �ָ�
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
     * ���� SQL ״̬��
     * @param sqlState SQL ״̬�롣������ "," �ָ���� SQL ״̬��
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
