package Client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import java.util.Random;


public class MonkeyRace extends Connection {

    private boolean animating = false;
    private Timer hopTimer;

    private JButton startBtn;
    private JLabel tidLabel;

    private JPanel trackPanel;
    private JLabel abeLabel;
    private JLabel finishLabel;

    private JLabel questionLabel;
    private JTextField answerField;
    private JButton enterBtn;

    private final Random random = new Random();
    private int a, b;
    private char op;
    private int expected;

    private int position = 0;
    private final int goal = 8;

    private Timer uiTimer;
    private int elapsedSeconds = 0;

    private final JLabel[] treeLabels = new JLabel[8];
    private final Point[] treePoints = new Point[8];

    public MonkeyRace(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window, title, socket, reader, sender);
        setUpWindow();
    }

    private void setUpWindow() {
        setBackground(new Color(50, 205, 50));
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        startBtn = new JButton("Start");
        startBtn.setFont(new Font("Arial", Font.BOLD, 22));
        startBtn.setPreferredSize(new Dimension(140, 50));
        startBtn.addActionListener(this);

        tidLabel = new JLabel("Tid: 0", SwingConstants.RIGHT);
        tidLabel.setFont(new Font("Arial", Font.BOLD, 22));

        top.add(startBtn, BorderLayout.LINE_START);
        top.add(tidLabel, BorderLayout.LINE_END);
        add(top, BorderLayout.PAGE_START);

        trackPanel = new JPanel(null);
        trackPanel.setOpaque(false);
        trackPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        ImageIcon icon = new ImageIcon("src/Client/Images/monkey.png");
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        abeLabel = new JLabel(new ImageIcon(img));
        abeLabel.setSize(80, 80);

        ImageIcon iconFinish = new ImageIcon(("src/Client/Images/banana.png"));
        Image imgFinish = iconFinish.getImage().getScaledInstance(80,80, Image.SCALE_SMOOTH);
        finishLabel = new JLabel(new ImageIcon(imgFinish));
        finishLabel.setSize(80,80);


        trackPanel.add(abeLabel);
        trackPanel.add(finishLabel);

        ImageIcon palmIcon = new ImageIcon("src/Client/Images/palm-tree.png");
        Image palmImg = palmIcon.getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH);
        ImageIcon scaledPalm = new ImageIcon(palmImg);

        for (int i = 0; i < treeLabels.length; i++) {
            JLabel tree = new JLabel(scaledPalm);
            tree.setSize(120, 160);
            treeLabels[i] = tree;
            trackPanel.add(tree);
        }

        add(trackPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 25, 20));

        questionLabel = new JLabel("Tryk Start", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 28));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputRow.setOpaque(false);

        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(200, 45));
        answerField.setFont(new Font("Arial", Font.BOLD, 24));
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.setEnabled(false);

        enterBtn = new JButton("Enter");
        enterBtn.setFont(new Font("Arial", Font.BOLD, 22));
        enterBtn.setPreferredSize(new Dimension(140, 45));
        enterBtn.setEnabled(false);
        enterBtn.addActionListener(this);

        inputRow.add(answerField);
        inputRow.add(enterBtn);

        bottom.add(questionLabel);
        bottom.add(inputRow);

        add(bottom, BorderLayout.PAGE_END);

        SwingUtilities.invokeLater(this::layoutTrack);
    }

    private void layoutTrack() {
        int w = trackPanel.getWidth();
        int h = trackPanel.getHeight();
        int y = Math.max(60, h / 2 - 50);

        int startX = 60;
        int finishX = Math.max(startX + 300, w - 140);

        finishLabel.setLocation(finishX, y);
        int usable = finishX - startX;
        for (int i = 0; i < 8; i++) {
            double t = (i + 1) / 9.0;
            int tx = (int) (startX + t * usable);
            int ty = y;

            treePoints[i] = new Point(tx, ty);
            treeLabels[i].setLocation(tx, ty);
        }
        updateMonkeyPosition(startX, finishX, y);
    }

    private void updateMonkeyPosition(int startX, int finishX, int y) {
        int yOffset = -50; // så aben står lidt over træet

        if (position == 0) {
            abeLabel.setLocation(startX, y);
        } else if (position >= goal) {
            abeLabel.setLocation(finishX, y);
        } else {
            Point p = treePoints[position - 1];
            abeLabel.setLocation(p.x, p.y + yOffset);
        }
        trackPanel.revalidate();
        trackPanel.repaint();
    }

    private void startGame() {
        tidLabel.setText("Tid: 0");

        if (uiTimer != null && uiTimer.isRunning()) uiTimer.stop();
        uiTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            tidLabel.setText("Tid: " + elapsedSeconds);
        });
        uiTimer.start();

        answerField.setEnabled(true);
        enterBtn.setEnabled(true);
        answerField.setText("");
        answerField.requestFocusInWindow();

        moveCorrect();
        nextQuestion();
        layoutTrack();
    }
    private void stopGame() {
        if (uiTimer != null) uiTimer.stop();
        answerField.setEnabled(false);
        enterBtn.setEnabled(false);

        JOptionPane.showMessageDialog(this,
                "Tillykke! Aben nåede målstregen.\nTid: " + elapsedSeconds + " sekunder");
    }

    private void nextQuestion() {
        a = random.nextInt(91) + 10;
        b = random.nextInt(91) + 10;

        if (random.nextBoolean()) {
            op = '+';
            expected = a + b;
        } else {
            op = '-';
            expected = a - b;
        }
        questionLabel.setText(a + " " + op + " " + b + " = ?");
    }

    private void checkAnswer() {

        String txt = answerField.getText().trim();
        answerField.setText("");

        if (!txt.matches("-?\\d+")) {
            moveWrong();
            return;
        }

        int guess = Integer.parseInt(txt);
        if (guess == expected) {
            moveCorrect();
        } else {
            moveWrong();
        }
    }

    private void moveCorrect() {
        position = Math.min(goal, position + 1);

        int w = trackPanel.getWidth();
        int h = trackPanel.getHeight();
        int y = Math.max(60, h / 2 - 50);
        int startX = 60;
        int finishX = Math.max(startX + 300, w - 140);

        updateMonkeyPosition(startX, finishX, y);

        if (position >= goal) {
            stopGame();
        } else {
            nextQuestion();
        }
    }

    private void moveWrong() {
        position = Math.max(0, position - 1);

        int w = trackPanel.getWidth();
        int h = trackPanel.getHeight();
        int y = Math.max(60, h / 2 - 50);
        int startX = 60;
        int finishX = Math.max(startX + 300, w - 140);

        updateMonkeyPosition(startX, finishX, y);

        JOptionPane.showMessageDialog(this, "Forkert – aben hopper tilbage!");
    }

    private void animateHopTo(int targetX, int baseY) {
        if (animating) return;
        animating = true;

        int startX = abeLabel.getX();
        int startY = abeLabel.getY();

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
                animating = false;

                if (position >= goal) {
                    stopGame();
                }
            }
        });

        hopTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startBtn) {
            startGame();
            return;
        }
        if (e.getSource() == enterBtn) {
            checkAnswer();
        }
    }
}