package com.hzjbbis.fk.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * �кż��㹤��
 * @author ������
 */
public class RowNumUtil {

    /**
     * ���㿪ʼ�ͽ����к�
     * @param pageNum ҳ��
     * @param pageSize ҳ��С��С�� 0 ��ʾ�������м�¼
     * @return ������ʼ�ͽ����кŵ� Map��"startRowNum" - ��ʼ��¼�ţ�����������"endRowNum" - ������¼�ţ�������
     */
    public static Map<String,Integer> calcRowNum(int pageNum, int pageSize) {
        // ��ʼ��¼�ţ���������
        int startRowNum = (pageNum - 1) * pageSize;
        // ������¼�ţ�������
        int endRowNum = startRowNum + pageSize;
        if (pageSize < 0) {
            startRowNum = 0;
            endRowNum = Integer.MAX_VALUE;
        }
        
        Map<String,Integer> params = new HashMap<String,Integer>();
        params.put("startRowNum", new Integer(startRowNum));
        params.put("endRowNum", new Integer(endRowNum));
        
        return params;
    }
}
