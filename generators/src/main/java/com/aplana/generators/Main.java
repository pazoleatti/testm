package com.aplana.generators;

import com.aplana.generators.data.FL;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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
                int argi = 0;
                String type = args[argi++];
                File file1 = new File(args[argi++]);
                File file2 = null;
                if (new File(args[argi]).exists()) {
                    file2 = new File(args[argi++]);
                }
                Integer numberOfFl = Integer.parseInt(args[argi++]);
                Integer numberOfFiles = args.length > argi ? Integer.parseInt(args[argi++]) : 1;

                printStream.println("Началось изменение атрибутов " + new Date());
                if (type.equalsIgnoreCase("xlsx")) {
                    if (!"xlsx".equals(FilenameUtils.getExtension(file1.getAbsolutePath()))) {
                        printStream.println("расширение файла не верное, должен быть xlsx");
                    }
                    new GeneratorRnuNdflXlsx().generateXlsx(file1, numberOfFl);
                } else if (type.equalsIgnoreCase("xml")) {
                    if (!"xml".equals(FilenameUtils.getExtension(file1.getName())) || file2 != null && !"xml".equals(FilenameUtils.getExtension(file2.getName()))) {
                        printStream.println("расширение файла не верное, должен быть xml");
                    }
                    for (int i = 0; i < numberOfFiles; i++) {
                        File file;
                        long seed = System.currentTimeMillis();
                        FL.random = new Random(seed);
                        if ((file = file1).getName().startsWith("FL") || (file = file2) != null && file2.getName().startsWith("FL")) {
                            new GeneratorFLNdflXml().generateXML(file, generateFileName(file.getAbsolutePath(), i), numberOfFl);
                        }
                        FL.random = new Random(seed);
                        if (!(file = file1).getName().startsWith("FL") || (file = file2) != null && !file2.getName().startsWith("FL")) {
                            new GeneratorRnuNdflXml().generateXML(file, generateFileName(file.getAbsolutePath(), i), numberOfFl);
                        }
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

    private static String generateFileName(String originalFileName, int i) {
        if (originalFileName.contains("FL_")) {
            String path = originalFileName.substring(0, originalFileName.lastIndexOf(File.separator) + 1);
            String fileName = originalFileName.substring(originalFileName.lastIndexOf(File.separator) + 1);
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            String newFileName = fileName.replaceAll(fileName.substring(3, 22), strDate);
            return path + File.separator + newFileName;
        }

        return originalFileName.replaceAll(originalFileName.substring(originalFileName.length() - 36, originalFileName.length() - 4),
                Integer.toString(i + 1).length() == 1 ?
                        RandomStringUtils.randomAlphanumeric(30) + "-" + Integer.toString(i + 1)
                        : RandomStringUtils.randomAlphanumeric(29) + "-" + Integer.toString(i + 1));
    }
}