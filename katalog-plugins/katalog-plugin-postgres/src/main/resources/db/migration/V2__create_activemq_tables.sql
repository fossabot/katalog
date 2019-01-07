create table activemq_msgs
(
	id bigint not null
		constraint activemq_msgs_pkey
			primary key,
	container varchar(250) not null,
	msgid_prod varchar(250),
	msgid_seq bigint,
	expiration bigint,
	msg bytea,
	priority bigint,
	xid varchar(250)
);

alter table activemq_msgs owner to katalog;

create index activemq_msgs_midx
	on activemq_msgs (msgid_prod, msgid_seq);

create index activemq_msgs_cidx
	on activemq_msgs (container);

create index activemq_msgs_eidx
	on activemq_msgs (expiration);

create index activemq_msgs_pidx
	on activemq_msgs (priority);

create index activemq_msgs_xidx
	on activemq_msgs (xid);

create table activemq_acks
(
	container varchar(250) not null,
	sub_dest varchar(250),
	client_id varchar(250) not null,
	sub_name varchar(250) not null,
	selector varchar(250),
	last_acked_id bigint,
	priority bigint default 5 not null,
	xid varchar(250),
	constraint activemq_acks_pkey
		primary key (container, client_id, sub_name, priority)
);

alter table activemq_acks owner to katalog;

create index activemq_acks_xidx
	on activemq_acks (xid);

create table activemq_lock
(
	id bigint not null
		constraint activemq_lock_pkey
			primary key,
	time bigint,
	broker_name varchar(250)
);

alter table activemq_lock owner to katalog;