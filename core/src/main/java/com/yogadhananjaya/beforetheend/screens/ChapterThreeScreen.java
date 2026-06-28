package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import java.util.Stack;

public class ChapterThreeScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Texture textbox;
    Animation<TextureRegion> ayuIdleAnim;
    Texture ayuIdleSheet;
    float stateTime;

    // --- DATA STRUCTURE: QUEUE & STACK ---
    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;
    Stack<String> gameStateStack;

    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    // Warna background disesuaikan per area
    Color bgColor = new Color(0.1f, 0.1f, 0.15f, 1);

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public ChapterThreeScreen(final BeforeTheEndGame game) {
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
        ayuIdleSheet = new Texture("characters/ayu_idle_sheet.png");
        int frameWidth = ayuIdleSheet.getWidth() / 6;
        int frameHeight = ayuIdleSheet.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(ayuIdleSheet, frameWidth, frameHeight);
        TextureRegion[] idleFrames = new TextureRegion[6];
        System.arraycopy(tmp[0], 0, idleFrames, 0, 6);
        ayuIdleAnim = new Animation<>(0.2f, idleFrames);
        ayuIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void loadDialogData() {
        Json json = new Json();
        Array<DialogLine> tempArray = json.fromJson(Array.class, DialogLine.class, Gdx.files.internal("data/chapter3_dialog.json"));
        for (DialogLine line : tempArray) {
            dialogQueue.add(line);
        }
        dequeueNextDialog();
    }

    // METHOD PUBLIC: Dipanggil oleh PuzzleScreen saat puzzle selesai
    public void resumeStory() {
        gameStateStack.push("PLAYING");
        dequeueNextDialog(); // Lanjut dialog setelah puzzle
    }

    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            // AUTO-SKIP: Abaikan teks Narator secara visual
            if (currentDialog.character.equals("Narator")) {
                dequeueNextDialog();
                return;
            }

            // SISTEM & TRIGGER
            if (currentDialog.character.equals("Sistem")) {
                String sysText = currentDialog.text;

                // Ubah mood warna background berdasarkan area
                if (sysText.contains("RUANG ARSIP")) bgColor.set(0.2f, 0.1f, 0.1f, 1); // Kemerahan (tegang)
                if (sysText.contains("KORIDOR EKSPEKTASI")) bgColor.set(0.1f, 0.1f, 0.2f, 1); // Biru gelap (sesak)
                if (sysText.contains("TAMAN SENJA")) bgColor.set(0.3f, 0.2f, 0.1f, 1); // Oranye (hangat)

                // Pindah ke Puzzle Screen
                if (sysText.contains("TRIGGER_PUZZLE")) {
                    gameStateStack.push("IN_PUZZLE");
                    String puzzleType = sysText.contains("ARSIP") ? "TREE_ARSIP" : "GRAPH_SUARA";

                    // Lempar referensi game dan screen ini agar bisa kembali
                    game.setScreen(new PuzzleScreen(game, this,null, puzzleType));
                    return;
                }

                dequeueNextDialog();
                return;
            }

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !gameStateStack.peek().equals("IN_PUZZLE")) {
            if (gameStateStack.peek().equals("PLAYING")) gameStateStack.push("PAUSED");
            else if (gameStateStack.peek().equals("PAUSED")) gameStateStack.pop();
        }

        if (!gameStateStack.peek().equals("PLAYING")) return;

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

        if (gameStateStack.peek().equals("PLAYING")) updateTypewriter(delta);

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(ayuIdleAnim.getKeyFrame(stateTime), 150, 220);

        if (currentDialog != null && gameStateStack.peek().equals("PLAYING")) {
            batch.draw(textbox, 50, 20, 1180, 200);

            font.setColor(currentDialog.character.equals("Voice") ? Color.RED : Color.YELLOW);
            font.draw(batch, currentDialog.character, 90, 180);
            font.setColor(Color.WHITE);
            font.draw(batch, displayedText, 90, 130);
        }

        if (gameStateStack.peek().equals("PAUSED")) {
            font.setColor(Color.RED);
            font.draw(batch, "PAUSED", WORLD_WIDTH/2 - 50, WORLD_HEIGHT/2);
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
