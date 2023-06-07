--liquibase formatted sql

--changeset lol:2 logicalFilePath:db/changelog/V1/V1__CREATE__TABLES.sql

create table if not exists app_user
(
    id bigint not null primary key default nextval('app_user_id_seq'),
    username varchar(255),
    password varchar(255),
    unique (username)
);

--rollback drop table app_user

--changeset lol:3 logicalFilePath:db/changelog/V1/V1__CREATE__TABLES.sql

create table if not exists user_roles
(
    user_id bigint not null references app_user(id),
    roles int4
);

--rollback drop table user_roles