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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

import java.util.ArrayList;
import java.util.List;

public class ChapterOneScreen extends ScreenAdapter {
    private enum State {
        NIGHTMARE,
        WAKING_UP,
        DIALOG,
        PLAY
    }

    private final BeforeTheEndGame game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    // States
    private State currentState = State.NIGHTMARE;
    private float stateTimer = 0f;

    // Textures & Assets
    private Texture roomBg;
    private Texture ayuIdle;
    private Texture textbox;
    private Texture blackTexture;

    // Animations
    private Animation<TextureRegion> walkRightAnim;
    private Animation<TextureRegion> walkLeftAnim;
    private float walkTime = 0f;

    // Nightmare Phase variables
    private static class NightmareText {
        String text;
        float x, y;
        float scale;
        Color color;

        NightmareText(String text, float x, float y, float scale, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.color = color;
        }
    }
    private final List<NightmareText> nightmareTexts = new ArrayList<>();
    private final String[] nightmarePhrases = {"Gagal", "Semua hancur", "Tidak ada waktu", "Kamu terlambat", "Sia-sia", "Sudah berakhir"};
    private float textSpawnTimer = 0f;

    // Waking Up Phase variables
    private float wakeFadeAlpha = 1f;

    // Dialog Phase variables
    private final String[] dialogTexts = {
        "Hah... hah... cuma mimpi?",
        "Tunggu, jam berapa ini?! Astaga, sidang skripsiku!"
    };
    private int currentDialogIndex = 0;
    private String displayedDialogText = "";
    private float typeTimer = 0f;
    private float typeSpeed = 0.05f;
    private int charIndex = 0;

    // Play Phase variables
    private float ayuX = 200f;
    private float ayuY = 100f;
    private final float AYU_SPEED = 250f;
    private boolean isMoving = false;
    private boolean facingRight = true;

    private final float WORLD_WIDTH = 1280f;
    private final float WORLD_HEIGHT = 720f;

    public ChapterOneScreen(final BeforeTheEndGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        this.batch = game.batch; // Re-use batch
        this.font = new BitmapFont();
        this.font.getData().setScale(2f);
        this.glyphLayout = new GlyphLayout();

        loadAssets();
    }

    private void loadAssets() {
        // Load Room Background
        if (Gdx.files.internal("backgrounds/Kamar-ayu.png").exists()) {
            roomBg = new Texture("backgrounds/Kamar-ayu.png");
        } else {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.DARK_GRAY);
            pm.fill();
            roomBg = new Texture(pm);
            pm.dispose();
        }

        // Load Ayu Idle
        if (Gdx.files.internal("character/Ayu/diam.png").exists()) {
            ayuIdle = new Texture("character/Ayu/diam.png");
        } else {
            Pixmap pm = new Pixmap(64, 128, Pixmap.Format.RGBA8888);
            pm.setColor(Color.PINK);
            pm.fill();
            ayuIdle = new Texture(pm);
            pm.dispose();
        }

        // Load Textbox
        if (Gdx.files.internal("ui/textbox.png").exists()) {
            textbox = new Texture("ui/textbox.png");
        } else {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.NAVY);
            pm.fill();
            textbox = new Texture(pm);
            pm.dispose();
        }

        // Create black 1x1 texture for fade effect
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK);
        pm.fill();
        blackTexture = new Texture(pm);
        pm.dispose();

        // Load Walk Animations
        Array<TextureRegion> rightFrames = new Array<>();
        Array<TextureRegion> leftFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            String rightPath = "character/Ayu/jalan_kanan_" + i + ".png";
            String leftPath = "character/Ayu/jalan_kiri_" + i + ".png";
            if (Gdx.files.internal(rightPath).exists()) {
                rightFrames.add(new TextureRegion(new Texture(rightPath)));
            }
            if (Gdx.files.internal(leftPath).exists()) {
                leftFrames.add(new TextureRegion(new Texture(leftPath)));
            }
        }

        if (rightFrames.size > 0) {
            walkRightAnim = new Animation<>(0.1f, rightFrames, Animation.PlayMode.LOOP);
        }
        if (leftFrames.size > 0) {
            walkLeftAnim = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);
        }
    }

    @Override
    public void render(float delta) {
        stateTimer += delta;

        // Clear Screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update Camera & Shake Logic
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        if (currentState == State.NIGHTMARE) {
            // Screen Shake
            float shakeIntensity = 5f;
            camera.position.add(
                MathUtils.random(-shakeIntensity, shakeIntensity),
                MathUtils.random(-shakeIntensity, shakeIntensity),
                0
            );
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Handle State Logic
        updateState(delta);

        // Render
        batch.begin();

        // 1. Draw Background & Character (Only if not in NIGHTMARE)
        if (currentState != State.NIGHTMARE) {
            batch.draw(roomBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            drawCharacter(delta);
        }

        // 2. State-Specific Rendering
        if (currentState == State.NIGHTMARE) {
            renderNightmare();
        } else if (currentState == State.WAKING_UP) {
            // Draw Waking Up Overlay
            batch.setColor(1, 1, 1, wakeFadeAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        } else if (currentState == State.DIALOG) {
            renderDialog(delta);
        }

        batch.end();
    }

    private void updateState(float delta) {
        switch (currentState) {
            case NIGHTMARE:
                // Nightmare State runs for 4 seconds
                textSpawnTimer += delta;
                if (textSpawnTimer >= 0.3f) {
                    textSpawnTimer = 0f;
                    String phrase = nightmarePhrases[MathUtils.random(nightmarePhrases.length - 1)];
                    float x = MathUtils.random(100f, WORLD_WIDTH - 300f);
                    float y = MathUtils.random(100f, WORLD_HEIGHT - 100f);
                    float scale = MathUtils.random(1.5f, 4f);
                    Color color = new Color(MathUtils.random(0.5f, 1f), 0f, 0f, MathUtils.random(0.5f, 1f));
                    nightmareTexts.add(new NightmareText(phrase, x, y, scale, color));
                }

                if (stateTimer >= 4.0f) {
                    currentState = State.WAKING_UP;
                    stateTimer = 0f;
                    nightmareTexts.clear();
                }
                break;

            case WAKING_UP:
                // Fade from black to screen
                wakeFadeAlpha = 1f - (stateTimer / 2.0f); // 2 seconds fade
                if (wakeFadeAlpha <= 0f) {
                    wakeFadeAlpha = 0f;
                    currentState = State.DIALOG;
                    stateTimer = 0f;
                    startDialog();
                }
                break;

            case DIALOG:
                // Handle Dialog input
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
                    if (charIndex < dialogTexts[currentDialogIndex].length()) {
                        // Complete typing immediately
                        displayedDialogText = dialogTexts[currentDialogIndex];
                        charIndex = dialogTexts[currentDialogIndex].length();
                    } else {
                        // Next dialog
                        currentDialogIndex++;
                        if (currentDialogIndex >= dialogTexts.length) {
                            currentState = State.PLAY;
                            stateTimer = 0f;
                        } else {
                            startDialog();
                        }
                    }
                }
                break;

            case PLAY:
                // Player free movement
                isMoving = false;
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                    ayuX -= AYU_SPEED * delta;
                    facingRight = false;
                    isMoving = true;
                } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    ayuX += AYU_SPEED * delta;
                    facingRight = true;
                    isMoving = true;
                }

                // Bound player position within screen boundaries
                if (ayuX < 50f) ayuX = 50f;
                if (ayuX > WORLD_WIDTH - 150f) ayuX = WORLD_WIDTH - 150f;
                break;
        }
    }

    private void drawCharacter(float delta) {
        float drawWidth = 150f;
        float drawHeight = 250f;

        if (currentState == State.PLAY && isMoving) {
            walkTime += delta;
            TextureRegion currentFrame = null;
            if (facingRight && walkRightAnim != null) {
                currentFrame = walkRightAnim.getKeyFrame(walkTime);
            } else if (!facingRight && walkLeftAnim != null) {
                currentFrame = walkLeftAnim.getKeyFrame(walkTime);
            }

            if (currentFrame != null) {
                batch.draw(currentFrame, ayuX, ayuY, drawWidth, drawHeight);
            } else {
                batch.draw(ayuIdle, ayuX, ayuY, drawWidth, drawHeight);
            }
        } else {
            walkTime = 0f;
            batch.draw(ayuIdle, ayuX, ayuY, drawWidth, drawHeight);
        }
    }

    private void renderNightmare() {
        for (NightmareText nt : nightmareTexts) {
            font.getData().setScale(nt.scale);
            font.setColor(nt.color);
            font.draw(batch, nt.text, nt.x, nt.y);
        }
        // Restore defaults
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    private void startDialog() {
        displayedDialogText = "";
        charIndex = 0;
        typeTimer = 0f;
    }

    private void renderDialog(float delta) {
        String currentText = dialogTexts[currentDialogIndex];
        if (charIndex < currentText.length()) {
            typeTimer += delta;
            if (typeTimer >= typeSpeed) {
                typeTimer = 0f;
                displayedDialogText += currentText.charAt(charIndex);
                charIndex++;
            }
        }

        // Draw Dialog Box
        batch.draw(textbox, 50, 20, WORLD_WIDTH - 100, 180);

        // Draw Character Name
        font.setColor(Color.YELLOW);
        font.draw(batch, "Ayu", 90, 160);

        // Draw Dialog Text
        font.setColor(Color.WHITE);
        font.draw(batch, displayedDialogText, 90, 110);
    }

    @Override
    public void dispose() {
        if (roomBg != null) roomBg.dispose();
        if (ayuIdle != null) ayuIdle.dispose();
        if (textbox != null) textbox.dispose();
        if (blackTexture != null) blackTexture.dispose();
        font.dispose();
    }
}
