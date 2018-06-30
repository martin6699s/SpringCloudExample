package xxx.xxx.xxx;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
* 参考文章:
* https://github.com/FasterXML/jackson-modules-java8
* http://www.baeldung.com/spring-boot-jsoncomponent
* https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-customize-the-jackson-objectmapper
* https://stackoverflow.com/questions/46263773/jackson-parse-custom-offset-date-time
* https://blog.csdn.net/m0_38016299/article/details/78338048
* Formatting Java Time with Spring Boot using JSON: https://touk.pl/blog/2016/02/12/formatting-java-time-with-spring-boot-using-json/
* 使用自定义HttpMessageConverter对返回内容进行加密:https://www.scienjus.com/custom-http-message-converter/
*
 */
@Configuration
public class HttpMessageConverterConfig {

    @Bean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter
                = new MappingJackson2HttpMessageConverter();

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true);


        // 配置json属性映射对象属性 不需要使用在属性中加@JsonProperty
        objectMapper.registerModule(new ParameterNamesModule());

        // Some other custom configuration for supporting Java 8 features
        objectMapper.registerModule(new Jdk8Module());

        // 配置时间 LocalDateTime 与 OffsetDateTime 序列化反序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //设置序列化 反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
        javaTimeModule.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        javaTimeModule.addSerializer(OffsetDateTime.class, new CustomOffsetDateTimeSerializer());
        javaTimeModule.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());

        objectMapper.registerModule(javaTimeModule);

        // Use property 设置属性驼峰命名 不需要加下划线
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);

        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        //设置中文编码格式 中文乱码解决方案
        List<MediaType> list = new ArrayList<MediaType>();
        //设定Json格式且编码为utf-8
        list.add(MediaType.APPLICATION_JSON_UTF8);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
        return mappingJackson2HttpMessageConverter;
    }

    /**
     * OffsetDateTime反序列化
     */
    //@JsonComponent 如何加该注解 jackson自动帮使用该反序列化类进行反序列化化得到对象
    public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

        public CustomOffsetDateTimeDeserializer() {
        }

        @Override
        public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context)
                throws IOException, JsonProcessingException {

            // 解析结果：2018-06-30T15:03:17.227+08:00
            System.out.println("解析结果：" + parser.getText());

            return OffsetDateTime.parse(parser.getText(), DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    /**
     * OffsetDateTime序列化
     */
    public class CustomOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

        public CustomOffsetDateTimeSerializer() {
        }

        @Override
        public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            gen.writeString(value.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }
}
