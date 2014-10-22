package com.hzjbbis.fas.protocol.meter.conf;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * ���Լ�������
 * @author netice
 *
 */
public class MeterProtocolDataItem {
	private String code;			/*���ݱ�ʶ*/
	private String description;		/*��������*/
	private String zjcode;			/*�㽭���ݱ�ʶ*/
	private String zjcode2;
	private int length;				/*���ݳ���*/
	private int type;				/*���ݽ�������*/
	private int fraction;			/*С��λ��*/
	private String familycode;		/*�������ʶ*/
	private Hashtable children;
	private List childarray=new ArrayList();
	
	public MeterProtocolDataItem(){
		this("","","",0,0,0,"");
	}
	
	public MeterProtocolDataItem(String code,String zjcode,String description,int len,int type,int fraction,String familycode){
		this.code=code;
		this.zjcode=zjcode;
		this.length=len;
		this.type=type;
		this.fraction=fraction;
		this.description=description;
		this.familycode=familycode;
		children=new Hashtable();
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
	}

	/**
	 * @return Returns the fraction.
	 */
	public int getFraction() {
		return fraction;
	}

	/**
	 * @param fraction The fraction to set.
	 */
	public void setFraction(int fraction) {
		this.fraction = fraction;
	}

	/**
	 * @return Returns the len.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param len The len to set.
	 */
	public void setLength(int len) {
		this.length = len;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return Returns the zjcode.
	 */
	public String getZjcode() {
		return zjcode;
	}

	/**
	 * @param zjcode The zjcode to set.
	 */
	public void setZjcode(String zjcode) {
		this.zjcode = zjcode;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the familycode.
	 */
	public String getFamilycode() {
		return familycode;
	}

	/**
	 * @param familycode The familycode to set.
	 */
	public void setFamilycode(String familycode) {
		this.familycode = familycode;
	}

	/**
	 * @return Returns the children.
	 */
	public Hashtable getChildren() {
		return children;
	}

	/**
	 * @param children The children to set.
	 */
	public void setChildren(Hashtable children) {
		this.children = children;
	}

	/**
	 * @return Returns the childarray.
	 */
	public List getChildarray() {
		return childarray;
	}

	/**
	 * @param childarray The childarray to set.
	 */
	public void setChildarray(ArrayList childarray) {
		this.childarray = childarray;		
	}

	/**
	 * @return Returns the zjcode2.
	 */
	public String getZjcode2() {
		return zjcode2;
	}

	/**
	 * @param zjcode2 The zjcode2 to set.
	 */
	public void setZjcode2(String zjcode2) {
		this.zjcode2 = zjcode2;
	}
	
}
