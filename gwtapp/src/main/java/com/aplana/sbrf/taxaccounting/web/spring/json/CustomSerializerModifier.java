package com.aplana.sbrf.taxaccounting.web.spring.json;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;

/**
 * Класс подменяющий стандартный серилизатор для предварительно обработки полей
 */
public class CustomSerializerModifier extends BeanSerializerModifier {

    private static final String THREE_DOTS = "...";

    @Override
    @SuppressWarnings("unchecked")
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return new ModifyingSerializer((JsonSerializer<Object>) serializer);
    }

    /**
     * Сериализатор, который модифицирует объект перед сериализацией
     */
    private static class ModifyingSerializer extends JsonSerializer<Object> implements ContextualSerializer {
        /**
         * Стандартный серилизатор
         */
        private final JsonSerializer<Object> defaultSerializer;

        public ModifyingSerializer(JsonSerializer<Object> serializer) {
            this.defaultSerializer = serializer;
        }

        @Override
        public void serialize(Object bean, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            JsonMixins.JsonMixin[] jsonMixins = JsonMixinThread.getMixins();

            try {
                if (jsonMixins != null) {
                    JsonMixins.JsonMixin jsonMixin = findMixin(jsonMixins, bean.getClass());

                    if (jsonMixin != null) {
                        for (JsonMixins.JsonAdditionParams additionParams : jsonMixin.additionParams()) {
                            Object propertyValue = PropertyUtils.getProperty(bean, additionParams.property());

                            // Для строк извлекаем максимальную длину и обрезаем по ней
                            if (propertyValue instanceof String) {
                                String propertyStringValue = (String) propertyValue;
                                int maxLength = additionParams.maxLength();

                                if (maxLength > 0 && propertyStringValue.length() > maxLength) {
                                    PropertyUtils.setProperty(bean, additionParams.property(), propertyStringValue.substring(0, maxLength) + THREE_DOTS);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("Mixin exception", e);
            }

            defaultSerializer.serialize(bean, jsonGenerator, serializerProvider);
        }

        /**
         * Находит среди mixIn-ов, тот, который описывает наш класс
         *
         * @param mixins список mixIn-ов у контроллера
         * @param clazz класс, для которого происходит поиск
         */
        private JsonMixins.JsonMixin findMixin(JsonMixins.JsonMixin[] mixins, Class clazz) {
            for (JsonMixins.JsonMixin jsonMixin : mixins) {
                if (jsonMixin.target().equals(clazz)) {
                    return jsonMixin;
                }
            }

            return null;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
            if (property != null) {
                JsonMixins.JsonMixin[] jsonMixins = JsonMixinThread.getMixins();

                if (jsonMixins != null) {
                    // Если приходит коллекция, то property.getType().getRawClass() возвращает класс коллекции, а тут нужен класс объекта
                    JsonMixins.JsonMixin jsonMixin = findMixin(jsonMixins, defaultSerializer.handledType());

                    if (jsonMixin != null && jsonMixin.additionParams().length > 0) {
                        return this;
                    }
                }

                return provider.handlePrimaryContextualization(defaultSerializer, property);
            }

            return provider.handlePrimaryContextualization(defaultSerializer, null);
        }
    }
}
