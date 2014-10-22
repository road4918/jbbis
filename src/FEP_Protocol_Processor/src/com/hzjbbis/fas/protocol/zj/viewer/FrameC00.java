package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	FrameC00.java
 * @auther 		yangdh
 * @date		2006-8-2 15:06:42
 * @version		1.0
 * TODO
 */
public class FrameC00 extends AbstractFrame{
	public static final String FUNC_NAME="�м�";
	public FrameC00(){
		//
	}
	
	public FrameC00(byte[] frame){
		super(frame);
	}
	
	public FrameC00(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("��������--").append(FUNC_NAME);
			sb.append("\n");
			//sb.append("����--").append(Util.BytesToHex(frame,11,length));			
			if(direction>0){	//�ն�Ӧ��
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("�˿ں�--").append(Util.BytesToHex(frame,11,1));
			buffer.append("    ");
			buffer.append("��ʱʱ��--").append(frame[12] & 0xFF);
			buffer.append("    ");
			buffer.append("�����ֽ�--").append(Util.BytesToHex(frame,13,1));
			buffer.append("    ");
			int loc=(frame[14] & 0xFF) +((frame[15] & 0xFF)<<8);
			buffer.append("��ȡ��ʼ--").append(loc);
			buffer.append("    ");
			loc=(frame[16] & 0xFF) +((frame[17] & 0xFF)<<8);
			buffer.append("��ȡ����--").append(loc);
			buffer.append("    ");
			buffer.append("�м�����--").append(Util.BytesToHex(frame,18,length-7));
		}catch(Exception e){
			//
		}
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){
				buffer.append("�м�ʧ��--").append(errCode(frame[11]));
			}else{
				buffer.append("�˿ں�--").append(Util.BytesToHex(frame,11,1));			
				buffer.append("\n");			
				buffer.append("�м̷���--").append(Util.BytesToHex(frame,12,length-1));
			}			
		}catch(Exception e){
			//
		}
	}
}
