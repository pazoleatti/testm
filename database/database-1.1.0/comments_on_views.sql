comment on table VW_DEPART_KPP_OKTMO is 'Взаимосвязь пар КПП и ОКТМО с подразделениями';
comment on column VW_DEPART_KPP_OKTMO.DEP_ID is 'Идентификатор подразделения';
comment on column VW_DEPART_KPP_OKTMO.DEP_NAME is 'Наименование подразделения';
comment on column VW_DEPART_KPP_OKTMO.KPP is 'КПП из параметров подразделения по НДФЛ';
comment on column VW_DEPART_KPP_OKTMO.OKTMO is 'ОКТМО  из параметров подразделения по НДФЛ';

comment on table VW_DEPART_KPP_OKTMO is 'Пары КПП и ОКТМО в отчетных формах подразделений';
comment on column VW_DECL_KPP_OKTMO_FORM7.ID is 'Идентификатор отчетной формы';
comment on column VW_DECL_KPP_OKTMO_FORM7.KPP is 'КПП';
comment on column VW_DECL_KPP_OKTMO_FORM7.OKTMO is 'ОКТМО';
comment on column VW_DECL_KPP_OKTMO_FORM7.NAME is 'Наименование отчетной формы';
comment on column VW_DECL_KPP_OKTMO_FORM7.FORM_KIND is 'Вид налоговой формы';
comment on column VW_DECL_KPP_OKTMO_FORM7.REPORT_PERIOD_ID is 'Идентификатор отчетного периода подразделения';
comment on column VW_DECL_KPP_OKTMO_FORM7.CORRECTION_DATE is 'Период сдачи корректировки';