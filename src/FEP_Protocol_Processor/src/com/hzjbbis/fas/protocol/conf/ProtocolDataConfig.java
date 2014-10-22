package com.hzjbbis.fas.protocol.conf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;

/**
 * Э�����ݽṹ����
 * @author ������
 */
public class ProtocolDataConfig implements IDataSets{
	private static final Log log=LogFactory.getLog(ProtocolDataConfig.class);
	
    /** ��������б� */
    private List dataItems;
    /** ��������������������ձ� */
    private Map dataset;
    /** ��׼�������뱾����������ձ� */
    private HashMap sdataset;	
    /** ���������ձ� */
    private HashMap parsers;
    
    /**
     * �����������
     * @param code ���ݱ�ʶ����
     * @return �������
     */
    public ProtocolDataItemConfig getDataItemConfig(String code) {
        if (dataset == null) {
            synchronized (this) {
                if (dataset == null) {
                    dataset = new HashMap();
                    sdataset=new HashMap();
                    parsers=new HashMap();
                    addToItemMap(dataItems);
                }
            }
        }
    	return (ProtocolDataItemConfig) dataset.get(code);
    }
    
    public synchronized void fillMap(){
    	dataset = new HashMap();
        sdataset=new HashMap();
        parsers=new HashMap();
    	addToItemMap(dataItems);
    }
    
    /**
     * �����������ӵ���������������������ձ�
     * @param items ��������б�
     */
    private void addToItemMap(List items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
    	for (int i = 0; i < items.size(); i++) {
            ProtocolDataItemConfig item = (ProtocolDataItemConfig) items.get(i);
            dataset.put(item.getCode(), item);
            if(item.getBean()!=null){
            	loadParser(item.getCode(),item.getBean());
            }
            if(item.getItems()!=null){            	
            	List sitems=item.getItems();
            	for(Iterator iter=sitems.iterator();iter.hasNext();){
            		String si=(String)iter.next();
            		String keychar=item.getKeychar();
            		if(keychar!=null){
            			int index=keychar.indexOf(":");
            			if(index>0){//���key
            				String[] keys=keychar.split(":");
            				for(int j=0;j<keys.length;j++){
            					String key=si+"|"+keys[j];
                        		sdataset.put(key, item.getCode());
            				}
            			}else{
            				String key=si+"|"+item.getKeychar();
                    		sdataset.put(key, item.getCode());
            			}
            		}            		
            	}
            }
            addToItemMap(item.getChildItems());
        }
    }
    
    private void loadParser(String key,String name){
    	if(name!=null){
    		try{
    			if(!parsers.containsKey(name)){
    				Class clazz = Class.forName(name);
    				parsers.put(key, clazz.newInstance());
    			}
    		}catch(Exception e){
    			log.error("load parser",e);
    		}
    	}
    }
    
    /**
     * @return Returns the dataItems.
     */
    public List getDataItems() {
        return dataItems;
    }
    /**
     * @param dataItems The dataItems to set.
     */
    public void setDataItems(List dataItems) {
        this.dataItems = dataItems;
    }

	public String getLocal(String key, Object para) {
		String local=null;
		if(para instanceof DataItem){
			DataItem di=(DataItem)para;
			String pt=(String)di.getProperty("point");
			if(pt!=null){
				String skey=key+"|"+pt;
				local=(String)sdataset.get(skey);
			}			
		}else if(para instanceof String){
			String skey=key+"|"+(String)para;
			local=(String)sdataset.get(skey);
		}
		return local;
	}

	public IItemParser getParser(String key) {
		IItemParser parser=null;
		if(key!=null){
			parser=(IItemParser)parsers.get(key);
		}
		return parser;
	}
	public List getMConf(String key){
		List conf = null;
		if(key!=null){
			
		}
		return conf;
	}
}
