package com.aplana.sbrf.taxaccounting.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipUtils {

    private ZipUtils() {
    }

    /**
     * Архивирует файл в формат zip.
     * На выходе создается временный файл, который желательно удалить после использования.
     *
     * @param file     архивируемый файл
     * @param fileName имя файла в архиве
     * @return temp-файл архива, подлежащий удалению после использования
     */
    public static File archive(File file, String fileName) throws IOException {
        File zipFile = File.createTempFile("archive", ".zip");
        FileOutputStream zipFileOutputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipStream = new ZipOutputStream(zipFileOutputStream);

        ZipEntry zipEntry = new ZipEntry(fileName);
        zipStream.putNextEntry(zipEntry);
        FileInputStream fileStream = new FileInputStream(file);

        try {
            IOUtils.copy(fileStream, zipStream);
        } finally {
            IOUtils.closeQuietly(fileStream);
            IOUtils.closeQuietly(zipStream);
            IOUtils.closeQuietly(zipFileOutputStream);
        }
        return zipFile;
    }
}
