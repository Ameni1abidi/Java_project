CREATE TABLE IF NOT EXISTS categorie (
    nom VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS ressource (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    categorie_nom VARCHAR(255) NOT NULL,
    CONSTRAINT fk_ressource_categorie
        FOREIGN KEY (categorie_nom) REFERENCES categorie(nom)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);
