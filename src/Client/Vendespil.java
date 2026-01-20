package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Vendespil extends Connection {
    private JButton startBtn;
    private final JButton[] cardList = new JButton[16];
    private JPanel lagContainer;
    private JPanel startButtonContainer;
    private final ArrayList<JButton> openCards = new ArrayList<>();
    JLabel timeLabel;
    JLabel moveLabel;
    int elapsedSeconds;
    Timer uiTimer;

    public  Vendespil(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window, title, socket, reader, sender);
        setupWindow();
        revalidate();
        repaint();

        Thread listener = new Thread(() -> {
            try {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();

                    SwingUtilities.invokeLater(() -> handleServerMessages(line));
                }
            } catch (Exception _) {

            }
        });
        listener.start();

    }

    void setupWindow(){
        lagContainer = new JPanel();
        lagContainer.setLayout(new OverlayLayout(lagContainer));

        startButtonContainer = new JPanel(new GridBagLayout());

        startButtonContainer.setOpaque(false);

        JPanel spilContainer = new JPanel(new BorderLayout());

        startBtn = new JButton("Start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));

        startBtn.setPreferredSize(new Dimension(300, 200));

        startBtn.addActionListener(this);

        startButtonContainer.add(startBtn);

        lagContainer.add(startButtonContainer);
        lagContainer.add(spilContainer);

        JPanel boardPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        boardPanel.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));
        boardPanel.setBackground(new Color(5, 203, 252));

        for (int i = 0; i < 16; i++) {
            JButton card = new JButton("");
            card.addActionListener(this);
            card.setEnabled(false);
            boardPanel.add(card);
            cardList[i] = card;
        }
        spilContainer.add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        infoPanel.setPreferredSize(new Dimension(100,50));
        infoPanel.setBackground(new Color(5, 203, 252));

        Font infoFont = new Font ("Arial",Font.BOLD,20);
        timeLabel = new JLabel("Tid: 0");
        timeLabel.setFont(infoFont);

        moveLabel = new JLabel("Træk: 0");
        moveLabel.setFont(infoFont);

        infoPanel.add(timeLabel);
        infoPanel.add(moveLabel);

        spilContainer.add(infoPanel, BorderLayout.PAGE_END);
        add(lagContainer,BorderLayout.CENTER);
    }

    void handleServerMessages(String line){
        System.out.println(line);
        if (Objects.equals(line, "Game has started")) {
            //Start Timer
        } else if (Objects.equals(line, "Game has ended")) {

        } else if (Objects.equals(line.split(":")[0], "Move received")) {
            openCards.getLast().setText(line.split(":")[1]);
            openCards.getLast().setEnabled(false);
        } else if (line.equals("Wrong") || line.equals("Correct")) {
            moveLabel.setText("Træk: " + (Integer.parseInt(moveLabel.getText().split(": ")[1]) + 1));
            if (line.equals("Wrong")){
                Timer timer = new Timer(1500, _ -> {
                    for (int i = 0; i < 2 ; i++){
                        openCards.getFirst().setEnabled(true);
                        openCards.removeFirst().setText("");
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                for (int i = 0; i < 2 ; i++){
                    openCards.removeFirst();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==startBtn) {
            lagContainer.remove(startButtonContainer);
            revalidate();
            repaint();
            for (JButton card : cardList) {
                card.setEnabled(true);
            }
            startLocalTimer();

        } else {
            for (int i = 0; i < cardList.length; i++) {
                if (cardList[i] == e.getSource()) {
                    sender.println("MOVE:" + i);
                    openCards.add(cardList[i]);
                }
            }
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
}