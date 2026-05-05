drop table if exists hm_message;
drop table if exists user_friend;
drop table if exists chat_member;
drop table if exists chat;
drop table if exists hm_user;

create type status_enm as enum (
  'incoming', --user2 to user1
  'outgoing', --user1 to user2
  'accepted'
);

create type role_enum as enum(
	'admin',
	'creator',
	'member'
);

create type mess_enum as enum(
	'regular',
	'system',
	'join',
	'leave'
);

-- === USER ===
create table hm_user (
	username varchar(64) NOT NULL,
		CONSTRAINT pk_user PRIMARY KEY (username),
	--display name
	pass varchar(128) NOT NULL,
	email varchar(128) 
);

INSERT INTO hm_user values('linktronus', 'ciscoconpa55');
INSERT INTO hm_user values('cozomin', 'ciscovtypa55');
select * from hm_user;

create table user_friend(
	user1 varchar(64),
		CONSTRAINT fk_user_friend1 FOREIGN KEY (user1) 
			references hm_user(username),
	user2 varchar(64),
		CONSTRAINT fk_user_friend2 FOREIGN KEY (user2) 
			references hm_user(username),
	status status_enm NOT NULL,
	CONSTRAINT pk_user_friend PRIMARY KEY (user1, user2)
);

create table chat(
	chatID bigserial,
		CONSTRAINT pk_chatID PRIMARY KEY (chatID),
	chatName varchar(128),
	isGroup bool NOT NULL,
	updated_at timestamptz --initial data crearii
);

insert into chat values(default, default, false);
select * from chat;


create table chat_member(
	chatID bigint,
		CONSTRAINT pk_chat_asoc FOREIGN KEY (chatID) 
			references chat(chatID),
	memberID varchar(64),
		CONSTRAINT fk_memberID FOREIGN KEY (memberID) 
			references hm_user(username),
	hm_role role_enum NOT NULL,
	last_access timestamptz, --NN?
	CONSTRAINT pk_chat_member PRIMARY KEY (chatID, memberID)
);

insert into chat_member values(1, 'linktronus', 'member');
insert into chat_member values(1, 'cozomin', 'member');
select * from chat_member;

create table hm_message(
	messID bigserial,
		CONSTRAINT pk_messID PRIMARY KEY (messID),
	senderID varchar(64),
		CONSTRAINT fk_senderID FOREIGN KEY (senderID)
			references hm_user(username),
	chatID bigint,
		CONSTRAINT fk_mess_chatID FOREIGN KEY (chatID)
			references chat(chatID),
	message_content text,
	sent_at timestamptz NOT NULL,
	message_type mess_enum NOT NULL DEFAULT 'regular'
);

insert into hm_message values (default, 'linktronus', 1, 'salut', (SELECT NOW()::TIMESTAMPTZ AS current_time) );


