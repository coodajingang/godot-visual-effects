package com.dus.pipeline.util;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainer 支持类
 * 用于容器化测试的基础设施
 * 
 * @author Dus
 * @version 1.0
 */
public class TestContainerSupport {
    
    private static MySQLContainer<?> mysqlContainer;
    private static GenericContainer<?> elasticsearchContainer;
    
    /**
     * 启动 MySQL 容器
     * 
     * @return MySQL 连接字符串
     */
    public static String startMySQLContainer() {
        if (mysqlContainer == null) {
            mysqlContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true);
            
            mysqlContainer.start();
        }
        
        return mysqlContainer.getJdbcUrl();
    }
    
    /**
     * 启动 Elasticsearch 容器
     * 
     * @return Elasticsearch HTTP 地址
     */
    public static String startElasticsearchContainer() {
        if (elasticsearchContainer == null) {
            elasticsearchContainer = new GenericContainer<>(DockerImageName.parse("elasticsearch:7.17.0"))
                .withExposedPorts(9200)
                .withEnv("discovery.type", "single-node")
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withReuse(true);
            
            elasticsearchContainer.start();
        }
        
        return "http://" + elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200);
    }
    
    /**
     * 停止所有容器
     */
    public static void stopAllContainers() {
        if (mysqlContainer != null) {
            mysqlContainer.stop();
            mysqlContainer = null;
        }
        
        if (elasticsearchContainer != null) {
            elasticsearchContainer.stop();
            elasticsearchContainer = null;
        }
    }
    
    /**
     * 获取 MySQL 容器实例
     */
    public static MySQLContainer<?> getMySQLContainer() {
        return mysqlContainer;
    }
    
    /**
     * 获取 Elasticsearch 容器实例
     */
    public static GenericContainer<?> getElasticsearchContainer() {
        return elasticsearchContainer;
    }
    
    /**
     * 检查容器是否运行
     */
    public static boolean isMySQLRunning() {
        return mysqlContainer != null && mysqlContainer.isRunning();
    }
    
    /**
     * 检查 Elasticsearch 容器是否运行
     */
    public static boolean isElasticsearchRunning() {
        return elasticsearchContainer != null && elasticsearchContainer.isRunning();
    }
}