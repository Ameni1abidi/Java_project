CREATE TABLE IF NOT EXISTS categorie (
    nom VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS ressource (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    categorie_nom VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    disponible_le VARCHAR(100),
    chapitre_id INT NULL,
    CONSTRAINT fk_ressource_categorie
        FOREIGN KEY (categorie_nom) REFERENCES categorie(nom)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS ressource_favori (
    user_id INT NOT NULL,
    ressource_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, ressource_id),
    CONSTRAINT fk_fav_ressource FOREIGN KEY (ressource_id) REFERENCES ressource(id) ON DELETE CASCADE
);
