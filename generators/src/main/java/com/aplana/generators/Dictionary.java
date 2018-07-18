package com.aplana.generators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Dictionary {

    public static List<String> lastnameDictionary;

    public static List<String> firstnameDictionary;

    public static List<String> middlenameDictionary;

    public static List<String> regionsDictionary;

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
            Scanner scanner = new Scanner(is, "Windows-1251");
            while (scanner.hasNextLine()) {
                toReturn.add(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
