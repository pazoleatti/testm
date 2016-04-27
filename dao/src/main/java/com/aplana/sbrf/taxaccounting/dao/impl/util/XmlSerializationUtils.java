package com.aplana.sbrf.taxaccounting.dao.impl.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.aplana.sbrf.taxaccounting.dao.impl.datarow.DataRowMapper;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

/**
 * Класс для сериализации преопределенные строки формы в XML. В идеале вообще
 * любых объектов.
 * 
 * @author Vitalii Samolovskikh
 */
public final class XmlSerializationUtils {
	private static final String TAG_ROWS = "rows";
	private static final String TAG_ROW = "row";
	private static final String ATTR_COLUMN_ALIAS = "alias";
	private static final String TAG_CELL = "cell";
	private static final String ATTR_DATE_VALUE = "dateValue";
	private static final String ATTR_NUMERIC_VALUE = "numericValue";
	private static final String ATTR_STRING_VALUE = "stringValue";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_ROWSPAN = "rowSpan";
	private static final String ATTR_COLSPAN = "colSpan";
	private static final String ATTR_STYLE_ALIAS = "styleAlias";
	private static final String ATTR_CELL_EDITABLE = "editable";

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
	 * @param rows
	 *            список строк формы
	 * @return Строка, содержащая XML
	 * @throws XmlSerializationException
	 *             если не удалось сериализовать. Например, не поддерживается
	 *             тип значения. Или что-то не так с поддержкой XML.
	 */
	public <T extends AbstractCell> String serialize(List<DataRow<T>> rows) {
		Document document = createEmptyDocument();

		Element root = document.createElement(TAG_ROWS);
		document.appendChild(root);
		
		for (DataRow<?> dataRow : rows) {
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
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			// Маловероятно, конечно, но всё же.
			throw new XmlSerializationException(e);
		}
	}

	/**
	 * Сериализует строку в XML-элемент.
	 * 
	 * @param document
	 *            документ
	 * @param dataRow
	 *            сериализуемая строка
	 * @return XML-элемент
	 */
	private Element serializeRow(Document document, DataRow<?> dataRow) {
		Element row = document.createElement(TAG_ROW);
		if (dataRow.getAlias() != null) {
			row.setAttribute(ATTR_COLUMN_ALIAS, dataRow.getAlias());
		}

		for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
			String alias = entry.getKey();
			AbstractCell cell = dataRow.getCell(alias);
			
			// SBRFACCTAX-2527
			if (cell instanceof Cell){
				row.appendChild(serializeCell(document, alias, (Cell)cell));
			} else if (cell instanceof HeaderCell){
				row.appendChild(serializeHeaderCell(document, alias, (HeaderCell)cell));
			} else {
				throw new XmlSerializationException("Неподдерживается ячейка типа " + cell.getClass().getName());
			}
		}
		return row;
	}

	/**
	 * Сериализует ячейку строки.
	 * 
	 * @param document
	 *            DOM-документ
	 * @param alias
	 *            алиас столбца ячейки
	 * @param cell
	 *            ячейка таблицы
	 * @return XML-элемент
	 * @throws XmlSerializationException
	 *             в случае если тип значения не поддерживается
	 */
	private Element serializeCell(Document document, String alias, Cell cell) {
		Element element = document.createElement(TAG_CELL);
		element.setAttribute(ATTR_COLUMN_ALIAS, alias);
		Object value = cell.getValue();
		if (value != null) {
			if (value instanceof Date) {
				element.setAttribute(ATTR_DATE_VALUE, FormatUtils
						.getShortDateFormat().format(value));
			} else if (value instanceof Number) {
				element.setAttribute(ATTR_NUMERIC_VALUE, FormatUtils
						.getSimpleNumberFormat().format(value));
			} else if (value instanceof String) {
				element.setAttribute(ATTR_STRING_VALUE, (String) value);
			} else {
				throw new XmlSerializationException("Unsupported type value.");
			}
		}
		element.setAttribute(ATTR_COLSPAN, String.valueOf(cell.getColSpan()));
		element.setAttribute(ATTR_ROWSPAN, String.valueOf(cell.getRowSpan()));
		if (!FormStyle.DEFAULT_STYLE.equals(cell.getStyle())) {
			element.setAttribute(ATTR_STYLE_ALIAS, cell.getStyle().toString());
		}
		if (cell.isEditable()){
			element.setAttribute(ATTR_CELL_EDITABLE, String.valueOf(true));
		}
		return element;
	}
	
	
	/**
	 * Сериализует ячейку строки заголовка.
	 * 
	 * @param document
	 *            DOM-документ
	 * @param alias
	 *            алиас столбца ячейки
	 * @param cell
	 *            ячейка таблицы
	 * @return XML-элемент
	 * @throws XmlSerializationException
	 *             в случае если тип значения не поддерживается
	 */
	private Element serializeHeaderCell(Document document, String alias, HeaderCell cell) {
		Element element = document.createElement(TAG_CELL);
		element.setAttribute(ATTR_COLUMN_ALIAS, alias);
		if (cell.getValue() != null) {
			element.setAttribute(ATTR_VALUE, (String)cell.getValue());
		}
		element.setAttribute(ATTR_COLSPAN, String.valueOf(cell.getColSpan()));
		element.setAttribute(ATTR_ROWSPAN, String.valueOf(cell.getRowSpan()));
		return element;
	}

	/**
	 * Преобразует документ в XML-строку.
	 * 
	 * @param doc
	 *            документ
	 * @return XML-строка с документом.
	 */
	private String documentToString(Document doc) {
		try {
			// set up a transformer
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			// create string from xml tree
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			return writer.toString();
		} catch (TransformerException e) {
			throw new XmlSerializationException(
					"Can't convert document to XML string.", e);
		}
	}

	/**
	 * Десериализует из XML-строки строки банковской формы.
	 * 
	 * @param str XML-строка
	 * @param formTemplate версия макета НФ
	 * @return список строк формы @throws XmlSerializationException любая ошибка
	 */
	public <T extends AbstractCell> List<DataRow<T>> deserialize(String str, FormTemplate formTemplate, Class<T> clazz) {
		List<DataRow<T>> rows = new ArrayList<DataRow<T>>();

		Document document = stringToDocument(str);
		Element root = document.getDocumentElement();
		NodeList nodeList = root.getElementsByTagName(TAG_ROW);
		for (int i = 0; i < nodeList.getLength(); i++) {
			DataRow<T> dataRow = parseDataRow((Element) nodeList.item(i), formTemplate, clazz);
			//Устанавливаем нумерацию строк в заголовках, так же как внутри таблицы. Необходимо для построения xlsm представления
			dataRow.setIndex(i + 1);
			rows.add(dataRow);
		}
		return rows;
	}

	/**
	 * Разбирает строку данных
	 * 
	 * @param element элемент строки данных
	 * @param formTemplate список столбцов формы
	 */
	private <T extends AbstractCell> DataRow<T> parseDataRow(Element element, FormTemplate formTemplate, Class<T> clazz) {
		// Value
		NodeList cells = element.getElementsByTagName(TAG_CELL);
		
		DataRow<T> dataRow = null;
		if (Cell.class.equals(clazz)){
			dataRow = new DataRow<T>((List<T>) FormDataUtils.createCells(formTemplate));
		} else if (HeaderCell.class.equals(clazz)){ 
			dataRow = new DataRow<T>((List<T>) FormDataUtils.createHeaderCells(formTemplate.getColumns()));
		}
		
		for (int j = 0; j < cells.getLength(); j++) {
			Node cellNode = cells.item(j);
			NamedNodeMap attributes = cellNode.getAttributes();
			Node cellAliasNode = attributes.getNamedItem(ATTR_COLUMN_ALIAS);
			if (cellAliasNode == null) {
				throw new XmlSerializationException("Cell alias is null.");
			}
			
			String columnAlias = cellAliasNode.getNodeValue();
			AbstractCell cell = dataRow.getCell(columnAlias);
			

			parseAbstractCell(cell, cellNode, formTemplate.getColumns(), formTemplate.getStyles());
			if (cell instanceof Cell){
				parseCell((Cell)cell, cellNode, formTemplate);
			} else if (cell instanceof HeaderCell){
				parseHeaderCell((HeaderCell)cell, cellNode, formTemplate.getColumns(), formTemplate.getStyles());
			} else {
				throw new XmlSerializationException("Неподдерживается ячейка типа " + cell.getClass().getName());
			}
			
		}
		
		// Alias
		Node aliasNode = element.getAttributes().getNamedItem(ATTR_COLUMN_ALIAS);
		if (aliasNode != null) {
			dataRow.setAlias(aliasNode.getNodeValue());
		}

		return dataRow;
	}
	
	private void parseAbstractCell(AbstractCell cell, Node cellNode, List<Column> columns, List<FormStyle> styles) {
		NamedNodeMap attributes = cellNode.getAttributes();
		
		Node valueNode = attributes.getNamedItem(ATTR_COLSPAN);
		if (valueNode != null) {
			cell.setColSpan(
					Integer.parseInt(valueNode.getNodeValue()));
		}
		valueNode = attributes.getNamedItem(ATTR_ROWSPAN);
		if (valueNode != null) {
			cell.setRowSpan(
					Integer.parseInt(valueNode.getNodeValue()));
		}
		
	}
	
	
	/**
	 * Парсит ячейку HeaderCell
	 * 
	 * @param cell
	 * @param cellNode
	 * @param columns
	 * @param styles
	 */
	private void parseHeaderCell(HeaderCell cell, Node cellNode, List<Column> columns, List<FormStyle> styles) {

		NamedNodeMap attributes = cellNode.getAttributes();	

		// String value
		Node valueNode = attributes.getNamedItem(ATTR_VALUE);
		if (valueNode != null) {
			cell.setValue(valueNode.getNodeValue(), null);
		}
	}
	
	
	/**
	 * Парсит ячейку Cell
	 * 
	 * @param cell
	 * @param cellNode
	 */
	private void parseCell(Cell cell, Node cellNode, FormTemplate formTemplate) {
		NamedNodeMap attributes = cellNode.getAttributes();
		// String value
		Node valueNode = attributes.getNamedItem(ATTR_STRING_VALUE);
		if (valueNode != null) {
			cell.setValue(valueNode.getNodeValue(), null);
		}
		// Date value
		valueNode = attributes.getNamedItem(ATTR_DATE_VALUE);
		if (valueNode != null) {
			try {
				cell.setValue(FormatUtils.getShortDateFormat()
						.parse(valueNode.getNodeValue()), null);
			} catch (ParseException e) {
				throw new XmlSerializationException(e);
			}
		}
		// Numeric value
		valueNode = attributes.getNamedItem(ATTR_NUMERIC_VALUE);
		if (valueNode != null) {
			try {
				cell.setValue(FormatUtils.getSimpleNumberFormat().parse(
						valueNode.getNodeValue()), null);
			} catch (ParseException e) {
				throw new XmlSerializationException(e);
			}
		}
		valueNode = attributes.getNamedItem(ATTR_STYLE_ALIAS);
		if (valueNode != null && valueNode.getNodeValue() != null) {
			String styleString = valueNode.getNodeValue();
			if(!styleString.isEmpty()) {
				if (styleString.contains(String.valueOf(FormStyle.COLOR_SEPARATOR))) { // новый способ оформления стилей
					DataRowMapper.parseCellStyle(cell, styleString);
				} else {
					cell.setStyle(formTemplate.getStyle(styleString));
				}
			}
		}
		valueNode = attributes.getNamedItem(ATTR_CELL_EDITABLE);
		if (valueNode != null) {
			cell.setEditable(Boolean.valueOf(valueNode.getNodeValue()));
		} else {
			cell.setEditable(false);
		}
	}

	/**
	 * Читает DOM-документ из XML-строки
	 * 
	 * @param str
	 *            XML-строка
	 * @return DOM-документ
	 */
	private Document stringToDocument(String str) {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(str.getBytes(ENCODING)));
		} catch (SAXException e) {
			throw new XmlSerializationException(e);
		} catch (IOException e) {
			throw new XmlSerializationException(e);
		} catch (ParserConfigurationException e) {
			throw new XmlSerializationException(e);
		}
	}
}
