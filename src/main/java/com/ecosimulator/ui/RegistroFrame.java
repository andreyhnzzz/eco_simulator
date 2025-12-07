package com.ecosimulator.ui;

import com.ecosimulator.model.User;
import com.ecosimulator.persistence.UserRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Registration frame for new users
 * Validates age >= 18 and email format
 */
public class RegistroFrame extends JFrame {
    
    private JTextField cedulaField;
    private JTextField nombreField;
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> dayCombo;
    private JRadioButton masculinoButton;
    private JRadioButton femeninoButton;
    private JRadioButton otroButton;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    
    private UserRepository userRepository;
    private JFrame parentFrame;
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public RegistroFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.userRepository = new UserRepository();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Registro de Usuario");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Registro de Usuario", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Cedula
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Cédula (ID):"), gbc);
        gbc.gridx = 1;
        cedulaField = new JTextField(15);
        formPanel.add(cedulaField, gbc);
        
        // Nombre
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        nombreField = new JTextField(15);
        formPanel.add(nombreField, gbc);
        
        // Fecha de nacimiento
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Fecha de Nacimiento:"), gbc);
        
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // Year (1920-2010)
        yearCombo = new JComboBox<>();
        for (int year = 2010; year >= 1920; year--) {
            yearCombo.addItem(year);
        }
        datePanel.add(new JLabel("Año:"));
        datePanel.add(yearCombo);
        
        // Month (1-12)
        monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }
        datePanel.add(new JLabel("Mes:"));
        datePanel.add(monthCombo);
        
        // Day (1-31)
        dayCombo = new JComboBox<>();
        for (int day = 1; day <= 31; day++) {
            dayCombo.addItem(day);
        }
        datePanel.add(new JLabel("Día:"));
        datePanel.add(dayCombo);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(datePanel, gbc);
        gbc.gridwidth = 1;
        
        // Género
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Género:"), gbc);
        
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup genderGroup = new ButtonGroup();
        masculinoButton = new JRadioButton("Masculino");
        femeninoButton = new JRadioButton("Femenino");
        otroButton = new JRadioButton("Otro");
        masculinoButton.setSelected(true);
        genderGroup.add(masculinoButton);
        genderGroup.add(femeninoButton);
        genderGroup.add(otroButton);
        genderPanel.add(masculinoButton);
        genderPanel.add(femeninoButton);
        genderPanel.add(otroButton);
        
        gbc.gridx = 1;
        formPanel.add(genderPanel, gbc);
        
        // Email
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        formPanel.add(emailField, gbc);
        
        // Password
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);
        
        // Confirm password
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Confirmar contraseña:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField, gbc);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        registerButton = new JButton("Registrar");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });
        buttonPanel.add(registerButton);
        
        cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                parentFrame.setVisible(true);
            }
        });
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void handleRegistration() {
        // Validate inputs
        String cedula = cedulaField.getText().trim();
        String nombre = nombreField.getText().trim();
        int year = (Integer) yearCombo.getSelectedItem();
        int month = (Integer) monthCombo.getSelectedItem();
        int day = (Integer) dayCombo.getSelectedItem();
        String genero = getSelectedGender();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate fields
        if (cedula.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor complete todos los campos", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Email inválido", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate password match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "Las contraseñas no coinciden", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }
        
        // Create birth date
        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = LocalDate.of(year, month, day);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Fecha de nacimiento inválida", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create user
        User user = new User(cedula, nombre, fechaNacimiento, genero, email, "", "");
        
        // Validate age
        if (!user.isAdult()) {
            JOptionPane.showMessageDialog(this, 
                "Debe ser mayor de 18 años para registrarse", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Register user
        try {
            boolean success = userRepository.register(user, password);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Usuario registrado exitosamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                parentFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "La cédula ya está registrada", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getSelectedGender() {
        if (masculinoButton.isSelected()) return "Masculino";
        if (femeninoButton.isSelected()) return "Femenino";
        return "Otro";
    }
}
