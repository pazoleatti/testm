package com.aplana.generatorTF;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import static com.aplana.generatorTF.Dictionary.initDictionaries;

public class Main {

    static PrintStream printStream;

    public static void main(String[] args) {
        try {
            printStream = new PrintStream(System.out, true, "cp866");
            if (args.length < 3) {
                printStream.print("Вы ввели неверное количество аргументов");
            } else {
                printStream.println("Время начала: " + new Date());
                initDictionaries();
                String path = args[0];

                File sourceFile = new File(path);
                File destFile;
                printStream.println("Началось изменение атрибутов " + new Date());
                if (args[1].equalsIgnoreCase("xlsx")) {
                    if (!"xlsx".equals(FilenameUtils.getExtension(sourceFile.getAbsolutePath()))) {
                        printStream.println("расширение файла не верное, должен быть xlsx");
                    }
                    new GeneratorXlsx().generateXlsx(sourceFile, Integer.parseInt(args[2]));
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
                        destFile = new File(tmpPath);
                        FileUtils.copyFile(sourceFile, destFile);
                        new GeneratorXml().changeXmlFile(destFile, tmpPath.substring(tmpPath.lastIndexOf("/") + 1, tmpPath.length() - 4), Integer.parseInt(args[2]));
                    }
                } else {
                    printStream.println("Тип формируемого файла не указан (xml или xlsx)");
                }
                printStream.println("Время окончания: " + new Date());
            }

        } catch (Exception e) {
            e.printStackTrace(printStream);
        }
    }
}