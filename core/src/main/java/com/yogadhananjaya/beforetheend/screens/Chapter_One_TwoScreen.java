package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import java.util.Stack;

public class Chapter_One_TwoScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Texture textbox;
    Animation<TextureRegion> ayuIdleAnim;
    Texture ayuIdleSheet;
    float stateTime;

    // --- DATA STRUCTURE: QUEUE ---
    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;

    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    // --- DATA STRUCTURE: STACK ---
    Stack<String> gameStateStack;

    boolean isPuzzleActive = false;
    boolean isBossFightActive = false;

    Color bgColor = new Color(0.2f, 0.2f, 0.25f, 1);

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public Chapter_One_TwoScreen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        textbox = new Texture("ui/textbox.png");
        font = new BitmapFont();
        font.getData().setScale(2f);

        gameStateStack = new Stack<>();
        gameStateStack.push("PLAYING");

        setupAyuAnimation();

        dialogQueue = new LinkedList<>();
        loadDialogData();
    }

    private void setupAyuAnimation() {
        if (Gdx.files.internal("characters/ayu_idle_sheet.png").exists()) {
            ayuIdleSheet = new Texture("characters/ayu_idle_sheet.png");
            int frameWidth = ayuIdleSheet.getWidth() / 6;
            int frameHeight = ayuIdleSheet.getHeight();
            TextureRegion[][] tmp = TextureRegion.split(ayuIdleSheet, frameWidth, frameHeight);
            TextureRegion[] idleFrames = new TextureRegion[6];
            System.arraycopy(tmp[0], 0, idleFrames, 0, 6);
            ayuIdleAnim = new Animation<>(0.2f, idleFrames);
        } else {
            if (Gdx.files.internal("character/Ayu/diam-2.png").exists()) {
                ayuIdleSheet = new Texture("character/Ayu/diam-2.png");
            } else {
                Pixmap pm = new Pixmap(64, 128, Pixmap.Format.RGBA8888);
                pm.setColor(Color.PINK);
                pm.fill();
                ayuIdleSheet = new Texture(pm);
                pm.dispose();
            }
            TextureRegion[] idleFrames = new TextureRegion[] { new TextureRegion(ayuIdleSheet) };
            ayuIdleAnim = new Animation<>(0.2f, idleFrames);
        }
        ayuIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    private void loadDialogData() {
        Json json = new Json();
        Array<DialogLine> tempArray = json.fromJson(Array.class, DialogLine.class, Gdx.files.internal("data/chapter2_dialog.json"));

        for (DialogLine line : tempArray) {
            dialogQueue.add(line);
        }

        dequeueNextDialog();
    }

    // --- LOGIKA AUTO-SKIP (Perbaikan Utama) ---
    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            // 1. Jika teks Narator -> Lewati tanpa menampilkan apapun
            if (currentDialog.character.equals("Narator")) {
                dequeueNextDialog(); // Panggil fungsi ini lagi secara berulang
                return; // Hentikan eksekusi yang sekarang
            }

            // 2. Jika teks Sistem -> Eksekusi logikanya secara sembunyi-sembunyi, lalu lewati
            if (currentDialog.character.equals("Sistem")) {
                String sysText = currentDialog.text;

                if (sysText.contains("LOOP")) {
                    bgColor.set(0f, 0f, 0f, 1); // Bikin background gelap
                } else if (sysText.equals("TRIGGER_PUZZLE")) {
                    isPuzzleActive = true;
                } else if (sysText.equals("TRIGGER_BOSS_FIGHT")) {
                    isBossFightActive = true;
                }

                dequeueNextDialog();
                return;
            }

            // 3. Jika karakter manusia biasa yang bicara, jalankan mesin tik-nya
            bgColor.set(0.2f, 0.2f, 0.25f, 1); // Kembalikan warna background normal
            targetText = currentDialog.text;
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;

        } else {
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameStateStack.peek().equals("PLAYING")) {
                gameStateStack.push("PAUSED");
            } else if (gameStateStack.peek().equals("PAUSED")) {
                gameStateStack.pop();
            }
        }

        if (!gameStateStack.peek().equals("PLAYING") || isPuzzleActive || isBossFightActive) return;

        if (Gdx.input.justTouched()) {
            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                dequeueNextDialog();
            }
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        handleInput();

        if (gameStateStack.peek().equals("PLAYING") && !isPuzzleActive && !isBossFightActive) {
            updateTypewriter(delta);
        }

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        TextureRegion currentIdle = ayuIdleAnim.getKeyFrame(stateTime);
        batch.draw(currentIdle, 150, 220, 198f * 0.85f, 422f * 0.85f);

        // --- RENDER TEXTBOX JAUH LEBIH BERSIH ---
        // Kita tidak perlu lagi mengecek if (Narator / Sistem) karena sudah disaring di atas!
        if (currentDialog != null && !isPuzzleActive && !isBossFightActive) {
            batch.draw(textbox, 50, 20, 1180, 200);

            font.setColor(Color.YELLOW);
            font.draw(batch, currentDialog.character, 90, 180);

            font.setColor(Color.WHITE);
            font.draw(batch, displayedText, 90, 130);
        }

        if (gameStateStack.peek().equals("PAUSED")) {
            font.getData().setScale(3f);
            font.setColor(Color.RED);
            font.draw(batch, "PAUSED", WORLD_WIDTH/2 - 100, WORLD_HEIGHT/2);
            font.getData().setScale(2f);
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
