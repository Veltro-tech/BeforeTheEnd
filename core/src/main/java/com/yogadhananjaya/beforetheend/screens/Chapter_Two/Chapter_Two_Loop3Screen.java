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
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;
import com.yogadhananjaya.beforetheend.screens.Chapter_One_OneScreen;
import com.yogadhananjaya.beforetheend.screens.DialogLine;

import java.util.LinkedList;
import java.util.Queue;

public class Chapter_Two_Loop3Screen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout glyphLayout = new GlyphLayout();

    Texture bgKamar, bgKamarTidur, bgDriving, bgDepanKampus, bgKoridor, bgKoridorAtas, bgKelas, bgAtap, bgKelasTerbang;
    Texture activeBg;
    Texture monsterTex;
    Texture rakaSprite;
    Texture alyaSprite;
    Texture textbox;
    Texture blackTexture;
    Texture whiteTexture;

    TextureRegion ayuIdleFrame;
    Animation<TextureRegion> walkRightAnim, walkLeftAnim, idleAnim;

    float ayuX = 483f, ayuY = 180f;
    boolean isWalking = false;
    boolean walkDirectionRight = true;
    float walkTime, idleTime;

    // Effects
    Sound heartbeatSfx;
    float shakeIntensity, shakeDuration;
    float vignetteAlpha, flashAlpha;
    Color flashColor = Color.WHITE;
    float targetZoom = 1f;
    float[] particlesX = new float[5], particlesY = new float[5];
    float flickerTimer, flickerAlpha;
    boolean lightsFlicker = false;

    private enum SubScene {
        SCENE_9A_KAMAR_DIALOG,
        SCENE_9A_EXPLORE,
        SCENE_9A_TRANSITION,
        SCENE_9B_DRIVING,
        SCENE_9C_DEPAN_KAMPUS,
        SCENE_9D_KORIDOR_DIALOG,
        SCENE_9E_TANGGA,
        SCENE_9F_KORIDOR_ATAS,
        SCENE_9G_ALYA_DIALOG,
        SCENE_9H_DEPAN_KELAS,
        SCENE_9I_ESCAPE_QTE,
        SCENE_9J_CHASE,
        SCENE_9K_ATAP_DIALOG,
        SCENE_9L_ENDING,
        SCENE_9L_FADE
    }
    SubScene currentSubScene = SubScene.SCENE_9A_KAMAR_DIALOG;

    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;
    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    float stateTime, subsceneTimer;
    boolean exploringKamar = false;
    boolean chaseStarted = false;
    float monsterX = 2500f;
    float qteTimer = 0f;
    boolean qteSuccess = false;
    boolean qteFinished = false;
    boolean isHiding = false;
    float hideTimer = 0f;

    final float WORLD_WIDTH = 1920f;
    final float WORLD_HEIGHT = 1080f;

    public Chapter_Two_Loop3Screen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        bgKamar = new Texture("backgrounds/kamar-ayu-6.png");
        bgKamarTidur = new Texture("backgrounds/Chapter_2/kamar-ayu-tidur.png");
        bgKamarTidur = new Texture("backgrounds/Chapter_2/kamar-ayu-tidur.png");
        bgDriving = new Texture("backgrounds/scene-4.png");
        bgDepanKampus = new Texture("backgrounds/Chapter_2/depan-kampus.png");
        bgKoridor = new Texture("backgrounds/otw-kelas.png");
        bgKoridorAtas = new Texture("backgrounds/Chapter_2/koridor-kampus-2.png");
        bgKelas = new Texture("backgrounds/Chapter_2/kelas-kosong.png");
        bgAtap = new Texture("backgrounds/Chapter_2/atap-kampus.png");
        bgKelasTerbang = new Texture("backgrounds/Chapter_2/kelas-terbang.png");
        activeBg = bgKamarTidur;
        textbox = new Texture("ui/textbox.png");

        rakaSprite = new Texture("character/Raka/diam.png");
        if (Gdx.files.internal("character/Teman/diam.png").exists())
            alyaSprite = new Texture("character/Teman/diam.png");
        monsterTex = new Texture("backgrounds/Chapter_2/monster.png");

        if (Gdx.files.internal("SFX/heartbeat.mp3").exists())
            heartbeatSfx = Gdx.audio.newSound(Gdx.files.internal("SFX/heartbeat.mp3"));

        for (int i = 0; i < 5; i++) {
            particlesX[i] = MathUtils.random(WORLD_WIDTH);
            particlesY[i] = MathUtils.random(WORLD_HEIGHT);
        }

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK); pm.fill(); blackTexture = new Texture(pm);
        pm.setColor(Color.WHITE); pm.fill(); whiteTexture = new Texture(pm);
        pm.dispose();

        setupAnimations();
        setupDialogQueue();
    }

    private void setupAnimations() {
        Texture idleTex = new Texture("character/Ayu/diam-2.png");
        ayuIdleFrame = new TextureRegion(idleTex);

        Array<TextureRegion> rightFrames = new Array<>();
        for (int i = 0; i <= 12; i++) {
            String p = "character/Ayu/ayu-yoga-jalan/frame_" + (i < 10 ? "0" : "") + i + "_delay-0.11s.png";
            if (Gdx.files.internal(p).exists()) rightFrames.add(new TextureRegion(new Texture(p)));
        }
        if (rightFrames.size > 0) walkRightAnim = new Animation<>(0.11f, rightFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> leftFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            if (Gdx.files.internal("character/Ayu/jalan_kiri_" + i + ".png").exists())
                leftFrames.add(new TextureRegion(new Texture("character/Ayu/jalan_kiri_" + i + ".png")));
        }
        if (leftFrames.size > 0) walkLeftAnim = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> idleFrames = new Array<>();
        int totalIdleFrames = 229;
        int maxIdleFrames = 30;
        int step = Math.max(1, totalIdleFrames / maxIdleFrames);
        for (int i = 1; i <= totalIdleFrames; i += step) {
            String p = "character/Ayu/ayu-yoga/Ayu_yoga_" + i + ".png";
            if (Gdx.files.internal(p).exists()) idleFrames.add(new TextureRegion(new Texture(p)));
            if (idleFrames.size >= maxIdleFrames) break;
        }
        if (idleFrames.size > 0) {
            float idleFrameDuration = 0.08f * step;
            idleAnim = new Animation<>(idleFrameDuration, idleFrames, Animation.PlayMode.LOOP);
        }
    }

    private void setupDialogQueue() {
        dialogQueue = new LinkedList<>();
        dialogQueue.add(new DialogLine("Narator", "Ayu terkejut. Sekali lagi dia bangun di kasur dengan kondisi yang sama..."));
        dialogQueue.add(new DialogLine("Narator", "Sakit di kepala, tapi sekarang ditambah sakit perut yang kadang datang."));
        dialogQueue.add(new DialogLine("Ayu", "Aduh, gw ini kenapa sih? Kalo kayak gini terus gw mending mati aja dah."));
        dialogQueue.add(new DialogLine("Sistem", "KAMAR_EXPLORE"));
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
            targetText = currentDialog.text;
            displayedText = "";
            charIndex = 0;
            textTimer = 0f;
        } else {
            currentDialog = null;
            if (currentSubScene == SubScene.SCENE_9I_ESCAPE_QTE && !qteFinished) {
                qteFinished = true;
            }
        }
    }

    private void handleSystemTrigger(String command) {
        switch (command) {
            case "KAMAR_EXPLORE":
                currentSubScene = SubScene.SCENE_9A_EXPLORE;
                activeBg = bgKamar;
                break;
            case "TO_DRIVING":
                currentSubScene = SubScene.SCENE_9A_TRANSITION;
                subsceneTimer = 0f;
                break;
            case "START_DRIVING":
                currentSubScene = SubScene.SCENE_9B_DRIVING;
                activeBg = bgDriving;
                ayuX = 100f; ayuY = 180f;
                addDrivingDialogs();
                break;
            case "TO_KAMPUS":
                currentSubScene = SubScene.SCENE_9C_DEPAN_KAMPUS;
                activeBg = bgDepanKampus;
                ayuX = 100f; ayuY = 180f;
                addDepanKampusDialogs();
                break;
            case "TABRAKAN":
                currentSubScene = SubScene.SCENE_9D_KORIDOR_DIALOG;
                activeBg = bgKoridor;
                ayuX = 600f;
                addRakaDialogs();
                break;
            case "NAIK_TANGGA":
                currentSubScene = SubScene.SCENE_9E_TANGGA;
                activeBg = bgKoridor;
                ayuX = 100f; ayuY = 180f;
                addTanggaDialogs();
                break;
            case "KORIDOR_ATAS":
                currentSubScene = SubScene.SCENE_9F_KORIDOR_ATAS;
                activeBg = bgKoridorAtas;
                ayuX = 100f;
                addKoridorAtasDialogs();
                break;
            case "ALYA_SCENE":
                currentSubScene = SubScene.SCENE_9G_ALYA_DIALOG;
                activeBg = bgKoridor;
                ayuX = 400f;
                addAlyaDialogs();
                break;
            case "DEPAN_KELAS":
                currentSubScene = SubScene.SCENE_9H_DEPAN_KELAS;
                ayuX = 200f;
                addDepanKelasDialogs();
                break;
            case "MASUK_KELAS":
                currentSubScene = SubScene.SCENE_9I_ESCAPE_QTE;
                activeBg = bgKelas;
                ayuX = 500f;
                addMonsterDialogs();
                break;
            case "START_CHASE":
                currentSubScene = SubScene.SCENE_9J_CHASE;
                activeBg = bgKoridorAtas;
                ayuX = 500f;
                monsterX = 600f;
                chaseStarted = false;
                addChaseDialogs();
                break;
            case "KE_ATAP":
                currentSubScene = SubScene.SCENE_9K_ATAP_DIALOG;
                activeBg = bgAtap;
                ayuX = 600f;
                addAtapDialogs();
                break;
            case "ENDING":
                currentSubScene = SubScene.SCENE_9L_ENDING;
                activeBg = bgKelasTerbang;
                ayuX = 300f;
                addEndingDialogs();
                break;
            case "FADE_OUT_LOOP3":
                currentSubScene = SubScene.SCENE_9L_FADE;
                subsceneTimer = 0f;
                break;
        }
    }

    private void addDrivingDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "TO_KAMPUS"));
        list.addFirst(new DialogLine("Narator", "Selama perjalanan Ayu sesekali melihat ke kanan dan kiri..."));
        list.addFirst(new DialogLine("Ayu", "Ada yang aneh. Tapi aku gak bisa jelasin apa."));
        list.addFirst(new DialogLine("Narator", "Ayu segera bersiap kembali ke kampus untuk sidang proyek."));
    }

    private void addDepanKampusDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "TABRAKAN"));
        list.addFirst(new DialogLine("Narator", "Ayu berjalan sambil membalas pesan. Notifikasi terus berdatangan."));
        list.addFirst(new DialogLine("Narator", "BRUK! Ia menabrak seseorang."));
        list.addFirst(new DialogLine("Narator", "Sesampainya di kampus, Ayu langsung bergegas. HP terus bergetar."));
    }

    private void addRakaDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "NAIK_TANGGA"));
        list.addFirst(new DialogLine("Raka", "Berarti bener."));
        list.addFirst(new DialogLine("Ayu", "..."));
        list.addFirst(new DialogLine("Raka", "Dua minggu?"));
        list.addFirst(new DialogLine("Ayu", "Seminggu sih nggak."));
        list.addFirst(new DialogLine("Raka", "Muka lu kayak habis begadang seminggu."));
        list.addFirst(new DialogLine("Ayu", "Hah?"));
        list.addFirst(new DialogLine("Raka", "Tapi serius, lu kenapa?"));
        list.addFirst(new DialogLine("Narator", "Mereka berdua tertawa kecil. Sudah beberapa hari Ayu tidak benar-benar tertawa."));
        list.addFirst(new DialogLine("Ayu", "Berat juga lawannya."));
        list.addFirst(new DialogLine("Raka", "Ke tuhan."));
        list.addFirst(new DialogLine("Ayu", "Gue laporin."));
        list.addFirst(new DialogLine("Raka", "Kalau iya gimana?"));
        list.addFirst(new DialogLine("Ayu", "Lo stalker ya?"));
        list.addFirst(new DialogLine("Raka", "Panjang kalo diceritain."));
        list.addFirst(new DialogLine("Ayu", "Kok bisa?"));
        list.addFirst(new DialogLine("Raka", "Tahu lah, apa coba yang gak gue tahu."));
        list.addFirst(new DialogLine("Ayu", "Kok lu tau?"));
        list.addFirst(new DialogLine("Raka", "Yang biasa dipanggil Ayu kan."));
        list.addFirst(new DialogLine("Narator", "Ayu terdiam."));
        list.addFirst(new DialogLine("Raka", "Kalau gitu kenalin, gue Raka."));
        list.addFirst(new DialogLine("Ayu", "Ohh..."));
        list.addFirst(new DialogLine("Raka", "Ada urusan keluar kota."));
        list.addFirst(new DialogLine("Ayu", "Hah, kok bisa?"));
        list.addFirst(new DialogLine("Raka", "Soalnya gue gak ikut."));
        list.addFirst(new DialogLine("Ayu", "Kok gue gak pernah liat lu pas PKKMB?"));
        list.addFirst(new DialogLine("Raka", "Iya."));
        list.addFirst(new DialogLine("Ayu", "Raka?"));
        list.addFirst(new DialogLine("Raka", "Nama gue Raka."));
        list.addFirst(new DialogLine("Ayu", "Iya lah lu, siapa lagi coba orang di depan mata gw sekarang."));
        list.addFirst(new DialogLine("Raka", "Gua?"));
        list.addFirst(new DialogLine("Ayu", "Eh bentar. Nama lu siapa dah? Gw kayaknya gak pernah liat lu di area kampus."));
        list.addFirst(new DialogLine("Narator", "Baru sekarang Ayu memperhatikan wajah cowok itu. Aneh, rasanya asing."));
        list.addFirst(new DialogLine("Ayu", "Iya maaf atuh, galak amat sih."));
        list.addFirst(new DialogLine("Raka", "Safety-nya diingat dong, gimana sih lu."));
        list.addFirst(new DialogLine("Ayu", "Iya, pak polisi."));
        list.addFirst(new DialogLine("Raka", "Tapi lain kali jangan main HP sambil jalan, bahaya."));
        list.addFirst(new DialogLine("Ayu", "Serius, gue gak sengaja."));
        list.addFirst(new DialogLine("Raka", "Iya, gue tahu kok."));
        list.addFirst(new DialogLine("Ayu", "Eh astaga! Maaf, maaf banget!"));
        list.addFirst(new DialogLine("Narator", "Cowo di depannya mengusap bahunya pelan."));
    }

    private void addTanggaDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "KORIDOR_ATAS"));
        list.addFirst(new DialogLine("Raka", "Mungkin."));
        list.addFirst(new DialogLine("Ayu", "Lu nyindir gue ya?"));
        list.addFirst(new DialogLine("Raka", "Iya kan, gw bukan si Ayu."));
        list.addFirst(new DialogLine("Ayu", "Lu tuh tipikal orang yang santai ya."));
        list.addFirst(new DialogLine("Raka", "Enggak."));
        list.addFirst(new DialogLine("Ayu", "Boleh?"));
        list.addFirst(new DialogLine("Raka", "Yaudah gak usah sidang."));
        list.addFirst(new DialogLine("Ayu", "Segitunya?"));
        list.addFirst(new DialogLine("Raka", "Yaelah."));
        list.addFirst(new DialogLine("Ayu", "Iyalah..."));
        list.addFirst(new DialogLine("Raka", "Oalah."));
        list.addFirst(new DialogLine("Ayu", "Sidang proyek."));
        list.addFirst(new DialogLine("Raka", "Bukan presentasi?")); // actually Raka says this
        list.addFirst(new DialogLine("Ayu", "Presentasi hari ini? Tanya Raka."));
        list.addFirst(new DialogLine("Narator", "Mereka menaiki tangga bersama."));
        list.addFirst(new DialogLine("Narator", "Raka menyusul dari belakang. \"Eh tunggu!\""));
        list.addFirst(new DialogLine("Narator", "Ayu langsung berlari menuju tangga."));
        list.addFirst(new DialogLine("Ayu", "ANJIR!"));
        list.addFirst(new DialogLine("Raka", "Makanya."));
        list.addFirst(new DialogLine("Ayu", "GW ADA SIDANG!"));
        list.addFirst(new DialogLine("Raka", "Nah kan."));
        list.addFirst(new DialogLine("Narator", "Tiba-tiba Ayu melirik jam di HP. Matanya langsung membelalak."));
    }

    private void addKoridorAtasDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "ALYA_SCENE"));
        list.addFirst(new DialogLine("Narator", "Beberapa detik kemudian ia menghilang di ujung koridor."));
        list.addFirst(new DialogLine("Narator", "Raka melambaikan tangan sambil berjalan pergi. \"Sampai nanti, Yu.\""));
        list.addFirst(new DialogLine("Narator", "\"Nanti juga lo paham.\" Raka berbalik."));
        list.addFirst(new DialogLine("Ayu", "Kenapa nggak sekarang aja? Eh bentar woi!"));
        list.addFirst(new DialogLine("Raka", "Temui gue di balkon setelah kelas selesai."));
        list.addFirst(new DialogLine("Narator", "Raka tidak langsung menjawab. Ia hanya menatap koridor yang panjang."));
        list.addFirst(new DialogLine("Ayu", "Maksud lu apa?"));
        list.addFirst(new DialogLine("Narator", "Jantung Ayu berdegup lebih cepat."));
        list.addFirst(new DialogLine("Raka", "Akhirnya lu sadar juga."));
        list.addFirst(new DialogLine("Narator", "Raka berhenti. Perlahan ia menoleh. Tatapannya berubah lebih serius."));
        list.addFirst(new DialogLine("Ayu", "Tempat ini aneh. Lu ngerasa nggak sih?"));
        list.addFirst(new DialogLine("Narator", "Semakin tinggi mereka naik, suasana kampus terasa semakin aneh."));
    }

    private void addAlyaDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "DEPAN_KELAS"));
        list.addFirst(new DialogLine("Ayu", "Ini aneh banget. Pada kenapa ya?"));
        list.addFirst(new DialogLine("Narator", "Ayu kaget. Dia meninggalkan Alya dan teman-teman yang lain."));
        list.addFirst(new DialogLine("Alya", "Lu gak siap, dan gak akan pernah siap."));
        list.addFirst(new DialogLine("Ayu", "Eh Alya, kok masih disini? Bukannya bagian kita ya sekarang?"));
        list.addFirst(new DialogLine("Narator", "Ayu bertemu dengan teman-temannya di koridor."));
    }

    private void addDepanKelasDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "MASUK_KELAS"));
        list.addFirst(new DialogLine("Ayu", "Tapi sidang ini udah gue siapin berbulan-bulan. Hari ini gue tuntaskan!"));
        list.addFirst(new DialogLine("Narator", "Ayu merasa ada sesuatu besar dan menyeramkan di balik pintu. Tapi dia tetap membukanya."));
    }

    private void addMonsterDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "START_MONSTER_QTE"));
        list.addFirst(new DialogLine("Narator", "Ayu merinding setengah mati. Ia menoleh kembali ke anak di depannya..."));
        list.addFirst(new DialogLine("Narator", "Ayu mendapat pesan dari nomor tak dikenal: \"lari yu\""));
        list.addFirst(new DialogLine("Narator", "Anak itu diam. Tiba-tiba berbicara: \"Cek aja HP lu.\""));
        list.addFirst(new DialogLine("Narator", "Ayu perlahan menghampirinya. \"Eh, lu tau gak anak kelas pada kemana?\""));
        list.addFirst(new DialogLine("Narator", "Di belakang kelas ada seorang perempuan duduk menghadap tembok. Ayu heran."));
        list.addFirst(new DialogLine("Narator", "Ayu menyalakan lampu. Kelas kosong, lampu jarang menyala."));
    }

    private void addChaseDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "KE_ATAP"));
        list.addFirst(new DialogLine("Narator", "Raka segera mengajak Ayu ke atas."));
        list.addFirst(new DialogLine("Raka", "Kita gak aman disini. Ayo ikut gw ke atas."));
        list.addFirst(new DialogLine("Narator", "Entitas itu melewati mereka tanpa sadar."));
        list.addFirst(new DialogLine("Raka", "Lu diam, jangan bersuara."));
        list.addFirst(new DialogLine("Narator", "Raka menarik Ayu ke tempat aman."));
        list.addFirst(new DialogLine("Narator", "Ayu terus berlari ke koridor. Entitas itu masih mengejar."));
        list.addFirst(new DialogLine("Narator", "Ayu tanpa berpikir langsung berlari keluar ruang kelas."));
        list.addFirst(new DialogLine("Narator", "Anak di depannya berubah menjadi entitas menyeramkan!"));
    }

    private void addAtapDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "ENDING"));
        list.addFirst(new DialogLine("Ayu", "Oke Rak, gw percaya sama lu."));
        list.addFirst(new DialogLine("Raka", "Percaya aja sama gw. Selagi ada gw, kita baik-baik aja."));
        list.addFirst(new DialogLine("Ayu", "Tapi gw takut, Rak."));
        list.addFirst(new DialogLine("Raka", "Tenang aja, semua masalah pasti ada jalan keluar."));
        list.addFirst(new DialogLine("Ayu", "Terus gimana caranya kita bisa kabur dari sini?"));
        list.addFirst(new DialogLine("Raka", "Kalo prediksi gw sih, itu bentuk dari sisi lain lu. Ketakutan hebat lu jadiin makhluk kayak gitu."));
        list.addFirst(new DialogLine("Narator", "Mereka berdua berdiam sejenak."));
        list.addFirst(new DialogLine("Ayu", "Terus gimana caranya makhluk itu bisa ada di sini?"));
        list.addFirst(new DialogLine("Raka", "Sssst udah, nanti kita ketahuan lagi gara-gara suara berisik lu."));
        list.addFirst(new DialogLine("Ayu", "Lu jadi cowo gimana sih."));
        list.addFirst(new DialogLine("Raka", "Dan kenapa juga gw harus tau."));
        list.addFirst(new DialogLine("Ayu", "Lah kok bisa lu gak tau?"));
        list.addFirst(new DialogLine("Raka", "Gw jujur gak tau sih."));
        list.addFirst(new DialogLine("Ayu", "Rak, jelasin sama gw tadi itu apa."));
    }

    private void addEndingDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "FADE_OUT_LOOP3"));
        list.addFirst(new DialogLine("Narator", "Mereka berdua tertawa. Tapi mereka tahu ada hal besar yang menanti di depan."));
        list.addFirst(new DialogLine("Ayu", "Siap deh pak, atur aja."));
        list.addFirst(new DialogLine("Raka", "Kayak yang gw bilang, selagi ada gw kita bakal baik-baik aja."));
        list.addFirst(new DialogLine("Ayu", "Lu serius? Kita gak mungkin bisa jangkau tempat setinggi itu."));
        list.addFirst(new DialogLine("Raka", "Itu target kita."));
        list.addFirst(new DialogLine("Narator", "Mereka melihat ruang kelas mereka terbang makin tinggi, memisahkan dari bangunan lain."));
        list.addFirst(new DialogLine("Narator", "Sesaat setelah Ayu berbicara, dunia yang ia lihat mulai retak dan runtuh."));
    }

    private void updateTypewriter(float delta) {
        if (charIndex < targetText.length()) {
            textTimer += delta;
            if (textTimer >= typeSpeed) { displayedText += targetText.charAt(charIndex); charIndex++; textTimer = 0f; }
        }
    }

    private void handleInput(float delta) {
        if (currentDialog != null && Gdx.input.justTouched()) {
            if (charIndex < targetText.length()) { displayedText = targetText; charIndex = targetText.length(); }
            else { dequeueNextDialog(); }
        }

        boolean moveEnabled = (currentSubScene == SubScene.SCENE_9A_EXPLORE) ||
                              (currentSubScene == SubScene.SCENE_9B_DRIVING) ||
                              (currentSubScene == SubScene.SCENE_9C_DEPAN_KAMPUS) ||
                              (currentSubScene == SubScene.SCENE_9H_DEPAN_KELAS) ||
                              (currentSubScene == SubScene.SCENE_9J_CHASE && !chaseStarted);

        if (moveEnabled) {
            isWalking = false;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                ayuX += 250f * delta; isWalking = true; walkDirectionRight = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                ayuX -= 250f * delta; isWalking = true; walkDirectionRight = false;
            }

            if (currentSubScene == SubScene.SCENE_9B_DRIVING) {
                ayuX += 150f * delta; isWalking = true; walkDirectionRight = true;
                if (ayuX >= WORLD_WIDTH * 0.8f) {
                    LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                    list.addFirst(new DialogLine("Sistem", "TO_KAMPUS"));
                    dequeueNextDialog();
                }
            }

            ayuX = MathUtils.clamp(ayuX, 50f, WORLD_WIDTH - 50f);

            if (currentSubScene == SubScene.SCENE_9A_EXPLORE && ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                exploringKamar = false;
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "TO_DRIVING"));
                dequeueNextDialog();
            }
            if (currentSubScene == SubScene.SCENE_9C_DEPAN_KAMPUS && ayuX >= 800f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "TABRAKAN"));
                dequeueNextDialog();
            }
            if (currentSubScene == SubScene.SCENE_9H_DEPAN_KELAS && ayuX >= WORLD_WIDTH - 500f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "MASUK_KELAS"));
                dequeueNextDialog();
            }
            if (currentSubScene == SubScene.SCENE_9J_CHASE && !chaseStarted && ayuX >= 1000f) {
                chaseStarted = true;
            }
        }
    }

    private void drawCharacter(float delta) {
        float drawHeight = 361f;
        TextureRegion currentFrame = null;
        if (isWalking) {
            walkTime += delta;
            if (walkDirectionRight && walkRightAnim != null) currentFrame = walkRightAnim.getKeyFrame(walkTime);
            else if (!walkDirectionRight && walkLeftAnim != null) currentFrame = walkLeftAnim.getKeyFrame(walkTime);
        } else { walkTime = 0f; }
        if (currentFrame == null) {
            if (idleAnim != null) { idleTime += delta; currentFrame = idleAnim.getKeyFrame(idleTime); }
            else { currentFrame = ayuIdleFrame; }
        } else { idleTime = 0f; }
        float drawWidth = drawHeight;
        if (currentFrame.getRegionHeight() > 0) drawWidth = drawHeight * ((float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight());
        batch.draw(currentFrame, ayuX, ayuY, drawWidth, drawHeight);
    }

    private void renderHint(String hint) {
        if (hint == null) return;
        float hintW = 400f, hintH = 55f;
        float hintX = ayuX + 99f - hintW / 2f, hintY = ayuY + 361f + 20f;
        if (hintX < 10f) hintX = 10f;
        if (hintX + hintW > WORLD_WIDTH - 10f) hintX = WORLD_WIDTH - 10f - hintW;
        batch.draw(blackTexture, hintX, hintY, hintW, hintH);
        batch.setColor(Color.GOLD);
        batch.draw(whiteTexture, hintX, hintY + hintH - 3f, hintW, 3f);
        batch.draw(whiteTexture, hintX, hintY, hintW, 3f);
        batch.draw(whiteTexture, hintX, hintY, 3f, hintH);
        batch.draw(whiteTexture, hintX + hintW - 3f, hintY, 3f, hintH);
        batch.setColor(Color.WHITE);
        font.getData().setScale(1.2f); font.setColor(Color.GOLD);
        glyphLayout.setText(font, hint);
        font.draw(batch, hint, hintX + (hintW - glyphLayout.width) / 2f, hintY + hintH - 15f);
        font.getData().setScale(2f); font.setColor(Color.WHITE);
    }

    private void renderDialogBox() {
        if (currentDialog == null) return;
        float bubbleW = 650f, bubbleH = 180f, bubbleX, bubbleY;
        if ("Narator".equalsIgnoreCase(currentDialog.character)) {
            bubbleW = 900f; bubbleH = 150f;
            bubbleX = WORLD_WIDTH / 2f - bubbleW / 2f;
            bubbleY = WORLD_HEIGHT - bubbleH - 50f;
        } else if ("Raka".equalsIgnoreCase(currentDialog.character)) {
            bubbleX = WORLD_WIDTH - bubbleW - 50f;
            bubbleY = ayuY + 440f;
        } else if ("Alya".equalsIgnoreCase(currentDialog.character)) {
            bubbleX = 200f;
            bubbleY = ayuY + 380f;
        } else {
            bubbleX = ayuX + 99f - bubbleW / 2f;
            bubbleY = ayuY + 380f;
        }
        if (bubbleX < 20f) bubbleX = 20f;
        if (bubbleX + bubbleW > WORLD_WIDTH - 20f) bubbleX = WORLD_WIDTH - 20f - bubbleW;
        batch.draw(blackTexture, bubbleX, bubbleY, bubbleW, bubbleH);
        batch.setColor(Color.GOLD);
        batch.draw(whiteTexture, bubbleX, bubbleY + bubbleH - 3f, bubbleW, 3f);
        batch.draw(whiteTexture, bubbleX, bubbleY, bubbleW, 3f);
        batch.draw(whiteTexture, bubbleX, bubbleY, 3f, bubbleH);
        batch.draw(whiteTexture, bubbleX + bubbleW - 3f, bubbleY, 3f, bubbleH);
        batch.setColor(Color.WHITE);
        if (!"Narator".equalsIgnoreCase(currentDialog.character)) {
            font.getData().setScale(1.4f); font.setColor(Color.GOLD);
            font.draw(batch, currentDialog.character, bubbleX + 25f, bubbleY + bubbleH - 20f);
        }
        font.setColor(Color.WHITE);
        float textY = "Narator".equalsIgnoreCase(currentDialog.character) ? bubbleY + bubbleH - 35f : bubbleY + bubbleH - 60f;
        font.draw(batch, displayedText, bubbleX + 25f, textY, bubbleW - 50f, com.badlogic.gdx.utils.Align.left, true);
        if (charIndex >= targetText.length() && (int)(stateTime * 2) % 2 == 0) {
            batch.setColor(Color.GOLD);
            float ax = bubbleX + bubbleW - 36f, ay = bubbleY + 20f;
            batch.draw(whiteTexture, ax, ay, 12f, 3f);
            batch.draw(whiteTexture, ax + 3f, ay - 3f, 6f, 3f);
            batch.draw(whiteTexture, ax + 6f, ay - 6f, 3f, 3f);
            batch.setColor(Color.WHITE);
        }
        font.getData().setScale(2f); font.setColor(Color.WHITE);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        subsceneTimer += delta;

        if (currentSubScene == SubScene.SCENE_9A_TRANSITION) {
            if (subsceneTimer >= 2.0f) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "START_DRIVING"));
                dequeueNextDialog();
            }
        } else if (currentSubScene == SubScene.SCENE_9L_FADE) {
            if (subsceneTimer >= 3.0f) {
                game.setScreen(new Chapter_One_OneScreen(game));
                return;
            }
        } else {
            handleInput(delta);
            updateTypewriter(delta);
        }

        if (currentSubScene == SubScene.SCENE_9I_ESCAPE_QTE && qteFinished && subsceneTimer > 0.5f) {
            subsceneTimer = 0f;
            currentSubScene = SubScene.SCENE_9J_CHASE;
            activeBg = bgKoridorAtas;
            ayuX = 500f;
            monsterX = 600f;
            chaseStarted = false;
        }

        if (currentSubScene == SubScene.SCENE_9J_CHASE && chaseStarted) {
            monsterX += delta * 80f;
            ayuX += delta * 120f;
            isWalking = true;
            walkDirectionRight = true;
            if (ayuX >= 1300f && monsterX < ayuX + 100f && !isHiding) {
                isHiding = true;
                hideTimer = 0f;
            }
            if (isHiding) {
                hideTimer += delta;
                if (hideTimer >= 3.0f) {
                    isHiding = false;
                    chaseStarted = false;
                    LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                    list.addFirst(new DialogLine("Sistem", "KE_ATAP"));
                    dequeueNextDialog();
                }
            }
        }

        // Effects update
        if (shakeDuration > 0f) shakeDuration -= delta;
        if (vignetteAlpha > 0f) vignetteAlpha -= delta * 0.5f;
        if (flashAlpha > 0f) flashAlpha -= delta * 2f;
        targetZoom += (1f - targetZoom) * delta * 5f;
        camera.zoom = targetZoom;
        if (lightsFlicker) { flickerTimer += delta; flickerAlpha = Math.abs(MathUtils.sin(flickerTimer * 3f)) * 0.15f; }

        // Scene-specific effect triggers
        if (currentSubScene == SubScene.SCENE_9C_DEPAN_KAMPUS && subsceneTimer < 0.3f) {
            targetZoom = 1.3f; vignetteAlpha = 0.2f; shakeIntensity = 3f; shakeDuration = 0.5f;
        }
        if (currentSubScene == SubScene.SCENE_9F_KORIDOR_ATAS) lightsFlicker = true; else lightsFlicker = false;
        if (currentSubScene == SubScene.SCENE_9F_KORIDOR_ATAS) { vignetteAlpha = Math.max(vignetteAlpha, 0.25f); }
        if (currentSubScene == SubScene.SCENE_9H_DEPAN_KELAS) { vignetteAlpha = Math.max(vignetteAlpha, 0.15f);
            if (heartbeatSfx != null && subsceneTimer % 1.2f < delta) heartbeatSfx.play(0.6f);
        }
        if (currentSubScene == SubScene.SCENE_9I_ESCAPE_QTE) { vignetteAlpha = Math.max(vignetteAlpha, 0.4f); }
        if (currentSubScene == SubScene.SCENE_9J_CHASE) { vignetteAlpha = Math.max(vignetteAlpha, 0.35f); }
        if (currentSubScene == SubScene.SCENE_9L_ENDING) { targetZoom = 0.85f; vignetteAlpha = Math.max(vignetteAlpha, 0.3f); }
        if (currentSubScene == SubScene.SCENE_9J_CHASE && isHiding) { targetZoom = 0.7f; }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        if (shakeDuration > 0f) {
            camera.position.add(MathUtils.random(-shakeIntensity, shakeIntensity),
                    MathUtils.random(-shakeIntensity, shakeIntensity), 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (currentSubScene == SubScene.SCENE_9A_TRANSITION) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else if (currentSubScene == SubScene.SCENE_9L_FADE) {
            batch.draw(bgKelasTerbang, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            float alpha = Math.min(1f, subsceneTimer / 2f);
            batch.setColor(0, 0, 0, alpha); batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT); batch.setColor(Color.WHITE);
            if (subsceneTimer >= 2f) {
                font.setColor(Color.RED);
                font.draw(batch, "LOOP 3", WORLD_WIDTH / 2f - 100, WORLD_HEIGHT / 2f);
            }
        } else {
            batch.draw(activeBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            if (currentSubScene == SubScene.SCENE_9D_KORIDOR_DIALOG) {
                batch.draw(rakaSprite, 1000f, 180f, 198f, 334f);
            }
            if (currentSubScene == SubScene.SCENE_9K_ATAP_DIALOG) {
                batch.draw(rakaSprite, 900f, 180f, 198f, 334f);
            }
            if (currentSubScene == SubScene.SCENE_9G_ALYA_DIALOG && alyaSprite != null) {
                batch.draw(alyaSprite, 800f, 180f, 198f, 334f);
            }
            if (currentSubScene == SubScene.SCENE_9I_ESCAPE_QTE || currentSubScene == SubScene.SCENE_9J_CHASE) {
                if (monsterTex != null) batch.draw(monsterTex, monsterX, 100f, 500f, 500f);
            }

            if (activeBg != bgKamarTidur) drawCharacter(delta);
            renderDialogBox();

            if (currentSubScene == SubScene.SCENE_9A_EXPLORE && currentDialog == null) {
                String hint = ayuX >= WORLD_WIDTH - 300f ? "Tekan E untuk keluar" : "Coba ke kanan...";
                renderHint(hint);
            }
            if (currentSubScene == SubScene.SCENE_9C_DEPAN_KAMPUS) {
                String hint = ayuX >= 800f ? "Tekan E untuk lanjut" : "Coba ke kanan...";
                renderHint(hint);
            }
            if (currentSubScene == SubScene.SCENE_9H_DEPAN_KELAS) {
                String hint = ayuX >= WORLD_WIDTH - 500f ? "Tekan E untuk masuk kelas" : "Jalan ke kanan menuju kelas...";
                renderHint(hint);
            }
            if (currentSubScene == SubScene.SCENE_9J_CHASE && !chaseStarted) {
                renderHint("Lari ke kanan!");
            }
        }

        // Effects render (vignette, flash, particles)
        if (flashAlpha > 0f) {
            batch.setColor(flashColor.r, flashColor.g, flashColor.b, flashAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        }
        if (vignetteAlpha > 0f) {
            batch.setColor(0, 0, 0, vignetteAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH / 4f, WORLD_HEIGHT);
            batch.draw(blackTexture, WORLD_WIDTH * 3 / 4f, 0, WORLD_WIDTH / 4f, WORLD_HEIGHT);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT / 6f);
            batch.draw(blackTexture, 0, WORLD_HEIGHT * 5 / 6f, WORLD_WIDTH, WORLD_HEIGHT / 6f);
            batch.setColor(Color.WHITE);
        }
        if (lightsFlicker && flickerAlpha > 0f) {
            batch.setColor(0, 0, 0, flickerAlpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        }
        for (int i = 0; i < 5; i++) {
            particlesY[i] += delta * 15f;
            if (particlesY[i] > WORLD_HEIGHT) { particlesY[i] = -10f; particlesX[i] = MathUtils.random(WORLD_WIDTH); }
            batch.setColor(1, 1, 1, 0.3f);
            batch.draw(whiteTexture, particlesX[i], particlesY[i], 3f, 3f);
            batch.setColor(Color.WHITE);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose(); font.dispose(); textbox.dispose();
        bgKamar.dispose(); bgKamarTidur.dispose(); bgDriving.dispose();
        bgDepanKampus.dispose(); bgKoridor.dispose(); bgKoridorAtas.dispose();
        bgKelas.dispose(); bgAtap.dispose(); bgKelasTerbang.dispose();
        monsterTex.dispose(); rakaSprite.dispose();
        if (heartbeatSfx != null) heartbeatSfx.dispose();
        if (alyaSprite != null) alyaSprite.dispose();
    }
}
