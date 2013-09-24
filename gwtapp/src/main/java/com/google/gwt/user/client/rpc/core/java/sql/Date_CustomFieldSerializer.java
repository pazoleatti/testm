package com.google.gwt.user.client.rpc.core.java.sql;

import java.sql.Date;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Custom field serializer for {@link java.sql.Date}.
 */
public final class Date_CustomFieldSerializer extends
		CustomFieldSerializer<Date> {


	public static void deserialize(SerializationStreamReader streamReader,
			Date instance) {
		com.google.gwt.user.client.rpc.core.java.util.Date_CustomFieldSerializer.deserialize(streamReader, instance);
	}

	public static Date instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		return new Date(com.google.gwt.user.client.rpc.core.java.util.Date_CustomFieldSerializer.instantiate(streamReader).getTime());
	}

	public static void serialize(SerializationStreamWriter streamWriter,
			Date instance) throws SerializationException {
		com.google.gwt.user.client.rpc.core.java.util.Date_CustomFieldSerializer.serialize(streamWriter, instance);
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
