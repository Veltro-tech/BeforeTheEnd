package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

public class MainMenuScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    SpriteBatch batch;

    // --- Variabel Animasi Full Background ---
    Animation<TextureRegion> bgAnimation;
    Texture bgSheet;
    float stateTime;

    // --- Variabel Kamera & UI ---
    OrthographicCamera camera;
    Viewport viewport;
    Stage stage;

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;



    public MainMenuScreen(final BeforeTheEndGame game) {
        this.game = game;
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupBackgroundAnimation();
        setupButtons();

        stateTime = 0f;
    }

    private void setupBackgroundAnimation() {
        // 1. Panggil sprite sheet full background kamu yang sudah BERSIH
        bgSheet = new Texture("ui/bg_anim_sheet.png");

        // 2. Tentukan jumlah kolom dan baris pada sprite sheet kamu
        // (Misalnya gambar di atas disusun 3 ke samping, 3 ke bawah)
        int cols = 3;
        int rows = 3;

        // 3. Hitung ukuran 1 frame layar penuh
        int frameWidth = bgSheet.getWidth() / cols;
        int frameHeight = bgSheet.getHeight() / rows;

        // 4. Potong gambar
        TextureRegion[][] tmp = TextureRegion.split(bgSheet, frameWidth, frameHeight);

        // 5. Masukkan ke dalam array 1 Dimensi secara berurutan
        // Kalikan cols * rows untuk mendapatkan total frame (contoh: 3x3 = 9 frame)
        int totalFrames = cols * rows;
        TextureRegion[] bgFrames = new TextureRegion[totalFrames];

        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (index < totalFrames) {
                    bgFrames[index++] = tmp[i][j];
                }
            }
        }

        // 6. Buat animasinya! (0.1f detik per frame biar gerakan anginnya smooth)
        bgAnimation = new Animation<>(0.1f, bgFrames);
        bgAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void setupButtons() {
        // Tombol New Game
        Image btnNewGame = new Image();
        btnNewGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Tombol New Game Diklik!");

                // <-- TAMBAHKAN DUA BARIS INI -->
                game.setScreen(new ChapterOneScreen(game)); // Pindah ke Chapter 1
                dispose(); // Bersihkan memori Main Menu
            }
        });
        stage.addActor(btnNewGame);

        // Tombol Continue
        Image btnContinue = new Image();
        btnContinue.setBounds(980, 190, 220, 45);
        btnContinue.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Tombol Continue Diklik!");
            }
        });
        stage.addActor(btnContinue);

        // Tombol Options
        Image btnOptions = new Image();
        btnOptions.setBounds(980, 130, 220, 45);
        btnOptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Tombol Options Diklik!");
            }
        });
        stage.addActor(btnOptions);

        // Tombol Quit
        Image btnQuit = new Image();
        btnQuit.setBounds(980, 70, 220, 45);
        btnQuit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        stage.addActor(btnQuit);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Ambil frame animasi saat ini
        TextureRegion currentBgFrame = bgAnimation.getKeyFrame(stateTime, true);

        // Gambar animasi tersebut langsung memenuhi layar (0, 0 sampai WORLD_WIDTH, WORLD_HEIGHT)
        batch.draw(currentBgFrame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        if (bgSheet != null) bgSheet.dispose();
    }
}
