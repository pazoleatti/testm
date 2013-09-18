package com.google.gwt.user.client.rpc.core.java.util;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Date;

/**
 * Custom field serializer for {@link java.util.Date}.
 */
//TODO Убрать трейсы
public final class Date_CustomFieldSerializer extends
		CustomFieldSerializer<Date> {

	/**
	 * Сброс/восстановление UTC
	 * 
	 * @param date
	 * @param plus
	 *            true - преобразовать дату в UTC0 false - преобразовать дату из
	 *            UTC0 в текущую
	 * 
	 * @return
	 */
	private static long offsetUTC(Date date, boolean plus) {
		System.out.println(plus);
		System.out.println(date);
		@SuppressWarnings("deprecation")
		long offset = date.getTimezoneOffset() * 60000;
		System.out.println(offset);
		long result = date.getTime() + (plus ? offset : -offset);
		System.out.println(new Date(result));
		return result;
	}

	public static void deserialize(SerializationStreamReader streamReader,
			Date instance) {
		// No fields
	}

	public static Date instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		System.out.println("instantiate");
		long time = streamReader.readLong();
		Date date = new Date(offsetUTC(new Date(time), false));
		return date;
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			Date instance) throws SerializationException {
		System.out.println("serialize");
		streamWriter.writeLong(offsetUTC(instance, true));
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
