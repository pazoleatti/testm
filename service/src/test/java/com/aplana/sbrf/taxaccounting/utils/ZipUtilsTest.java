package com.aplana.sbrf.taxaccounting.utils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Test;

import java.io.*;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.*;


public class ZipUtilsTest {

    @Test
    public void test_archive() throws IOException {
        File fileToArchive = null;
        String fileName = "file.txt";
        File zip = null;

        try {
            fileToArchive = File.createTempFile("file", ".txt");
            zip = ZipUtils.archive(fileToArchive, fileName);

            assertThat(zip.exists()).isTrue();

            ZipFile zipFile = new ZipFile(zip);
            ZipArchiveEntry fileEntry = zipFile.getEntry(fileName);
            assertThat(fileEntry).isNotNull();

            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            int numberOfEntries = 0;
            while (entries.hasMoreElements()) {
                numberOfEntries++;
                entries.nextElement();
            }
            assertThat(numberOfEntries).isEqualTo(1);
        } finally {
            if (fileToArchive != null) fileToArchive.delete();
            if (zip != null) zip.delete();
        }
    }
}
