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
	 * @param streamReader a SerializationStreamReader instance
	 * @param instance the instance to be deserialized
	 */
	public static void deserialize(SerializationStreamReader streamReader,
	                               Date instance) {
		// No fields
	}

	public static Date instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		String[] arr = streamReader.readString().split("\\.");
		Integer[] intArr = new Integer[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = Integer.valueOf(arr[i]);
		}
		@SuppressWarnings("deprecation")
		Date d = new Date(intArr[0], intArr[1], intArr[2], intArr[3], intArr[4], intArr[5]);
		return d;
	}

	@SuppressWarnings("deprecation")
	public static void serialize(SerializationStreamWriter streamWriter,
	                             Date instance) throws SerializationException {
		String date = instance.getYear() + "." +
				instance.getMonth() + '.' +
				instance.getDate() + '.';

		if (instance instanceof java.sql.Date) {
			date += "0.0.0";
		} else {
			date += instance.getHours() + "." +
			instance.getMinutes() + '.' +
			instance.getSeconds();
		}
		streamWriter.writeString(date);
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
