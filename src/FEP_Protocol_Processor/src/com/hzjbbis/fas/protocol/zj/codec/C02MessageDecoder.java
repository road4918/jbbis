package com.hzjbbis.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fas.model.RtuData;
import com.hzjbbis.fas.model.RtuDataItem;
import com.hzjbbis.fas.protocol.codec.MessageCodecContext;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.meter.IMeterParser;
import com.hzjbbis.fas.protocol.meter.MeterParserFactory;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;
import com.hzjbbis.fas.protocol.zj.parse.TaskSetting;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.MeasuredPoint;
/**
 * 读任务数据(功能码：02H)响应消息解码器
 * @author yangdinghuan
 *
 */
public class C02MessageDecoder extends AbstractMessageDecoder{	
	
	public Object decode(IMessage message) {
        List<RtuData> datas = new ArrayList<RtuData>();
		try{
			if(getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
				int rtype=(getErrCode(message));			
				if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
					//取应答数据
        			byte[] data=getData(message);
                    
        			if(data.length>9){	//至少9字节数据（1字节任务号+6字节起始时间+1字节时间间隔单位+1字节时间间隔值）        				
        				String tasknum=String.valueOf(data[0] & 0xff);
        				TaskSetting ts=new TaskSetting(((MessageZj)message).head.rtua,(data[0] & 0xff),super.dataConfig);        				
        				if(ts!=null && ts.getRtask()!=null && ts.getDataNum()>0){	//终端任务配置获取正确
        					Calendar time=getTime(data,1);
        					if(time==null){
        						throw new MessageDecodeException("帧中包含的时间数据错误:"+ParseTool.BytesToHex(data,1,5));
        					}
        					
        					//时间判断，如果是终端自动上报，且接受报文时间是当前时间，
        					//如果报文内数据时间与当前时间差6小时，则报时钟错误。应用启动12小时内不判断，用以防止网关积压

        					int pnum=data[6] & 0xFF;	//数据点数
        					int ti=getTimeInterval(data[7]);	//数据点时间间隔类型
        					int tn=data[8] & 0xFF;		//数据点时间间隔值
        					
        					if(ts.getTT()==2){
        						parseRelayTask(data,9,time,tasknum,ts,datas);
        					}else{        						
        						//任务解析
        						if((data.length-9)==(ts.getDataLength()*pnum)){        						            						
            						List datacs=ts.getDI();
            						int loc=9;
            						Hashtable<String,String> keys=new Hashtable<String,String>();//防止重复定义项 by yangdh---2007/02/06            						
            						for(int i=0;i<pnum;i++){	//循环每个数据点
            							RtuData bean = new RtuData();
            							RtuDataItem beanItem=null;
            							keys.clear();						
            							for(int j=0;j<ts.getDataNum();j++){	//循环每个数据项
            								ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)datacs.get(j);            								
            								List childs=pdc.getChildItems();
            								if(childs!=null && childs.size()>0){
            									for(Iterator iterc=childs.iterator();iterc.hasNext();){
            										ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)iterc.next();
            										Object di=DataItemParser.parsevalue(data,loc,cpdc.getLength(),cpdc.getFraction(),cpdc.getParserno());        	    								
        	        								loc+=cpdc.getLength();       	        								    	                                                    	                                           
    	                                            if(!keys.containsKey(cpdc.getCode())){
    	                                            	beanItem=new RtuDataItem();
    	                                            	beanItem.setCode(cpdc.getCode());
    	                                            	beanItem.setValue((di==null)?null:di.toString());  
    	                                            	bean.addDataList(beanItem);
    	                                            	keys.put(cpdc.getCode(), cpdc.getCode());
    	                                            }   
    	                                            bean.setLogicAddress(ts.getRtu().getLogicAddress());    	                                                    	                                        
        	                                        bean.setTaskNum(tasknum);        	                                      
        	                                        bean.setTime(time.getTime());
        	                                        /*
        	                                        bean.setTn(String.valueOf(ts.getTN()));
        	                                        bean.setDeptCode(ts.getDeptCode());
    	                                            bean.setTaskProperty(ts.getTaskProperty());
        	                                        bean.setDataSaveID(ts.getDataSaveID());
        	                                        bean.setCt(ts.getCt());
        	                                        bean.setPt(ts.getPt());*/
            									}
            								}else{
    	        								Object di=DataItemParser.parsevalue(data,loc,pdc.getLength(),pdc.getFraction(),pdc.getParserno());   	        								
    	        								loc+=pdc.getLength();  	                                        	                                        	
	                                            if(!keys.containsKey(pdc.getCode())){	                                            	
	                                            	beanItem=new RtuDataItem();
	                                            	beanItem.setCode(pdc.getCode());
	                                            	beanItem.setValue((di==null)?null:di.toString());  
	                                            	bean.addDataList(beanItem);
	                                            	keys.put(pdc.getCode(), pdc.getCode());
	                                            }  
	                                            bean.setLogicAddress(ts.getRtu().getLogicAddress());	                                            
	                                            bean.setTime(time.getTime());
	                                            bean.setTaskNum(tasknum);
	                                            /*
	                                            bean.setTn(String.valueOf(ts.getTN()));
	                                            bean.setDeptCode(ts.getDeptCode());	                                           
    	                                        bean.setDataSaveID(ts.getDataSaveID());    	                                            	                                       
    	                                        bean.setTaskProperty(ts.getTaskProperty());    	                                        
    	                                        bean.setCt(ts.getCt());
    	                                        bean.setPt(ts.getPt());*/
            								}
            							}  
            							datas.add(bean);
            							if(ti<=1440){
            								time.add(Calendar.MINUTE,ti*tn);
            							}else{
            								//时间间隔为月
            								time.add(Calendar.MONTH,tn);
            							}
            						}        						
            					}else{
            						MessageCodecContext.setTaskNum(tasknum);
            						String msg="终端逻辑地址："+ParseTool.IntToHex4(((MessageZj)message).head.rtua)+"，任务号："+tasknum;
            						msg+="\r\n"+"数据长度不对,期望数据长度："+(ts.getDataLength()*pnum+9)+"  上报数据长度："+data.length;
            						throw new MessageDecodeException(msg);
            					}
        					}        					
        				}else{
        					//未知的任务
        					MessageCodecContext.setTaskNum(tasknum);
        					throw new MessageDecodeException("未知的任务");
        				}
        			}
        			else{
        				String msg="终端逻辑地址："+ParseTool.IntToHex4(((MessageZj)message).head.rtua)+"任务数据长度非法";   						
                        throw new MessageDecodeException(msg);
        			}
        		}else{
        		}
			}else{
				//by yangjie 屏蔽下行请求命令ID,需要在数据库查询获得
				/*
				//处理任务补召返回帧为6897056201C100680200009216的情况 by yangjie 2008/03/19
				HostCommand hc=(HostCommand)message.getAttachment();
				if (hc != null) {
                    hc.setStatus(HostCommand.STATUS_PARSE_ERROR);                    
                    String msg="终端逻辑地址："+ParseTool.IntToHex4(((MessageZj)message).head.rtua)+"任务数据格式非法";   						
                    throw new MessageDecodeException(msg);
                }*/
				String msg="终端逻辑地址："+ParseTool.IntToHex4(((MessageZj)message).head.rtua)+"任务数据格式非法";   						
                throw new MessageDecodeException(msg);
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
//		if(log.isDebugEnabled()){
//			log.debug("task msg parse is over,item counter is:"+datas.size());
//		}
		return datas;
	}
    
   /* private RtuData getBean(String beanClass, int tn, int pnum, List datas, Map<String,Object> beanMap) {
        String key = beanClass + "." + tn + "." + pnum;
        
        Object bean = beanMap.get(key);
        if (bean == null) {
        
            Class clazz;
            try {
                clazz = Class.forName(beanClass);
                bean = clazz.newInstance();
            }
            catch (Exception ex) {
                throw new RuntimeException("Error to instantiating bean: " + beanClass, ex);
            }
            
            datas.add(bean);
            beanMap.put(key, bean);
        }
        
        return (RtuData) bean;
    }*/
	
	/**
     * 取传输方向
     * @param message
     * @return 0：主站下发 1：终端应答
     */
    private int getOrientation(IMessage message){    	
    	return ParseTool.getOrientation(message);
    }
    
    /**
     * 取错误编码
     * @param message
     * @return
     */
    private int getErrCode(IMessage message){    	
    	return ParseTool.getErrCode(message);
    }
    
    /**
     * 取消息数据体
     * @param message
     * @return
     */
    private byte[] getData(IMessage message){
    	return ParseTool.getData(message);
    }
        
    /**
     * 解析数据时间
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
    		e.printStackTrace();
    	}    	
    	return rt;
    }
    
    /**
     * 时间间隔（统一单位为分，月比较特殊，单独处理，见解析过程）
     * @param type
     * @return
     */
    private int getTimeInterval(byte type){
    	int rt=0;
    	switch(type){
	    	case 2:
	    		rt=1;
	    		break;
	    	case 3:
	    		rt=60;
	    		break;
	    	case 4:
	    		rt=1440;
	    		break;
	    	case 5:
	    		rt=43200;
	    		break;
	    	default:
	    		break;
    	}    	
    	return rt;
    }
    
    private void parseRelayTask(byte[] data,int pos,Calendar time,String tasknum,TaskSetting ts,List<RtuData> result){
    	if(data!=null){
    		//取测量点规约类型
    		MeasuredPoint mp=ts.getRtu().getMeasuredPoint(String.valueOf(ts.getTN()));
    		if(mp==null){
        		throw new MessageDecodeException("指定测量点不存在！终端--"+ts.getRtu().getLogicAddress()+"  测量点--"+ts.getTN());
        	}
    		String pm=ParseTool.getMeterProtocol(mp.getAtrProtocol());
        	if(pm==null){
        		throw new MessageDecodeException("不支持的表规约："+mp.getAtrProtocol());
        	}
        	IMeterParser mparser=MeterParserFactory.getMeterParser(pm);
        	if(mparser==null){
        		throw new MessageDecodeException("不支持的表规约："+mp.getAtrProtocol());
        	}        	
    		//解析表帧
        	Object[] dis=mparser.parser(data,pos,data.length-pos);
        	if(dis!=null){
        		RtuData bean = new RtuData();
        		RtuDataItem beanItem=null;
        		for(int i=0;i<dis.length;i++){
        			DataItem di=(DataItem)dis[i]; 
                    beanItem=new RtuDataItem();
                	beanItem.setCode((String)di.getProperty("datakey"));
                	beanItem.setValue((di==null)?null:di.toString());  
                	bean.addDataList(beanItem);                  	
                    bean.setLogicAddress(ts.getRtu().getLogicAddress());                                        
                    bean.setTaskNum(tasknum);                    
                    bean.setTime(time.getTime());
                    /*
                    bean.setTn(String.valueOf(ts.getTN()));
                    bean.setTaskProperty(ts.getTaskProperty());
                    bean.setDeptCode(ts.getDeptCode());
                    bean.setCt(ts.getCt());
                    bean.setPt(ts.getPt());*/
                    result.add(bean);
        		}
        	}
    	}
    }
}
