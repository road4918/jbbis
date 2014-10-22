/**
 * �ն�RTU�ĳ�ʼ�����ء��ն˵Ĳ����㡢�����������
 */
package com.hzjbbis.db.initrtu.dao;

import java.util.List;

import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuAlertCode;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.SysConfig;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskTemplate;

/**
 * @author bhw
 *
 */
public interface BizRtuDao {
	/**
	 * ����ҵ������ʹ�õ��ն��б���Щ�նˣ�ֻ���ػ������ԡ����ڼ��������ԣ�������أ�Ȼ����װ��
	 * @return
	 */
	List<BizRtu> loadBizRtu();
	
	/**
	 * �����ն������б�
	 * Use Case: ��ʼ��Service����loadRtuTask(),����BizRtu�����tasklist Ϊnull��
	 * 			����list����ӵ�list�С�
	 * @return
	 */
	List<RtuTask> loadRtuTasks();
	
	/**
	 * �����ն˵Ĳ������б�
	 * @return
	 */
	List<MeasuredPoint> loadMeasuredPoints();
	
	/**
	 * ����ȫ���澯���붨��
	 * @return
	 */
	List<RtuAlertCode> loadRtuAlertCodes();
	
	/**
	 * �������񱣴�����ݿ��������Ϣ��
	 * @return
	 */
	List<TaskDbConfig> loadTaskDbConfig();
	
	/**
	 * ��ʼ������ģ��
	 * @return
	 */
	List<TaskTemplate> loadTaskTemplate();
	/**
	 * ��ʼ�����ݿ��ϵͳ���ò���ֵ
	 * @return
	 */
	List<SysConfig> loadSysConfig();
}
