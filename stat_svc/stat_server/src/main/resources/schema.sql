DROP TABLE IF EXISTS hits;
drop table if exists users cascade;
drop table if exists categories cascade;
drop table if exists locations cascade;
drop table if exists events cascade;
drop table if exists compilations cascade;
drop table if exists compilations_events cascade;
drop table if exists comments cascade;


CREATE TABLE IF NOT EXISTS hits
(
    id
    bigint
    generated
    by
    default as
    identity
    not
    null,

    app
    VARCHAR
(
    32
) NOT NULL,
    uri VARCHAR
(
    128
) NOT NULL,
    ip VARCHAR
(
    16
) NOT NULL,
    timestamp timestamp WITHOUT TIME ZONE NOT NULL
    );