package com.hzjbbis.fas.protocol.conf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * Э���������
 * @author ������
 */
public class ProtocolDataItemConfig implements IDataItem{

    /** ȱʡ bean ���� */
    public static final String MISC_BEAN_CLASS = "com.hzjbbis.fas.model.RtuMiscData";
    /** ����ӳ��� */
    private static final Map alias = new HashMap();
    
    /** ���ݱ�ʶ���� */
    private String code;
    /** �������ݳ��� */
    private int length;
    /** ���� ����λparseno+����λfraction*/
    private String type;
    /** ��Լ�������ͣ���Ӧ�ڽ�������� */
    private int parserno=0;
    /** ���ܰ�����С��λ�� */
    private int fraction=0;
    
    private String keychar;
    
    /** 
     * ������������ Bean �����ơ�����ָ���������������������� Bean �������£�
     *   ����            ��������
     * =============== =============================================
     *   raw            com.hzjbbis.fas.model.RtuRawData
     *   extremum       com.hzjbbis.fas.model.RtuExtremumData
     *   loadcurve      com.hzjbbis.fas.model.RtuLoadCurveData
     *   electricenergy com.hzjbbis.fas.model.RtuElectricEnergyData
     *   misc           com.hzjbbis.fas.model.RtuMiscData
     * =============== =============================================
     * ���������ֵΪ null����Ĭ��Ϊ misc
     */
    private String bean;
    /** ��Ӧ Bean �������� */
    private String property;
    /** ��������� */
    private List childItems;
    /** ���ݱ�ʶ*/
    private int datakey;
    /** Bean ���������� */
    private String beanClass;
    /** ��Ӧ�ı�׼���ݱ�ʶ*/
    private List items;
    
    private int dkey;
    
    // ��ʼ������ӳ���
    static {
        alias.put("raw", "com.hzjbbis.fas.model.RtuRawData");
        alias.put("extremum", "com.hzjbbis.fas.model.RtuExtremumData");
        alias.put("loadcurve", "com.hzjbbis.fas.model.RtuLoadCurveData");
        alias.put("electricenergy", "com.hzjbbis.fas.model.RtuElectricEnergyData");
        alias.put("misc", "com.hzjbbis.fas.model.RtuMiscData");
    }
    
    /**
     * �������ݱ�ʶ������ֵ
     * @return ���ݱ�ʶ������ֵ
     */
    public int getDataKey(){
    	return datakey;
    }
    
    public int getDatakey(){
    	return datakey;
    }
    
    /**
     * ������� Bean ����������
     * @return Bean ����������
     */
    public String getBeanClass() {
        // ������ӳ��
        if (beanClass == null) {
            beanClass = (String) alias.get(bean);
        }
        
        // ������ܸ��ݱ���ӳ�䣬������������
        if (beanClass == null) {
            Iterator it = alias.values().iterator();
            while (it.hasNext()) {
                String clazz = (String) it.next();
                if (clazz.equals(bean)) {
                    beanClass = bean;
                    break;
                }
            }
        }
        
        // ���ָ���� bean ���ƷǷ�����ʹ��ȱʡ����
        if (beanClass == null) {
            beanClass = MISC_BEAN_CLASS;
        }
        
        return beanClass;
    }
    
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
        this.datakey=ParseTool.HexToDecimal(code);
    }
    /**
     * @return Returns the length.
     */
    public int getLength() {        
        if (length == 0 && childItems != null) {
            for (int i = 0; i < childItems.size(); i++) {
                length += ((ProtocolDataItemConfig) childItems.get(i)).getLength();
            }
        }
        return length;
    }
    /**
     * @param length The length to set.
     */
    public void setLength(int length) {
        this.length = length;
    }
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
        try{
        	if(type!=null){
        		if(type.length()>3){
        			this.parserno=Integer.parseInt(type.substring(0,2));
        			this.fraction=Integer.parseInt(type.substring(2,4));
        		}
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return bean;
    }
    /**
     * @param bean The bean to set.
     */
    public void setBean(String bean) {
        this.bean = bean;
        this.beanClass = null;
    }
    /**
     * @return Returns the property.
     */
    public String getProperty() {
        return property;
    }
    /**
     * @param property The property to set.
     */
    public void setProperty(String property) {
        this.property = property;
    }
    /**
     * @return Returns the childItems.
     */
    public List getChildItems() {
        return childItems;
    }
    /**
     * @param childItems The childItems to set.
     */
    public void setChildItems(List childItems) {
        this.childItems = childItems;
        length = 0;
    }

	/**
	 * @return Returns the fraction.
	 */
	public int getFraction() {
		return fraction;
	}

	/**
	 * @return Returns the parserno.
	 */
	public int getParserno() {
		return parserno;
	}

	public String getSdRobot() {		
		return null;
	}

	public List getStandardDatas() {		
		return items;
	}

	public boolean isMe(String dataid) {		
		boolean rt=false;
		if(items!=null){
			for(Iterator iter=items.iterator();iter.hasNext();){
				String dk=(String)iter.next();
				if(dk.equalsIgnoreCase(dataid)){
					rt=true;
					break;
				}
			}
		}
		return rt;
	}

	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public String getKeychar() {
		return keychar;
	}

	public void setKeychar(String keychar) {
		this.keychar = keychar;
	}

	public int getDkey() {
		return dkey;
	}

	public void setDkey(int dkey) {
		this.dkey = dkey;
	}
    
    
}
