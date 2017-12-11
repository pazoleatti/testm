package com.aplana.generatorTF;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Dictionary {

    static List<String> lastnameDictionary;

    static List<String> firstnameDictionary;

    static List<String> middlenameDictionary;

    static List<String> regionsDictionary;

    /**
     * Инициализирует словари
     */
    static void initDictionaries() {
        firstnameDictionary = initDictionary("/firstnames.txt");
        lastnameDictionary = initDictionary("/lastnames.txt");
        middlenameDictionary = initDictionary("/middlenames.txt");
        regionsDictionary = initDictionary("/regions.txt");
    }

    /**
     * Инициализирует словарь из файла
     * @param filePath путь к файлу
     * @return словарь
     */
    private static List<String> initDictionary(String filePath) {
        List<String> toReturn = new ArrayList<>();
        try (InputStream is = Main.class.getResourceAsStream(filePath)) {
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                toReturn.add(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
