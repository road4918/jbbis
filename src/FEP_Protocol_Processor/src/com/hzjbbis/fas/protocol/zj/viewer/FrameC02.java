package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	FrameC02.java
 * @auther 		yangdh
 * @date		2006-8-2 15:08:02
 * @version		1.0
 * TODO
 */
public class FrameC02 extends AbstractFrame{
	public static final String FUNC_NAME="�ն���������";
	public FrameC02(){
		//
	}
	
	public FrameC02(byte[] frame){
		super(frame);
	}
	
	public FrameC02(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("��������--").append(FUNC_NAME);
			sb.append("\n");			
			if(direction>0){	//�ն�Ӧ��
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){				
				buffer.append("�쳣Ӧ��--").append(errCode(frame[11]));				
			}else{				
				buffer.append("�����--")	.append(frame[11] & 0xFF).append("    ");
				buffer.append("��ʼ�������ݲɼ�ʱ��---");
				buffer.append("20").append(Util.ByteToHex(frame[12])).append("-").append(Util.ByteToHex(frame[13]))
						.append("-").append(Util.ByteToHex(frame[14])).append(" ").append(Util.ByteToHex(frame[15]))
						.append(":").append(Util.ByteToHex(frame[16])).append(":00\n");
				buffer.append("�����������ݵ���--").append(frame[17] & 0xFF).append("    ");
				buffer.append("���ݵ�ɼ����ʱ��--").append(frame[19] & 0xFF).append(timeUnit(frame[18])).append("\n");
				buffer.append("��������--").append(Util.BytesToHex(frame,20,length-9));
			}
		}catch(Exception e){
			//
		}
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("�����--")	.append(frame[11] & 0xFF).append("    ");
			buffer.append("��ʼ�������ݲɼ�ʱ��---");
			buffer.append("20").append(Util.ByteToHex(frame[12])).append("-").append(Util.ByteToHex(frame[13]))
					.append("-").append(Util.ByteToHex(frame[14])).append(" ").append(Util.ByteToHex(frame[15]))
					.append(":").append(Util.ByteToHex(frame[16])).append(":00\n");
			buffer.append("�����ٲ����ݵ���--").append(frame[17] & 0xFF).append("    ");
			buffer.append("���ݵ�������--").append(frame[18] & 0xFF).append("\n");
		}catch(Exception e){
			//
		}
	}
}
