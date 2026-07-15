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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Interpolation;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

public class MainMenuScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;

    BitmapFont font;         // Font kustom untuk menu
    BitmapFont titleFont;    // Font kustom untuk judul utama
    BitmapFont subTitleFont; // Font kustom untuk subtitle

    // Aset Gambar & Audio
    Texture bgImage;
    Texture blackScreen; // Digunakan untuk efek fade to black
    Texture logoTexture;
    Music bgMusic;
    GlyphLayout glyphLayout;

    // State Sistem
    public enum MenuState {
        SPLASH,
        TITLE,
        TRANSITION,
        MENU
    }
    private MenuState currentState = MenuState.SPLASH;
    private float stateTime = 0f;
    private float splashTime = 0f;
    private float transitionTime = 0f;
    
    // Konfigurasi Waktu
    private final float FADE_IN_TIME = 2.0f;
    private final float STAY_TIME = 3.0f;
    private final float FADE_OUT_TIME = 2.0f;
    private final float TRANSITION_DURATION = 1.0f;

    // Variabel Menu
    String[] menuItems = {"New Game", "Continue", "Chapter", "Setting", "Quit"};
    int selectedIndex = 0;

    // Variabel Logika
    boolean hasSaveData = false; // Ubah ke true nanti jika sistem save sudah dibuat
    boolean isFading = false;
    float fadeAlpha = 0f;

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public MainMenuScreen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        // Pastikan kita menggunakan batch dari class utama
        batch = game.batch;

        // Load Background (Pastikan file bg_mainmenu.png ada di folder assets/ui/)
        if (Gdx.files.internal("ui/bg_mainmenu.png").exists()) {
            bgImage = new Texture("ui/bg_mainmenu.png");
        } else if (Gdx.files.internal("libgdx.png").exists()) {
            bgImage = new Texture("libgdx.png");
        } else {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLUE);
            pixmap.fill();
            bgImage = new Texture(pixmap);
            pixmap.dispose();
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
            Pixmap pixmapLogo = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmapLogo.setColor(Color.RED);
            pixmapLogo.fill();
            logoTexture = new Texture(pixmapLogo);
            pixmapLogo.dispose();
        }

        // Initialize GlyphLayout
        glyphLayout = new GlyphLayout();

        // --- SETUP CUSTOM FONT (.TTF) UNTUK MENU ---
        if (Gdx.files.internal("fonts/Merriweather-VariableFont_opsz,wdth,wght.ttf").exists()) {
            FreeTypeFontGenerator menuGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Merriweather-VariableFont_opsz,wdth,wght.ttf"));
            FreeTypeFontParameter menuParameter = new FreeTypeFontParameter();
            menuParameter.size = 28;
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
            parameter.size = 72;
            parameter.color = Color.WHITE;
            titleFont = generator.generateFont(parameter);

            // Atur Parameter untuk Subtitle
            parameter.size = 28;
            parameter.color = Color.LIGHT_GRAY;
            subTitleFont = generator.generateFont(parameter);

            generator.dispose();
        } else {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(3.0f);
            subTitleFont = new BitmapFont();
            subTitleFont.getData().setScale(1.5f);
        }

        // --- SETUP BACKGROUND MUSIC ---
        if (Gdx.files.internal("music/music_mainmenu.mp3").exists()) {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music_mainmenu.mp3"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.2f); // Santai mengayun, tidak kencang
            bgMusic.play();
        }
    }

    private void handleInput() {
        if (currentState == MenuState.SPLASH) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
                currentState = MenuState.TITLE;
            }
            return;
        }

        if (currentState == MenuState.TITLE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                currentState = MenuState.TRANSITION;
                transitionTime = 0f;
            }
            return;
        }

        if (currentState == MenuState.MENU) {
            // Jika sedang transisi redup, abaikan semua input
            if (isFading) return;

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = menuItems.length - 1;
            }
            else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedIndex++;
                if (selectedIndex >= menuItems.length) selectedIndex = 0;
            }
            else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectMenuItem();
            }
        }
    }

    private void selectMenuItem() {
        switch (selectedIndex) {
            case 0: // New Game
                System.out.println("Memulai New Game... Layar Meredup");
                isFading = true; // Trigger animasi fade to black
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

        // Logika Transisi State
        if (currentState == MenuState.SPLASH) {
            splashTime += delta;
            if (splashTime >= FADE_IN_TIME + STAY_TIME + FADE_OUT_TIME) {
                currentState = MenuState.TITLE;
            }
        } else if (currentState == MenuState.TRANSITION) {
            transitionTime += delta;
            if (transitionTime >= TRANSITION_DURATION) {
                currentState = MenuState.MENU;
            }
        }

        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 1. Render Splash Screen
        if (currentState == MenuState.SPLASH) {
            batch.begin();

            float alpha;
            if (splashTime < FADE_IN_TIME) {
                alpha = splashTime / FADE_IN_TIME;
            } else if (splashTime < FADE_IN_TIME + STAY_TIME) {
                alpha = 1f;
            } else if (splashTime < FADE_IN_TIME + STAY_TIME + FADE_OUT_TIME) {
                alpha = 1f - (splashTime - FADE_IN_TIME - STAY_TIME) / FADE_OUT_TIME;
            } else {
                alpha = 0f;
            }

            // Center the Paus.png logo
            float logoWidth = logoTexture.getWidth();
            float logoHeight = logoTexture.getHeight();
            float logoX = (WORLD_WIDTH - logoWidth) / 2;
            float logoY = (WORLD_HEIGHT - logoHeight) / 2;

            batch.setColor(1, 1, 1, alpha);
            batch.draw(logoTexture, logoX, logoY, logoWidth, logoHeight);
            batch.setColor(Color.WHITE);

            batch.end();
            return;
        }

        // 2. Render Main Game Menu Screens (TITLE, TRANSITION, MENU)
        batch.begin();

        // Selalu gambar background menu utama
        batch.draw(bgImage, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Hitung Posisi Title & Subtitle berdasarkan state
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

        if (currentState == MenuState.TITLE) {
            titleX = centerTitleX;
            titleY = centerTitleY;
            subX = centerSubX;
            subY = centerSubY;
        } else if (currentState == MenuState.TRANSITION) {
            float progress = transitionTime / TRANSITION_DURATION;
            titleX = Interpolation.pow3Out.apply(centerTitleX, targetTitleX, progress);
            titleY = Interpolation.pow3Out.apply(centerTitleY, targetTitleY, progress);
            subX = Interpolation.pow3Out.apply(centerSubX, targetSubX, progress);
            subY = Interpolation.pow3Out.apply(centerSubY, targetSubY, progress);
        } else {
            titleX = targetTitleX;
            titleY = targetTitleY;
            subX = targetSubX;
            subY = targetSubY;
        }

        // Gambar Judul dan Subtitle
        titleFont.draw(batch, "BEFORE THE END", titleX, titleY);
        subTitleFont.draw(batch, "A GAME BY GROUP 5", subX, subY);

        // Render Press Enter To Start (Hanya saat TITLE atau TRANSITION)
        if (currentState == MenuState.TITLE || currentState == MenuState.TRANSITION) {
            glyphLayout.setText(font, "Press Enter To Start");
            float pressX = (WORLD_WIDTH - glyphLayout.width) / 2;
            float pressY = 200f;
            
            float pressAlpha = 0f;
            if (currentState == MenuState.TITLE) {
                pressAlpha = (float) Math.abs(Math.sin(stateTime * 3f));
            } else {
                // Fade out Press Enter during transition
                pressAlpha = 1f - (transitionTime / TRANSITION_DURATION);
            }
            
            font.setColor(1, 1, 1, pressAlpha);
            font.draw(batch, "Press Enter To Start", pressX, pressY);
            font.setColor(Color.WHITE);
        }

        // Render Menu Items (Hanya saat TRANSITION atau MENU)
        if (currentState == MenuState.TRANSITION || currentState == MenuState.MENU) {
            float startX = 80f;
            float startY = 320f;
            float spacing = 50f;

            float menuAlpha = 1f;
            if (currentState == MenuState.TRANSITION) {
                menuAlpha = transitionTime / TRANSITION_DURATION; // Fade-in effect on left side
            }

            for (int i = 0; i < menuItems.length; i++) {
                Color itemColor;
                if (i == 1 && !hasSaveData) {
                    itemColor = Color.DARK_GRAY.cpy();
                } else if (i == selectedIndex) {
                    itemColor = Color.YELLOW.cpy();
                } else {
                    itemColor = Color.WHITE.cpy();
                }
                
                itemColor.a = menuAlpha;
                font.setColor(itemColor);

                String text = (i == selectedIndex) ? "> " + menuItems[i] : menuItems[i];
                font.draw(batch, text, startX, startY - (i * spacing));
            }
            font.setColor(Color.WHITE);
        }

        // Logika Efek Fade-to-Black & Audio Fade-out
        if (isFading) {
            fadeAlpha += delta * 1.0f;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                if (bgMusic != null) bgMusic.stop();
                game.setScreen(new ChapterOneScreen(game));
            } else {
                if (bgMusic != null) bgMusic.setVolume(0.2f * (1f - fadeAlpha)); // Fade out volume
            }
            batch.setColor(1, 1, 1, fadeAlpha);
            batch.draw(blackScreen, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE); // Reset warna agar render berikutnya aman
        }

        batch.end();
    }

    @Override
    public void dispose() {
        bgImage.dispose();
        blackScreen.dispose();
        logoTexture.dispose();
        font.dispose();
        titleFont.dispose();
        subTitleFont.dispose();
        if (bgMusic != null) bgMusic.dispose();
    }
}
