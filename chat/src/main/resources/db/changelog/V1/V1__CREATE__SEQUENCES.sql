--liquibase formatted sql

--changeset lol:4 logicalFilePath:classpath:db/changelog/V1/V1__CREATE_SEQUENCES.sql

create sequence if not exists room_id_seq
    as bigint
    start with 1
    increment by 1;

--rollback drop sequence room_id_seq

--changeset lol:7 logicalFilePath:classpath:db/changelog/V1/V1__CREATE_SEQUENCES.sql

create sequence if not exists message_id_seq
    as bigint
    start with 1
    increment by 1;

--rollback drop sequence message_id_seq