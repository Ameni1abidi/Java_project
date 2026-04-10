CREATE TABLE IF NOT EXISTS categorie (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ressource (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    categorie_id INT NOT NULL,
    CONSTRAINT fk_ressource_categorie
        FOREIGN KEY (categorie_id) REFERENCES categorie(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);
