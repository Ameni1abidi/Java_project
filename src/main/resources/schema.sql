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
    is_sensitive TINYINT(1) NOT NULL DEFAULT 0,
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

CREATE TABLE IF NOT EXISTS ressource_interaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ressource_id INT NOT NULL,
    user_id INT NULL,
    interaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ressource_interaction_resource (ressource_id),
    INDEX idx_ressource_interaction_type (interaction_type),
    CONSTRAINT fk_interaction_ressource
        FOREIGN KEY (ressource_id) REFERENCES ressource(id)
        ON DELETE CASCADE
);
