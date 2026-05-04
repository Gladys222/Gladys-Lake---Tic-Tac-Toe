package tictactoe;

// Explicit imports — NO wildcards to avoid Timer ambiguity
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;          // javax.swing.Timer — explicit, no ambiguity
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * SwingUI — Tic-Tac-Toe with Original, Continuous, and Challenge modes.
 *
 * Three screens via CardLayout:
 *   MENU      — enter names, pick PvP/PvC, choose Original/Continuous/Challenge
 *   CHALLENGE — pick any combo of C1-C4 modifiers
 *   GAME      — the board
 *
 * Challenge modifiers:
 *   C1 Time Pressure : 3-second countdown per human turn; auto-move on expiry
 *   C2 Piece Stealer : spend your turn blanking one opponent piece
 *   C3 Vanishing Act : 1-2 random pieces disappear mid-game (never awards a win)
 *   C4 Big Board     : 4x4 or 5x5 grid, need 4-in-a-row to win
 */
public class SwingUI extends JFrame
{
    // ── Colours (all static final so static nested classes can access them) ──
    static final Color BG          = new Color(173, 216, 230);
    static final Color GRID_LINE   = new Color( 80, 145, 190);
    static final Color BTN_DEFAULT = new Color(145, 198, 222);
    static final Color BTN_HOVER   = new Color(188, 226, 244);
    static final Color X_YELLOW    = new Color(255, 244, 110);
    static final Color X_SHADOW    = new Color(185, 148,   8);
    static final Color O_BLUE      = new Color( 16,  44, 100);
    static final Color O_SHADOW    = new Color(  6,  18,  50);
    static final Color TEXT_DARK   = new Color( 16,  44, 100);
    static final Color WIN_COLOR   = new Color( 28, 155,  75);
    static final Color DRAW_COLOR  = new Color(195, 125,   8);
    static final Color WARN_COLOR  = new Color(220,  60,  60);

    // ── App state ────────────────────────────────────────────────
    private String  xName      = "Player 1";
    private String  oName      = "Player 2";
    private boolean vsComputer = false;
    private boolean continuous = false;
    private boolean challenge  = false;
    private int     xWins = 0, oWins = 0, draws = 0;

    private final ChallengeSettings cs = new ChallengeSettings();

    // ── Core game objects ─────────────────────────────────────────
    private final Board     board  = new Board("board.csv");
    private final GameLogic logic  = new GameLogic();
    private ComputerPlayer  computer;

    // ── Card screens ─────────────────────────────────────────────
    private static final String SCREEN_MENU      = "MENU";
    private static final String SCREEN_CHALLENGE = "CHALLENGE";
    private static final String SCREEN_GAME      = "GAME";

    private final CardLayout     cards = new CardLayout();
    private final JPanel         root  = new JPanel(cards);
    private       MenuPanel      menuPanel;
    private       ChallengePanel challengePanel;
    private       GamePanel      gamePanel;

    // ─────────────────────────────────────────────────────────────
    public SwingUI()
    {
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 920);
        setMinimumSize(new Dimension(660, 750));
        setLocationRelativeTo(null);
        setResizable(true);
        root.setBackground(BG);

        menuPanel      = new MenuPanel();
        challengePanel = new ChallengePanel();
        gamePanel      = new GamePanel();

        root.add(menuPanel,      SCREEN_MENU);
        root.add(challengePanel, SCREEN_CHALLENGE);
        root.add(gamePanel,      SCREEN_GAME);

        setContentPane(root);
        showMenu();
        setVisible(true);
    }

    // ── Navigation ───────────────────────────────────────────────
    private void showMenu()
    {
        menuPanel.syncFromApp();
        cards.show(root, SCREEN_MENU);
    }

    private void showChallengeSetup()
    {
        challengePanel.reset();
        cards.show(root, SCREEN_CHALLENGE);
    }

    private void startGame()
    {
        xWins = 0; oWins = 0; draws = 0;
        computer = vsComputer ? new ComputerPlayer(logic, 'O') : null;
        gamePanel.beginSession();
        cards.show(root, SCREEN_GAME);
    }

    private void goBackToMenu()
    {
        MusicPlayer.playMenuClick();
        gamePanel.stopCelebration();
        showMenu();
    }

    // ════════════════════════════════════════════════════════════
    //  MENU SCREEN
    // ════════════════════════════════════════════════════════════
    private class MenuPanel extends JPanel
    {
        private final JTextField    field1, field2;
        private final JToggleButton btnPvP, btnPvC;
        private final JToggleButton btnOriginal, btnContinuous, btnChallenge;
        private final JLabel        oLabel;

        MenuPanel()
        {
            setBackground(BG);
            setLayout(new GridBagLayout());

            JPanel inner = new JPanel();
            inner.setBackground(BG);
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setBorder(BorderFactory.createEmptyBorder(20, 50, 24, 50));

            inner.add(centeredLabel("TIC  TAC  TOE",
                new Font("Georgia", Font.BOLD, 46), TEXT_DARK));
            inner.add(centeredLabel("The upgraded version",
                new Font("Georgia", Font.ITALIC, 15), new Color(70, 110, 150)));
            inner.add(gap(24));
            inner.add(divider());
            inner.add(gap(18));

            // ── Player names ──────────────────────────────────────
            inner.add(centeredLabel("Player Names",
                new Font("Georgia", Font.BOLD, 17), TEXT_DARK));
            inner.add(gap(12));

            field1 = styledField("Player 1");
            inner.add(labeledRow("Player 1  (plays X)", field1));
            inner.add(gap(10));

            oLabel = new JLabel("Player 2  (plays O)");
            oLabel.setFont(new Font("Georgia", Font.BOLD, 13));
            oLabel.setForeground(new Color(50, 90, 130));
            oLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            field2 = styledField("Player 2");

            JPanel oRow = new JPanel();
            oRow.setBackground(BG);
            oRow.setLayout(new BoxLayout(oRow, BoxLayout.Y_AXIS));
            oRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            oRow.add(oLabel);
            oRow.add(gap(4));
            oRow.add(field2);
            inner.add(oRow);
            inner.add(gap(22));
            inner.add(divider());
            inner.add(gap(18));

            // ── Opponent ──────────────────────────────────────────
            inner.add(centeredLabel("Opponent",
                new Font("Georgia", Font.BOLD, 17), TEXT_DARK));
            inner.add(gap(12));

            btnPvP = modeToggle("vs Player",   true,  new Color(45, 110, 185));
            btnPvC = modeToggle("vs Computer", false, new Color(45, 110, 185));

            btnPvP.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btnPvP.setSelected(true);
                    btnPvC.setSelected(false);
                    field2.setEnabled(true);
                    oLabel.setText("Player 2  (plays O)");
                }
            });
            btnPvC.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btnPvC.setSelected(true);
                    btnPvP.setSelected(false);
                    field2.setEnabled(false);
                    oLabel.setText("Computer  (plays O)");
                }
            });

            JPanel oppRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            oppRow.setBackground(BG);
            oppRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            oppRow.add(btnPvP);
            oppRow.add(btnPvC);
            inner.add(oppRow);
            inner.add(gap(22));
            inner.add(divider());
            inner.add(gap(18));

            // ── Game mode ─────────────────────────────────────────
            inner.add(centeredLabel("Game Mode",
                new Font("Georgia", Font.BOLD, 17), TEXT_DARK));
            inner.add(gap(12));

            btnOriginal   = modeToggle("Original",   true,  new Color(45, 110, 185));
            btnContinuous = modeToggle("Continuous",  false, new Color(45, 110, 185));
            btnChallenge  = modeToggle("Challenge",   false, new Color(200, 60,  60));

            btnOriginal.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btnOriginal.setSelected(true);
                    btnContinuous.setSelected(false);
                    btnChallenge.setSelected(false);
                }
            });
            btnContinuous.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btnContinuous.setSelected(true);
                    btnOriginal.setSelected(false);
                    btnChallenge.setSelected(false);
                }
            });
            btnChallenge.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btnChallenge.setSelected(true);
                    btnOriginal.setSelected(false);
                    btnContinuous.setSelected(false);
                }
            });

            JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            modeRow.setBackground(BG);
            modeRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            modeRow.add(btnOriginal);
            modeRow.add(btnContinuous);
            modeRow.add(btnChallenge);
            inner.add(modeRow);
            inner.add(gap(5));
            inner.add(centeredLabel(
                "Challenge: mix special rules to spice up the game!",
                new Font("Georgia", Font.ITALIC, 12), new Color(80, 120, 160)));
            inner.add(gap(28));

            BigButton play = new BigButton("Let's Play!");
            play.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    applyAndStart();
                }
            });
            inner.add(play);
            add(inner);
        }

        private void applyAndStart()
        {
            String n1 = field1.getText().trim();
            String n2 = field2.getText().trim();
            xName      = n1.isEmpty() ? "Player 1" : n1;
            vsComputer = btnPvC.isSelected();
            oName      = vsComputer ? "Computer" : (n2.isEmpty() ? "Player 2" : n2);
            continuous = btnContinuous.isSelected();
            challenge  = btnChallenge.isSelected();
            if (challenge) showChallengeSetup();
            else           startGame();
        }

        void syncFromApp()
        {
            field1.setText(xName);
            field2.setText(vsComputer ? "" : oName);
            field2.setEnabled(!vsComputer);
            oLabel.setText(vsComputer ? "Computer  (plays O)" : "Player 2  (plays O)");
            btnPvP.setSelected(!vsComputer);
            btnPvC.setSelected(vsComputer);
            btnOriginal.setSelected(!continuous && !challenge);
            btnContinuous.setSelected(continuous && !challenge);
            btnChallenge.setSelected(challenge);
        }

        // ── helpers ───────────────────────────────────────────────
        private JLabel centeredLabel(String t, Font f, Color c)
        {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setFont(f); l.setForeground(c);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            return l;
        }
        private Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
        private JSeparator divider()
        {
            JSeparator s = new JSeparator();
            s.setForeground(new Color(130, 185, 215));
            s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            return s;
        }
        private JTextField styledField(String placeholder)
        {
            JTextField f = new JTextField(placeholder, 18);
            f.setFont(new Font("Georgia", Font.PLAIN, 15));
            f.setForeground(TEXT_DARK);
            f.setBackground(new Color(200, 232, 245));
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(110, 170, 210), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            f.setMaximumSize(new Dimension(320, 40));
            return f;
        }
        private JPanel labeledRow(String labelText, JTextField field)
        {
            JPanel p = new JPanel();
            p.setBackground(BG);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel l = new JLabel(labelText);
            l.setFont(new Font("Georgia", Font.BOLD, 13));
            l.setForeground(new Color(50, 90, 130));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l); p.add(Box.createRigidArea(new Dimension(0, 4))); p.add(field);
            return p;
        }
        private JToggleButton modeToggle(String text, boolean selected, final Color activeColor)
        {
            JToggleButton b = new JToggleButton(text, selected)
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected() ? activeColor : new Color(150, 200, 228));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            b.setFont(new Font("Georgia", Font.BOLD, 13));
            b.setForeground(selected ? Color.WHITE : TEXT_DARK);
            b.setOpaque(false); b.setBorderPainted(false); b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(155, 42));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CHALLENGE SETUP SCREEN
    // ════════════════════════════════════════════════════════════
    private class ChallengePanel extends JPanel
    {
        private final JCheckBox      chkTime, chkSteal, chkVanish, chkBigBoard;
        private final JToggleButton  btn4x4, btn5x5;

        ChallengePanel()
        {
            setBackground(BG);
            setLayout(new GridBagLayout());

            JPanel inner = new JPanel();
            inner.setBackground(BG);
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setBorder(BorderFactory.createEmptyBorder(28, 50, 28, 50));

            inner.add(centeredLabel("CHALLENGE  MODE",
                new Font("Georgia", Font.BOLD, 34), new Color(200, 60, 60)));
            inner.add(centeredLabel("Mix and match any combination of these rules:",
                new Font("Georgia", Font.ITALIC, 14), new Color(80, 100, 140)));
            inner.add(gap(22));
            inner.add(divider());
            inner.add(gap(18));

            chkTime = ruleBox(
                "C1  Time Pressure",
                "Each player has only 3 seconds per turn — or a random move is placed for you!");
            chkSteal = ruleBox(
                "C2  Piece Stealer",
                "On your turn, hit the Steal button then click an opponent piece to erase it.");
            chkVanish = ruleBox(
                "C3  Vanishing Act",
                "1-2 random pieces vanish mid-game. Nobody controls when or which ones go!");
            chkBigBoard = ruleBox(
                "C4  Big Board",
                "Play on a 4x4 or 5x5 grid — still need 4 in a row to win.");

            inner.add(chkTime);    inner.add(gap(14));
            inner.add(chkSteal);   inner.add(gap(14));
            inner.add(chkVanish);  inner.add(gap(14));
            inner.add(chkBigBoard); inner.add(gap(6));

            // Board size sub-row (enabled only when C4 checked)
            btn4x4 = sizeToggle("4 x 4", true);
            btn5x5 = sizeToggle("5 x 5", false);
            btn4x4.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btn4x4.setSelected(true); btn5x5.setSelected(false);
                }
            });
            btn5x5.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btn5x5.setSelected(true); btn4x4.setSelected(false);
                }
            });
            btn4x4.setEnabled(false); btn5x5.setEnabled(false);

            chkBigBoard.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    btn4x4.setEnabled(chkBigBoard.isSelected());
                    btn5x5.setEnabled(chkBigBoard.isSelected());
                }
            });

            JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
            sizeRow.setBackground(BG);
            sizeRow.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel sizeLabel = new JLabel("Board size: ");
            sizeLabel.setFont(new Font("Georgia", Font.PLAIN, 13));
            sizeLabel.setForeground(TEXT_DARK);
            sizeRow.add(sizeLabel); sizeRow.add(btn4x4); sizeRow.add(btn5x5);
            inner.add(sizeRow);

            inner.add(gap(28));
            inner.add(divider());
            inner.add(gap(22));

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
            btns.setBackground(BG);
            btns.setAlignmentX(Component.CENTER_ALIGNMENT);

            RoundedButton back = new RoundedButton("Back", new Color(120, 170, 205), 120, 46);
            back.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick(); showMenu();
                }
            });
            BigButton go = new BigButton("Start Challenge!");
            go.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick(); applyAndStart();
                }
            });
            btns.add(back); btns.add(go);
            inner.add(btns);
            add(inner);
        }

        private void applyAndStart()
        {
            cs.timePressure = chkTime.isSelected();
            cs.pieceStealer = chkSteal.isSelected();
            cs.vanishingAct = chkVanish.isSelected();
            cs.bigBoard     = chkBigBoard.isSelected();
            if (cs.bigBoard)
            {
                cs.boardSize = btn5x5.isSelected() ? 5 : 4;
                cs.winLength = 4;
            }
            else
            {
                cs.boardSize = 3;
                cs.winLength = 3;
            }
            startGame();
        }

        void reset()
        {
            chkTime.setSelected(false); chkSteal.setSelected(false);
            chkVanish.setSelected(false); chkBigBoard.setSelected(false);
            btn4x4.setSelected(true); btn5x5.setSelected(false);
            btn4x4.setEnabled(false); btn5x5.setEnabled(false);
        }

        // ── helpers ───────────────────────────────────────────────
        private JLabel centeredLabel(String t, Font f, Color c)
        {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setFont(f); l.setForeground(c);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            return l;
        }
        private Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
        private JSeparator divider()
        {
            JSeparator s = new JSeparator();
            s.setForeground(new Color(130, 185, 215));
            s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            return s;
        }
        private JCheckBox ruleBox(String title, String desc)
        {
            JCheckBox cb = new JCheckBox(
                "<html><b>" + title + "</b><br>"
                + "<font color='#3C5070'>" + desc + "</font></html>");
            cb.setFont(new Font("Georgia", Font.PLAIN, 13));
            cb.setForeground(TEXT_DARK); cb.setBackground(BG);
            cb.setAlignmentX(Component.CENTER_ALIGNMENT);
            cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cb.setMaximumSize(new Dimension(580, 70));
            cb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { MusicPlayer.playMenuClick(); }
            });
            return cb;
        }
        private JToggleButton sizeToggle(String text, boolean selected)
        {
            JToggleButton b = new JToggleButton(text, selected)
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected() ? new Color(45, 110, 185) : new Color(150, 200, 228));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            b.setFont(new Font("Georgia", Font.BOLD, 12));
            b.setForeground(selected ? Color.WHITE : TEXT_DARK);
            b.setOpaque(false); b.setBorderPainted(false); b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(90, 34));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  GAME SCREEN
    // ════════════════════════════════════════════════════════════
    private class GamePanel extends JPanel
    {
        // Grid
        private AnimatedButton[][] buttons;
        private int gridSize = 3;

        // Header labels
        private JLabel statusLabel;
        private JLabel scoreLabel;
        private JLabel timerLabel;
        private JLabel challengeLabel;

        // Footer buttons
        private JButton nextRoundBtn;
        private JButton stealBtn;

        // Layered pane for confetti overlay
        private final JLayeredPane     layered     = new JLayeredPane();
        private final JPanel           mainContent = new JPanel(new BorderLayout());
        private final CelebrationPanel cel         = new CelebrationPanel();

        // C2: steal state
        private boolean stealModeActive = false;

        // Challenge board (non-null only in challenge mode)
        private DynamicBoard dynBoard = null;

        // Timers — javax.swing.Timer (explicit import, no ambiguity)
        private Timer countdownTimer  = null;
        private Timer vanishScheduler = null;
        private int   secondsLeft     = 3;

        // ── Constructor ───────────────────────────────────────────
        GamePanel()
        {
            setLayout(new BorderLayout());
            setBackground(BG);
            add(layered, BorderLayout.CENTER);

            mainContent.setBackground(BG);
            layered.add(mainContent, JLayeredPane.DEFAULT_LAYER);
            cel.setOpaque(false);
            layered.add(cel, JLayeredPane.POPUP_LAYER);

            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent e) { syncLayers(); }
            });

            buildHeader();
            buildGridContainer();
            buildFooter();
        }

        private void syncLayers()
        {
            Dimension d = layered.getSize();
            mainContent.setBounds(0, 0, d.width, d.height);
            cel.setBounds(0, 0, d.width, d.height);
        }

        // ── Build header ──────────────────────────────────────────
        private void buildHeader()
        {
            JPanel h = new JPanel();
            h.setBackground(BG);
            h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
            h.setBorder(BorderFactory.createEmptyBorder(18, 20, 4, 20));

            JLabel title = new JLabel("TIC  TAC  TOE", SwingConstants.CENTER);
            title.setFont(new Font("Georgia", Font.BOLD, 36));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            challengeLabel = new JLabel(" ", SwingConstants.CENTER);
            challengeLabel.setFont(new Font("Georgia", Font.ITALIC, 12));
            challengeLabel.setForeground(new Color(180, 60, 60));
            challengeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            timerLabel = new JLabel(" ", SwingConstants.CENTER);
            timerLabel.setFont(new Font("Georgia", Font.BOLD, 26));
            timerLabel.setForeground(WARN_COLOR);
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            scoreLabel = new JLabel(" ", SwingConstants.CENTER);
            scoreLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
            scoreLabel.setForeground(new Color(50, 90, 130));
            scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

            statusLabel = new JLabel(" ", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Georgia", Font.BOLD, 18));
            statusLabel.setForeground(new Color(150, 110, 0));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

            h.add(title);
            h.add(challengeLabel);
            h.add(timerLabel);
            h.add(scoreLabel);
            h.add(statusLabel);
            mainContent.add(h, BorderLayout.NORTH);
        }

        // ── Grid ──────────────────────────────────────────────────
        private final JPanel gridContainer = new JPanel(new GridBagLayout());

        private void buildGridContainer()
        {
            gridContainer.setBackground(BG);
            mainContent.add(gridContainer, BorderLayout.CENTER);
        }

        private void rebuildGrid(int size)
        {
            gridContainer.removeAll();
            gridSize = size;
            buttons  = new AnimatedButton[size][size];

            int cellSize  = (size == 3) ? 160 : (size == 4) ? 120 : 95;
            int totalSize = size * cellSize + (size - 1) * 6 + 16;

            JPanel grid = new JPanel(new GridLayout(size, size, 6, 6));
            grid.setBackground(GRID_LINE);
            grid.setBorder(BorderFactory.createLineBorder(GRID_LINE, 8, true));
            grid.setPreferredSize(new Dimension(totalSize, totalSize));

            for (int r = 0; r < size; r++)
            {
                for (int c = 0; c < size; c++)
                {
                    AnimatedButton btn = new AnimatedButton();
                    final int row = r, col = c;
                    btn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e) { handleCellClick(row, col); }
                    });
                    buttons[r][c] = btn;
                    grid.add(btn);
                }
            }

            gridContainer.add(grid);
            gridContainer.revalidate();
            gridContainer.repaint();
        }

        // ── Build footer ──────────────────────────────────────────
        private void buildFooter()
        {
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
            foot.setBackground(BG);

            RoundedButton backBtn = new RoundedButton("Menu", new Color(120, 170, 205), 120, 44);
            backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { stopAllTimers(); goBackToMenu(); }
            });

            RoundedButton newGameBtn = new RoundedButton("New Game", new Color(100, 160, 210), 140, 44);
            newGameBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick(); stopAllTimers(); resetGame();
                }
            });

            nextRoundBtn = new RoundedButton("Next Round", new Color(45, 140, 85), 140, 44);
            ((RoundedButton) nextRoundBtn).setLabelColor(Color.WHITE);
            nextRoundBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick(); stopAllTimers(); nextRound();
                }
            });
            nextRoundBtn.setVisible(false);

            // C2 steal button — label shows how many steals remain
            stealBtn = new RoundedButton("Steal (2 left)", new Color(200, 80, 60), 145, 44);
            ((RoundedButton) stealBtn).setLabelColor(Color.WHITE);
            stealBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MusicPlayer.playMenuClick();
                    if (dynBoard == null) return;
                    char current = dynBoard.currentPlayer();
                    // Only the human's turn can toggle steal
                    if (vsComputer && current == 'O') return;
                    // Check this player still has steals left
                    if (!dynBoard.canSteal(current))
                    {
                        statusLabel.setText("You have used all " + DynamicBoard.MAX_STEALS + " steals!");
                        return;
                    }
                    stealModeActive = !stealModeActive;
                    updateStealButton();
                }
            });
            stealBtn.setVisible(false);

            foot.add(backBtn);
            foot.add(newGameBtn);
            foot.add(nextRoundBtn);
            foot.add(stealBtn);
            mainContent.add(foot, BorderLayout.SOUTH);
        }

        // Updates steal button text and colour based on mode + steals left
        private void updateStealButton()
        {
            if (dynBoard == null) { stealBtn.setVisible(false); return; }
            char current = dynBoard.currentPlayer();
            int  left    = DynamicBoard.MAX_STEALS - dynBoard.stealsUsed(current);
            if (stealModeActive)
                stealBtn.setText("Stealing... (" + left + " left)");
            else
                stealBtn.setText("Steal (" + left + " left)");
            stealBtn.repaint();
        }

        // ── Session lifecycle ─────────────────────────────────────
        void beginSession()
        {
            stealModeActive = false;

            if (challenge)
            {
                dynBoard = new DynamicBoard(cs.boardSize);
                challengeLabel.setText(cs.summary());
                rebuildGrid(cs.boardSize);
            }
            else
            {
                dynBoard = null;
                challengeLabel.setText(" ");
                rebuildGrid(3);
                board.clearBoard();
            }

            cel.stop();
            clearButtons();
            nextRoundBtn.setVisible(false);
            stealBtn.setVisible(challenge && cs.pieceStealer);
            if (challenge && cs.pieceStealer) updateStealButton();
            timerLabel.setText(" ");
            refreshScore();
            refreshStatus();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { syncLayers(); }
            });

            if (challenge && cs.vanishingAct) scheduleVanish();
            maybeComputerMove();
        }

        private void resetGame()
        {
            stealModeActive = false;
            xWins = 0; oWins = 0; draws = 0;
            if (challenge && dynBoard != null) dynBoard.clear();
            else board.clearBoard();
            cel.stop();
            clearButtons();
            nextRoundBtn.setVisible(false);
            if (challenge && cs.pieceStealer) updateStealButton();
            timerLabel.setText(" ");
            refreshScore();
            refreshStatus();
            if (challenge && cs.vanishingAct) scheduleVanish();
            maybeComputerMove();
        }

        private void nextRound()
        {
            stealModeActive = false;
            if (challenge && dynBoard != null) dynBoard.clear();
            else board.clearBoard();
            cel.stop();
            clearButtons();
            nextRoundBtn.setVisible(false);
            if (challenge && cs.pieceStealer) updateStealButton();
            timerLabel.setText(" ");
            refreshStatus();
            if (challenge && cs.vanishingAct) scheduleVanish();
            maybeComputerMove();
        }

        void stopCelebration() { cel.stop(); }

        private void clearButtons()
        {
            if (buttons == null) return;
            for (int r = 0; r < gridSize; r++)
                for (int c = 0; c < gridSize; c++)
                    buttons[r][c].reset();
        }

        // ── Timer helpers ─────────────────────────────────────────
        private void stopAllTimers()
        {
            if (countdownTimer  != null) { countdownTimer.stop();  countdownTimer  = null; }
            if (vanishScheduler != null) { vanishScheduler.stop(); vanishScheduler = null; }
            timerLabel.setText(" ");
        }

        private void stopCountdown()
        {
            if (countdownTimer != null) { countdownTimer.stop(); countdownTimer = null; }
        }

        // ── C1 Countdown ──────────────────────────────────────────
        private void startCountdown()
        {
            if (!challenge || !cs.timePressure) return;
            stopCountdown();
            secondsLeft = 3;
            updateTimerLabel();
            countdownTimer = new Timer(1000, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    secondsLeft--;
                    if (secondsLeft <= 0)
                    {
                        countdownTimer.stop();
                        countdownTimer = null;
                        timerLabel.setForeground(WARN_COLOR);
                        timerLabel.setText("Time's up!");
                        forceRandomMove();
                    }
                    else
                    {
                        updateTimerLabel();
                    }
                }
            });
            countdownTimer.start();
        }

        private void updateTimerLabel()
        {
            timerLabel.setForeground(secondsLeft <= 1 ? WARN_COLOR : new Color(80, 140, 60));
            timerLabel.setText(secondsLeft + "s");
        }

        private void forceRandomMove()
        {
            if (isGameOver()) return;
            // Cancel any steal mode — random move is always a placement
            stealModeActive = false;
            List<int[]> empty = (dynBoard != null) ? dynBoard.emptyCells() : classicEmpty();
            if (empty.isEmpty()) return;
            int[] cell = empty.get(new Random().nextInt(empty.size()));
            MusicPlayer.playClickSound();
            doPlace(cell[0], cell[1]);
        }

        private List<int[]> classicEmpty()
        {
            List<int[]> list = new ArrayList<int[]>();
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    if (board.getCell(r, c) == 'E') list.add(new int[]{r, c});
            return list;
        }

        // ── C3 Vanish ─────────────────────────────────────────────
        private void scheduleVanish()
        {
            if (vanishScheduler != null) vanishScheduler.stop();
            int delay = 4000 + new Random().nextInt(5000);
            vanishScheduler = new Timer(delay, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    vanishScheduler.stop();
                    vanishScheduler = null;
                    doVanish();
                }
            });
            vanishScheduler.setRepeats(false);
            vanishScheduler.start();
        }

        private void doVanish()
        {
            if (isGameOver() || dynBoard == null) return;
            // vanish() does NOT change turnCount, so whose turn it is stays correct
            int removed = dynBoard.vanish(cs.winLength);
            if (removed > 0)
            {
                syncButtonsFromDynamic();
                MusicPlayer.playErrorSound();
                final String savedStatus = statusLabel.getText();
                statusLabel.setForeground(WARN_COLOR);
                statusLabel.setText(removed + " piece(s) vanished!");
                Timer restoreTimer = new Timer(1400, new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        ((Timer) ev.getSource()).stop();
                        if (!isGameOver())
                        {
                            statusLabel.setForeground(new Color(150, 110, 0));
                            statusLabel.setText(savedStatus);
                        }
                    }
                });
                restoreTimer.setRepeats(false);
                restoreTimer.start();
            }
        }

        private void syncButtonsFromDynamic()
        {
            if (dynBoard == null) return;
            for (int r = 0; r < gridSize; r++)
                for (int c = 0; c < gridSize; c++)
                {
                    char v = dynBoard.getCell(r, c);
                    if (v == 'E') buttons[r][c].reset();
                    else          buttons[r][c].setSymbol(v);
                }
        }

        /**
         * Find the currently blocked cell (if any) and fully restore it to
         * normal empty state so it can be clicked on the next turn.
         */
        private void clearBlockedButton()
        {
            if (dynBoard == null) return;
            for (int r = 0; r < gridSize; r++)
                for (int c = 0; c < gridSize; c++)
                    if (dynBoard.isBlocked(r, c))
                        buttons[r][c].reset();   // clears blocked flag + restores normal look
        }

        // ── Cell click ────────────────────────────────────────────
        private void handleCellClick(int row, int col)
        {
            if (isGameOver()) return;

            char current = currentPlayer();

            // Ignore clicks during computer's turn
            if (vsComputer && current == 'O') return;

            // ── C2 Steal mode ─────────────────────────────────────
            if (challenge && cs.pieceStealer && stealModeActive)
            {
                if (dynBoard == null) return;
                char opponent = (current == 'X') ? 'O' : 'X';
                if (dynBoard.getCell(row, col) == opponent)
                {
                    // Valid steal
                    stopCountdown();
                    MusicPlayer.playClickSound();
                    dynBoard.stealCell(row, col);    // blank the cell + marks blocked
                    dynBoard.recordSteal(current);   // count this steal
                    dynBoard.advanceTurn();           // move to next player
                    buttons[row][col].reset();        // make cell look completely empty
                    buttons[row][col].setBlocked(true); // block clicks for one turn (no visual change)
                    stealModeActive = false;
                    updateStealButton();
                    checkChallengeOver();
                }
                else
                {
                    // Wrong cell clicked — play error, keep steal mode on
                    MusicPlayer.playErrorSound();
                    statusLabel.setText("Click one of " + ((current=='X') ? oName : xName) + "'s pieces!");
                }
                return;
            }

            // ── Normal placement ──────────────────────────────────
            if (challenge)
            {
                if (dynBoard == null || dynBoard.getCell(row, col) != 'E'
                        || dynBoard.isBlocked(row, col))
                { MusicPlayer.playErrorSound(); return; }
                stopCountdown();
                MusicPlayer.playClickSound();
                doPlace(row, col);
            }
            else
            {
                if (board.getCell(row, col) != 'E')
                { MusicPlayer.playErrorSound(); return; }
                MusicPlayer.playClickSound();
                logic.makeMove(board, row, col);
                buttons[row][col].animateIn(current);
                checkClassicOver();
            }
        }

        /**
         * Places the current player's piece and advances the turn.
         * This is the ONLY place we call advanceTurn() for a placement.
         */
        private void doPlace(int row, int col)
        {
            char current = currentPlayer();
            if (challenge && dynBoard != null)
            {
                clearBlockedButton();                 // un-grey stolen cell from last turn
                dynBoard.clearBlock();                // remove the one-turn block
                dynBoard.setCell(row, col, current);  // place piece
                dynBoard.advanceTurn();               // next player's turn
                buttons[row][col].animateIn(current);
                checkChallengeOver();
            }
            else
            {
                logic.makeMove(board, row, col);
                buttons[row][col].animateIn(current);
                checkClassicOver();
            }
        }

        // ── Computer move ─────────────────────────────────────────
        private void maybeComputerMove()
        {
            if (!vsComputer || isGameOver()) return;
            char current = currentPlayer();

            if (current != 'O')
            {
                // Human's turn — start countdown if C1 active
                if (challenge && cs.timePressure) startCountdown();
                return;
            }

            // Computer's turn — short delay so the UI finishes rendering
            int delay = challenge ? 600 : 520;
            Timer t = new Timer(delay, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ((Timer) e.getSource()).stop();
                    if (isGameOver()) return;

                    if (challenge && dynBoard != null)
                    {
                        // Check if computer can/should steal
                        boolean computerCanSteal = cs.pieceStealer && dynBoard.canSteal('O');
                        int[] move = computer.chooseChallengeMove(
                            dynBoard, cs.winLength, computerCanSteal);
                        MusicPlayer.playClickSound();

                        if (move[2] == 1)   // steal chosen
                        {
                            clearBlockedButton();                       // clear old block first
                            dynBoard.clearBlock();
                            dynBoard.stealCell(move[0], move[1]);      // blank + marks blocked
                            dynBoard.recordSteal('O');
                            dynBoard.advanceTurn();
                            buttons[move[0]][move[1]].reset();          // looks fully empty
                            buttons[move[0]][move[1]].setBlocked(true); // click-blocked one turn
                        }
                        else                // normal placement
                        {
                            clearBlockedButton();                    // un-grey stolen cell
                            dynBoard.clearBlock();
                            dynBoard.setCell(move[0], move[1], 'O');
                            dynBoard.advanceTurn();
                            buttons[move[0]][move[1]].animateIn('O');
                        }
                        checkChallengeOver();
                    }
                    else
                    {
                        int[] move = computer.chooseMove(board);
                        MusicPlayer.playClickSound();
                        logic.makeMove(board, move[0], move[1]);
                        buttons[move[0]][move[1]].animateIn('O');
                        checkClassicOver();
                    }
                }
            });
            t.setRepeats(false);
            t.start();
        }

        // ── Game-over checks ──────────────────────────────────────
        private void checkClassicOver()
        {
            if (logic.checkWin(board, 'X'))
            { xWins++; refreshScore(); celebrateWin(xName); }
            else if (logic.checkWin(board, 'O'))
            { oWins++; refreshScore(); celebrateWin(oName); }
            else if (logic.isDraw(board))
            { draws++; refreshScore(); celebrateDraw(); }
            else
            { refreshStatus(); maybeComputerMove(); }
        }

        private void checkChallengeOver()
        {
            if (dynBoard == null) return;
            int wl = cs.winLength;
            if (dynBoard.checkWin('X', wl))
            { xWins++; refreshScore(); celebrateWin(xName); }
            else if (dynBoard.checkWin('O', wl))
            { oWins++; refreshScore(); celebrateWin(oName); }
            else if (dynBoard.isDraw(wl))
            { draws++; refreshScore(); celebrateDraw(); }
            else
            { refreshStatus(); updateStealButton(); maybeComputerMove(); }
        }

        private boolean isGameOver()
        {
            if (challenge && dynBoard != null)
                return dynBoard.isGameOver(cs.winLength);
            return logic.isGameOver(board);
        }

        private char currentPlayer()
        {
            if (challenge && dynBoard != null) return dynBoard.currentPlayer();
            return logic.getCurrentPlayer(board);
        }

        // ── Celebration ───────────────────────────────────────────
        private void celebrateWin(String name)
        {
            stopAllTimers();
            disableAll();
            statusLabel.setForeground(WIN_COLOR);
            statusLabel.setText(name + " WINS!  Congratulations!");
            cel.startWin(layered.getWidth(), layered.getHeight());
            MusicPlayer.playWinMusic();
            nextRoundBtn.setVisible(continuous || challenge);
        }

        private void celebrateDraw()
        {
            stopAllTimers();
            statusLabel.setForeground(DRAW_COLOR);
            statusLabel.setText("It's a Draw,  " + xName + "  &  " + oName + "!");
            cel.startDraw(layered.getWidth(), layered.getHeight());
            MusicPlayer.playDrawMusic();
            nextRoundBtn.setVisible(continuous || challenge);
        }

        private void disableAll()
        {
            for (int r = 0; r < gridSize; r++)
                for (int c = 0; c < gridSize; c++)
                    buttons[r][c].setEnabled(false);
        }

        private void refreshStatus()
        {
            char next = currentPlayer();
            String name = (next == 'X') ? xName : oName;
            statusLabel.setForeground(next == 'X' ? new Color(150, 110, 0) : O_BLUE);
            statusLabel.setText(name + ", it's your turn!");
            // C1: start countdown only on the human player's turn
            if (challenge && cs.timePressure)
            {
                boolean humanTurn = !vsComputer || next != 'O';
                if (humanTurn) startCountdown();
            }
        }

        private void refreshScore()
        {
            if (continuous || challenge)
                scoreLabel.setText(
                    xName + ": " + xWins + "   |   Draws: " + draws + "   |   " + oName + ": " + oWins);
            else
                scoreLabel.setText(xName + " (X)   vs   " + oName + " (O)");
        }
    }


    // ════════════════════════════════════════════════════════════
    //  ANIMATED CELL BUTTON  (static — no outer-instance reference)
    // ════════════════════════════════════════════════════════════
    static class AnimatedButton extends JButton
    {
        private char    symbol  = 'E';
        private float   scale   = 1f;
        private float   alpha   = 1f;
        private float   pulse   = 0f;
        private boolean blocked = false;   // true = just stolen, can't place here yet

        // Stored as field so we can stop it cleanly
        private Timer pulseTimer = null;

        AnimatedButton()
        {
            setBackground(BTN_DEFAULT);
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    if (symbol == 'E' && !blocked)
                    {
                        setBackground(BTN_HOVER);
                        startPulse();
                        MusicPlayer.playHoverSound();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e)
                {
                    if (symbol == 'E' && !blocked) { setBackground(BTN_DEFAULT); stopPulse(); }
                }
            });
        }

        private void startPulse()
        {
            pulse = 0f;
            pulseTimer = new Timer(28, new ActionListener()
            {
                public void actionPerformed(ActionEvent e) { pulse += 0.14f; repaint(); }
            });
            pulseTimer.start();
        }

        private void stopPulse()
        {
            if (pulseTimer != null) { pulseTimer.stop(); pulseTimer = null; }
            pulse = 0f;
            repaint();
        }

        /**
         * Mark this cell as blocked (just stolen).
         * The cell looks like a normal empty cell — no colour change, no X, no O.
         * The flag only gates click logic so neither player can place here this turn.
         */
        public void setBlocked(boolean b)
        {
            blocked = b;
            // Always show normal empty background — the cell is visually empty
            setBackground(BTN_DEFAULT);
            repaint();
        }

        // Called by syncButtonsFromDynamic to restore a symbol without animation
        public void setSymbol(char s)
        {
            symbol = s; scale = 1f; alpha = 1f; repaint();
        }

        public void animateIn(final char player)
        {
            symbol = player;
            scale  = 0f;
            alpha  = 0f;
            final float[] t = {0f};
            Timer anim = new Timer(13, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    t[0] += 0.075f;
                    scale = (float)(1.0 - Math.exp(-6.0 * t[0]) * Math.cos(12.0 * t[0]));
                    alpha = Math.min(1f, t[0] * 2.8f);
                    repaint();
                    if (t[0] >= 1.3f)
                    {
                        scale = 1f; alpha = 1f; repaint();
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            anim.start();
        }

        public void reset()
        {
            symbol = 'E'; scale = 1f; alpha = 1f; blocked = false;
            stopPulse();
            setEnabled(true);
            setBackground(BTN_DEFAULT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Hover glow
            if (symbol == 'E' && pulse > 0)
            {
                float glow = (float)(Math.sin(pulse) * 0.5 + 0.5);
                g2.setColor(new Color(90, 180, 255, (int)(glow * 55)));
                g2.fillRoundRect(-5, -5, getWidth() + 10, getHeight() + 10, 28, 28);
            }

            // Background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            // Symbol
            if (symbol != 'E')
            {
                float safeAlpha = Math.max(0f, Math.min(1f, alpha));
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, safeAlpha));

                int cx = getWidth()  / 2;
                int cy = getHeight() / 2;
                AffineTransform orig = g2.getTransform();
                g2.translate(cx, cy);
                g2.scale(scale, scale);
                g2.translate(-cx, -cy);

                String text = String.valueOf(symbol);
                Font   f    = new Font("Georgia", Font.BOLD, (int)(getHeight() * 0.54));
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(text)) / 2;
                int ty = (getHeight() - fm.getHeight())       / 2 + fm.getAscent();

                if (symbol == 'X')
                {
                    g2.setColor(X_SHADOW); g2.drawString(text, tx + 3, ty + 3);
                    g2.setColor(X_YELLOW); g2.drawString(text, tx, ty);
                }
                else
                {
                    g2.setColor(O_SHADOW); g2.drawString(text, tx + 3, ty + 3);
                    g2.setColor(O_BLUE);   g2.drawString(text, tx, ty);
                }
                g2.setTransform(orig);
            }
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ROUNDED BUTTON (static)
    // ════════════════════════════════════════════════════════════
    static class RoundedButton extends JButton
    {
        private Color normalBg;

        RoundedButton(String text, Color bg, int w, int h)
        {
            super(text);
            this.normalBg = bg;
            setFont(new Font("Georgia", Font.BOLD, 13));
            setForeground(TEXT_DARK);
            setBackground(bg);
            setBorderPainted(false); setFocusPainted(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(w, h));
            final Color hover = bg.darker();
            addMouseListener(new MouseAdapter()
            {
                @Override public void mouseEntered(MouseEvent e) { setBackground(hover); repaint(); }
                @Override public void mouseExited(MouseEvent e)  { setBackground(normalBg); repaint(); }
            });
        }

        void setLabelColor(Color c) { setForeground(c); }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  BIG PLAY BUTTON (static)
    // ════════════════════════════════════════════════════════════
    static class BigButton extends JButton
    {
        BigButton(String text)
        {
            super(text);
            setFont(new Font("Georgia", Font.BOLD, 18));
            setForeground(Color.WHITE);
            setBackground(new Color(50, 120, 190));
            setBorderPainted(false); setFocusPainted(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(260, 56));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addMouseListener(new MouseAdapter()
            {
                @Override public void mouseEntered(MouseEvent e)
                { setBackground(new Color(30, 95, 165)); repaint(); }
                @Override public void mouseExited(MouseEvent e)
                { setBackground(new Color(50, 120, 190)); repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 38));
            g2.fillRoundRect(3, 5, getWidth() - 3, getHeight() - 3, 28, 28);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 28, 28);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CELEBRATION PANEL (static)
    // ════════════════════════════════════════════════════════════
    static class CelebrationPanel extends JPanel
    {
        private final ArrayList<Particle> particles = new ArrayList<Particle>();
        private final Random rng = new Random();
        private Timer animTimer = null;

        static final Color[] PAL = {
            new Color(255, 244, 110), new Color( 16,  44, 100),
            new Color(173, 216, 230), new Color(255, 180,  60),
            new Color(160, 255, 200), new Color(255, 120, 160),
            new Color(120, 220, 255), new Color(255, 210,  80)
        };

        CelebrationPanel() { setOpaque(false); }

        void startWin(int w, int h)
        {
            particles.clear();
            for (int i = 0; i < 180; i++) particles.add(new Confetti(rng, w));
            for (int i = 0; i < 22;  i++) particles.add(new Balloon(rng, w, h));
            run();
        }

        void startDraw(int w, int h)
        {
            particles.clear();
            for (int i = 0; i < 75; i++) particles.add(new Star(rng, w / 2, h / 2));
            for (int i = 0; i < 90; i++) particles.add(new Confetti(rng, w));
            run();
        }

        void stop()
        {
            if (animTimer != null) { animTimer.stop(); animTimer = null; }
            particles.clear();
            repaint();
        }

        private void run()
        {
            if (animTimer != null) animTimer.stop();
            animTimer = new Timer(16, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    particles.removeIf(Particle::isDead);
                    for (Particle p : particles) p.update();
                    repaint();
                    if (particles.isEmpty()) { ((Timer) e.getSource()).stop(); }
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            for (Particle p : particles) p.draw(g2);
            g2.dispose();
        }

        // ── Particle base ─────────────────────────────────────────
        abstract static class Particle
        {
            float x, y, alpha = 1f;
            boolean dead = false;
            abstract void update();
            abstract void draw(Graphics2D g2);
            boolean isDead() { return dead || alpha <= 0f; }
            void applyAlpha(Graphics2D g2)
            {
                float a = Math.max(0f, Math.min(1f, alpha));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            }
        }

        // ── Confetti ──────────────────────────────────────────────
        static class Confetti extends Particle
        {
            float vx, vy, rot, rotV, w, h;
            Color col;
            Confetti(Random rng, int sw)
            {
                col  = PAL[rng.nextInt(PAL.length)];
                w    = 7 + rng.nextInt(10);
                h    = w * 0.44f;
                x    = rng.nextInt(Math.max(1, sw));
                y    = -(10 + rng.nextInt(350));
                vx   = (rng.nextFloat() - 0.5f) * 4.5f;
                vy   = 2.2f + rng.nextFloat() * 4.5f;
                rot  = rng.nextFloat() * 360;
                rotV = (rng.nextFloat() - 0.5f) * 13f;
            }
            public void update()
            {
                x += vx; y += vy; rot += rotV;
                vy += 0.11f; vx *= 0.997f;
                if (y > 1050) dead = true;
            }
            public void draw(Graphics2D g2)
            {
                applyAlpha(g2);
                AffineTransform old = g2.getTransform();
                g2.translate(x + w / 2, y + h / 2);
                g2.rotate(Math.toRadians(rot));
                g2.setColor(col);
                g2.fillRoundRect(-(int)(w/2), -(int)(h/2), (int)w, (int)h, 3, 3);
                g2.setTransform(old);
            }
        }

        // ── Balloon ───────────────────────────────────────────────
        static class Balloon extends Particle
        {
            float vx, vy, size, wobble;
            Color col;
            Balloon(Random rng, int sw, int sh)
            {
                col  = PAL[rng.nextInt(PAL.length)];
                size = 32 + rng.nextInt(30);
                x    = 20 + rng.nextInt(Math.max(1, sw - 40));
                y    = sh + 20 + rng.nextInt(220);
                vx   = (rng.nextFloat() - 0.5f) * 1.6f;
                vy   = -(2.6f + rng.nextFloat() * 2.8f);
            }
            public void update()
            {
                wobble += 0.07f;
                x += vx + (float) Math.sin(wobble) * 0.6f;
                y += vy;
                vy   *= 0.999f;
                alpha -= 0.0022f;
                if (y < -120) dead = true;
            }
            public void draw(Graphics2D g2)
            {
                applyAlpha(g2);
                int bw = (int) size, bh = (int)(size * 1.3f);
                g2.setColor(col);
                g2.fillOval((int)x, (int)y, bw, bh);
                g2.setColor(new Color(255, 255, 255, 88));
                g2.fillOval((int)x + bw/5, (int)y + bh/8, bw/4, bh/5);
                g2.setColor(col.darker());
                g2.fillOval((int)(x + bw/2 - 3), (int)(y + bh - 2), 6, 6);
                g2.setColor(new Color(70, 70, 70, 130));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine((int)(x+bw/2), (int)(y+bh+4),
                    (int)(x+bw/2 + (int)(Math.sin(wobble*2)*7)), (int)(y+bh+28));
            }
        }

        // ── Star ──────────────────────────────────────────────────
        static class Star extends Particle
        {
            float vx, vy, rot, rotV, size;
            Color col;
            Star(Random rng, int cx, int cy)
            {
                col  = PAL[rng.nextInt(PAL.length)];
                size = 9 + rng.nextInt(16);
                double ang = rng.nextDouble() * Math.PI * 2;
                float  sp  = 2.5f + rng.nextFloat() * 7.5f;
                vx   = (float)(Math.cos(ang) * sp);
                vy   = (float)(Math.sin(ang) * sp);
                x    = cx; y = cy;
                rot  = rng.nextFloat() * 360;
                rotV = (rng.nextFloat() - 0.5f) * 20f;
            }
            public void update()
            {
                x += vx; y += vy; vy += 0.20f; vx *= 0.97f;
                rot += rotV; alpha -= 0.010f;
                if (alpha <= 0) dead = true;
            }
            public void draw(Graphics2D g2)
            {
                applyAlpha(g2);
                AffineTransform old = g2.getTransform();
                g2.translate(x, y);
                g2.rotate(Math.toRadians(rot));
                g2.setColor(col);
                int r = (int) size;
                int[] xp = new int[10], yp = new int[10];
                for (int i = 0; i < 10; i++)
                {
                    double a   = Math.PI / 5 * i - Math.PI / 2;
                    int    rad = (i % 2 == 0) ? r : r / 2;
                    xp[i] = (int)(rad * Math.cos(a));
                    yp[i] = (int)(rad * Math.sin(a));
                }
                g2.fillPolygon(xp, yp, 10);
                g2.setTransform(old);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() { new SwingUI(); }
        });
    }
}