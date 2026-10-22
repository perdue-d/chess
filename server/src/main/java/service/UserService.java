
package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class UserService {
    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public record LoginResult(String username, String authToken) {}

    public LoginResult register(UserData user) {
        if (user == null || blank(user.username()) ||
                blank(user.password()) || blank(user.email())) {
            throw new ServiceException(400, "Error: bad request");
        }

        if (users.getUser(user.username()) != null) {
            throw new ServiceException(403, "Error: already taken");
        }

        users.createUser(user);
        return new LoginResult(user.username(), newToken(user.username()));
    }

    public LoginResult login(String username, String password) {
        if (blank(username) || blank(password)) {
            throw new ServiceException(400, "Error: bad request");
        }

        UserData user = users.getUser(username);

        if (user == null || !user.password().equals(password)) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        return new LoginResult(username, newToken(username));
    }

    public void logout(String token) {
        authenticate(token);
        auths.deleteAuth(token);
    }

    public String authenticate(String token) {
        AuthData auth = token == null ? null : auths.getAuth(token);

        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        return auth.username();
    }

    private String newToken(String username) {
        String token = UUID.randomUUID().toString();
        auths.createAuth(new AuthData(token, username));
        return token;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
