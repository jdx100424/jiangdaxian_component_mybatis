package com.jiangdaxian.mybatis.pagelimit;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangdaxian.mybatis.masterandslave.MasterAndSlaveDataSourceHolder;
import com.jiangdaxian.mybatis.masterandslave.MasterAndSlaveDataSourceInterceptor;

@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
		RowBounds.class, ResultHandler.class }) })
public class PageCountInterceptor implements Interceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PageCountInterceptor.class);

	public Object intercept(Invocation invocation) throws Throwable {
		try {
			final Object[] queryArgs = invocation.getArgs();
			final MappedStatement ms = (MappedStatement) queryArgs[0];
			final Object parameter = queryArgs[1];
			final RowBounds rowBounds = (RowBounds) queryArgs[2];
			
			boolean isSelectTotalCount =false;
			PageLimitBounds pageLimitBounds = null;
			if(rowBounds!=null && rowBounds instanceof PageLimitBounds) {
				pageLimitBounds = (PageLimitBounds) rowBounds;
				if(pageLimitBounds.isContainsTotalCount()) {
					isSelectTotalCount = true;
				}
			}
			if (isSelectTotalCount) {
				final BoundSql boundSql = ms.getBoundSql(parameter);
				final String countSql = boundSql.getSql();
				Integer count = getCountSql(countSql, ms,boundSql);
				Object resultObject =  invocation.proceed();
				if(resultObject!=null && resultObject instanceof List<?>) {
					List<?> resultList = (List<?>) resultObject;
					PageList pageList = new PageList();
					pageList.addAll(resultList);
					pageList.setPage(pageLimitBounds.getPage());
					pageList.setLimit(pageLimitBounds.getLimit());
					pageList.setTotalCount(count);
					return pageList;
				}else {
					return resultObject;
				}
			}else {
				return invocation.proceed();
			}
		}catch(Exception e) {
			LOGGER.error(e.getMessage(),e);
			throw e;
		}finally {
			
		}
	}
	
	private Integer getCountSql(String originalSql,MappedStatement mappedStatement,BoundSql boundSql) throws Exception {
		//先把最后面的分号去掉
		while(true) {
			if(originalSql.lastIndexOf(";")>-1) {
				originalSql = originalSql.substring(0, originalSql.length()-1);
			}else {
				break;
			}
		}
		Connection connection = null;
		PreparedStatement countStmt = null;
		ResultSet rs = null;
		try {
			String countSql = new StringBuilder().append("select count(1) from (").append(originalSql).append(") a").toString();
			connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
			countStmt = connection.prepareStatement(countSql);
			DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
			handler.setParameters(countStmt);
			rs = countStmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			return count;
		}catch(Exception e) { 
			LOGGER.error(e.getMessage(),e);
			throw e;
		}finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} finally {
				try {
					if (countStmt != null) {
						countStmt.close();
					}
				} finally {
					if (connection != null && !connection.isClosed()) {
						connection.close();
					}
				}
			}
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
