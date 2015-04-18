create or replace package body manual is

    procedure dump(
        points_of_sale varchar2,
        database_link varchar2
    ) is
        handle number;
    begin
        dbms_datapump.data_filter(
            handle => handle,
            name => 'SUBQUERY',
            value => 'WHERE POINT_OF_SALE in (' ||  points_of_sale || ')',
            table_name => 'BOOKING_SETTER',
            schema_name => 'PERGO'
        );
    end;

end;
/