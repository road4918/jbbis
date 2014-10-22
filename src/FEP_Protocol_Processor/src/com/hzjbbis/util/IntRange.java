package com.hzjbbis.util;

/**
 * ������Χ
 * @author ������
 */
public class IntRange {

    /** ��Сֵ�������� */
    private int min;
    /** ���ֵ�������� */
    private int max;
    private int hashCode = 0;
    
    /**
     * ����һ��������Χ
     * @param min ��Сֵ��������
     * @param max ���ֵ��������
     */
    public IntRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must equal or greater than min");
        }
        
        this.min = min;
        this.max = max;
        this.hashCode = 37 * (37 * 17 + min) + max;
    }
    
    /**
     * �жϸ�����ֵ�Ƿ�����ڷ�Χ��
     * @param val ������ֵ
     * @return true - ������false - ������
     */
    public boolean contains(int val) {
        return (val >= min && val <= max);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof IntRange)) {
            return false;
        }
        
        IntRange range = (IntRange) obj;
        return (this.min == range.min && this.max == range.max);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return hashCode;
    }
}
