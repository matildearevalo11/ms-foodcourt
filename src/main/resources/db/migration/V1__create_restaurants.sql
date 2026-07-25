CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    nit VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(13) NOT NULL,
    url_logo VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT ck_restaurant_nit_numeric CHECK (nit ~ '^[0-9]+$'),
    CONSTRAINT ck_restaurant_phone CHECK (phone ~ '^\+?[0-9]+$'),
    CONSTRAINT ck_restaurant_name_not_numeric CHECK (name !~ '^[0-9]+$')
);
