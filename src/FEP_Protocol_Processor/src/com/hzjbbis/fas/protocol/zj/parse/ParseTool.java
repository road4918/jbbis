package com.hzjbbis.fas.protocol.zj.parse;

import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;

/**
 * 
 * @author yangdh
 * �����Ĺ��ߺ���
 */
public class ParseTool {
	public static final String[] hex=new String[]{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	public static final double FRACTION_TIMES_10=10.0;
	public static final double FRACTION_TIMES_100=100.0;
	public static final double FRACTION_TIMES_1000=1000.0;
	public static final double FRACTION_TIMES_10000=10000.0;
	public static final double fraction[]=new double[]{1.0,FRACTION_TIMES_10,FRACTION_TIMES_100,FRACTION_TIMES_1000,FRACTION_TIMES_10000};
	public static final int[] days=new int[]{0,31,29,31,30,31,30,31,31,30,31,30,31};
	public static final String METER_PROTOCOL_BB="10";
	public static final String METER_PROTOCOL_ZJ="20";
	public static final String METER_PROTOCOL_SM="40";
	
	private static final Log log=LogFactory.getLog(ParseTool.class);
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * �ֽ�����ת��Ϊʮ�������ִ�
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static String BytesToHex(byte[] data,int start,int len){
		StringBuffer sb=new StringBuffer();
		for(int i=start;i<start+len;i++){			
			sb.append(hex[(data[i] & 0xf0)>>4]);
			sb.append(hex[(data[i] & 0xf)]);
			sb.append(" ");
		}		
		return sb.substring(0,sb.length()-1);
	}
	
	public static String ByteToHex(byte data){
		String bt="";
		bt=hex[(data & 0xf0)>>4]+hex[(data & 0xf)];		
		return bt;
	}
	
	/**
	 * �ֽ�����ת��Ϊʮ�������ִ����ֽ�֮��û�зָ����
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static String BytesToHexL(byte[] data,int start,int len){
		StringBuffer sb=new StringBuffer();
		for(int i=start;i<start+len;i++){
			sb.append(hex[(data[i] & 0xf0)>>4]);
			sb.append(hex[(data[i] & 0xf)]);
		}
		return sb.toString();
	}
	
	/**
	 * �ֽ�����ת��Ϊʮ�������ִ����ֽ�֮��û�зָ����--����
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static String BytesToHexC(byte[] data,int start,int len){
		StringBuffer sb=new StringBuffer();
		int loc=start+len-1;
		for(int i=0;i<len;i++){
			sb.append(hex[(data[loc] & 0xf0)>>4]);
			sb.append(hex[(data[loc] & 0xf)]);
			loc--;
		}
		return sb.toString();
	}
	
	/**
	 * BCD������ת��Ϊ�ִ����ֽ�֮��û�зָ����--����
	 * @param data
	 * @param start
	 * @param len
	 * @param invalid �Ƿ��ַ���־
	 * @return
	 */
	public static String BytesToHexC(byte[] data,int start,int len,byte invalid){
		StringBuffer sb=new StringBuffer();
		int loc=start+len-1;
		for(int i=0;i<len;i++){
			if(data[loc]!=invalid){
				sb.append(hex[(data[loc] & 0xf0)>>4]);
				sb.append(hex[(data[loc] & 0xf)]);
			}
			loc--;
		}
		return sb.toString();
	}
	
	/**
	 * 1�ֽ�BCDת��Ϊʮ����
	 * @param bcd
	 * @return ��Ч���ݷ��ظ���
	 */
	public static int BCDToDecimal(byte bcd){
		int high=(bcd & 0xf0)>>>4;
		int low=(bcd & 0xf);
		if(high>9 || low>9){
			return -1;
		}
		return high*10+low;
	}
	
	/**
	 * ���ֽ�BCDת��Ϊint
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static int nBcdToDecimal(byte[] data,int start,int len){
		int rt=0;
		for(int i=0;i<len;i++){
			rt*=100;			
			//rt+=BCDToDecimal(data[start+len-i-1]);			
			int bval=BCDToDecimal(data[start+len-i-1]);
			if(bval<0){
				rt=-1;
				break;
			}
			rt+=bval;
		}
		return rt;
	}
	
	/**
	 * ���ֽ�BCDת��Ϊint  1234-->0x12 0x34
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static int nBcdToDecimalC(byte[] data,int start,int len){
		int rt=0;
		for(int i=start;i<start+len;i++){
			rt*=100;				
			int bval=BCDToDecimal(data[i]);
			if(bval<0){
				rt=-1;
				break;
			}
			rt+=bval;
		}
		return rt;
	}
	
	/**
	 * ���ֽ�BCDת��Ϊint ��λ������λ (���������)
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static int nBcdToDecimalS(byte[] data,int start,int len){
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		int rt=0;
		
		int loc1=start+len-1;
		for(int i=0;i<len;i++){
			rt*=100;
			/*if(i>0){
				rt+=BCDToDecimal(data[loc1-i]);
			}else{
				rt+=BCDToDecimal((byte)(data[loc1-i] & 0xf));
			}*/
			int bval;
			if(i>0){
				bval=BCDToDecimal(data[loc1-i]);
			}else{
				bval=BCDToDecimal((byte)(data[loc1-i] & 0xf));
			}
			if(bval<0){
				rt=-1;
				break;
			}
			rt+=bval;
		}
		//System.out.println(nf.format(rt));
		return rt;
	}
	
	/**
	 * n�ֽ�byteת��Ϊint
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static int nByteToInt(byte[] data,int start,int len){
		int rt=0;
		for(int i=0;i<len;i++){
			rt<<=8;
			rt+=(data[start+len-i-1] & 0xff);
		}
		return rt;
	}
	
	/**
	 * n�ֽ�byteת��Ϊint ���λ�Ƿ���λ
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 */
	public static int nByteToIntS(byte[] data,int start,int len){
		int rt=0;
		int loc=start+len-1;		
		for(int i=0;i<len;i++){
			rt<<=8;
			if(i>0){
				rt+=(data[loc-i] & 0xff);
			}else{
				rt+=(data[loc-i] & 0x7f);
			}
		}
		return rt;
	}
	
	
	/**
	 * ʮ�������Ӵ�ת��Ϊʮ��������
	 * @param hex
	 * @return
	 */
	public static int HexToDecimal(String hex){
    	int rt=0;
    	for(int i=0;i<hex.length();i++){
    		rt<<=4;
    		rt+=CharToDecimal(hex.substring(i,i+1));
    	}
    	return rt;
    }
    
	/**
	 * ����ͨѶ����
	 * @param data  ֡����
	 * @param index ��ʼ����
	 * @param len   ���ν�������
	 * @param invalid �Ƿ����ݱ�־
	 * @return ͨѶ�����ַ���
	 */
	public static String toPhoneCode(byte[] data,int index,int len,int invalid){		
		StringBuffer sb=new StringBuffer();
		int valid=index+len-1;		
		for(int i=(index+len-1);i>=index;i--){	//ȥ��ǰ׺
			if((data[i] & 0xff)!=invalid){
				valid=i;
				break;
			}
		}
		if(valid>=index){
			if(!((data[valid] & 0xf0)==0x0)){//��λ0ȥ��
				sb.append(hex[(data[valid] & 0xf0)>>4]);				
			}
			sb.append(hex[(data[valid] & 0xf)]);
			valid--;
		}
		for(int j=valid;j>=index;j--){	//��������
			sb.append(hex[(data[j] & 0xf0)>>4]);
			sb.append(hex[(data[j] & 0xf)]);
		}
		return sb.toString();
	}
	
	public static int CharToDecimalB(String c){
		int rt=0;
		int head=0;
		int tail=15;
		rt=(head+tail)>>1;
		while(!hex[rt].equals(c)){
			if(head==tail){
				//û�ҵ�
				break;
			}			
			int var=c.compareTo(hex[rt]);
			if(var==0){
				break;
			}
			if(var>0){				
				if(rt==head){
					rt=tail;
					break;
				}
				head=rt;
				rt=(head+tail)>>1;
			}else{
				tail=rt;
				rt=(head+tail)>>1;
			}			
		}
		return rt;
	}
	
    public static int CharToDecimal(String hex){
    	int rt=0;
    	if(hex.equals("0")){
    		return 0;
    	}
    	if(hex.equals("1")){
    		return 1;
    	}
    	if(hex.equals("2")){
    		return 2;
    	}
    	if(hex.equals("3")){
    		return 3;
    	}
    	if(hex.equals("4")){
    		return 4;
    	}
    	if(hex.equals("5")){
    		return 5;
    	}
    	if(hex.equals("6")){
    		return 6;
    	}
    	if(hex.equals("7")){
    		return 7;
    	}
    	if(hex.equals("8")){
    		return 8;
    	}
    	if(hex.equals("9")){
    		return 9;
    	}
    	if(hex.equals("A") || hex.equals("a")){
    		return 10;
    	}
    	if(hex.equals("B") || hex.equals("b")){
    		return 11;
    	}
    	if(hex.equals("C") || hex.equals("c")){
    		return 12;
    	}
    	if(hex.equals("D") || hex.equals("d")){
    		return 13;
    	}
    	if(hex.equals("E") || hex.equals("e")){
    		return 14;
    	}
    	if(hex.equals("F") || hex.equals("f")){
    		return 15;
    	}
    	return rt;
    }
    
    /**
     * ���ݱ�ʶר��
     * @param data
     * @return
     */
    public static String IntToHex(int data){    	
    	StringBuffer sb=new StringBuffer();
    	sb.append(hex[(data & 0xf000)>>>12]);
    	sb.append(hex[(data & 0xf00)>>>8]);
    	sb.append(hex[(data & 0xf0)>>>4]);
    	sb.append(hex[(data & 0xf)]);
    	return sb.toString();
    }
    
    /**
     * ���ݱ�ʶר��
     * @param data
     * @return
     */
    public static String IntToHex4(int data){    	
    	StringBuffer sb=new StringBuffer();
    	sb.append(hex[(data & 0xf0000000)>>>28]);
    	sb.append(hex[(data & 0xf000000)>>>24]);
    	sb.append(hex[(data & 0xf00000)>>>20]);
    	sb.append(hex[(data & 0xf0000)>>>16]);
    	sb.append(hex[(data & 0xf000)>>>12]);
    	sb.append(hex[(data & 0xf00)>>>8]);
    	sb.append(hex[(data & 0xf0)>>>4]);
    	sb.append(hex[(data & 0xf)]);
    	return sb.toString();
    }
    
    /**
     * �ֽڵ�bit�ַ��� bits�ַ���ת��Ϊ�ֽ����� |b7|b6|....|b0|----->'b7b6b5b4b3b2b1b0'
     * @param data
     * @return
     */
    public static String ByteBit(byte data){
    	StringBuffer sb=new StringBuffer();
    	int bd=(data & 0xff);
    	for(int i=0;i<8;i++){
    		if((bd & 0x80)>0){
    			sb.append("1");
    		}else{
    			sb.append("0");
    		}
    		bd<<=1;
    	}
    	return sb.toString();
    }
    
    /**
     * �ֽڵ�bit�ַ��� |b7|b6|....|b0|----->'b0b1b2b3b4b5b6b7'
     * @param data
     * @return
     */
    public static String ByteBitC(byte data){
    	StringBuffer sb=new StringBuffer();
    	int bd=(data & 0xff);
    	for(int i=0;i<8;i++){
    		if((bd & 0x1)>0){
    			sb.append("1");
    		}else{
    			sb.append("0");
    		}
    		bd>>>=1;
    	}
    	return sb.toString();
    }
    
    /**
     * bytes to bits |b7|b6|....|b0|----->'b7b6b5b4b3b2b1b0'
     * @param data
     * @return
     */
    public static String BytesBit(byte[] data){
    	StringBuffer sb=new StringBuffer();
    	int len=data.length;
    	for(int i=0;i<len;i++){
    		sb.append(ByteBit(data[len-i-1]));
    	}
    	return sb.toString();
    }
    
    /**
     * bytes to bits |b7|b6|....|b0|----->'b7b6b5b4b3b2b1b0'
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static String BytesBit(byte[] data,int start,int len){
    	StringBuffer sb=new StringBuffer();    	
    	int loc=start+len-1;
    	for(int i=0;i<len;i++){
    		sb.append(ByteBit(data[loc]));
    		loc--;
    	}
    	return sb.toString();
    }
    
    /**
     * bytes to bits |b7|b6|....|b0|----->'b0b1b2b3b4b5b6b7'
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static String BytesBitC(byte[] data,int start,int len){
    	StringBuffer sb=new StringBuffer();    	
    	int loc=start;
    	for(int i=0;i<len;i++){
    		sb.append(ByteBitC(data[loc]));
    		loc++;
    	}
    	return sb.toString();
    }
    
    /**
     * bits�ַ���ת��Ϊ�ֽ����� 'b7b6b5b4b3b2b1b0'----->|b7|b6|....|b0|
     * @param frame
     * @param bits
     * @param pos
     * @return
     */
    public static int bitToBytes(byte[] frame,String bits,int pos){
    	int rt=-1;
    	try{    		
			int vlen=bits.length();
			boolean valid=true;
			for(int i=0;i<vlen;i++){
				if((bits.substring(i,i+1).equals("0")) || (bits.substring(i,i+1).equals("1"))){
					//
				}else{
					valid=false;
					break;
				}
			}    			
			if(valid && (vlen & 0x7)==0){//��8����������
				int blen=0;
				int len=(vlen>>>3);
				int iloc=pos+len-1;    				
				while(blen<vlen){
					frame[iloc]=ParseTool.bitToByte(bits.substring(blen,blen+8));
					blen+=8;
					iloc--;
				}
				rt=len;
			}    		
    		return rt;
    	}catch(Exception e){
    		log.error("bits to bytes",e);
    	}
    	return rt;
    }
    
    /**
     * bits�ַ���ת��Ϊ�ֽ����� 'b0b1b2b3b4b5b6b7'----->|b7|b6|....|b0|
     * @param frame
     * @param bits
     * @param pos
     * @return
     */
    public static int bitToBytesC(byte[] frame,String bits,int pos){
    	int rt=-1;
    	try{    		
			int vlen=bits.length();
			boolean valid=true;
			for(int i=0;i<vlen;i++){
				if((bits.substring(i,i+1).equals("0")) || (bits.substring(i,i+1).equals("1"))){
					//
				}else{
					valid=false;
					break;
				}
			}
			if(valid && (vlen & 0x7)==0){//��8����������
				int blen=0;
				int len=(vlen>>>3);
				int iloc=pos;    				
				while(blen<vlen){
					frame[iloc]=ParseTool.bitToByteC(bits.substring(blen,blen+8));
					blen+=8;
					iloc++;
				}
				rt=len;
			}
    		return rt;
    	}catch(Exception e){
    		log.error("bits to bytes",e);
    	}
    	return rt;
    }
    
    /**
     * bit8 to byte 'b7b6....b0'----->|b7|b6|......|b0|
     * @param value
     * @return
     */
    public static byte bitToByte(String value){
    	byte rt=0;
    	byte[] aa=value.getBytes();
    	for(int i=0;i<aa.length;i++){
    		rt<<=1;
    		rt+=AsciiToInt(aa[i]);
    	}
    	return rt;
    }
    
    /**
     * bit8 to byte 'b0b1....b7'----->|b7|b6|......|b0|
     * @param value
     * @return
     */
    public static byte bitToByteC(String value){
    	byte rt=0;
    	byte[] aa=value.getBytes();
    	for(int i=aa.length-1;i>=0;i--){
    		rt<<=1;
    		rt+=AsciiToInt(aa[i]);
    	}
    	return rt;
    }
    
    /**
     * intת��Ϊ1�ֽ�BCD
     * @param data
     * @return
     */
    public static byte IntToBcd(int data){
    	byte rt=0;
    	int i=data;
    	i%=100;
    	rt=(byte)((i % 10)+((i/10)<<4));    	
    	return rt;
    }
    
    /**
     * intת��Ϊbcd������----��λ��ǰ ��λ�ں�
     * @param frame
     * @param value
     * @param loc
     * @param len
     */
    public static void IntToBcd(byte[] frame,int value,int loc,int len){
    	int val=value;
    	int valxx=val%100;
    	for(int i=0;i<len;i++){
    		frame[loc+i]=(byte)((valxx % 10)+((valxx/10)<<4));
    		val/=100;
    		valxx=val%100;
    	}
    }
            
    /**
     * intת��Ϊbcd������-----��λ��ǰ ��λ�ں�
     * @param frame
     * @param value
     * @param loc
     * @param len
     */
    public static void IntToBcdC(byte[] frame,int value,int loc,int len){
    	int val=value;
    	int valxx=val%100;
    	int start=loc+len-1;
    	for(int i=0;i<len;i++){
    		frame[start]=(byte)((valxx % 10)+((valxx/10)<<4));
    		val/=100;
    		valxx=val%100;
    		start--;
    	}
    }
    
    /**
     * �ַ���ת��Ϊһ�ֽ�BCD
     * @param data
     * @return
     */
    public static byte StringToBcd(String data){
    	byte rt=0;
    	try{
    		int i=Integer.parseInt(data);
    		rt=IntToBcd(i);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * bitλ�е�һ������λ��
     * @param data
     * @return
     */
    public static int ByteToFlag(byte data){
    	int rt=0;
    	int val=data & 0xff;
    	int flag=0x1;
    	if(data>0){
    		rt=1;
    		while((flag & val)<=0){
    			rt++;
    			flag<<=1;
    		}
    	}
    	return rt;
    }
    
    /**
     * �ֽ�ת��Ϊ�ٷ�������Լ�ж������λΪ����λ��
     * @param data
     * @return
     */
    public static double ByteToPercent(byte data){
    	double rt=(double)(data & 0x7f);
    	if((data & 0x80)>0){
    		rt*=-1.0;
    	}
    	return rt;
    }
    
    /**
     * �ַ�����ת��ΪBCD������ ��λ��ǰ����λ�ں�
     * @param data
     * @return
     */
    public static void StringToBcds(byte[] frame,int loc,String data){    	
    	String row=data;
    	if(row.length()>0){
    		if((row.length()%2)>0){
    			row="0"+row;
    		}
    		int len=row.length()/2;
    		
    		for(int i=0;i<len;i++){
    			frame[loc+len-i-1]=StringToBcd(row.substring(i<<1,(i+1)<<1));
    		}
    		row=null;
    	}    	
    }
    
    /**
     * �ַ�����ת��ΪBCD������ ��λ��ǰ, ��λ�ں�
     * @param data
     * @return
     */
    public static void StringToBcds1(byte[] frame,int loc,String data){    	
    	String row=data;
    	if(row.length()>0){
    		if((row.length()%2)>0){
    			row="0"+row;
    		}
    		int len=(row.length()/2);
    		
    		for(int i=0;i<len;i++){
    			frame[loc+len-i-1]=StringToBcd(row.substring(i<<1,(i+1)<<1));
    		}
    		row=null;
    	}    	
    }
    
    public static byte[] StringToBcdDec(String data){
    	if( null == data || 0 == data.length() )
    		return new byte[0];
    	if( 0 != data.length()%2 )
    		data = "0"+data;
    	byte[] ret = new byte[data.length()/2];
    	int j = ret.length-1;
    	byte b1,b2;
    	for(int i=0;i<data.length()-1;i+=2){
    		b1 = (byte)(data.charAt(i)-'0');
    		b2 = (byte)(data.charAt(i+1)-'0');
    		ret[j--] = (byte)((b1<<4) + b2);
    	}
    	return ret;
    }
    
    /**
     * �ַ�����ת��ΪBCD������ ��λ��ǰ����λ�ں�---ָ�����ȣ��ִ��������ռλ�����
     * @param frame
     * @param loc
     * @param data
     * @param len
     * @param invalid
     */
    public static void StringToBcds(byte[] frame,int loc,String data,int len,byte invalid){    	
		int slen=(data.length()>>>1)+(data.length() & 0x1);
		int iloc=slen+loc-1;
		int head=0;
		if((data.length() & 0x1)>0){
    		frame[iloc]=StringToBcd(data.substring(0,1));
    		head=1;
    	}else{
    		frame[iloc]=StringToBcd(data.substring(0,2));
    		head=2;
    	}
		iloc--;
		for(int i=1;i<slen;i++){
			frame[iloc]=StringToBcd(data.substring(head,head+2));
			head+=2;
			iloc--;
		}
		iloc=slen+loc;
    	for(int i=slen;i<len;i++){
    		frame[iloc]=invalid;
    		iloc++;
    	}
    }
    
    /**
     * HEX�ַ���ת��Ϊ�ֽ����� ��λ��ǰ����λ�ں�
     * @param data
     * @return
     */
    public static void HexsToBytesC(byte[] frame,int loc,String data){    	
    	try{	    	
    		int len=(data.length()>>>1)+(data.length() & 0x1);	    	
	    	int head=0;
	    	if((data.length() & 0x1)>0){
	    		frame[loc]=HexToByte(data.substring(0,1));
	    		head=1;
	    	}else{
	    		frame[loc]=HexToByte(data.substring(0,2));
	    		head=2;
	    	}
	    	for(int i=1;i<len;i++){	    		
	    		frame[i+loc]=HexToByte(data.substring(head,head+2));
	    		head+=2;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public static byte HexToByte(String data){
    	int rt=0;
    	if(data.length()<=2){    		
    		for(int i=0;i<data.length();i++){
    			rt<<=4;
    			rt+=CharToDecimal(data.substring(i,i+1));
    		}
    	}
    	return (byte)rt;
    }
    
    public static int AsciiToInt(byte val){
    	int rt=val & 0xff;
    	if(val<58){
    		rt-=48;
    	}else if(rt<71){
    		rt-=55;
    	}else{
    		rt-=87;
    	}
    	return rt;
    }
    
    /**
     * hex to byte[] ��λ��ǰ����λ�ں�
     * @param hex
     * @return
     */
    public static void HexsToBytesCB(byte[] frame,int loc,String hex){    	
    	try{
    		int len=(hex.length()>>>1)+(hex.length() & 0x1);    		
	    	
	    	byte[] bt=hex.getBytes();
	    	int head=0;
	    	if((hex.length() & 0x1)>0){
	    		frame[loc]=(byte)AsciiToInt(bt[0]);
	    		head=1;
	    	}else{
	    		frame[loc]=(byte)((AsciiToInt(bt[0])<<4)+AsciiToInt(bt[1]));
	    		head=2;
	    	}	    	
	    	for(int i=1;i<len;i++){	    		
	    		frame[loc+i]=(byte)((AsciiToInt(bt[head])<<4)+AsciiToInt(bt[head+1]));
	    		head+=2;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    /**
     * hex to byte[] 
     * @param hex
     * @return
     */
    public static void HexsToBytes(byte[] frame,int loc,String hex){    	
    	try{
    		int len=(hex.length()>>>1)+(hex.length() & 0x1);
	    	byte[] bt=hex.getBytes();
	    	int head=0;
	    	if((hex.length() & 0x1)>0){
	    		frame[loc+len-1]=(byte)AsciiToInt(bt[0]);
	    		head=1;
	    	}else{
	    		frame[loc+len-1]=(byte)((AsciiToInt(bt[0])<<4)+AsciiToInt(bt[1]));
	    		head=2;
	    	}
	    	int start=loc+len-2;
	    	for(int i=1;i<len;i++){	    		
	    		frame[start]=(byte)((AsciiToInt(bt[head])<<4)+AsciiToInt(bt[head+1]));
	    		head+=2;
	    		start--;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    /**
     * hex to byte[] 
     * @param hex
     * @return
     */
    public static void HexsToBytesAA(byte[] frame,int loc,String hex,int flen,byte invalid){    	
    	try{
    		int len=(hex.length()>>>1)+(hex.length() & 0x1);
	    	byte[] bt=hex.getBytes();
	    	int head=0;
	    	if((hex.length() & 0x1)>0){
	    		frame[loc+len-1]=(byte)AsciiToInt(bt[0]);
	    		head=1;
	    	}else{
	    		frame[loc+len-1]=(byte)((AsciiToInt(bt[0])<<4)+AsciiToInt(bt[1]));
	    		head=2;
	    	}
	    	int start=loc+len-2;
	    	for(int i=1;i<len;i++){	    		
	    		frame[start]=(byte)((AsciiToInt(bt[head])<<4)+AsciiToInt(bt[head+1]));
	    		head+=2;
	    		start--;
	    	}
	    	start=len+loc;
	    	for(int i=len;i<flen;i++){
	    		frame[start]=invalid;
	    		start++;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    /**
     * ʮ��������ת��Ϊbyte���飨��λ��ǰ����λ�ں�
     * @param data     ԭʼ����
     * @param len      ת�����ֽ��� ������4
     * @param fraction С��λ��
     * @return
     */
    public static void DecimalToBytes(byte[] frame,int val,int loc,int len){    	
    	try{    		
    		int vals=val;
    		for(int i=0;i<len;i++){    			
    			frame[loc+i]=(byte)(vals & 0xff);
    			vals>>>=8;
    		}    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * ʮ��������ת��Ϊbyte���飨��λ�ں󣬸�λ��ǰ��
     * @param data     ԭʼ����
     * @param len      ת�����ֽ��� ������4
     * @param fraction С��λ��
     * @return
     */
    public static void DecimalToBytesC(byte[] frame,int val,int loc,int len){    	
    	try{    		
    		int vals=val;
    		for(int i=0;i<len;i++){    			
    			frame[loc+len-1-i]=(byte)(vals & 0xff);
    			vals>>>=8;
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * A1A2B2B1��������ַת��Ϊ�ֽ�A1A2B1B2(������4�ֽ�)
     * @param frame
     * @param val
     * @param loc
     * @param len
     */
    public static void RtuaToBytesC(byte[] frame,int val,int loc,int len){    	
    	try{    		
    		frame[loc]=(byte)((val & 0xff000000)>>>24);
    		frame[loc+1]=(byte)((val & 0xff0000)>>>16);
    		frame[loc+2]=(byte)(val & 0xff);
    		frame[loc+3]=(byte)((val & 0xff00)>>>8);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * A1A2B2B1��������ַת��Ϊ�ֽ�A1A2B1B2(������4�ֽ�)
     * @param frame
     * @param val
     * @param loc
     * @param len
     */
    public static void RtuaToBytesC(byte[] frame,String val,int loc,int len){    	
    	try{    		
    		int ival=Integer.parseInt(val);
    		frame[loc]=(byte)((ival & 0xff000000)>>>24);
    		frame[loc+1]=(byte)((ival & 0xff0000)>>>16);
    		frame[loc+2]=(byte)(ival & 0xff);
    		frame[loc+3]=(byte)((ival & 0xff00)>>>8);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * �����ִ�ת��Ϊ�ֽ�����
     * @param time ʱ���ִ�
     * @param len  ����
     * @param type ���� 0��yyyy-MM-dd hh:mm:ss 1: yyyy-MM-dd hh:mm 
     * @return
     */
    public static byte[] DateToBytes(String time,int len,int type){
    	byte[] rt=null;
    	try{    		
    		if(type==0){
    			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		Date date=sdf.parse(time);    		
        		Calendar cd=Calendar.getInstance();
        		cd.setTime(date);
    			rt=new byte[6];
    			rt[0]=IntToBcd(cd.get(Calendar.SECOND));
    			rt[1]=IntToBcd(cd.get(Calendar.MINUTE));
    			rt[2]=IntToBcd(cd.get(Calendar.HOUR_OF_DAY));
    			rt[3]=IntToBcd(cd.get(Calendar.DAY_OF_MONTH));
    			rt[4]=IntToBcd(cd.get(Calendar.MONTH)+1);
    			rt[5]=IntToBcd(cd.get(Calendar.YEAR));
    		}
    		if(type==1){
    			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        		Date date=sdf.parse(time);    		
        		Calendar cd=Calendar.getInstance();
        		cd.setTime(date);
    			rt=new byte[5];    			
    			rt[0]=IntToBcd(cd.get(Calendar.MINUTE));
    			rt[1]=IntToBcd(cd.get(Calendar.HOUR_OF_DAY));
    			rt[2]=IntToBcd(cd.get(Calendar.DAY_OF_MONTH));
    			rt[3]=IntToBcd(cd.get(Calendar.MONTH)+1);
    			rt[4]=IntToBcd(cd.get(Calendar.YEAR));
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * ʱ���ִ�ת��Ϊ�ֽ�����
     * @param time ʱ���ִ�
     * @param len  ����
     * @param type ���� 0��hh:mm:ss 1: hh:mm 
     * @return
     */
    public static byte[] TimeToBytes(String time,int len,int type){
    	byte[] rt=null;
    	try{    		
    		String[] cells=time.split(":");
    		if(type==0){    			
    			rt=new byte[3];
    			rt[0]=IntToBcd(Integer.parseInt(cells[2]));
    			rt[1]=IntToBcd(Integer.parseInt(cells[1]));
    			rt[2]=IntToBcd(Integer.parseInt(cells[0]));    			
    		}
    		if(type==1){    			
    			rt=new byte[2];    			
    			rt[0]=IntToBcd(Integer.parseInt(cells[1]));
    			rt[1]=IntToBcd(Integer.parseInt(cells[0]));    			
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * ȡ���䷽��
     * @param message
     * @return 0����վ�·� 1���ն�Ӧ��
     */
    public static int getOrientation(IMessage message){
    	int rt=0;
    	try{
    		byte reply=((MessageZj)message).head.c_dir;
    		if((reply & 0xff)>0){
    			rt=1;
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * ȡ�������
     * @param message
     * @return
     */
    public static int getErrCode(IMessage message){
    	int rt=0;
    	try{
    		byte err=((MessageZj)message).head.c_expflag;
    		rt=(err & 0xff);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * ȡ��Ϣ������
     * @param message
     * @return
     */
    public static byte[] getData(IMessage message){
    	byte[] rt=null;
    	try{		
    		ByteBuffer data=null;
    		if(message instanceof MessageZj){
    			data=((MessageZj)message).data;
    		}  		
    		data.rewind();		//just for debug --- by yangdh  2006/12/29
    		int len=data.limit();
    		if(len>0){
    			rt=new byte[len];
    			data.get(rt);
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return rt;
    }
    
    /**
     * ��������ʱ��(������ ʱ��)
     * @param data
     * @param offset
     * @return
     */
    public static Calendar getTime(byte[] data,int offset){
    	Calendar rt=Calendar.getInstance();
    	try{
	    	int num=ParseTool.BCDToDecimal(data[4+offset]);
	    	rt.set(Calendar.YEAR,num+2000);
	    	num=ParseTool.BCDToDecimal((byte)(data[3+offset] & 0x1f));
	    	rt.set(Calendar.MONTH,num-1);
	    	num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
	    	rt.set(Calendar.DAY_OF_MONTH,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[1+offset] & 0x3f));
	    	rt.set(Calendar.HOUR_OF_DAY,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[0+offset] & 0x7f));
	    	rt.set(Calendar.MINUTE,num);
	    	rt.set(Calendar.SECOND,0);
	    	rt.set(Calendar.MILLISECOND,0);
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    	return rt;
    }
    
    /**
     * ��������ʱ��(������ ʱ����)
     * @param data
     * @param offset
     * @return
     */
    public static Calendar getTimeW(byte[] data,int offset){
    	Calendar rt=Calendar.getInstance();
    	try{
	    	int num=ParseTool.BCDToDecimal(data[5+offset]);
	    	rt.set(Calendar.YEAR,num+2000);
	    	num=ParseTool.BCDToDecimal((byte)(data[4+offset] & 0x1f));
	    	rt.set(Calendar.MONTH,num-1);
	    	num=ParseTool.BCDToDecimal((byte)(data[3+offset] & 0x3f));
	    	rt.set(Calendar.DAY_OF_MONTH,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
	    	rt.set(Calendar.HOUR_OF_DAY,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[1+offset] & 0x7f));
	    	rt.set(Calendar.MINUTE,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[0+offset] & 0x7f));
	    	rt.set(Calendar.SECOND,num);
	    	rt.set(Calendar.MILLISECOND,0);
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    	return rt;
    }
    
    /**
     * ��������ʱ��(���� ʱ��)
     * @param data
     * @param offset
     * @return
     */
    public static Calendar getTimeM(byte[] data,int offset){
    	Calendar rt=Calendar.getInstance();
    	try{	    	
	    	int num=ParseTool.BCDToDecimal((byte)(data[3+offset] & 0x1f));
	    	rt.set(Calendar.MONTH,num-1);
	    	num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
	    	rt.set(Calendar.DAY_OF_MONTH,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[1+offset] & 0x3f));
	    	rt.set(Calendar.HOUR_OF_DAY,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[0+offset] & 0x7f));
	    	rt.set(Calendar.MINUTE,num);
	    	rt.set(Calendar.SECOND,0);
	    	rt.set(Calendar.MILLISECOND,0);
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    	return rt;
    }
    
    /**
     * ��������ʱ��(ʱ����)
     * @param data
     * @param offset
     * @return
     */
    public static Calendar getTimeL(byte[] data,int offset){
    	Calendar rt=Calendar.getInstance();
    	try{	    	
	    	int num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
	    	rt.set(Calendar.HOUR_OF_DAY,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[1+offset] & 0x5f));
	    	rt.set(Calendar.MINUTE,num);
	    	num=ParseTool.BCDToDecimal((byte)(data[0+offset] & 0x5f));
	    	rt.set(Calendar.SECOND,0);
	    	rt.set(Calendar.MILLISECOND,0);
    	}catch(Exception e){
    		e.printStackTrace();
    	}    	
    	return rt;
    }
    
    /**
     * ��IP��IP��ַ�Ͷ˿� 
     * @param frame
     * @param loc
     * @param ip 127.0.0.1:9001
     */
    public static void IPToBytes(byte[] frame,int loc,String ip){
    	try{
    		String[] para=ip.split(":");
    		Inet4Address netaddress=(Inet4Address)Inet4Address.getByName(para[0]);
    		byte[] bip=netaddress.getAddress();
    		for(int i=0;i<bip.length;i++){
    			frame[loc+2+i]=bip[bip.length-i-1];
    		}
    		int port=Integer.parseInt(para[1]);
    		frame[loc]=(byte)(port & 0xff);
    		frame[loc+1]=(byte)((port & 0xff00)>>8);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * �Ƿ�Ϊ�Ƿ����ݣ�ȫ0xff��
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isValid(byte[] data,int start,int len){
    	boolean rt=false;
    	for(int i=start;i<start+len;i++){
    		if((data[i] & 0xff)!=0xff){
    			rt=true;
    			break;
    		}
    	}
    	return rt;
    }
    
    /**
     * �Ƿ�Ϊ�Ƿ����ݣ�ȫ0xff,0xee��
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isValidBCD(byte[] data,int start,int len){
    	boolean rt=true;
    	if((data[start]& 0xff)==0xff){
    		rt=!isAllFF(data,start,len);
    	}
    	if((data[start]& 0xff)==0xee){
    		//rt=!isAllEE(data,start,len);
    		rt=false;//hualong just one
    	}
    	return rt;
    }
    /**
     * �Ƿ��зǷ����ݣ�0xff��
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isHaveValidBCD(byte[] data,int start,int len){
    	boolean rt=true;
    	rt=!isHaveFF(data,start,len);
    	if((data[start]& 0xff)==0xee){
    		//rt=!isAllEE(data,start,len);
    		rt=false;//hualong just one
    	}
    	return rt;
    }
    /**
     * �Ƿ�Ϊ�Ƿ����ݣ�ȫ0xee��
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isAllEE(byte[] data,int start,int len){
    	boolean rt=true;
    	for(int i=start;i<start+len;i++){
    		if((data[i] & 0xff)!=0xEE){
    			rt=false;
    			break;
    		}
    	}
    	return rt;
    }
    /**
     * �Ƿ�Ϊ�Ƿ����ݣ�ȫ0xff��
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isAllFF(byte[] data,int start,int len){
    	boolean rt=true;
    	for(int i=start;i<start+len;i++){
    		if((data[i] & 0xff)!=0xFF){
    			rt=false;
    			break;
    		}
    	}
    	return rt;
    }
    /**
     * �Ƿ��зǷ����ݣ�0xff��,Ϊ������Լ���ӵ��ж�
     * @param data
     * @param start
     * @param len
     * @return
     */
    public static boolean isHaveFF(byte[] data,int start,int len){
    	boolean rt=false;
    	for(int i=start;i<start+len;i++){
    		if((data[i] & 0xff)==0xFF){
    			rt=true;
    			break;
    		}
    	}
    	return rt;
    }
    /**
     * �Ϸ�BCD�·��ж�
     * @param data
     * @return
     */
    public static boolean isValidMonth(byte data){
    	boolean rt=false;
    	int hi=BCDToDecimal(data);
    	if((hi>=0 && hi<=12)){
    		rt=true;
    	}
    	return rt;
    }
    
    /**
     * �Ϸ�BCD���ж�
     * @param data
     * @param month
     * @param year
     * @return
     */
    public static boolean isValidDay(byte data,int month,int year){
    	boolean rt=false;
    	int hi=BCDToDecimal(data);
    	if((hi>=0 && hi<=31)){	//valid range
    		if(month==2){
    			if(year<0){
    				if(hi<days[month]){
    					rt=true;
    				}
    			}else{
    				if(isLeapYear(year)){
    					if(hi<=29){
    						rt=true;
    					}
    				}else{
    					if(hi<=28){
    						rt=true;
    					}
    				}
    			}
    		}else{
    			if(hi<=days[month]){
    				rt=true;
    			}
    		}    		
    	}
    	return rt;
    }
    
    /**
     * �Ϸ�ʱ/��/���ж�
     * @param data
     * @return
     */
    public static boolean isValidHHMMSS(byte data){
    	boolean rt=false;
    	int hi=BCDToDecimal(data);
    	if(hi>=0 && hi<=60){		//rtu may be wrong,but we need record
    		rt=true;
    	}
    	return rt;
    }
    
    /**
     * �����ж�
     * @param year
     * @return
     */
    public static boolean isLeapYear(int year){
    	boolean rt=false;
    	if(year>=0){
	    	if((year % 100)==0){
	    		if((year % 400)==0){
	    			rt=true;
	    		}
	    	}else{
	    		if((year % 4)==0){
	    			rt=true;
	    		}
	    	}
    	}
    	return rt;
    }
    
   
    public static byte calculateCS(byte[] data,int start,int len){
		int cs=0;
		for(int i=start;i<start+len;i++){
			cs+=(data[i] & 0xff);
			cs&=0xff;
		}
		return (byte)(cs & 0xff);
	}
    
    /**
     * ȡ��Լ����
     * @param type
     * @return
     */
    public static String getMeterProtocol(String type){
    	if(type!=null){
    		if(type.equals("10")){	//����
    			return "BBMeter";
    		}
    		if(type.equals("20")){	//�㽭
    			return "ZJMeter";
    		}
    		if(type.equals("40")){	//siemens
    			return "SMMeter";
    		}
    	}
    	return null;
    }
    
    /**
     * �Ƿ�Ϊ��������������
     * @param datakey
     * @return
     */
    public static boolean isTask(int datakey){
    	boolean rt=false;
    	if((datakey>=0x8101) && (datakey<0x81FE)){
    		rt=true;
    	}
    	return rt;
    }
    
    public static boolean isValidBCDString(String val){
    	boolean rt=true;
    	if(val!=null){
    		for(int i=0;i<val.length();i++){
    			char c=val.charAt(i);				
				if(c>='0' && c<='9'){
					continue;
				}else{
					rt=false;
					break;
				}
    		}
    	}else{
    		rt=false;
    	}
    	return rt;
    }
}
