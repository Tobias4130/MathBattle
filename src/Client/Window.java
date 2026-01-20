package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Window extends JFrame implements ActionListener {

    String ip = "localhost";
    int port = 6000;
    JPanel menuContainer;
    JPanel usernameContainer;
    JButton submitButton;
    JTextField usernameField;
    public String username;

    public Window() {
        this.setTitle("MathBattle");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1200,800);
        this.setResizable(false);

        createGetUsernameUI();

        this.setVisible(true);
    }

    public void MainMenuSetup() {
        menuContainer = new JPanel(new BorderLayout(10,10));
        add(menuContainer);
        JLabel title =new JLabel("MathBattle", SwingConstants.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,48));
        menuContainer.add(title,BorderLayout.PAGE_START);

        JPanel menuPanel = new JPanel();
        menuContainer.add(menuPanel,BorderLayout.CENTER);
        menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10,20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(100,0,0,0));

        Dimension buttonSize = new Dimension(200, 200);
        Font gameButtonFont = new Font("Arial", Font.BOLD, 20);
        JButton btnVend = new JButton("Vendespil");
        JButton btnTab = new JButton("Tabeltræning");
        JButton btnRegn = new JButton("Regnestykker");
        JButton btnMonkey =  new JButton("Monkey Race");
        JButton btnLeaderBoard = new JButton ("Leaderboard");
        btnVend.addActionListener(this);
        btnTab.addActionListener(this);
        btnRegn.addActionListener(this);
        btnMonkey.addActionListener(this);
        btnLeaderBoard.addActionListener(this);

        btnVend.setFont(gameButtonFont);
        btnTab.setFont(gameButtonFont);
        btnRegn.setFont(gameButtonFont);
        btnMonkey.setFont(gameButtonFont);
        btnLeaderBoard.setFont((new  Font("Arial",Font.BOLD,24)));

        menuPanel.add(btnVend);
        menuPanel.add(btnTab);
        menuPanel.add(btnRegn);
        menuPanel.add(btnMonkey);

        btnVend.setPreferredSize(buttonSize);
        btnTab.setPreferredSize(buttonSize);
        btnRegn.setPreferredSize(buttonSize);
        btnMonkey.setPreferredSize(buttonSize);
        btnLeaderBoard.setPreferredSize((new Dimension(600,60)));


        btnVend.setBackground(new Color(186, 85, 211));
        btnTab.setBackground(new Color(5, 203, 252));
        btnRegn.setBackground(new Color(200, 200, 200));
        btnMonkey.setBackground(new Color(50, 205, 50));
        btnLeaderBoard.setBackground(new Color(255,215,0));


        for (JButton b : new JButton[]{btnVend, btnTab, btnRegn, btnMonkey, btnLeaderBoard}) {
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setContentAreaFilled(true);
            b.setForeground(Color.BLACK);
        }

        JPanel nede = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nede.setBorder((BorderFactory.createEmptyBorder(0,0,100,0)));
        nede.add(btnLeaderBoard);


        menuContainer.add(nede,BorderLayout.PAGE_END);
        this.revalidate();
        this.repaint();
    }

    private void createGetUsernameUI() {
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(400, 40));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        submitButton = new JButton("Choose username");
        submitButton.addActionListener(this);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameContainer = new JPanel();
        usernameContainer.setLayout(new BoxLayout(usernameContainer, BoxLayout.PAGE_AXIS));
        usernameContainer.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));

        JLabel enterYourUsernameLabel = new JLabel("Enter your username");
        enterYourUsernameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        enterYourUsernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameContainer.add(enterYourUsernameLabel);
        usernameContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        usernameContainer.add(usernameField);
        usernameContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        usernameContainer.add(submitButton);
        usernameContainer.add(Box.createVerticalGlue());

        this.getContentPane().add(usernameContainer, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonText = e.getActionCommand();
        if (Objects.equals(buttonText, "Vendespil")) {
            try {
                Socket socket = new Socket(ip, port);
                Scanner sockReceiver = new Scanner(socket.getInputStream());
                PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true);

                sockSender.println("Vendespil");
                Vendespil game = new Vendespil(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server: " + ex.getMessage());
            }
        } else if (Objects.equals(buttonText, "Tabeltræning")) {
            try {
                Socket socket = new Socket(ip, port);
                Scanner sockReceiver = new Scanner(socket.getInputStream());
                PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true);

                sockSender.println("Tabeltræning");
                TabelKrig game = new TabelKrig(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server: " + ex.getMessage());
            }
        } else if (Objects.equals(buttonText, "Regnestykker")) {
            try {
                Socket socket = new Socket(ip, port);
                Scanner sockReceiver = new Scanner(socket.getInputStream());
                PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true);

                sockSender.println("Regnestykker");
                simpleRegnestyk game = new simpleRegnestyk(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server: " + Arrays.toString(ex.getStackTrace()));
            }
        } else if (Objects.equals(buttonText, "Monkey Race")) {
            try {
                Socket socket = new Socket(ip, port);
                Scanner sockReceiver = new Scanner(socket.getInputStream());
                PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true);

                sockSender.println("Monkey Race");
                MonkeyRace game = new MonkeyRace(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server: " + Arrays.toString(ex.getStackTrace()));
            }

        } else if (Objects.equals(buttonText, "Leaderboard")) {
            try {
                Socket socket = new Socket(ip, port);
                Scanner sockReceiver = new Scanner(socket.getInputStream());
                PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true);

                sockSender.println("Leaderboard");
                Leaderboard leaderboard = new Leaderboard(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(leaderboard);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server: " + Arrays.toString(ex.getStackTrace()));
            }
        } else if (e.getSource() == submitButton) {
            String data = usernameField.getText();
            if (data.isEmpty()){
                return;
            }
            username = data;
            this.remove(usernameContainer);
            this.revalidate();
            this.repaint();
            MainMenuSetup();
        }
    }
}