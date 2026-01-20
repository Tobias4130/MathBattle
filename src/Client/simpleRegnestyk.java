package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
//TODO Lav det så man kan sende svar med Enter
public class simpleRegnestyk extends Connection {
    private JLabel titleLabel;
    private JPanel statusPanel;
    private JPanel exercisePanel;
    private JLabel[] statusBox = new JLabel[10];
    private JLabel questionLabel;
    private JTextField textField;
    private JButton startBtn;
    private JButton enterBtn;
    private JLabel timeLabel;
    private int currentIndex = 0;
    private Timer uiTimer;
    private int elapsedSeconds = 0;

    private final Scanner reader;
    private final PrintWriter sender;

    public  simpleRegnestyk (Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title, socket, reader, sender);
        this.reader = reader;
        this.sender = sender;
        setupWindow();



    }
    void setupWindow(){
        exercisePanel = new JPanel();
        exercisePanel.setBackground(new Color(5, 203, 252));
        exercisePanel.setLayout(new BoxLayout(exercisePanel, BoxLayout.PAGE_AXIS));
        exercisePanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        titleLabel = new JLabel("Simple Regnestykker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusPanel = new JPanel(new GridLayout(1, 10, 10, 10));
        statusPanel.setBackground(new Color(5, 203, 252));
        statusPanel.setMaximumSize(new Dimension(900, 60));
        statusPanel.setPreferredSize(new Dimension(900, 60));
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Font statusFont = new Font("Arial", Font.BOLD, 20);
        for (int i = 0; i < 10; i++) {
            JLabel box = new JLabel("·", SwingConstants.CENTER);
            box.setFont(statusFont);
            box.setOpaque(true);
            box.setBackground(Color.WHITE);
            box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            statusBox[i] = box;
            statusPanel.add(box);
        }

        questionLabel = new JLabel("Tryk Start", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 32));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textField = new JTextField();
        Dimension fieldSize = new Dimension(320, 50);
        textField.setMaximumSize(fieldSize);
        textField.setFont(new Font("Arial", Font.BOLD, 28));
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setEnabled(false);

        startBtn = new JButton("Start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 24));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setPreferredSize(new Dimension(320, 55));
        startBtn.addActionListener(this);

        enterBtn = new JButton("Enter");
        enterBtn.setFont(new Font("Arial", Font.BOLD, 24));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.setPreferredSize(new Dimension(320, 55));
        enterBtn.setEnabled(false);
        enterBtn.addActionListener(this);

        timeLabel = new JLabel("Tid: 0", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        exercisePanel.add(Box.createVerticalGlue());
        exercisePanel.add(titleLabel);
        exercisePanel.add(Box.createVerticalStrut(20));
        exercisePanel.add(statusPanel);
        exercisePanel.add(Box.createVerticalStrut(25));
        exercisePanel.add(questionLabel);
        exercisePanel.add(Box.createVerticalStrut(20));
        exercisePanel.add(textField);
        exercisePanel.add(Box.createVerticalStrut(25));
        exercisePanel.add(startBtn);
        exercisePanel.add(Box.createVerticalStrut(12));
        exercisePanel.add(enterBtn);
        exercisePanel.add(Box.createVerticalStrut(20));
        exercisePanel.add(timeLabel);
        exercisePanel.add(Box.createVerticalGlue());

        add(exercisePanel, BorderLayout.CENTER);
    }
        @Override
    public void actionPerformed(ActionEvent e) {
            if (e.getSource() == startBtn) {
                resetStatusBoxes();
                startLocalTimer();

                sender.println("START");
                enterBtn.setEnabled(true);
                textField.setEnabled(true);
                textField.requestFocusInWindow();

                Thread listener = new Thread(() -> {
                    try {
                        while (reader.hasNextLine()) {
                            String line = reader.nextLine();

                            SwingUtilities.invokeLater(() -> handleServerMessage(line));
                        }
                    } catch (Exception _) {

                    }
                });
                listener.start();

                return;
            }
            if (e.getSource() == enterBtn) {
                sendAnswer();
            }
        }
    private void sendAnswer () {
        String txt = textField.getText().trim();
        if (!txt.matches("-?\\d+")) {
            System.out.println("Please enter a valid number");
            textField.setText("");
            return;
        }
        sender.println("ANSWER:" + txt);
        System.out.println("ANSWER:" + txt);
        textField.setText("");
    }
    private void handleServerMessage(String msg) {
        System.out.println("Server Message: " + msg);
        if (msg.startsWith("Q:")) {
            String[] parts = msg.split(":");
            int nr = Integer.parseInt(parts[1]); // 1..10
            String expr = parts[2];

            currentIndex = nr - 1;
            questionLabel.setText(expr);
            statusBox[currentIndex].setBackground(new Color(31, 173, 255));
            return;
        }
        if (msg.startsWith("OK:")) {
            int nr = Integer.parseInt(msg.split(":")[1]);
            int idx = nr - 1;

            statusBox[idx].setBackground(new Color(120, 255, 120));
            statusBox[idx].setText("✓");
            return;
        }
        if (msg.startsWith("WRONG:")) {
            int nr = Integer.parseInt(msg.split(":")[1]);
            int idx = nr - 1;

            statusBox[idx].setBackground(new Color(255, 130, 130));
            statusBox[idx].setText("✗");

            JOptionPane.showMessageDialog(this, "Forkert – prøv igen!");
            return;
        }
        if (msg.startsWith("DONE:")) {
            String time = msg.split(":")[1];

            if (uiTimer != null) uiTimer.stop();
            timeLabel.setText("Tid: " + time + " sekunder");
            enterBtn.setEnabled(false);
            textField.setEnabled(false);

            JOptionPane.showMessageDialog(this, "Tillykke du er færdig!\nDin tid: " + time + " sekunder");
        }
    }
    private void resetStatusBoxes() {
        for (int i = 0; i < statusBox.length; i++) {
            statusBox[i].setText("·");
            statusBox[i].setBackground(Color.WHITE);
        }
        currentIndex = 0;
        questionLabel.setText("Tryk Start");
    }

    private void startLocalTimer() {
        elapsedSeconds = 0;
        timeLabel.setText("Tid: 0");

        if (uiTimer != null && uiTimer.isRunning()) uiTimer.stop();

        uiTimer = new Timer(1000, ev -> {
            elapsedSeconds++;
            timeLabel.setText("Tid: " + elapsedSeconds);
        });
        uiTimer.start();
    }
}