create table events (
  id       serial,
  metadata jsonb,
  type     text,
  contents jsonb
);

create table blobs (
  id       serial,
  path     text,
  contents bytea
);