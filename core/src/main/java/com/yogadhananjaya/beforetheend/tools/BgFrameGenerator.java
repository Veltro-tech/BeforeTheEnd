package com.yogadhananjaya.beforetheend.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BgFrameGenerator {
    static final int COLS = 4;
    static final int ROWS = 4;
    static final int FRAMES = COLS * ROWS;

    public static void main(String[] args) {
        try {
            String projectRoot = findProjectRoot();
            File inputFile = new File(projectRoot, "assets/ui/bg_mainmenu.png");
            File outputFile = new File(projectRoot,
                    "assets/ui/main_menu-animasi/bg_mainmenu_sheet.png");

            if (!inputFile.exists()) {
                System.err.println("Input not found: " + inputFile.getAbsolutePath());
                System.exit(1);
            }

            BufferedImage bg = ImageIO.read(inputFile);
            int fw = bg.getWidth();
            int fh = bg.getHeight();

            System.out.println("Generating " + FRAMES + " frames (" + fw + "x" + fh + ")...");

            BufferedImage sheet = new BufferedImage(fw * COLS, fh * ROWS,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D gSheet = sheet.createGraphics();

            Random rng = new Random(0xDEADBEEFL);

            for (int i = 0; i < FRAMES; i++) {
                int col = i % COLS;
                int row = i / COLS;

                BufferedImage frame = generateFrame(bg, i, FRAMES, rng);
                gSheet.drawImage(frame, col * fw, row * fh, null);
                frame.flush();
            }

            gSheet.dispose();

            outputFile.getParentFile().mkdirs();
            ImageIO.write(sheet, "PNG", outputFile);
            System.out.println("Generated: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static BufferedImage generateFrame(BufferedImage bg, int frameIdx, int totalFrames,
                                       Random rng) {
        int w = bg.getWidth();
        int h = bg.getHeight();
        BufferedImage f = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = f.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(bg, 0, 0, null);

        double phase = (double) frameIdx / totalFrames * 2 * Math.PI;
        float brightness = 1.0f + (float) Math.sin(phase) * 0.025f;
        float blueShift = 1.0f + (float) Math.sin(phase + 1.0) * 0.035f;

        Color overlay = new Color(
                clamp(brightness, 0.92f, 1.0f),
                clamp(brightness, 0.92f, 1.0f),
                clamp(blueShift, 0.92f, 1.0f),
                0.012f);
        g.setColor(overlay);
        g.fillRect(0, 0, w, h);

        drawPapers(g, w, h, frameIdx, totalFrames, rng);
        drawParticles(g, w, h, frameIdx, rng);

        g.dispose();
        return f;
    }

    static void drawPapers(Graphics2D g, int w, int h, int frameIdx, int totalFrames,
                           Random rng) {
        double phase = (double) frameIdx / totalFrames * 2 * Math.PI;

        int[][] bases = {
                { w / 2 - 80, h / 3 + 40 },
                { w / 2 + 120, h / 3 - 20 },
                { w / 2 - 200, h / 3 + 60 },
                { w / 2 + 60, h / 3 - 60 },
        };

        float[] sizes = { 14, 10, 12, 9 };
        float[] alphas = { 0.75f, 0.6f, 0.7f, 0.55f };

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < bases.length; i++) {
            double offsetX = Math.sin(phase + i * 1.5) * 25 + rng.nextGaussian() * 3;
            double offsetY = Math.cos(phase + i * 0.8) * 18 + rng.nextGaussian() * 2;
            double rotation = Math.sin(phase + i) * 0.25 + rng.nextGaussian() * 0.05;

            int px = (int) (bases[i][0] + offsetX);
            int py = (int) (bases[i][1] + offsetY);
            int size = (int) sizes[i];

            Composite orig = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphas[i]));

            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(px, py);
            g2.rotate(rotation);
            g2.setColor(Color.WHITE);
            g2.fillRect(-size, -size / 3, size * 2, size * 2 / 3);
            g2.setColor(new Color(200, 200, 210));
            g2.drawRect(-size, -size / 3, size * 2, size * 2 / 3);
            g2.dispose();

            g.setComposite(orig);
        }
    }

    static void drawParticles(Graphics2D g, int w, int h, int frameIdx, Random rng) {
        int count = 6 + rng.nextInt(5);
        for (int i = 0; i < count; i++) {
            int px = rng.nextInt(w);
            int py = rng.nextInt(h * 2 / 3);
            int size = 1 + rng.nextInt(2);
            float alpha = 0.08f + rng.nextFloat() * 0.18f;

            Color c = rng.nextBoolean()
                    ? new Color(1f, 1f, 1f, alpha)
                    : new Color(0.8f, 0.85f, 1f, alpha);
            g.setColor(c);
            g.fillOval(px, py, size, size);
        }
    }

    static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    static String findProjectRoot() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            if (new File(dir, "assets/ui/bg_mainmenu.png").exists()) {
                return dir.getAbsolutePath();
            }
            File parent = dir.getParentFile();
            if (parent == null) break;
            dir = parent;
        }
        return System.getProperty("user.dir");
    }
}
