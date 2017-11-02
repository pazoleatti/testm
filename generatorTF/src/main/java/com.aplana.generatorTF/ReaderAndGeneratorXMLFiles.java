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
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReaderAndGeneratorXMLFiles {

    private static List<String> lastnameDictionary;

    private static List<String> firstnameDictionary;

    private static List<String> middlenameDictionary;

    private static List<String> regionsDictionary;

    /**
     * Максимальная возможная дата для генераци даты рождения
     */
    private static final long dateLimit = 946684799000L;

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.print("Вы ввели неверное количество аргументов");
            } else {
                initDictionaries();
                String path = args[0];
                Integer numberOfFiles = Integer.parseInt(args[1]);

                File sourceFile = new File(path);
                File destFile;

                for (int i = 0; i < numberOfFiles; i++) {
                    String tmpPath = path.replaceAll(path.substring(path.length() - 36, path.length() - 4),
                            Integer.toString(i + 1).length() == 1 ?
                                    RandomStringUtils.randomAlphanumeric(30) + "-" + Integer.toString(i + 1)
                                    : RandomStringUtils.randomAlphanumeric(29) + "-" + Integer.toString(i + 1));
                    destFile = new File(tmpPath);
                    FileUtils.copyFile(sourceFile, destFile);
                    changeXmlFile(destFile, tmpPath.substring(tmpPath.lastIndexOf("/") + 1, tmpPath.length() - 4));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void changeXmlFile(File fXmlFile, String fileName) {
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

            NodeList nList = doc.getElementsByTagName("ПолучДох");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateSnils(Random r) {
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
        controlSum += 3 * tmp / 100 + 2 * (tmp / 10 % 10) + tmp % 100;
        snils += tmp + "-";

        String strControlSum = String.valueOf(controlSum % 101);
        if (strControlSum.length() == 1) {
            snils += "0" + strControlSum;
        } else {
            snils += strControlSum.charAt(strControlSum.length() - 2);
            snils += strControlSum.charAt(strControlSum.length() - 1);
        }

        return snils;
    }

    private static String generateNumberDul(Random r) {
        String numberDul = "";

        numberDul += (10 + r.nextInt(89)) + " " + (10 + r.nextInt(89)) + " " + (100000 + r.nextInt(899999));

        return numberDul;
    }

    /**
     * @param filePath
     * @return
     */
    private static List<String> initDictionary(String filePath) {
        List<String> toReturn = new ArrayList<>();
        try (InputStream is = ReaderAndGeneratorXMLFiles.class.getResourceAsStream(filePath)) {
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                toReturn.add(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * Инициализирует словари
     */
    private static void initDictionaries() {
        firstnameDictionary = initDictionary("/firstnames.txt");
        lastnameDictionary = initDictionary("/lastnames.txt");
        middlenameDictionary = initDictionary("/middlenames.txt");
        regionsDictionary = initDictionary("/regions.txt");
    }

    /**
     * Генерирует дату
     *
     * @param r
     * @return
     */
    private static String generateDate(Random r) {
        Calendar calendar = Calendar.getInstance();
        int year = randomBetween(1970, 2000, r);
        int dayOfYear = randomBetween(1, calendar.getActualMaximum(Calendar.DAY_OF_YEAR), r);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return formatter.format(calendar.getTime());
    }

    /**
     * Генерирует случайное целое число для интервала
     *
     * @param start
     * @param end
     * @param r
     * @return
     */
    private static int randomBetween(int start, int end, Random r) {
        int difference = end - start;
        return r.nextInt(difference) + start;
    }

    /**
     * генерирует ИНН
     *
     * @param r
     * @return
     */
    private static String generateInn(Random r) {
        StringBuilder builder = new StringBuilder(regionsDictionary.get(r.nextInt(regionsDictionary.size())));
        for (int i = 0; i < 8; i++) {
            builder.append(generateDigit(r));
        }
        builder.append(computeControlDigit(builder.toString(), new int[]{7, 2, 4, 10, 3, 5, 9, 4, 6, 8}));
        builder.append(computeControlDigit(builder.toString(), new int[]{3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8}));
        return builder.toString();
    }

    /**
     * Генерирует цифру
     *
     * @param r
     * @return
     */
    private static char generateDigit(Random r) {
        String digits = "0123456789";
        return digits.charAt(r.nextInt(10));
    }

    /**
     * Расчет контрольной цифры для ИНН.
     *
     * @param inn
     * @param weights коэффициенты весов
     * @return
     */
    private static int computeControlDigit(String inn, int[] weights) {
        int key = 0;
        // Замечено что до jdk 8 использование String#split с параметром "" - возвращает массив где первый элемент ""
        // поэтому вводится дополнительная проверка на длину строки и массива весовых коэффициентов чтобы в случае
        // наличия убрать лишний символ ""
        String[] innSplited = inn.split("");
        String[] innAsArray = new String[weights.length];
        if (innSplited.length == weights.length) {
            innAsArray = innSplited;
        } else {
            for (int i = 0; i < innSplited.length; i++) {
                if (!innSplited[i].equals("")) {
                    innAsArray[i-1] = innSplited[i];
                }
            }
        }
        for (int i = 0; i < weights.length; i++) {
            key += Integer.valueOf(innAsArray[i]) * weights[i];
        }
        return key % 11 % 10;
    }
}