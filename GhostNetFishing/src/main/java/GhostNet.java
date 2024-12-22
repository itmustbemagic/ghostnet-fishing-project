import jakarta.persistence.*;

@Entity
public class GhostNet {
    @Id
    @GeneratedValue
    private int id;

    // Latitude of net location
    private String latitude;

    // Longitude of net location
    private String longitude;

    // Length of net
    private String length;

    // Width of net
    private String width;

    @ManyToOne
    @JoinColumn(name="recover_id", nullable=true)
    private AppUser recoverUser;

    /*
     * Status could be one of the following values:
     * Gemeldet
     * Bergung bevorstehend
     * Geborgen
     * Verschollen
     */
    private String status;

    public GhostNet() {}

    public GhostNet(String latitude, String longitude, String length, String width) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.length = length;
        this.width = width;
        this.status = "Gemeldet";
    }

    public int getId() {
        return this.id;
    }

    public String getStatus() {
        return this.status;
    }

    public String getLength() {
        return this.length;
    }

    public String getWidth() {
        return this.width;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public AppUser getRecoverUser() {
        return this.recoverUser;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRecoverUser(AppUser recoverUser) {
        this.recoverUser = recoverUser;
    }
}
