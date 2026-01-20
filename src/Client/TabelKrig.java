package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class TabelKrig extends Connection implements KeyListener {
    private JPanel boardPanel;
    private JPanel exercisePanel;
    private JTextField textField;
    private JButton enterBtn;
    private JButton startBtn;
    PrintWriter sender;
    private String selectedTable;
    Scanner reader;
    Socket socket;
    private JLabel timeLabel;
    private int elapsedSeconds = 0;
    private Timer uiTimer;
    private JPanel statusPanel;
    private JLabel[] statusBox = new JLabel[10];
    private int statusIndex = 0;

    public  TabelKrig(Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title,  socket, reader, sender);
        this.sender = sender;
        this.reader = reader;
        this.socket = socket;

        setupWindow();
    }

    void setupWindow(){
        boardPanel = new JPanel (new GridLayout(3,3,10,10));
        boardPanel.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));
        boardPanel.setBackground(new Color(5, 203, 252));

        Dimension btnSize = new Dimension(250, 250);
        Font btnFont = new Font("Arial", Font.BOLD, 26);

        for (int tabel = 2; tabel <= 10; tabel++) {
            JButton btn = new JButton(tabel + " tabel");
            btn.setPreferredSize(btnSize);
            btn.setFont(btnFont);
            btn.setFocusPainted(false);
            btn.putClientProperty("tabel", tabel);
            btn.addActionListener(this);
            boardPanel.add(btn);
        }
        add(boardPanel, BorderLayout.CENTER);

    }
    void exercisePanel (String tabel) {
        exercisePanel = new JPanel();
        exercisePanel.setBackground(new Color(5, 203, 252));
        exercisePanel.setLayout(new BoxLayout(exercisePanel, BoxLayout.PAGE_AXIS));
        exercisePanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JLabel titlePanel = new JLabel(tabel + " tabellen");
        titlePanel.setFont(new Font("Arial", Font.BOLD, 42));

        statusPanel= new JPanel(new GridLayout(1,10,10,10));
        statusPanel.setBackground(new Color(5, 203, 252));
        statusPanel.setMaximumSize(new Dimension(900, 60));
        statusPanel.setPreferredSize(new Dimension(900, 60));
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font statusFont = new Font("Arial", Font.BOLD, 20);

        for (int i = 0; i < 10; i++) {
            JLabel box = new JLabel("·", SwingConstants.CENTER); // center punktum
            box.setFont(statusFont);
            box.setOpaque(true);
            box.setBackground(Color.WHITE);
            box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            statusBox[i] = box;
            statusPanel.add(box);
        }
        statusIndex = 0;

        textField = new JTextField(JTextField.CENTER);
        Dimension size = new Dimension(320,50);
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setFont(new Font("Arial", Font.BOLD, 28));
        textField.addKeyListener(this);
        textField.setEnabled(false);

        enterBtn = new JButton("Enter");
        enterBtn.setFont(new Font("Arial", Font.BOLD, 24));
        enterBtn.addActionListener(this);
        enterBtn.setEnabled(false);

        startBtn = new JButton("Start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 24));
        startBtn.addActionListener(this);

        Dimension btnSize = new Dimension(320, 55);
        startBtn.setPreferredSize(btnSize);
        enterBtn.setPreferredSize(btnSize);

        timeLabel = new JLabel("Tid: 0");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        exercisePanel.add(Box.createVerticalGlue());

        exercisePanel.add(titlePanel);
        exercisePanel.add(Box.createVerticalStrut(20));

        exercisePanel.add(statusPanel);
        exercisePanel.add(Box.createVerticalStrut(25));

        exercisePanel.add(textField);
        exercisePanel.add(Box.createVerticalStrut(25));
        exercisePanel.add(startBtn);
        exercisePanel.add(Box.createVerticalStrut(12));
        exercisePanel.add(enterBtn);
        exercisePanel.add(Box.createVerticalStrut(20));

        exercisePanel.add(timeLabel);

        exercisePanel.add(Box.createVerticalGlue());
        add(exercisePanel, BorderLayout.CENTER);
        remove(boardPanel);
        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().split(" ")[0].matches("[2-9]|10")){
            selectedTable = e.getActionCommand().split(" ")[0];
            exercisePanel(selectedTable);

        } else if (e.getSource()==startBtn) {
            if (selectedTable != null) {
                sender.println("START:" + selectedTable);
                sender.flush();
                enterBtn.setEnabled(true);
                textField.setEnabled(true);
                exercisePanel.remove(startBtn);
                revalidate();
                repaint();

                startLocalTimer();
            }
        } else if (e.getSource() == enterBtn) {
            EnterFunc();
        }
    }

    private void startLocalTimer () {
        elapsedSeconds = 0;
        timeLabel.setText("Tid: 0");
        if (uiTimer != null && uiTimer.isRunning()) {
            uiTimer.stop();
        }
        uiTimer = new Timer(1000, _ -> {
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
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER) {
            EnterFunc();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void EnterFunc() {
        String txt = textField.getText().trim();
        if (!txt.matches("-?\\d+")) {
            textField.setText("");
            return;
        }
        String lastGuess = txt;
        sender.println(txt);
        sender.flush();
        textField.setText("");

        if (reader.hasNextLine()) {
            String response = reader.nextLine();
            if (response.contains(":")) {
                String[] splitResponse = response.split(":");
                System.out.println(Arrays.toString(splitResponse));
                String action = splitResponse[0];
                String data = splitResponse[1];
                if (action.equals("DONE")) {
                    if (statusIndex < 10) {
                        statusBox[statusIndex].setText(lastGuess);
                        statusIndex++;
                    }
                    timeLabel.setText("Tid:"+ data + "sekunder");
                    uiTimer.stop();
                    JOptionPane.showMessageDialog(this, "Tillykke du er færdig!\n Din tid:" + data + "sekunder");
                }
            }
            if (response.equals("OK")) {
                if (statusIndex < 10) {
                    statusBox[statusIndex].setText(lastGuess);
                    statusIndex++;
                }
                return;
            }
            if (response.equals("WRONG")) {
                JOptionPane.showMessageDialog(this, "Forkert – prøv igen!");
            }
        }
    }
}
