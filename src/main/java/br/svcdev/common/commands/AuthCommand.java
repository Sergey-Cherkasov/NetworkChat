package br.svcdev.common.commands;

import java.io.Serializable;

public class AuthCommand implements Serializable {

   private final String login;
   private final String password;

   private String fullname;
   private String nickname;

   public AuthCommand(String login, String password) {
      this.login = login;
      this.password = password;
   }

   public String getLogin() {
      return login;
   }

   public String getPassword() {
      return password;
   }

   public String getNickname() {
      return nickname;
   }

   public void setNickname(String nickname) {
      this.nickname = nickname;
   }

   public String getFullname() {
      return fullname;
   }

   public void setFullname(String fullname) {
      this.fullname = fullname;
   }
}
