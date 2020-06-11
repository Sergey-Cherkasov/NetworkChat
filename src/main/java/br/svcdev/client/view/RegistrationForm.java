package br.svcdev.client.view;

import br.svcdev.client.controller.ClientController;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegistrationForm extends JFrame {
    private JPanel mainPanel;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtNickName;
    private JPasswordField txtPassword;
    private JPasswordField txtPassRepeat;
    private JButton btnRegister;
    private JButton btnCancel;
    private JTextField txtLogin;

    private final ClientController clientController;

    public RegistrationForm(ClientController clientController) {
        this.clientController = clientController;
        initWindow();
    }

    private void initWindow() {
        setTitle("Network chat::Registration");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(300, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        btnRegister.addActionListener(e -> toRegister());
        btnCancel.addActionListener(e -> onExit());
        getRootPane().setDefaultButton(btnCancel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
        setContentPane(mainPanel);
    }

    private void onExit() {
        System.exit(0);
    }

    private void toRegister() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String nickName = txtNickName.getText().trim();
        String login = txtLogin.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String passRepeat = new String(txtPassRepeat.getPassword()).trim();
        if (password.equals(passRepeat)) {
            clientController.sendRegMessage(firstName, lastName, nickName, login, password);
        } else {
            showError("Password is not correct.");
        }
    }

    public void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage);
    }
}
