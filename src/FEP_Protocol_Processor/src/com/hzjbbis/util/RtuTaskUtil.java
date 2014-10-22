package com.hzjbbis.util;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fk.model.TaskTemplate;


/**
 *@filename	RtuTaskUtil.java
 *@auther	netice
 *@date		2007-5-28
 *@version	1.0
 *TODO		�ն����񹤾�
 */
public class RtuTaskUtil {
	private static final Log log=LogFactory.getLog(RtuTaskUtil.class);
	public static final String TIME_UNIT_MINUTE="02";
	public static final String TIME_UNIT_HOUR="03";
	public static final String TIME_UNIT_DAY="04";
	public static final String TIME_UNIT_MONTH="05";
	
	public static final long SECONDES_IN_MINUTE=60;
	public static final long SECONDES_IN_HOUR=3600;
	public static final long SECONDES_IN_DAY=86400;
	
	public static final int[] dayInMonth=new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
	
	/**
	 * ���������ʱ��---ָ�ϱ�ʱ��
	 * @param task		��������
	 * @param time		ָ����ֹʱ��
	 * @return			���������ʱ�̣�������time
	 */
	public static long lastRuntime(TaskTemplate task,long time){
		long rt=time;
		try{
			if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				rt=lastRuntimeM(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_HOUR)){
				rt=lastRuntimeH(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_DAY)){
				rt=lastRuntimeD(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else{
				//
			}			
		}catch(Exception e){
			log.error("ȡ�������ʱ��",e);
		}
		return rt;
	}
	
	/**
	 * ����Ĳ���ʱ��
	 * @param task		��������
	 * @param time		ָ����ֹʱ��
	 * @return			����Ĳ���ʱ�̣�������time
	 */
	public static long lastSampTime(TaskTemplate task,long time){
		long rt=time;
		try{
			if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				rt=lastRuntimeM(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_HOUR)){
				rt=lastRuntimeH(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_DAY)){
				rt=lastRuntimeD(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else{
				//
			}	
		}catch(Exception e){
			log.error("ȡ�������ʱ��",e);
		}
		return rt; 
	}
	
	/**
	 * һ���Զ��ϱ������Ĳ�������
	 * @param task
	 * @return			����ֵ��0ʱ���п����ǰ�����������������������
	 */
	public static int rCapacity(TaskTemplate task){
		int rt=0;
		try{
			if(task!=null){
				if(task.getSampleInterval()>0 && task.getUploadInterval()>0){
					long sinterval=getUnitSecondes(task.getSampleIntervalUnit())*((long)task.getSampleInterval());
					long rinterval=getUnitSecondes(task.getUploadIntervalUnit())*((long)task.getUploadInterval());
					if(sinterval==0){	//������λ����
						if(rinterval==0){
							rt=task.getUploadInterval()/task.getSampleInterval();
						}
					}else{
						long frq=task.getFrequence();
						if(frq<=0){
							frq=1;
						}
						rt=(int)(rinterval/(sinterval*frq));
					}
				}
			}
		}catch(Exception e){
			log.error("ȡ�ϱ��������ݵ���",e);
		}
		return rt;
	}
	
	/**
	 * ��ȡ�����ϱ����ݿ�ʼ��ʱ��
	 * @param task		��������
	 * @param time		�ϱ�ʱ��
	 * @return
	 */
	public static long getStartTimeOfTask(TaskTemplate task,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//����
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			if(task.getUploadStartTimeUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				ctime.add(Calendar.MINUTE, -task.getUploadStartTime());
			}else{
				if(task.getUploadStartTimeUnit().equalsIgnoreCase(task.getUploadIntervalUnit())){
					int delt=(int)getUnitSecondes(task.getUploadStartTimeUnit())*task.getUploadStartTime();
					ctime.add(Calendar.SECOND, -delt);
				}
			}
			int spu=(int)getUnitSecondes(task.getUploadIntervalUnit())*task.getUploadInterval();
			ctime.add(Calendar.SECOND, spu);
			rt=ctime.getTimeInMillis();
		}catch(Exception e){
			log.error("",e);
		}
		return rt;
	}
	
	public static long getUnitSecondes(String unit){
		long rt=0;
		if(unit!=null){
			try{
				int u=Integer.parseInt(unit);
				switch(u){
					case 2:
						rt=SECONDES_IN_MINUTE;
						break;
					case 3:
						rt=SECONDES_IN_HOUR;
						break;
					case 4:
						rt=SECONDES_IN_DAY;
						break;
				}
			}catch(Exception e){
				log.error("��λʱ���������",e);
			}
		}
		return rt;
	}
	
	/**
	 * �������ʱ��----��������Ƿ�
	 * @param bUU		���л�׼��λ
	 * @param bNN		���л�׼
	 * @param iUU		���м����λ
	 * @param iNN		���м��
	 * @param time		��ֹʱ��
	 * @return			�������ʱ��
	 */
	private static long lastRuntimeM(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//����
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//��ֹʱ�̾����ķ�����
			int minutes=ctime.get(Calendar.HOUR_OF_DAY)*60+ctime.get(Calendar.MINUTE);
			if(minutes<bNN){//ָ��ʱ�̵���δ����һ��
				ctime.set(Calendar.MINUTE, bNN);
				ctime.add(Calendar.MINUTE, -iNN);
				rt=ctime.getTimeInMillis();
			}else{
				minutes-=bNN;
				long mtime=minutes % iNN;
				if(mtime>0){
					ctime.add(Calendar.MINUTE, -(int)mtime);
				}else{
					//��ֹʱ�̾�������ʱ��
					
				}
				rt=ctime.getTimeInMillis();
			}
		}catch(Exception e){
			log.error("��ȡ�������ʱ��-��",e);
		}
		return rt;
	}
	
	/**
	 * �������ʱ��----���������ʱ
	 * @param bUU
	 * @param bNN
	 * @param iUU
	 * @param iNN
	 * @param time
	 * @return
	 */
	private static long lastRuntimeH(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//����
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//��ֹʱ�̾����ľ��룬��λ��
			long dtime=((long)ctime.get(Calendar.HOUR_OF_DAY))*SECONDES_IN_HOUR+((long)ctime.get(Calendar.MINUTE))*SECONDES_IN_MINUTE;
			//ÿ����״�����ʱ�̾������룬��λ��
			long ftime=getUnitSecondes(bUU)*(long)bNN;
			//���м������λ��
			long interval=getUnitSecondes(iUU)*(long)iNN;
			if(ftime>dtime){//����δ���й�
				if(bUU.equalsIgnoreCase(TIME_UNIT_MINUTE)){
					ctime.set(Calendar.MINUTE, bNN);
					ctime.set(Calendar.HOUR_OF_DAY, 0);
					ctime.add(Calendar.SECOND, (int)interval);	//��һ����ʱ��
					rt=ctime.getTimeInMillis();
				}else if(bUU.equalsIgnoreCase(TIME_UNIT_HOUR)){
					ctime.set(Calendar.MINUTE, 0);
					ctime.set(Calendar.HOUR_OF_DAY, bNN);
					ctime.add(Calendar.SECOND, (int)interval);	//��һ����ʱ��
					rt=ctime.getTimeInMillis();
				}else{
					//�����ʱ������
					log.warn("�����ʱ������ bUU--"+bUU+" iUU--"+iUU);
				}				
			}else{
				long delt=(dtime-ftime)%interval;
				ctime.add(Calendar.SECOND, (int)delt);
				rt=ctime.getTimeInMillis();
			}
		}catch(Exception e){
			log.error("��ȡ�������ʱ��-ʱ",e);
		}
		return rt;
	}
	
	/**
	 * �������ʱ��----�����������
	 * @param bUU
	 * @param bNN
	 * @param iUU
	 * @param iNN
	 * @param time
	 * @return
	 */
	private static long lastRuntimeD(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//����
			ctime.set(Calendar.MINUTE, 0);
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//��ʼʱ��
			if(bUU.equalsIgnoreCase(TIME_UNIT_DAY)){
				//Ĭ����ʼʱ��Ϊ����bNN�գ���ʱ���
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				if(bNN>cday){
					//����δ���й�
					ctime.set(Calendar.DAY_OF_MONTH, bNN);
					ctime.add(Calendar.DAY_OF_MONTH, -iNN);
					rt=ctime.getTimeInMillis();
				}else{
					cday-=(cday-bNN)%iNN;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					rt=ctime.getTimeInMillis();
				}
			}else if(bUU.equalsIgnoreCase(TIME_UNIT_HOUR)){
				//Ĭ����ʼʱ��Ϊ����1�գ�bNNʱ���
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				int delt=(cday-1)%iNN;
				if(delt<=0){
					//��ֹ������������
					if(ctime.get(Calendar.HOUR_OF_DAY)<bNN){
						//����δ������ʱ��
						ctime.set(Calendar.HOUR_OF_DAY, bNN);
						ctime.add(Calendar.DAY_OF_MONTH, -iNN);
						rt=ctime.getTimeInMillis();
					}else{
						//�ѹ�����ʱ��
						ctime.set(Calendar.HOUR_OF_DAY, bNN);
						rt=ctime.getTimeInMillis();
					}
				}else{
					cday-=delt;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					ctime.set(Calendar.HOUR_OF_DAY, bNN);
					rt=ctime.getTimeInMillis();
				}
			}else if(bUU.equalsIgnoreCase(TIME_UNIT_MINUTE)){
				//Ĭ����ʼʱ��Ϊ����1�գ���ʱbNN��
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				int delt=(cday-1)%iNN;
				if(delt<=0){
					//��ֹ������������
					if(ctime.get(Calendar.HOUR_OF_DAY)==0 && ctime.get(Calendar.MINUTE)<bNN){
						//����δ������ʱ��
						ctime.set(Calendar.MINUTE, bNN);
						ctime.add(Calendar.DAY_OF_MONTH, -iNN);
						rt=ctime.getTimeInMillis();
					}else{
						//�ѹ�����ʱ��
						ctime.set(Calendar.HOUR_OF_DAY, 0);
						ctime.set(Calendar.MINUTE, bNN);
						rt=ctime.getTimeInMillis();
					}
				}else{
					cday-=delt;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					ctime.set(Calendar.HOUR_OF_DAY, 0);
					ctime.set(Calendar.MINUTE, bNN);
					rt=ctime.getTimeInMillis();
				}
			}else{
				//�����ʱ������
				log.warn("�����ʱ������ bUU--"+bUU+" iUU--"+iUU);
			}			
		}catch(Exception e){
			log.error("��ȡ�������ʱ��-��",e);
		}
		return rt;
	}
}
