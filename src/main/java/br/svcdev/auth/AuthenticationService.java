package br.svcdev.auth;

import br.svcdev.db.handler.DBHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * Класс аутентификации клиентов
 */
public class AuthenticationService implements AuthenticationServiceInterface {

    private static final Map<DataUsers, String> USERNAME_BY_LOGIN_PASSWORD = Map.of(
            new DataUsers("Denials", "pass1"), "Denials",
            new DataUsers("Lawson", "pass2"), "Lawson",
            new DataUsers("McGregor", "pass3"), "McGregor"
    );
    private Connection connection;

    @Override
    public void start() {
        System.out.println("Authentication service has been started");
    }

    @Override
    public void stop() {
        System.out.println("Authentication service has been stopped");
    }

    /**
     * Метод возвращает имя пользователя при совпадении логина/пароля, либо null.
     *
     * @param login    логин пользователя
     * @param password пароль пользователя
     * @return имя пользователя, либо null.
     */
    @Override
    public String[] getUserNameByLoginPassword(String login, String password) {
        String[] authQueryResult = new String[0];
//        String fullName = null;
//        String nickname = null;
        try {
            authQueryResult = DBHandler.authQuery(login, password);
//            fullName = authQueryResult[0];
//            nickname = authQueryResult[1];
        } catch (SQLException e) {
            System.err.println("SQL query execution error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error accessing the database driver: " + e.getMessage());
            e.printStackTrace();
        }
//        System.err.println("User name = " + nickname);
        return authQueryResult;
    }

    /**
     * Внутреннй клас, описывающий модель данных клиента (логин/пароль/имя пользователя)
     */
    private static class DataUsers {
        private final String login;
        private final String password;

        public DataUsers(String login, String password) {
            this.login = login;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataUsers dataUsers = (DataUsers) o;
            return Objects.equals(login, dataUsers.login) &&
                    Objects.equals(password, dataUsers.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(login, password);
        }
    }

}
