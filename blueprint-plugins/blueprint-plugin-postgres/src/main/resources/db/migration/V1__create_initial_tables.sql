create table events (
  id       serial,
  type     text,
  contents jsonb
);

create table blobs (
  id       serial,
  path     text,
  contents bytea
);