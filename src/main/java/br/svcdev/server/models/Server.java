package br.svcdev.server.models;

import br.svcdev.auth.AuthenticationService;
import br.svcdev.auth.AuthenticationServiceInterface;
import br.svcdev.common.Command;
import br.svcdev.db.handler.DBHandler;
import br.svcdev.server.handlers.ClientHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс Server создает экземпляр сервера для обмена сообщениями с данными между клиентами.
 */

public class Server {

   private static Server server;
   private final int port;
   private final AuthenticationServiceInterface authenticationServiceInterface;
   private final List<ClientHandler> clients;

   private Server() {
      this.port = 82_83;
      this.clients = new ArrayList<>();
      this.authenticationServiceInterface = new AuthenticationService();
   }

   private Server(int port) {
      this.port = port;
      this.clients = new ArrayList<>();
      this.authenticationServiceInterface = new AuthenticationService();
   }

   public static Server getServer(int... port) {
      if (server == null) {
         if (port.length != 0) {
            server = new Server(port[0]);
         } else {
            server = new Server();
         }
      }
      return server;
   }

   /**
    * Метод осуществляет инициализацию сервера и ожидает подключения клиента.
    * При подключении клиента создает экземпляр обработчика действий клиента.
    */
   public void initConnection() {
      Logger logger = Logger.getLogger("admin");
      try (ServerSocket serverSocket = new ServerSocket(port)) {
         authenticationServiceInterface.start();
         while (true) {
            System.out.println("Сервер ожидает подключения клиента");
            logger.info("Сервер ожидает подключения клиента");
            Socket clientSocket = serverSocket.accept();
            String textClientConnected = "Клиент подключился";
            logger.info("Клиент подключился");
            DBHandler.insertRecordLog(textClientConnected);
            System.out.println(textClientConnected);
            ClientHandler handler = new ClientHandler(this, clientSocket);
            try {
               handler.handle();
            } catch (IOException e) {
               System.err.println("Failed to handle br.svcdev.client connection");
               logger.fatal("Failed to handle br.svcdev.client connection");
               clientSocket.close();
            }
         }
      } catch (IOException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      } catch (SQLException | ClassNotFoundException e) {
         e.printStackTrace();
      } finally {
         authenticationServiceInterface.stop();
      }
   }

   public AuthenticationServiceInterface getAuthenticationServiceInterface() {
      return authenticationServiceInterface;
   }

   /**
    * Метод осуществляет проверку имени пользователя на существование
    *
    * @param userName имя пользователя
    * @return true - если имя пользователя совпадает с уже имеющимися именами пользователей в списке,
    * false - если имя пользователя нет в списке
    */
   public boolean isUserNameBusy(String userName) {
      for (ClientHandler client : clients) {
         if (client.getNickname().equals(userName)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Метод осуществляет отписку клиента на получение сообщений
    *
    * @param client объект типа ClientHandler
    */
   public synchronized void unsubscribe(ClientHandler client) throws IOException {
      clients.remove(client);
      List<String> users = getAllUsernames();
      broadcastMessage(Command.updateUsersListCommand(users));
   }

   /**
    * Метод осуществляет подписку клиента на получение сообщений
    *
    * @param client объект типа ClientHandler
    */
   public synchronized void subscribe(ClientHandler client) throws IOException {
      clients.add(client);
      List<String> users = getAllUsernames();
      broadcastMessage(Command.updateUsersListCommand(users));
   }

   /**
    * Метод осуществляет широковещательную отправку всем клиентам, подключенным к серверу
    *
    * @param command текст с данными, отправляемый подключенным клиентам.
    */
   public synchronized void broadcastMessage(Command command) throws IOException {
      for (ClientHandler client : clients) {
         client.sendMessage(command);
      }
   }

   public synchronized void sendPrivateMessage(String receiverUserName, Command command) throws IOException {
      for (ClientHandler client : clients) {
         if (client.getNickname().equals(receiverUserName)) {
            client.sendMessage(command);
            return;
         }
      }
   }

   private List<String> getAllUsernames() {
      return clients.stream().map(ClientHandler::getNickname).collect(Collectors.toList());
   }

}
