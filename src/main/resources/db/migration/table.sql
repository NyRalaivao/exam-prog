CREATE TABLE IF NOT EXISTS images (
                                      id UUID PRIMARY KEY,
                                      nom_fichier VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT now()
    );