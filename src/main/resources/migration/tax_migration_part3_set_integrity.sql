--Set Integrity
--PK constraints
alter table DEPARTMENT add constraint SYS_C003404 primary key (ID);
alter table NSI_CURRENCY add constraint ID_CURRENCY_PK primary key (ID);
alter table NSI_DEPARTAX add constraint PRKEY_IDDEPARTAX primary key (IDDEPARTAX);
alter table NSI_OKATO add constraint ID_NSI_OKATO_PK primary key (ID_OKATO);
alter table NSI_RATES add constraint ID_NSI_RATES_PK primary key (ID_RATES);
alter table NSI_SECURITY add constraint ID_NSI_SECURITY_PK primary key (ID);

alter table APP add constraint APP_PK primary key (IDAPP);
alter table ROLES add constraint ROLE_PK primary key (RL_ID);
alter table USEREVENT add constraint USEREVENT_PK primary key (UE_ID);
alter table ACSLEVEL add constraint PK_ACSLEVEL primary key (IDACSLEVEL);
alter table ASYSTEM add constraint PK_ASYSTEM primary key (IDASYSTEM);
alter table DOP_TRAN add constraint PK_DOPTRAN primary key (IDDOPTRAN);
alter table ENTRANCEDIR add constraint PK_ENTRANCEDIR primary key (IDENTRANCEDIR);
alter table EVENTGROUP add constraint PK_EVENTGROUP primary key (IDEVENTGROUP);
alter table EVENTS add constraint PK_EVENTS primary key (IDEVENT);
alter table EXEMPLAR add constraint PK_EXEMPLAR primary key (IDEXEMPLAR);
alter table IN_FILE add constraint PK_INFILE primary key (IDFILE);
alter table OBJ add constraint PK_OBJ primary key (IDOBJ);
alter table PERIODLIST add constraint PK_PERIODLIST primary key (IDPERIODLIST);
alter table PRELIMPROC add constraint PK_PRELIMPROC primary key (IDPRELIMPROC);
alter table PROVIDER add constraint PK_PROVIDER primary key (IDPROVIDER);
alter table SUBDICT add constraint PK_SUBDICT primary key (IDSUBDICT);
alter table TRANSACTION add constraint PK_TRANSACTION primary key (IDTRANSACTION);
alter table VEROBJ add constraint VEROBJ_PK primary key (IDVEROBJ);
alter table WAY add constraint PK_WAY primary key (IDWAY);

alter table TRD_25  add constraint TR_PK25 primary key (IDROW);
alter table TRD_25M  add constraint TR_PK25M primary key (IDROW);
alter table TRD_26  add constraint TR_PK26 primary key (IDROW);
alter table TRD_26M  add constraint TR_PK26M primary key (IDROW);
alter table TRD_27  add constraint TR_PK27 primary key (IDROW);
alter table TRD_27M  add constraint TR_PK27M primary key (IDROW);
alter table TRD_31  add constraint TR_PK31 primary key (IDROW);
alter table TRD_31M  add constraint TR_PK31M primary key (IDROW);
alter table TRD_51  add constraint TR_PK51 primary key (IDROW);
alter table TRD_51M  add constraint TR_PK51M primary key (IDROW);
alter table TRD_53 add constraint TR_PK53 primary key (IDROW);
alter table TRD_53M add constraint TR_PK53M primary key (IDROW);
alter table TRD_54 add constraint TR_PK54 primary key (IDROW);
alter table TRD_54M add  constraint TR_PK54M primary key (IDROW);
alter table TRD_59 add constraint TR_PK59 primary key (IDROW);
alter table TRD_59M add constraint TR_PK59M primary key (IDROW);
alter table TRD_60 add constraint TR_PK60 primary key (IDROW);
alter table TRD_60M add constraint TR_PK60M primary key (IDROW);
alter table TRD_64 add constraint TR_PK64 primary key (IDROW);
alter table TRD_64M add constraint TR_PK64M primary key (IDROW);

alter table SEVERITIES add constraint SYS_C009662 primary key (ID);
alter table OBJDICT add constraint PK_OBJDICT primary key (IDOBJDICT);
alter table PERIODITY add constraint PK_PERIODITY primary key (IDPERIODITY);


alter table APP add CONSTRAINT app_name_uq UNIQUE (app_name);
alter table DEPARTMENT add CONSTRAINT unq_depart_code UNIQUE (code);

--Check constraints

--Indexes
create index IDX_DEPARTMENT_DEFINITION on DEPARTMENT (DEFINITION);
create index IDX_DEPARTMENT_PAR_FIELD on DEPARTMENT (PAR_FIELD);
create index IDX_EVENTS_FIDACSLEVEL on EVENTS (FIDACSLEVEL ASC);
create index IDX_NSI_BANK_RATE_FIDEXEMPLAR on NSI_BANK_RATE (FIDEXEMPLAR ASC);
create index IDX_NSI_CURRENCY_FIDEXEMPLAR on NSI_CURRENCY (FIDEXEMPLAR ASC);
create index IDX_NSI_CURRENCY_6 on NSI_CURRENCY (CODE ASC, FIDEXEMPLAR ASC);
create index IDX_NSI_SECURITY_FIDEXEMPLAR on NSI_SECURITY (FIDEXEMPLAR);
create index EXEMP_INDX_NSI_RATES on NSI_RATES (FIDEXEMPLAR);
create index IDX_DOP_TRAN_FIDEVENT on DOP_TRAN (FIDEVENT ASC);
create index IDX_DOP_TRAN_FIDTRAN on DOP_TRAN (FIDTRAN ASC);
create index IDX_ENTRANCEDIR_RECEIVEDIR on ENTRANCEDIR (RECEIVEDIR);
create index IDX_EXEMPLAR_FIDOBJ on EXEMPLAR (FIDOBJ ASC);
create index IDX_EXEMPLAR_FIDPERIODLIST on EXEMPLAR (FIDPERIODLIST ASC);
create index IDX_EXEMPLAR_FIDTRANSACTION on EXEMPLAR (FIDTRANSACTION ASC);
create index IDX_EXEMPLAR_TYPEEXEMPLAR on EXEMPLAR (TYPEEXEMPLAR ASC);
create index IDX_GENPARAM_46 on GENPARAM (FIDSECTION ASC,IDGENPARAM ASC);
create index IDX_GENPARAM_48 on GENPARAM (FIDSECTION ASC,TEXT ASC);
create index IDX_IN_FILE_FIDPROCESS on IN_FILE (FIDPROCESS ASC);
create index IDX_IN_FILE_FIDSUBDICT on IN_FILE (FIDSUBDICT ASC);
create index IDX_IN_FILE_FIDVERLOAD on IN_FILE (FIDVERLOAD ASC);
create index IDX_OBJ_66 on OBJ (FIDOBJDICT ASC,FIDPERIODITY ASC,FIDPROVIDER ASC,FIDSUBDICT ASC,FIDVEROBJ ASC);
create index FCODEPERIODITY on PERIODLIST (FCODEPERIODITY);
create index IDX_PERIODLIST_FIDPERIODITY on PERIODLIST (FIDPERIODITY ASC);
create index IDX_PRELIMPROC_FIDCONVERSE on PRELIMPROC (FIDCONVERSE ASC);
create index IDX_PRELIMPROC_FIDENTRANCEDIR on PRELIMPROC (FIDENTRANCEDIR ASC);
create index IDX_PRELIMPROC_FIDPERIODITY on PRELIMPROC (FIDPERIODITY ASC);
create index IDX_PRELIMPROC_FIDPROVIDER on PRELIMPROC (FIDPROVIDER ASC);
create index IDX_PRELIMPROC_FIDSUBDICT on PRELIMPROC (FIDSUBDICT ASC);
create index IDX_PRELIMPROC_FIDWAY on PRELIMPROC (FIDWAY ASC);
create index IDX_PRELIMPROC_80 on PRELIMPROC (FIDENTRANCEDIR ASC, FILEMASK ASC);
create index IDX_PROVIDER_FIDASYSTEM on PROVIDER (FIDASYSTEM ASC);
create index IDX_PROVIDER_FIDDEPARTMENT on PROVIDER (FIDDEPARTMENT ASC);
create index IDX_PROVIDER_96 on PROVIDER (DATELOAD ASC, IDPROVIDER ASC);
create index IDX_SUBDICT_FIDOBJDICT on SUBDICT (FIDOBJDICT ASC);
create index IDX_TRANSACTION_FIDMONITOR on TRANSACTION (FIDMONITOR ASC);
create index IDX_TRANSACTION_FIDPRELIMPROC on TRANSACTION (FIDPRELIMPROC ASC);
create index IDX_TRANSACTION_FILENAME on TRANSACTION (FILENAME ASC);
create index IDX_WAY_FIDPROCESS on WAY (FIDPROCESS ASC);

create index TR_EXNUMDEAL25 on TRD_25 (FIDEXEMPLAR ASC,NUMDEAL ASC);
create index TRI_FIDEXEMPLAR_25 on TRD_25 (FIDEXEMPLAR ASC);
create index TR_TRI_NUMDEAL on TRD_25 (NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_25M on TRD_25M (FIDMNLEXEMPLAR ASC);
create index TR_EXNUMDEAL26 on TRD_26 (FIDEXEMPLAR ASC, NUMDEAL ASC);
create index TRI_FIDEXEMPLAR_26 on TRD_26 (FIDEXEMPLAR ASC);
create index TR_NUMDEAL on TRD_26 (NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_26M on TRD_26M (FIDMNLEXEMPLAR ASC);
create index TR_EXNUMDEAL27 on TRD_27 (FIDEXEMPLAR ASC, NUMDEAL ASC);
create index TRI_FIDEXEMPLAR_27 on TRD_27 (FIDEXEMPLAR ASC);
create index TRI_FIDMNLEXEMPLAR_27M on TRD_27M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_31 on TRD_31 (FIDEXEMPLAR ASC);
create index TRI_SORT_31 on TRD_31 (CODETYPEPAPER ASC);
create index TRI_FIDMNLEXEMPLAR_31M on TRD_31M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_51 on TRD_51 (FIDEXEMPLAR ASC);
create index TRI_SORT_51 on TRD_51 (NUM ASC);
create index TRI_FIDMNLEXEMPLAR_51M on TRD_51M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_53 on TRD_53 (FIDEXEMPLAR ASC);
create index TRI_SORT_53 on TRD_53 (DREPO1 ASC,NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_53M on TRD_53M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_54 on TRD_54 (FIDEXEMPLAR ASC);
create index TRI_SORT_54 on TRD_54 (DREPO1 ASC,NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_54M on TRD_54M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_59 on TRD_59 (FIDEXEMPLAR ASC);
create index TRI_SORT_59 on TRD_59 (DREPO1 ASC,NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_59M on TRD_59M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_60 on TRD_60 (FIDEXEMPLAR ASC);
create index TRI_SORT_60 on TRD_60 (DREPO1 ASC,NUMDEAL ASC);
create index TRI_FIDMNLEXEMPLAR_60M on TRD_60M (FIDMNLEXEMPLAR ASC);
create index TRI_FIDEXEMPLAR_64 on TRD_64 (FIDEXEMPLAR ASC);
create index TRI_SORT_64 on TRD_64 (NUM ASC);
create index TRI_FIDMNLEXEMPLAR_64M on TRD_64M (FIDMNLEXEMPLAR ASC);

--Unique constraints
alter table NSI_DEPARTAX add constraint UNQ_NSI_DEPARTAX unique (TB, BRANCH, SUBBRANCH, FLD, DATEOPEN);
alter table ROLES add constraint ROLES_RL_NAME_UK unique (RL_NAME);
alter table ROLES add constraint ROLE_UNQ unique (RL_APP_ID, RL_UNICODE);
alter table ASYSTEM add constraint UNQ_ASYSTEM_CODE unique (CODEASYSTEM);
alter table ASYSTEM add constraint UNQ_ASYSTEM_DEF unique (DEFINITION);
alter table GENPARAM add constraint SECTION_IDPARAM_UNQ unique (IDGENPARAM, FIDSECTION);
alter table OBJ add constraint UNQ_OBJ unique (FIDOBJDICT, FIDVEROBJ, FIDPROVIDER, FIDSUBDICT, FIDPERIODITY);
alter table OBJDICT  add constraint OBJDICT_CODE_UQ unique (CODEOBJDICT);
alter table PERIODLIST add constraint PERIODLIST_UQ unique (DATEBEGIN, DATEEND, FIDPERIODITY);
alter table PRELIMPROC add constraint UNQ_DIR_MASK unique (FILEMASK, FIDENTRANCEDIR);
alter table PROVIDER add constraint UNQ_PROVIDER unique (FIDASYSTEM, FIDDEPARTMENT);
alter table SUBDICT add constraint SUBDICT_CODESUBDICT_UQ unique (CODESUBDICT);
alter table VEROBJ add constraint UNQ_VEROBJ unique (CODE) using index;

--FK constraints
alter table NSI_CURRENCY
   add constraint NSI_CURRENCY_EXEMPLAR foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      on delete cascade
      not deferrable;
	  
alter table NSI_DEPARTAX
   add constraint FIDDEPARTMENT_FK foreign key (FIDDEPARTMENT)
      references DEPARTMENT (ID)
      not deferrable;

alter table USEREVENT
   add constraint USEREVENT_APP_FK foreign key (UE_APP_ID)
      references APP (IDAPP)
      not deferrable;

alter table USERLOG
   add constraint USERLIST_ROLE_FK foreign key (UL_RL_ID)
      references ROLES (RL_ID)
      not deferrable;

alter table USERLOG
   add constraint USERLIST_USEREVENT_FK foreign key (UL_UE_ID)
      references USEREVENT (UE_ID)
      not deferrable;

alter table USERLOG
   add constraint USERLOG_SEVERITIES_FK1 foreign key (UL_SEVERITY)
      references SEVERITIES (ID)
      not deferrable;

alter table ASYSTEM
   add constraint ASYSTEM_FK1 foreign key (FIDASYSTEM)
      references ASYSTEM (IDASYSTEM)
      on delete cascade
      not deferrable;

alter table DOP_TRAN
   add constraint FK_DOPTRAN_EVENT foreign key (FIDEVENT)
      references EVENTS (IDEVENT)
      not deferrable;

alter table DOP_TRAN
   add constraint FK_DOPTRAN_OBJ foreign key (FIDOBJ)
      references OBJ (IDOBJ)
	  on delete cascade
      not deferrable;

alter table DOP_TRAN
   add constraint FK_DOPTRAN_PERIODLIST foreign key (FIDPERIODLIST)
      references PERIODLIST (IDPERIODLIST)
      not deferrable;

alter table DOP_TRAN
   add constraint FK_DOPTRAN_TRANSACTION foreign key (FIDTRAN)
      references TRANSACTION (IDTRANSACTION)
      not deferrable;

alter table EVENTS
   add constraint FK_EVENT_ACSLEVEL foreign key (FIDACSLEVEL)
      references ACSLEVEL (IDACSLEVEL)
      not deferrable;

alter table EVENTS
   add constraint FK_EVENT_EVENTGROUP foreign key (FIDEVENTGROUP)
      references EVENTGROUP (IDEVENTGROUP)
      not deferrable;

alter table EXEMPLAR
   add constraint FK_EXEMPLAR_FILE foreign key (FIDFILE)
      references IN_FILE (IDFILE)
      not deferrable;

alter table EXEMPLAR
   add constraint EXEMPLAR_TRANSACTION foreign key (FIDTRANSACTION)
      references TRANSACTION (IDTRANSACTION)
      not deferrable;	  
	  
alter table EXEMPLAR
   add constraint FK_EXEMPLAR_OBJ foreign key (FIDOBJ)
      references OBJ (IDOBJ)
	  on delete cascade
      not deferrable;

alter table EXEMPLAR
   add constraint FK_EXEMPLAR_PERIODLIST foreign key (FIDPERIODLIST)
      references PERIODLIST (IDPERIODLIST)
      not deferrable;

alter table IN_FILE
   add constraint INFILE_SUBDICT foreign key (FIDSUBDICT)
      references  SUBDICT(IDSUBDICT)
      not deferrable;
	  
alter table OBJ
   add constraint OBJ_OBJDICT_FK foreign key (FIDOBJDICT)
      references OBJDICT (IDOBJDICT)
      not deferrable;

alter table OBJ
   add constraint OBJ_PERIODITY_FK foreign key (FIDPERIODITY)
      references PERIODITY (IDPERIODITY)
      not deferrable;

alter table OBJ
   add constraint OBJ_PROVIDER_FK foreign key (FIDPROVIDER)
      references PROVIDER (IDPROVIDER)
      not deferrable;

alter table OBJ
   add constraint OBJ_SUBDICT_FK foreign key (FIDSUBDICT)
      references SUBDICT (IDSUBDICT)
      not deferrable;

alter table OBJ
   add constraint OBJ_VEROBJ_FK foreign key (FIDVEROBJ)
      references VEROBJ (IDVEROBJ)
      not deferrable;

alter table PERIODLIST
   add constraint FK_PERIODLIST_PERIODITY foreign key (FIDPERIODITY)
      references PERIODITY (IDPERIODITY)
      not deferrable;

alter table PRELIMPROC
   add constraint PRELIMPROC_ENTRANCEDIR foreign key (FIDENTRANCEDIR)
      references ENTRANCEDIR (IDENTRANCEDIR)
      not deferrable;

alter table PRELIMPROC
   add constraint PRELIMPROC_SUBDICT foreign key (FIDSUBDICT)
      references SUBDICT (IDSUBDICT)
      not deferrable;
	  
alter table PRELIMPROC
   add constraint PRELIMPROC_WAY foreign key (FIDWAY)
      references WAY (IDWAY)
      not deferrable;

alter table PROVIDER
   add constraint FK_PROVIDER_ASYSTEM foreign key (FIDASYSTEM)
      references ASYSTEM (IDASYSTEM)
      not deferrable;
	  
alter table ROLES
   add constraint FK_ROLES_APP foreign key (RL_APP_ID)
      references APP (IDAPP)
      not deferrable;
	  
alter table SUBDICT
   add constraint FK_SUBDICT_OBJDICT foreign key (FIDOBJDICT)
      references OBJDICT (IDOBJDICT)
      not deferrable;

alter table TRD_25
   add constraint TR_FK1_25 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_26
   add constraint TR_FK1_26 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_27
   add constraint TR_FK1_27 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_31
   add constraint TR_FK1_31 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_51
   add constraint TR_FK1_51 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_53
   add constraint TR_FK1_53 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_54
   add constraint TR_FK1_54 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_59
   add constraint TR_FK1_59 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_60
   add constraint TR_FK1_60 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;

alter table TRD_64
   add constraint TR_FK1_64 foreign key (FIDEXEMPLAR)
      references EXEMPLAR (IDEXEMPLAR)
      not deferrable;	

--RUNSTATS
exec dbms_stats.gather_schema_stats('MIGRATION', DBMS_STATS.AUTO_SAMPLE_SIZE);	  