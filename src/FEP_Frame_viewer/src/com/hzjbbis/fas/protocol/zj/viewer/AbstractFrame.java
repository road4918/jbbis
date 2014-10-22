package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	AbstractFrame.java
 * @auther 		yangdh
 * @date		2006-8-1 15:17:38
 * @version		1.0
 * TODO
 * 				0x68--------------------��һ֡ͷ��־
 * 				  A1--------------------��������
 * 				  A2--------------------��������	
 * 				  B1--------------------�ն˵�ַ
 * 				  B2--------------------�ն˵�ַ
 * 				 MS1--------------------��վ��ַ���������
 * 				 MS2--------------------��վ��ַ���������
 * 				0x68--------------------�ڶ�֡ͷ��־
 * 				   C--------------------������
 * 				   L--------------------֡��(���ֽ�)
 * 				   L--------------------֡��(���ֽ�)	
 * 				DATA--------------------����N�ֽ�
 * 				  CS--------------------У��(CS֮ǰ�����ֽ�У���)
 * 				0x16--------------------֡β��־ 
 * 
 * 
 */
public abstract class AbstractFrame {
	protected byte A1;		/*��������*/
	protected byte A2;
	protected byte B1;		/*�ն˵�ַ*/
	protected byte B2;
	protected int mast;		/*��վ���*/
	protected int fseq;		/*֡���*/
	protected int ifseq;	/*֡�����*/
	protected int C;		/*������*/
	protected int fexp;		/*�쳣��־ 1:�쳣 0������*/
	protected int direction;/*���䷽�� 1���ն����� 0����վ����*/
	protected int length;	/*���ݳ���*/
	protected byte cs;
	protected byte rcs;
	protected byte[] frame;	/*֡*/
	
	public AbstractFrame(){
		//
	}
	
	public AbstractFrame(byte[] frame){		
		if(frame!=null && frame.length>=13){
			this.frame=frame;
			fillinfo();
		}
	}
	
	public AbstractFrame(String data){
		if(data!=null){
			if(Util.validHex(data)){//�Ϸ���HEX�ִ�
				frame=null;
				frame=new byte[(data.length()>>>1)+(data.length() & 0x1)];
				Util.HexsToBytes(frame,0,data);
				fillinfo();
			}
		}
	}
	
	private void fillinfo(){
		A1=frame[1];
		A2=frame[2];
		B1=frame[3];
		B2=frame[4];
		mast=frame[5] & 0x3f;
		fseq=((frame[6] & 0x1f)<<2)+((frame[5] & 0xC0)>>>6);
		ifseq=((frame[6] & 0xE0)>>>5);
		C=frame[8] & 0x3f;
		fexp=(frame[8] & 0x40)==0x40?1:0;
		direction=(frame[8] & 0x80)==0x80?1:0;
		length=((frame[10] & 0xff)<<8)+(frame[9] & 0xff);
		cs=frame[frame.length-2];
		rcs=Util.calculateCS(frame,0,frame.length-2);
	}
	
	protected String getBase(){
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(direction>0?"���䷽��--�ն�����":"���䷽��--��վ����");
			sb.append("    ");
			sb.append("�ն��߼���ַ--").append(Util.ByteToHex(A1)).append(Util.ByteToHex(A2)).append(Util.ByteToHex(B2)).append(Util.ByteToHex(B1));
			//sb.append("    ");
			//sb.append("�ն˵�ַ--").append(Util.ByteToHex(B2)).append(Util.ByteToHex(B1));
			sb.append("    ");
			sb.append("��վ��ַ--").append(String.valueOf(mast));
			sb.append("    ");
			sb.append("֡���--").append(String.valueOf(fseq));
			sb.append("    ");
			sb.append("֡�����--").append(String.valueOf(ifseq));
			sb.append("    ");
			sb.append("���ݳ���--").append(String.valueOf(length));
			sb.append("\n");
			sb.append("CS--").append(Util.ByteToHex(cs));
			sb.append("    DATA CS--").append(Util.ByteToHex(rcs));
			sb.append("\n");
			return sb.toString();
		}
		return null;
	}
	
	public String errCode(byte errcode){
		String err=null;
		switch(errcode & 0xFF){
			case 0x00:
				err="�ɹ�";
				break;
			case 0x01:
				err="�м������޷���";
				break;
			case 0x02:
				err="�������ݷǷ�";
				break;
			case 0x03:
				err="����Ȩ�޲���";
				break;
			case 0x04:
				err="�޴�������";
				break;
			case 0x05:
				err="����ʱ��ʧЧ";
				break;
			case 0x11:
				err="Ŀ���ַ������";
				break;
			case 0x12:
				err="����ʧ��";
				break;
			case 0x13:
				err="����Ϣ̫֡��";
				break;
			default:
				break;
		}
		return err;
	}
	
	public String timeUnit(byte tunit){
		String ustr=null;
		switch(tunit & 0xFF){
			case 0x2:
				ustr="����";
				break;
			case 0x3:
				ustr="Сʱ";
				break;
			case 0x4:
				ustr="��";
				break;
			case 0x5:
				ustr="��";
				break;
			default:
				ustr="δ֪ʱ�䵥λ";
				break;
		}
		return ustr;
	}
	
	public abstract String getDescription();
}
