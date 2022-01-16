DROP TABLE IF EXISTS Task;
DROP TABLE IF EXISTS Etat;
DROP TABLE IF EXISTS Fichier;
DROP TABLE IF EXISTS FileType;
DROP TABLE IF EXISTS Personne;
DROP TABLE IF EXISTS Indice;

DROP FUNCTION IF EXISTS CleanExpiredSession;

CREATE TABLE Personne(
	IdPersonne SERIAL PRIMARY KEY,
	IdLogin char(8) UNIQUE NOT NULL,
	DtCrea TIMESTAMP NOT NULL,
	DtModif TIMESTAMP
);

CREATE UNIQUE INDEX personne_IdLogin On Personne(idLogin);

CREATE TABLE FileType(
	IdFileType SERIAL PRIMARY KEY,
	NomFileType varchar(20) NOT NULL
);

CREATE TABLE Fichier(
	IdFichier SERIAL PRIMARY KEY,
	FileName VARCHAR(255) NOT NULL,
	DtCrea TIMESTAMP NOT NULL,
	FK_IdFileType int NOT NULL REFERENCES FileType(IdFileType),
	FK_IdPersonne int NOT NULL REFERENCES Personne(IdPersonne)
);

CREATE TABLE Etat(
	IdEtat SERIAL PRIMARY KEY,
	NomEtat VARCHAR(20) NOT NULL
);


CREATE TABLE Task(
	IdTask SERIAL PRIMARY KEY,
	FK_IdEtat int NOT NULL REFERENCES Etat(IdEtat),
	DtLaunched TIMESTAMP NOT NULL,
	DtModif TIMESTAMP NOT NULL,
	FK_IdPersonne int REFERENCES Personne(IdPersonne)
);


CREATE TABLE Indice (
	IdIndice SERIAL PRIMARY KEY,
	NomIndice varchar(50) NOT NULL,
	Unite varchar(8) NOT NULL
);

CREATE FUNCTION CleanExpiredSession()
RETURNS integer
AS $$
DECLARE
    --result text;
BEGIN
/*
    result := '';
    FOR i IN 1..times LOOP
        result := result || s;
    END LOOP;
    RETURN result;
*/

END;
$$
LANGUAGE plpgsql;

---- AJOUT DES VALEURS STATIQUES ----

-- FileType
INSERT INTO FileType (NomFileType) VALUES ('timeline');
INSERT INTO FileType (NomFileType) VALUES ('json');
INSERT INTO FileType (NomFileType) VALUES ('pdf');


-- Indices
INSERT INTO Indice (NomIndice,Unite) VALUES ('dioxyde d’azote','NO2');
INSERT INTO Indice (NomIndice,Unite) VALUES ('dioxyde de soufre','SO2');
INSERT INTO Indice (NomIndice,Unite) VALUES ('ozone','O3');
INSERT INTO Indice (NomIndice,Unite) VALUES ('particules de diamètre inférieur à 10 micromètres','PM10');
INSERT INTO Indice (NomIndice,Unite) VALUES ('particules de diamètre inférieur à 2,5 micromètres','PM2,5');

