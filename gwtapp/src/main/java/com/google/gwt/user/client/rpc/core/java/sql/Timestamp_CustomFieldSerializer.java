package com.google.gwt.user.client.rpc.core.java.sql;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.sql.Timestamp;

/**
 * Этот класс блокирует сериализацию {@link java.util.Timestamp} в проекте.
 */
public final class Timestamp_CustomFieldSerializer extends
		CustomFieldSerializer<Timestamp> {


	public static void deserialize(SerializationStreamReader streamReader,
			Timestamp instance) {
		// No fields
	}

	public static Timestamp instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		throw new SerializationException();
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			Timestamp instance) throws SerializationException {
		throw new SerializationException();
	}

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader,
			Timestamp instance) throws SerializationException {
		throw new SerializationException();
	}

	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}

	@Override
	public Timestamp instantiateInstance(SerializationStreamReader streamReader)
			throws SerializationException {
		return instantiate(streamReader);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter,
			Timestamp instance) throws SerializationException {
		serialize(streamWriter, instance);
	}
}
