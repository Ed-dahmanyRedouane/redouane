# BookShop API 📚

API REST de boutique de livres en ligne — Spring Boot 3 + JWT + Docker

---

## Informations Équipe & Serveur

| Champ              | Valeur                                                 |
|--------------------|--------------------------------------------------------|
| Chef de projet     | Redouane                                               |
| Utilisateur Linux  | `redouane`                                             |
| Dossier de travail | `/home/redouane/bookshop`                              |
| Repo GitHub        | `https://github.com/Ed-dahmanyRedouane/redouane`       |
| Serveur            | `37.27.214.35`                                         |

---

## Commandes utilisées (copier-coller)

### Création utilisateur Linux

```bash
# Connecté en tant que admin (compte fourni par le professeur)
sudo useradd -m -s /bin/bash redouane
sudo passwd redouane
sudo usermod -aG sudo redouane
```

### Création dossier de travail

```bash
sudo mkdir -p /home/redouane/bookshop
sudo chown -R redouane:redouane /home/redouane/bookshop
```

### Clone du dépôt

```bash
cd /home/redouane/bookshop
git clone https://github.com/Ed-dahmanyRedouane/redouane.git .
```

### Création base de données MySQL

```bash
mysql -u root -p1111 -e "
  CREATE DATABASE IF NOT EXISTS bookshop_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  GRANT ALL PRIVILEGES ON bookshop_db.* TO 'root'@'172.17.0.1'
    IDENTIFIED BY '1111';
  FLUSH PRIVILEGES;
"
```

### Démarrage avec Docker Compose

```bash
cd /home/redouane/bookshop

# Créer le fichier .env avec les secrets
cat > .env << 'EOF'
MYSQL_PASSWORD=1111
JWT_SECRET=8f2e9c1b4a7d6e5f8a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f
EOF

docker compose up -d --build
docker compose ps
```

### Vérification santé

```bash
curl http://37.27.214.35:8080/actuator/health
```

---

## Architecture & Stack

- **Framework** : Spring Boot 3.3
- **Sécurité** : Spring Security + JWT (jjwt 0.12.5)
- **BDD** : MySQL 8 (préinstallé sur serveur)
- **Containerisation** : Docker + Docker Compose
- **CI/CD** : GitHub Actions (3 jobs : build → docker → deploy)
- **Java** : 21 (LTS)
- **Documentation** : Swagger / OpenAPI 3

---

## API Endpoints

### Public (sans JWT)

| Méthode | Route                               | Description                |
|---------|-------------------------------------|----------------------------|
| GET     | `/api/public/categories`            | Liste des catégories       |
| GET     | `/api/public/books?page=0&size=10`  | Liste paginée des livres   |
| GET     | `/api/public/books/{id}`            | Détail d'un livre          |

### Auth

| Méthode | Route              | Description              |
|---------|--------------------|--------------------------|
| POST    | `/api/auth/login`  | Connexion → retourne JWT |

### Panier (JWT requis)

| Méthode | Route                  | Description             |
|---------|------------------------|-------------------------|
| GET     | `/api/cart`            | Consulter le panier     |
| POST    | `/api/cart/items`      | Ajouter un livre        |
| PUT     | `/api/cart/items/{id}` | Modifier la quantité    |
| DELETE  | `/api/cart/items/{id}` | Supprimer un item       |

### Admin (JWT + rôle ADMIN)

| Méthode | Route                   | Description         |
|---------|-------------------------|---------------------|
| POST    | `/api/admin/books`      | Ajouter un livre    |
| DELETE  | `/api/admin/books/{id}` | Supprimer un livre  |

### Swagger UI

| URL                                          | Description                |
|----------------------------------------------|----------------------------|
| `http://37.27.214.35:8080/swagger-ui/index.html` | Documentation interactive  |

---

## Exemples de tests rapides

### Login admin

```bash
curl -X POST http://37.27.214.35:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookshop.com","password":"admin123"}'
```

### Lister les livres (public)

```bash
curl "http://37.27.214.35:8080/api/public/books?page=0&size=5"
```

### Ajouter au panier (avec JWT)

```bash
curl -X POST http://37.27.214.35:8080/api/cart/items \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "quantity": 2}'
```

---

## Comptes pré-seedés

| Email                | Mot de passe | Rôle  |
|----------------------|-------------|-------|
| `admin@bookshop.com` | `admin123`  | ADMIN |
| `user@bookshop.com`  | `user123`   | USER  |

---

## Structure du dépôt

```
bookshop/
├── src/main/java/com/bookshop/
│   ├── config/          # SecurityConfig, DataInitializer, OpenApiConfig
│   ├── controller/      # AuthController, PublicController, CartController, AdminController
│   ├── dto/             # LoginRequest, BookRequest, CartItemRequest...
│   ├── entity/          # Book, Category, User, CartItem
│   ├── repository/      # Interfaces Spring Data JPA
│   ├── security/        # JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
│   └── service/         # BookService, CartService, AuthService
├── src/test/java/       # 22 integration tests (Auth, Public, Admin, Cart)
├── .github/workflows/deploy.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```
