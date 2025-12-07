package com.ecosimulator.ui;

import com.ecosimulator.model.User;
import com.ecosimulator.persistence.UserRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login frame for user authentication
 * Authenticates by cedula (ID) and password
 */
public class LoginFrame extends JFrame {
    
    private JTextField cedulaField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserRepository userRepository;
    
    public LoginFrame() {
        this.userRepository = new UserRepository();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Eco Simulator - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Ecosystem Simulator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Cedula field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Cédula (ID):"), gbc);
        
        gbc.gridx = 1;
        cedulaField = new JTextField(15);
        formPanel.add(cedulaField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        buttonPanel.add(loginButton);
        
        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationFrame();
            }
        });
        buttonPanel.add(registerButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Make Enter key trigger login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void handleLogin() {
        String cedula = cedulaField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (cedula.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both cedula and password", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Authenticate
        User user = userRepository.authenticate(cedula, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, 
                "Welcome, " + user.getNombre() + "!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open main application window
            openMainFrame(user);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid credentials", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    private void openRegistrationFrame() {
        RegistroFrame registroFrame = new RegistroFrame(this);
        registroFrame.setVisible(true);
        setVisible(false);
    }
    
    private void openMainFrame(User user) {
        MainFrame mainFrame = new MainFrame(user);
        mainFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
