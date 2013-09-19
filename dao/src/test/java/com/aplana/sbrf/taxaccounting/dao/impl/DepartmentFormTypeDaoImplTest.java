package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentFormTypeDaoImplTest.xml" })
@Transactional
public class DepartmentFormTypeDaoImplTest {
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
	@Before
	public void init(){
		/*
		-- В подразделении 1 есть все налоговые формы
		insert into department_form_type (id, department_id, form_type_id, kind) values (1, 1, 1, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (2, 1, 2, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (3, 1, 3, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (4, 1, 4, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (5, 1, 11, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (6, 1, 12, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (7, 1, 21, 3);

		-- В подразделении 2 есть формы 1 и 2
		insert into department_form_type (id, department_id, form_type_id, kind) values (11, 2, 1, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (12, 2, 2, 3);

		-- В подразделении 3 есть формы 1 и 2
		insert into department_form_type (id, department_id, form_type_id, kind) values (21, 3, 1, 3);
		insert into department_form_type (id, department_id, form_type_id, kind) values (22, 3, 2, 3);

		-- Для Формы 1 в подразделении 2 источниками являются формы 1, 2, 3, 4 из подразделения 1  и форма 2 из подразделения 3
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 1);
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 2);
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 3);
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 4);
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (11, 22);

		-- Для формы 2 в подразделении 2 источниками являются форма 1 из подразлделения 1 и форма 1 из подразделения 2
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 1);
		insert into form_data_source (department_form_type_id, src_department_form_type_id) values (12, 11);

		-- В подразделении 2 есть декларация 1 
		insert into department_declaration_type (id, department_id, declaration_type_id) values (1, 2, 1);

		-- Для декларации 1 в подразделении 2 источником является формы 1,2 из подразделения 3 и формы 11, 12 из подразделения 1
		insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 21);
		insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 22);
		insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 5);
		insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (1, 6);
		*/
	}
	
	@Test
	public void saveSucsess(){
		departmentFormTypeDao.save(1, 1, 2);
	}
	
	@Test(expected = DaoException.class)
	public void saveError(){
		departmentFormTypeDao.save(1, 1, 2);
		departmentFormTypeDao.save(1, 1, 2);
	}
	
	@Test
	public void getByDep(){
		assertEquals(7, departmentFormTypeDao.get(1).size());
	}
	
	@Test
	public void getByDepAndTaxType(){
		assertEquals(3, departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT).size());
	}	
	
	@Test
	public void getFormSources(){
		assertEquals(5, departmentFormTypeDao.getFormSources(2, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void getDeclarationSources(){
		assertEquals(4, departmentFormTypeDao.getDeclarationSources(2, 1).size());
	}
	
	@Test
	public void getFormDestinations(){
		assertEquals(2, departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.fromId(3)).size());
	}

	@Test
	public void getDeclarationDestinations1(){
		assertEquals(1, departmentFormTypeDao.getDeclarationDestinations(3, 1, FormDataKind.fromId(3)).size());
	}

	@Test
	public void getDeclarationDestinations2(){
		assertEquals(0, departmentFormTypeDao.getDeclarationDestinations(1, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void testGetAllSources() {
		assertEquals(5, departmentFormTypeDao.getDepartmentSources(2, TaxType.TRANSPORT).size());
	}

	@Test
	public void testSaveFormSources() {
		List<DepartmentFormType> sources = departmentFormTypeDao.getFormSources(2, 2, FormDataKind.fromId(3));
		List<Long> sourceIds = new ArrayList<Long>();

		for (DepartmentFormType source : sources) {
			sourceIds.add(source.getId());
		}

		sourceIds.add(6l);

		assertEquals(3, sourceIds.size());
		assertTrue(sourceIds.contains(1l));
		assertTrue(sourceIds.contains(6l));
		assertTrue(sourceIds.contains(11l));

		sourceIds.remove(1l);

		departmentFormTypeDao.saveFormSources(12l, sourceIds);
		sources = departmentFormTypeDao.getFormSources(2, 2, FormDataKind.fromId(3));
		sourceIds.clear();

		for (DepartmentFormType source : sources) {
			sourceIds.add(source.getId());
		}

		assertEquals(2, sourceIds.size());
		assertTrue(sourceIds.contains(6l));
		assertTrue(sourceIds.contains(11l));

	}

	@Test
	public void testSaveDeclarationSources() {
		List<DepartmentFormType> sources = departmentFormTypeDao.getDeclarationSources(2, 1);
		List<Long> sourceIds = new ArrayList<Long>();

		for (DepartmentFormType source : sources) {
			sourceIds.add(source.getId());
		}

		sourceIds.add(3l);

		assertEquals(5, sourceIds.size());
		assertTrue(sourceIds.contains(3l));
		assertTrue(sourceIds.contains(5l));
		assertTrue(sourceIds.contains(6l));
		assertTrue(sourceIds.contains(21l));
		assertTrue(sourceIds.contains(22l));

		sourceIds.remove(21l);

		departmentFormTypeDao.saveDeclarationSources(1l, sourceIds);
		sources = departmentFormTypeDao.getDeclarationSources(2, 1);
		sourceIds.clear();

		for (DepartmentFormType source : sources) {
			sourceIds.add(source.getId());
		}

		assertEquals(4, sourceIds.size());
		assertTrue(sourceIds.contains(3l));
		assertTrue(sourceIds.contains(5l));
		assertTrue(sourceIds.contains(6l));
		assertTrue(sourceIds.contains(22l));

	}
}
