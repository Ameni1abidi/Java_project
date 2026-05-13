# Plateforme de Gestion Educative

## Description
Cette application permet de gérer une plateforme éducative à travers une application web Symfony et une application desktop JavaFX connectées à une base de données MySQL.

## Technologies utilisées
- Symfony
- JavaFX
- MySQL
- PHP
- Java
- CSS

## Fonctionnalités
- Authentification des utilisateurs
- Gestion des cours
- Gestion des catégories
- gestion Evaluation
- Gestion des ressources pédagogiques
- Forum de discussion
- Gestion des examens

## Installation

### Partie Web Symfony
1. Installer les dépendances :
   composer install

2. Lancer le serveur :
   symfony server:start

### Partie Desktop JavaFX
1. Ouvrir le projet dans IntelliJ
2. Configurer JavaFX SDK
3. Exécuter le projet

## Base de données
- MySQL
- phpMyAdmin

## 🌐 APIs & Services externes utilisés

L’application intègre plusieurs APIs et services externes pour enrichir les fonctionnalités du système éducatif.

| API / Service | Utilisation | Lien / Endpoint |
|---------------|-------------|------------------|
| Open-Meteo Forecast API | Météo du jour à Tunis (WeatherService.php) | https://api.open-meteo.com/v1/forecast |
| Ollama API (local) | Génération de résumé, QCM et chatbot IA (modèle phi) | http://127.0.0.1:11434/api/generate |
| Google Translate (stichoza/google-translate-php) | Traduction des cours et chapitres | https://github.com/Stichoza/google-translate-php |
| Symfony Mailer / SMTP | Envoi d’e-mails aux étudiants et tests | smtp://127.0.0.1:1025 |
| MySQL via Doctrine DBAL | Base de données principale (eduflex) | mysql://root:@127.0.0.1:3306/eduflex |
| Google Fonts API | Chargement de la police Poppins dans le design | https://fonts.googleapis.com/css2?family=Poppins |

---

## 📌 Objectif des intégrations

Ces services permettent de :
- Améliorer l’expérience utilisateur (météo, traduction)
- Ajouter des fonctionnalités intelligentes (IA via Ollama)
- Gérer la communication (emails)
- Structurer les données (MySQL)
- Améliorer le design (Google Fonts)

## Auteurs
- Assil Daassi
- Ameni Abidi
- Mariem Rahmouni
- Mohamed Aziz Abbes
- Souha eljani

## Mots clés
symfony, javafx, mysql, education, desktop-app, web-app