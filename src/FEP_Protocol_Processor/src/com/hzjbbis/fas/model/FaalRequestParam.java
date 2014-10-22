/*
 * Created on 2006-2-27
 */
package com.hzjbbis.fas.model;

import java.io.Serializable;

/**
 * FAAL ͨѶ�������
 * @author ������
 */
public class FaalRequestParam implements Serializable {
    
    private static final long serialVersionUID = 8826872062189860703L;
    
    /** �������� */
    private String name;
    /** ����ֵ */
    private String value;

    /**
     * ����һ���������
     */
    public FaalRequestParam() {
        super();
    }
    
    /**
     * ����һ���������
     * @param name ��������
     * @param value ����ֵ
     */
    public FaalRequestParam(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return "DI" + name + "=" + value;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
