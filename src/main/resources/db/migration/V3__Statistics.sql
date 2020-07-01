CREATE TABLE statistics (
    period_start    TIMESTAMP   NOT NULL    PRIMARY KEY,
    period_end      TIMESTAMP   NOT NULL,
    period_hours    DOUBLE      NOT NULL,
    charge_hours    DOUBLE      NOT NULL,
    avg_price       DOUBLE      NOT NULL,
    avg_price_unopt DOUBLE      NOT NULL
);
