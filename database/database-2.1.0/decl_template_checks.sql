declare 
	v_count number;
BEGIN
	select count(1) into v_count from decl_template_checks;
	if v_count=0 then
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '000-0007-00001', 'Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ', 'Проверка наличия или отсутствия значения в графе в зависимости от условий', 1);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '001-0001-00002', 'Значение не соответствует данным справочника', 'Соответствие кода гражданства справочнику', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '003-0001-00002', '"Дата начисленного дохода" указана некорректно', 'Заполнение Раздела 3 Графы 10.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '003-0001-00003', '"Дата применения вычета в текущем периоде" не соответствует "Дате начисления дохода"', 'Заполнение Раздела 3 Графы 10.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '001-0001-00006', '"Сумма применения вычета" указана некорректно', 'Заполнение Раздела 3 Графы 16.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '004-0001-00004', '«Дата исчисленного/удержанного/излишне удержанного/возвращенного налогоплательщику  налога»  рассчитана неверно.', 'Заполнение Раздела 2 Графы 15.', 1);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '004-0001-00005', '"НДФЛ исчисленный" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 16.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '004-0001-00006', '"НДФЛ удержанный" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 17.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 100, '004-0001-00010', '"Срок перечисления в бюджет" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 21.', 0);

		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '000-0007-00001', 'Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ', 'Проверка наличия или отсутствия значения в графе в зависимости от условий', 1);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '001-0001-00002', 'Значение не соответствует данным справочника', 'Соответствие кода гражданства справочнику', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '003-0001-00002', '"Дата начисленного дохода" указана некорректно', 'Заполнение Раздела 3 Графы 10.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '003-0001-00003', '"Дата применения вычета в текущем периоде" не соответствует "Дате начисления дохода"', 'Заполнение Раздела 3 Графы 10.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '001-0001-00006', '"Сумма применения вычета" указана некорректно', 'Заполнение Раздела 3 Графы 16.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '004-0001-00004', '«Дата исчисленного/удержанного/излишне удержанного/возвращенного налогоплательщику  налога»  рассчитана неверно.', 'Заполнение Раздела 2 Графы 15.', 1);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '004-0001-00005', '"НДФЛ исчисленный" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 16.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '004-0001-00006', '"НДФЛ удержанный" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 17.', 0);
		insert into decl_template_checks (id, declaration_type_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, 101, '004-0001-00010', '"Срок перечисления в бюджет" рассчитан некорректно.', 'Заполнение Раздела 2 Графы 21.', 0);

		insert into decl_template_checks (id, declaration_type_id, declaration_template_id, check_code, check_type, description, is_fatal)
		select seq_decl_template_checks.nextval, ch.declaration_type_id, dt.id, ch.check_code, ch.check_type, ch.description, ch.is_fatal
		from decl_template_checks ch
		join declaration_template dt on dt.declaration_type_id = ch.declaration_type_id;
		
		update decl_template_checks set CHECK_CODE='003-0001-00006' where CHECK_CODE='001-0001-00006';
	end if;
END;
/