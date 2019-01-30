create table events (
  id        serial,
  timestamp timestamptz,
  userId    text,
  type      text,
  contents  jsonb
);

create table blobs (
  id       serial,
  path     text,
  contents bytea
);