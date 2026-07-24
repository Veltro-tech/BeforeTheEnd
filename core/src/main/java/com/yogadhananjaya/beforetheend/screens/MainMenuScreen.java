package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

public class MainMenuScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;

    BitmapFont font; // Font kustom untuk menu
    BitmapFont titleFont; // Font kustom untuk judul utama
    BitmapFont subTitleFont; // Font kustom untuk subtitle

    // Aset Gambar & Audio
    Animation<Texture> bgAnim;
    Texture blackScreen; // Digunakan untuk efek fade to black
    Texture logoTexture;
    Music bgMusic;
    GlyphLayout glyphLayout;

    // State Sistem
    public enum MenuState {
        SPLASH,
        TITLE,
        TRANSITION,
        MENU,
        PROLOGUE_TEASER
    }

    private MenuState currentState = MenuState.TITLE;
    private float stateTime = 0f;
    private float splashTime = 0f;
    private float transitionTime = 0f;

    // Teaser Text Typewriter
    private String teaserText = "Beberapa jam sebelum segalanya terjadi...";
    private float teaserTimer = 0f;
    private float typewriterTimer = 0f;
    private int teaserCharIndex = 0;

    // Konfigurasi Waktu
    private final float FADE_IN_TIME = 2.0f;
    private final float STAY_TIME = 3.0f;
    private final float FADE_OUT_TIME = 2.0f;
    private final float TRANSITION_DURATION = 1.0f;

    // Variabel Menu
    String[] menuItems = { "New Game", "Continue", "Chapter", "Setting", "Quit" };
    int selectedIndex = 0;

    // Variabel Logika
    boolean hasSaveData = false; // Ubah ke true nanti jika sistem save sudah dibuat
    boolean isFading = false;
    float fadeAlpha = 0f;

    float animTime;

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public MainMenuScreen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        // Pastikan kita menggunakan batch dari class utama
        batch = game.batch;

        // Load Background frames
        Array<Texture> frames = new Array<>();
        for (int i = 1; i <= 80; i++) {
            String path = String.format("ui/main_menu-animasi/frame_%03d.png", i);
            if (Gdx.files.internal(path).exists()) {
                frames.add(new Texture(path));
            }
        }

        if (frames.size > 0) {
            bgAnim = new Animation<>(0.104f, frames, Animation.PlayMode.LOOP);
        } else {
            // Fallback jika tidak ada frame
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLUE);
            pixmap.fill();
            Texture fallbackTex = new Texture(pixmap);
            pixmap.dispose();
            frames.add(fallbackTex);
            bgAnim = new Animation<>(1f, frames, Animation.PlayMode.LOOP);
        }

        // Membuat tekstur hitam polos 1x1 pixel untuk efek redup
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackScreen = new Texture(pixmap);
        pixmap.dispose();

        // Load Logo Texture
        if (Gdx.files.internal("ui/Paus.png").exists()) {
            logoTexture = new Texture("ui/Paus.png");
        } else if (Gdx.files.internal("libgdx.png").exists()) {
            logoTexture = new Texture("libgdx.png");
        } else {
            logoTexture = blackScreen;
        }

        // Initialize GlyphLayout
        glyphLayout = new GlyphLayout();

        // --- SETUP CUSTOM FONT (.TTF) UNTUK MENU ---
        if (Gdx.files.internal("fonts/menu.ttf").exists()) {
            FreeTypeFontGenerator menuGenerator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/menu.ttf"));
            FreeTypeFontParameter menuParameter = new FreeTypeFontParameter();
            menuParameter.size = 32;
            menuParameter.color = Color.WHITE;
            font = menuGenerator.generateFont(menuParameter);
            menuGenerator.dispose();
        } else {
            font = new BitmapFont();
        }

        // --- SETUP CUSTOM FONT (.TTF) UNTUK JUDUL ---
        if (Gdx.files.internal("fonts/judul.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/judul.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();

            // Atur Parameter untuk Judul Utama
            parameter.size = 101;
            parameter.color = Color.WHITE;
            titleFont = generator.generateFont(parameter);

            // Atur Parameter untuk Subtitle
            parameter.size = 39;
            subTitleFont = generator.generateFont(parameter);

            generator.dispose();
        } else {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(4.2f);
            subTitleFont = new BitmapFont();
            subTitleFont.getData().setScale(2.1f);
        }

        // --- SETUP BACKGROUND MUSIC ---
        if (Gdx.files.internal("music/music_mainmenu.mp3").exists()) {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_mainmenu.mp3"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.16f); // Diperkecil 20% (0.2f -> 0.16f)
            bgMusic.play();
        }

        animTime = 0f;
    }

    private void handleInput() {
        if (currentState == MenuState.TITLE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                currentState = MenuState.TRANSITION;
                transitionTime = 0f;
            }
            return;
        }

        if (currentState == MenuState.PROLOGUE_TEASER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || Gdx.input.justTouched()) {
                if (teaserCharIndex < teaserText.length()) {
                    teaserCharIndex = teaserText.length();
                } else {
                    game.setScreen(new Chapter_One_OneScreen(game));
                }
            }
            return;
        }

        if (currentState == MenuState.MENU) {
            // Jika sedang transisi redup, abaikan semua input
            if (isFading)
                return;

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedIndex--;
                if (selectedIndex < 0)
                    selectedIndex = menuItems.length - 1;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedIndex++;
                if (selectedIndex >= menuItems.length)
                    selectedIndex = 0;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectMenuItem();
            }
        }
    }

    private void selectMenuItem() {
        switch (selectedIndex) {
            case 0: // New Game - Transisi Fade to Black lalu Teks Teaser
                isFading = true;
                fadeAlpha = 0f;
                break;
            case 1: // Continue
                if (hasSaveData) {
                    System.out.println("Melanjutkan Permainan...");
                } else {
                    System.out.println("Tidak ada data save!");
                }
                break;
            case 2: // Chapter
                System.out.println("Membuka Pemilihan Chapter...");
                break;
            case 3: // Setting
                System.out.println("Membuka Setting...");
                break;
            case 4: // Quit
                System.out.println("Keluar dari Game...");
                Gdx.app.exit();
                break;
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        animTime += delta;

        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update Logika Fade
        if (isFading) {
            fadeAlpha += delta * 1.2f;
            if (bgMusic != null) {
                bgMusic.setVolume(Math.max(0f, 0.16f * (1f - fadeAlpha)));
            }
            if (fadeAlpha >= 1.0f) {
                fadeAlpha = 1.0f;
                isFading = false;
                if (bgMusic != null) {
                    bgMusic.stop();
                }
                currentState = MenuState.PROLOGUE_TEASER;
                teaserTimer = 0f;
                typewriterTimer = 0f;
                teaserCharIndex = 0;
            }
        }

        batch.begin();

        if (currentState == MenuState.PROLOGUE_TEASER) {
            teaserTimer += delta;
            typewriterTimer += delta;

            if (typewriterTimer >= 0.06f) {
                typewriterTimer = 0f;
                if (teaserCharIndex < teaserText.length()) {
                    teaserCharIndex++;
                }
            }

            if (teaserTimer >= 4.5f) {
                game.setScreen(new Chapter_One_OneScreen(game));
                batch.end();
                return;
            }

            batch.setColor(1, 1, 1, 1);
            batch.draw(blackScreen, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            String currentTeaser = teaserText.substring(0, teaserCharIndex);
            glyphLayout.setText(subTitleFont, currentTeaser);
            float textX = (WORLD_WIDTH - glyphLayout.width) / 2f;
            float textY = WORLD_HEIGHT / 2f + 20f;
            subTitleFont.setColor(Color.WHITE);
            subTitleFont.draw(batch, currentTeaser, textX, textY);

            if (teaserCharIndex >= teaserText.length()) {
                glyphLayout.setText(font, "[ Tekan Enter ]");
                float skipX = (WORLD_WIDTH - glyphLayout.width) / 2f;
                font.setColor(Color.GRAY);
                font.draw(batch, "[ Tekan Enter ]", skipX, 100f);
                font.setColor(Color.WHITE);
            }

            batch.end();
            return;
        }

        Texture currentFrame = bgAnim.getKeyFrame(animTime);
        float duration = bgAnim.getAnimationDuration();
        float localTime = animTime % duration;

        // Jika mendekati akhir animasi (0.2 detik terakhir sebelum loop ulang ke frame 1), lakukan crossfade manual ke frame 1
        float crossFadeThreshold = 0.2f; 
        if (duration - localTime < crossFadeThreshold) {
            float progressFade = (localTime - (duration - crossFadeThreshold)) / crossFadeThreshold;
            batch.setColor(1, 1, 1, 1 - progressFade);
            batch.draw(currentFrame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            
            batch.setColor(1, 1, 1, progressFade);
            batch.draw(bgAnim.getKeyFrame(0f), 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(1, 1, 1, 1); // Reset color batch ke normal
        } else {
            batch.draw(currentFrame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }

        float titleX, titleY;
        float subX, subY;

        glyphLayout.setText(titleFont, "BEFORE THE END");
        float centerTitleX = (WORLD_WIDTH - glyphLayout.width) / 2;
        float centerTitleY = 450f;

        glyphLayout.setText(subTitleFont, "A GAME BY GROUP 5");
        float centerSubX = (WORLD_WIDTH - glyphLayout.width) / 2;
        float centerSubY = centerTitleY - 80f;

        float targetTitleX = 80f;
        float targetTitleY = WORLD_HEIGHT - 60f;
        float targetSubX = 85f;
        float targetSubY = WORLD_HEIGHT - 140f;

        // Logika Animasi Pergeseran Teks Judul (Smooth Glide)
        if (currentState == MenuState.TRANSITION) {
            transitionTime += delta * 1.5f;
            if (transitionTime >= 1.0f) {
                transitionTime = 1.0f;
                currentState = MenuState.MENU;
            }
        }

        float progress = 0f;
        if (currentState == MenuState.TITLE) {
            progress = 0f;
        } else if (currentState == MenuState.TRANSITION) {
            progress = transitionTime;
        } else {
            progress = 1.0f;
        }

        // Smooth Interpolation (Smoothstep)
        float smoothProgress = progress * progress * (3f - 2f * progress);

        titleX = centerTitleX + (targetTitleX - centerTitleX) * smoothProgress;
        titleY = centerTitleY + (targetTitleY - centerTitleY) * smoothProgress;
        subX = centerSubX + (targetSubX - centerSubX) * smoothProgress;
        subY = centerSubY + (targetSubY - centerSubY) * smoothProgress;

        titleFont.draw(batch, "BEFORE THE END", titleX, titleY);
        subTitleFont.draw(batch, "A GAME BY GROUP 5", subX, subY);

        // Tombol Press Enter To Start Memudar (Fade Out)
        if (progress < 1.0f) {
            float pressAlpha = 1.0f - smoothProgress;
            font.setColor(1, 1, 1, pressAlpha);
            glyphLayout.setText(font, "Press Enter To Start");
            float pressX = (WORLD_WIDTH - glyphLayout.width) / 2;
            float pressY = 300f;
            font.draw(batch, "Press Enter To Start", pressX, pressY);
            font.setColor(Color.WHITE);
        }

        // Menu Meluncur Masuk Dari Kiri (-200f ke 80f)
        if (progress > 0f) {
            float startX = -200f + (80f - (-200f)) * smoothProgress;
            float startY = 320f;
            float spacing = 50f;

            for (int i = 0; i < menuItems.length; i++) {
                float itemY = startY - (i * spacing);

                Color itemColor;
                if (i == 1 && !hasSaveData) {
                    itemColor = new Color(0.3f, 0.3f, 0.3f, smoothProgress);
                } else if (i == selectedIndex) {
                    itemColor = new Color(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, smoothProgress);
                } else {
                    itemColor = new Color(1f, 1f, 1f, smoothProgress);
                }

                font.setColor(itemColor);

                String text = (i == selectedIndex) ? "> " + menuItems[i] : menuItems[i];
                font.draw(batch, text, startX, itemY);
            }
            font.setColor(Color.WHITE);
        }

        // Efek Layar Redup (Fade Out)
        if (isFading || fadeAlpha > 0f) {
            batch.setColor(1, 1, 1, fadeAlpha);
            batch.draw(blackScreen, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(1, 1, 1, 1);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        for (Object frame : bgAnim.getKeyFrames()) {
            ((Texture) frame).dispose();
        }
        blackScreen.dispose();
        logoTexture.dispose();
        font.dispose();
        titleFont.dispose();
        subTitleFont.dispose();
        if (bgMusic != null)
            bgMusic.dispose();
    }
}
