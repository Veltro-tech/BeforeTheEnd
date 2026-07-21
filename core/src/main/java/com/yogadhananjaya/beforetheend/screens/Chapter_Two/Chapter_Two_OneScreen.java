package com.yogadhananjaya.beforetheend.screens.Chapter_Two;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
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
    Animation<TextureRegion> idleAnim;
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
    float idleTime;

    // Ayu's State/Position
    float ayuX = 100f;
    float ayuY = 180f;
    boolean isWalking = false;
    boolean walkDirectionRight = true;

    // Narrative/Scene control
    private enum SubScene {
        NIGHTMARE,
        WAKING_UP, // Transisi bangun
        SCENE_7A, // Kamar
        PLAY_DRIVING, // Perjalanan menyetir
        DRIVING_QTE, // Quick Time Event menyetir
        DIALOG_DRIVING, // Dialog pasca QTE menyetir
        SCENE_7C, // Lobby
        SCENE_7C_TRANSITION, // Fade to black before 7D
        SCENE_7D, // Tangga & Pingsan
        LOOP_TRANSITION // Fade out dan restart ke Chapter_One_TwoScreen (Loop 2)
    }

    SubScene currentSubScene = SubScene.NIGHTMARE;

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
    int scene7AStep = 0;

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

    private final java.util.List<NightmareText> nightmareTexts = new java.util.ArrayList<>();
    private final String[] nightmarePhrases = { "Gagal", "Semua hancur", "Tidak ada waktu", "Kamu terlambat", "Sia-sia",
            "Sudah berakhir" };
    private float textSpawnTimer = 0f;
    private Sound heartbeatSfx;
    private Sound nightmareBreathingSfx;
    private boolean nightmareSfxPlaying = false;
    boolean phoneTriggerQueued = false;
    boolean lobbyExploring = false;

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

    final float WORLD_WIDTH = 1920f;
    final float WORLD_HEIGHT = 1080f;

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
        bgTangga = new Texture("backgrounds/otw-kelas.png");
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

        if (Gdx.files.internal("SFX/heartbeat.mp3").exists())
            heartbeatSfx = Gdx.audio.newSound(Gdx.files.internal("SFX/heartbeat.mp3"));
        if (Gdx.files.internal("SFX/tunetank.com_breathing-heavily-female.wav").exists())
            nightmareBreathingSfx = Gdx.audio.newSound(Gdx.files.internal("SFX/tunetank.com_breathing-heavily-female.wav"));
    }

    private Texture createSolidTexture(Color color, int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void stopNightmareSfx() {
        nightmareSfxPlaying = false;
        if (heartbeatSfx != null) heartbeatSfx.stop();
        if (nightmareBreathingSfx != null) nightmareBreathingSfx.stop();
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

        // Walk right animation (yoga-jalan)
        Array<TextureRegion> rightFrames = new Array<>();
        for (int i = 0; i <= 12; i++) {
            String p = "character/Ayu/ayu-yoga-jalan/frame_" + (i < 10 ? "0" : "") + i + "_delay-0.11s.png";
            if (Gdx.files.internal(p).exists())
                rightFrames.add(new TextureRegion(new Texture(p)));
        }
        if (rightFrames.size > 0) {
            walkRightAnim = new Animation<>(0.11f, rightFrames, Animation.PlayMode.LOOP);
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

        // Idle yoga animation
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 1; i <= 229; i++) {
            String path = "character/Ayu/ayu-yoga/Ayu_yoga_" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                idleFrames.add(new TextureRegion(new Texture(path)));
            }
        }
        if (idleFrames.size > 0) {
            idleAnim = new Animation<>(0.08f, idleFrames, Animation.PlayMode.LOOP);
        }

        if (Gdx.files.internal("character/Ayu/selesail_presentasi_kiri.png").exists()) {
            ayuSelesaiPresentasi = new Texture("character/Ayu/selesail_presentasi_kiri.png");
        } else if (Gdx.files.internal("character/Ayu/selesai_presentasi_kiri.png").exists()) {
            ayuSelesaiPresentasi = new Texture("character/Ayu/selesai_presentasi_kiri.png");
        }
    }

    private void setupDialogQueue() {
        dialogQueue = new LinkedList<>();
        // Dialog 7-A (Kamar Ayu) — phone & driving triggered by player movement
        dialogQueue.add(new DialogLine("Narator", "Ayu terbangun di kamarnya dalam keadaan masih sakit kepala..."));
        dialogQueue.add(new DialogLine("Ayu", "Aduh, kepalaku... Kenapa sakit sekali? Apa yang terjadi kemarin ya?"));
        dialogQueue.add(new DialogLine("Sistem", "WAKE_UP"));

        dequeueNextDialog();
    }

    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            if (currentDialog.character.equals("Sistem")) {
                handleSystemTrigger(currentDialog.text);
                // After WAKE_UP, let player explore
                if (currentDialog.text.equals("WAKE_UP")) {
                    scene7AStep = 1;
                    phoneTriggerQueued = false;
                }
                if (currentDialog.text.equals("LOBBY_EXPLORE")) {
                    lobbyExploring = true;
                    currentDialog = null;
                    return;
                }
                dequeueNextDialog();
                return;
            }

            // When "Aduh, kepalaku..." appears: switch bg to kamar-ayu-6 + show Ayu di
            // kasur
            if (currentSubScene == SubScene.SCENE_7A && currentDialog.text.contains("Aduh, kepalaku")) {
                activeBg = bgKamar;
                ayuX = 483f;
            }

            targetText = currentDialog.text;
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
        } else {
            currentDialog = null;
            // Phone dialog fully consumed — now explore door
            if (scene7AStep == 2) {
                scene7AStep = 3;
            }
        }
    }

    private void handleSystemTrigger(String command) {
        if (command.equals("WAKE_UP")) {
            activeBg = bgKamar;
        } else if (command.equals("RINGING_PHONE")) {
            // Visual/audio trigger can go here
        } else if (command.equals("START_DRIVING")) {
            currentSubScene = SubScene.DIALOG_DRIVING;
            activeBg = bgDriving;
            ayuX = 100f;
            ayuY = 180f;
            isWalking = false;
            LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
            list.addFirst(new DialogLine("Sistem", "START_GAMEPLAY"));
            list.addFirst(new DialogLine("Ayu", "Kenapa jalanan ini terasa sangat familiar ya? Deja vu...?"));
        } else if (command.equals("START_GAMEPLAY")) {
            currentSubScene = SubScene.PLAY_DRIVING;
            showMobilMerah = true;
            mobilMerahX = WORLD_WIDTH + 200f;
        } else if (command.equals("START_LOBBY")) {
            currentSubScene = SubScene.SCENE_7C;
            activeBg = bgLobby;
            ayuX = 100f;
            ayuY = 180f;
            scene7AStep = 0;
        } else if (command.equals("LOBBY_EXPLORE")) {
            lobbyExploring = true;
        } else if (command.equals("START_STAIRS")) {
            currentSubScene = SubScene.SCENE_7D;
            activeBg = bgTangga;
            ayuX = 100f;
            ayuY = 180f;
        } else if (command.equals("TRIGGER_HEADACHE")) {
            isBuzzing = true;
            headBuzzTimer = 0f;
        } else if (command.equals("TRIGGER_FALL")) {
            // Start fade out and loop
            currentSubScene = SubScene.LOOP_TRANSITION;
        }
    }

    private void queueAfterDrivingDialogs() {
        dialogQueue.add(new DialogLine("Sistem", "START_LOBBY"));
        dialogQueue.add(new DialogLine("Narator", "Ayu sampai di kampus dan segera bergegas ke lobby..."));
        dialogQueue.add(new DialogLine("Ayu", "Astaga, antrian lift tidak masuk akal panjangnya! Aku tidak punya waktu!"));
        dialogQueue.add(new DialogLine("Ayu", "Lebih baik aku naik tangga saja ke lantai 3!"));
        dialogQueue.add(new DialogLine("Sistem", "LOBBY_EXPLORE"));
        dialogQueue.add(new DialogLine("Narator", "Ayu berjalan menyusuri koridor menuju ruang sidang..."));
        dialogQueue.add(new DialogLine("Ayu", "Sedikit lagi... aku harus sampai tepat waktu..."));
        dialogQueue.add(new DialogLine("Sistem", "TRIGGER_HEADACHE"));
        dialogQueue.add(new DialogLine("Narator", "Tiba-tiba dengungan hebat menyerang kepala Ayu..."));
        dialogQueue.add(new DialogLine("Sistem", "TRIGGER_FALL"));
    }

    private void setupDrivingDialog() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        if (qteSuccess) {
            list.addFirst(new DialogLine("Ayu", "Aku harus tetap tancap gas biar tidak telat presentasi!"));
            list.addFirst(new DialogLine("Ayu", "Rasa deja vu ini benar-benar mengganggu, tapi aku harus fokus."));
            list.addFirst(new DialogLine("Ayu", "Fuuuh... Hampir saja! Untung refleksku cepat!"));
        } else {
            list.addFirst(new DialogLine("Ayu", "Waktu sudah mepet, aku tidak punya pilihan selain terus tancap gas!"));
            list.addFirst(
                    new DialogLine("Ayu", "Kepalaku makin sakit karena kaget! Deja vu ini membuatku tidak fokus!"));
            list.addFirst(new DialogLine("Ayu", "AAAKH!! Rem mendadak!!"));
        }
        queueAfterDrivingDialogs();
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
        if (Gdx.input.justTouched() && currentSubScene != SubScene.PLAY_DRIVING
                && currentSubScene != SubScene.DRIVING_QTE
                && !(currentSubScene == SubScene.SCENE_7C && lobbyExploring)) {
            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                dequeueNextDialog();
            }
        }

        // Scene 7-A interactive exploration (press E to interact)
        if (currentSubScene == SubScene.SCENE_7A && scene7AStep != 0 && scene7AStep != 2) {
            isWalking = false;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                ayuX -= 250f * delta;
                isWalking = true;
                walkDirectionRight = false;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                ayuX += 250f * delta;
                isWalking = true;
                walkDirectionRight = true;
            }
            ayuX = MathUtils.clamp(ayuX, 50f, WORLD_WIDTH - 50f);

            // Press E near phone → answer call
            if (ayuX >= 400f && scene7AStep == 1 && Gdx.input.isKeyJustPressed(Input.Keys.E) && !phoneTriggerQueued) {
                phoneTriggerQueued = true;
                scene7AStep = 2;
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Ayu", "Hah?! Hari ini?! Oh tidak, aku harus segera bergegas ke kampus!"));
                list.addFirst(new DialogLine("Alya", "Yu, dimana lu? Kita hari ini mau ada presentasi projek loh!"));
                list.addFirst(new DialogLine("Sistem", "RINGING_PHONE"));
                dequeueNextDialog();
                return;
            }
            // Press E at door → START_DRIVING
            if (ayuX >= WORLD_WIDTH - 300f && scene7AStep == 3 && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                scene7AStep = 4;
                LinkedList<DialogLine> list2 = (LinkedList<DialogLine>) dialogQueue;
                list2.addFirst(new DialogLine("Sistem", "START_DRIVING"));
                dequeueNextDialog();
                return;
            }
        }

        // Debug: F7 restart ke scene 7-A
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            stopNightmareSfx();
            currentSubScene = SubScene.NIGHTMARE;
            subsceneTimer = 0f;
            nightmareTexts.clear();
            activeBg = bgKamarTidur;
            ayuX = 100f;
            ayuY = 180f;
            showMobilMerah = false;
            showKesakitan = false;
            showSiluetMobil = false;
            showMobilRusak = false;
            impactStarted = false;
            scene7AStep = 0;
            phoneTriggerQueued = false;
            lobbyExploring = false;
            idleTime = 0f;
            dialogQueue.clear();
            currentDialog = null;
            setupDialogQueue();
            return;
        }

        // Debug: F10 ke Loop 3
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            game.setScreen(new Chapter_Three_Loop3Screen(game));
            return;
        }

        // Debug: F9 ke Loop 2
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            game.setScreen(new Chapter_Two_Loop2Screen(game));
            return;
        }

        // Debug: F8 ke Scene 7-D
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            currentSubScene = SubScene.SCENE_7D;
            activeBg = bgTangga;
            ayuX = 100f;
            ayuY = 180f;
            isWalking = false;
            isBuzzing = false;
            glitchIntensity = 0f;
            lobbyExploring = false;
            dialogQueue.clear();
            currentDialog = null;
            dialogQueue.add(new DialogLine("Narator", "Ayu berjalan menyusuri koridor menuju ruang sidang..."));
            dialogQueue.add(new DialogLine("Ayu", "Sedikit lagi... aku harus sampai tepat waktu..."));
            dialogQueue.add(new DialogLine("Sistem", "TRIGGER_HEADACHE"));
            dialogQueue.add(new DialogLine("Narator", "Tiba-tiba dengungan hebat menyerang kepala Ayu..."));
            dialogQueue.add(new DialogLine("Sistem", "TRIGGER_FALL"));
            dequeueNextDialog();
            return;
        }

        // Subscene gameplay movement
        if (currentSubScene == SubScene.PLAY_DRIVING) {
            isWalking = true;
            walkDirectionRight = true;
            ayuX += delta * 200f;
            if (ayuX > WORLD_WIDTH - 300f)
                ayuX = WORLD_WIDTH - 300f;
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
            if (!lobbyExploring) {
                // Dialog phase: player still moves freely
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
            } else {
                // Exploring: walk to stairs, press E
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
                ayuX = MathUtils.clamp(ayuX, 50f, WORLD_WIDTH - 50f);
                if (ayuX >= WORLD_WIDTH * 0.7f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    lobbyExploring = false;
                    currentSubScene = SubScene.SCENE_7C_TRANSITION;
                    subsceneTimer = 0f;
                }
            }
        } else if (currentSubScene == SubScene.SCENE_7D) {
            isWalking = false;
            if (!isBuzzing) {
                ayuX += 200f * delta;
                isWalking = true;
                walkDirectionRight = true;
                if (ayuX >= WORLD_WIDTH * 0.25f) {
                    dequeueNextDialog();
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        // Subscene Update Logic
        if (currentSubScene == SubScene.NIGHTMARE) {
            if (!nightmareSfxPlaying) {
                nightmareSfxPlaying = true;
                if (heartbeatSfx != null) heartbeatSfx.loop(0.6f);
                if (nightmareBreathingSfx != null) nightmareBreathingSfx.loop(0.5f);
            }
            subsceneTimer += delta;
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
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                stopNightmareSfx();
                currentSubScene = SubScene.WAKING_UP;
                subsceneTimer = 0f;
                nightmareTexts.clear();
            } else if (subsceneTimer >= 10.0f) {
                stopNightmareSfx();
                currentSubScene = SubScene.WAKING_UP;
                subsceneTimer = 0f;
                nightmareTexts.clear();
            }
        } else if (currentSubScene == SubScene.WAKING_UP) {
            subsceneTimer += delta;
            wakeFadeAlpha = 1f - (subsceneTimer / 2.0f);
            if (wakeFadeAlpha <= 0f) {
                wakeFadeAlpha = 0f;
                currentSubScene = SubScene.SCENE_7A;
                subsceneTimer = 0f;
                nightmareTexts.clear();
            }
        } else if (currentSubScene == SubScene.SCENE_7C_TRANSITION) {
            subsceneTimer += delta;
            if (subsceneTimer >= 2.0f) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "START_STAIRS"));
                dequeueNextDialog();
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
        if (currentSubScene == SubScene.NIGHTMARE) {
            float shakeIntensity = 5f;
            camera.position.add(MathUtils.random(-shakeIntensity, shakeIntensity),
                    MathUtils.random(-shakeIntensity, shakeIntensity), 0);
        }
        if (drivingShakeTimer > 0f) {
            drivingShakeTimer -= delta;
            float shakeIntensity = qteSuccess ? 8f : 20f;
            camera.position.add(MathUtils.random(-shakeIntensity, shakeIntensity),
                    MathUtils.random(-shakeIntensity, shakeIntensity), 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (currentSubScene == SubScene.NIGHTMARE) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            for (NightmareText nt : nightmareTexts) {
                font.getData().setScale(nt.scale);
                font.setColor(nt.color);
                font.draw(batch, nt.text, nt.x, nt.y);
            }
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        } else if (currentSubScene == SubScene.WAKING_UP) {
            // Render Waking Up Fade out of black
            batch.draw(bgKamarTidur, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(1, 1, 1, wakeFadeAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        } else if (currentSubScene == SubScene.SCENE_7C_TRANSITION) {
            batch.draw(activeBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else {
            // Render Background with optional headache offset
            float bgOffset = isBuzzing ? MathUtils.random(-glitchIntensity, glitchIntensity) : 0f;
            batch.draw(activeBg, bgOffset, 0, WORLD_WIDTH, WORLD_HEIGHT);

            // Render Player / Ayu
            if (currentSubScene == SubScene.SCENE_7A && activeBg == bgKamar) {
                drawCharacter(delta);
            } else if (currentSubScene == SubScene.PLAY_DRIVING || currentSubScene == SubScene.SCENE_7C
                    || currentSubScene == SubScene.SCENE_7D) {
                // Mobil Merah with scale approach
                if (showMobilMerah && mobilMerah != null) {
                    float x = mobilMerahX - 500f;
                    float y = ayuY * 0.72f;
                    batch.draw(mobilMerah, x, y, 500f, 375f);
                }
                if (showMobilRusak && mobilRusak != null) {
                    batch.draw(mobilRusak, 1400f, 100f, 500f, 375f);
                }
                if (showSiluetMobil && siluetMobil != null) {
                    batch.draw(siluetMobil, siluetMobilY * 0.5f, siluetMobilY, 500f, 375f);
                }
                if (showKesakitan && kesakitan != null) {
                    batch.draw(kesakitan, ayuX, ayuY, 198f, 334f);
                } else {
                    drawCharacter(delta);
                }
            } else if (currentSubScene == SubScene.DRIVING_QTE) {
                if (showMobilMerah && mobilMerah != null) {
                    float x = mobilMerahX - 500f;
                    float y = ayuY * 0.72f;
                    batch.draw(mobilMerah, x, y, 500f, 375f);
                }
                if (whiteFlashAlpha > 0f) {
                    batch.setColor(1f, 1f, 1f, whiteFlashAlpha);
                    batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                    batch.setColor(Color.WHITE);
                }
                renderDrivingQTE();
            }

            // Render Textbox & Dialogs (Ch1 pop-up style)
            if (currentDialog != null && currentSubScene != SubScene.PLAY_DRIVING
                    && currentSubScene != SubScene.DRIVING_QTE) {
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
                    bubbleY = ayuY + 195f;
                } else {
                    bubbleX = ayuX + 32f - (bubbleWidth / 2f);
                    bubbleY = ayuY + 418f;
                }

                if (bubbleX < 20f)
                    bubbleX = 20f;
                if (bubbleX + bubbleWidth > WORLD_WIDTH - 20f)
                    bubbleX = WORLD_WIDTH - 20f - bubbleWidth;

                // Background
                batch.draw(blackTexture, bubbleX, bubbleY, bubbleWidth, bubbleHeight);

                // Gold Border
                batch.setColor(Color.GOLD);
                float borderThickness = 3f;
                batch.draw(whiteTexture, bubbleX, bubbleY + bubbleHeight - borderThickness, bubbleWidth,
                        borderThickness);
                batch.draw(whiteTexture, bubbleX, bubbleY, bubbleWidth, borderThickness);
                batch.draw(whiteTexture, bubbleX, bubbleY, borderThickness, bubbleHeight);
                batch.draw(whiteTexture, bubbleX + bubbleWidth - borderThickness, bubbleY, borderThickness,
                        bubbleHeight);
                batch.setColor(Color.WHITE);

                // Speaker Name
                if (!"Narator".equalsIgnoreCase(currentDialog.character)) {
                    font.getData().setScale(1.4f);
                    font.setColor(Color.GOLD);
                    font.draw(batch, currentDialog.character, bubbleX + 25f, bubbleY + bubbleHeight - 20f);
                }

                // Dialog Text
                font.setColor(Color.WHITE);
                float textY = "Narator".equalsIgnoreCase(currentDialog.character) ? (bubbleY + bubbleHeight - 35f)
                        : (bubbleY + bubbleHeight - 60f);
                font.draw(batch, displayedText, bubbleX + 25f, textY, bubbleWidth - 50f,
                        com.badlogic.gdx.utils.Align.left, true);

                // Indicator arrow
                if (charIndex >= targetText.length()) {
                    if ((int) (stateTime * 2) % 2 == 0) {
                        batch.setColor(Color.GOLD);
                        float arrowSize = 12f;
                        float arrowX = bubbleX + bubbleWidth - 30f;
                        float arrowY = bubbleY + 20f;
                        batch.draw(whiteTexture, arrowX - arrowSize / 2f, arrowY, arrowSize, borderThickness);
                        batch.draw(whiteTexture, arrowX - arrowSize / 4f, arrowY - borderThickness, arrowSize / 2f,
                                borderThickness);
                        batch.draw(whiteTexture, arrowX, arrowY - borderThickness * 2f, borderThickness,
                                borderThickness);
                        batch.setColor(Color.WHITE);
                    }
                }
                font.getData().setScale(2f);
            }

            // Scene 7-A interaction hints (above Ayu, Ch1 style)
            if (currentSubScene == SubScene.SCENE_7A && scene7AStep != 0 && scene7AStep != 2) {
                String hint = null;
                if (scene7AStep == 1 && ayuX >= 400f)
                    hint = "Tekan E untuk angkat telepon";
                else if (scene7AStep == 1)
                    hint = "Coba ke kanan...";
                else if (scene7AStep == 3 && ayuX >= WORLD_WIDTH - 300f)
                    hint = "Tekan E untuk keluar";
                else if (scene7AStep == 3)
                    hint = "Pergi ke kanan menuju pintu...";
                if (hint != null) {
                    float hintW = 400f;
                    float hintH = 55f;
                    float hintX = ayuX + 99f - hintW / 2f;
                    float hintY = ayuY + 401f + 20f;
                    if (hintX < 10f) hintX = 10f;
                    if (hintX + hintW > WORLD_WIDTH - 10f) hintX = WORLD_WIDTH - 10f - hintW;
                    batch.draw(blackTexture, hintX, hintY, hintW, hintH);
                    batch.setColor(Color.GOLD);
                    batch.draw(whiteTexture, hintX, hintY + hintH - 3f, hintW, 3f);
                    batch.draw(whiteTexture, hintX, hintY, hintW, 3f);
                    batch.draw(whiteTexture, hintX, hintY, 3f, hintH);
                    batch.draw(whiteTexture, hintX + hintW - 3f, hintY, 3f, hintH);
                    batch.setColor(Color.WHITE);
                    font.getData().setScale(1.2f);
                    font.setColor(Color.GOLD);
                    glyphLayout.setText(font, hint);
                    font.draw(batch, hint, hintX + (hintW - glyphLayout.width) / 2f, hintY + hintH - 15f);
                    font.getData().setScale(2f);
                    font.setColor(Color.WHITE);
                }
            }

            // Lobby explore hint
            if (currentSubScene == SubScene.SCENE_7C && lobbyExploring) {
                String hint = ayuX >= WORLD_WIDTH * 0.7f ? "Tekan E untuk Naik Tangga" : "Pergi ke kanan menuju tangga...";
                float hintW = 400f;
                float hintH = 55f;
                float hintX = ayuX + 99f - hintW / 2f;
                float hintY = ayuY + 361f + 20f;
                if (hintX < 10f) hintX = 10f;
                if (hintX + hintW > WORLD_WIDTH - 10f) hintX = WORLD_WIDTH - 10f - hintW;
                batch.draw(blackTexture, hintX, hintY, hintW, hintH);
                batch.setColor(Color.GOLD);
                batch.draw(whiteTexture, hintX, hintY + hintH - 3f, hintW, 3f);
                batch.draw(whiteTexture, hintX, hintY, hintW, 3f);
                batch.draw(whiteTexture, hintX, hintY, 3f, hintH);
                batch.draw(whiteTexture, hintX + hintW - 3f, hintY, 3f, hintH);
                batch.setColor(Color.WHITE);
                font.getData().setScale(1.2f);
                font.setColor(Color.GOLD);
                glyphLayout.setText(font, hint);
                font.draw(batch, hint, hintX + (hintW - glyphLayout.width) / 2f, hintY + hintH - 15f);
                font.getData().setScale(2f);
                font.setColor(Color.WHITE);
            }

            // Fainting overlay (Fade to black)
            if (currentSubScene == SubScene.LOOP_TRANSITION) {
                faintAlpha += delta * 0.5f;
                if (faintAlpha > 1.0f)
                    faintAlpha = 1.0f;
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
                        game.setScreen(new Chapter_Two_Loop2Screen(game));
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
                if (whiteFlashAlpha < 0f)
                    whiteFlashAlpha = 0f;
            }
            if (impactTimer >= 0.3f && !showSiluetMobil) {
                showSiluetMobil = true;
                siluetMobilY = WORLD_HEIGHT + 200f;
            }
            if (showSiluetMobil) {
                siluetMobilY -= delta * 800f;
                if (siluetMobilY <= -500f) {
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

    private void drawCharacter(float delta) {
        float drawHeight = 361f;
        TextureRegion currentFrame = null;

        if (isWalking) {
            float speedMultiplier = 1.0f;
            if (currentSubScene == SubScene.PLAY_DRIVING
                    && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                speedMultiplier = 1.8f;
            }
            walkTime += delta * speedMultiplier;
            if (walkDirectionRight && walkRightAnim != null) {
                currentFrame = walkRightAnim.getKeyFrame(walkTime);
            } else if (!walkDirectionRight && walkLeftAnim != null) {
                currentFrame = walkLeftAnim.getKeyFrame(walkTime);
            }
        } else {
            walkTime = 0f;
        }

        if (currentFrame == null) {
            if (idleAnim != null) {
                idleTime += delta;
                currentFrame = idleAnim.getKeyFrame(idleTime);
            } else {
                currentFrame = ayuIdleFrame;
            }
        } else {
            idleTime = 0f;
        }

        float drawWidth = drawHeight;
        if (currentFrame.getRegionHeight() > 0) {
            drawWidth = drawHeight * ((float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight());
        }
        batch.draw(currentFrame, ayuX, ayuY, drawWidth, drawHeight);
    }

    @Override
    public void dispose() {
        stopNightmareSfx();
        if (heartbeatSfx != null) heartbeatSfx.dispose();
        if (nightmareBreathingSfx != null) nightmareBreathingSfx.dispose();
        batch.dispose();
        font.dispose();
        textbox.dispose();
        bgKamar.dispose();
        bgDriving.dispose();
        bgLobby.dispose();
        bgTangga.dispose();
        if (mobilMerah != null)
            mobilMerah.dispose();
        if (kesakitan != null)
            kesakitan.dispose();
        if (siluetMobil != null)
            siluetMobil.dispose();
        if (mobilRusak != null)
            mobilRusak.dispose();
        if (ayuSelesaiPresentasi != null)
            ayuSelesaiPresentasi.dispose();
    }
}
