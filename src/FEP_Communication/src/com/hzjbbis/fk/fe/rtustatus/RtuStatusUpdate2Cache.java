/**
 * 终端工况更新到数据库。
 * 具体更新执行的调度，由Spring配置Quartz来实现。
 */
package com.hzjbbis.fk.fe.rtustatus;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.fe.filecache.RtuStatusCache;
import com.hzjbbis.fk.model.RtuManage;

/**
 * @author bhw
 *
 */
public class RtuStatusUpdate2Cache {
	private static final Logger log = Logger.getLogger(RtuStatusUpdate2Cache.class);
	//更新到数据库的Dao配置
	/**
	 * 定期更新到本地文件。为了防止数据丢失，保存的频度较高。
	 */
	public void update2Cache(){
		log.info("定期更新终端状态信息到本地文件。");
		RtuStatusCache.save2File(RtuManage.getInstance().getAllComRtu());
	}
}
