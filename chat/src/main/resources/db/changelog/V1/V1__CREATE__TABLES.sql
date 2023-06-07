--liquibase formatted sql

--changeset lol:5 logicalFilePath:db/changelog/V1/V1__CREATE__TABLES.sql

create table if not exists room
(
    id bigint not null primary key default nextval('room_id_seq')
);

--rollback drop table room

--changeset lol:8 logicalFilePath:db/changelog/V1/V1__CREATE__TABLES.sql

create table if not exists message
(
    id bigint not null primary key default nextval('message_id_seq'),
    content text,
    sender varchar(255),
    room_id bigint not null references room(id)
);

--rollback drop table message

--changeset lol:9 logicalFilePath:db/changelog/V1/V1__CREATE__TABLES.sql

create table if not exists rooms_members
(
    room_id bigint not null references room(id),
    members varchar(255) not null
);

--rollback drop table rooms_members