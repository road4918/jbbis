/**
 * �㽭�������ع�Լ��Ϣͷ���塣
 */
package com.hzjbbis.fk.message.zj;

import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * 2008-06-02 21:26
 */
public class MessageZjHead {
	public byte flag1; //֡��ʼ�� 68h

	public byte rtua_a1; //������

	public byte rtua_a2; //������

	public short rtua_b1b2; //�ն˵�ַ

	public int rtua; //�ն��߼���ַ

	public byte msta; //��վ��ַ

	public byte fseq; //֡���

	public byte iseq; //֡�����

	public byte flag2; //֡��ʼ�� 68h

	public byte c_dir; //�������еĴ��ͷ���

	public byte c_expflag; //�쳣��־

	public byte c_func; //������

	public short dlen; //���ݳ���

	//��������...

	public byte cs; //У����

	public byte flag3; //������ 16h

	public MessageZjHead(){
		flag1 = 0x68;
		flag2 = 0x68;
		flag3 = 0x16;
		iseq = 0;
		c_dir = 0; //��վ�·�
		c_expflag = 0;
		c_func = 0;
		dlen = 0;
		cs = 0;
		fseq = 0;
		msta = 1;
		rtua = 0;
		rtua_a1 = rtua_a2 = 0;
		rtua_b1b2 = 0;
	}
	
	public void parseRtua() {
		if (rtua != 0) {
			return;
		} else {
			rtua |= (0xFF & rtua_a1)<< 24;
			rtua |= (0xFF & rtua_a2)<< 16;
			rtua |= (0xFF & rtua_b1b2)<<8;
			rtua |= (0xFF & (rtua_b1b2>>8));
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append("[rtua=").append(HexDump.toHex(rtua));
		//sb.append(",rtua_a1=").append(HexDump.toHex(rtua_a1));
		//sb.append(",rtua_a2=").append(HexDump.toHex(rtua_a2));
		//sb.append(",rtua_b1b2=").append(HexDump.toHex(rtua_b1b2));
		sb.append(",msta=").append(HexDump.toHex(msta));
		sb.append(",fseq=").append(HexDump.toHex(fseq));
		sb.append(",iseq=").append(HexDump.toHex(iseq));
		//sb.append(",flag2=").append(HexDump.toHex(flag2));
		sb.append(",c_dir=").append(HexDump.toHex(c_dir));
		sb.append(",c_expflag=").append(HexDump.toHex(c_expflag));
		sb.append(",c_func=").append(HexDump.toHex(c_func));
		sb.append(",datalen=").append(HexDump.toHex(dlen));
		sb.append("]");
		return sb.toString();
	}
}
