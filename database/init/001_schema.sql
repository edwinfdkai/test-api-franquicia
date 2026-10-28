USE franchise_db;

CREATE TABLE franchises (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE branches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    franchise_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_branches_franchise
        FOREIGN KEY (franchise_id) REFERENCES franchises (id)
) ENGINE=InnoDB;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    stock INT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_branch
        FOREIGN KEY (branch_id) REFERENCES branches (id)
) ENGINE=InnoDB;
