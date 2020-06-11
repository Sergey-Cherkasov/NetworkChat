package br.svcdev.common.commands;

import java.io.Serializable;

public class RegCommand implements Serializable {
    private final String firstName;
    private final String lastName;
    private final String nickName;
    private final String login;
    private final String password;

    public RegCommand(String firstName, String lastName, String nickName, String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.login = login;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
