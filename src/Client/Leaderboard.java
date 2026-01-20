package Client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Leaderboard extends Connection {
    private final JButton btnTotal = new JButton("Total");
    private final JButton btnVend = new JButton("Vendespil");
    private final JButton btnTabel = new JButton("TabelKrig");
    private final JButton btnRegn = new JButton("Regnestykker");
    private final JButton btnMonkey = new JButton("MonkeyRace");

    private final JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
    private final JPanel scoresContainer;

    public Leaderboard(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window,title,socket,reader,sender);

        BasicArrowButton btnBack =  new BasicArrowButton(BasicArrowButton.WEST) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 50);
            }
        };

        btnBack.setPreferredSize(new Dimension(30,15));
        btnBack.setFont(new Font("Arial",Font.BOLD,20));
        btnBack.addActionListener(_ -> {window.getContentPane().remove(this);window.MainMenuSetup();this.revalidate();this.repaint();});

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(255,215,0));
        setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnTotal.setFont(new Font("Arial", Font.BOLD, 26));
        btnTotal.setPreferredSize(new Dimension(800, 60));
        btnTotal.setMaximumSize(new Dimension(800, 60));
        btnTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTotal.setFocusPainted(false);
        btnTotal.addActionListener(this);

        JPanel gameButtons = new JPanel(new GridLayout(1, 3, 15, 0));
        gameButtons.setOpaque(false);
        gameButtons.setMaximumSize(new Dimension(800, 60));
        gameButtons.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font smallBtnFont = new Font("Arial", Font.BOLD, 22);

        btnVend.setFont(smallBtnFont);
        btnTabel.setFont(smallBtnFont);
        btnRegn.setFont(smallBtnFont);
        btnMonkey.setFont(smallBtnFont);

        btnVend.setFocusPainted(false);
        btnTabel.setFocusPainted(false);
        btnRegn.setFocusPainted(false);
        btnMonkey.setFocusPainted(false);

        btnVend.addActionListener(this);
        btnTabel.addActionListener(this);
        btnRegn.addActionListener(this);
        btnMonkey.addActionListener(this);


        gameButtons.add(btnVend);
        gameButtons.add(btnTabel);
        gameButtons.add(btnRegn);
        gameButtons.add(btnMonkey);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(btnBack, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(header);
        top.add(Box.createVerticalStrut(20));
        top.add(btnTotal);
        top.add(Box.createVerticalStrut(15));
        top.add(gameButtons);
        top.add(Box.createVerticalStrut(20));

        add(top, BorderLayout.PAGE_START);

        scoresContainer = new JPanel();
        scoresContainer.setLayout(new BoxLayout(scoresContainer, BoxLayout.Y_AXIS));
        scoresContainer.setOpaque(false);
        scoresContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        scoresContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnVend){
            sender.println("Vendespil");
        } else if (e.getSource() == btnTabel) {
            sender.println("TabelKrig");
        } else if (e.getSource() == btnRegn) {
            sender.println("simpleRegnestyk");
        } else if (e.getSource() == btnTotal) {
            sender.println("Total");
        } else if (e.getSource() == btnMonkey) {
            sender.println("MonkeyRace");
        }
    }

    void handleServerMessages(String line){
        scoresContainer.removeAll();
        String[] splitLine = line.split(";");
        for (int i = 0 ; i < splitLine.length; i++) {
            if (!splitLine[i].contains(",")){
                continue;
            }
            String user = splitLine[i].split(",")[0];
            String score = splitLine[i].split(",")[1];
            JLabel tempScore = new JLabel("Top "+i+" - "+user+": "+score);
            tempScore.setAlignmentX(Component.CENTER_ALIGNMENT);
            tempScore.setFont(new Font("Arial", Font.PLAIN, 24));
            scoresContainer.add(tempScore);
            scoresContainer.add(Box.createVerticalStrut(5));
        }
        if (scoresContainer.getParent() == null) {
            add(scoresContainer, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
}