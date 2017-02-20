package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationDataFileDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationDataFileDaoTest {

	@Autowired
	private DeclarationDataFileDao declarationDataFileDao;

	@Test
	public void getFilesTest() {
		List<DeclarationDataFile> files = declarationDataFileDao.getFiles(1);
		assertEquals(2, files.size());
		assertEquals(1, files.get(0).getDeclarationDataId());
        assertEquals("uuid_1", files.get(0).getUuid());
        assertEquals("file_1", files.get(0).getFileName());
		assertEquals("name1", files.get(0).getUserName());
        assertEquals("dep1", files.get(0).getUserDepartmentName());
        assertNull(files.get(0).getNote());
        assertEquals(1, files.get(1).getDeclarationDataId());
        assertEquals(1, files.get(0).getFileTypeId());
        assertEquals("ТФ", files.get(0).getFileTypeName());

        assertEquals("uuid_2", files.get(1).getUuid());
        assertEquals("file_2", files.get(1).getFileName());
        assertEquals("name2", files.get(1).getUserName());
        assertEquals("dep2", files.get(1).getUserDepartmentName());
        assertEquals("str", files.get(1).getNote());
        assertEquals(2, files.get(1).getFileTypeId());
        assertEquals("Исходящий в ФНС", files.get(1).getFileTypeName());
    }

	@Test
	public void saveFilesTest() {
        //проверка исходных данных
        List<DeclarationDataFile> files = declarationDataFileDao.getFiles(2);
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
        declarationDataFileDao.saveFiles(2, files);
        files = declarationDataFileDao.getFiles(2);
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
        newFile.setFileTypeId(4);
        files.add(newFile);

        //проверка добавления и удаления файла, изменения комментария к файлу
        declarationDataFileDao.saveFiles(2, files);
        files = declarationDataFileDao.getFiles(2);
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
                assertEquals("file_5", file.getFileName());
                assertEquals("Контролер", file.getUserName());
                assertEquals("Банк", file.getUserDepartmentName());
                assertEquals("", file.getNote());
                assertEquals(4, file.getFileTypeId());
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
        newFile.setFileTypeId(3);
        files.add(newFile);
        declarationDataFileDao.saveFiles(3, files);

        files = declarationDataFileDao.getFiles(3);
        assertEquals(1, files.size());
        DeclarationDataFile file = files.get(0);
        assertEquals(3, file.getDeclarationDataId());
        assertEquals("uuid_6", file.getUuid());
        assertEquals("file_6", file.getFileName());
        assertEquals("Контролер", file.getUserName());
        assertEquals("Банк", file.getUserDepartmentName());
        assertEquals("прим", file.getNote());
        assertEquals(3, file.getFileTypeId());
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
        newFile.setFileTypeId(6);
        declarationDataFileDao.saveFile(newFile);

        List<DeclarationDataFile> files = declarationDataFileDao.getFiles(4);
        Assert.assertEquals(1, files.size());
        DeclarationDataFile file = files.get(0);
        assertEquals(4, file.getDeclarationDataId());
        assertEquals("uuid_7", file.getUuid());
        assertEquals("file_7", file.getFileName());
        assertEquals("Контролер", file.getUserName());
        assertEquals("Банк", file.getUserDepartmentName());
        assertNull(file.getNote());
        assertEquals(6, file.getFileTypeId());
    }

    @Test
    public void testFindFileWithMaxWeight() {
        declarationDataFileDao.findFileWithMaxWeight(-1L);
    }
}
