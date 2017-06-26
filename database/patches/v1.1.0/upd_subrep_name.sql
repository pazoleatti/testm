update declaration_subreport 
set name = (case when name='2-НДФЛ 1' then '2-НДФЛ (1) по физическому лицу' 
                 when name='2-НДФЛ 2' then '2-НДФЛ (2) по физическому лицу'
                 else name
            end)
where name in ('2-НДФЛ 1','2-НДФЛ 2');