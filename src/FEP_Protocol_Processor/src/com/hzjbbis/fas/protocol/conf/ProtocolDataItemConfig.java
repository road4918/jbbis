package com.hzjbbis.fas.protocol.conf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * 协议数据项定义
 * @author 张文亮
 */
public class ProtocolDataItemConfig implements IDataItem{

    /** 缺省 bean 名称 */
    public static final String MISC_BEAN_CLASS = "com.hzjbbis.fas.model.RtuMiscData";
    /** 别名映射表 */
    private static final Map alias = new HashMap();
    
    /** 数据标识代码 */
    private String code;
    /** 数据内容长度 */
    private int length;
    /** 类型 高四位parseno+低四位fraction*/
    private String type;
    /** 规约解析类型，对应于解析器编号 */
    private int parserno=0;
    /** 可能包含的小数位数 */
    private int fraction=0;
    
    private String keychar;
    
    /** 
     * 数据项所属的 Bean 的名称。可以指定完整类名或别名，允许的 Bean 名称如下：
     *   别名            完整类名
     * =============== =============================================
     *   raw            com.hzjbbis.fas.model.RtuRawData
     *   extremum       com.hzjbbis.fas.model.RtuExtremumData
     *   loadcurve      com.hzjbbis.fas.model.RtuLoadCurveData
     *   electricenergy com.hzjbbis.fas.model.RtuElectricEnergyData
     *   misc           com.hzjbbis.fas.model.RtuMiscData
     * =============== =============================================
     * 如果该属性值为 null，则默认为 misc
     */
    private String bean;
    /** 对应 Bean 的属性名 */
    private String property;
    /** 子数据项定义 */
    private List childItems;
    /** 数据标识*/
    private int datakey;
    /** Bean 的完整类名 */
    private String beanClass;
    /** 对应的标准数据标识*/
    private List items;
    
    private int dkey;
    
    // 初始化别名映射表
    static {
        alias.put("raw", "com.hzjbbis.fas.model.RtuRawData");
        alias.put("extremum", "com.hzjbbis.fas.model.RtuExtremumData");
        alias.put("loadcurve", "com.hzjbbis.fas.model.RtuLoadCurveData");
        alias.put("electricenergy", "com.hzjbbis.fas.model.RtuElectricEnergyData");
        alias.put("misc", "com.hzjbbis.fas.model.RtuMiscData");
    }
    
    /**
     * 返回数据标识的整型值
     * @return 数据标识的整型值
     */
    public int getDataKey(){
    	return datakey;
    }
    
    public int getDatakey(){
    	return datakey;
    }
    
    /**
     * 获得所属 Bean 的完整类名
     * @return Bean 的完整类名
     */
    public String getBeanClass() {
        // 检查别名映射
        if (beanClass == null) {
            beanClass = (String) alias.get(bean);
        }
        
        // 如果不能根据别名映射，则检查完整类名
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
        
        // 如果指定的 bean 名称非法，则使用缺省名称
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
