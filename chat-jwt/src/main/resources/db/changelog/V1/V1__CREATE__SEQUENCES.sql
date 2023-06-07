--liquibase formatted sql

--changeset lol:1 logicalFilePath:classpath:db/changelog/V1/V1__CREATE_SEQUENCES.sql

create sequence if not exists app_user_id_seq
    as bigint
    start with 1
    increment by 1;

--rollback drop sequence person_id_seq