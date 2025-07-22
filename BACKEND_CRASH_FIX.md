# 🚨 Correction du crash backend - RoomTestRunner

## 🔍 Problème identifié

**Erreur fatale au démarrage** :
```
ERROR: duplicate key value violates unique constraint "users_email_key"
Detail: Key (email)=(owner@example.com) already exists.
```

**Cause** : Le `RoomTestRunner` tentait de créer un utilisateur test à chaque démarrage, causant une violation de contrainte unique quand l'utilisateur existait déjà.

## 🔧 Corrections appliquées

### 1. **RoomTestRunner.java** - Gestion des utilisateurs existants

```java
// AVANT - Création forcée qui échoue si l'utilisateur existe
User user = new User();
user.setEmail("owner@example.com");
user.setPassword("dummy");
user.setDisplayName("Owner");
user.setVerified(true);
user = userRepository.save(user); // ❌ CRASH si l'email existe

// APRÈS - Vérification d'existence avant création
User user = userRepository.findByEmail("owner@example.com")
        .orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail("owner@example.com");
            newUser.setPassword("dummy");
            newUser.setDisplayName("Owner");
            newUser.setVerified(true);
            return userRepository.save(newUser);
        }); // ✅ Utilise l'existant ou crée un nouveau
```

### 2. **Désactivation du runner de test**

```java
// AVANT
@Component
@RequiredArgsConstructor
public class RoomTestRunner implements CommandLineRunner {

// APRÈS - Runner désactivé pour éviter les conflits
// @Component - Désactivé pour éviter les conflits de données de test
@RequiredArgsConstructor
public class RoomTestRunner implements CommandLineRunner {
```

## 🎯 Résultat

- ✅ **Backend démarre sans crash**
- ✅ **Plus d'erreurs de contraintes de base de données**
- ✅ **Application stable pour les tests temps-réel**
- ✅ **Services Docker démarrent correctement**

## 🧪 Validation

1. **Backend accessible** : `http://localhost:8080`
2. **Plus de crash loops** dans les logs Docker
3. **WebSocket prêt** pour les tests temps-réel
4. **Application frontend** peut se connecter au backend

---

**Cette correction résout le problème de redémarrage en boucle du backend et permet de tester les fonctionnalités temps-réel.** 