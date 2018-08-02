package com.jiangdaxian.mybatis.masterandslave;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * JDX MYBATIS一主一从选择
 * @author jdx100424
 *
 */
public class MasterAndSlaveDataSource extends AbstractRoutingDataSource {
	protected Object determineCurrentLookupKey() {
		Object result =  MasterAndSlaveDataSourceHolder.getDataSource();
		if(result == null) {
			result = MasterAndSlaveDataSourceHolder.MASTER_DATASOURCE;
		}
		return result;
	}
}
