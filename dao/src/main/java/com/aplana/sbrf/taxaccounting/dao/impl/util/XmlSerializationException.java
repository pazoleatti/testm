package com.aplana.sbrf.taxaccounting.dao.impl.util;

/**
 * Исключения, возникающие при работе сериализатора.
 *
 * @author Vitalii Samolovskikh
 * @see XmlSerializationUtils
 */
public class XmlSerializationException extends RuntimeException {
	public XmlSerializationException(String message) {
		super(message);
	}

	public XmlSerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlSerializationException(Throwable cause) {
		super(cause);
	}
}
