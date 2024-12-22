import jakarta.annotation.ManagedBean;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
@ManagedBean
public class CurrentUser implements Serializable {
    private boolean reporter;

    private boolean recover;

    private AppUser user;

    public CurrentUser() {
        reporter = false;
        recover = false;
        user = null;
    }

    public AppUser getUser() {return user;}

    public void setUser(AppUser user) {this.user = user;}

    public boolean getReporter() {return reporter;}

    public void setReporter(boolean reporter) {this.reporter = reporter;}

    public boolean getRecover() {return recover;}

    public void setRecover(boolean recover) {this.recover = recover;}

    public void reset() {
        reporter = false; recover = false;
        user = null;
    }

    public boolean isValid() {
        return getReporter() || getRecover();
    }
}
