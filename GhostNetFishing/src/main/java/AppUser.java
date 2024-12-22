import jakarta.persistence.*;

import java.io.Serializable;


@Entity
public class AppUser implements Serializable
{
    @Id
    @GeneratedValue
    private int id;

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private String salt;

    private String phoneNumber;

    public AppUser() {}

    public AppUser(String firstName, String lastName, String username, String password, String salt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    public AppUser(String firstName, String lastName, String username, String password, String salt, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.salt = salt;
    }

    public int getId()
    {
        return this.id;
    }

    public String getLastName()
    {
    	return this.lastName;
    }
    
    public String getFirstName()
    {
    	return this.firstName;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSalt() { return salt; }

    public void setLastName(String lastName)
    {
    	this.lastName = lastName;
    }
    
    public void setFirstName(String firstName)
    {
    	this.firstName = firstName;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
