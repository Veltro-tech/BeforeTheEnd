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

public class Chapter_One_FiveScreen extends ScreenAdapter {
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

    Color bgColor = new Color(0.2f, 0.05f, 0.05f, 1); // Merah gelap (suasana Boss Fight)

    // ==========================================================
    // --- STRUKTUR DATA: ADT PLAYER & ADT BOSS (Sesuai Notes) ---
    // ==========================================================

    // 1. ADT Player
    class PlayerData {
        int hp = 100;
        int armor = 50;
        boolean isArmorActive = false;
        float x = 200, y = 200;
        public void removeArmor() { isArmorActive = false; armor = 0; }
    }
    PlayerData ayuPlayer = new PlayerData();

    // 2. ADT Boss (Echo)
    class BossData {
        int hp = 1000;
        int maxHp = 1000;
        int phase = 1; // 1 = Pressure, 2 = Distortion, 3 = Collapse, 4 = Despair, 5 = Acceptance
        boolean isVulnerable = false; // Untuk mekanisme DAMAGE WINDOW

        public void transitionToNextPhase() {
            if (phase < 5) phase++;
            System.out.println("Echo memasuki Fase: " + phase);
        }
    }
    BossData echoBoss = new BossData();

    // Status visual Raka yang perlahan menjadi abu
    float rakaOpacity = 1.0f;
    final float WORLD_WIDTH = 1280f;
    final float WORLD_HEIGHT = 720f;

    public Chapter_One_FiveScreen(final BeforeTheEndGame game) {
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
        Array<DialogLine> tempArray = json.fromJson(Array.class, DialogLine.class, Gdx.files.internal("data/chapter5_dialog.json"));
        for (DialogLine line : tempArray) {
            dialogQueue.add(line);
        }
        dequeueNextDialog();
    }

    private void dequeueNextDialog() {
        if (!dialogQueue.isEmpty()) {
            currentDialog = dialogQueue.poll();

            if (currentDialog.character.equals("Narator")) {
                dequeueNextDialog(); return;
            }

            if (currentDialog.character.equals("Sistem")) {
                String sysText = currentDialog.text;

                if (sysText.equals("TRIGGER_BOSS_BATTLE")) {
                    gameStateStack.push("IN_BATTLE");
                    ayuPlayer.isArmorActive = true;
                    System.out.println("--- BOSS BATTLE DIMULAI ---");
                    // Nanti kita akan merender HP Bar di metode render()
                } else if (sysText.equals("TRIGGER_BATTLE_END")) {
                    gameStateStack.pop(); // Keluar dari IN_BATTLE
                    bgColor.set(0.9f, 0.9f, 0.9f, 1); // Warna langit putih / sunyi
                    rakaOpacity = 0.5f; // Raka mulai transparan
                } else if (sysText.equals("TRIGGER_EPILOGUE")) {
                    System.out.println("PINDAH KE LAYAR EPILOGUE & CREDITS");
                    // game.setScreen(new EpilogueScreen(game));
                }

                dequeueNextDialog(); return;
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameStateStack.peek().equals("PLAYING") || gameStateStack.peek().equals("IN_BATTLE"))
                gameStateStack.push("PAUSED");
            else if (gameStateStack.peek().equals("PAUSED"))
                gameStateStack.pop();
        }

        // --- SIMULASI MEKANIK GAMEPLAY SEMENTARA (HANYA BERJALAN SAAT BATTLE) ---
        if (gameStateStack.peek().equals("IN_BATTLE")) {
            // Simulasi klik spasi untuk memukul bos
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                echoBoss.hp -= 100; // Damage
                System.out.println("HP Echo: " + echoBoss.hp);

                // Logika Perpindahan Fase Otomatis
                if (echoBoss.hp <= 660 && echoBoss.phase == 1) {
                    echoBoss.transitionToNextPhase();
                    rakaOpacity = 0.8f;
                } else if (echoBoss.hp <= 330 && echoBoss.phase == 2) {
                    echoBoss.transitionToNextPhase();
                    rakaOpacity = 0.6f;
                } else if (echoBoss.hp <= 0 && echoBoss.phase == 3) {
                    echoBoss.phase = 4;
                    echoBoss.hp += 330; // Echo Heal (Despair Phase)
                    ayuPlayer.removeArmor();
                } else if (echoBoss.phase == 4) {
                    echoBoss.transitionToNextPhase(); // Masuk Acceptance
                    dequeueNextDialog(); // Lanjut dialog untuk cinematic
                }
            }
            return; // Hentikan input klik layar (dialog) saat bertarung
        }

        if (gameStateStack.peek().equals("PAUSED")) return;

        // Input klik untuk lanjut dialog biasa
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

        if (!gameStateStack.peek().equals("PAUSED") && !gameStateStack.peek().equals("IN_BATTLE")) {
            updateTypewriter(delta);
        }

        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(ayuIdleAnim.getKeyFrame(stateTime), 150, 220, 198f * 0.85f, 422f * 0.85f);

        // Render Teks Dialog (Kecuali sedang bertarung)
        if (currentDialog != null && !gameStateStack.peek().equals("IN_BATTLE")) {
            batch.draw(textbox, 50, 20, 1180, 200);

            font.setColor(currentDialog.character.equals("Echo") ? Color.RED : Color.YELLOW);
            font.draw(batch, currentDialog.character, 90, 180);

            // Ubah warna teks jadi gelap kalau backgroundnya putih (Ending)
            if (bgColor.r > 0.8f) font.setColor(Color.LIGHT_GRAY);
            else font.setColor(Color.WHITE);

            font.draw(batch, displayedText, 90, 130);
        }

        // --- UI BOSS BATTLE ---
        if (gameStateStack.peek().equals("IN_BATTLE")) {
            font.setColor(Color.WHITE);
            font.draw(batch, "--- BOSS FIGHT: ECHO ---", 500, 680);
            font.draw(batch, "PHASE: " + echoBoss.phase, 580, 640);

            // HP Bar Sederhana
            font.setColor(Color.RED);
            font.draw(batch, "ECHO HP: " + echoBoss.hp + "/" + echoBoss.maxHp, 520, 600);

            font.setColor(Color.CYAN);
            if(ayuPlayer.isArmorActive) font.draw(batch, "ARMOR AKTIF", 50, 680);
            font.draw(batch, "AYU HP: " + ayuPlayer.hp, 50, 640);

            font.setColor(Color.GRAY);
            font.draw(batch, "(Tekan SPASI untuk Simulasi Damage)", 450, 100);
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
