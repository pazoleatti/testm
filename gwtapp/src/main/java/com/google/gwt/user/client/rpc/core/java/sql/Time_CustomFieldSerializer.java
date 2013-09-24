package com.google.gwt.user.client.rpc.core.java.sql;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.sql.Time;

/**
 * Этот класс блокирует сериализацию {@link java.util.Time} в проекте.
 */
public final class Time_CustomFieldSerializer extends
		CustomFieldSerializer<Time> {


	public static void deserialize(SerializationStreamReader streamReader,
			Time instance) {
		// No fields
	}

	public static Time instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		throw new SerializationException();
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			Time instance) throws SerializationException {
		throw new SerializationException();
	}

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader,
			Time instance) throws SerializationException {
		throw new SerializationException();
	}

	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}

	@Override
	public Time instantiateInstance(SerializationStreamReader streamReader)
			throws SerializationException {
		return instantiate(streamReader);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter,
			Time instance) throws SerializationException {
		serialize(streamWriter, instance);
	}
}
