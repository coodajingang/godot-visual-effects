package com.dus.pipeline.connectors.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL 连接池管理类
 * 基于 HikariCP 实现
 */
public class MysqlConnectionPool {
    
    private HikariDataSource dataSource;
    
    /**
     * 初始化连接池
     */
    public void init(String host, int port, String database, String username, String password,
                    int poolSize, long connectionTimeout) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(Math.min(5, poolSize));
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(config);
    }
    
    /**
     * 获取连接
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Connection pool not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * 关闭连接池
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * 检查连接池是否已关闭
     */
    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }
}
