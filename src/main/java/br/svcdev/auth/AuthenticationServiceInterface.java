package br.svcdev.auth;

public interface AuthenticationServiceInterface {
   void start();
   void stop();
   String[] getUserNameByLoginPassword(String login, String password);
}
