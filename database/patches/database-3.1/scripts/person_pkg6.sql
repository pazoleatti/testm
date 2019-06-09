create or replace 
package person_pkg as
/* Пакет для идентификации физических лиц*/
  type ref_cursor is ref cursor;

  procedure FillRecordVersions(p_date date default trunc(sysdate));

  -- Получение курсоров для идентификации НДФЛ
  function GetPersonForUpd(p_declaration number,p_asnu number default 1) return ref_cursor;
  function GetPersonForCheck(p_declaration number,p_asnu number default 1) return ref_cursor;

end;
/