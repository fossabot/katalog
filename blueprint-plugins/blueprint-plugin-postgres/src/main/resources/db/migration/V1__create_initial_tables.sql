create table events (
  id        serial,
  timestamp timestamptz,
  username  text,
  type      text,
  contents  jsonb
);

create table blobs (
  id       serial,
  path     text,
  contents bytea
);