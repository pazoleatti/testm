update declaration_subreport 
set name = (case when name='2-���� 1' then '2-���� (1) �� ����������� ����' 
                 when name='2-���� 2' then '2-���� (2) �� ����������� ����'
                 else name
            end)
where name in ('2-���� 1','2-���� 2');