package br.svcdev.client.view;

import br.svcdev.client.controller.ClientController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AuthForm extends JFrame {

   private JPanel mainPanel;
   private JButton cancelButton;
   private JButton btnOk;
   private JPasswordField passwordField;
   private JTextField loginTextField;
   private JLabel hyperlink;

   private final ClientController clientController;

   public AuthForm(ClientController clientController) {
      this.clientController = clientController;
      initAuthWindow();
   }

   private void initAuthWindow() {
      setTitle("Network chat::Authorization");
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setSize(300, 225);
      setResizable(false);
      setLocationRelativeTo(null);
      btnOk.addActionListener(e -> onBtnOk());
      cancelButton.addActionListener(e -> onExit());
      getRootPane().setDefaultButton(btnOk);
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            onExit();
         }
      });
      hyperlink.setForeground(Color.BLUE.darker());
      hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      hyperlink.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            onRegHyperlink();
         }
      });
      loginTextField.addFocusListener(new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
              btnOk.setEnabled(true);
          }
      });
      passwordField.addFocusListener(new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
              btnOk.setEnabled(true);
          }
      });
      setContentPane(mainPanel);
   }

   private void onExit() {
      ClientController.shutdown();
      System.exit(0);
   }

   private void onBtnOk() {
      String login = loginTextField.getText().trim();
      String pass = new String(passwordField.getPassword()).trim();
      clientController.sendAuthMessage(login, pass);
   }

   private void onRegHyperlink(){
      clientController.sendRegRequestMessage();
   }

   public void showError(String errorMessage) {
       btnOk.setEnabled(false);
       JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
   }
}
