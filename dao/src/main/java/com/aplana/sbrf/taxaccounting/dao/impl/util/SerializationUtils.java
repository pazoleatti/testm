package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Класс для сериализации строк формы. В идеале вообще любых объектов.
 *
 * @author Vitalii Samolovskikh
 */
public class SerializationUtils {
	public static String serialize(List<DataRow> rows) {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("rows");
			doc.appendChild(root);

			for (DataRow dataRow : rows) {
				Element row = doc.createElement("row");
				root.appendChild(row);
				if (dataRow.getAlias() != null) {
					row.setAttribute("alias", dataRow.getAlias());
				}
				row.setAttribute("ord", String.valueOf(dataRow.getOrder()));

				for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
					String alias = entry.getKey();
					Object value = entry.getValue();

					Element cell = doc.createElement("cell");
					row.appendChild(cell);
					cell.setAttribute("alias", alias);
					if (value != null) {
						if (value instanceof Date) {
							cell.setAttribute("dateValue", FormatUtils.getShortDateFormat().format(value));
						} else if (value instanceof Number) {
							cell.setAttribute("numericValue", FormatUtils.getSimpleNumberFormat().format(value));
						} else if (value instanceof String) {
							cell.setAttribute("stringValue", (String) value);
						} else {
							throw new Exception("Serialization exception. Unsupported value type.");
						}
					}
				}
			}

			//set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			//create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();

			return xmlString;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static List<DataRow> deserialize(String str, List<Column> columns) {
		try {
			List<DataRow> rows = new ArrayList<DataRow>();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(str.getBytes()));
			Element root = doc.getDocumentElement();
			NodeList nodeList = root.getElementsByTagName("row");

			for (int i = 0; i < nodeList.getLength(); i++) {
				DataRow dataRow = new DataRow(columns);

				Node node = nodeList.item(i);
				Node aliasNode = node.getAttributes().getNamedItem("alias");
				if (aliasNode != null) {
					dataRow.setAlias(aliasNode.getNodeValue());
				}
				dataRow.setOrder(Integer.valueOf(node.getAttributes().getNamedItem("ord").getNodeValue()));

				NodeList cells = ((Element)node).getElementsByTagName("cell");
				for (int j = 0; j < cells.getLength(); j++) {
					Node cellNode = cells.item(j);
					NamedNodeMap attributes = cellNode.getAttributes();
					String columnAlias = attributes.getNamedItem("alias").getNodeValue();

					Node valueNode = attributes.getNamedItem("stringValue");
					if (valueNode != null) {
						dataRow.put(columnAlias, valueNode.getNodeValue());
					}

					valueNode = attributes.getNamedItem("dateValue");
					if (valueNode != null) {
						dataRow.put(columnAlias, FormatUtils.getShortDateFormat().parse(valueNode.getNodeValue()));
					}

					valueNode = attributes.getNamedItem("numericValue");
					if (valueNode != null) {
						dataRow.put(columnAlias, FormatUtils.getSimpleNumberFormat().parse(valueNode.getNodeValue()));
					}
				}

				rows.add(dataRow);
			}

			return rows;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
