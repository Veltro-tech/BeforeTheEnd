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

public class Chapter_One_FourScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Texture textbox;
    Animation<TextureRegion> ayuIdleAnim;
    Texture ayuIdleSheet;
    float stateTime;

    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;
    Stack<String> gameStateStack;

    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    // Warna dasar: Biru gelap (langit retak)
    Color bgColor = new Color(0.15f, 0.15f, 0.2f, 1);

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public Chapter_One_FourScreen(final BeforeTheEndGame game) {
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
            ayuIdleSheet = new Texture("character/Ayu/diam.png");
            TextureRegion[] idleFrames = new TextureRegion[] { new TextureRegion(ayuIdleSheet) };
            ayuIdleAnim = new Animation<>(0.2f, idleFrames);
        }
        ayuIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void loadDialogData() {
        Json json = new Json();
        Array<DialogLine> tempArray = json.fromJson(Array.class, DialogLine.class, Gdx.files.internal("data/chapter4_dialog.json"));
        for (DialogLine line : tempArray) {
            dialogQueue.add(line);
        }
        dequeueNextDialog();
    }

    public void resumeStory() {
        gameStateStack.push("PLAYING");
        dequeueNextDialog();
    }

    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            if (currentDialog.character.equals("Narator")) {
                dequeueNextDialog();
                return;
            }

            if (currentDialog.character.equals("Sistem")) {
                String sysText = currentDialog.text;

                // Transisi Lingkungan
                if (sysText.contains("RUANG HAMPA")) {
                    bgColor.set(0.0f, 0.0f, 0.0f, 1); // Hitam total
                } else if (sysText.contains("LORONG SMA")) {
                    bgColor.set(0.3f, 0.2f, 0.15f, 1); // Sepia/kusam untuk memory
                }

                // Trigger Puzzle
                if (sysText.equals("TRIGGER_PUZZLE_MEMORY")) {
                    gameStateStack.push("IN_PUZZLE");
                    game.setScreen(new PuzzleScreen(game, null, this, "GRAPH_MEMORY"));
                    return;
                }

                // Trigger Boss Terakhir
                if (sysText.equals("TRIGGER_ECHO_APPEAR")) {
                    System.out.println("Transisi ke Layar Final Battle...");
                    // game.setScreen(new BossFightScreen(game));
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

        batch.draw(ayuIdleAnim.getKeyFrame(stateTime), 150, 220, 198f * 0.85f, 422f * 0.85f);

        if (currentDialog != null && gameStateStack.peek().equals("PLAYING")) {
            batch.draw(textbox, 50, 20, 1180, 200);

            font.setColor(Color.YELLOW);
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
