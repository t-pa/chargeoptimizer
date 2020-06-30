CREATE TABLE chargelog (
  time             TIMESTAMP  NOT NULL  PRIMARY KEY,
  carconnected     BOOLEAN    NOT NULL,
  charging         BOOLEAN    NOT NULL,
  chargingAllowed  BOOLEAN    NOT NULL,
  price            DOUBLE     NOT NULL
);
