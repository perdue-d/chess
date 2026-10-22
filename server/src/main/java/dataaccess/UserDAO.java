package dataaccess;
import model.Userdata;

public interface UserDao {
    void clear();
    Userdata getUser(String username);
    void createUser(Userdata user);

}
