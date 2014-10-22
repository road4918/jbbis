/**
 * Balanced factor for messages dispatch to all BP processors.
 * 分配算法：
 * 按照地市进行分配。DistrictFactor代表一个地市，含权重。
 */
package com.hzjbbis.fk.fe.msgqueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;

/**
 * @author bhw
 *
 */
public class BpBalanceFactor {
	private static final BpBalanceFactor instance = new BpBalanceFactor();
	public static final BpBalanceFactor getInstance(){
		return instance;
	}
	private final Map<Byte,DistrictFactor> factors = new HashMap<Byte,DistrictFactor>();
	
	private BpBalanceFactor(){}
	
	public void travelRtus(){
		travelRtus(RtuManage.getInstance().getAllComRtu());
	}
	
	public void travelRtus(Collection<ComRtu> rtus){
		byte districtCode;
		DistrictFactor factor;
		factors.clear();
		for(ComRtu rtu: rtus ){
			districtCode =  (byte)(rtu.getRtua() >>> 24) ;
			factor = factors.get(districtCode);
			if( null == factor ){
				factor = new DistrictFactor();
				factor.districtCode = (byte)districtCode;
				factors.put(factor.districtCode, factor);
			}
			factor.rtuCount++;
		}
	}
	
	public Collection<DistrictFactor> getAllDistricts(){
		List<DistrictFactor> facts = new ArrayList<DistrictFactor>(factors.values());
		List<DistrictFactor> result = new ArrayList<DistrictFactor>();
		while( facts.size()>0 ){
			result.add( removeMaxFactor(facts) );
		}
		return result;
	}
	
	private DistrictFactor removeMaxFactor(List<DistrictFactor> facts){
		DistrictFactor max=null;
		int pos = -1;
		for(int i=0; i<facts.size(); i++ ){
			if( null == max || max.rtuCount < facts.get(i).rtuCount){
				max = facts.get(i);
				pos = i;
			}
		}
		if( pos>=0 )
			facts.remove(pos);
		return max;
	}
	
	/**
	 * 地市终端的因子
	 * @author bhw
	 *
	 */
	class DistrictFactor {
		public byte districtCode = 0;
		public int rtuCount = 0;
	}
}
