package Client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class Game extends JPanel implements ActionListener {

    Socket socket;
    Scanner reader;
    PrintWriter sender;

    public Game(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        this.socket = socket;
        this.reader = reader;
        this.sender = sender;

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

class Vendespil extends Game{
    private JButton startBtn;
    private final JButton[] cardList = new JButton[16];
    private JPanel lagContainer;
    private JPanel startButtonContainer;

    public  Vendespil(Window window, String title, Socket socket, Scanner reader, PrintWriter sender) {
        super(window, title, socket, reader, sender);
        setupWindow();
        revalidate();
        repaint();

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
        JLabel timeLabel = new JLabel("Tid: 0");
        timeLabel.setFont(infoFont);

        JLabel moveLabel = new JLabel("Træk: 0");
        moveLabel.setFont(infoFont);

        infoPanel.add(timeLabel);
        infoPanel.add(moveLabel);

        spilContainer.add(infoPanel, BorderLayout.PAGE_END);
        add(lagContainer,BorderLayout.CENTER);
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
            sender.println("START");
            System.out.println("Starting game");
        } else {
            sender.println("MOVE:"+e.getActionCommand());
            System.out.println("Sending move");
        }
    }
}

class TabelKrig extends Game{
    private JPanel boardPanel;
    private JTextField textField;
    private JButton enterBtn;
    private JButton startBtn;
    PrintWriter sender;
    private String selectedTable;
    Scanner reader;
    Socket socket;

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
        JPanel exercisePanel = new JPanel();
        exercisePanel.setBackground(new Color(5, 203, 252));
        exercisePanel.setLayout(new BoxLayout(exercisePanel, BoxLayout.PAGE_AXIS));
        exercisePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel titlePanel = new JLabel(tabel + " tabellen");
        titlePanel.setFont(new Font("Arial", Font.BOLD, 42));

        textField = new JTextField(JTextField.CENTER);
        Dimension size = new Dimension(320,50);
        textField.setPreferredSize(size);
        textField.setMaximumSize(size);
        textField.setFont(new Font("Arial", Font.BOLD, 28));

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

        exercisePanel.add(Box.createVerticalGlue());

        exercisePanel.add(titlePanel);
        exercisePanel.add(textField);
        exercisePanel.add(startBtn);
        exercisePanel.add(enterBtn);

        exercisePanel.add(Box.createVerticalGlue());

        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exercisePanel.add(Box.createVerticalStrut(30));
        exercisePanel.add(Box.createVerticalStrut(30));
        exercisePanel.add(Box.createVerticalStrut(30));
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

        }else if (e.getSource()==startBtn) {
            if (selectedTable != null) {
                sender.println("START:" + selectedTable);
                sender.flush();
                enterBtn.setEnabled(true);

            }
        }else if (e.getSource() == enterBtn) {
            String txt = textField.getText().trim();
            if (!txt.matches("-?\\d+")) {
                textField.setText("");
                return;
            }
            sender.println(txt);
            sender.flush();
            textField.setText("");

            if (reader.hasNextLine()) {
                String response = reader.nextLine().trim();

                if (response.equals("WRONG")) {
                    JOptionPane.showMessageDialog(this, "Forkert – prøv igen!");
                } else if (response.equals("DONE")) {
                    JOptionPane.showMessageDialog(this, "Tillykke du er færdig!");
                }
            }
        }
    }
}

class FaldendeRegnestykker extends Game{

    public  FaldendeRegnestykker(Window window, String title, Socket socket, Scanner reader, PrintWriter sender){
        super(window, title, socket, reader, sender);
        setupWindow();
    }

    void setupWindow(){

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

