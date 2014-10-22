package com.hzjbbis.fas.protocol.meter.conf;

import java.util.Enumeration;
import java.util.Hashtable;

import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.MapHandler;
import org.exolab.castor.mapping.ValidityException;

/**
 * Castor �� Collection���͵��Զ��崦��
 * @author netice
 *
 */
public class CollectionFieldHandler implements MapHandler,FieldHandler{
	private Hashtable map;
	
	public Object create() {		
		return new Hashtable();
	}

	public Object put(Object map, Object key, Object object) throws ClassCastException {
		
		//valid check
		if((map!=null)&&(key!=null)&&(object!=null)){
			if(object instanceof MeterProtocolDataItem){
				System.out.println("type is ok!");
			}
		}		
		return map;
	}

	public Enumeration elements(Object map) throws ClassCastException {
		if (map == null) map = new Hashtable();
        return ((Hashtable)map).elements();
	}

	public Enumeration keys(Object map) throws ClassCastException {
		if (map == null) map = new Hashtable();
        return ((Hashtable)map).keys();
	}

	public int size(Object map) throws ClassCastException {
		if (map == null) return 0;
        return ((Hashtable)map).size();
	}

	public void clear(Object map) throws ClassCastException {
		if (map == null) return;
        ((Hashtable)map).clear();
	}

	public Object get(Object map, Object key) throws ClassCastException {
		if (map == null) return null;
        return ((Hashtable)map).get(key);
	}

	public Object getValue(Object object) throws IllegalStateException {		
		return map;
	}

	public void setValue(Object object, Object value) throws IllegalStateException, IllegalArgumentException {
		if((object instanceof MeterProtocolDataItem)){
			if(map==null){
				map=new Hashtable();
			}
			MeterProtocolDataItem child=((MeterProtocolDataItem)object);
			MeterProtocolDataItem parent=(MeterProtocolDataItem)map.get(child.getFamilycode());
			if(parent!=null){	//�Ǽ������				
				addChild(parent,child);				
			}
			map.put(child.getCode(),child);
		}
	}

	public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
		//
	}

	public void checkValidity(Object object) throws ValidityException, IllegalStateException {
		//
	}

	public Object newInstance(Object parent) throws IllegalStateException {		
		map=new Hashtable();
		return map;
	}
	
	/**
	 * ��������(����ĸ���Ҫ�����ӱ����أ��������)
	 * @param item
	 */
	public void addChild(MeterProtocolDataItem parent,MeterProtocolDataItem item){
		Hashtable children=parent.getChildren();
		if(item.getFamilycode().equals(parent.getCode())){			
			if(children==null){
				children=new Hashtable();
			}
			children.put(item.getCode(),item);
		}
	}
}
