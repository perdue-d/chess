
package dataaccess;

import model.UserData;

public interface UserDAO {
    void clear();
    UserData getUser(String username);
    void createUser(UserData user);
}

