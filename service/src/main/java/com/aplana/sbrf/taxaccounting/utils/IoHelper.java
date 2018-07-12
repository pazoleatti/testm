package com.aplana.sbrf.taxaccounting.utils;


import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class IoHelper {

    @Autowired
    private IoHelper.CreateArchiveDataHelper createArchiveDataHelper;
    @Autowired
    private BlobDataService blobDataService;

    public void addFileToZip(String uuid, ZipArchiveOutputStream zipOutputStream, String path) throws IOException {
        if (!StringUtils.isEmpty(uuid)) {
            ZipArchiveEntry zipEntry = createArchiveDataHelper.createZipArchiveEntry(path);
            zipOutputStream.putArchiveEntry(zipEntry);
            IOUtils.copy(blobDataService.get(uuid).getInputStream(), zipOutputStream);
            zipOutputStream.closeArchiveEntry();
        }
    }

    @Component
    public static class CreateArchiveDataHelper {

        public File createTempFile(String prefix, String suffix) throws IOException {
            return File.createTempFile(prefix, suffix);
        }

        public ZipArchiveOutputStream createZipArchiveOutputStream(File file) throws FileNotFoundException {
            return new ZipArchiveOutputStream(new FileOutputStream(file));
        }

        public InputStream createFileInputStream(File file) throws FileNotFoundException {
            return new BufferedInputStream(new FileInputStream(file));
        }

        public ZipArchiveEntry createZipArchiveEntry(String path) {
            return new ZipArchiveEntry(path);
        }

    }
}
