package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Класс для сериализации преопределенные строки формы в XML. В идеале вообще любых объектов.
 *
 * @author Vitalii Samolovskikh
 */
public final class XmlSerializationUtils {
	private static final String TAG_ROWS = "rows";
	private static final String TAG_ROW = "row";
	private static final String ATTR_ALIAS = "alias";
	private static final String ATTR_ORDER = "ord";
	private static final String TAG_CELL = "cell";
	private static final String ATTR_DATE_VALUE = "dateValue";
	private static final String ATTR_NUMERIC_VALUE = "numericValue";
	private static final String ATTR_STRING_VALUE = "stringValue";

	private static final String ENCODING = "utf-8";

	private static final XmlSerializationUtils instance = new XmlSerializationUtils();

	/**
	 * @return экземпляр класса
	 */
	public static XmlSerializationUtils getInstance() {
		return instance;
	}

	/**
	 * Закрытый конструктор, чтобы никто его не сериализовал.
	 */
	private XmlSerializationUtils() {
	}

	/**
	 * Сериализует список строк
	 *
	 * @param rows список строк формы
	 * @return Строка, содержащая XML
	 * @throws XmlSerializationException если не удалось сериализовать. Например, не поддерживается тип значения.
	 *                                   Или что-то не так с поддержкой XML.
	 */
	public String serialize(List<DataRow> rows) {
		Document document = createEmptyDocument();

		Element root = document.createElement(TAG_ROWS);
		document.appendChild(root);

		for (DataRow dataRow : rows) {
			root.appendChild(serializeRow(document, dataRow));
		}

		// Convert DOM-document to XML-string
		return documentToString(document);
	}

	/**
	 * Создает пустой DOM-документ
	 *
	 * @return DOM-документ
	 */
	private Document createEmptyDocument() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// Маловероятно, конечно, но всё же.
			throw new XmlSerializationException(e);
		}
	}

	/**
	 * Сериализует строку в XML-элемент.
	 *
	 * @param document документ
	 * @param dataRow  сериализуемая строка
	 * @return XML-элемент
	 * @throws XmlSerializationException {@link #serializeCell(org.w3c.dom.Document, String, Object)}
	 */
	private Element serializeRow(Document document, DataRow dataRow) {
		Element row = document.createElement(TAG_ROW);
		if (dataRow.getAlias() != null) {
			row.setAttribute(ATTR_ALIAS, dataRow.getAlias());
		}
		row.setAttribute(ATTR_ORDER, String.valueOf(dataRow.getOrder()));

		for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
			String alias = entry.getKey();
			Object value = entry.getValue();

			if (value != null) {
				row.appendChild(serializeCell(document, alias, value));
			}
		}
		return row;
	}

	/**
	 * Сериализует ячейку строки.
	 *
	 * @param document DOM-документ
	 * @param alias    алиас столбца ячейки
	 * @param value    значение
	 * @return XML-элемент
	 * @throws XmlSerializationException в случае если тип значения не поддерживается
	 */
	private Element serializeCell(Document document, String alias, Object value) {
		Element cell = document.createElement(TAG_CELL);
		cell.setAttribute(ATTR_ALIAS, alias);
		if (value instanceof Date) {
			cell.setAttribute(ATTR_DATE_VALUE, FormatUtils.getShortDateFormat().format(value));
		} else if (value instanceof Number) {
			cell.setAttribute(ATTR_NUMERIC_VALUE, FormatUtils.getSimpleNumberFormat().format(value));
		} else if (value instanceof String) {
			cell.setAttribute(ATTR_STRING_VALUE, (String) value);
		} else {
			throw new XmlSerializationException("Unsupported type value.");
		}
		return cell;
	}

	/**
	 * Преобразует документ в XML-строку.
	 *
	 * @param doc документ
	 * @return XML-строка с документом.
	 */
	private String documentToString(Document doc) {
		try {
			//set up a transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);

			//create string from xml tree
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			return writer.toString();
		} catch (TransformerException e) {
			throw new XmlSerializationException("Can't convert document to XML string.", e);
		}
	}

	/**
	 * Десериализует из XML-строки строки банковской формы.
	 *
	 * @param str     XML-строка
	 * @param columns список столбцов формы
	 * @return список строк формы
	 * @throws XmlSerializationException любая ошибка
	 */
	public List<DataRow> deserialize(String str, List<Column> columns) {
		List<DataRow> rows = new ArrayList<DataRow>();

		Document document = stringToDocument(str);
		Element root = document.getDocumentElement();
		NodeList nodeList = root.getElementsByTagName(TAG_ROW);
		for (int i = 0; i < nodeList.getLength(); i++) {
			rows.add(parseDataRow((Element) nodeList.item(i), columns));
		}

		return rows;
	}

	/**
	 * Разбирает строку данных
	 *
	 * @param element елемент строки данных
	 * @param columns список столбцов формы
	 */
	private DataRow parseDataRow(Element element, List<Column> columns) {
		DataRow dataRow = new DataRow(columns);

		// Alias
		Node aliasNode = element.getAttributes().getNamedItem(ATTR_ALIAS);
		if (aliasNode != null) {
			dataRow.setAlias(aliasNode.getNodeValue());
		}

		// Order
		dataRow.setOrder(Integer.valueOf(element.getAttributes().getNamedItem(ATTR_ORDER).getNodeValue()));

		// Value
		NodeList cells = element.getElementsByTagName(TAG_CELL);
		for (int j = 0; j < cells.getLength(); j++) {
			Node cellNode = cells.item(j);
			NamedNodeMap attributes = cellNode.getAttributes();
			Node cellAliasNode = attributes.getNamedItem(ATTR_ALIAS);
			if (cellAliasNode == null) {
				throw new XmlSerializationException("Cell alias is null.");
			}
			String columnAlias = cellAliasNode.getNodeValue();

			// String value
			Node valueNode = attributes.getNamedItem(ATTR_STRING_VALUE);
			if (valueNode != null) {
				dataRow.put(columnAlias, valueNode.getNodeValue());
			}

			// Date value
			valueNode = attributes.getNamedItem(ATTR_DATE_VALUE);
			if (valueNode != null) {
				try {
					dataRow.put(columnAlias, FormatUtils.getShortDateFormat().parse(valueNode.getNodeValue()));
				} catch (ParseException e) {
					throw new XmlSerializationException(e);
				}
			}

			// Numeric value
			valueNode = attributes.getNamedItem(ATTR_NUMERIC_VALUE);
			if (valueNode != null) {
				try {
					dataRow.put(columnAlias, FormatUtils.getSimpleNumberFormat().parse(valueNode.getNodeValue()));
				} catch (ParseException e) {
					throw new XmlSerializationException(e);
				}
			}
		}
		return dataRow;
	}

	/**
	 * Читает DOM-документ из XML-строки
	 *
	 * @param str XML-строка
	 * @return DOM-документ
	 */
	private Document stringToDocument(String str) {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(str.getBytes(ENCODING)));
		} catch (SAXException e) {
			throw new XmlSerializationException(e);
		} catch (IOException e) {
			throw new XmlSerializationException(e);
		} catch (ParserConfigurationException e) {
			throw new XmlSerializationException(e);
		}
	}
}
