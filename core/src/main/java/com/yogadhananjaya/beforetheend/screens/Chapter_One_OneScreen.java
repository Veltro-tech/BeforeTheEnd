package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
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
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

import java.util.ArrayList;
import java.util.List;

public class Chapter_One_OneScreen extends ScreenAdapter {
    private enum State {
        WAKING_UP,
        DIALOG_ROOM, // Kamar
        PLAY_ROOM,
        ROOM_TO_KITCHEN_TRANSITION,
        DIALOG_KITCHEN, // Dapur (Ibu memanggil, Ayu sarapan)
        PLAY_KITCHEN,
        KITCHEN_BATH_TRANSITION,
        PLAY_KITCHEN_RIGHT_DOOR_CHECKED,
        PLAY_KITCHEN_LEFT_DOOR_CHECKED,
        DIALOG_KITCHEN_RIGHT_DOOR,
        DIALOG_KITCHEN_LEFT_DOOR,
        KITCHEN_TO_DRIVING_TRANSITION,
        PLAY_DRIVING,
        DRIVING_QTE,
        DIALOG_DRIVING,
        DRIVING_TO_GARDEN_TRANSITION,
        PLAY_GARDEN, // Scene 4: Taman Kampus
        GARDEN_TO_LOBBY_TRANSITION,
        PLAY_LOBBY, // Scene 5: Lift Penuh & Tangga
        LOBBY_TO_CLASS_TRANSITION,
        PLAY_CLASSROOM, // Masuk kelas, jalan ke dosen penguji
        DIALOG_CLASSROOM_START, // Dialog konfrontasi awal Ayu & Dosen
        SCENE6_CLASSROOM, // Scene 6: Ruang Kelas Presentasi
        FAINT_SEQUENCE, // Pusing, pandangan gelap
        TITLE_CARD // Judul Game emas di layar hitam
    }

    private final BeforeTheEndGame game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    // States
    private State currentState = State.WAKING_UP;
    private float stateTimer = 0f;

    // Textures & Assets
    private Texture roomBg;
    private Texture roomBgInitial;
    private Texture kitchenBg;
    private Texture scene3Bg; // Driving
    private Texture mobilMerah;
    private boolean showMobilMerah = false;
    private Texture scene4Bg; // Garden (TAMAN2.png)
    private Texture scene5Bg; // Lobby (LIFT_PENUH.png)
    private Texture scene6Bg; // Classroom (RUANG_KELAS.png)

    private Texture ayuIdle;
    private Texture ayuDiam;
    private Texture ayuPanikJam;
    private Texture ayuSyok;
    private Animation<TextureRegion> ayuKelelahanAnim;
    private float ayuKelelahanTime = 0f;
    private Texture ayuPusing;
    private Texture ayuPingsan;
    private Texture ayuMohon;
    private Texture ayuHadapKiri;
    private Texture ayuPakaianTidur;
    private Texture ayuSelesaiPresentasi;
    private Music showerMusic;
    private float gardenRingingTimer = 0f;
    private boolean ringEarsStopped = false;
    private Texture textbox;
    private Texture blackTexture;
    private Texture whiteTexture;

    // Support Character Textures
    private Texture ibuSprite;
    private Texture dosenSprite;
    private Texture temanSprite;

    // Impact assets
    private Texture kesakitan;
    private Texture siluetMobil;
    private Texture mobilRusak;
    private boolean showKesakitan = false;
    private boolean showSiluetMobil = false;
    private float siluetMobilY;
    private boolean showMobilRusak = false;
    private boolean impactStarted = false;
    private float impactTimer = 0f;
    private float whiteFlashAlpha = 0f;

    private float transitionTimer = 0f;
    private boolean useInitialBg = false;
    private float redFlashAlpha = 0f;
    private float mobilMerahX;

    // Animations
    private Animation<TextureRegion> walkRightAnim;
    private Animation<TextureRegion> walkLeftAnim;
    private float walkTime = 0f;

    // Waking Up Phase variables
    private float wakeFadeAlpha = 1f;

    // Dialogue Lines Data
    private static class DialogInfo {
        String speaker;
        String text;
        Texture expression;

        DialogInfo(String speaker, String text) {
            this(speaker, text, null);
        }

        DialogInfo(String speaker, String text, Texture expression) {
            this.speaker = speaker;
            this.text = text;
            this.expression = expression;
        }
    }

    private final List<DialogInfo> currentDialogs = new ArrayList<>();
    private int currentDialogIndex = 0;
    private String displayedDialogText = "";
    private float typeTimer = 0f;
    private float typeSpeed = 0.04f;
    private int charIndex = 0;

    // Play Phase variables
    private float ayuX = 1150f;
    private float ayuY = 180f;
    private final float AYU_SPEED = 250f;
    private boolean isMoving = false;
    private boolean facingRight = true;

    // Driving Cutscene Variables
    private float drivingProgress = 0f;
    private float drivingDialogTimer = 0f;
    private int drivingDialogStep = 0;
    private float qteTimer = 0f;
    private boolean qteFinished = false;
    private boolean qteSuccess = false;
    private float drivingShakeTimer = 0f;
    private DialogInfo[] drivingDialogs;

    // Classroom Cutscene Variables
    private int classroomStep = 0;
    private float classroomTimer = 0f;
    private DialogInfo[] classroomStartDialogs;
    private DialogInfo[] classroomDialogs;

    private float faintTimer = 0f;
    private float titleAlpha = 0f;
    private boolean rightDoorChecked = false;
    private boolean leftDoorChecked = false;
    private boolean sudahMandi = false;
    private boolean sudahSarapan = false;
    private boolean midRoomDialogTriggered = false;
    private boolean showingChoice = false;
    private int roomChoice = 0;
    private Sound messageSfx;
    private Sound doorSfx;
    private Sound carHonkSfx;
    private Sound tireSkidSfx;
    private Sound carCrashSfx;
    private Sound heartbeatSfx;
    private boolean honkPlayed = false;
    private boolean skidPlayed = false;
    private boolean crashPlayed = false;
    private Music bgmMusic;
    private Music shockBgmMusic;
    private Sound ayuShockSfx;
    private boolean ayuShockPlayed = false;
    private Sound ringEarsSfx;
    private Sound heavyBreathingSfx;
    private Sound crowdChatterSfx;
    private Sound footstepSfx;
    private Sound showerSfx;
    private Music classroomBgm;
    private Sound bodyFallSfx;
    private Sound dramaticBoomSfx;
    private boolean gardenAudioPlayed = false;
    private boolean crowdChatterPlayed = false;
    private boolean bodyFallPlayed = false;
    private boolean dramaticBoomPlayed = false;
    private long crowdChatterId = -1;
    private long heavyBreathingId = -1;
    private long ringEarsId = -1;
    private long currentSfxId = -1;

    private final float WORLD_WIDTH = 1920f;
    private final float WORLD_HEIGHT = 1080f;

    public Chapter_One_OneScreen(final BeforeTheEndGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        this.batch = game.batch;
        this.font = new BitmapFont();
        this.font.getData().setScale(2f);
        this.glyphLayout = new GlyphLayout();

        loadAssets();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
        dispose();
    }

    private Texture createSolidTexture(Color color, int width, int height) {
        Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void loadAssets() {
        // Room BG (kamar-ayu-6.png)
        if (Gdx.files.internal("backgrounds/kamar-ayu-6.png").exists()) {
            roomBg = new Texture("backgrounds/kamar-ayu-6.png");
        } else {
            roomBg = createSolidTexture(Color.DARK_GRAY, 1, 1);
        }

        // Room Initial BG (kamar-ayu-duduk-2.png)
        if (Gdx.files.internal("backgrounds/kamar-ayu-duduk-2.png").exists()) {
            roomBgInitial = new Texture("backgrounds/kamar-ayu-duduk-2.png");
        } else {
            roomBgInitial = createSolidTexture(Color.DARK_GRAY, 1, 1);
        }

        // Kitchen BG (dapur3.png)
        if (Gdx.files.internal("backgrounds/dapur-5.png").exists()) {
            kitchenBg = new Texture("backgrounds/dapur-5.png");
        } else {
            kitchenBg = createSolidTexture(Color.GRAY, 1, 1);
        }

        // Scene 3 BG (scene-4.png)
        if (Gdx.files.internal("backgrounds/scene-4.png").exists()) {
            scene3Bg = new Texture("backgrounds/scene-4.png");
        } else {
            scene3Bg = createSolidTexture(Color.NAVY, 1, 1);
        }

        // Mobil Merah
        if (Gdx.files.internal("backgrounds/mobil-merah.png").exists()) {
            mobilMerah = new Texture("backgrounds/mobil-merah.png");
        } else {
            mobilMerah = createSolidTexture(Color.RED, 1, 1);
        }

        // Impact assets
        if (Gdx.files.internal("character/Ayu/kesakitan.png").exists()) {
            kesakitan = new Texture("character/Ayu/kesakitan.png");
        } else {
            kesakitan = createSolidTexture(Color.PINK, 100, 100);
        }
        if (Gdx.files.internal("backgrounds/siluet_mobil.png").exists()) {
            siluetMobil = new Texture("backgrounds/siluet_mobil.png");
        } else {
            siluetMobil = null;
        }
        if (Gdx.files.internal("backgrounds/mobil_rusak.png").exists()) {
            mobilRusak = new Texture("backgrounds/mobil_rusak.png");
        } else {
            mobilRusak = null;
        }

        // Scene 4 BG (TAMAN2.png)
        if (Gdx.files.internal("backgrounds/TAMAN2.png").exists()) {
            scene4Bg = new Texture("backgrounds/TAMAN2.png");
        } else {
            scene4Bg = createSolidTexture(Color.FOREST, 1, 1);
        }

        // Scene 5 BG (otw-kelas.png)
        if (Gdx.files.internal("backgrounds/otw-kelas.png").exists()) {
            scene5Bg = new Texture("backgrounds/otw-kelas.png");
        } else {
            scene5Bg = createSolidTexture(Color.SLATE, 1, 1);
        }

        // Scene 6 BG (ruang-kelas-2.png)
        if (Gdx.files.internal("backgrounds/ruang-kelas-2.png").exists()) {
            scene6Bg = new Texture("backgrounds/ruang-kelas-2.png");
        } else {
            scene6Bg = createSolidTexture(Color.BROWN, 1, 1);
        }

        // Character Ayu Expressions
        if (Gdx.files.internal("character/ekspresi_ayu/diam-3.png").exists()) {
            ayuDiam = new Texture("character/ekspresi_ayu/diam-3.png");
        } else if (Gdx.files.internal("character/Ayu/diam-3.png").exists()) {
            ayuDiam = new Texture("character/Ayu/diam-3.png");
        } else {
            ayuDiam = createSolidTexture(Color.PINK, 64, 128);
        }
        ayuIdle = ayuDiam;

        if (Gdx.files.internal("character/ekspresi_ayu/panik-liat-jam.png").exists()) {
            ayuPanikJam = new Texture("character/ekspresi_ayu/panik-liat-jam.png");
        } else {
            ayuPanikJam = ayuDiam;
        }

        if (Gdx.files.internal("character/ekspresi_ayu/syok-berat-ketakutan.png").exists()) {
            ayuSyok = new Texture("character/ekspresi_ayu/syok-berat-ketakutan.png");
        } else {
            ayuSyok = ayuDiam;
        }

        ayuKelelahanAnim = AnimationUtils.fromFiles("character/ekspresi_ayu/Kelelahan",
            "kelelahan_frame", ".png", 1, 8, 0.15f, Animation.PlayMode.LOOP);
        if (ayuKelelahanAnim.getKeyFrames().length == 0) {
            ayuKelelahanAnim = null;
        }

        if (Gdx.files.internal("character/ekspresi_ayu/pusing.png").exists()) {
            ayuPusing = new Texture("character/ekspresi_ayu/pusing.png");
        } else {
            ayuPusing = ayuDiam;
        }

        if (Gdx.files.internal("character/ekspresi_ayu/pingsan.png").exists()) {
            ayuPingsan = new Texture("character/ekspresi_ayu/pingsan.png");
        } else {
            ayuPingsan = ayuDiam;
        }

        if (Gdx.files.internal("character/ekspresi_ayu/mohon.png").exists()) {
            ayuMohon = new Texture("character/ekspresi_ayu/mohon.png");
        } else {
            ayuMohon = ayuDiam;
        }

        if (Gdx.files.internal("character/ekspresi_ayu/hadap-kiri.png").exists()) {
            ayuHadapKiri = new Texture("character/ekspresi_ayu/hadap-kiri.png");
        } else {
            ayuHadapKiri = ayuDiam;
        }
        if (Gdx.files.internal("character/Ayu/Pakaian-tidur/pakaian-tidur.png").exists()) {
            ayuPakaianTidur = new Texture("character/Ayu/Pakaian-tidur/pakaian-tidur.png");
        } else {
            ayuPakaianTidur = ayuDiam;
        }

        showerMusic = loadMusicSafely("SFX/shower.mp3");

        if (Gdx.files.internal("character/Ayu/selesai_presentasi_kiri.png").exists()) {
            ayuSelesaiPresentasi = new Texture("character/Ayu/selesai_presentasi_kiri.png");
        } else {
            ayuSelesaiPresentasi = ayuPusing;
        }

        // UI Textbox
        if (Gdx.files.internal("ui/textbox.png").exists()) {
            textbox = new Texture("ui/textbox.png");
        } else {
            textbox = createSolidTexture(Color.NAVY, 1, 1);
        }

        // Support Characters
        if (Gdx.files.internal("character/ibu-ayu/diam.png").exists()) {
            ibuSprite = new Texture("character/ibu-ayu/diam.png");
        } else {
            ibuSprite = createSolidTexture(Color.CORAL, 64, 128);
        }

        if (Gdx.files.internal("character/Dosen/diam.png").exists()) {
            dosenSprite = new Texture("character/Dosen/diam.png");
        } else {
            dosenSprite = createSolidTexture(Color.TEAL, 64, 128);
        }

        if (Gdx.files.internal("character/Teman/diam.png").exists()) {
            temanSprite = new Texture("character/Teman/diam.png");
        } else {
            temanSprite = createSolidTexture(Color.LIGHT_GRAY, 64, 128);
        }

        blackTexture = createSolidTexture(Color.BLACK, 1, 1);
        whiteTexture = createSolidTexture(Color.WHITE, 1, 1);

        // Populate Dialog Arrays with Specific Ayu Expressions
        drivingDialogs = new DialogInfo[] {
                new DialogInfo("Ayu", "Aduh, jam berapa ini?! Jalanan macet banget lagi!", ayuSyok),
                new DialogInfo("Ayu", "Kenapa semua orang harus keluar jam segini sih?!", ayuSyok),
                new DialogInfo("Ayu", "Aku gak boleh telat! Sidang ini sangat penting!", ayuSyok)
        };

        classroomStartDialogs = new DialogInfo[] {
                new DialogInfo("Ayu", "Hah... hah... Akhirnya sampai kelas..."),
                new DialogInfo("Dosen", "Ayu? Kamu terlihat sangat pucat dan kehabisan napas."),
                new DialogInfo("Dosen", "Kamu tidak apa-apa? Sebaiknya kamu istirahat dulu di UKS."),
                new DialogInfo("Ayu", "Tidak perlu, Pak! Saya tidak mau menunda lagi!", ayuDiam),
                new DialogInfo("Ayu", "Tolong dengarkan presentasi project saya sekarang juga!", ayuDiam),
                new DialogInfo("Dosen", "Tapi kondisi fisikmu tidak mendukung—"),
                new DialogInfo("Ayu", "Saya mohon, Pak! Mulai sidangnya sekarang!", ayuMohon)
        };

        classroomDialogs = new DialogInfo[] {
                new DialogInfo("Ayu", "...Dan demikianlah presentasi project akhir saya.", ayuSelesaiPresentasi),
                new DialogInfo("Dosen", "Penjelasan yang cukup baik, Ayu. Tapi bagian awal penjelasanmu masih kurang jelas."),
                new DialogInfo("Dosen", "Kamu terlihat sangat pucat. Apakah kamu ingin istirahat dulu?"),
                new DialogInfo("Ayu", "Tidak perlu, Pak... Saya... saya baik-baik saja...", ayuPusing),
                new DialogInfo("Teman", "Yu, wajahmu pucat banget. Jangan dipaksain!"),
                new DialogInfo("Ayu", "Aku... aku harus menyelesaikan ini...", ayuPusing),
                new DialogInfo("Ayu", "Kenapa tiba-tiba semuanya berputar...?", ayuPusing)
        };

        // Load walk animations
        Array<TextureRegion> rightFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            String rightPath = "character/Ayu/jalan_kanan_" + i + ".png";
            if (Gdx.files.internal(rightPath).exists())
                rightFrames.add(new TextureRegion(new Texture(rightPath)));
        }
        if (rightFrames.size > 0) {
            walkRightAnim = new Animation<>(0.1f, rightFrames, Animation.PlayMode.LOOP);
        }

        Array<TextureRegion> leftFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            String leftPath = "character/Ayu/jalan_kiri_" + i + ".png";
            if (Gdx.files.internal(leftPath).exists())
                leftFrames.add(new TextureRegion(new Texture(leftPath)));
        }
        if (leftFrames.size > 0) {
            walkLeftAnim = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);
        }

        messageSfx = loadSoundSafely("SFX/message_popup.mp3");
        doorSfx = loadSoundSafely("SFX/door_open.mp3");
        carHonkSfx = loadSoundSafely("SFX/car_honk.mp3");
        tireSkidSfx = loadSoundSafely("SFX/tire_skid.mp3");
        carCrashSfx = loadSoundSafely("SFX/car_crash.mp3");
        heartbeatSfx = loadSoundSafely("SFX/heartbeat.mp3");
        ayuShockSfx = loadSoundSafely("SFX/ayu_shock.mp3");
        ringEarsSfx = loadSoundSafely("SFX/ring_ears.mp3");
        heavyBreathingSfx = loadSoundSafely("SFX/heavy_breathing.wav");
        crowdChatterSfx = loadSoundSafely("SFX/crowd_chatter_muffled.mp3");
        footstepSfx = loadSoundSafely("SFX/footstep.mp3");
        showerSfx = loadSoundSafely("SFX/shower.mp3");

        classroomBgm = loadMusicSafely("SFX/calm_classroom_ambient.mp3");
        if (classroomBgm != null) {
            classroomBgm.setLooping(true);
            classroomBgm.setVolume(0.4f);
        }

        bodyFallSfx = loadSoundSafely("SFX/body_fall.mp3");
        dramaticBoomSfx = loadSoundSafely("SFX/dramatic_cinematic_boom.mp3");

        String bgmPath = "SFX/bgm_kamar.mp3";
        if (!Gdx.files.internal(bgmPath).exists()) {
            bgmPath = "SFX/[Non Copyrighted Music] Scott Buckley - Growing Up [Piano].mp3";
        }
        bgmMusic = loadMusicSafely(bgmPath);
        if (bgmMusic != null) {
            bgmMusic.setLooping(true);
            bgmMusic.setVolume(0.28f);
        }
    }

    private Sound loadSoundSafely(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newSound(Gdx.files.internal(path));
            }
        } catch (Throwable t) {
            System.err.println("Gagal memuat SFX [" + path + "]: " + t.getMessage());
        }
        return null;
    }

    private Music loadMusicSafely(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return Gdx.audio.newMusic(Gdx.files.internal(path));
            }
        } catch (Throwable t) {
            System.err.println("Gagal memuat BGM [" + path + "]: " + t.getMessage());
        }
        return null;
    }

    @Override
    public void render(float delta) {
        stateTimer += delta;

        // Clear Screen
        if (currentState == State.PLAY_KITCHEN || currentState == State.PLAY_GARDEN
                || currentState == State.PLAY_LOBBY) {
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        } else {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Camera positioning logic
        if (currentState != State.TITLE_CARD && currentState != State.FAINT_SEQUENCE) {
            camera.zoom = 1.0f;
            float halfViewportWidth = (camera.viewportWidth * camera.zoom) / 2f;
            float halfViewportHeight = (camera.viewportHeight * camera.zoom) / 2f;

            float targetCamX = ayuX + 99f;
            float targetCamY = ayuY + 167f;

            targetCamX = MathUtils.clamp(targetCamX, halfViewportWidth, WORLD_WIDTH - halfViewportWidth);
            targetCamY = MathUtils.clamp(targetCamY, halfViewportHeight, WORLD_HEIGHT - halfViewportHeight);

            camera.position.set(targetCamX, targetCamY, 0);
        } else {
            camera.zoom = 1.0f;
            camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        }

        if (drivingShakeTimer > 0f) {
            drivingShakeTimer -= delta;
            float shakeIntensity = 15f; // Goncangan panik menyetir ugal-ugalan
            camera.position.add(MathUtils.random(-shakeIntensity, shakeIntensity),
                    MathUtils.random(-shakeIntensity, shakeIntensity), 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Debug Shortcut: F4 ke Scene 3, F5 ke Scene 5, F6 ke Chapter 2
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            currentState = State.PLAY_DRIVING;
            ayuX = 100f;
            ayuY = 80f;
            facingRight = true;
            showMobilMerah = true;
            mobilMerahX = WORLD_WIDTH + 200f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            game.setScreen(new com.yogadhananjaya.beforetheend.screens.Chapter_Two.Chapter_Two_OneScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            currentState = State.PLAY_LOBBY;
            ayuX = 100f;
            ayuY = 80f;
            facingRight = true;
        }

        // Update State Logic
        updateState(delta);

        // Render
        batch.begin();

        // Backgrounds
        if (currentState != State.TITLE_CARD) {
            useInitialBg = false;
            if (currentState == State.WAKING_UP) {
                useInitialBg = true;
            } else if (currentState == State.DIALOG_ROOM) {
                if (!midRoomDialogTriggered) {
                    // All initial dialogs: stay on initial bg, no character
                    useInitialBg = true;
                } else {
                    // Mid-room: hide character until "Jam Tujuh" (index 3)
                    if (currentDialogIndex < 3) {
                        useInitialBg = true;
                    }
                }
            }

            if (currentState == State.WAKING_UP || currentState == State.DIALOG_ROOM || currentState == State.PLAY_ROOM) {
                if (useInitialBg) {
                    batch.draw(roomBgInitial, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                } else {
                    batch.draw(roomBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                }
            } else if (currentState == State.DIALOG_KITCHEN || currentState == State.PLAY_KITCHEN ||
                    currentState == State.KITCHEN_BATH_TRANSITION ||
                    currentState == State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED ||
                    currentState == State.DIALOG_KITCHEN_RIGHT_DOOR || currentState == State.DIALOG_KITCHEN_LEFT_DOOR ||
                    currentState == State.KITCHEN_TO_DRIVING_TRANSITION) {
                batch.draw(kitchenBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                // Draw Ibu static di Dapur
                batch.draw(ibuSprite, 1400f, 100f, 198f * 1.08f, 334f * 1.08f);
            } else if (currentState == State.PLAY_DRIVING || currentState == State.DRIVING_QTE
                    || currentState == State.DIALOG_DRIVING
                    || currentState == State.DRIVING_TO_GARDEN_TRANSITION) {
                batch.draw(scene3Bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                // Mobil Merah or rusak
                if (showMobilRusak && mobilRusak != null) {
                    batch.draw(mobilRusak, 1400f, 100f, 500f, 375f);
                } else if (showMobilMerah && mobilMerah != null) {
                    float x = mobilMerahX - 500f;
                    float y = ayuY * 0.54f;
                    batch.draw(mobilMerah, x, y, 500f, 375f);
                }
                // Siluet mobil shadow pass
                if (showSiluetMobil && siluetMobil != null) {
                    batch.draw(siluetMobil, siluetMobilY * 0.5f, siluetMobilY, 500f, 375f);
                }
                // Kesakitan character on QTE fail
                if (showKesakitan && kesakitan != null) {
                    batch.draw(kesakitan, ayuX, ayuY, 198f * 1.08f, 334f * 1.08f);
                }
            } else if (currentState == State.PLAY_GARDEN || currentState == State.GARDEN_TO_LOBBY_TRANSITION) {
                batch.draw(scene4Bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            } else if (currentState == State.PLAY_LOBBY || currentState == State.LOBBY_TO_CLASS_TRANSITION) {
                batch.draw(scene5Bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            } else if (currentState == State.SCENE6_CLASSROOM || currentState == State.FAINT_SEQUENCE
                    || currentState == State.PLAY_CLASSROOM || currentState == State.DIALOG_CLASSROOM_START) {
                batch.draw(scene6Bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            }

            // Draw Ayu
            if (!useInitialBg &&
                    currentState != State.WAKING_UP &&
                    currentState != State.KITCHEN_BATH_TRANSITION &&
                    currentState != State.ROOM_TO_KITCHEN_TRANSITION &&
                    currentState != State.KITCHEN_TO_DRIVING_TRANSITION &&
                    currentState != State.DRIVING_QTE &&
                    currentState != State.DRIVING_TO_GARDEN_TRANSITION &&
                    currentState != State.GARDEN_TO_LOBBY_TRANSITION &&
                    currentState != State.LOBBY_TO_CLASS_TRANSITION &&
                    currentState != State.FAINT_SEQUENCE) {
                drawCharacter(delta);
            }
        }

        // Overlay screens
        if (currentState == State.WAKING_UP) {
            batch.setColor(1, 1, 1, wakeFadeAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        } else if (currentState == State.DIALOG_ROOM || currentState == State.DIALOG_KITCHEN ||
                currentState == State.DIALOG_KITCHEN_RIGHT_DOOR || currentState == State.DIALOG_KITCHEN_LEFT_DOOR ||
                currentState == State.SCENE6_CLASSROOM || currentState == State.DIALOG_DRIVING
                || currentState == State.DIALOG_CLASSROOM_START) {
            renderDialogBox(delta);
        } else if (currentState == State.ROOM_TO_KITCHEN_TRANSITION) {
            if (transitionTimer < 1.0f) {
                batch.draw(roomBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                float alpha = transitionTimer / 1.0f;
                batch.setColor(0, 0, 0, alpha);
                batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                batch.setColor(Color.WHITE);
            } else {
                batch.draw(kitchenBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                batch.draw(ibuSprite, 1400f, 100f, 198f * 1.08f, 334f * 1.08f);
                float alpha = 1.0f - ((transitionTimer - 1.0f) / 1.0f);
                batch.setColor(0, 0, 0, alpha);
                batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                batch.setColor(Color.WHITE);
            }
        } else if (currentState == State.KITCHEN_BATH_TRANSITION
                || currentState == State.KITCHEN_TO_DRIVING_TRANSITION ||
                currentState == State.DRIVING_TO_GARDEN_TRANSITION || currentState == State.GARDEN_TO_LOBBY_TRANSITION
                ||
                currentState == State.LOBBY_TO_CLASS_TRANSITION) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else if (currentState == State.DRIVING_QTE) {
            if (redFlashAlpha > 0f) {
                batch.setColor(1f, 0f, 0f, redFlashAlpha);
                batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                batch.setColor(Color.WHITE);
                redFlashAlpha -= Gdx.graphics.getDeltaTime() * 1.5f;
            }
            if (whiteFlashAlpha > 0f) {
                batch.setColor(1f, 1f, 1f, whiteFlashAlpha);
                batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                batch.setColor(Color.WHITE);
            }
            renderDrivingQTE();
        } else if (currentState == State.PLAY_ROOM || currentState == State.PLAY_KITCHEN ||
                currentState == State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED ||
                currentState == State.PLAY_GARDEN || currentState == State.PLAY_LOBBY
                || currentState == State.PLAY_CLASSROOM) {
            renderInteractionHints();
        } else if (currentState == State.FAINT_SEQUENCE) {
            // Pandangan menggelap perlahan
            float alpha = Math.min(1.0f, faintTimer / 3.0f);
            batch.setColor(0, 0, 0, alpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        } else if (currentState == State.TITLE_CARD) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            font.getData().setScale(4f);
            font.setColor(1f, 0.84f, 0f, titleAlpha); // Gold color
            glyphLayout.setText(font, "BEFORE THE END");
            font.draw(batch, "BEFORE THE END", (WORLD_WIDTH - glyphLayout.width) / 2f,
                    (WORLD_HEIGHT + glyphLayout.height) / 2f);
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }

        batch.end();
    }

    private void updateState(float delta) {
        switch (currentState) {
            case WAKING_UP:
                wakeFadeAlpha = 1f - (stateTimer / 2.0f);
                if (wakeFadeAlpha <= 0f) {
                    wakeFadeAlpha = 0f;
                    currentState = State.DIALOG_ROOM;
                    stateTimer = 0f;
                    setupRoomDialog();
                    if (bgmMusic != null && !bgmMusic.isPlaying()) {
                        bgmMusic.setLooping(true);
                        bgmMusic.setVolume(0.22f); // Diperkecil 30% dari 0.32f
                        bgmMusic.play();
                    }
                }
                break;

            case DIALOG_ROOM:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    advanceDialog(State.PLAY_ROOM);
                }
                break;

            case PLAY_ROOM:
                handlePlayerMovement(delta);

                // Pemicu dialog Ibu di 70% map
                if (!midRoomDialogTriggered && ayuX <= WORLD_WIDTH * 0.7f) {
                    midRoomDialogTriggered = true;
                    currentDialogs.clear();
                    currentDialogs.add(new DialogInfo("IBU AYU", "Ayu, turun yuk sarapan!"));
                    currentDialogs.add(new DialogInfo("Ayu", "Iya, Bentar Lagi bu", ayuDiam));
                    currentDialogs.add(new DialogInfo("NARRATOR",
                            "Waktu terus berdentang. Ayu tidak sadar, jam sudah menunjukkan pukul 7 tepat."));
                    currentDialogs.add(new DialogInfo("Ayu",
                            "Jam tujuh?! Sial, sidang jam setengah delapan!, Aku Harus Segara Mandi", ayuPanikJam));
                    currentDialogIndex = 0;
                    startDialog();
                    currentState = State.DIALOG_ROOM;
                    break;
                }

                if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    if (doorSfx != null)
                        doorSfx.play(1.0f);
                    currentState = State.ROOM_TO_KITCHEN_TRANSITION;
                    transitionTimer = 0f;
                }
                break;

            case ROOM_TO_KITCHEN_TRANSITION:
                transitionTimer += delta;
                if (transitionTimer >= 2.0f) {
                    currentState = State.DIALOG_KITCHEN;
                    ayuX = 100f;
                    ayuY = 80f;
                    facingRight = true;
                    setupKitchenDialog();
                }
                break;

            case DIALOG_KITCHEN:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    advanceDialog(sudahMandi ? State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED : State.PLAY_KITCHEN);
                }
                break;

            case PLAY_KITCHEN:
                handlePlayerMovement(delta);
                if (!sudahSarapan) {
                    if (ayuX >= 1100f && ayuX <= 1300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        sudahSarapan = true;
                        currentDialogs.clear();
                        currentDialogs.add(new DialogInfo("Ayu", "Nyam nyam... makan Apel cepat selesai!"));
                        currentDialogs.add(new DialogInfo("Ayu",
                                "Sekarang aku harus segera mandi! Pintu Kamar Mandi ada di sebelah Kanan"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    } else if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentDialogs.clear();
                        currentDialogs
                                .add(new DialogInfo("Ayu", "Aku harus sarapan dulu biar punya energi untuk sidang!"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    }
                } else if (!sudahMandi) {
                    if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentState = State.KITCHEN_BATH_TRANSITION;
                        transitionTimer = 0f;
                        if (doorSfx != null)
                            doorSfx.play(1.0f);
                    }
                } else {
                    if (ayuX <= 60f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        leftDoorChecked = true;
                        currentState = State.DIALOG_KITCHEN_LEFT_DOOR;
                        setupLeftDoorDialog();
                    } else if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentDialogs.clear();
                        currentDialogs.add(new DialogInfo("Ayu",
                                "Aku sudah mandi bersih. Sekarang saatnya berangkat lewat pintu keluar kiri mentok!"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    }
                }
                break;

            case KITCHEN_BATH_TRANSITION:
                if (transitionTimer == 0f && showerSfx != null) showerSfx.loop(0.5f);
                transitionTimer += delta;
                if (transitionTimer >= 3.0f) {
                    if (showerSfx != null) showerSfx.stop();
                    if (showerMusic != null) {
                        showerMusic.stop();
                    }
                    sudahMandi = true;
                    rightDoorChecked = true;
                    facingRight = false;
                    currentDialogs.clear();
                    currentDialogs.add(new DialogInfo("Ayu", "Aku Sudah selesai mandi"));
                    currentDialogs.add(new DialogInfo("Ayu",
                            "Aku harus segera ke kampus! Pintu keluarnya ada di sebelah KIRI mentok."));
                    currentDialogIndex = 0;
                    startDialog();
                    currentState = State.DIALOG_KITCHEN;
                }
                break;

            case DIALOG_KITCHEN_RIGHT_DOOR:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    advanceDialog(State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED);
                }
                break;

            case PLAY_KITCHEN_RIGHT_DOOR_CHECKED:
                handlePlayerMovement(delta);
                if (!sudahSarapan) {
                    if (ayuX >= 1100f && ayuX <= 1300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        sudahSarapan = true;
                        currentDialogs.clear();
                        currentDialogs.add(new DialogInfo("Ayu", "Nyam nyam... makan Apel cepat selesai!"));
                        currentDialogs.add(new DialogInfo("Ayu",
                                "Sekarang aku harus segera mandi! Pintu Kamar Mandi ada di sebelah Kanan"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    } else if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentDialogs.clear();
                        currentDialogs
                                .add(new DialogInfo("Ayu", "Aku harus sarapan dulu biar punya energi untuk sidang!"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    }
                } else if (!sudahMandi) {
                    if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentState = State.KITCHEN_BATH_TRANSITION;
                        transitionTimer = 0f;
                        if (doorSfx != null)
                            doorSfx.play(1.0f);
                        if (showerMusic != null) {
                            showerMusic.setLooping(true);
                            showerMusic.play();
                        }
                    }
                } else {
                    if (ayuX <= 60f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        leftDoorChecked = true;
                        currentState = State.DIALOG_KITCHEN_LEFT_DOOR;
                        setupLeftDoorDialog();
                    } else if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        currentDialogs.clear();
                        currentDialogs.add(new DialogInfo("Ayu",
                                "Aku sudah mandi bersih. Sekarang saatnya berangkat lewat pintu keluar kiri mentok!"));
                        currentDialogIndex = 0;
                        startDialog();
                        currentState = State.DIALOG_KITCHEN;
                    }
                }
                break;

            case DIALOG_KITCHEN_LEFT_DOOR:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    advanceDialog(State.KITCHEN_TO_DRIVING_TRANSITION);
                    transitionTimer = 0f;
                }
                break;

            case KITCHEN_TO_DRIVING_TRANSITION:
                if (transitionTimer == 0f) {
                    if (doorSfx != null)
                        doorSfx.play(1.0f);
                }
                transitionTimer += delta;
                if (transitionTimer >= 2.0f) {
                    currentState = State.PLAY_DRIVING;
                    ayuX = 100f;
                    ayuY = 80f;
                    facingRight = true;
                    showMobilMerah = true;
                    mobilMerahX = WORLD_WIDTH + 200f;
                    honkPlayed = false;
                    skidPlayed = false;
                    crashPlayed = false;
                }
                break;

            case PLAY_DRIVING:
                if (bgmMusic != null && !bgmMusic.isPlaying() && currentState != State.DIALOG_DRIVING) {
                    bgmMusic.play();
                }
                // Auto-walk both toward each other
                ayuX += delta * 200f;
                isMoving = true;
                facingRight = true;
                if (ayuX > WORLD_WIDTH - 300f)
                    ayuX = WORLD_WIDTH - 300f;
                if (showMobilMerah) {
                    mobilMerahX -= delta * 300f;
                    // Klakson mobil berbunyi keras sebagai peringatan awal
                    if (!honkPlayed && mobilMerahX <= WORLD_WIDTH - 300f) {
                        honkPlayed = true;
                        if (carHonkSfx != null)
                            carHonkSfx.play(1.0f);
                    }
                    if (mobilMerahX - 200f <= ayuX) {
                        redFlashAlpha = 0.4f;
                        currentState = State.DRIVING_QTE;
                        qteTimer = 0f;
                        qteFinished = false;
                        qteSuccess = false;
                        if (bgmMusic != null && bgmMusic.isPlaying()) {
                            bgmMusic.stop();
                        }
                        if (heartbeatSfx != null)
                            heartbeatSfx.play(0.8f);
                    }
                }
                break;

            case DRIVING_QTE:
                qteTimer += delta;
                if (!qteFinished) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                        qteSuccess = true;
                        qteFinished = true;
                        drivingShakeTimer = 0.5f;
                        if (!skidPlayed && tireSkidSfx != null) {
                            skidPlayed = true;
                            tireSkidSfx.play(1.0f);
                        }
                        if (!ayuShockPlayed && ayuShockSfx != null) {
                            ayuShockPlayed = true;
                            ayuShockSfx.play(1.0f);
                        }
                    } else if (qteTimer >= 2.0f) {
                        qteSuccess = false;
                        qteFinished = true;
                        drivingShakeTimer = 1.5f;
                        if (!crashPlayed && carCrashSfx != null) {
                            crashPlayed = true;
                            carCrashSfx.play(1.0f);
                        }
                        if (!ayuShockPlayed && ayuShockSfx != null) {
                            ayuShockPlayed = true;
                            ayuShockSfx.play(1.0f);
                        }
                    }
                } else if (qteSuccess) {
                    if (drivingShakeTimer <= 0f) {
                        showMobilMerah = false;
                        currentState = State.DIALOG_DRIVING;
                        setupDrivingDialog();
                    }
                } else {
                    // QTE failed: impact sequence
                    drivingShakeTimer -= delta;
                    if (!impactStarted) {
                        impactStarted = true;
                        impactTimer = 0f;
                        whiteFlashAlpha = 0.8f;
                        if (!crashPlayed && carCrashSfx != null) {
                            crashPlayed = true;
                            carCrashSfx.play(1.0f);
                        }
                    }
                    impactTimer += delta;
                    // White flash fade
                    if (whiteFlashAlpha > 0f) {
                        whiteFlashAlpha -= delta * 3f;
                        if (whiteFlashAlpha < 0f)
                            whiteFlashAlpha = 0f;
                    }
                    // Shadow pass after flash
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
                    // End impact
                    if (impactTimer >= 3.0f) {
                        showKesakitan = false;
                        showMobilRusak = false;
                        impactStarted = false;
                        currentState = State.DIALOG_DRIVING;
                        setupDrivingDialog();
                    }
                }
                break;

            case DIALOG_DRIVING:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    advanceDialog(State.DRIVING_TO_GARDEN_TRANSITION);
                    transitionTimer = 0f;
                }
                break;

            case DRIVING_TO_GARDEN_TRANSITION:
                transitionTimer += delta;
                if (transitionTimer >= 2.0f) {
                    currentState = State.PLAY_GARDEN;
                    ayuX = 100f;
                    ayuY = 80f;
                    facingRight = true;
                }
                break;

            case PLAY_GARDEN:
                if (!gardenAudioPlayed) {
                    gardenAudioPlayed = true;
                    if (ringEarsSfx != null)
                        ringEarsId = ringEarsSfx.play(0.6f);
                    if (heavyBreathingSfx != null)
                        heavyBreathingId = heavyBreathingSfx.play(0.5f);
                }
                gardenRingingTimer += delta;
                if (gardenRingingTimer >= 2.0f && !ringEarsStopped) {
                    ringEarsStopped = true;
                    if (ringEarsSfx != null && ringEarsId != -1) {
                        ringEarsSfx.stop(ringEarsId);
                    }
                }
                handlePlayerMovement(delta);
                // Mentok kanan -> langsung pindah ke Scene 5 (Lobby/Lift)
                if (ayuX >= WORLD_WIDTH - 300f) {
                    if (ringEarsSfx != null && ringEarsId != -1)
                        ringEarsSfx.stop(ringEarsId);
                    if (heavyBreathingSfx != null && heavyBreathingId != -1)
                        heavyBreathingSfx.stop(heavyBreathingId);
                    currentState = State.GARDEN_TO_LOBBY_TRANSITION;
                    transitionTimer = 0f;
                }
                break;

            case GARDEN_TO_LOBBY_TRANSITION:
                transitionTimer += delta;
                if (transitionTimer >= 1.5f) {
                    currentState = State.PLAY_LOBBY;
                    ayuX = 100f;
                    facingRight = true;
                }
                break;

            case PLAY_LOBBY:
                handlePlayerMovement(delta);
                // Tangga berada di ujung kanan -> tekan E untuk naik/masuk kelas
                if (ayuX >= WORLD_WIDTH - 400f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    currentState = State.LOBBY_TO_CLASS_TRANSITION;
                    transitionTimer = 0f;
                }
                break;

            case LOBBY_TO_CLASS_TRANSITION:
                transitionTimer += delta;
                if (transitionTimer >= 2.0f) {
                    currentState = State.PLAY_CLASSROOM;
                    ayuX = 100f; // Di dekat pintu masuk kelas
                    ayuY = 80f;
                    if (classroomBgm != null && !classroomBgm.isPlaying()) {
                        classroomBgm.play();
                    }
                    if (crowdChatterSfx != null) {
                        crowdChatterId = crowdChatterSfx.loop(0.28f);
                    }
                }
                break;

            case PLAY_CLASSROOM:
                // Gerak Ayu melambat (sakit/pusing/ngos-ngosan)
                isMoving = false;
                float speed = AYU_SPEED * 0.4f;
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                    ayuX -= speed * delta;
                    facingRight = false;
                    isMoving = true;
                } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    ayuX += speed * delta;
                    facingRight = true;
                    isMoving = true;
                }

                if (ayuX < 50f)
                    ayuX = 50f;
                if (ayuX > WORLD_WIDTH - 300f)
                    ayuX = WORLD_WIDTH - 300f;

                // Mendekati meja dosen penguji
                if (ayuX >= 300f) {
                    currentDialogs.clear();
                    for (DialogInfo d : classroomStartDialogs) {
                        currentDialogs.add(d);
                    }
                    currentDialogIndex = 0;
                    startDialog();
                    currentState = State.DIALOG_CLASSROOM_START;
                }
                break;

            case DIALOG_CLASSROOM_START:
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    if (charIndex < currentDialogs.get(currentDialogIndex).text.length()) {
                        displayedDialogText = currentDialogs.get(currentDialogIndex).text;
                        charIndex = displayedDialogText.length();
                    } else {
                        currentDialogIndex++;
                        if (currentDialogIndex >= currentDialogs.size()) {
                            currentState = State.SCENE6_CLASSROOM;
                            if (crowdChatterSfx != null && crowdChatterId != -1) {
                                crowdChatterSfx.stop(crowdChatterId);
                            }
                            isMoving = false;
                            ayuX = 1500f; // Berdiri di posisi presentasi (X: 1000f)
                            ayuY = 80f;
                            classroomStep = 0;
                            classroomTimer = 0f;
                            setupClassroomDialog();
                        } else {
                            startDialog();
                        }
                    }
                }
                break;

            case SCENE6_CLASSROOM:
                isMoving = false;
                updateDialogText(delta);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.justTouched()) {
                    if (charIndex < currentDialogs.get(currentDialogIndex).text.length()) {
                        displayedDialogText = currentDialogs.get(currentDialogIndex).text;
                        charIndex = displayedDialogText.length();
                    } else {
                        currentDialogIndex++;
                        if (currentDialogIndex >= currentDialogs.size()) {
                            currentState = State.FAINT_SEQUENCE;
                            faintTimer = 0f;
                        } else {
                            startDialog();
                        }
                    }
                }
                break;

            case FAINT_SEQUENCE:
                if (classroomBgm != null && classroomBgm.isPlaying()) {
                    classroomBgm.stop();
                }
                if (!bodyFallPlayed) {
                    bodyFallPlayed = true;
                    if (bodyFallSfx != null)
                        bodyFallSfx.play(1.0f);
                    if (heartbeatSfx != null)
                        heartbeatSfx.play(0.8f);
                }
                faintTimer += delta;
                if (faintTimer >= 4.0f) {
                    currentState = State.TITLE_CARD;
                    titleAlpha = 0f;
                    stateTimer = 0f;
                }
                break;

            case TITLE_CARD:
                if (!dramaticBoomPlayed) {
                    dramaticBoomPlayed = true;
                    if (dramaticBoomSfx != null)
                        dramaticBoomSfx.play(1.0f);
                }
                if (stateTimer < 3.0f) {
                    titleAlpha = stateTimer / 3.0f;
                } else if (stateTimer >= 6.0f) {
                    // Berpindah ke Chapter 2 Screen setelah judul game selesai ditampilkan
                    game.setScreen(new com.yogadhananjaya.beforetheend.screens.Chapter_Two.Chapter_Two_OneScreen(game));
                }
                break;
        }
    }

    private void handlePlayerMovement(float delta) {
        isMoving = false;
        if (currentDialogIndex >= 0 && currentDialogIndex < currentDialogs.size()) {
            return;
        }
        float speed = AYU_SPEED;
        if ((currentState == State.PLAY_DRIVING || currentState == State.PLAY_GARDEN
                || currentState == State.PLAY_LOBBY)
                && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            speed = AYU_SPEED * 2.0f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            ayuX -= speed * delta;
            facingRight = false;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            ayuX += speed * delta;
            facingRight = true;
            isMoving = true;
        }

        // Bounding Ayu
        if (ayuX < 50f)
            ayuX = 50f;
        if (ayuX > WORLD_WIDTH - 300f)
            ayuX = WORLD_WIDTH - 300f;
    }

    private void setupRoomDialog() {
        currentDialogs.clear();
        currentDialogs.add(
                new DialogInfo("NARRATOR", "Senin pagi. Seperti biasa, kamar Ayu tidak pernah benar-benar gelap."));
        currentDialogs.add(new DialogInfo("Ayu", "Sedikit lagi... tinggal bagian ini yang belum aku benerin.", ayuDiam));
        currentDialogs.add(new DialogInfo("NARRATOR",
                "Ini sudah hari kesekian Ayu terjaga semalaman, hanya demi satu projek yang menurutnya belum juga sempurna."));
        currentDialogIndex = 0;
        startDialog();
    }

    private void setupKitchenDialog() {
        currentDialogs.clear();
        currentDialogs.add(new DialogInfo("NARRATOR", "Ayu bergegas turun ke dapur."));
        currentDialogs.add(new DialogInfo("IBU AYU", "Eh Ayu, sudah bangun. Itu Ibu sudah siapkan Apel di meja."));
        currentDialogs.add(new DialogInfo("Ayu", "Iya, Bu! Aku sarapan cepat dulu ya, soalnya buru-buru.", ayuDiam));
        currentDialogIndex = 0;
        startDialog();
    }

    private void setupRightDoorDialog() {
        currentDialogs.clear();
        currentDialogs.add(new DialogInfo("Ayu", "*(Suara air mengucur cepat dari kamar mandi)*", ayuDiam));
        currentDialogs.add(new DialogInfo("Ayu", "Wah, segar sekali! Sekarang badanku sudah bersih.", ayuDiam));
        currentDialogs.add(new DialogInfo("Ayu", "Ayo segera keluar lewat pintu kiri mentok untuk berangkat kuliah!", ayuDiam));
        currentDialogIndex = 0;
        startDialog();
    }

    private void setupLeftDoorDialog() {
        currentDialogs.clear();
        currentDialogs.add(new DialogInfo("Ayu", "Ayo cepat berangkat sebelum jalanan semakin macet!", ayuHadapKiri));
        currentDialogIndex = 0;
        startDialog();
    }

    private void setupDrivingDialog() {
        currentDialogs.clear();
        if (qteSuccess) {
            currentDialogs.add(new DialogInfo("Ayu", "Fuuuh... Hampir saja! Untung refleksku cepat!", ayuSyok));
            currentDialogs.add(new DialogInfo("Ayu", "Jalanan macet dan semua orang menyetir ugal-ugalan!", ayuSyok));
            currentDialogs.add(new DialogInfo("Ayu", "Aku harus tetap fokus dan ngebut biar tidak terlambat sidang!", ayuSyok));
        } else {
            currentDialogs.add(new DialogInfo("Ayu", "AAAKH!! Rem mendadak!!", ayuSyok));
            currentDialogs.add(
                    new DialogInfo("Ayu", "Kepalaku hampir membentur setir! Sialan, ugal-ugalan sekali mobil depan!", ayuSyok));
            currentDialogs
                    .add(new DialogInfo("Ayu", "Waktu sudah mepet, aku tidak punya pilihan selain terus tancap gas!", ayuSyok));
        }
        currentDialogIndex = 0;
        startDialog();
    }

    private void setupClassroomDialog() {
        currentDialogs.clear();
        for (DialogInfo d : classroomDialogs) {
            currentDialogs.add(d);
        }
        currentDialogIndex = 0;
        startDialog();
    }

    private void startDialog() {
        isMoving = false;
        displayedDialogText = "";
        charIndex = 0;
        typeTimer = 0f;
        if (messageSfx != null && currentSfxId != -1) {
            messageSfx.stop(currentSfxId);
            currentSfxId = -1;
        }
    }

    private void updateDialogText(float delta) {
        if (currentDialogIndex >= currentDialogs.size())
            return;
        String fullText = currentDialogs.get(currentDialogIndex).text;
        if (charIndex < fullText.length()) {
            if (!fullText.trim().isEmpty() && messageSfx != null && currentSfxId == -1) {
                currentSfxId = messageSfx.loop(1.0f);
            }
            typeTimer += delta;
            if (typeTimer >= typeSpeed) {
                typeTimer = 0f;
                displayedDialogText += fullText.charAt(charIndex);
                charIndex++;
            }
        } else {
            if (messageSfx != null && currentSfxId != -1) {
                messageSfx.stop(currentSfxId);
                currentSfxId = -1;
            }
        }
    }

    private void advanceDialog(State nextState) {
        if (currentDialogIndex >= currentDialogs.size())
            return;
        String fullText = currentDialogs.get(currentDialogIndex).text;
        if (messageSfx != null && currentSfxId != -1) {
            messageSfx.stop(currentSfxId);
            currentSfxId = -1;
        }
        if (charIndex < fullText.length()) {
            displayedDialogText = fullText;
            charIndex = fullText.length();
        } else {
            currentDialogIndex++;
            if (currentDialogIndex >= currentDialogs.size()) {
                currentState = nextState;
                stateTimer = 0f;
            } else {
                startDialog();
            }
        }
    }

    private void renderDialogBox(float delta) {
        if (currentDialogIndex >= currentDialogs.size())
            return;
        DialogInfo dialog = currentDialogs.get(currentDialogIndex);

        float drawWidth = 198f * 1.08f;
        float drawHeight = 422f;

        float bubbleWidth;
        float bubbleHeight;
        float bubbleX;
        float bubbleY;

        if ("NARRATOR".equalsIgnoreCase(dialog.speaker)) {
            bubbleWidth = 1000f;
            bubbleHeight = 150f;
            bubbleX = camera.position.x - (bubbleWidth / 2f);
            bubbleY = camera.position.y + (camera.viewportHeight / 2f) - bubbleHeight - 50f;
        } else if ("IBU AYU".equalsIgnoreCase(dialog.speaker)) {
            bubbleWidth = showingChoice ? 420f : 650f;
            bubbleHeight = showingChoice ? 200f : 180f;
            bubbleX = camera.position.x + camera.viewportWidth / 2f - bubbleWidth - 20f;
            bubbleY = (currentState == State.DIALOG_ROOM)
                    ? ayuY + drawHeight * 1.5f + 20f
                    : ayuY + drawHeight + 20f;
        } else if ("DOSEN".equalsIgnoreCase(dialog.speaker)) {
            bubbleWidth = 650f;
            bubbleHeight = 180f;
            bubbleX = 1000f;
            bubbleY = 700f;
        } else if ("TEMAN".equalsIgnoreCase(dialog.speaker) || "TEMAN AYU".equalsIgnoreCase(dialog.speaker)) {
            bubbleWidth = 650f;
            bubbleHeight = 180f;
            bubbleX = 960f - (bubbleWidth / 2f);
            bubbleY = 600f;
        } else {
            bubbleWidth = showingChoice ? 420f : 650f;
            bubbleHeight = showingChoice ? 200f : 180f;

            if (currentState == State.DIALOG_ROOM && !midRoomDialogTriggered) {
                // Di cutscene awal saat Ayu duduk di meja belajar (kanan)
                bubbleX = 1150f - (bubbleWidth / 2f);
                if (useInitialBg) {
                    bubbleY = 750f;
                } else {
                    bubbleY = ayuY + drawHeight - 40f;
                }
            } else {
                // Saat Ayu berjalan di map
                bubbleX = (ayuX + drawWidth / 2f) - (bubbleWidth / 2f);

                String txt = dialog.text;
                if (txt != null && (txt.contains("Iya, bentar lagi") || txt.contains("Iya, Bentar Lagi"))) {
                    bubbleY = 750f;
                } else {
                    bubbleY = ayuY + drawHeight + 20f;
                }
            }

            if (bubbleX < camera.position.x - camera.viewportWidth / 2f + 20f) {
                bubbleX = camera.position.x - camera.viewportWidth / 2f + 20f;
            }
            if (bubbleX + bubbleWidth > camera.position.x + camera.viewportWidth / 2f - 20f) {
                bubbleX = camera.position.x + camera.viewportWidth / 2f - 20f - bubbleWidth;
            }
        }

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
        if (!"NARRATOR".equalsIgnoreCase(dialog.speaker)) {
            font.getData().setScale(1.4f);
            font.setColor(Color.GOLD);
            font.draw(batch, dialog.speaker, bubbleX + 25f, bubbleY + bubbleHeight - 20f);
        }

        // Dialog text
        font.setColor(Color.WHITE);
        float textY = "NARRATOR".equalsIgnoreCase(dialog.speaker) ? (bubbleY + bubbleHeight - 35f)
                : (bubbleY + bubbleHeight - 60f);
        font.draw(batch, displayedDialogText, bubbleX + 25f, textY, bubbleWidth - 50f,
                com.badlogic.gdx.utils.Align.left, true);

        // Indicator arrow
        if (charIndex >= dialog.text.length()) {
            if ((int) (stateTimer * 2) % 2 == 0) {
                batch.setColor(Color.GOLD);
                float arrowSize = 12f;
                float arrowX = bubbleX + bubbleWidth - 30f;
                float arrowY = bubbleY + 20f;
                batch.draw(whiteTexture, arrowX - arrowSize / 2f, arrowY, arrowSize, borderThickness);
                batch.draw(whiteTexture, arrowX - arrowSize / 4f, arrowY - borderThickness, arrowSize / 2f,
                        borderThickness);
                batch.draw(whiteTexture, arrowX, arrowY - borderThickness * 2f, borderThickness, borderThickness);
                batch.setColor(Color.WHITE);
            }
        }
        font.getData().setScale(2f);
    }

    private void renderInteractionHints() {
        boolean showHint = false;
        String hintText = "";

        if (currentState == State.PLAY_ROOM) {
            if (ayuX >= WORLD_WIDTH - 300f) {
                showHint = true;
                hintText = "Tekan E untuk Keluar Kamar";
            }
        } else if (currentState == State.PLAY_KITCHEN) {
            if (ayuX >= 1100f && ayuX <= 1300f && !sudahSarapan) {
                showHint = true;
                hintText = "Tekan E untuk Sarapan Cepat";
            } else if (ayuX >= WORLD_WIDTH - 300f) {
                showHint = true;
                hintText = "Tekan E untuk Membuka Pintu Kanan";
            }
        } else if (currentState == State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED) {
            if (ayuX >= 1100f && ayuX <= 1300f && !sudahSarapan) {
                showHint = true;
                hintText = "Tekan E untuk Sarapan Cepat";
            } else if (ayuX <= 60f) {
                showHint = true;
                hintText = "Tekan E untuk Membuka Pintu Kiri";
            }
        } else if (currentState == State.PLAY_LOBBY && ayuX >= WORLD_WIDTH - 400f) {
            showHint = true;
            hintText = "Tekan E untuk Masuk Kelas";
        } else if (currentState == State.PLAY_CLASSROOM) {
            showHint = true;
            hintText = "Jalan menghampiri Dosen Penguji";
        }

        if (showHint) {
            float characterWidth = 198f * 1.08f;
            float characterHeight = 334f * 1.08f;
            float buttonWidth = 380f;
            float buttonHeight = 65f;

            float buttonX = (ayuX + characterWidth / 2f) - (buttonWidth / 2f);
            float buttonY = ayuY + characterHeight + 20f;

            if (buttonX < 20f)
                buttonX = 20f;
            if (buttonX + buttonWidth > WORLD_WIDTH - 20f)
                buttonX = WORLD_WIDTH - 20f - buttonWidth;

            batch.draw(blackTexture, buttonX, buttonY, buttonWidth, buttonHeight);

            batch.setColor(Color.GOLD);
            float thickness = 3f;
            batch.draw(whiteTexture, buttonX + 6f, buttonY, buttonWidth - 12f, thickness);
            batch.draw(whiteTexture, buttonX + 6f, buttonY + buttonHeight - thickness, buttonWidth - 12f, thickness);
            batch.draw(whiteTexture, buttonX, buttonY + 6f, thickness, buttonHeight - 12f);
            batch.draw(whiteTexture, buttonX + buttonWidth - thickness, buttonY + 6f, thickness, buttonHeight - 12f);
            batch.draw(whiteTexture, buttonX + 3f, buttonY + 3f, 3f, 3f);
            batch.draw(whiteTexture, buttonX + buttonWidth - 6f, buttonY + 3f, 3f, 3f);
            batch.draw(whiteTexture, buttonX + 3f, buttonY + buttonHeight - 6f, 3f, 3f);
            batch.draw(whiteTexture, buttonX + buttonWidth - 6f, buttonY + buttonHeight - 6f, 3f, 3f);
            batch.setColor(Color.WHITE);

            font.getData().setScale(1.1f);
            font.setColor(Color.WHITE);
            glyphLayout.setText(font, hintText);
            font.draw(batch, hintText, buttonX + (buttonWidth - glyphLayout.width) / 2f,
                    buttonY + (buttonHeight + glyphLayout.height) / 2f);
            font.getData().setScale(2f);

            batch.setColor(Color.GOLD);
            float triX = buttonX + buttonWidth / 2f;
            float triY = buttonY - 6f;
            batch.draw(whiteTexture, triX - 8f, triY + 4f, 16f, 2f);
            batch.draw(whiteTexture, triX - 6f, triY + 2f, 12f, 2f);
            batch.draw(whiteTexture, triX - 4f, triY, 8f, 2f);
            batch.draw(whiteTexture, triX - 2f, triY - 2f, 4f, 2f);
            batch.setColor(Color.WHITE);
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
        float drawHeight = 422f;
        float drawWidth = 198f * 1.08f;

        if (isMoving) {
            float speedMultiplier = 1.0f;
            if ((currentState == State.PLAY_DRIVING || currentState == State.PLAY_GARDEN)
                    && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                speedMultiplier = 1.8f;
            }
            walkTime += delta * speedMultiplier;
            TextureRegion currentFrame = null;
            if (facingRight && walkRightAnim != null) {
                currentFrame = walkRightAnim.getKeyFrame(walkTime);
            } else if (!facingRight && walkLeftAnim != null) {
                currentFrame = walkLeftAnim.getKeyFrame(walkTime);
            }

            if (currentFrame != null) {
                if (currentFrame.getRegionHeight() > 0) {
                    drawWidth = drawHeight * ((float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight());
                }
                batch.draw(currentFrame, ayuX, ayuY, drawWidth, drawHeight);
            } else {
                if (ayuIdle != null && ayuIdle.getHeight() > 0) {
                    drawWidth = drawHeight * ((float) ayuIdle.getWidth() / ayuIdle.getHeight());
                }
                batch.draw(ayuIdle, ayuX, ayuY, drawWidth, drawHeight);
            }
        } else {
            walkTime = 0f;
            Texture activeIdleTex = null;

            if (currentDialogIndex >= 0 && currentDialogIndex < currentDialogs.size()) {
                DialogInfo info = currentDialogs.get(currentDialogIndex);
                if (info.expression != null) {
                    activeIdleTex = info.expression;
                }
            }

            if (activeIdleTex == null) {
                if (currentState == State.WAKING_UP || currentState == State.DIALOG_ROOM || currentState == State.PLAY_ROOM
                        || currentState == State.DIALOG_KITCHEN || currentState == State.PLAY_KITCHEN
                        || currentState == State.PLAY_KITCHEN_RIGHT_DOOR_CHECKED || currentState == State.PLAY_KITCHEN_LEFT_DOOR_CHECKED
                        || currentState == State.DIALOG_KITCHEN_RIGHT_DOOR || currentState == State.DIALOG_KITCHEN_LEFT_DOOR) {
                    activeIdleTex = sudahMandi ? ayuHadapKiri : (ayuPakaianTidur != null ? ayuPakaianTidur : ayuDiam);
                } else if (currentState == State.PLAY_DRIVING || currentState == State.DRIVING_QTE || currentState == State.DIALOG_DRIVING) {
                    activeIdleTex = (ayuSyok != null) ? ayuSyok : ayuDiam;
                } else if (currentState == State.PLAY_GARDEN || currentState == State.PLAY_LOBBY) {
                    activeIdleTex = ayuDiam; // animasi kelelahan ditangani terpisah
                } else if (currentState == State.PLAY_CLASSROOM || currentState == State.DIALOG_CLASSROOM_START || currentState == State.SCENE6_CLASSROOM) {
                    activeIdleTex = ayuDiam;
                    if (currentState == State.SCENE6_CLASSROOM && ayuSelesaiPresentasi != null) {
                        if (currentDialogIndex >= 0 && currentDialogIndex < currentDialogs.size()) {
                            activeIdleTex = ayuSelesaiPresentasi;
                        }
                    }
                } else if (currentState == State.FAINT_SEQUENCE) {
                    activeIdleTex = (ayuPingsan != null) ? ayuPingsan : ayuDiam;
                } else {
                    activeIdleTex = ayuDiam;
                }
            }

            if (activeIdleTex == null && (currentState == State.PLAY_GARDEN || currentState == State.PLAY_LOBBY)
                    && ayuKelelahanAnim != null) {
                ayuKelelahanTime += delta;
                TextureRegion kelelahanFrame = ayuKelelahanAnim.getKeyFrame(ayuKelelahanTime);
                if (kelelahanFrame.getRegionHeight() > 0) {
                    drawWidth = drawHeight * ((float) kelelahanFrame.getRegionWidth() / kelelahanFrame.getRegionHeight());
                }
                batch.draw(kelelahanFrame, ayuX, ayuY, drawWidth, drawHeight);
            } else {
                if (activeIdleTex != null && activeIdleTex.getHeight() > 0) {
                    drawWidth = drawHeight * ((float) activeIdleTex.getWidth() / activeIdleTex.getHeight());
                }
                batch.draw(activeIdleTex, ayuX, ayuY, drawWidth, drawHeight);
            }
        }
    }

    @Override
    public void dispose() {
        if (roomBg != null)
            roomBg.dispose();
        if (roomBgInitial != null)
            roomBgInitial.dispose();
        if (kitchenBg != null)
            kitchenBg.dispose();
        if (scene3Bg != null)
            scene3Bg.dispose();
        if (ayuDiam != null)
            ayuDiam.dispose();
        if (ayuPanikJam != null)
            ayuPanikJam.dispose();
        if (ayuSyok != null)
            ayuSyok.dispose();
        if (ayuKelelahanAnim != null)
            for (TextureRegion region : ayuKelelahanAnim.getKeyFrames()) region.getTexture().dispose();
        if (ayuPusing != null)
            ayuPusing.dispose();
        if (ayuPingsan != null)
            ayuPingsan.dispose();
        if (ayuMohon != null)
            ayuMohon.dispose();
        if (ayuHadapKiri != null)
            ayuHadapKiri.dispose();
        if (ayuPakaianTidur != null && ayuPakaianTidur != ayuDiam)
            ayuPakaianTidur.dispose();
        if (mobilMerah != null)
            mobilMerah.dispose();
        if (kesakitan != null)
            kesakitan.dispose();
        if (siluetMobil != null)
            siluetMobil.dispose();
        if (mobilRusak != null)
            mobilRusak.dispose();
        if (scene4Bg != null)
            scene4Bg.dispose();
        if (scene5Bg != null)
            scene5Bg.dispose();
        if (scene6Bg != null)
            scene6Bg.dispose();
        if (ayuIdle != null)
            ayuIdle.dispose();
        if (ayuSelesaiPresentasi != null && ayuSelesaiPresentasi != ayuIdle)
            ayuSelesaiPresentasi.dispose();
        if (textbox != null)
            textbox.dispose();
        if (blackTexture != null)
            blackTexture.dispose();
        if (whiteTexture != null)
            whiteTexture.dispose();
        if (ibuSprite != null)
            ibuSprite.dispose();
        if (dosenSprite != null)
            dosenSprite.dispose();
        if (temanSprite != null)
            temanSprite.dispose();
        if (messageSfx != null)
            messageSfx.dispose();
        if (doorSfx != null)
            doorSfx.dispose();
        if (carHonkSfx != null)
            carHonkSfx.dispose();
        if (tireSkidSfx != null)
            tireSkidSfx.dispose();
        if (carCrashSfx != null)
            carCrashSfx.dispose();
        if (heartbeatSfx != null)
            heartbeatSfx.dispose();
        if (ayuShockSfx != null)
            ayuShockSfx.dispose();
        if (ringEarsSfx != null)
            ringEarsSfx.dispose();
        if (heavyBreathingSfx != null)
            heavyBreathingSfx.dispose();
        if (crowdChatterSfx != null)
            crowdChatterSfx.dispose();
        if (footstepSfx != null)
            footstepSfx.dispose();
        if (bodyFallSfx != null)
            bodyFallSfx.dispose();
        if (dramaticBoomSfx != null)
            dramaticBoomSfx.dispose();
        if (classroomBgm != null) {
            classroomBgm.stop();
            classroomBgm.dispose();
        }
        if (bgmMusic != null) {
            bgmMusic.stop();
            bgmMusic.dispose();
        }
        font.dispose();
    }
}
