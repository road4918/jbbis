package com.hzjbbis.fk.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ���鹤����
 */
public class ArraysUtil {

    /**
     * �ж��������Ƿ����������Ԫ��
     * @param elements ����
     * @param element ������Ԫ��
     * @return true - ����
     */
    public static boolean contains(Object[] elements, Object element) {
        return indexOf(elements, element) > -1;
    }
    
    /**
     * ���ظ���Ԫ���������е�λ��
     * @param elements ����
     * @param element ������Ԫ��
     * @return �� 0 ��ʼ��λ�á����������û�и�����Ԫ�أ����� -1
     */
    public static int indexOf(Object[] elements, Object element) {
        if (elements == null || element == null) {
            return -1;
        }
        
        int index = -1;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == null) {
                continue;
            }
            
            if (elements[i].equals(element)) {
                index = i;
                break;
            }
        }
        
        return index;
    }
    
    /**
     * ������Ԫ��ת�Ƶ��б��У�����������б��б�Ĵ�С��Ԫ��˳��������һ��
     * @param elements ����
     * @return �б�
     */
    public static List<Object> asList(Object[] elements) {
        return asList(elements, 0, elements.length);
    }
    
    /**
     * ������Ԫ��ת�Ƶ��б��У�����������б��б�Ĵ�СΪ length ָ����ֵ�����
     * offset + length �������������±꣬���б�Ĵ�СΪ elements.length - offset
     * @param elements ����
     * @param offset ƫ����
     * @param length ����
     * @return �б�
     */
    public static List<Object> asList(Object[] elements, int offset, int length) {
        int size = length;
        if (offset + length >= elements.length) {
            size = elements.length - offset;
        }
        
        List<Object> l = new ArrayList<Object>(size);
        for (int i = 0; i < length; i++) {
            int index = offset + i;
            if (index >= elements.length) {
                break;
            }
            l.add(elements[index]); 
        }
        
        return l;
    }
    
    /**
     * ������תΪ�ַ�������
     * @param elements ����
     * @return �ַ�����������ʽΪ [e1, e2, e3, ...]
     */
    public static String asString(Object[] elements) {
        if (elements == null) {
            return "null";
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements[i].toString());
        }
        sb.append("]");
        
        return sb.toString();
    }
}
