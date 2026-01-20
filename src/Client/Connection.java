package Client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class Connection extends JPanel implements ActionListener {

    Socket socket;
    Scanner reader;
    PrintWriter sender;
    String username;

    public Connection(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        this.socket = socket;
        this.reader = reader;
        this.sender = sender;
        this.username = window.username;

        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        add(top, BorderLayout.PAGE_START);
        top.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        BasicArrowButton btnBack =  new BasicArrowButton(BasicArrowButton.WEST) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 50);
            }
        };
        btnBack.setPreferredSize(new Dimension(30,15));
        btnBack.setFont(new Font("Arial",Font.BOLD,20));
        btnBack.addActionListener(_ -> {window.getContentPane().remove(this);window.MainMenuSetup();this.revalidate();this.repaint();});

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font ("Arial", Font.BOLD, 48));

        top.add(btnBack, BorderLayout.LINE_START);
        top.add(titleLabel, BorderLayout.PAGE_START);
        this.setVisible(true);
    }
}