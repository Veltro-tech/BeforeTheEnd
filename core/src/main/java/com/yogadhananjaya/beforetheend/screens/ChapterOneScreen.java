package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

import java.util.LinkedList;
import java.util.Queue;

public class ChapterOneScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Texture textbox;
    Animation<TextureRegion> ayuIdleAnim;
    Texture ayuIdleSheet;
    float stateTime;

    // --- IMPLEMENTASI POIN 4: QUEUE UNTUK DIALOG ---
    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;      // Menyimpan dialog yang sedang aktif

    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    boolean isShowingTitle = false;

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public ChapterOneScreen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        textbox = new Texture("ui/textbox.png");
        font = new BitmapFont();
        font.getData().setScale(2f);

        setupAyuAnimation();

        // Inisialisasi Queue (LinkedList mengimplementasikan Queue di Java)
        dialogQueue = new LinkedList<>();
        loadDialogData();
    }

    private void setupAyuAnimation() {
        ayuIdleSheet = new Texture("characters/ayu_idle_sheet.png");
        int frameWidth = ayuIdleSheet.getWidth() / 6;
        int frameHeight = ayuIdleSheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(ayuIdleSheet, frameWidth, frameHeight);
        TextureRegion[] idleFrames = new TextureRegion[6];
        System.arraycopy(tmp[0], 0, idleFrames, 0, 6);

        ayuIdleAnim = new Animation<>(0.2f, idleFrames);
        ayuIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    private void loadDialogData() {
        Json json = new Json();
        // 1. Baca data dari JSON ke dalam Array sementara
        Array<DialogLine> tempArray = json.fromJson(Array.class, DialogLine.class, Gdx.files.internal("data/prolog_dialog.json"));

        // 2. Masukkan semua data ke dalam Antrian (Enqueue)
        for (DialogLine line : tempArray) {
            dialogQueue.add(line);
        }

        // 3. Panggil dialog pertama
        dequeueNextDialog();
    }

    private void dequeueNextDialog() {
        // Cek apakah antrian masih ada isinya
        if (!dialogQueue.isEmpty()) {
            // Ambil dan hapus dialog paling depan dari antrian (Dequeue)
            currentDialog = dialogQueue.poll();

            targetText = currentDialog.text;
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
        } else {
            // Jika antrian habis (meski di Prolog tertahan oleh MUNCUL_JUDUL)
            currentDialog = null;
        }
    }

    private void updateTypewriter(float delta) {
        if (charIndex < targetText.length()) {
            textTimer += delta;
            if (textTimer >= typeSpeed) {
                displayedText += targetText.charAt(charIndex);
                charIndex++;
                textTimer = 0f;
            }
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (isShowingTitle) {
                System.out.println("Pindah ke Chapter 2!");
                return;
            }

            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                // Panggil dialog selanjutnya dari antrian
                dequeueNextDialog();
            }
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        if (!isShowingTitle) updateTypewriter(delta);
        handleInput();

        if (isShowingTitle) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        } else {
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.25f, 1);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (isShowingTitle) {
            font.getData().setScale(4f);
            font.setColor(Color.WHITE);
            font.draw(batch, "BEFORE THE END", 400, 400);
            font.getData().setScale(2f);
            batch.end();
            return;
        }

        TextureRegion currentIdle = ayuIdleAnim.getKeyFrame(stateTime);
        batch.draw(currentIdle, 150, 220);

        // Render teks hanya jika ada dialog aktif dari Queue
        if (currentDialog != null) {
            if (currentDialog.character.equals("Sistem") && targetText.equals("MUNCUL_JUDUL")) {
                isShowingTitle = true;
            } else {
                batch.draw(textbox, 50, 20, 1180, 200);

                if (currentDialog.character.equals("Narator")) {
                    font.setColor(Color.LIGHT_GRAY);
                } else if (currentDialog.character.equals("Sistem")) {
                    font.setColor(Color.CYAN);
                } else {
                    font.setColor(Color.YELLOW);
                }

                if (!currentDialog.character.equals("Narator") && !currentDialog.character.equals("Sistem")) {
                    font.draw(batch, currentDialog.character, 90, 180);
                }

                font.setColor(Color.WHITE);
                font.draw(batch, displayedText, 90, 130);
            }
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        textbox.dispose();
        if (ayuIdleSheet != null) ayuIdleSheet.dispose();
    }
}
