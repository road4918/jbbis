/**
 * ֧������IBatis resultMap����ģʽ��
 */
package com.hzjbbis.db.resultmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author bhw
 *
 */
public class ResultMapper<T> {
	private static final Logger log = Logger.getLogger(ResultMapper.class);
	//spring��������
	private String resultClass;
	private List<ColumnMapper> columnMapper = new ArrayList<ColumnMapper>();
	private String columnSequence;		//���ݼ���������ӳ�䣬��','�ָ�������������ӳ�䵽��������ԡ�
	//�ڲ�����
	private Class<T> resultClassObject;
	
	public T mapOneRow(ResultSet rs){
		try{
			T dest = resultClassObject.newInstance();
			mapRow2Object(rs,dest);
			return dest;
		}catch(Exception exp){
			log.error("�Ѽ�¼����ӳ�䵽�����쳣��"+exp.getLocalizedMessage(),exp);
		}
		return null;
	}
	
	/**
	 * ����ֱ�Ӵ�JDBC���صļ�¼��������sping��װ��ParameterizedRowMapper���ᵼ�½����һ�С�
	 * ��Ϊ�����λ�ڵ�һ��֮ǰ����˵�һ�ε���next���ѹ�����ڵ�һ���ϡ�
	 * @param rs
	 * @return
	 */
	public List<T> mapAllRows(ResultSet rs){
		List<T> objList = new ArrayList<T>();
		try{
			while( rs.next() ){
				T result = mapOneRow(rs);
				if( null != result )
					objList.add(result);
			}
		}
		catch(SQLException e){
			log.error(e.getLocalizedMessage(),e);
		}
		return objList;
	}
	
	private void mapRow2Object(ResultSet rs,T dest)throws SQLException,
		IllegalArgumentException,IllegalAccessException{
		try{
			for( ColumnMapper item : columnMapper ){
				int index = item.getIndex();
				if( index > 0 ){
					Class<?> clz = item.method.getParameterTypes()[0];
					if( clz == String.class ){
						item.method.invoke(dest, rs.getString(index));
					}
					else if( clz == Integer.TYPE || clz == Integer.class ){
						item.method.invoke(dest, rs.getInt(index));
					}
					else if( clz == Long.TYPE || clz == Long.class ){
						item.method.invoke(dest, rs.getLong(index));
					}
					else if( clz == java.util.Date.class ){
						item.method.invoke(dest, rs.getDate(index));
					}
					else if( clz == Short.TYPE || clz == Short.class ){
						item.method.invoke(dest, rs.getShort(index));
					}
					else if( clz == Byte.TYPE || clz == Byte.class ){
						item.method.invoke(dest, rs.getByte(index));
					}
					else if( clz == Character.TYPE || clz == Character.class ){
						item.method.invoke(dest, (char)rs.getByte(index));
					}
					else if( clz == Boolean.TYPE || clz == Boolean.class ){
						item.method.invoke(dest, rs.getBoolean(index));
					}
					else if( clz == Double.TYPE || clz == Double.class ){
						item.method.invoke(dest, rs.getDouble(index));
					}
					else{
						try{
							item.method.invoke(dest, rs.getString(index));
						}catch(Exception exp){
							log.warn("�������Բ�������["+item.getProperty()+"]��ת�����ʹ���:"+exp.getLocalizedMessage(),exp);
						}
					}
				}
				else{
					Class<?> clz = item.method.getParameterTypes()[0];
					String column = item.getColumn();
					if( clz == String.class ){
						item.method.invoke(dest, rs.getString(column));
					}
					else if( clz == Integer.TYPE || clz == Integer.class ){
						item.method.invoke(dest, rs.getInt(column));
					}
					else if( clz == Long.TYPE || clz == Long.class ){
						item.method.invoke(dest, rs.getLong(column));
					}
					else if( clz == java.util.Date.class ){
						item.method.invoke(dest, rs.getDate(column));
					}
					else if( clz == Short.TYPE || clz == Short.class ){
						item.method.invoke(dest, rs.getShort(column));
					}
					else if( clz == Byte.TYPE || clz == Byte.class ){
						item.method.invoke(dest, rs.getByte(column));
					}
					else if( clz == Character.TYPE || clz == Character.class ){
						item.method.invoke(dest, (char)rs.getByte(column));
					}
					else if( clz == Boolean.TYPE || clz == Boolean.class ){
						item.method.invoke(dest, rs.getBoolean(column));
					}
					else if( clz == Double.TYPE || clz == Double.class ){
						item.method.invoke(dest, rs.getDouble(column));
					}
					else{
						try{
							item.method.invoke(dest, rs.getString(column));
						}catch(Exception exp){
							log.warn("�������Բ�������["+item.getProperty()+"]��ת�����ʹ���:"+exp.getLocalizedMessage(),exp);
						}
					}
				}
			}
		}
		catch(InvocationTargetException exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	public String getResultClass() {
		return resultClass;
	}
	
	public void setResultClass(String resultClass) {
		this.resultClass = resultClass;
		if( null != columnMapper ){
			populateMethod();
		}
	}
	
	public List<ColumnMapper> getColumnMapper() {
		return columnMapper;
	}
	
	public void setColumnMapper(List<ColumnMapper> columnMapper) {
		this.columnMapper = columnMapper;
		if( null != resultClass && resultClass.length()>1 ){
			populateMethod();
		}
	}

	/**
	 * ��¼����Rowӳ�䵽��������ԣ���Ҫ�������Ե�set����������������ļ����غ���ҪԤ�ȼ����������÷�����
	 */
	@SuppressWarnings("unchecked")
	private void populateMethod(){
		try{
			resultClassObject = (Class<T>)Class.forName(resultClass);
			Method[] allMethods = resultClassObject.getMethods();
			Map<String,Method> methods = new HashMap<String,Method>();
			for(Method m: allMethods ){
				if( m.getName().startsWith("set") )
					methods.put(m.getName(), m);
			}
			for( ColumnMapper column : columnMapper ){
				String name = column.getProperty();
				name = "set"+name.substring(0, 1).toUpperCase()+name.substring(1);
				Method method = methods.get(name);
				if( null == method ){
					String errInfo = "JdbcBaseDao�Ķ����������¼����Ӧ��ϵ������������Բ�����:"+ column.getProperty();
					log.error(errInfo);
					throw new RuntimeException(errInfo);
				}
				column.method = method;
			}
		}
		catch(ClassNotFoundException exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
		catch(LinkageError exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}

	public void setColumnSequence(String columnSequence) {
		this.columnSequence = columnSequence;
		this.columnSequence.trim();
		String[] cols = this.columnSequence.split(",");
		int index = 1;
		for( String cstr: cols ){
			cstr = StringUtils.strip(cstr);
			if( cstr.length() == 0 )
				continue;
			ColumnMapper cm = new ColumnMapper();
			cm.setProperty(cstr);
			cm.setIndex(index++);
			columnMapper.add(cm);
		}
		//�ǵ�ͨ��reflection�ҵ�Method
		if( null != resultClass && resultClass.length()>1 ){
			populateMethod();
		}
	}
}
