package com.aplana.generatorTF;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Date;
import java.util.Random;

/**
 * Класс для генерации ТФ РНУ
 */
public class ReaderAndGeneratorXMLFiles {


    public static void main(String[] args) {
        try {
            if (args.length < 2){
                System.out.print("Вы ввели неверное количество аргументов");
            } else {
                String path = args[0];
                Integer numberOfFiles = Integer.parseInt(args[1]);

                File sourceFile = new File(path);
                File destFile;

                for (int i = 0; i < numberOfFiles; i++){
                    String tmpPath = path.replaceAll(path.substring(path.length() - 36, path.length()-4),
                            Integer.toString(i+1).length() == 1 ?
                                    RandomStringUtils.randomAlphanumeric(30) + "-" + Integer.toString(i+1)
                                    : RandomStringUtils.randomAlphanumeric(29) + "-" + Integer.toString(i+1));
                    destFile = new File(tmpPath);
                    FileUtils.copyFile(sourceFile, destFile);
                    changeXmlFile(destFile, tmpPath.substring(tmpPath.lastIndexOf("/") + 1, tmpPath.length() - 4), args.length == 2 ? 0 : Integer.parseInt(args[2]));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void changeXmlFile (File fXmlFile, String fileName, int countTF) {
        try {
            System.out.println("Время начала: " + new Date());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            if (fileName.length() > 59){
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
                System.out.println("Количество новых объектов = " + countNewNode);
                Node nodeForInsert;
                for (int i = 0; i < countNewNode; i++){
                    nodeForInsert = infoPiece.cloneNode(true);
                    infoPiece.getParentNode().insertBefore(nodeForInsert, infoPiece);
                }
            }

            System.out.println("Началось изменение атрибутов " + new Date());
            NodeList nList = doc.getElementsByTagName("ПолучДох");
            System.out.println("Количество ПолучДох = " + nList.getLength());

            Random r = new Random(System.currentTimeMillis());
            String alph = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
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
                    eElement.setAttribute("ФамФЛ", String.valueOf(alph.charAt(r.nextInt(33))));
                    eElement.setAttribute("ИмяФЛ", String.valueOf(alph.charAt(r.nextInt(33))));
                    eElement.setAttribute("ОтчФЛ", String.valueOf(alph.charAt(r.nextInt(33))));

                    //генерация номера уд.лич.
                    if (eElement.getAttribute("УдЛичнФЛКод").equals("21")){
                        eElement.setAttribute("УдЛичнФЛНом", generateNumberDul(r));
                    }
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(fXmlFile);
            transformer.transform(source, result);
            System.out.println("Время окончания: " + new Date());
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    private static String generateSnils(Random r){
        String snils = "";
        int controlSum = 0;
        int tmp = 0;

        int k = 7;
        for (int i = 0; i < 2; i++) {
            tmp = r.nextInt(2);
            controlSum += tmp * k;
            snils += "00" + tmp + "-";
            k -= 3;
        }

        tmp = 100 + r.nextInt(899);
        controlSum += 3 * tmp/100 + 2 * (tmp/10 % 10) + tmp % 100;
        snils += tmp + "-";

        String strControlSum = String.valueOf(controlSum % 101);
        if (strControlSum.length() == 1){
            snils += "0" + strControlSum;
        } else {
            snils += strControlSum.charAt(strControlSum.length() - 2);
            snils += strControlSum.charAt(strControlSum.length() - 1);
        }

        return snils;
    }

    private static String generateNumberDul (Random r){
        String numberDul = "";

        numberDul += (10 + r.nextInt(89)) + " " + (10 + r.nextInt(89)) + " " + (100000 + r.nextInt(899999));

        return numberDul;
    }
}
