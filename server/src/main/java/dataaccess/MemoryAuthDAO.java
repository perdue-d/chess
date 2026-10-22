
package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> tokens = new HashMap<>();

    public void clear() {
        tokens.clear();
    }

    public AuthData getAuth(String token) {
        return tokens.get(token);
    }

    public void createAuth(AuthData auth) {
        tokens.put(auth.authToken(), auth);
    }

    public void deleteAuth(String token) {
        tokens.remove(token);
    }
}

