package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDataFileDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationDataFileDaoTest {

    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;

    @Test
    public void getFilesTest() {
        List<DeclarationDataFile> files = declarationDataFileDao.fetchByDeclarationDataId(1);
        assertEquals(3, files.size());
        assertEquals(1, files.get(0).getDeclarationDataId());
        assertEquals("uuid_1", files.get(0).getUuid());
        assertEquals("file_1", files.get(0).getFileName());
        assertEquals("name1", files.get(0).getUserName());
        assertEquals("dep1", files.get(0).getUserDepartmentName());
        assertNull(files.get(0).getNote());
        assertEquals(1, files.get(1).getDeclarationDataId());
        assertEquals(21657200, files.get(0).getFileTypeId());
        assertEquals("ТФ", files.get(0).getFileTypeName());

        assertEquals("uuid_2", files.get(1).getUuid());
        assertEquals("file_2", files.get(1).getFileName());
        assertEquals("name2", files.get(1).getUserName());
        assertEquals("dep2", files.get(1).getUserDepartmentName());
        assertEquals("str", files.get(1).getNote());
        assertEquals(21657300, files.get(1).getFileTypeId());
        assertEquals("Исходящий в ФНС", files.get(1).getFileTypeName());
    }

    @Test
    public void saveFilesTest() {
        //проверка исходных данных
        List<DeclarationDataFile> files = declarationDataFileDao.fetchByDeclarationDataId(2);
        assertEquals(2, files.size());
        Iterator<DeclarationDataFile> iterator = files.iterator();
        while (iterator.hasNext()) {
            DeclarationDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_3")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_3", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertNull(file.getNote());
                file.setNote("прим");
            } else if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("test", file.getNote());
            } else {
                assert false;
            }
        }

        //проверка изменения комментария к файлу
        declarationDataFileDao.createOrUpdateList(2, files);
        files = declarationDataFileDao.fetchByDeclarationDataId(2);
        assertEquals(2, files.size());
        iterator = files.iterator();
        while (iterator.hasNext()) {
            DeclarationDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_3")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_3", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("прим", file.getNote());
                iterator.remove();
            } else if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("test", file.getNote());
                file.setNote("f");
            } else {
                assert false;
            }
        }
        DeclarationDataFile newFile = new DeclarationDataFile();
        newFile.setUuid("uuid_5");
        newFile.setUserName("Контролер");
        newFile.setUserDepartmentName("Банк");
        newFile.setNote("");
        newFile.setFileTypeId(21657500);
        files.add(newFile);

        //проверка добавления и удаления файла, изменения комментария к файлу
        declarationDataFileDao.createOrUpdateList(2, files);
        files = declarationDataFileDao.fetchByDeclarationDataId(2);
        assertEquals(2, files.size());
        iterator = files.iterator();
        while (iterator.hasNext()) {
            DeclarationDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("f", file.getNote());
            } else if (file.getUuid().equals("uuid_5")) {
                assertEquals(2, file.getDeclarationDataId());
                assertEquals("file_5.xlsx", file.getFileName());
                assertEquals("Контролер", file.getUserName());
                assertEquals("Банк", file.getUserDepartmentName());
                assertEquals("", file.getNote());
                assertEquals(21657500, file.getFileTypeId());
                assertEquals("Отчет", file.getFileTypeName());
            } else {
                assert false;
            }
        }
    }

    @Test
    public void saveFilesTest2() {
        List<DeclarationDataFile> files = new ArrayList<DeclarationDataFile>();
        DeclarationDataFile newFile = new DeclarationDataFile();
        newFile.setUuid("uuid_6");
        newFile.setUserName("Контролер");
        newFile.setUserDepartmentName("Банк");
        newFile.setNote("прим");
        newFile.setFileTypeId(21657400);
        files.add(newFile);
        declarationDataFileDao.createOrUpdateList(3, files);

        files = declarationDataFileDao.fetchByDeclarationDataId(3);
        assertEquals(1, files.size());
        DeclarationDataFile file = files.get(0);
        assertEquals(3, file.getDeclarationDataId());
        assertEquals("uuid_6", file.getUuid());
        assertEquals("file_6", file.getFileName());
        assertEquals("Контролер", file.getUserName());
        assertEquals("Банк", file.getUserDepartmentName());
        assertEquals("прим", file.getNote());
        assertEquals(21657400, file.getFileTypeId());
        assertEquals("Входящий из ФНС", file.getFileTypeName());
    }

    @Test
    public void saveSingle() {
        DeclarationDataFile newFile = new DeclarationDataFile();
        newFile.setDeclarationDataId(4);
        newFile.setUuid("uuid_7");
        newFile.setUserName("Контролер");
        newFile.setUserDepartmentName("Банк");
        newFile.setNote(null);
        newFile.setFileTypeId(21657700);
        declarationDataFileDao.create(newFile);

        List<DeclarationDataFile> files = declarationDataFileDao.fetchByDeclarationDataId(4);
        Assert.assertEquals(1, files.size());
        DeclarationDataFile file = files.get(0);
        assertEquals(4, file.getDeclarationDataId());
        assertEquals("uuid_7", file.getUuid());
        assertEquals("file_7", file.getFileName());
        assertEquals("Контролер", file.getUserName());
        assertEquals("Банк", file.getUserDepartmentName());
        assertNull(file.getNote());
        assertEquals(21657700, file.getFileTypeId());
    }

    @Test
    public void testFindFileWithMaxWeight() {
        declarationDataFileDao.fetchWithMaxWeight(-1L);
    }

    @Test
    public void testFindAllByDeclarationIdAndType() {
        assertThat(declarationDataFileDao.findAllByDeclarationIdAndType(1L, AttachFileType.TRANSPORT_FILE), hasSize(2));
        assertThat(declarationDataFileDao.findAllByDeclarationIdAndType(1L, AttachFileType.OUTGOING_TO_FNS), hasSize(1));
    }

    @Test
    public void testDeleteByDeclarationDataIdAndTypeSuccess() {
        long result = declarationDataFileDao.deleteByDeclarationDataIdAndType(1, AttachFileType.TRANSPORT_FILE);
        assertEquals(2L, result);
        assertEquals(1, declarationDataFileDao.fetchByDeclarationDataId(1).size());
    }

    @Test
    public void testDeleteByDeclarationDataIdAndTypeFail() {
        long result = declarationDataFileDao.deleteByDeclarationDataIdAndType(1, AttachFileType.OTHER);
        assertEquals(0L, result);
        assertEquals(3, declarationDataFileDao.fetchByDeclarationDataId(1).size());
    }

    @Test
    public void testDeleteTransportFileExcel() {
        declarationDataFileDao.deleteTransportFileExcel(1L);
        List<DeclarationDataFile> result = declarationDataFileDao.fetchByDeclarationDataId(1L);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testIsExistsTrue() {
        boolean isExist = declarationDataFileDao.isExists(1, "uuid_2");
        assertTrue(isExist);
    }

    @Test
    public void testIsExistsFalse() {
        boolean isExist = declarationDataFileDao.isExists(1, "uuid_3");
        assertFalse(isExist);
    }
}
