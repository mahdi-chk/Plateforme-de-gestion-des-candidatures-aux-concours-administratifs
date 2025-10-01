# 📌 Plateforme de Gestion des Candidatures aux Concours Administratifs

## 📖 Description
Cette application est une plateforme web permettant la gestion complète des candidatures aux concours administratifs.  
Elle offre un espace pour les **candidats** et plusieurs interfaces pour les **administrateurs, gestionnaires globaux et locaux**, facilitant le suivi et le traitement des dossiers.

Développée avec **Spring Boot**, **Spring Security (JWT)**, **Thymeleaf** et **Maven**, elle intègre des fonctionnalités avancées telles que la gestion des utilisateurs, l’authentification, les notifications, les statistiques et l’export des données.

---

## 🚀 Fonctionnalités principales
- ✅ Authentification et gestion des rôles (**Candidat, Admin, Gestionnaire Global, Gestionnaire Local**)
- 📑 Gestion des candidatures (dépôt, suivi, validation, traitement)
- 🏛️ Gestion des concours, spécialités, centres d’examen et villes
- 📂 Gestion et stockage de documents
- ✉️ Notifications aux candidats
- 📊 Tableaux de bord et statistiques
- 📤 Exportation de rapports (PDF/Excel)
- 🔐 Sécurité basée sur **JWT**

---

## 🛠️ Technologies utilisées
- **Back-end** : Spring Boot, Spring Security (JWT), Spring Data JPA, Hibernate  
- **Base de données** : MySQL / PostgreSQL  
- **Front-end** : Thymeleaf, HTML5, CSS3, JavaScript  
- **Outils de build** : Maven  
- **Gestion des versions** : Git / GitHub  

---

## 📂 Structure du projet
```bash
gestion-candidatures/
 ┣ src/main/java/com/concours/
 ┃ ┣ config/              # Configuration (sécurité, CORS, upload, etc.)
 ┃ ┣ controller/          # Contrôleurs (Admin, Auth, Concours, Candidatures…)
 ┃ ┣ dto/                 # Data Transfer Objects
 ┃ ┣ entity/              # Entités JPA
 ┃ ┣ exception/           # Gestion des exceptions
 ┃ ┣ mapper/              # Mapping entre entités et DTOs
 ┃ ┣ repository/          # Interfaces de persistance (Spring Data JPA)
 ┃ ┣ security/            # JWT et sécurité
 ┃ ┣ service/             # Logique métier
 ┃ ┣ util/                # Utilitaires
 ┃ ┗ GestionCandidaturesApplication.java  # Classe principale
 ┣ src/main/resources/
 ┃ ┣ db/migration/        # Scripts Flyway (migrations DB)
 ┃ ┣ templates/           # Vues Thymeleaf (admin, public, gestionnaires…)
 ┃ ┣ application.properties
 ┃ ┗ static/              # Fichiers statiques (images, CSS, JS)
 ┗ pom.xml                # Dépendances Maven


⚙️ Installation et exécution
1️⃣ Prérequis
Java 17+

Maven 3.8+

MySQL  installé

Git

2️⃣ Cloner le projet

git clone https://github.com/mahdi-chk/Plateforme-de-gestion-des-candidatures-aux-concours-administratifs.git
cd Plateforme-de-gestion-des-candidatures-aux-concours-administratifs

3️⃣ Configurer la base de données
Éditer src/main/resources/application.properties :

properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_candidatures
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true


4️⃣ Lancer l'application

mvn spring-boot:run
👉 L'application sera accessible sur : http://localhost:8080

👥 Rôles utilisateurs et accès démo
Candidat : S'inscrire, déposer et suivre ses candidatures

Gestionnaire Local : Gérer les candidatures au niveau d'un centre

Gestionnaire Global : Superviser plusieurs centres

Administrateur : Gérer les concours, utilisateurs, statistiques globales

🔑 Comptes de démonstration :
Admin → admin@mef.gov.ma / admin123

Gestionnaire Global → gestionnaire.global@mef.gov.ma / gest123

Gestionnaire Local → gestionnaire.local@mef.gov.ma / local123

🧪 Tests
Lancer les tests avec :


mvn test
📌 Auteur
👤 Mahdi Chakouch
🔗 GitHub : mahdi-chk
