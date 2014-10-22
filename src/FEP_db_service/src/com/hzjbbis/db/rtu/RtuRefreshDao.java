/**
 * �ն�ˢ�²������ݿ�ӿ�
 */
package com.hzjbbis.db.rtu;

import java.util.List;

import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.TaskTemplate;
import com.hzjbbis.fk.model.TaskTemplateItem;

/**
 * @author bhw
 *
 */
public interface RtuRefreshDao {
	/**
	 * �����ն˾ֺ����¼����ն˶���
	 * Use Case����վ����֪ͨǰ�û�ˢ���ն���Ϣ��
	 * @param zdjh
	 * @return
	 */
	BizRtu getRtu(String zdjh);
	
	/**
	 * �����ն�RTUA���¼����ն˶���
	 * Use Case: �������ʧ�ܣ�����RTUA���ض���
	 * @param rtua
	 * @return
	 */
	BizRtu getRtu(int rtua);
	
	/**
	 * �����ն˾ֺż��ظ��ն˵Ĳ�������Ϣ�б�
	 * @param zdjh���ն˾ֺ�
	 * @return
	 */
	List<MeasuredPoint> getMeasurePoints(String zdjh);
	
	/**
	 * ˢ���ն������б�
	 * @param zdjh�� �ն˾ֺš�
	 * @return
	 */
	List<RtuTask> getRtuTasks(String zdjh);
	
	/**
	 * ˢ������ģ����Ϣ��
	 * Use Case����վ�޸��ն�����ģ�壬���������¸�ģ����ն������֪ͨǰ�û�ˢ��ģ����Ϣ��
	 * @param templID
	 * @return
	 */
	TaskTemplate getTaskTemplate(String templID);
	
	/**
	 * ˢ��ĳ������ģ������в����
	 * Use Case: ��վˢ������ģ�����Ҫ���¸�ģ��Ĳ������б�
	 * @param templID
	 * @return
	 */
	List<TaskTemplateItem> getTaskTemplateItems(String templID);
	
	
}
