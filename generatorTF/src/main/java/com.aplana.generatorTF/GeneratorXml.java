package com.aplana.generatorTF;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Random;

import static com.aplana.generatorTF.Dictionary.firstnameDictionary;
import static com.aplana.generatorTF.Dictionary.lastnameDictionary;
import static com.aplana.generatorTF.Dictionary.middlenameDictionary;
import static com.aplana.generatorTF.Utils.*;
import static com.aplana.generatorTF.Main.printStream;

/**
 * Класс для генерации ТФ (xml) РНУ
 */
class GeneratorXml {

    private int nListCounter;

    void changeXmlFile(File fXmlFile, String fileName, int countTF) {
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            if (fileName.length() > 59) {
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }

            Node slPeace = doc.getElementsByTagName("СлЧасть").item(0);
            if (slPeace.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) slPeace;
                element.setAttribute("ИдФайл", fileName);
            }


            if (countTF != 0 && countTF > doc.getElementsByTagName("ИнфЧасть").getLength()){
                Node infoPiece = doc.getElementsByTagName("ИнфЧасть").item(0);
                int countNewNode = countTF - doc.getElementsByTagName("ИнфЧасть").getLength();
                printStream.println("Количество новых объектов = " + countNewNode);
                Node nodeForInsert;
                for (int i = 0; i < countNewNode; i++){
                    nodeForInsert = infoPiece.cloneNode(true);
                    infoPiece.getParentNode().insertBefore(nodeForInsert, infoPiece);
                }
            }

            /*printStream.println("Началось изменение атрибутов " + new Date());*/

            NodeList nList = doc.getElementsByTagName("ПолучДох");

            nListCounter += nList.getLength();

            /*printStream.println("Количество ПолучДох = " + nList.getLength());*/

            Random r = new Random(System.currentTimeMillis());

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    //генерация ИНП
                    String inp = String.valueOf(1000000000 + r.nextInt(2000000000));
                    eElement.setAttribute("ИНП", inp.length() == 10 ? inp : inp.substring(inp.length() - 10, inp.length()));

                    //генерация СНИЛС
                    eElement.setAttribute("СНИЛС", generateSnils(r));

                    //генерация ФИО
                    eElement.setAttribute("ФамФЛ", lastnameDictionary.get(r.nextInt(lastnameDictionary.size())));
                    eElement.setAttribute("ИмяФЛ", firstnameDictionary.get(r.nextInt(firstnameDictionary.size())));
                    eElement.setAttribute("ОтчФЛ", middlenameDictionary.get(r.nextInt(middlenameDictionary.size())));

                    //генерация номера уд.лич.
                    if (eElement.getAttribute("УдЛичнФЛКод").equals("21")) {
                        eElement.setAttribute("УдЛичнФЛНом", generateNumberDul(r));
                    }

                    // генерация даты рождения
                    eElement.setAttribute("ДатаРожд", generateDate(r));

                    // генерация ИНН
                    eElement.setAttribute("ИННФЛ", generateInn(r));
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(fXmlFile);
            transformer.transform(source, result);
            printStream.println("Количество ПолучДох = " + nListCounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
