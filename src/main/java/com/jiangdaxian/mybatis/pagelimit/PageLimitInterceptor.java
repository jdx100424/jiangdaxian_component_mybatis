package com.jiangdaxian.mybatis.pagelimit;


import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class,Integer.class }) })

public class PageLimitInterceptor implements Interceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PageLimitInterceptor.class);
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

	static int MAPPED_STATEMENT_INDEX = 0;
	static int PARAMETER_INDEX = 1;
	static int ROWBOUNDS_INDEX = 2;
	static int RESULT_HANDLER_INDEX = 3;

	static ExecutorService Pool;
	String dialectClass;
	boolean asyncTotalCount = false;

	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,
				DEFAULT_OBJECT_WRAPPER_FACTORY,DEFAULT_REFLECTOR_FACTORY);
		String originalSql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
		BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");	
		
		if (StringUtils.isNotBlank(originalSql)) {
			MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
			if(mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)==true){
				RowBounds rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");
				//保存原本的参数符信息,并增加LIMIT
				originalSql = getLimitSql(originalSql,rowBounds);
				metaStatementHandler.setValue("delegate.boundSql.sql", originalSql);	
				metaStatementHandler.setValue("delegate.boundSql",boundSql);
				return invocation.proceed();
			}else {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("非查询sql,无需分页，直接操作");
				}
				return invocation.proceed();
			}
		}else{
			throw new Exception("sql is not allow null");
		}
	}
	

	/**
	 * 增加LIMIT分页
	 * @param sql
	 * @return
	 */
	private String getLimitSql(String sql,RowBounds rowBounds) {
		//先把最后面的分号去掉
		while(true) {
			if(sql.lastIndexOf(";")>-1) {
				sql = sql.substring(0, sql.length()-1);
			}else {
				break;
			}
		}
		PageLimitBounds pageLimitBounds = null;
		if(rowBounds!=null && rowBounds instanceof PageLimitBounds) {
			pageLimitBounds = (PageLimitBounds) rowBounds;
		}
		StringBuilder newSql = new StringBuilder();
		newSql.append(sql);
		if(pageLimitBounds!=null) {
			int page = pageLimitBounds.getPage()<1?0:pageLimitBounds.getPage()-1;
			newSql.append(" limit ").append(page).append(",").append(pageLimitBounds.getLimit()).append(" ");
		}
		return newSql.toString();
	}

	@Override
	public Object plugin(Object target) {
		// 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的
		// 次数
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {

	}

}
