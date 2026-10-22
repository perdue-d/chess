
package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear();
    AuthData getAuth(String token);
    void createAuth(AuthData auth);
    void deleteAuth(String token);
}
