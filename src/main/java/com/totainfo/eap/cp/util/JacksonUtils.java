package com.totainfo.eap.cp.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author xiaobin.Guo
 * @date 2022年07月18日 17:45
 */
public class JacksonUtils {

     public final static ObjectMapper mapper = new ObjectMapper();
     // 日期格式化
     private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
     static {
         // 取消默认的timestamp转换
         mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
         // 忽略空bean转json的错误
         mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         // 设置日期格式
         mapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
         // 忽略json中存在,但java对象中不存在对应属性的情况
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

         mapper.setSerializationInclusion(Include.NON_NULL);

     }

    public  static ObjectNode getJson(){
        return mapper.createObjectNode();
    }

    public static JsonNode getJson(String json){
        JsonNode actualObj = null;
        try {
            actualObj = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            ;
        }
        return actualObj;
    }

    public static ObjectNode getJson2(String json){
        ObjectNode actualObj = null;
        try {
            actualObj = (ObjectNode) mapper.readTree(json);
        } catch (JsonProcessingException e) {
            ;
        }
        return actualObj;
    }

           /**
          * Object转字符串
          *
          * @param obj
          * @param <T>
          * @return
          */
    public static <T> String object2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

   /**
    * 转美化的 格式化的Json字符串
    *
    * @param obj
    * @return
    */
    public static <T> String object2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
         try {
             return obj instanceof String ? (String) obj : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
         } catch (JsonProcessingException e) {
             return null;
         }
    }
    /**
    * json转java对象
    *
    * @param json
    * @param clazz
    * @param <T>
    * @return
    */
    public static <T> T string2Object(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json) || clazz == null) {
                return null;
        }
        try {
             return String.class.equals(clazz) ? (T) json : mapper.readValue(json, clazz);
        } catch (IOException e) {
             return null;
        }
    }

    /**
    * json转集合类型
    *
    * @param json
    * @param collectionClazz
    * @param elementClazz
    * @return
    */
    public static <T> T string2Object(String json, Class<?> collectionClazz, Class<?> elementClazz) {
         if (!StringUtils.hasText(json) || collectionClazz == null || elementClazz == null) {
                 return null;
         }
         JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClazz, elementClazz);
         try {
             return mapper.readValue(json, javaType);
         } catch (IOException e) {
             return null;
         }
    }

    /**
    * json转java对象
    * 可以转集合类型,如 string2Object(json, new TypeReference<List<User>>() {})
    *
    * @param json
    * @param typeReference
    * @param <T>
    * @return
    */
     public static <T> T string2Object(String json, TypeReference<T> typeReference) {
         if (!StringUtils.hasText(json) || typeReference == null) {
             return null;
         }
         try {
             return typeReference.getType().equals(String.class) ? (T) json : mapper.readValue(json, typeReference);
         } catch (IOException e) {
             return null;
         }
    }
}
