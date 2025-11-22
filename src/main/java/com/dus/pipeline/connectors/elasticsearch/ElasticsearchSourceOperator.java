//package com.dus.pipeline.connectors.elasticsearch;
//
//import com.dus.pipeline.core.SourceOperator;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.hc.client5.http.auth.AuthScope;
//import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
//import org.apache.hc.client5.http.classic.HttpClient;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
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
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Elasticsearch 源算子
// * 从 Elasticsearch 索引读取数据
// */
//public class ElasticsearchSourceOperator extends SourceOperator<List<Map<String, Object>>> {
//
//    private final ElasticsearchSourceConfig config;
//    private final ObjectMapper objectMapper;
//    private HttpClient httpClient;
//    private String scrollId;
//    private boolean hasMore;
//
//    public ElasticsearchSourceOperator(ElasticsearchSourceConfig config) {
//        if (config == null) {
//            throw new IllegalArgumentException("Config cannot be null");
//        }
//        this.config = config;
//        this.objectMapper = new ObjectMapper();
//        this.scrollId = null;
//        this.hasMore = true;
//    }
//
//    @Override
//    protected void before() throws Exception {
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
//    }
//
//    @Override
//    protected List<Map<String, Object>> doNextBatch() throws Exception {
//        if (!hasMore) {
//            return null;
//        }
//
//        List<Map<String, Object>> data = new ArrayList<>();
//
//        if (scrollId == null) {
//            // 初始查询
//            String searchUrl = buildSearchUrl();
//            String response = executeGet(searchUrl);
//            JsonNode root = objectMapper.readTree(response);
//
//            scrollId = root.at("/_scroll_id").asText();
//            JsonNode hits = root.at("/hits/hits");
//
//            if (hits.isArray()) {
//                for (JsonNode hit : hits) {
//                    JsonNode source = hit.at("/_source");
//                    if (!source.isMissingNode()) {
//                        @SuppressWarnings("unchecked")
//                        Map<String, Object> doc = objectMapper.convertValue(source, Map.class);
//                        data.add(doc);
//                    }
//                }
//            }
//
//            if (data.size() < config.getPageSize()) {
//                hasMore = false;
//            }
//        } else {
//            // 使用 scroll API 继续获取
//            String scrollUrl = buildScrollUrl();
//            String response = executePost(scrollUrl, "{\"scroll\":\"" + config.getScrollTimeoutMs() + "ms\"}");
//            JsonNode root = objectMapper.readTree(response);
//
//            scrollId = root.at("/_scroll_id").asText();
//            JsonNode hits = root.at("/hits/hits");
//
//            if (hits.isArray()) {
//                for (JsonNode hit : hits) {
//                    JsonNode source = hit.at("/_source");
//                    if (!source.isMissingNode()) {
//                        @SuppressWarnings("unchecked")
//                        Map<String, Object> doc = objectMapper.convertValue(source, Map.class);
//                        data.add(doc);
//                    }
//                }
//            }
//
//            if (data.size() < config.getPageSize()) {
//                hasMore = false;
//            }
//        }
//
//        return data.isEmpty() ? null : data;
//    }
//
//    @Override
//    protected void after(List<Map<String, Object>> batch) throws Exception {
//        // 默认实现，不需要特殊处理
//    }
//
//    private String buildSearchUrl() {
//        String protocol = config.isSsl() ? "https" : "http";
//        String url = protocol + "://" + config.getHost() + ":" + config.getPort() + "/" + config.getIndex();
//        if (config.getType() != null && !config.getType().isEmpty()) {
//            url += "/" + config.getType();
//        }
//        url += "/_search?scroll=" + config.getScrollTimeoutMs() + "ms";
//        return url;
//    }
//
//    private String buildScrollUrl() {
//        String protocol = config.isSsl() ? "https" : "http";
//        return protocol + "://" + config.getHost() + ":" + config.getPort() + "/_search/scroll";
//    }
//
//    private String executeGet(String url) throws Exception {
//        HttpGet request = new HttpGet(url);
//        request.setHeader("Content-Type", "application/json");
//
//        return httpClient.execute(request, response -> {
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                byte[] bytes = entity.getContent().readAllBytes();
//                return new String(bytes, StandardCharsets.UTF_8);
//            }
//            return "";
//        });
//    }
//
//    private String executePost(String url, String body) throws Exception {
//        HttpPost request = new HttpPost(url);
//        request.setHeader("Content-Type", "application/json");
//        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
//
//        return httpClient.execute(request, response -> {
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                byte[] bytes = entity.getContent().readAllBytes();
//                return new String(bytes, StandardCharsets.UTF_8);
//            }
//            return "";
//        });
//    }
//
//    @Override
//    public String name() {
//        return "ElasticsearchSourceOperator";
//    }
//}
