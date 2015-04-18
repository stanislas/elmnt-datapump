declare
    handle number;
    job_state varchar2(30);
    sts ku$_Status;
begin
    handle := dbms_datapump.open('EXPORT', 'SCHEMA');
    dbms_datapump.add_file(handle => handle, filename => 'coop_2015_3_18.exp', directory => 'DPDUMP', filetype => dbms_datapump.KU$_FILE_TYPE_DUMP_FILE);
    dbms_datapump.add_file(handle => handle, filename => 'coop_2015_3_18.log', directory => 'DPDUMP', filetype => dbms_datapump.KU$_FILE_TYPE_LOG_FILE);
    dbms_datapump.metadata_filter(handle => handle, name => 'SCHEMA_EXPR', value => 'IN (''STAN1'')');
    dbms_datapump.data_filter(handle => handle, name => 'SUBQUERY', value => 'WHERE ID in (2001, 1000)', table_name => 'CUSTOMER', schema_name => 'STAN1');
    dbms_datapump.start_job(handle => handle);
    dbms_datapump.wait_for_job(handle => handle, job_state => job_state);
    dbms_output.put_line('Job finished with state ' || job_state);
    --   exception when others then
     --  dbms_datapump.get_status (
      --     handle => handle,
       --    mask => dbms_datapump.ku$_status_job_error ,
        --   job_state => job_state,
         --  status => sts
      -- );
       --dbms_output.put_line('Job status :' || job_state);
       --raise;
end;
/


select dbms_datapump.KU$_STATUS_JOB_ERROR from dual;
