create table PROJECT (
        id integer not null,
        name varchar(50),
        url varchar(100),
        );

        create table PROJECT_1 (
        id integer not null,
        name varchar(50),
        url varchar(100),
        );


        create table PROJECT_2 (
        id integer not null,
        name varchar(50),
        url varchar(100),
        );



SELECT np.id,np.inp,np.snils,np.last_name,np.first_name,np.middle_name,np.birth_day,np.citizenship,np.inn_np,np.inn_foreign,np.id_doc_type,np.id_doc_number,np.status,np.post_index,np.region_code,np.area,np.city,np.locality,np.street,np.building,np.building_1,np.flat,np.country_code,np.address,np.additional_data
FROM person p;