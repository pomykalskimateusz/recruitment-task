create table coupon
(
    id                      uuid primary key,
    code                    varchar not null,
    code_normalized         varchar not null,
    usage_limit             integer not null,
    country                 CHAR(2) not null,
    version                 integer not null,
    created_date_timestamp  timestamp default current_timestamp,
    modified_date_timestamp timestamp
);

CREATE UNIQUE INDEX unique_coupon_code ON coupon (code_normalized);

create table coupon_usage
(
    id                      uuid primary key,
    coupon_id               uuid references coupon (id),
    user_id                 uuid not null,
    version                 integer not null,
    created_date_timestamp  timestamp default current_timestamp,
    modified_date_timestamp timestamp
);

CREATE UNIQUE INDEX unique_coupon_usage ON coupon_usage (coupon_id, user_id);
