package com.google.gwt.user.client.rpc.core.java.util;

import java.util.Date;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Этот класс реализует сериализацию {@link java.util.Date} со сдвигом временных зон.
 */
public final class Date_CustomFieldSerializer extends
		CustomFieldSerializer<Date> {

	/**
	 * Сброс/восстановление UTC
	 * 
	 * @param date
	 * @param plus
	 * 
	 * @return
	 */
	private static long offsetUTC(long time, boolean plus) {
		// * 
		// Получение текущего сдвига. Нужно будет придумать другой способ,
		// без вызова deprecated методов
		Date getOffsetDate = new Date();
		long offset = getOffsetDate.getTimezoneOffset() * 60000;
		// *
		long result = time + (plus ? offset : -offset);
		return result;
	}

	public static void deserialize(SerializationStreamReader streamReader,
			Date instance) {
		// No fields
	}

	public static Date instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		long time = streamReader.readLong();
		Date date = new Date(offsetUTC(time, true));
		return date;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			Date instance) throws SerializationException {
		streamWriter.writeLong(offsetUTC(instance.getTime(), false));
	}

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader,
			Date instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}

	@Override
	public Date instantiateInstance(SerializationStreamReader streamReader)
			throws SerializationException {
		return instantiate(streamReader);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter,
			Date instance) throws SerializationException {
		serialize(streamWriter, instance);
	}
}
