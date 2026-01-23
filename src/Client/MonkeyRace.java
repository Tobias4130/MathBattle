package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;

//Skal løse regnestykker og aben hopper en palme frem og aben skal nå bananerne
public class MonkeyRace extends Connection implements KeyListener {

    private Timer hopTimer;

    private JPanel top;
    private JButton startBtn;
    private JLabel tidLabel;

    private JPanel trackPanel;
    private JLabel abeLabel;
    private JLabel finishLabel;
    private JPanel gameContainerPanel;

    private JLabel questionLabel;
    private JTextField answerField;
    private JButton enterBtn;
    private JPanel inputRow;

    private int position = 0;
    private final int goal = 9;
    private final int startX = 60;
    private int w;
    private int h;
    private int y;
    private int finishX;

    private Timer uiTimer;
    private int elapsedSeconds = 0;

    private final JLabel[] treeLabels = new JLabel[8];
    private final Point[] treePoints = new Point[8];

    public MonkeyRace(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window, title, socket, reader, sender);
        setUpWindow(window);
    }
    //Opsætter GUI
    private void setUpWindow(Window window) {

        gameContainerPanel = new JPanel(new BorderLayout());
        gameContainerPanel.setBackground(new Color(50, 205, 50));

        top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        startBtn = new JButton("Start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 22));
        startBtn.setPreferredSize(new Dimension(140, 50));
        startBtn.addActionListener(this);

        tidLabel = new JLabel("Tid: 0", SwingConstants.RIGHT);
        tidLabel.setFont(new Font("Arial", Font.BOLD, 22));

        top.add(tidLabel, BorderLayout.LINE_END);
        gameContainerPanel.add(top, BorderLayout.PAGE_START);

        trackPanel = new JPanel(null);
        trackPanel.setOpaque(false);
        trackPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        //Abe billede
        ImageIcon icon = new ImageIcon("src/Client/Images/monkey.png");
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        abeLabel = new JLabel(new ImageIcon(img));
        abeLabel.setSize(80, 80);
        //Banan billede (præmie)
        ImageIcon iconFinish = new ImageIcon(("src/Client/Images/banana.png"));
        Image imgFinish = iconFinish.getImage().getScaledInstance(80,80, Image.SCALE_SMOOTH);
        finishLabel = new JLabel(new ImageIcon(imgFinish));
        finishLabel.setSize(80,80);


        trackPanel.add(abeLabel);
        trackPanel.add(finishLabel);
        //Palmetræ
        ImageIcon palmIcon = new ImageIcon("src/Client/Images/palm-tree.png");
        Image palmImg = palmIcon.getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH);
        ImageIcon scaledPalm = new ImageIcon(palmImg);

        for (int i = 0; i < treeLabels.length; i++) {
            JLabel tree = new JLabel(scaledPalm);
            tree.setSize(120, 160);
            treeLabels[i] = tree;
            trackPanel.add(tree);
        }

        gameContainerPanel.add(trackPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 25, 20));

        questionLabel = new JLabel("Tryk Start", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 28));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputRow.setOpaque(false);

        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(200, 45));
        answerField.setFont(new Font("Arial", Font.BOLD, 24));
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.setEnabled(false);
        answerField.addKeyListener(this);

        enterBtn = new JButton("Enter");
        enterBtn.setFont(new Font("Arial", Font.BOLD, 22));
        enterBtn.setPreferredSize(new Dimension(140, 45));
        enterBtn.setEnabled(false);
        enterBtn.addActionListener(this);

        inputRow.add(answerField);
        inputRow.add(enterBtn);
        inputRow.add(startBtn);

        bottom.add(questionLabel);
        bottom.add(inputRow);

        gameContainerPanel.add(bottom, BorderLayout.PAGE_END);
        add(gameContainerPanel, BorderLayout.CENTER);
        //Tråd lytter på beskeder fra server
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

        SwingUtilities.invokeLater(this::layOutTrack);
    }
    //Beregner og placering af træer, start og mål
    private void layOutTrack() {
        w = trackPanel.getWidth();
        h = trackPanel.getHeight();
        y = Math.max(60, h / 2 - 50);
        finishX = Math.max(startX + 300, w - 140);

        finishLabel.setLocation(finishX, y);
        int usable = finishX - startX;
        for (int i = 0; i < 8; i++) {
            double t = (i + 1) / 9.0;
            int tx = (int) (startX + t * usable);
            int ty = y;

            treePoints[i] = new Point(tx, ty);
            treeLabels[i].setLocation(tx, ty);
        }
        updateMonkeyPosition();
    }
    //Abens position på banen
    private void updateMonkeyPosition() {
        int yOffset = -50; // så aben står lidt over træet

        if (position == 0) {
            abeLabel.setLocation(startX, y);
        } else if (position >= goal) {
            animateHopTo(finishX, y+yOffset);
        } else {
            Point p = treePoints[position - 1];
            animateHopTo(p.x, p.y+yOffset);
        }
        trackPanel.revalidate();
        trackPanel.repaint();
    }
    //Starter spillet og tid
    private void startGame() {
        inputRow.remove(startBtn);
        revalidate();
        repaint();

        tidLabel.setText("Tid: 0");

        if (uiTimer != null && uiTimer.isRunning()) uiTimer.stop();
        uiTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            tidLabel.setText("Tid: " + elapsedSeconds);
        });
        uiTimer.start();

        answerField.setEnabled(true);
        enterBtn.setEnabled(true);
        startBtn.setEnabled(false);
        answerField.setText("");
        answerField.requestFocusInWindow();

        moveCorrect();
        layOutTrack();
        sender.println("Start");
    }
    //Stopper spil og viser din slut tid for at gennemføre banen
    private void stopGame() {
        if (uiTimer != null) uiTimer.stop();
        answerField.setEnabled(false);
        enterBtn.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Tid: "+elapsedSeconds+" sekunder");
    }
    //Aben hopper et palmetræ frem ved rigtig svar
    private void moveCorrect() {
        position = Math.min(goal, position + 1);

        updateMonkeyPosition();
    }
    //Aben hopper et palmetræ tilbage ved forkert svar
    private void moveWrong() {
        position = Math.max(1, position - 1);

        updateMonkeyPosition();
    }
    //Animerer abens hop
    private void animateHopTo(int targetX, int baseY) {
        int startX = abeLabel.getX();
        int frames = 18;
        int durationMs = 280;
        int delay = durationMs / frames;
        int maxJumpHeight = 55;

        if (hopTimer != null && hopTimer.isRunning()) hopTimer.stop();

        final int[] f = {0};

        hopTimer = new Timer(delay, ev -> {
            f[0]++;
            double t = f[0] / (double) frames;
            int x = (int) (startX + (targetX - startX) * t);
            int y = (int) (baseY - Math.sin(Math.PI * t) * maxJumpHeight);

            abeLabel.setLocation(x, y);
            trackPanel.revalidate();
            trackPanel.repaint();

            if (f[0] >= frames) {
                ((Timer) ev.getSource()).stop();
                abeLabel.setLocation(targetX, baseY);
                trackPanel.revalidate();
                trackPanel.repaint();
            }
        });
        hopTimer.start();
    }
    //Håndtere beskeder fra serveren
    private void handleServerMessages(String line) {
        System.out.println("Received: " + line);
        if (line.length() > 1) {
            String[] lineSplit = line.split(":");
            String action = lineSplit[0];
            String information = lineSplit[1];
            if (action.equals("Q")) {
                questionLabel.setText(information);
            }
        } else if (line.equals("C")) {
            moveCorrect();
        } else if (line.equals("I")) {
            moveWrong();
        } else if (line.equals("W")) {
            sender.println(username);
            moveCorrect();
            stopGame();
        }
    }
    //Sender spillerens svar til server
    private void sendAnswer() {
        String input = answerField.getText();
        sender.println("A:"+input);
        answerField.setText("");
        answerField.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startBtn) {
            startGame();
            return;
        }
        if (e.getSource() == enterBtn) {
            sendAnswer();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendAnswer();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {}
}