package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class Game extends JPanel implements ActionListener {

    public Game(Window window, String title) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        add(top, BorderLayout.PAGE_START);
        top.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JButton btnBack =  new JButton("Back");
        btnBack.setPreferredSize(new Dimension(100,50));
        btnBack.setFont(new Font("Arial",Font.BOLD,20));
        btnBack.addActionListener(_ -> window.MainMenuSetup());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font ("Arial", Font.BOLD, 48));

        top.add(btnBack, BorderLayout.LINE_START);
        top.add(titleLabel, BorderLayout.PAGE_START);
        this.setVisible(true);
    }
}

class Vendespil extends Game{
    private JPanel boardPanel;
    private JLabel timeLabel;
    private JLabel moveLabel;
    private final JButton[] cardList = new JButton[16];

    public  Vendespil(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window, title);
        setupWindow();
    }

    void setupWindow(){
        setLayout(new BorderLayout());

        boardPanel = new JPanel (new GridLayout(4,4,10,10));
        boardPanel.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));
        boardPanel.setBackground(new Color(5, 203, 252));

        Dimension cardSize = new Dimension(100,50);
        boardPanel.setPreferredSize(cardSize);

        for (int i = 0; i < 16; i++) {
            JButton card = new JButton(".");
            card.addActionListener(this);
            boardPanel.add(card);
            cardList[i] = card;
        }
        add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        infoPanel.setPreferredSize(new Dimension(100,50));
        infoPanel.setBackground(new Color(5, 203, 252));

        Font infoFont = new Font ("Arial",Font.BOLD,20);
        timeLabel = new JLabel("Tid: 0");
        timeLabel.setFont(infoFont);

        moveLabel = new JLabel ("Træk: 0");
        moveLabel.setFont(infoFont);

        infoPanel.add(timeLabel);
        infoPanel.add(moveLabel);

        add (infoPanel, BorderLayout.PAGE_END);

    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

class TabelKrig extends Game{

    public  TabelKrig(Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title);
        setupWindow();
    }

    void setupWindow(){

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

class FaldendeRegnestykker extends Game{

    public  FaldendeRegnestykker(Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title);
        setupWindow();
    }

    void setupWindow(){

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

