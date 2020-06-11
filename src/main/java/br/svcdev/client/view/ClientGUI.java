package br.svcdev.client.view;

import br.svcdev.client.controller.ClientController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class ClientGUI extends JFrame {
   private JButton sendButton;
   private JPanel mainPanel;
   private JList<String> userList;
   private JTextArea textArea;
   private JTextField inputText;
   private JPanel userListPanel;
   private JButton btnChangeNickname;

   private final ClientController clientController;

   public ClientGUI(ClientController clientController) {
      this.clientController = clientController;
      initClientWindow();
      inputText.requestFocus();
   }

   private void initClientWindow() {
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setTitle(clientController.getNickname());
      setSize(500, 500);
      setLocationRelativeTo(null);
      sendButton.addActionListener(e -> ClientGUI.this.sendMessage());
      inputText.addActionListener(e -> sendMessage());
      btnChangeNickname.addActionListener(e -> onChangeNickName());
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            ClientController.shutdown();
         }
      });
      setContentPane(mainPanel);
   }

   private void onChangeNickName() {
      String newNickName;
      newNickName = JOptionPane.showInputDialog(this, "Enter a new nickname:", QUESTION_MESSAGE);
      clientController.sendUpdateNickname(newNickName);
   }

   private void sendMessage() {
      String textMessage = inputText.getText().trim();
      if (textMessage.isEmpty()) {
         return;
      }
      appendOwnMessageIntoTextChatArea(textMessage);

      if (userList.getSelectedIndex() < 1) {
         clientController.sendMessage(textMessage);
      } else {
         String selectedUserName = userList.getSelectedValue();
         clientController.sendPrivateMessage(selectedUserName, textMessage);
      }
      inputText.setText(null);
   }

   public void appendMessageIntoTextChatArea(String textMessage) {
      SwingUtilities.invokeLater(() -> {
         textArea.append(textMessage);
         textArea.append(System.lineSeparator());
      });
   }

   public void appendOwnMessageIntoTextChatArea(String textMessage) {
      appendMessageIntoTextChatArea("Я: " + textMessage);
   }

   public void updateUsers(List<String> users) {
      SwingUtilities.invokeLater(() -> {
         DefaultListModel<String> model = new DefaultListModel<>();
         for (String userName : users) {
            model.addElement(userName);
         }
         userList.setModel(model);
      });
   }

   public void showError(String errorMessage) {
      JOptionPane.showMessageDialog(this, errorMessage);
   }
}
