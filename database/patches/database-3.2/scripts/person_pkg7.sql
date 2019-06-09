create or replace 
package person_pkg as
/* Пакет для идентификации физических лиц*/
  type ref_cursor is ref cursor;

  /*
  Процедура подготовки актуальных версий ФЛ на заданную дату. 
  Актуальные версии помещаются во временную таблицу, которая затем используется остальными функциями.
  */
  procedure FillRecordVersions(p_date date default trunc(sysdate));

  /*
  Функция возвращает ссылку на АСНУ хранящуюся в декларации
  */
  function Get_ASNU_Id(p_declaration number) return number;

  -- Получение курсоров для идентификации НДФЛ
  /*
  Выборка ФЛ, которые есть в справочнике ФЛ, для их дальнейшего обновления 
  */
  function GetPersonForUpd(p_declaration number) return ref_cursor;
  /*
  Выборка ФЛ для их дальнейшей проверки с вычислением весов
  */
  function GetPersonForCheck(p_declaration number) return ref_cursor;

end;
/