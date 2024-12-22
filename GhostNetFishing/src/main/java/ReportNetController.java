import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.annotation.ManagedBean;
import java.io.Serializable;
import java.util.regex.Pattern;

@Named
@RequestScoped
@ManagedBean
public class ReportNetController implements Serializable {
    @Inject
    private CurrentUser currentUser;

    private String _longitude, _latitude, _width, _length;

    private final Pattern NUMBER_REGEX = Pattern.compile("^\\d+(\\.\\d+)?$");

    public ReportNetController() {}

    public String getLatitude() {
        return _latitude;
    }

    public void setLatitude(String latitude) {
        _latitude = latitude;
    }

    public String getLongitude() {
        return _longitude;
    }

    public void setLongitude(String longitude) {
        _longitude = longitude;
    }

    public String getWidth() {
        return _width;
    }

    public void setWidth(String width) {
        _width = width;
    }

    public String getLength() {
        return _length;
    }

    public void setLength(String length) {
        _length = length;
    }

    public String reportGhostNet() {
        GhostNetController gn = new GhostNetController();
        gn.reportGhostNet(currentUser, _latitude + "°", _longitude + "°", _width, _length);

        return "ghostnets.xhtml?faces-redirect=true";
    }

    public void validateNumber(FacesContext context, UIComponent component, Object value) {
        String numberInput = (String) value;

        if (numberInput.isEmpty() || !NUMBER_REGEX.matcher(numberInput).find()) {
            throw new ValidatorException(new FacesMessage("Die Eingabe muss eine Natürliche Zahl oder Gleitkommazahl mit der Punkt (.) schreibweise sein."));
        }
    }
}