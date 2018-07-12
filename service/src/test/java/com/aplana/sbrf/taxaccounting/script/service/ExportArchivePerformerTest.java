package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.script.service.impl.RefBookServiceImpl;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.utils.IoHelper;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ExportArchivePerformerTest.xml")
public class ExportArchivePerformerTest {
    @Autowired
    private IoHelper.CreateArchiveDataHelper createArchiveDataHelper;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookServiceImpl.ExportArchivePerformer exportArchivePerformer;


    @Test
    public void testCreateExportArchive() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        ZipArchiveOutputStream zos = mock(ZipArchiveOutputStream.class);
        File file = mock(File.class);
        when(createArchiveDataHelper.createTempFile(anyString(), anyString())).thenReturn(file);
        when(createArchiveDataHelper.createZipArchiveOutputStream(file)).thenReturn(zos);
        when(createArchiveDataHelper.createFileInputStream(file)).thenReturn(inputStream);

        exportArchivePerformer.createExportArchive();

        verify(blobDataService, Mockito.times(1)).create(inputStream, "refBooksData.zip");
    }
}
