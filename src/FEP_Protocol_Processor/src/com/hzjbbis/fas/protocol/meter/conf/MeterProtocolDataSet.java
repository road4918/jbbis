package com.hzjbbis.fas.protocol.meter.conf;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


/**
 * ���Լ���ݼ�
 * @author netice
 *
 */
public class MeterProtocolDataSet {
	private static final String PROTOCOL_NAME="ZJMETER";
	private String name;
	private Hashtable dataset;
	private List dataarray=new ArrayList();
	
	
	/**
	 * default constructor
	 *
	 */
	public MeterProtocolDataSet(){
		this(PROTOCOL_NAME,new Hashtable());
	}
	
	/**
	 * constructor
	 * @param dataset
	 */
	public MeterProtocolDataSet(String name,Hashtable dataset){		
		this.name=name;
		this.dataset=dataset;
	}
	
	/**
	 * ��ȡ���Լ����������
	 * @param code
	 * @return
	 */
	public MeterProtocolDataItem getDataItem(String code){
		return (MeterProtocolDataItem)dataset.get(code);
	}
	
	/**
	 * ��ȡ���Լ���ݼ�
	 * @return Returns the dataset.
	 */
	public Hashtable getDataset() {
		return dataset;
	}

	/**
	 * @param dataset The dataset to set.
	 */
	public void setDataset(Hashtable dataset) {
		this.dataset = dataset;
	}
		
	/**
	 * @return Returns the dataarray.
	 */
	public List getDataarray() {
		return dataarray;
	}

	/**
	 * @param dataarray The dataarray to set.
	 */
	public void setDataarray(List dataarray) {
		this.dataarray = dataarray;		
	}

	/**
	 * ��ȡ���Լ����
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void packup(){
		arrayToMap(dataarray);
	}
	
	private void arrayToMap(List datas){
		if(datas!=null){
			for(Iterator iter=datas.iterator();iter.hasNext();){
				MeterProtocolDataItem child=((MeterProtocolDataItem)iter.next());				
				addChild(child);				
			}
		}
	}
	
	/**
	 * ��������(����ĸ���Ҫ�����ӱ����أ��������)
	 * @param item
	 */
	private void addChild(MeterProtocolDataItem item){		
		dataset.put(item.getCode(),item);	//��¶��map��
		
		List cnodes=item.getChildarray();	//�����ӽڵ�
		arrayToMap(cnodes);
	}
	
}
