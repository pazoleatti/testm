package com.google.gwt.user.client.rpc.core.com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.ColumnFormatter;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;


/**
 * Этот класс формамально реализует сериализацию {@link com.aplana.sbrf.taxaccounting.model.ColumnFormatter}
 */
public final class ColumnFormatter_CustomFieldSerializer extends
		CustomFieldSerializer<ColumnFormatter> {

	/**
	 * @param streamReader a SerializationStreamReader instance
	 * @param instance the instance to be deserialized
	 */
	public static void deserialize(SerializationStreamReader streamReader,
                                   ColumnFormatter instance) {
		// No fields
	}

	public static ColumnFormatter instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void serialize(SerializationStreamWriter streamWriter,
                                 ColumnFormatter instance) throws SerializationException {
	}

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader,
                                    ColumnFormatter instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}

	@Override
	public ColumnFormatter instantiateInstance(SerializationStreamReader streamReader)
			throws SerializationException {
		return instantiate(streamReader);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter,
                                  ColumnFormatter instance) throws SerializationException {
		serialize(streamWriter, instance);
	}
}
