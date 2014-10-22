package com.hzjbbis.fas.protocol.zj.codec;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.RtuAlert;
import com.hzjbbis.fas.model.RtuAlertArg;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


public class C09MessageDecoder extends AbstractMessageDecoder{
	private static final Log log=LogFactory.getLog(C09MessageDecoder.class);
	
	public Object decode(IMessage message) {
		List<RtuAlert> rt=null;
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
				int rtype=(ParseTool.getErrCode(message));
				if(rtype==DataMappingZJ.ERROR_CODE_OK){	//�����ն�Ӧ��
					//ȡӦ������
        			byte[] data=ParseTool.getData(message);
        			
        			if(data!=null){
        				int num=(data[0] & 0xff);	//�澯����
        				int loc=1;
        				//ȡ�ն˶���
        				int rtu=((MessageZj)message).head.rtua;
        				BizRtu ortu=RtuManage.getInstance().getBizRtuInCache(rtu);
        				if(ortu==null){
        					//log.warn("�޷���ȡ�ն���Ϣ--"+ParseTool.IntToHex4(rtu));
        					throw new MessageDecodeException("�޷���ȡ�ն���Ϣ--"+ParseTool.IntToHex4(rtu));
        				}
        				NumberFormat nf=NumberFormat.getInstance();
        				nf.setMaximumFractionDigits(4);	        				
        				rt=new ArrayList<RtuAlert>();
        				Calendar date=Calendar.getInstance();
                        //��IOʱ����Ϊ����ʱ��        				
        				date.setTimeInMillis(((MessageZj)message).getIoTime());        				
        				while(loc<data.length){
        					if(8<=(data.length-loc)){	        						
        						int point=(data[loc] & 0xff);	//������
        						Calendar stime=getTime(data,loc+1);	//�澯����ʱ��
        						int alr=(data[loc+6] & 0xff)+((data[loc+7] & 0xff)<<8);	//�澯����
        						loc+=8;	        						
        						List rac=getRtuAlertCode(alr);	 //ȡ�澯����	
        						if(rac!=null){
        							RtuAlert ra=new RtuAlert();	        							
	        						ra.setRtuId(ortu.getRtuId());
                                    ra.setCorpNo(ortu.getDeptCode());                                        
                                    String tn = String.valueOf(point);
                                    ra.setTn(tn);
                                    if (ortu.getMeasuredPoint(tn)!=null){
                                    	ra.setDataSaveID(ortu.getMeasuredPoint(tn).getDataSaveID());
                                        ra.setCustomerNo(ortu.getMeasuredPoint(tn).getCustomerNo());                                       
                                        ra.setStationNo(ortu.getMeasuredPoint(tn).getCustomerNo());
                                    }
                                    else log.warn("rtu="+ParseTool.IntToHex4(rtu)+",tn="+tn+"δ������");
                                    ra.setAlertCode(alr);
                                    ra.setAlertTime(stime.getTime());
                                    //��IOʱ����Ϊ����ʱ��
                                    ra.setReceiveTime(new Date(((MessageZj) message).getIoTime()));                                    
        							List<RtuAlertArg> alertdatas=new ArrayList<RtuAlertArg>();
	        						String olddi="";
        							Hashtable<String,RtuAlertArg> databag=new Hashtable<String,RtuAlertArg>();	//�����ʱ���ݣ�Ϊ��ƥ���Ӧ��
	        						for(Iterator iter=rac.iterator();iter.hasNext();){
        								String di=(String)iter.next();
        								boolean is8ffe=false;	        								
        								if(di.equals("8FFE") || di.equals("8ffe")){
        									//ȡǰһ������id
        									if(olddi.length()<=0){
        										//���ô���
        										throw new MessageDecodeException("�澯���ô����ն�--"+ParseTool.IntToHex4(rtu)+" �澯����--"+ParseTool.IntToHex(alr) );
        									}
        									is8ffe=true;
        									di=olddi;
        								}else{
        									is8ffe=false;
        									olddi=di;
        								}
        								ProtocolDataItemConfig pdc=super.dataConfig.getDataItemConfig(di);
        								if(pdc!=null){
        									if(pdc.getLength()<=(data.length-loc)){
        										List childs=pdc.getChildItems();
        										if(childs!=null && childs.size()>0){
        											for(Iterator iterc=childs.iterator();iterc.hasNext();){
        												ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)iterc.next();
        												Object dt=null;
        												try{
        													dt=DataItemParser.parsevalue(data,loc,cpdc.getLength(),cpdc.getFraction(),cpdc.getParserno());			        										
        												}catch(Exception e){
        													//�������ݽ�������
        													log.error("�澯���ݽ�������",e);
        												}
        												RtuAlertArg arg=null;
        												if(dt!=null){
		        											if(is8ffe){
		        												arg=(RtuAlertArg)databag.get(cpdc.getCode());
		        												arg.setCorrelValue(dt.toString());
		        											}else{
		        												arg=new RtuAlertArg();
				        										arg.setCode(cpdc.getCode());
		        												arg.setValue(dt.toString());
		        												databag.put(cpdc.getCode(),arg);
		        												alertdatas.add(arg);
		        											}
		        											
		        										}else{
		        											if(!is8ffe){
			        											arg=new RtuAlertArg();
				        										arg.setCode(cpdc.getCode());
			        											arg.setValue(null);
			        											alertdatas.add(arg);
			        											databag.put(cpdc.getCode(),arg);
		        											}
		        										}			        										
		        										loc+=cpdc.getLength();
        											}
        										}else{
        											Object dt=DataItemParser.parsevalue(data,loc,pdc.getLength(),pdc.getFraction(),pdc.getParserno());
	        										RtuAlertArg arg=null;		        										
	        										if(dt!=null){
	        											if(is8ffe){
	        												arg=(RtuAlertArg)databag.get(pdc.getCode());
	        												arg.setCorrelValue(dt.toString());		        												
	        											}else{
	        												arg=new RtuAlertArg();
			        										arg.setCode(pdc.getCode());				        										
	        												arg.setValue(dt.toString());
	        												alertdatas.add(arg);
	        												databag.put(pdc.getCode(),arg);
	        											}
	        										}else{
	        											if(!is8ffe){
		        											arg=new RtuAlertArg();
			        										arg.setCode(pdc.getCode());	
		        											arg.setValue(null);
		        											alertdatas.add(arg);
		        											databag.put(pdc.getCode(),arg);
	        											}
	        										}
	        										loc+=pdc.getLength();
        										}
        									}else{
        										//���ݳ��Ȳ���        										    									
        										log.info("���ݳ��Ȳ���,�����"+pdc.getCode()+" �������ݳ��ȣ�"+pdc.getLength()+" �������ȣ�"+(data.length-loc));
        										loc=data.length;	//����澯���ݽ������󣨱���澯�������ʹ������ݿ������ô��󣩣�������¼�澯
        										break;
        									}
        								}else{
        									//�޷�ʶ���������
        									throw new MessageDecodeException("�޷�ʶ���������");
        								}
        							}
	        						ra.setArgs(alertdatas);
	        						rt.add(ra);
        						}else{
	        						throw new MessageDecodeException("�޷���ȡ�澯����"+" �澯����--"+ParseTool.IntToHex(alr));
        						}
        					}else{
        						//���ݳ��Ȳ���
        						throw new MessageDecodeException("���ݳ��Ȳ���");
        					}
        					num--;
        					if((num<=0) && (loc<data.length)){
        						//���ݳ��Ȳ���
        						//throw new MessageDecodeException("�澯��������");	        						
								log.info("���ݳ��Ȳ���,Ԥ�ڳ��ȣ�"+loc+" �������ȣ�"+data.length);
								loc=data.length;	//����澯���ݽ������󣨱���澯�������ʹ������ݿ������ô��󣩣�������¼�澯
								break;
        					}
        				}
        			}else{
        				//����ȱʧ
        				throw new MessageDecodeException("���ݳ��Ȳ���");
        			}
				}else{
				}
			}else{
				//��վ�ٲ�				
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		//���쳣Я����������ת�����ַ���,���ڱ���
		if (rt!=null){
			for(int i=0;i<rt.size();i++){
				RtuAlert rtuAlert=(RtuAlert)rt.get(i);
				rtuAlertSetSbcs(rtuAlert);
			}
		}		
		return rt;
	}
	private void rtuAlertSetSbcs(RtuAlert rtuAlert){
		try{
			StringBuffer sb=new StringBuffer();
	        List args = rtuAlert.getArgs();
	        if(args!=null && args.size()>0){
	        	RtuAlertArg arg = (RtuAlertArg) args.get(0);
	        	sb.append(arg.getCode());
	        	sb.append("=");
	        	if(arg.getValue()!=null){
	        		sb.append(arg.getValue());
	        	}
	        	sb.append("@");
	        	if(arg.getCorrelValue()!=null){
	        		sb.append(arg.getCorrelValue());
	        	}
	        	for (int i = 1; i < args.size(); i++) {
	        		arg = (RtuAlertArg) args.get(i);
	        		sb.append(";");
	        		sb.append(arg.getCode());
	            	sb.append("=");
	            	if(arg.getValue()!=null){
	            		sb.append(arg.getValue());
	            	}
	            	sb.append("@");
	            	if(arg.getCorrelValue()!=null){
	            		sb.append(arg.getCorrelValue());
	            	}
	        	}
	        	rtuAlert.setSbcs(sb.toString());
	        }
		}catch(Exception ex){
			
		}
	}
	
	/**
     * ��������ʱ��
     * @param data
     * @param offset
     * @return
     */
    private Calendar getTime(byte[] data,int offset){
    	Calendar rt=null;
    	try{
    		int month=ParseTool.BCDToDecimal(data[1+offset]);
    		int year=ParseTool.BCDToDecimal(data[offset]);
    		if(month>0 && year>=0){
	    		if(ParseTool.isValidMonth(data[1+offset]) && ParseTool.isValidDay(data[2+offset],month,year+2000)
	    				&& ParseTool.isValidHHMMSS(data[3+offset]) && ParseTool.isValidHHMMSS(data[4+offset]))
	    		{		    	
			    	rt=Calendar.getInstance();		    		
			    	rt.set(Calendar.YEAR,year+2000);			    	
			    	rt.set(Calendar.MONTH,month-1);			    	
			    	int num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
			    	rt.set(Calendar.DAY_OF_MONTH,num);
			    	num=ParseTool.BCDToDecimal((byte)(data[3+offset] & 0x3f));
			    	rt.set(Calendar.HOUR_OF_DAY,num);
			    	num=ParseTool.BCDToDecimal((byte)(data[4+offset] & 0x7f));
			    	if(num>=60){
			    		rt.add(Calendar.HOUR_OF_DAY,1);
			    		num=0;
			    	}
			    	rt.set(Calendar.MINUTE,num);
			    	rt.set(Calendar.SECOND,0);
			    	rt.set(Calendar.MILLISECOND,0);
	    		}
    		}
    	}catch(Exception e){
    		log.error("alert decode",e);
    	}    	
    	return rt;
    }
    
	/**
	 * ȡ�ն˸澯����
	 * @param rtu
	 * @param alert
	 */
	private List getRtuAlertCode(int alert){
		List rt=null;
		try{
			rt=RtuManage.getInstance().getRtuAlertCode(alert).getArgs();
		}catch(Exception e){
			log.error("get alert code",e);
		}
		return rt;
	}
		
}
