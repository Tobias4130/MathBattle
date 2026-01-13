package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class Window extends JFrame implements ActionListener {

    String ip = "localhost";
    int port = 6000;
    JPanel menuContainer;

    public Window() {
        this.setTitle("MathBattle");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1200,800);
        this.setResizable(false);

        MainMenuSetup();

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

        Dimension buttonSize = new Dimension(300, 300);
        Font gameButtonFont = new Font("Arial", Font.BOLD, 20);
        JButton btnVend = new JButton("Vendespil");
        JButton btnTab = new JButton("Tabeltræning");
        JButton btnFald = new JButton("Faldende Regnestykker");
        btnVend.addActionListener(this);
        btnTab.addActionListener(this);
        btnFald.addActionListener(this);
        btnVend.setFont(gameButtonFont);
        btnTab.setFont(gameButtonFont);
        btnFald.setFont(gameButtonFont);
        menuPanel.add(btnVend);
        menuPanel.add(btnTab);
        menuPanel.add(btnFald);

        btnVend.setPreferredSize(buttonSize);
        btnTab.setPreferredSize(buttonSize);
        btnFald.setPreferredSize(buttonSize);

        JPanel nede = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nede.setBorder((BorderFactory.createEmptyBorder(0,0,100,0)));

        JButton btnLeaderBoard = new JButton ("Leaderboard");
        btnLeaderBoard.setPreferredSize((new Dimension(600,60)));
        btnLeaderBoard.setFont((new  Font("Arial",Font.BOLD,24)));

        nede.add(btnLeaderBoard);
        menuContainer.add(nede,BorderLayout.PAGE_END);
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
                System.out.println("Could not connect to server");
            }
        } else if (Objects.equals(e.getActionCommand(), "Tabeltræning")) {
            try (Socket socket = new Socket(ip, port); Scanner sockReceiver = new Scanner(socket.getInputStream()); PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true)) {

                sockSender.println("Tabeltræning");
                TabelKrig game = new TabelKrig(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server");
            }
        } else if (Objects.equals(e.getActionCommand(), "Faldende Regnestykker")) {
            try (Socket socket = new Socket(ip, port); Scanner sockReceiver = new Scanner(socket.getInputStream()); PrintWriter sockSender = new PrintWriter(socket.getOutputStream(), true)) {

                sockSender.println("Faldende Regnestykker");
                FaldendeRegnestykker game = new FaldendeRegnestykker(this, buttonText, socket, sockReceiver, sockSender);
                getContentPane().remove(menuContainer);
                this.add(game);
                this.revalidate();
                this.repaint();
            } catch (Exception ex) {
                System.out.println("Could not connect to server");
            }
        }
    }
}


