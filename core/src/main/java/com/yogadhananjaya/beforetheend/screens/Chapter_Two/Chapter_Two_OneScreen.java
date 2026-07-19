package com.yogadhananjaya.beforetheend.screens.Chapter_Two;

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
import com.badlogic.gdx.utils.Json;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;
import com.yogadhananjaya.beforetheend.screens.Chapter_One_TwoScreen;
import com.yogadhananjaya.beforetheend.screens.DialogLine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Chapter_Two_OneScreen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout glyphLayout = new GlyphLayout();

    Texture textbox;
    Animation<TextureRegion> walkRightAnim;
    Animation<TextureRegion> walkLeftAnim;
    TextureRegion ayuIdleFrame;
    Texture ayuSelesaiPresentasi;
    
    // Backgrounds
    Texture bgKamar;
    Texture bgKamarTidur;
    Texture bgDriving;
    Texture bgLobby;
    Texture bgTangga;
    Texture activeBg;

    float stateTime;
    float walkTime;
    
    // Ayu's State/Position
    float ayuX = 100f;
    float ayuY = 180f;
    boolean isWalking = false;
    boolean walkDirectionRight = true;

    // Narrative/Scene control
    private enum SubScene {
        WAKING_UP, // Transisi bangun
        SCENE_7A, // Kamar
        PLAY_DRIVING, // Perjalanan menyetir
        DRIVING_QTE, // Quick Time Event menyetir
        DIALOG_DRIVING, // Dialog pasca QTE menyetir
        SCENE_7C, // Lobby
        SCENE_7D, // Tangga & Pingsan
        LOOP_TRANSITION // Fade out dan restart ke Chapter_One_TwoScreen (Loop 2)
    }
    SubScene currentSubScene = SubScene.WAKING_UP;

    // Typewriter
    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;
    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    // Driving QTE (Scene 7B) variables
    float qteTimer = 0f;
    boolean qteFinished = false;
    boolean qteSuccess = false;
    float drivingShakeTimer = 0f;
    // Scene 3 driving assets
    Texture mobilMerah;
    Texture kesakitan;
    Texture siluetMobil;
    Texture mobilRusak;
    boolean showMobilMerah = false;
    float mobilMerahX;
    boolean showKesakitan = false;
    boolean showSiluetMobil = false;
    float siluetMobilY;
    boolean showMobilRusak = false;
    boolean impactStarted = false;
    float impactTimer = 0f;
    float whiteFlashAlpha = 0f;

    private float wakeFadeAlpha = 1f;
    private float subsceneTimer = 0f;
    private Texture blackTexture;
    private Texture whiteTexture;

    // Visual Glitch / Faint Sequence
    float glitchIntensity = 0f;
    float headBuzzTimer = 0f;
    boolean isBuzzing = false;
    float faintAlpha = 0f;
    float loopAlpha = 0f;

    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public Chapter_Two_OneScreen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        textbox = new Texture("ui/textbox.png");
        font = new BitmapFont();
        font.getData().setScale(2f);

        // Load Backgrounds
        bgKamar = new Texture("backgrounds/kamar-ayu-6.png");
        bgKamarTidur = new Texture("backgrounds/Chapter_2/kamar-ayu-tidur.png");
        bgDriving = new Texture("backgrounds/scene-4.png"); // Using scene-4 road as driving bg
        bgLobby = new Texture("backgrounds/LIFT_PENUH.png");
        bgTangga = new Texture("backgrounds/TAMAN2.png"); // Fallback for stairs
        activeBg = bgKamarTidur;

        Pixmap pmBlack = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmBlack.setColor(Color.BLACK);
        pmBlack.fill();
        blackTexture = new Texture(pmBlack);
        pmBlack.dispose();

        Pixmap pmWhite = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmWhite.setColor(Color.WHITE);
        pmWhite.fill();
        whiteTexture = new Texture(pmWhite);
        pmWhite.dispose();

        setupAnimations();

        // Scene 3 driving assets
        mobilMerah = new Texture("backgrounds/mobil-merah.png");
        if (Gdx.files.internal("character/Ayu/kesakitan.png").exists()) {
            kesakitan = new Texture("character/Ayu/kesakitan.png");
        } else {
            kesakitan = ayuIdleFrame.getTexture();
        }
        if (Gdx.files.internal("backgrounds/siluet_mobil.png").exists()) {
            siluetMobil = new Texture("backgrounds/siluet_mobil.png");
        } else {
            siluetMobil = createSolidTexture(Color.DARK_GRAY, 1, 1);
        }
        if (Gdx.files.internal("backgrounds/mobil_rusak.png").exists()) {
            mobilRusak = new Texture("backgrounds/mobil_rusak.png");
        } else {
            mobilRusak = mobilMerah;
        }

        setupDialogQueue();
    }

    private Texture createSolidTexture(Color color, int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void setupAnimations() {
        Texture ayuIdleSheet;
        if (Gdx.files.internal("characters/ayu_idle_sheet.png").exists()) {
            ayuIdleSheet = new Texture("characters/ayu_idle_sheet.png");
            int frameWidth = ayuIdleSheet.getWidth() / 6;
            int frameHeight = ayuIdleSheet.getHeight();
            TextureRegion[][] tmp = TextureRegion.split(ayuIdleSheet, frameWidth, frameHeight);
            ayuIdleFrame = tmp[0][0];
        } else {
            ayuIdleSheet = new Texture("character/Ayu/diam-2.png");
            ayuIdleFrame = new TextureRegion(ayuIdleSheet);
        }

        // Walk right animation
        Array<TextureRegion> rightFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            if (Gdx.files.internal("character/Ayu/jalan_kanan_" + i + ".png").exists()) {
                rightFrames.add(new TextureRegion(new Texture("character/Ayu/jalan_kanan_" + i + ".png")));
            }
        }
        if (rightFrames.size > 0) {
            walkRightAnim = new Animation<>(0.1f, rightFrames, Animation.PlayMode.LOOP);
        }

        // Walk left animation
        Array<TextureRegion> leftFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            if (Gdx.files.internal("character/Ayu/jalan_kiri_" + i + ".png").exists()) {
                leftFrames.add(new TextureRegion(new Texture("character/Ayu/jalan_kiri_" + i + ".png")));
            }
        }
        if (leftFrames.size > 0) {
            walkLeftAnim = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);
        }

        if (Gdx.files.internal("character/Ayu/selesail_presentasi_kiri.png").exists()) {
            ayuSelesaiPresentasi = new Texture("character/Ayu/selesail_presentasi_kiri.png");
        } else if (Gdx.files.internal("character/Ayu/selesai_presentasi_kiri.png").exists()) {
            ayuSelesaiPresentasi = new Texture("character/Ayu/selesai_presentasi_kiri.png");
        }
    }

    private void setupDialogQueue() {
        dialogQueue = new LinkedList<>();
        // Dialog 7-A (Kamar Ayu)
        dialogQueue.add(new DialogLine("Narator", "Ayu terbangun di kamarnya dalam keadaan masih sakit kepala..."));
        dialogQueue.add(new DialogLine("Ayu", "Aduh, kepalaku... Kenapa sakit sekali? Apa yang terjadi kemarin ya?"));
        dialogQueue.add(new DialogLine("Sistem", "WAKE_UP"));
        dialogQueue.add(new DialogLine("Sistem", "RINGING_PHONE"));
        dialogQueue.add(new DialogLine("Alya", "Yu, dimana lu? Kita hari ini mau ada presentasi projek loh!"));
        dialogQueue.add(new DialogLine("Ayu", "Hah?! Hari ini?! Oh tidak, aku harus segera bergegas ke kampus!"));
        
        // Dialog 7-B (Perjalanan)
        dialogQueue.add(new DialogLine("Sistem", "START_DRIVING"));
        dialogQueue.add(new DialogLine("Ayu", "Kenapa jalanan ini terasa sangat familiar ya? Deja vu...?"));
        dialogQueue.add(new DialogLine("Sistem", "DRIVING_GAMEPLAY"));
        
        // Dialog 7-C (Lobby Kampus)
        dialogQueue.add(new DialogLine("Sistem", "START_LOBBY"));
        dialogQueue.add(new DialogLine("Narator", "Ayu sampai di kampus dan segera bergegas ke lobby..."));
        dialogQueue.add(new DialogLine("Ayu", "Astaga, antrian lift tidak masuk akal panjangnya! Aku tidak punya waktu!"));
        dialogQueue.add(new DialogLine("Ayu", "Lebih baik aku naik tangga saja ke lantai 3!"));

        // Dialog 7-D (Tangga)
        dialogQueue.add(new DialogLine("Sistem", "START_STAIRS"));
        dialogQueue.add(new DialogLine("Narator", "Ayu berlari menaiki tangga dengan cepat..."));
        dialogQueue.add(new DialogLine("Ayu", "Ayo cepat... tinggal sedikit lagi..."));
        dialogQueue.add(new DialogLine("Sistem", "TRIGGER_HEADACHE"));
        dialogQueue.add(new DialogLine("Narator", "Tiba-tiba dengungan hebat menyerang kepala Ayu..."));
        dialogQueue.add(new DialogLine("Sistem", "TRIGGER_FALL"));

        dequeueNextDialog();
    }

    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            if (currentDialog.character.equals("Sistem")) {
                handleSystemTrigger(currentDialog.text);
                dequeueNextDialog();
                return;
            }

            // When "Aduh, kepalaku..." appears: switch bg to kamar-ayu-6 + show Ayu
            if (currentSubScene == SubScene.SCENE_7A && currentDialog.text.contains("Aduh, kepalaku")) {
                activeBg = bgKamar;
                ayuX = 1150f;
            }

            targetText = currentDialog.text;
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
        } else {
            currentDialog = null;
        }
    }

    private void handleSystemTrigger(String command) {
        if (command.equals("WAKE_UP")) {
            activeBg = bgKamar;
        } else if (command.equals("RINGING_PHONE")) {
            // Visual/audio trigger can go here
        } else if (command.equals("START_DRIVING")) {
            currentSubScene = SubScene.PLAY_DRIVING;
            activeBg = bgDriving;
            ayuX = 100f;
            ayuY = 180f;
            isWalking = false;
            showMobilMerah = true;
            mobilMerahX = WORLD_WIDTH + 200f;
        } else if (command.equals("DRIVING_GAMEPLAY")) {
            currentSubScene = SubScene.PLAY_DRIVING;
        } else if (command.equals("START_LOBBY")) {
            currentSubScene = SubScene.SCENE_7C;
            activeBg = bgLobby;
            ayuX = 100f;
            ayuY = 150f;
        } else if (command.equals("START_STAIRS")) {
            currentSubScene = SubScene.SCENE_7D;
            activeBg = bgTangga;
            ayuX = 100f;
            ayuY = 150f;
        } else if (command.equals("TRIGGER_HEADACHE")) {
            isBuzzing = true;
            headBuzzTimer = 0f;
        } else if (command.equals("TRIGGER_FALL")) {
            // Start fade out and loop
            currentSubScene = SubScene.LOOP_TRANSITION;
        }
    }

    private void setupDrivingDialog() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        if (qteSuccess) {
            list.addFirst(new DialogLine("Ayu", "Aku harus tetap tancap gas biar tidak telat presentasi!"));
            list.addFirst(new DialogLine("Ayu", "Rasa deja vu ini benar-benar mengganggu, tapi aku harus fokus."));
            list.addFirst(new DialogLine("Ayu", "Fuuuh... Hampir saja! Untung refleksku cepat!"));
        } else {
            list.addFirst(new DialogLine("Ayu", "Waktu sudah mepet, aku tidak punya pilihan selain terus tancap gas!"));
            list.addFirst(new DialogLine("Ayu", "Kepalaku makin sakit karena kaget! Deja vu ini membuatku tidak fokus!"));
            list.addFirst(new DialogLine("Ayu", "AAAKH!! Rem mendadak!!"));
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

    private void handleInput(float delta) {
        if (Gdx.input.justTouched() && currentSubScene != SubScene.PLAY_DRIVING && currentSubScene != SubScene.DRIVING_QTE) {
            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                dequeueNextDialog();
            }
        }

        // Subscene gameplay movement
        if (currentSubScene == SubScene.PLAY_DRIVING) {
            isWalking = true;
            walkDirectionRight = true;
            ayuX += delta * 200f;
            if (ayuX > WORLD_WIDTH - 300f) ayuX = WORLD_WIDTH - 300f;
            if (showMobilMerah) {
                mobilMerahX -= delta * 250f;
                if (mobilMerahX - 200f <= ayuX) {
                    currentSubScene = SubScene.DRIVING_QTE;
                    qteTimer = 0f;
                    qteFinished = false;
                    qteSuccess = false;
                }
            }
        } else if (currentSubScene == SubScene.SCENE_7C) {
            // Move Ayu towards right to trigger next scene
            isWalking = false;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                ayuX += 250f * delta;
                isWalking = true;
                walkDirectionRight = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                ayuX -= 250f * delta;
                isWalking = true;
                walkDirectionRight = false;
            }

            if (ayuX > 1100f) {
                ayuX = 100f;
                dequeueNextDialog();
            }
        } else if (currentSubScene == SubScene.SCENE_7D) {
            // Move Ayu to climbing stairs
            isWalking = false;
            if (!isBuzzing) {
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    ayuX += 200f * delta;
                    ayuY += 100f * delta; // Climbing effect
                    isWalking = true;
                    walkDirectionRight = true;
                }
                if (ayuX > 800f) {
                    dequeueNextDialog();
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        if (isWalking) {
            walkTime += delta;
        }

        // Subscene Update Logic
        if (currentSubScene == SubScene.WAKING_UP) {
            subsceneTimer += delta;
            wakeFadeAlpha = 1f - (subsceneTimer / 2.0f);
            if (wakeFadeAlpha <= 0f) {
                wakeFadeAlpha = 0f;
                currentSubScene = SubScene.SCENE_7A;
                subsceneTimer = 0f;
            }
        } else {
            handleInput(delta);
            if (currentSubScene != SubScene.PLAY_DRIVING && currentSubScene != SubScene.DRIVING_QTE) {
                updateTypewriter(delta);
            }
        }

        // Gameplay calculations
        if (currentSubScene == SubScene.DRIVING_QTE) {
            updateDriving(delta);
        }

        if (isBuzzing) {
            headBuzzTimer += delta;
            glitchIntensity = MathUtils.sin(headBuzzTimer * 20f) * 20f;
            if (headBuzzTimer > 3.0f) {
                isBuzzing = false;
                glitchIntensity = 0f;
                dequeueNextDialog();
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.zoom = 1.0f;
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        if (drivingShakeTimer > 0f) {
            drivingShakeTimer -= delta;
            float shakeIntensity = qteSuccess ? 8f : 20f;
            camera.position.add(MathUtils.random(-shakeIntensity, shakeIntensity), MathUtils.random(-shakeIntensity, shakeIntensity), 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (currentSubScene == SubScene.WAKING_UP) {
            // Render Waking Up Fade out of black
            batch.draw(bgKamarTidur, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(1, 1, 1, wakeFadeAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        } else {
            // Render Background with optional headache offset
            float bgOffset = isBuzzing ? MathUtils.random(-glitchIntensity, glitchIntensity) : 0f;
            batch.draw(activeBg, bgOffset, 0, WORLD_WIDTH, WORLD_HEIGHT);

            // Render Player / Ayu
            if (currentSubScene == SubScene.SCENE_7A) {
                if (activeBg == bgKamar) {
                    float drawHeight = 334f;
                    float drawWidth = 198f;
                    batch.draw(ayuIdleFrame, ayuX, ayuY, drawWidth, drawHeight);
                }
            } else if (currentSubScene == SubScene.PLAY_DRIVING || currentSubScene == SubScene.SCENE_7C || currentSubScene == SubScene.SCENE_7D) {
                // Mobil Merah with scale approach
                if (showMobilMerah && mobilMerah != null) {
                    float dist = Math.max(1f, mobilMerahX - ayuX);
                    float scale = MathUtils.clamp(0.6f + 0.9f * (1f - dist / WORLD_WIDTH), 0.6f, 1.5f);
                    float w = 400f * scale;
                    float h = 300f * scale;
                    float x = mobilMerahX - w;
                    float y = ayuY;
                    batch.draw(mobilMerah, x, y, w, h);
                }
                if (showMobilRusak && mobilRusak != null) {
                    batch.draw(mobilRusak, 1400f, 100f, 400f, 300f);
                }
                if (showSiluetMobil && siluetMobil != null) {
                    batch.draw(siluetMobil, siluetMobilY * 0.5f, siluetMobilY, 400f, 300f);
                }
                if (showKesakitan && kesakitan != null) {
                    batch.draw(kesakitan, ayuX, ayuY, 198f, 334f);
                } else {
                    TextureRegion currentFrame = ayuIdleFrame;
                    if (isWalking) {
                        if (walkDirectionRight && walkRightAnim != null) {
                            currentFrame = walkRightAnim.getKeyFrame(walkTime);
                        } else if (!walkDirectionRight && walkLeftAnim != null) {
                            currentFrame = walkLeftAnim.getKeyFrame(walkTime);
                        }
                    }
                    batch.draw(currentFrame, ayuX, ayuY);
                }
            } else if (currentSubScene == SubScene.DRIVING_QTE) {
                if (showMobilMerah && mobilMerah != null) {
                    float dist = Math.max(1f, mobilMerahX - ayuX);
                    float scale = MathUtils.clamp(0.6f + 0.9f * (1f - dist / WORLD_WIDTH), 0.6f, 1.5f);
                    float w = 400f * scale;
                    float h = 300f * scale;
                    float x = mobilMerahX - w;
                    float y = ayuY;
                    batch.draw(mobilMerah, x, y, w, h);
                }
                if (whiteFlashAlpha > 0f) {
                    batch.setColor(1f, 1f, 1f, whiteFlashAlpha);
                    batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                    batch.setColor(Color.WHITE);
                }
                renderDrivingQTE();
            }

            // Render Textbox & Dialogs (Ch1 pop-up style)
            if (currentDialog != null && currentSubScene != SubScene.PLAY_DRIVING && currentSubScene != SubScene.DRIVING_QTE) {
            float bubbleWidth = 650f;
            float bubbleHeight = 180f;
            float bubbleX = 0f;
            float bubbleY = 0f;

            if ("Narator".equalsIgnoreCase(currentDialog.character)) {
                bubbleWidth = 900f;
                bubbleHeight = 150f;
                bubbleX = WORLD_WIDTH / 2f - (bubbleWidth / 2f);
                bubbleY = WORLD_HEIGHT - bubbleHeight - 50f;
            } else if ("Alya".equalsIgnoreCase(currentDialog.character)) {
                bubbleX = WORLD_WIDTH - bubbleWidth - 50f;
                bubbleY = ayuY + 150f;
            } else {
                bubbleX = ayuX + 32f - (bubbleWidth / 2f);
                bubbleY = ayuY + 150f;
            }

            if (bubbleX < 20f) bubbleX = 20f;
            if (bubbleX + bubbleWidth > WORLD_WIDTH - 20f) bubbleX = WORLD_WIDTH - 20f - bubbleWidth;

            // Background
            batch.draw(blackTexture, bubbleX, bubbleY, bubbleWidth, bubbleHeight);

            // Gold Border
            batch.setColor(Color.GOLD);
            float borderThickness = 3f;
            batch.draw(whiteTexture, bubbleX, bubbleY + bubbleHeight - borderThickness, bubbleWidth, borderThickness);
            batch.draw(whiteTexture, bubbleX, bubbleY, bubbleWidth, borderThickness);
            batch.draw(whiteTexture, bubbleX, bubbleY, borderThickness, bubbleHeight);
            batch.draw(whiteTexture, bubbleX + bubbleWidth - borderThickness, bubbleY, borderThickness, bubbleHeight);
            batch.setColor(Color.WHITE);

            // Speaker Name
            if (!"Narator".equalsIgnoreCase(currentDialog.character)) {
                font.getData().setScale(1.4f);
                font.setColor(Color.GOLD);
                font.draw(batch, currentDialog.character, bubbleX + 25f, bubbleY + bubbleHeight - 20f);
            }

            // Dialog Text
            font.setColor(Color.WHITE);
            float textY = "Narator".equalsIgnoreCase(currentDialog.character) ? (bubbleY + bubbleHeight - 35f) : (bubbleY + bubbleHeight - 60f);
            font.draw(batch, displayedText, bubbleX + 25f, textY, bubbleWidth - 50f, com.badlogic.gdx.utils.Align.left, true);

            // Indicator arrow
            if (charIndex >= targetText.length()) {
                if ((int)(stateTime * 2) % 2 == 0) {
                    batch.setColor(Color.GOLD);
                    float arrowSize = 12f;
                    float arrowX = bubbleX + bubbleWidth - 30f;
                    float arrowY = bubbleY + 20f;
                    batch.draw(whiteTexture, arrowX - arrowSize / 2f, arrowY, arrowSize, borderThickness);
                    batch.draw(whiteTexture, arrowX - arrowSize / 4f, arrowY - borderThickness, arrowSize / 2f, borderThickness);
                    batch.draw(whiteTexture, arrowX, arrowY - borderThickness * 2f, borderThickness, borderThickness);
                    batch.setColor(Color.WHITE);
                }
            }
            font.getData().setScale(2f);
        }

        // Fainting overlay (Fade to black)
        if (currentSubScene == SubScene.LOOP_TRANSITION) {
            faintAlpha += delta * 0.5f;
            if (faintAlpha > 1.0f) faintAlpha = 1.0f;
            batch.setColor(0, 0, 0, faintAlpha);
            // Draws full black overlay
            // Just reusing textbox texture but colored black
            batch.draw(textbox, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);

            if (faintAlpha >= 1.0f) {
                font.setColor(Color.RED);
                font.draw(batch, "Loop 1...", WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2);
                
                loopAlpha += delta;
                if (loopAlpha > 3.0f) {
                    // Transition to Chapter 2 (Loop 2) Screen
                    game.setScreen(new Chapter_One_TwoScreen(game));
                }
            }
        }
        } // Closing the 'else' block from line 422

        batch.end();
    }

    private void updateDriving(float delta) {
        qteTimer += delta;
        if (!qteFinished) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                qteSuccess = true;
                qteFinished = true;
                drivingShakeTimer = 0.5f;
            } else if (qteTimer >= 2.0f) {
                qteSuccess = false;
                qteFinished = true;
                drivingShakeTimer = 1.5f;
            }
        } else if (qteSuccess) {
            if (drivingShakeTimer <= 0f) {
                showMobilMerah = false;
                currentSubScene = SubScene.DIALOG_DRIVING;
                setupDrivingDialog();
                dequeueNextDialog();
            }
        } else {
            drivingShakeTimer -= delta;
            if (!impactStarted) {
                impactStarted = true;
                impactTimer = 0f;
                whiteFlashAlpha = 0.8f;
            }
            impactTimer += delta;
            if (whiteFlashAlpha > 0f) {
                whiteFlashAlpha -= delta * 3f;
                if (whiteFlashAlpha < 0f) whiteFlashAlpha = 0f;
            }
            if (impactTimer >= 0.3f && !showSiluetMobil) {
                showSiluetMobil = true;
                siluetMobilY = WORLD_HEIGHT + 200f;
            }
            if (showSiluetMobil) {
                siluetMobilY -= delta * 800f;
                if (siluetMobilY <= -400f) {
                    showSiluetMobil = false;
                    showKesakitan = true;
                    showMobilRusak = true;
                    showMobilMerah = false;
                }
            }
            if (impactTimer >= 3.0f) {
                showKesakitan = false;
                showMobilRusak = false;
                showMobilMerah = false;
                impactStarted = false;
                currentSubScene = SubScene.DIALOG_DRIVING;
                setupDrivingDialog();
                dequeueNextDialog();
            }
        }
    }

    private void renderDrivingQTE() {
        float qteWidth = 600f;
        float qteHeight = 250f;
        float qteX = (WORLD_WIDTH - qteWidth) / 2f;
        float qteY = (WORLD_HEIGHT - qteHeight) / 2f - 100f;

        // Background box QTE
        batch.draw(blackTexture, qteX, qteY, qteWidth, qteHeight);

        // Gold border
        batch.setColor(Color.GOLD);
        float thickness = 4f;
        batch.draw(whiteTexture, qteX + 6f, qteY, qteWidth - 12f, thickness);
        batch.draw(whiteTexture, qteX + 6f, qteY + qteHeight - thickness, qteWidth - 12f, thickness);
        batch.draw(whiteTexture, qteX, qteY + 6f, thickness, qteHeight - 12f);
        batch.draw(whiteTexture, qteX + qteWidth - thickness, qteY + 6f, thickness, qteHeight - 12f);
        batch.setColor(Color.WHITE);

        if (!qteFinished) {
            // Instruksi QTE
            font.getData().setScale(1.8f);
            font.setColor(Color.WHITE);
            String prompt = "!! HINDARI MOBIL DEPAN !!";
            glyphLayout.setText(font, prompt);
            font.draw(batch, prompt, qteX + (qteWidth - glyphLayout.width) / 2f, qteY + qteHeight - 40f);

            font.getData().setScale(2.5f);
            font.setColor(Color.GOLD);
            String keyPrompt = "Tekan 'W' Cepat!";
            glyphLayout.setText(font, keyPrompt);
            font.draw(batch, keyPrompt, qteX + (qteWidth - glyphLayout.width) / 2f, qteY + qteHeight / 2f + 15f);

            // Progress bar sisa waktu
            float maxTime = 2.0f;
            float remaining = Math.max(0f, maxTime - qteTimer);
            float progressRatio = remaining / maxTime;
            float barWidth = qteWidth - 80f;
            float barHeight = 20f;
            float barX = qteX + 40f;
            float barY = qteY + 40f;

            // Bar background (red/dark gray)
            batch.setColor(Color.DARK_GRAY);
            batch.draw(whiteTexture, barX, barY, barWidth, barHeight);

            // Active bar (orange/gold changing to red)
            Color barColor = Color.GOLD;
            if (progressRatio < 0.4f) {
                barColor = Color.RED;
            }
            batch.setColor(barColor);
            batch.draw(whiteTexture, barX, barY, barWidth * progressRatio, barHeight);
            batch.setColor(Color.WHITE);
        } else {
            // Tampilkan feedback hasil QTE
            font.getData().setScale(2.5f);
            if (qteSuccess) {
                font.setColor(Color.GREEN);
                String successText = "BERHASIL MENGHINDAR!";
                glyphLayout.setText(font, successText);
                font.draw(batch, successText, qteX + (qteWidth - glyphLayout.width) / 2f, qteY + qteHeight / 2f + 20f);
            } else {
                font.setColor(Color.RED);
                String failText = "HAMPIR TABRAKAN!";
                glyphLayout.setText(font, failText);
                font.draw(batch, failText, qteX + (qteWidth - glyphLayout.width) / 2f, qteY + qteHeight / 2f + 20f);
            }
        }
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        textbox.dispose();
        bgKamar.dispose();
        bgDriving.dispose();
        bgLobby.dispose();
        bgTangga.dispose();
        if (mobilMerah != null) mobilMerah.dispose();
        if (kesakitan != null) kesakitan.dispose();
        if (siluetMobil != null) siluetMobil.dispose();
        if (mobilRusak != null) mobilRusak.dispose();
        if (ayuSelesaiPresentasi != null) ayuSelesaiPresentasi.dispose();
    }
}
