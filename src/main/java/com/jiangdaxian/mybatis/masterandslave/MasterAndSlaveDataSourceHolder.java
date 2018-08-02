package com.jiangdaxian.mybatis.masterandslave;

public class MasterAndSlaveDataSourceHolder {
	public static final ThreadLocal<String> holder = new ThreadLocal<String>();
	public static final String MASTER_DATASOURCE = "master";
	public static final String SLAVE_DATASOURCE = "slave";

	public static void removeDataSource() {
		holder.set(null);
	}
	
	public static String getDataSource() {
		String result = holder.get();
		return result;
	}
	public static void putDataSourceMaster() {
		putDataSource(MASTER_DATASOURCE);
	}
	public static void putDataSourceSlave() {
		putDataSource(SLAVE_DATASOURCE);
	}
	public static void putDataSource(String dataSourceName) {
		holder.set(dataSourceName);
	}
}
