package com.aplana.generators;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import static com.aplana.generators.Dictionary.initDictionaries;

public class Main {

    static PrintStream printStream;

    public static void main(String[] args) {
        Date startDate = new Date();
        try {
            printStream = new PrintStream(System.out, true, "cp866");
            if (args.length < 3) {
                printStream.print("Вы ввели неверное количество аргументов");
            } else {
                initDictionaries();
                String path = args[0];

                File sourceFile = new File(path);
                File destFile;
                printStream.println("Началось изменение атрибутов " + new Date());
                if (args[1].equalsIgnoreCase("xlsx")) {
                    if (!"xlsx".equals(FilenameUtils.getExtension(sourceFile.getAbsolutePath()))) {
                        printStream.println("расширение файла не верное, должен быть xlsx");
                    }
                    new GeneratorRnuNdflXlsx().generateXlsx(sourceFile, Integer.parseInt(args[2]));
                } else if (args[1].equalsIgnoreCase("xml")) {
                    if (!"xml".equals(FilenameUtils.getExtension(sourceFile.getAbsolutePath()))) {
                        printStream.println("расширение файла не верное, должен быть xml");
                    }
                    Integer numberOfFiles = args.length == 4 ? Integer.parseInt(args[3]) : 1;
                    for (int i = 0; i < numberOfFiles; i++) {
                        String tmpPath = path.replaceAll(path.substring(path.length() - 36, path.length() - 4),
                                Integer.toString(i + 1).length() == 1 ?
                                        RandomStringUtils.randomAlphanumeric(30) + "-" + Integer.toString(i + 1)
                                        : RandomStringUtils.randomAlphanumeric(29) + "-" + Integer.toString(i + 1));
                        new GeneratorRnuNdflXml().generateXML(sourceFile, tmpPath, Integer.parseInt(args[2]));
                    }
                } else {
                    printStream.println("Тип формируемого файла не указан (xml или xlsx)");
                }
            }

        } catch (Exception e) {
            e.printStackTrace(printStream);
        } finally {
            printStream.println("Время начала: " + startDate);
            printStream.println("Время окончания: " + new Date());
        }
    }
}