package com.hzjbbis.db.initrtu.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.hzjbbis.db.initrtu.dao.ComRtuDao;
import com.hzjbbis.db.resultmap.ResultMapper;
import com.hzjbbis.fk.model.ComRtu;
/**
 * @author bhw
 *
 */
public class JdbcComRtuDao implements ComRtuDao {
	//可配置属性
	private String sqlLoadRtu;
	private ResultMapper<ComRtu> mapperLoadRtu;
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public List<ComRtu> loadComRtu() {
		ParameterizedRowMapper<ComRtu> rowMap = new ParameterizedRowMapper<ComRtu>(){
			public ComRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtu, rowMap);
	}

	public void setSqlLoadRtu(String sqlLoadRtu) {
		this.sqlLoadRtu = sqlLoadRtu.trim();
	}
	
	public void setMapperLoadRtu(ResultMapper<ComRtu> resultMap) {
		this.mapperLoadRtu = resultMap;
	}

}
