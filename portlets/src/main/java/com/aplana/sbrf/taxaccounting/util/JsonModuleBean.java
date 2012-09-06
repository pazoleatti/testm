package com.aplana.sbrf.taxaccounting.util;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * Класс-обёртка над SimpleModule, чтобы можно было удобнее создавать экземпляры через spring-контекст
 */
public class JsonModuleBean extends SimpleModule {
	
	public JsonModuleBean(String name) {
		super(name, new Version(1, 0, 0, null));
	}
	
	public void setSerializers(List<JsonSerializer<?>> serializers) {
		for (JsonSerializer<?> s: serializers) {
			addSerializer(s);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setDeserializers(Map<Class, JsonDeserializer> deserializers) {
		for (Map.Entry<Class, JsonDeserializer> entry: deserializers.entrySet()) {
			addDeserializer(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Установка MixIn-классов
	 * @param mixins - Map, где ключ - название целевого класса, а значение - название MixIn-класса
	 * @throws ClassNotFoundException 
	 */
	public void setMixIns(Map<String, String> mixins) throws ClassNotFoundException {
		for (Map.Entry<String, String> entry: mixins.entrySet()) {
			this.setMixInAnnotation(Class.forName(entry.getKey()), Class.forName(entry.getValue()));
		}
	}
}
