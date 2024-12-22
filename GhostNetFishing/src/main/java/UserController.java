import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.FacesException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import javax.annotation.ManagedBean;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Named
@RequestScoped
@ManagedBean
public class UserController implements Serializable {
    private final SecureRandom _random = new SecureRandom();
    private final EntityManager _entityManager;

    private String generateSalt() {
        byte[] salt = new byte[16];
        _random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String name, String pass, String salt) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digester.digest((name + pass + salt)
                    .getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(hashBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<AppUser> getInitialUsers() {
        // Reporter user
        String reporterFirstName = "Reporter";
        String reporterLastName = "User";
        String reporterUsername = "reporter";
        String reporterPassword = "secret";
        String reporterSalt = generateSalt();
        // Recover user
        String recoverFirstName = "Recover";
        String recoverLastName = "User";
        String recoverUsername = "recover";
        String recoverPassword = "secret";
        String recoverSalt = generateSalt();
        String recoverPhoneNumber = "+1234567890";

        return Arrays.asList(new AppUser[]{
                new AppUser(reporterFirstName, reporterLastName, reporterUsername, hashPassword(reporterUsername, reporterPassword, reporterSalt), reporterSalt),
                new AppUser(recoverFirstName, recoverLastName, recoverUsername, hashPassword(recoverUsername, recoverPassword, recoverSalt), recoverSalt, recoverPhoneNumber),
        });
    }

    private AppUser getUser(String username) {
        List<AppUser> appUsers = _entityManager.createQuery("SELECT u FROM AppUser u WHERE u.username = :username", AppUser.class)
                .setParameter("username", username)
                .getResultList();

        if (appUsers.isEmpty()) {
            throw new FacesException("User " + username + " not found");
        }

        return appUsers.get(0);
    }

    public void validateUsernameAndPassword(CurrentUser currentUser, String username, String pass) {
        currentUser.reset();

        try {
            AppUser appUser = getUser(username);
            String passHash = hashPassword(username, pass, appUser.getSalt());

            if (!appUser.getPassword().equals(passHash)) {
                return;
            }

            currentUser.setUser(appUser);
            if (appUser.getPhoneNumber() == null || appUser.getPhoneNumber().isEmpty()) {
                currentUser.setReporter(true);
            } else {
                currentUser.setRecover(true);
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }

    public boolean isUsernameTaken(String username) {
        try {
            getUser(username);
            System.out.println("User " + username + " is taken");
            return true;
        } catch (FacesException e) {
            return false;
        }
    }

    public void registerUser(String firstName, String lastName, String username, String password, String phoneNumber) {
        EntityTransaction transaction = _entityManager.getTransaction();
        transaction.begin();
        String salt = generateSalt();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            _entityManager.persist(new AppUser(firstName, lastName, username, hashPassword(username, password, salt), salt));
        } else {
            _entityManager.persist(new AppUser(firstName, lastName, username, hashPassword(username, password, salt), salt, phoneNumber));
        }
        transaction.commit();
    }

    public UserController() {
        _entityManager = Persistence.createEntityManagerFactory("ghostnet").createEntityManager();
        List<AppUser> users = _entityManager.createQuery("SELECT u FROM AppUser u").getResultList();
        System.out.println("count: " + users.toArray().length);
        if (users.toArray().length == 0) {
            EntityTransaction transaction = _entityManager.getTransaction();
            transaction.begin();
            for (AppUser u : getInitialUsers()) {
                _entityManager.persist(u);
            }
            transaction.commit();
        }
    }
}