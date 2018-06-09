package com.aplana.sbrf.taxaccounting.web.spring.json;

import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * {@code HttpMessageConverter} that can write JSON using {@link JsonMixins.JsonMixin} filters.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class JsonFilterHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    @Autowired
    CommonRefBookService commonRefBookService;
    @Autowired
    HttpServletRequest request;

    public JsonFilterHttpMessageConverter() {
        super();
        ObjectMapper objectMapper = getObjectMapper();

        // Модули:
        // 1. Ленивые поля, которые не проинициализированы, не будут выгружаться
        objectMapper.registerModules(new SimpleModule().setSerializerModifier(new CustomSerializerModifier()));
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
        ObjectMapper objectMapper = getObjectMapper();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator =
                objectMapper.getFactory().createGenerator(outputStream, encoding);
        // A workaround for JsonGenerators not applying serialization features
        // https://github.com/FasterXML/jackson-databind/issues/12
        if (objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
            jsonGenerator.useDefaultPrettyPrinter();
        }

        // Берем все описания маппингов из аннотаций и применяем их к указанным классам
        JsonMixins.JsonMixin[] mixins = JsonMixinThread.getMixins();
        if (ArrayUtils.isNotEmpty(mixins)) {
            objectMapper = prepareObjectMapper(mixins);
        }

        try {
            objectMapper.writeValue(jsonGenerator, object);
            outputMessage.getBody().write(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    private ObjectMapper prepareObjectMapper(JsonMixins.JsonMixin[] mixins) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        for (JsonMixins.JsonMixin mixin : mixins) {
            // Берем описания фильтров из аннотаций и передаем их objectMapper'у
            addFilterDescriptions(filterProvider, mixin);
            objectMapper.addMixIn(mixin.target(), mixin.mixinSource());
        }
        objectMapper.setFilterProvider(filterProvider);

        // Модули:
        // 1. Ленивые поля, которые не проинициализированы, не будут выгружаться
        objectMapper.registerModules(new SimpleModule().setSerializerModifier(new CustomSerializerModifier()));

        return objectMapper;
    }

    private void addFilterDescriptions(SimpleFilterProvider filterProvider, JsonMixins.JsonMixin mixin) {
        JsonFilterDescription filterDescription = mixin.mixinSource().getAnnotation(JsonFilterDescription.class);
        if (filterDescription != null) {
            // Объединяем содержимое поля fields() и предустановленный фильтр из filter()
            filterProvider.addFilter(filterDescription.name(),
                    SimpleBeanPropertyFilter.filterOutAllExcept(
                            ArrayUtils.addAll(filterDescription.fields(), filterDescription.filter().getFields())));
        }
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) {

        ObjectMapper objectMapper = getObjectMapper();

        // Берем все описания маппингов из аннотаций и применяем их к указанным классам
        JsonMixins.JsonMixin[] mixins = JsonMixinThread.getMixins();
        if (ArrayUtils.isNotEmpty(mixins)) {
            objectMapper = prepareObjectMapper(mixins);
        }

        // Отключаем попытки десериализации неизвестных полей
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String path = request.getRequestURL().toString();

            /*if (javaType.hasRawClass(RefBookSimple.class)) {

                Integer id = Integer.parseInt(path.substring(path.indexOf("referenceValues") + "referenceValues".length() + 1));
                String jsonBody = IOUtils.toString(inputMessage.getBody(), "UTF-8");

                Reference reference = referenceService.findOne(id);

                return objectMapper.readValue(jsonBody, reference.getJavaClass());
            }*/
//            } else if (javaType.hasRawClass(CachableReference.class)
//                    && path.contains("/rest/checkActual")) {
//
//                Integer id = Integer.parseInt(query.substring(query.indexOf("referenceId") + "referenceId".length() + 1));
//                String jsonBody = IOUtils.toString(inputMessage.getBody(), "UTF-8");
//
//                Reference reference = referenceService.findOne(id);
//
//                return objectMapper.readValue(jsonBody, reference.getJavaClass());
//            } else {
//                return objectMapper.readValue(inputMessage.getBody(), javaType);
//            }

            return objectMapper.readValue(inputMessage.getBody(), javaType);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }
}
