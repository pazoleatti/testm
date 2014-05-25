package com.aplana.sbrf.taxaccounting.service.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: avanteev
 */
public class FormTemplateImpexpServiceImplTest {

    private static String folder = "Архив-зип";
    private static String zipName = "zip_file";
    private List<String> files;
    private File tempFolder;

    @Before
    public void init() throws IOException {
        tempFolder = File.createTempFile(folder,"");
        tempFolder.delete();
        tempFolder.mkdir();
        files = new ArrayList<String>(10);

        for (int i = 0; i < 10; i++){
            File file = new File(tempFolder.getAbsolutePath() + File.separator +
                    "(РНУ-102) Регистр " + File.separator + "Налог");
            file.delete();
            file.mkdirs();
        }

        for (int i = 0; i < 10; i++)
            files.add(File.createTempFile(tempFolder.getName() + File.separator + "name", ".xml").getName());
    }

    @Test
    public void testZip() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(File.createTempFile(zipName, ".zip"));
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zipOutputStream));
        FileInputStream in = null;
        for (String fileName : files){
            ZipEntry ze = new ZipEntry(new String((folder + File.separator + fileName).getBytes("UTF-8")));
            zipOutputStream.putNextEntry(ze);
            try {
                in = new FileInputStream(tempFolder.getAbsolutePath() + File.separator + fileName);
                IOUtils.copy(in, writer, "UTF-8");
            } finally {
                in.close();
            }
            zipOutputStream.closeEntry();
        }

        zipOutputStream.finish();
        IOUtils.closeQuietly(writer);
        dirDelete(tempFolder);
    }

    private void dirDelete(File directory) throws FileNotFoundException {
        if (directory.listFiles() == null){
            System.out.println(directory.delete());
            return;
        }
        for (File file : directory.listFiles()){
            if (file.isDirectory()){
                dirDelete(file);
                if (!file.delete())
                    throw new FileNotFoundException("");
            } else {
                if (!file.delete())
                    throw new FileNotFoundException("");
            }
        }
    }
}
