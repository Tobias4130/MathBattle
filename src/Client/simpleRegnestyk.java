package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class simpleRegnestyk extends Connection implements KeyListener {
    private JLabel titleLabel; //Overskrift på spil
    private JPanel statusPanel; //Panel 10 status bokse
    private JPanel exercisePanel;
    private JLabel[] statusBox = new JLabel[10];
    private JLabel questionLabel; //aktuel regnestykke
    private JTextField textField; //inputfelt til svar
    private JButton startBtn; //startknap
    private JButton enterBtn; //enterknap
    private JLabel timeLabel; //Lokal tid
    private int currentIndex = 0; //Spørgsmål nr
    private Timer uiTimer; //Tid
    private int elapsedSeconds = 0; //Sekunder

    private final Scanner reader;
    private final PrintWriter sender;

    public  simpleRegnestyk (Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title, socket, reader, sender);
        this.reader = reader;
        this.sender = sender;
        setupWindow();


    //Opsætter GUI, titel, status 10 bokse, opgave, tekstfel, start/enter, tid
    }
    void setupWindow(){
        exercisePanel = new JPanel();
        exercisePanel.setBackground(new Color(200, 200, 200));
        exercisePanel.setLayout(new BoxLayout(exercisePanel, BoxLayout.PAGE_AXIS));
        exercisePanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        titleLabel = new JLabel("Simple Regnestykker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusPanel = new JPanel(new GridLayout(1, 10, 10, 10));
        statusPanel.setBackground(new Color(200, 200, 200));
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
        textField.addKeyListener(this);

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
    //Når der klikkes på knapperne
        @Override
    public void actionPerformed(ActionEvent e) {
        //Start knap, starter
            if (e.getSource() == startBtn) {
                resetStatusBoxes();
                startLocalTimer();
                exercisePanel.remove(startBtn);

                sender.println("START");
                enterBtn.setEnabled(true);
                textField.setEnabled(true);
                textField.requestFocusInWindow();
            //Tråd lytter efter server beskeder og opdatere GUI
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
        //Sender svar til server og validere input er tal
    private void sendAnswer () {
        String txt = textField.getText().trim();
        //hvis input ikke et tal, ryddes tekstfelt
        if (!txt.matches("-?\\d+")) {
            System.out.println("Please enter a valid number");
            textField.setText("");
            textField.requestFocusInWindow();
            return;
        }
        //Sender svar til server og gør klar til næste
        sender.println("ANSWER:" + txt);
        System.out.println("ANSWER:" + txt);
        textField.setText("");
        textField.requestFocusInWindow();
    }
    //Modtager og håndtere server beskeder
    private void handleServerMessage(String msg) {
        System.out.println("Server Message: " + msg);
        //Nyt spørgsmål
        if (msg.startsWith("Q:")) {
            String[] parts = msg.split(":");
            int nr = Integer.parseInt(parts[1]); // 1..10
            String expr = parts[2];

            currentIndex = nr - 1;
            questionLabel.setText(expr);
            statusBox[currentIndex].setBackground(new Color(31, 173, 255));
            return;
        }
        //korrekt svar
        if (msg.startsWith("OK:")) {
            int nr = Integer.parseInt(msg.split(":")[1]);
            int idx = nr - 1;

            statusBox[idx].setBackground(new Color(120, 255, 120));
            statusBox[idx].setText("✓");
            return;
        }
        //forkert svar
        if (msg.startsWith("WRONG:")) {
            int nr = Integer.parseInt(msg.split(":")[1]);
            int idx = nr - 1;

            statusBox[idx].setBackground(new Color(255, 130, 130));
            statusBox[idx].setText("✗");

            JOptionPane.showMessageDialog(this, "Forkert – prøv igen!");
            return;
        }
        //Afslutnign
        if (msg.startsWith("DONE:")) {
            String time = msg.split(":")[1];

            if (uiTimer != null) uiTimer.stop();
            timeLabel.setText("Tid: " + time + " sekunder");
            enterBtn.setEnabled(false);
            textField.setEnabled(false);
            sender.println(username);
            JOptionPane.showMessageDialog(this, "Tillykke du er færdig!\nDin tid: " + time + " sekunder");
        }
    }
    //Nulsiller status bokse til starttilstand
    private void resetStatusBoxes() {
        for (int i = 0; i < statusBox.length; i++) {
            statusBox[i].setText("·");
            statusBox[i].setBackground(Color.WHITE);
        }
        currentIndex = 0;
        questionLabel.setText("Tryk Start");
    }

    //Lokal tid
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendAnswer();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}