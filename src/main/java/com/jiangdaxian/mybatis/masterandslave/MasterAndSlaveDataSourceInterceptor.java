package com.jiangdaxian.mybatis.masterandslave;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }) })
public class MasterAndSlaveDataSourceInterceptor implements Interceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MasterAndSlaveDataSourceInterceptor.class);

	public Object intercept(Invocation invocation) throws Throwable {
		try {
			Object[] objects = invocation.getArgs();
			MappedStatement ms = (MappedStatement) objects[0];
			// 有强制指定了，直接设置
			if (StringUtils.isNotBlank(MasterAndSlaveDataSourceHolder.getDataSource())) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("is use force,value is {}",MasterAndSlaveDataSourceHolder.getDataSource());
				}
				return invocation.proceed();
			} 
			// 没有强制指定库的，根据SQL语句处理
			if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
				MasterAndSlaveDataSourceHolder.putDataSourceSlave();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("is use slave by auto");
				}
			} else {
				MasterAndSlaveDataSourceHolder.putDataSourceMaster();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("is use master by auto");
				}
			}
			//默认空的处理，直接master
			if (StringUtils.isBlank(MasterAndSlaveDataSourceHolder.getDataSource())) {
				MasterAndSlaveDataSourceHolder.putDataSourceMaster();
			}
			return invocation.proceed();
		}finally {
			MasterAndSlaveDataSourceHolder.removeDataSource();
		}
	}

	public Object plugin(Object arg0) {
		return Plugin.wrap(arg0, this);
	}

	@Override
	public void setProperties(Properties arg0) {
		// TODO Auto-generated method stub
		
	}
}
