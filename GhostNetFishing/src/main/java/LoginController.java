import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.regex.Pattern;

@Named
@ViewScoped
public class LoginController implements Serializable {
    @Inject
    private CurrentUser currentUser;

    private String username, password;
    private String firstName, lastName, phoneNumber;

    private String failureMessage = "";

    private final Pattern ONE_DIGIT_PATTERN = Pattern.compile("([0-9]+)");
    private final Pattern LOWER_CASE_PATTERN = Pattern.compile("([a-z]+)");
    private final Pattern UPPER_CASE_PATTERN = Pattern.compile("([A-Z]+)");
    private final Pattern PHONE_REGEX = Pattern.compile("(\\+*\\d{8,12})");

    private void resetValues() {
        this.username = "";
        this.password = "";
        this.firstName = "";
        this.lastName = "";
        this.phoneNumber = "";
        this.failureMessage = "";
    }

    public LoginController() {
        resetValues();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() { return this.phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }


    // this method should be called early to avoid providing information if the user is not logged in
    public void checkLogin() {
        failureMessage = "";
        if(!currentUser.isValid()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            NavigationHandler nh = fc.getApplication().getNavigationHandler();
            nh.handleNavigation(fc, null, "login.xhtml?faces-redirect=true");
        }
    }

    // this method should be called early to avoid providing information if the user is not logged in
    public void checkAlreadyLoggedIn() {
        failureMessage = "";
        if(currentUser.isValid()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            NavigationHandler nh = fc.getApplication().getNavigationHandler();
            nh.handleNavigation(fc, null, "ghostnets.xhtml?faces-redirect=true");
        }

    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        resetValues();
        return "login.xhtml?faces-redirect=true";
    }

    public void postValidateUser(ComponentSystemEvent ev) {
        UIInput temp = (UIInput) ev.getComponent();
        this.username = (String) temp.getValue();
    }

    public void validateLogin(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String password = (String) value;
        UserController userController = new UserController();
        userController.validateUsernameAndPassword(currentUser, username, password);
        if (!currentUser.isValid())
            throw new ValidatorException(new FacesMessage("Benutzername oder Passwort sind falsch, oder existieren nicht."));
    }

    public String login() {
        if (currentUser.getRecover()) {
            this.failureMessage = "";
            return "ghostnets.xhtml?faces-redirect=true";
        } else if (currentUser.getReporter()) {
            this.failureMessage = "";
            return "ghostnets.xhtml?faces-redirect=true";
        } else {
            this.failureMessage = "Passwort und Benutzername nicht erkannt.";
            return "";
        }
    }

    public void validateUsername(FacesContext context, UIComponent component, Object value) {
        String usernameInput = (String) value;
        UserController userController = new UserController();

        boolean isUsernameTaken = userController.isUsernameTaken(usernameInput);
        if (isUsernameTaken) {
            throw new ValidatorException(new FacesMessage("Der Benutzername existiert bereits."));
        }
    }

    public void validatePhoneNumber(FacesContext context, UIComponent component, Object value) {
        String phoneNumberInput = (String) value;

        if (phoneNumberInput != null && !phoneNumberInput.isEmpty() && (UPPER_CASE_PATTERN.matcher(phoneNumberInput).find() || LOWER_CASE_PATTERN.matcher(phoneNumberInput).find() || !PHONE_REGEX.matcher(phoneNumberInput).find())) {
            throw new ValidatorException(new FacesMessage("Die eingegebene Nummer ist nicht valide."));
        } else {
            this.phoneNumber = phoneNumberInput;
        }

    }

    public void validatePassword(FacesContext context, UIComponent component, Object value) {
        String passwordInput = (String) value;

        if (passwordInput.length() < 8 || !ONE_DIGIT_PATTERN.matcher(passwordInput).find()  || !UPPER_CASE_PATTERN.matcher(passwordInput).find() || !LOWER_CASE_PATTERN.matcher(passwordInput).find()) {
            throw new ValidatorException(new FacesMessage("Das Passwort muss mindestens 8 Zeichen besitzen, einen Gross- und Kleinbuchstaben sowie eine Zahl"));
        }
    }

    public String register() {
        UserController userController = new UserController();
        userController.registerUser(firstName, lastName, username, password, phoneNumber);
        resetValues();
        return "login.xhtml?faces-redirect=true";
    }

}