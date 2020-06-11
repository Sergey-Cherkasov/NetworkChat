package br.svcdev.server.handlers;

import br.svcdev.auth.AuthenticationServiceInterface;
import br.svcdev.common.Command;
import br.svcdev.common.commands.AuthCommand;
import br.svcdev.common.commands.BroadcastMessageCommand;
import br.svcdev.common.commands.PrivateMessageCommand;
import br.svcdev.common.commands.UpdateNicknameCommand;
import br.svcdev.db.handler.DBHandler;
import br.svcdev.server.models.Server;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Класс обработчика действий клиента
 */
public class ClientHandler {
   private final Server server;
   private final Socket socket;
   private ObjectInputStream inputStream;
   private ObjectOutputStream outputStream;

   private String fullName;
   private String nickname;

   private final AuthenticationServiceInterface authenticationServiceInterface;

   private final Logger logger;

   /**
    * Конструктор обработчика действий клиента запускает аутентификацию и метод чтения сообщений
    * в отдельном потоке.
    *
    * @param server экземпляр запущенного сервера
    * @param socket экземпляр сокета
    */
   public ClientHandler(Server server, Socket socket) {
      this.server = server;
      this.socket = socket;
      this.authenticationServiceInterface = server.getAuthenticationServiceInterface();
      this.logger = Logger.getLogger(br.svcdev.server.handlers.ClientHandler.class);
   }

   /**
    * Метод осуществляет закрытие соединения и освобождение ресурсов
    */
   private void closeConnection() {
      try {
         server.unsubscribe(this);
         logger.info(nickname + " quit from chat");
         server.broadcastMessage(Command.broadcastMessageCommand(nickname + " вышел из чата"));
         socket.close();
      } catch (IOException e) {
         System.err.println(e.getMessage());
      }
   }

   /**
    * Метод осуществляет чтение сообщений, полученных от сервера
    */
   private void readMessages() throws IOException, SQLException, ClassNotFoundException {
      while (true) {
         Command command = readCommand();
         if (command == null) {
            continue;
         }
         switch (command.getType()) {
            case CMD_END:
               return;
            case CMD_BROADCAST_MESSAGE:
               BroadcastMessageCommand data = (BroadcastMessageCommand) command.getData();
               server.broadcastMessage(Command.messageCommand(nickname, data.getMessage()));
               break;
            case CMD_PRIVATE_MESSAGE:
               PrivateMessageCommand privateMessageCommand = (PrivateMessageCommand) command.getData();
               String receiver = privateMessageCommand.getReceiver();
               String message = privateMessageCommand.getMessage();
               server.sendPrivateMessage(receiver, Command.messageCommand(nickname, message));
               break;
            case CMD_UPDATE_NICKNAME:
               updateNickname(command);
               break;
            default:
               logger.fatal("Unknown type of command : " + command.getType());
               String errorMessage = "Unknown type of command : " + command.getType();
               System.err.println(errorMessage);
               sendMessage(Command.errorCommand(errorMessage));
         }
      }
   }

   private Command readCommand() throws IOException {
      try {
         return (Command) inputStream.readObject();
      } catch (ClassNotFoundException e) {
         logger.warn("Unknown type of object from br.svcdev.client!");
         String errorMessage = "Unknown type of object from br.svcdev.client!";
         System.err.println(errorMessage);
         e.printStackTrace();
         sendMessage(Command.errorCommand(errorMessage));
         return null;
      }
   }

   /**
    * Метод аутентификации клиента
    */
   private void authentication() throws IOException, SQLException, ClassNotFoundException {

      Thread timeOut = authTimeout();

      while (true) {
         Command command = readCommand();
         if (command == null) {
            continue;
         }
         switch (command.getType()){
            case CMD_AUTH:
               if (processAuthCommand(command)) {
                  timeOut.interrupt();
                  return;
               }
               break;
            case CMD_REG_REQUEST:
               timeOut.interrupt();
               sendMessage(command);
               break;
            case CMD_REG:
               sendMessage(command);
               break;
            case CMD_END:
               timeOut.interrupt();
               return;
            default:
               logger.fatal("Illegal command for authentication: " + command.getType());
               String errorMessage = "Illegal command for authentication: " + command.getType();
               System.err.println(errorMessage);
               sendMessage(Command.errorCommand(errorMessage));
         }
      }
   }

   /**
    * Метод отслеживает время авторизации пользователя в чате
    */
   private Thread authTimeout() {
      Thread timeOut = new Thread(() -> {
         try {
            Thread.sleep(30000);
            String errorMessage = "Вышло время авторизации.\nПерезапустите приложение";
            DBHandler.insertRecordLog("Time for authorization has expired");
            logger.info("Time for authorization has expired");
            sendMessage(Command.errorCommand(errorMessage));
            System.err.println(errorMessage);
            closeConnection();
         } catch (InterruptedException e) {
            System.out.println("Поток timeOut успешно прерван");
            logger.info("Поток timeOut успешно прерван");
         } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
         }
      }, "timeOut");
      timeOut.start();
      return timeOut;
   }

   private boolean processAuthCommand(Command command) throws IOException, SQLException, ClassNotFoundException {
      AuthCommand authCommand = (AuthCommand) command.getData();
      String login = authCommand.getLogin();
      String password = authCommand.getPassword();
      String[] authQueryResult = authenticationServiceInterface.getUserNameByLoginPassword(login, password);
      String fullName = authQueryResult[0];
      String nickname = authQueryResult[1];
      if (nickname == null) {
         String textWrongLoginPassword = "Неверные логин/пароль!";
         DBHandler.insertRecordLog(textWrongLoginPassword);
         sendMessage(Command.authErrorCommand(textWrongLoginPassword));
         logger.info(textWrongLoginPassword);
      } else if (server.isUserNameBusy(nickname)) {
         String textAccountAlreadyUsed = "Учетная запись уже используется!";
         logger.info(textAccountAlreadyUsed);
         DBHandler.insertRecordLog(textAccountAlreadyUsed);
         sendMessage(Command.authErrorCommand(textAccountAlreadyUsed));
      } else {
         String textSuccessfullyLoggedIn = "The user has successfully logged in";
         authCommand.setFullname(fullName);
         authCommand.setNickname(nickname);
         sendMessage(command);
         setFullName(fullName);
         setNickname(nickname);
         DBHandler.insertRecordLog(textSuccessfullyLoggedIn);
         logger.info(textSuccessfullyLoggedIn);
         server.broadcastMessage(Command.messageCommand(null, nickname + " Зашел в чат!"));
         server.subscribe(this);
         return true;
      }
      return false;
   }

   /**
    * Метод осуществляет отправку сообщения на сервер
    *
    * @param command текст с данными сообщения
    */
   public void sendMessage(Command command) throws IOException {
      outputStream.writeObject(command);
   }

   public void handle() throws IOException {
      inputStream = new ObjectInputStream(socket.getInputStream());
      outputStream = new ObjectOutputStream(socket.getOutputStream());

      new Thread(() -> {
         try {
            authentication();
            readMessages();
         } catch (IOException e) {
            System.out.println("Connect has been failed.");
            logger.fatal("Connect has been failed.");
         } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
         } finally {
            closeConnection();
         }
      }).start();
   }

   private void setNickname(String nickname) {
      this.nickname = nickname;
   }

   public String getNickname() {
      return nickname;
   }

   public void setFullName(String fullName) {
      this.fullName = fullName;
   }

   public String getFullName() {
      return fullName;
   }

   private void updateNickname(Command command) throws SQLException, ClassNotFoundException, IOException {
      server.unsubscribe(this);
      UpdateNicknameCommand data = (UpdateNicknameCommand) command.getData();
      String nickname = data.getNickname();
      String newNickname = data.getNewNickname();
      DBHandler.changeNickname(nickname, newNickname);
      setNickname(newNickname);
      server.subscribe(this);
   }
}
