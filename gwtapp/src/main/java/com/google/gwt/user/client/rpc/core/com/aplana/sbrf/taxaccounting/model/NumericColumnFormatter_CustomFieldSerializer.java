package com.google.gwt.user.client.rpc.core.com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.NumericColumnFormatter;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;


/**
 * Этот класс формамально реализует сериализацию {@link com.aplana.sbrf.taxaccounting.model.NumericColumnFormatter}
 */
public final class NumericColumnFormatter_CustomFieldSerializer extends
		CustomFieldSerializer<NumericColumnFormatter> {

	/**
	 * @param streamReader a SerializationStreamReader instance
	 * @param instance the instance to be deserialized
	 */
	public static void deserialize(SerializationStreamReader streamReader,
	                               NumericColumnFormatter instance) {
		// No fields
	}

	public static NumericColumnFormatter instantiate(SerializationStreamReader streamReader)
			throws SerializationException {
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void serialize(SerializationStreamWriter streamWriter,
                                 NumericColumnFormatter instance) throws SerializationException {
	}

	@Override
	public void deserializeInstance(SerializationStreamReader streamReader,
                                    NumericColumnFormatter instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}

	@Override
	public NumericColumnFormatter instantiateInstance(SerializationStreamReader streamReader)
			throws SerializationException {
		return instantiate(streamReader);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter,
                                  NumericColumnFormatter instance) throws SerializationException {
		serialize(streamWriter, instance);
	}
}
