-- drop tables if they exist
drop table if exists user_interests;
drop table if exists fire_reports;
drop table if exists users;
drop table if exists regions;

-- create regions table
create table regions (
    district_id varchar(50) primary key,
    district_name varchar(100) not null,
    province_name varchar(100) not null,
    constraint uk_region_district unique (province_name, district_name)
);

-- create users table
create table users (
    id bigserial primary key,
    email varchar(100) not null,
    password varchar(100) not null,
    name varchar(100) not null,
    constraint uk_user_email unique (email)
);

-- create user_interests table
create table user_interests (
    id bigserial primary key,
    user_id bigint not null,
    district_id varchar(50) not null,
    constraint fk_user_interests_user foreign key (user_id) references users(id) on delete cascade,
    constraint fk_user_interests_region foreign key (district_id) references regions(district_id) on delete cascade,
    constraint uk_user_district unique (user_id, district_id)
);

-- create fire_reports table
create table fire_reports (
    id bigserial primary key,
    occurred_at timestamp not null,
    district_id varchar(50) not null,
    fire_type varchar(50),
    ignition_cause_main varchar(100),
    ignition_cause_sub varchar(100),
    casualty_total integer default 0,
    death integer default 0,
    injury integer default 0,
    property_damage_total bigint default 0,
    real_estate bigint default 0,
    personal_property bigint default 0,
    location_category_main varchar(100),
    location_category_middle varchar(100),
    location_category_sub varchar(100),
    constraint fk_fire_reports_region foreign key (district_id) references regions(district_id) on delete restrict
);

-- create indexes for better query performance
create index idx_fire_reports_occurred_at on fire_reports(occurred_at);
create index idx_fire_reports_district on fire_reports(district_id);
create index idx_regions_province on regions(province_name);
create index idx_user_interests_district on user_interests(district_id);
create index idx_user_interests_user on user_interests(user_id);

-- temporary table for importing district_id
create table staging_fire_reports (
    occurred_at timestamp,
    province_name varchar(50),
    district_name varchar(50),
    fire_type varchar(100),
    ignition_cause_main varchar(100),
    ignition_cause_sub varchar(100),
    casualty_total int,
    death int,
    injury int,
    property_damage_total int,
    real_estate int,
    personal_property int,
    location_category_main varchar(100),
    location_category_middle varchar(100),
    location_category_sub varchar(100)
);

-- JOIN
insert into fire_reports (
    district_id, occurred_at, fire_type, ignition_cause_main, ignition_cause_sub, 
    casualty_total, death, injury, property_damage_total, real_estate, personal_property,
    location_category_main, location_category_middle, location_category_sub
)
select 
    r.district_id, s.occurred_at, s.fire_type, s.ignition_cause_main, s.ignition_cause_sub,
    s.casualty_total, s.death, s.injury, s.property_damage_total, s.real_estate, s.personal_property,
    s.location_category_main, s.location_category_middle, s.location_category_sub
from staging_fire_reports s
join regions r 
    on s.province_name = r.province_name 
    and s.district_name = r.district_name;

-- drop the temporary table
drop table staging_fire_reports;
alter table fire_reports drop column personal_property_damage, real_estate_damage cascade;

 select * from fire_reports;
 