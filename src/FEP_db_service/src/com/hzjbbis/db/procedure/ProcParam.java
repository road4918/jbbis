/**
 * ����洢���̵��������������
 */
package com.hzjbbis.db.procedure;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author bhw
 *
 */
public class ProcParam {
	public static final int MODE_IN = 0;
	public static final int MODE_OUT = 1;
	public static final int MODE_INOUT = 2;
	private static final Map<String,Integer> typeMap = new HashMap<String,Integer>();
	static{
		typeMap.put("CHAR", Types.CHAR);
		typeMap.put("NUMERIC", Types.NUMERIC);
		typeMap.put("DECIMAL", Types.DECIMAL);
		typeMap.put("INTEGER", Types.INTEGER);
		typeMap.put("SMALLINT", Types.SMALLINT);
		typeMap.put("FLOAT", Types.FLOAT);		//6
		typeMap.put("REAL", Types.REAL);
		typeMap.put("DOUBLE", Types.DOUBLE);	//8
		typeMap.put("VARCHAR", Types.VARCHAR);	//12
		typeMap.put("TINYINT", Types.TINYINT);	//-6
		typeMap.put("BIGINT", Types.BIGINT);	//-5
		typeMap.put("DATE", Types.DATE);	//91
		typeMap.put("TIME", Types.TIME);	//92
		typeMap.put("TIMESTAMP", Types.TIMESTAMP);	//93
		typeMap.put("BOOLEAN", Types.BOOLEAN);	//16
	}
	private String pname;
	private int jdbcType = Types.VARCHAR;
	private int index;
	private int mode = MODE_IN;
	
	public ProcParam(String paramName,String jtype,String pmode,int index){
		pname = paramName;
		Integer t = typeMap.get(jtype.toUpperCase());
		if( null != t )
			jdbcType = t;
		pmode = pmode.toUpperCase();
		if( pmode.equals("IN") )
			mode = MODE_IN;
		else if( pmode.equals("OUT") )
			mode = MODE_OUT;
		else if( pmode.equals("INOUT"))
			mode = MODE_INOUT;
		this.index = index;
	}
	
	public String getParamName(){
		return pname;
	}
	
	public int getJdbcType(){
		return jdbcType;
	}
	
	public int getParamIndex(){
		return index;
	}
	
	public void setParamIndex(int i){
		index = i;
	}
	
	public int getParamMode(){
		return mode;
	}
	
	public void setInputValueByBean(CallableStatement stmt,Object bean )throws SQLException{
		try{
			Object pval = PropertyUtils.getProperty(bean, pname);
			setInputValue(stmt,pval);
		}catch(Exception e){
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	public void setInputValue(CallableStatement stmt,Object val )throws SQLException{
		switch( jdbcType ){
		case Types.VARCHAR:
		case Types.CHAR:
			stmt.setString(index, val.toString());
			break;
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.SMALLINT:
			if( val instanceof Integer )
				stmt.setInt(index,(Integer)val);
			else if( val instanceof Long )
				stmt.setLong(index, (Long)val);
			else if( val instanceof Short )
				stmt.setShort(index, (Short)val);
			else if( val instanceof Byte )
				stmt.setByte(index, (Byte)val);
			else if( val instanceof String )
				stmt.setInt(index, Long.valueOf((String)val).intValue());
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
			if( val instanceof Float )
				stmt.setFloat(index, (Float)val);
			else if( val instanceof Double )
				stmt.setDouble(index, (Double)val);
			else if( val instanceof Integer ){
				int v = (Integer)val;
				stmt.setDouble(index, v);
			}
			else if( val instanceof Long ){
				long v = (Long)val;
				stmt.setDouble(index, v);
			}
			else if( val instanceof String ){
				double d = Double.parseDouble((String)val);
				stmt.setDouble(index, d);
			}
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.TINYINT:
			if( val instanceof Byte )
				stmt.setByte(index, (Byte)val);
			else if( val instanceof Integer )
				stmt.setByte(index, ((Integer)val).byteValue());
			else if( val instanceof Short )
				stmt.setByte(index, ((Short)val).byteValue());
			else if( val instanceof String ){
				Integer ib = Integer.parseInt((String)val);
				stmt.setByte(index, ib.byteValue());
			}
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.BIGINT:
			if( val instanceof Integer )
				stmt.setInt(index, (Integer)val);
			else if( val instanceof Long )
				stmt.setLong(index, (Long)val);
			else if( val instanceof String ){
				Long l = Long.parseLong((String)val);
				stmt.setLong(index, l);
			}
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.DATE:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Date )
					stmt.setDate(index, (java.sql.Date)val);
				else
					stmt.setDate( index, new java.sql.Date(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setDate(index, new java.sql.Date( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setDate(index, new java.sql.Date( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Date sqlDate = new java.sql.Date( udate.getTime() );
					stmt.setDate(index, sqlDate);
				}catch(ParseException e){
					String err = "�ַ���["+(String)val+"]ת��ΪDate���󡣲����ֶ�:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			break;
		case Types.TIME:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Time )
					stmt.setTime(index, (java.sql.Time)val);
				else
					stmt.setTime( index, new java.sql.Time(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setTime(index, new java.sql.Time( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setTime(index, new java.sql.Time( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Time sqlDate = new java.sql.Time( udate.getTime() );
					stmt.setTime(index, sqlDate);
				}catch(ParseException e){
					String err = "�ַ���["+(String)val+"]ת��ΪDate���󡣲����ֶ�:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			break;
		case Types.TIMESTAMP:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Timestamp )
					stmt.setTimestamp(index, (java.sql.Timestamp)val);
				else
					stmt.setTimestamp( index, new java.sql.Timestamp(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setTimestamp(index, new java.sql.Timestamp( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setTimestamp(index, new java.sql.Timestamp( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Timestamp sqlDate = new java.sql.Timestamp( udate.getTime() );
					stmt.setTimestamp(index, sqlDate);
				}catch(ParseException e){
					String err = "�ַ���["+(String)val+"]ת��ΪDate���󡣲����ֶ�:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.BOOLEAN:
			if( val instanceof Boolean )
				stmt.setBoolean(index, (Boolean)val);
			else if( val instanceof Integer ){
				int i = (Integer)val;
				stmt.setBoolean(index, i!=0);
			}
			else if( val instanceof String ){
				String s = ((String)val).toLowerCase();
				stmt.setBoolean(index, s == "true");
			}
			else
				throw new RuntimeException("����Ĳ����������ò����ݡ���������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		default:
			throw new RuntimeException("����Ĳ�����û�ж�Ӧ��jdbcType����������[name="+pname+",index="+index+"],value type="+val.getClass().getName());
		}
	}
}
