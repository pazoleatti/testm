package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataFileDao;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import org.junit.Ignore;
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

@Ignore("Налоговые формы не используются!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataFileDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormDataFileDaoTest {

	@Autowired
	private FormDataFileDao formDataFileDao;

	@Test
	public void getFilesTest() {
		List<FormDataFile> files = formDataFileDao.getFiles(1);
		assertEquals(2, files.size());
		assertEquals(1, files.get(0).getFormDataId());
        assertEquals("uuid_1", files.get(0).getUuid());
        assertEquals("file_1", files.get(0).getFileName());
		assertEquals("name1", files.get(0).getUserName());
        assertEquals("dep1", files.get(0).getUserDepartmentName());
        assertNull(files.get(0).getNote());
        assertEquals(1, files.get(1).getFormDataId());
        assertEquals("uuid_2", files.get(1).getUuid());
        assertEquals("file_2", files.get(1).getFileName());
        assertEquals("name2", files.get(1).getUserName());
        assertEquals("dep2", files.get(1).getUserDepartmentName());
        assertEquals("str", files.get(1).getNote());
    }

	@Test
	public void saveFilesTest() {
        //проверка исходных данных
        List<FormDataFile> files = formDataFileDao.getFiles(2);
		assertEquals(2, files.size());
        Iterator<FormDataFile> iterator = files.iterator();
        while (iterator.hasNext()) {
            FormDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_3")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_3", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertNull(file.getNote());
                file.setNote("прим");
            } else if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("test", file.getNote());
            } else {
                assert false;
            }
        }

        //проверка изменения комментария к файлу
        formDataFileDao.saveFiles(2, files);
        files = formDataFileDao.getFiles(2);
        assertEquals(2, files.size());
        iterator = files.iterator();
        while (iterator.hasNext()) {
            FormDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_3")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_3", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("прим", file.getNote());
                iterator.remove();
            } else if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("test", file.getNote());
                file.setNote("f");
            } else {
                assert false;
            }
        }
        FormDataFile newFile = new FormDataFile();
        newFile.setUuid("uuid_5");
        newFile.setUserName("Контролер");
        newFile.setUserDepartmentName("Банк");
        newFile.setNote("");
        files.add(newFile);

        //проверка добавления и удаления файла, изменения комментария к файлу
        formDataFileDao.saveFiles(2, files);
        files = formDataFileDao.getFiles(2);
        assertEquals(2, files.size());
        iterator = files.iterator();
        while (iterator.hasNext()) {
            FormDataFile file = iterator.next();
            if (file.getUuid().equals("uuid_4")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_4", file.getFileName());
                assertEquals("name3", file.getUserName());
                assertEquals("подр3", file.getUserDepartmentName());
                assertEquals("f", file.getNote());
            } else if (file.getUuid().equals("uuid_5")) {
                assertEquals(2, file.getFormDataId());
                assertEquals("file_5", file.getFileName());
                assertEquals("Контролер", file.getUserName());
                assertEquals("Банк", file.getUserDepartmentName());
                assertEquals("", file.getNote());
            } else {
                assert false;
            }
        }
	}

    @Test
    public void saveFilesTest2() {
        List<FormDataFile> files = new ArrayList<FormDataFile>();
        FormDataFile newFile = new FormDataFile();
        newFile.setUuid("uuid_6");
        newFile.setUserName("Контролер");
        newFile.setUserDepartmentName("Банк");
        newFile.setNote("прим");
        files.add(newFile);
        formDataFileDao.saveFiles(3, files);

        files = formDataFileDao.getFiles(3);
        assertEquals(1, files.size());
        FormDataFile file = files.get(0);
        assertEquals(3, file.getFormDataId());
        assertEquals("uuid_6", file.getUuid());
        assertEquals("file_6", file.getFileName());
        assertEquals("Контролер", file.getUserName());
        assertEquals("Банк", file.getUserDepartmentName());
        assertEquals("прим", file.getNote());
    }
}
