package com.hzjbbis.fk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * �澯���붨��
 * @author ������
 */
public class RtuAlertCode {

    /** �澯���� */
    private String code;
    /** �澯��������[String] */
    private List<String> args=new ArrayList<String>();	//��δ֧�ָ澯��������
    
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    /**
     * @return Returns the args.
     */
    public List<String> getArgs() {
        return args;
    }
    /**
     * @param args The args to set.
     */
    public void setArgs(List<String> args) {
        if(args!=null){
        	this.args = args;
        }
    }
}
