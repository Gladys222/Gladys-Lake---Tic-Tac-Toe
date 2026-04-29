package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

public class SwingUI extends JFrame
{
    // ── Theme colors ────────────────────────────────────────────
    private static final Color BG_BABY_BLUE  = new Color(173, 216, 230);
    private static final Color PANEL_BLUE    = new Color(140, 190, 215);
    private static final Color GRID_LINE     = new Color(100, 160, 200);
    private static final Color X_YELLOW      = new Color(255, 245, 157);
    private static final Color X_YELLOW_DARK = new Color(200, 170, 30);
    private static final Color O_DARK_BLUE   = new Color(25,  55,  109);
    private static final Color O_DARK_BORDER = new Color(10,  30,   70);
    private static final Color BTN_DEFAULT   = new Color(155, 205, 230);
    private static final Color BTN_HOVER     = new Color(190, 225, 245);
    private static final Color WIN_GREEN     = new Color(50,  160,  80);
    private static final Color DRAW_ORANGE   = new Color(220, 140,  20);
    private static final Color TEXT_DARK     = new Color(20,   50, 100);

    private Board board;
    private GameLogic logic;
    private AnimatedButton[][] buttons;
    private JLabel statusLabel;
    private ConfettiPanel confettiPanel;

    public SwingUI(String filename)
    {
        board   = new Board(filename);
        logic   = new GameLogic();
        buttons = new AnimatedButton[3][3];

        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 800);
        setMinimumSize(new Dimension(600, 680));
        setLocationRelativeTo(null);
        setResizable(true);

        // Layered pane: game below, confetti on top
        JLayeredPane layered = new JLayeredPane();
        setContentPane(layered);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG_BABY_BLUE);
        layered.add(main, JLayeredPane.DEFAULT_LAYER);

        confettiPanel = new ConfettiPanel();
        confettiPanel.setOpaque(false);
        layered.add(confettiPanel, JLayeredPane.POPUP_LAYER);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension d = getContentPane().getSize();
                main.setBounds(0, 0, d.width, d.height);
                confettiPanel.setBounds(0, 0, d.width, d.height);
            }
        });

        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(BG_BABY_BLUE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(32, 20, 10, 20));

        JLabel titleLabel = new JLabel("TIC  TAC  TOE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 40));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Player X's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        statusLabel.setForeground(new Color(160, 120, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        header.add(titleLabel);
        header.add(statusLabel);
        main.add(header, BorderLayout.NORTH);

        // ── Grid ─────────────────────────────────────────────────
        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setBackground(BG_BABY_BLUE);

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        gridPanel.setBackground(GRID_LINE);
        gridPanel.setBorder(BorderFactory.createLineBorder(GRID_LINE, 8, true));
        gridPanel.setPreferredSize(new Dimension(500, 500));

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 3; col++)
            {
                AnimatedButton btn = new AnimatedButton();
                final int r = row, c = col;
                btn.addActionListener(e -> handleButtonClick(r, c));
                buttons[row][col] = btn;
                gridPanel.add(btn);
            }
        }

        gridWrapper.add(gridPanel);
        main.add(gridWrapper, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        footer.setBackground(BG_BABY_BLUE);

        RoundedButton resetBtn = new RoundedButton("New Game");
        resetBtn.addActionListener(e -> resetGame());
        footer.add(resetBtn);
        main.add(footer, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            Dimension d = getContentPane().getSize();
            main.setBounds(0, 0, d.width, d.height);
            confettiPanel.setBounds(0, 0, d.width, d.height);
        });

        setVisible(true);
        board.clearBoard();
        refreshUI();
    }

    private void handleButtonClick(int row, int col)
    {
        if (logic.isGameOver(board) || board.getCell(row, col) != 'E') return;

        char player = logic.getCurrentPlayer(board);
        logic.makeMove(board, row, col);
        buttons[row][col].animateIn(player);
        refreshUI();

        if (logic.checkWin(board, 'X'))
        {
            statusLabel.setForeground(WIN_GREEN);
            statusLabel.setText("Player X Wins!");
            disableAllButtons();
            confettiPanel.startCelebration(getWidth());
            SoundPlayer.playWin();
        }
        else if (logic.checkWin(board, 'O'))
        {
            statusLabel.setForeground(WIN_GREEN);
            statusLabel.setText("Player O Wins!");
            disableAllButtons();
            confettiPanel.startCelebration(getWidth());
            SoundPlayer.playWin();
        }
        else if (logic.isDraw(board))
        {
            statusLabel.setForeground(DRAW_ORANGE);
            statusLabel.setText("It's a Draw!");
            SoundPlayer.playDraw();
        }
        else
        {
            char next = logic.getCurrentPlayer(board);
            statusLabel.setForeground(next == 'X' ? new Color(160, 120, 0) : O_DARK_BLUE);
            statusLabel.setText("Player " + next + "'s Turn");
        }
    }

    private void refreshUI()
    {
        char[][] grid = board.getGrid();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (grid[r][c] != 'E') buttons[r][c].setSymbol(grid[r][c]);
    }

    private void disableAllButtons()
    {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                buttons[r][c].setEnabled(false);
    }

    private void resetGame()
    {
        board.clearBoard();
        confettiPanel.stop();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                buttons[r][c].reset();
        statusLabel.setForeground(new Color(160, 120, 0));
        statusLabel.setText("Player X's Turn");
    }

    // ────────────────────────────────────────────────────────────
    // Animated cell button
    // ────────────────────────────────────────────────────────────
    private static class AnimatedButton extends JButton
    {
        private char  symbol = 'E';
        private float scale  = 1f;
        private float alpha  = 1f;

        public AnimatedButton()
        {
            setBackground(BTN_DEFAULT);
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (symbol == 'E') { setBackground(BTN_HOVER); repaint(); } }
                public void mouseExited (MouseEvent e) { if (symbol == 'E') { setBackground(BTN_DEFAULT); repaint(); } }
            });
        }

        public void setSymbol(char s) { this.symbol = s; repaint(); }

        public void animateIn(char player)
        {
            this.symbol = player;
            this.scale  = 0f;
            this.alpha  = 0f;
            final float[] t = {0f};
            Timer anim = new Timer(14, null);
            anim.addActionListener(e -> {
                t[0] += 0.09f;
                // Elastic overshoot
                scale = (float)(1 + 0.3 * Math.sin(t[0] * Math.PI) * Math.exp(-t[0] * 2.5));
                alpha = Math.min(1f, t[0] * 2f);
                repaint();
                if (t[0] >= 1.2f) { scale = 1f; alpha = 1f; repaint(); ((Timer)e.getSource()).stop(); }
            });
            anim.start();
        }

        public void reset()
        {
            symbol = 'E'; scale = 1f; alpha = 1f;
            setEnabled(true); setBackground(BTN_DEFAULT); repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            if (symbol != 'E')
            {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.translate(cx, cy);
                g2.scale(scale, scale);
                g2.translate(-cx, -cy);

                String text = String.valueOf(symbol);
                Font f = new Font("Georgia", Font.BOLD, (int)(getHeight() * 0.56));
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(text)) / 2;
                int ty = (getHeight() - fm.getHeight())       / 2 + fm.getAscent();

                if (symbol == 'X')
                {
                    g2.setColor(X_YELLOW_DARK);
                    g2.drawString(text, tx + 3, ty + 3);
                    g2.setColor(X_YELLOW);
                    g2.drawString(text, tx, ty);
                }
                else
                {
                    g2.setColor(O_DARK_BORDER);
                    g2.drawString(text, tx + 3, ty + 3);
                    g2.setColor(O_DARK_BLUE);
                    g2.drawString(text, tx, ty);
                }
            }
            g2.dispose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // Rounded footer button
    // ────────────────────────────────────────────────────────────
    private static class RoundedButton extends JButton
    {
        public RoundedButton(String text)
        {
            super(text);
            setFont(new Font("Georgia", Font.BOLD, 16));
            setForeground(TEXT_DARK);
            setBackground(new Color(110, 170, 210));
            setBorderPainted(false); setFocusPainted(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(190, 50));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(new Color(80, 140, 190)); repaint(); }
                public void mouseExited (MouseEvent e) { setBackground(new Color(110, 170, 210)); repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // Confetti + Balloon panel
    // ────────────────────────────────────────────────────────────
    static class ConfettiPanel extends JPanel
    {
        private final ArrayList<Particle> particles = new ArrayList<>();
        private final Random rng = new Random();
        private Timer timer;

        private static final Color[] PALETTE = {
            new Color(255, 245, 157), new Color(25, 55, 109),
            new Color(173, 216, 230), new Color(255, 180, 60),
            new Color(200, 240, 255), new Color(255, 120, 160),
            new Color(120, 220, 180), new Color(255, 200, 100)
        };

        ConfettiPanel() { setOpaque(false); }

        public void startCelebration(int width)
        {
            particles.clear();
            for (int i = 0; i < 140; i++) particles.add(new Particle(rng, width, false));
            for (int i = 0; i < 16;  i++) particles.add(new Particle(rng, width, true));

            if (timer != null) timer.stop();
            timer = new Timer(16, e -> {
                particles.removeIf(Particle::isDead);
                particles.forEach(Particle::update);
                repaint();
                if (particles.isEmpty()) ((Timer)e.getSource()).stop();
            });
            timer.start();
        }

        public void stop()
        {
            if (timer != null) timer.stop();
            particles.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Particle p : particles) p.draw(g2);
            g2.dispose();
        }

        static class Particle
        {
            float x, y, vx, vy, rot, rotV, size, alpha = 1f;
            Color color;
            boolean balloon, dead = false;
            Random rng;

            Particle(Random rng, int width, boolean balloon)
            {
                this.rng = rng; this.balloon = balloon;
                color = PALETTE[rng.nextInt(PALETTE.length)];
                if (balloon)
                {
                    size = 30 + rng.nextInt(26);
                    x    = 30 + rng.nextInt(Math.max(1, width - 60));
                    y    = 900 + rng.nextInt(150);
                    vx   = (rng.nextFloat() - 0.5f) * 1.4f;
                    vy   = -(2.8f + rng.nextFloat() * 2.2f);
                }
                else
                {
                    size = 7 + rng.nextInt(9);
                    x    = rng.nextInt(Math.max(1, width));
                    y    = -(10 + rng.nextInt(200));
                    vx   = (rng.nextFloat() - 0.5f) * 5f;
                    vy   = 2.5f + rng.nextFloat() * 4f;
                    rot  = rng.nextFloat() * 360f;
                    rotV = (rng.nextFloat() - 0.5f) * 10f;
                }
            }

            void update()
            {
                x += vx; y += vy; rot += rotV;
                if (balloon) { vx += (rng.nextFloat()-0.5f)*0.18f; alpha -= 0.003f; }
                else         vy += 0.14f;
                if (y > 950 || alpha <= 0) dead = true;
            }

            boolean isDead() { return dead; }

            void draw(Graphics2D g2)
            {
                float a = Math.max(0f, Math.min(1f, alpha));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                if (balloon)
                {
                    int bw = (int)size, bh = (int)(size * 1.25f);
                    g2.setColor(color);
                    g2.fillOval((int)x, (int)y, bw, bh);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillOval((int)x + 5, (int)y + 5, bw/4, bh/5);
                    g2.setColor(new Color(80, 80, 80, 120));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawLine((int)(x+bw/2), (int)(y+bh), (int)(x+bw/2), (int)(y+bh+22));
                }
                else
                {
                    AffineTransform old = g2.getTransform();
                    g2.translate(x + size/2, y + size/2);
                    g2.rotate(Math.toRadians(rot));
                    g2.setColor(color);
                    g2.fillRoundRect(-(int)(size/2), -(int)(size/4), (int)size, (int)(size/2), 3, 3);
                    g2.setTransform(old);
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    // Sound synthesis (no external files needed)
    // ────────────────────────────────────────────────────────────
    public static class SoundPlayer
    {
        public static void playWin()
        {
            new Thread(() -> {
                try {
                    int[] notes = {523, 659, 784, 1047, 1319};
                    int[] dur   = {120, 120, 120, 180,  280};
                    for (int i = 0; i < notes.length; i++) {
                        tone(notes[i], dur[i], 0.28f);
                        Thread.sleep(dur[i] - 20);
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        public static void playDraw()
        {
            new Thread(() -> {
                try {
                    int[] notes = {440, 392, 349, 330};
                    for (int n : notes) { tone(n, 200, 0.22f); Thread.sleep(170); }
                } catch (Exception ignored) {}
            }).start();
        }

        private static void tone(int hz, int ms, float vol) throws Exception
        {
            AudioFormat fmt = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) return;
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, 4096);
            line.start();
            int n = 44100 * ms / 1000;
            byte[] buf = new byte[n * 2];
            for (int i = 0; i < n; i++) {
                double env = Math.min(1.0, Math.min(i / 600.0, (n - i) / 600.0));
                short v = (short)(Math.sin(2 * Math.PI * i * hz / 44100.0) * 32767 * vol * env);
                buf[i*2] = (byte)(v & 0xFF); buf[i*2+1] = (byte)((v >> 8) & 0xFF);
            }
            line.write(buf, 0, buf.length);
            line.drain(); line.close();
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new SwingUI("board.csv"));
    }
}