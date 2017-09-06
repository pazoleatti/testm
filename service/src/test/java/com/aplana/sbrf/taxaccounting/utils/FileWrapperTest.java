package com.aplana.sbrf.taxaccounting.utils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class FileWrapperTest {
    private static FileWrapper folder;
    private static FileWrapper file;

    private static File folderFile;
    private static File fileFile;

    private static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void init() throws IOException {
        temporaryFolder.create();

        folderFile = new File(temporaryFolder.getRoot().getPath());
        fileFile = temporaryFolder.newFile();
        file = new FileWrapper(fileFile);
        folder = new FileWrapper(folderFile);
    }

    @AfterClass
    public static void clean() {
        file.delete();
        folder.delete();
        folder = null;
        file = null;
        folder = null;
        file = null;
        temporaryFolder.delete();
        temporaryFolder = null;
    }

    @Test
    public void existsTest() throws MalformedURLException {
        Assert.assertTrue(folder.exists());
        Assert.assertTrue(file.exists());
    }

    @Test
    public void getNameTest() {
        Assert.assertEquals(folderFile.getName(), folder.getName());
        Assert.assertEquals(fileFile.getName(), file.getName());
    }

    @Test
    public void isDirectoryTest() {
        Assert.assertTrue(folder.isDirectory());
        Assert.assertFalse(file.isDirectory());
    }

    @Test
    public void isFileTest() {
        Assert.assertFalse(folder.isFile());
        Assert.assertTrue(file.isFile());
    }

    @Test
    public void canReadTest() {
        Assert.assertTrue(folder.canRead());
        Assert.assertTrue(file.canRead());
    }

    @Test
    public void canWriteTest() {
        if (folderFile.setWritable(true, false)) {
            Assert.assertTrue(folder.canWrite());
        }
        Assert.assertTrue(folder.canWrite());
        if (fileFile.setWritable(true, false)) {
            Assert.assertTrue(file.canWrite());
        }
        if (fileFile.setWritable(false, false)) {
            Assert.assertFalse(file.canWrite());
        }
    }

    @Test
    public void canReadFolderTest() {
        Assert.assertTrue(FileWrapper.canReadFolder(folderFile.getPath()));
        Assert.assertFalse(FileWrapper.canReadFolder(fileFile.getPath()));
    }

    @Test
    public void canReadFileTest() {
        Assert.assertFalse(FileWrapper.canReadFile(folderFile.getPath()));
        Assert.assertTrue(FileWrapper.canReadFile(fileFile.getPath()));
    }

    @Test
    public void canWriteFolderTest() {
        Assert.assertTrue(FileWrapper.canWriteFolder(folderFile.getPath()));
        Assert.assertFalse(FileWrapper.canWriteFolder(fileFile.getPath()));
        Assert.assertFalse(FileWrapper.canWriteFolder("1"));
        Assert.assertFalse(FileWrapper.canWriteFolder("2"));
        Assert.assertFalse(FileWrapper.canWriteFolder("3"));
    }

    @Test
    public void canWriteFileTest() {
        if (folderFile.setWritable(true, false)) {
            Assert.assertFalse(FileWrapper.canWriteFile(folderFile.getPath()));
        }

        if (fileFile.setWritable(true, false)) {
            Assert.assertTrue(FileWrapper.canWriteFile(fileFile.getPath()));
        }
        if (fileFile.setWritable(false, false)) {
            Assert.assertFalse(FileWrapper.canWriteFile(fileFile.getPath()));
        }
    }

    @Test
    public void getPathTest() {
        Assert.assertTrue(folder.getPath().contains(folderFile.getName()));
        Assert.assertTrue(file.getPath().contains(folderFile.getName()));
        Assert.assertTrue(file.getPath().contains(fileFile.getName()));
    }
}
