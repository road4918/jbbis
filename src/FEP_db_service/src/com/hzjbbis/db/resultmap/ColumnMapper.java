/**
 * ��¼����ÿ���У���Ӧ������������á�
 */
package com.hzjbbis.db.resultmap;

import java.lang.reflect.Method;

/**
 * @author bhw
 *
 */
public class ColumnMapper {
	private String property;	//���������
	private String column;		//��¼����������
	private int index;			//��¼����������
	public Method method;

	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
