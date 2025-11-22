//package com.dus.pipeline.connectors.elasticsearch;
//
//import com.dus.pipeline.core.SinkOperator;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.hc.client5.http.auth.AuthScope;
//import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
//import org.apache.hc.client5.http.classic.HttpClient;
//import org.apache.hc.client5.http.classic.methods.HttpPost;
//import org.apache.hc.client5.http.config.RequestConfig;
//import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.HttpEntity;
//import org.apache.hc.core5.http.io.entity.StringEntity;
//import org.apache.hc.core5.util.TimeValue;
//import org.apache.hc.core5.util.Timeout;
//
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
///**
// * Elasticsearch 汇算子
// * 将数据写入 Elasticsearch
// */
//public class ElasticsearchSinkOperator extends SinkOperator<List<Map<String, Object>>> {
//
//    private final ElasticsearchSinkConfig config;
//    private final ObjectMapper objectMapper;
//    private HttpClient httpClient;
//    private List<Map<String, Object>> batch;
//
//    public ElasticsearchSinkOperator(ElasticsearchSinkConfig config) {
//        if (config == null) {
//            throw new IllegalArgumentException("Config cannot be null");
//        }
//        this.config = config;
//        this.objectMapper = new ObjectMapper();
//        this.batch = new ArrayList<>();
//    }
//
//    @Override
//    protected void before(List<Map<String, Object>> input) throws Exception {
//        RequestConfig requestConfig = RequestConfig.custom()
//            .setConnectTimeout(Timeout.ofSeconds(30))
//            .setResponseTimeout(Timeout.ofSeconds(30))
//            .build();
//
//        if (config.getUsername() != null && config.getPassword() != null) {
//            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//            credentialsProvider.setCredentials(
//                new AuthScope(config.getHost(), config.getPort()),
//                new UsernamePasswordCredentials(config.getUsername(), config.getPassword().toCharArray())
//            );
//
//            this.httpClient = HttpClients.custom()
//                .setDefaultRequestConfig(requestConfig)
//                .setDefaultCredentialsProvider(credentialsProvider)
//                .build();
//        } else {
//            this.httpClient = HttpClients.custom()
//                .setDefaultRequestConfig(requestConfig)
//                .build();
//        }
//
//        this.batch = new ArrayList<>();
//    }
//
//    @Override
//    protected void write(List<Map<String, Object>> input) throws Exception {
//        if (input == null || input.isEmpty()) {
//            return;
//        }
//
//        for (Map<String, Object> item : input) {
//            batch.add(item);
//            if (batch.size() >= config.getBatchSize()) {
//                flushBatch();
//            }
//        }
//    }
//
//    @Override
//    protected void after(List<Map<String, Object>> input, Void output) throws Exception {
//        if (!batch.isEmpty()) {
//            flushBatch();
//        }
//        if (httpClient != null) {
//            //httpClient.close();
//        }
//    }
//
//    private void flushBatch() throws Exception {
//        if (batch.isEmpty()) {
//            return;
//        }
//
//        String bulkUrl = buildBulkUrl();
//        String bulkBody = buildBulkBody();
//
//        int retries = 0;
//        while (retries <= config.getMaxRetries()) {
//            try {
//                HttpPost request = new HttpPost(bulkUrl);
//                request.setHeader("Content-Type", "application/json");
//                request.setEntity(new StringEntity(bulkBody, StandardCharsets.UTF_8));
//
//                int statusCode = httpClient.execute(request, response -> {
//                    // Consume response
//                    HttpEntity entity = response.getEntity();
//                    if (entity != null) {
//                        entity.getContent().readAllBytes();
//                    }
//                    return response.getCode();
//                });
//
//                if (statusCode >= 200 && statusCode < 300) {
//                    batch.clear();
//                    return;
//                } else {
//                    throw new RuntimeException("HTTP " + statusCode);
//                }
//            } catch (Exception e) {
//                retries++;
//                if (retries <= config.getMaxRetries()) {
//                    long backoffTime = (long) Math.pow(2, retries - 1) * 1000;
//                    Thread.sleep(backoffTime);
//                } else {
//                    throw e;
//                }
//            }
//        }
//    }
//
//    private String buildBulkUrl() {
//        String protocol = config.isSsl() ? "https" : "http";
//        return protocol + "://" + config.getHost() + ":" + config.getPort() + "/_bulk";
//    }
//
//    private String buildBulkBody() throws Exception {
//        StringBuilder sb = new StringBuilder();
//        String indexName = formatIndexName(config.getIndex());
//
//        for (Map<String, Object> doc : batch) {
//            // Metadata line
//            String metadata;
//            if (config.getWriteMode() == WriteMode.DELETE) {
//                metadata = "{\"delete\":{\"_index\":\"" + indexName + "\"}}";
//            } else if (config.getWriteMode() == WriteMode.UPDATE) {
//                metadata = "{\"update\":{\"_index\":\"" + indexName + "\"}}";
//            } else {
//                metadata = "{\"index\":{\"_index\":\"" + indexName + "\"}}";
//            }
//            sb.append(metadata).append("\n");
//
//            // Document body
//            if (config.getWriteMode() == WriteMode.UPDATE) {
//                sb.append("{\"doc\":").append(objectMapper.writeValueAsString(doc)).append("}\n");
//            } else if (config.getWriteMode() != WriteMode.DELETE) {
//                sb.append(objectMapper.writeValueAsString(doc)).append("\n");
//            }
//        }
//
//        return sb.toString();
//    }
//
//    private String formatIndexName(String indexPattern) {
//        if (indexPattern == null) {
//            return "index";
//        }
//
//        // Support for date formatting like {yyyy-MM-dd}
//        if (indexPattern.contains("{")) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String today = sdf.format(new Date());
//            return indexPattern.replace("{yyyy-MM-dd}", today);
//        }
//
//        return indexPattern;
//    }
//
//    @Override
//    public String name() {
//        return "ElasticsearchSinkOperator";
//    }
//}
