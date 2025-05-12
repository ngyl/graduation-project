package com.animesocial.platform.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * ElasticSearch配置类
 * 配置ES连接和相关设置
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.animesocial.platform.repository.es")
public class ElasticsearchConfig extends ElasticsearchConfigurationSupport {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${elasticsearch.socket-timeout:60000}")
    private int socketTimeout;

    /**
     * 配置Elasticsearch客户端
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port))
                .setRequestConfigCallback(requestConfigBuilder -> 
                    requestConfigBuilder
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout))
                .build();
    }

    /**
     * 配置ElasticsearchTransport
     */
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    /**
     * 配置ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
    
    /**
     * 配置ElasticsearchCustomConversions
     * 自定义Elasticsearch和Java类型间的转换
     */
    @Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
            Arrays.asList(
                new LocalDateTimeToStringConverter(),
                new StringToLocalDateTimeConverter()
            )
        );
    }

    /**
     * 自定义序列化：LocalDateTime → String "yyyy-MM-dd'T'HH:mm:ss"
     * 将Java LocalDateTime转换为Elasticsearch可识别的ISO日期时间格式
     */
    @WritingConverter
    static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime source) {
            if (source == null) {
                return null;
            }
            return source.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
    }

    /**
     * 自定义反序列化：String → LocalDateTime
     * 支持多种日期格式的灵活转换
     */
    @ReadingConverter
    static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        private static final Logger logger = LoggerFactory.getLogger(StringToLocalDateTimeConverter.class);
        
        // 支持多种日期格式
        private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            // ISO标准格式
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            // 自定义格式
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
        
        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            // 去除字符串两端的空白
            String trimmedSource = source.trim();
            
            // 特殊处理：如果是纯日期，自动添加时间部分
            if (trimmedSource.matches("\\d{4}-\\d{2}-\\d{2}")) {
                trimmedSource = trimmedSource + "T00:00:00";
            }
            
            // 尝试所有支持的日期格式
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    // 对于纯日期格式，需要转换为LocalDateTime
                    if (trimmedSource.length() <= 10) {
                        return LocalDate.parse(trimmedSource, formatter).atStartOfDay();
                    }
                    
                    // 普通日期时间格式
                    return LocalDateTime.parse(trimmedSource, formatter);
                } catch (DateTimeParseException e) {
                    // 尝试下一个格式
                    continue;
                }
            }
            
            // 尝试处理带时区的日期时间
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(trimmedSource);
                return zonedDateTime.toLocalDateTime();
            } catch (DateTimeParseException e) {
                // 忽略错误，继续后续处理
            }
            
            // 所有格式都解析失败，记录警告并返回null
            logger.warn("无法将字符串 [{}] 转换为LocalDateTime，尝试了所有支持的格式", source);
            return null;
        }
    }
} 