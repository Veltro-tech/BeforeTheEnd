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
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;
import com.yogadhananjaya.beforetheend.screens.Chapter_One_OneScreen;
import com.yogadhananjaya.beforetheend.screens.DialogLine;

import java.util.LinkedList;
import java.util.Queue;

public class Chapter_Two_Loop2Screen extends ScreenAdapter {
    final BeforeTheEndGame game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout glyphLayout = new GlyphLayout();

    Texture bgKamar, bgKamarTidur, bgDapur, bgKM;
    Texture activeBg;
    Texture ibuSprite;
    Texture textbox;
    Texture blackTexture;
    Texture whiteTexture;

    TextureRegion ayuIdleFrame;
    Animation<TextureRegion> walkRightAnim, walkLeftAnim, idleAnim;

    float ayuX = 483f, ayuY = 180f;
    boolean isWalking = false;
    boolean walkDirectionRight = true;
    float walkTime;
    float idleTime;

    private enum SubScene {
        SCENE_8A_KAMAR,
        SCENE_8A_EXPLORE,
        SCENE_8A_TRANSITION,
        SCENE_8B_DAPUR_DIALOG,
        SCENE_8B_EXPLORE,
        SCENE_8B_EAT,
        SCENE_8C_IBU_DIALOG,
        SCENE_8D_PERUT_SAKIT,
        SCENE_8D_FADE
    }

    SubScene currentSubScene = SubScene.SCENE_8A_KAMAR;

    Queue<DialogLine> dialogQueue;
    DialogLine currentDialog;
    String targetText = "";
    String displayedText = "";
    float textTimer = 0f;
    float typeSpeed = 0.04f;
    int charIndex = 0;

    float stateTime;
    float subsceneTimer;
    final float WORLD_WIDTH = 1920f;
    final float WORLD_HEIGHT = 1080f;

    public Chapter_Two_Loop2Screen(final BeforeTheEndGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        bgKamar = new Texture("backgrounds/kamar-ayu-6.png");
        bgKamarTidur = loadTexture("backgrounds/Chapter_2/kamar-ayu-tidur.png");
        bgDapur = loadTexture("backgrounds/dapur-5.png");
        bgKM = loadTexture("backgrounds/scene-4.png");
        activeBg = bgKamarTidur;
        textbox = new Texture("ui/textbox.png");
        ibuSprite = loadTexture("character/ibu-ayu/diam.png");

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK);
        pm.fill();
        blackTexture = new Texture(pm);
        pm.setColor(Color.WHITE);
        pm.fill();
        whiteTexture = new Texture(pm);
        pm.dispose();

        setupAnimations();
        setupDialogQueue();
    }

    private Texture loadTexture(String path) {
        if (Gdx.files.internal(path).exists()) return new Texture(path);
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.MAGENTA);
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private void setupAnimations() {
        Texture idleTex = new Texture("character/Ayu/diam-2.png");
        ayuIdleFrame = new TextureRegion(idleTex);

        Array<TextureRegion> rightFrames = new Array<>();
        for (int i = 0; i <= 12; i++) {
            String p = "character/Ayu/ayu-yoga-jalan/frame_" + (i < 10 ? "0" : "") + i + "_delay-0.11s.png";
            if (Gdx.files.internal(p).exists())
                rightFrames.add(new TextureRegion(new Texture(p)));
        }
        if (rightFrames.size > 0)
            walkRightAnim = new Animation<>(0.11f, rightFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> leftFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            if (Gdx.files.internal("character/Ayu/jalan_kiri_" + i + ".png").exists())
                leftFrames.add(new TextureRegion(new Texture("character/Ayu/jalan_kiri_" + i + ".png")));
        }
        if (leftFrames.size > 0)
            walkLeftAnim = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> idleFrames = new Array<>();
        int totalIdleFrames = 229;
        int maxIdleFrames = 30;
        int step = Math.max(1, totalIdleFrames / maxIdleFrames);
        for (int i = 1; i <= totalIdleFrames; i += step) {
            String p = "character/Ayu/ayu-yoga/Ayu_yoga_" + i + ".png";
            if (Gdx.files.internal(p).exists())
                idleFrames.add(new TextureRegion(new Texture(p)));
            if (idleFrames.size >= maxIdleFrames) break;
        }
        if (idleFrames.size > 0) {
            float idleFrameDuration = 0.08f * step;
            idleAnim = new Animation<>(idleFrameDuration, idleFrames, Animation.PlayMode.LOOP);
        }
    }

    private void setupDialogQueue() {
        dialogQueue = new LinkedList<>();
        dialogQueue.add(new DialogLine("Narator", "(Beep, Beep, Beep) Ayu terbangun dan tersadar..."));
        dialogQueue.add(new DialogLine("Sistem", "EXPLORE_KAMAR"));
        dialogQueue.add(new DialogLine("Ayu", "Aku masih di kamar? Ternyata semua cuma mimpi..."));
        dialogQueue.add(new DialogLine("Ayu", "Hmm, masih jam 6 . Tidur lagi apa enggak ya?"));
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
        }
    }

    private void handleSystemTrigger(String command) {
        if (command.equals("EXPLORE_KAMAR")) {
            currentSubScene = SubScene.SCENE_8A_EXPLORE;
            activeBg = bgKamar;
        } else if (command.equals("TO_DAPUR")) {
            currentSubScene = SubScene.SCENE_8A_TRANSITION;
            subsceneTimer = 0f;
        } else if (command.equals("START_DAPUR")) {
            currentSubScene = SubScene.SCENE_8B_DAPUR_DIALOG;
            activeBg = bgDapur;
            ayuX = 100f;
            ayuY = 126f;
            addDapurDialogs();
        } else if (command.equals("DAPUR_EXPLORE")) {
            currentSubScene = SubScene.SCENE_8B_EXPLORE;
        } else if (command.equals("EAT_FOOD")) {
            currentSubScene = SubScene.SCENE_8B_EAT;
        } else if (command.equals("IBU_MASUK")) {
            currentSubScene = SubScene.SCENE_8C_IBU_DIALOG;
            addIbuDialogs();
        } else if (command.equals("PERUT_SAKIT")) {
            currentSubScene = SubScene.SCENE_8D_PERUT_SAKIT;
            addPerutSakitDialogs();
        } else if (command.equals("FADE_OUT")) {
            currentSubScene = SubScene.SCENE_8D_FADE;
            subsceneTimer = 0f;
        }
    }

    private void addDapurDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "DAPUR_EXPLORE"));
        list.addFirst(new DialogLine("Ayu", "Wah ada apel di meja! Lumayan buat ganjal perut."));
        list.addFirst(new DialogLine("Ayu", "Aduh, lapar banget. Coba cek dapur, siapa tau ada makanan."));
    }

    private void addIbuDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "PERUT_SAKIT"));
        list.addFirst(new DialogLine("Narator", "Ayu panik bukan main. Sedetik kemudian..."));
        list.addFirst(new DialogLine("IBU AYU", "ASTAGA! Makanan itu udah basi, Ayu! Harusnya Ibu buang tadi!"));
        list.addFirst(new DialogLine("Narator", "Tiba-tiba Ibu datang ke dapur dan terkejut."));
    }

    private void addPerutSakitDialogs() {
        LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
        list.addFirst(new DialogLine("Sistem", "FADE_OUT"));
        list.addFirst(new DialogLine("Narator", "Makin lama ia berjalan, matanya mulai kabur..."));
        list.addFirst(new DialogLine("Narator", "Yang terdengar hanya suara sang Ibu:"));
        list.addFirst(new DialogLine("IBU AYU", "Ayu bangun... Ayu bangun! Jangan tinggalin Ibu!"));
        list.addFirst(new DialogLine("Narator",
                "Ayu bergegas ke kamar mandi bersama Ibu. Ia sudah tak bisa merasakan kakinya."));
        list.addFirst(new DialogLine("Narator", "Rasa sakit seperti ditusuk-tusuk jarum menyerang perut Ayu."));
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
        if (Gdx.input.justTouched() && currentDialog != null) {
            if (charIndex < targetText.length()) {
                displayedText = targetText;
                charIndex = targetText.length();
            } else {
                dequeueNextDialog();
            }
        }

        if (currentSubScene == SubScene.SCENE_8A_EXPLORE) {
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
            if (ayuX >= WORLD_WIDTH - 300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "TO_DAPUR"));
                dequeueNextDialog();
            }
        }

        if (currentSubScene == SubScene.SCENE_8B_EXPLORE) {
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
            if (ayuX >= 1100f && ayuX <= 1300f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "IBU_MASUK"));
                list.addFirst(
                        new DialogLine("Ayu", "Beberapa makanan ini kok rasanya agak aneh ya? Tapi bodo amat lah."));
                list.addFirst(new DialogLine("Narator", "Ayu memakan apel itu dengan lahap."));
                list.addFirst(new DialogLine("Sistem", "EAT_FOOD"));
                dequeueNextDialog();
            }
        }
    }

    private void drawCharacter(float delta) {
        float drawHeight = 361f * 1.105f;
        TextureRegion currentFrame = null;
        if (isWalking) {
            walkTime += delta;
            if (walkDirectionRight && walkRightAnim != null)
                currentFrame = walkRightAnim.getKeyFrame(walkTime);
            else if (!walkDirectionRight && walkLeftAnim != null)
                currentFrame = walkLeftAnim.getKeyFrame(walkTime);
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
        if (currentFrame.getRegionHeight() > 0)
            drawWidth = drawHeight * ((float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight());
        batch.draw(currentFrame, ayuX, ayuY, drawWidth, drawHeight);
    }

    private void renderHint(String hint) {
        if (hint == null)
            return;
        float hintW = 400f, hintH = 55f;
        float hintX = ayuX + 99f - hintW / 2f;
        float hintY = ayuY + 361f + 20f;
        if (hintX < 10f)
            hintX = 10f;
        if (hintX + hintW > WORLD_WIDTH - 10f)
            hintX = WORLD_WIDTH - 10f - hintW;
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

    private void renderDialogBox() {
        if (currentDialog == null)
            return;
        float bubbleW = 650f, bubbleH = 180f, bubbleX, bubbleY;

        if ("Narator".equalsIgnoreCase(currentDialog.character)) {
            bubbleW = 900f;
            bubbleH = 150f;
            bubbleX = WORLD_WIDTH / 2f - bubbleW / 2f;
            bubbleY = WORLD_HEIGHT - bubbleH - 50f;
        } else if ("IBU AYU".equalsIgnoreCase(currentDialog.character)) {
            bubbleX = WORLD_WIDTH - bubbleW - 50f;
            bubbleY = ayuY + 440f;
        } else {
            bubbleX = ayuX + 99f - bubbleW / 2f;
            bubbleY = ayuY + 380f;
        }

        if (bubbleX < 20f)
            bubbleX = 20f;
        if (bubbleX + bubbleW > WORLD_WIDTH - 20f)
            bubbleX = WORLD_WIDTH - 20f - bubbleW;

        batch.draw(blackTexture, bubbleX, bubbleY, bubbleW, bubbleH);
        batch.setColor(Color.GOLD);
        batch.draw(whiteTexture, bubbleX, bubbleY + bubbleH - 3f, bubbleW, 3f);
        batch.draw(whiteTexture, bubbleX, bubbleY, bubbleW, 3f);
        batch.draw(whiteTexture, bubbleX, bubbleY, 3f, bubbleH);
        batch.draw(whiteTexture, bubbleX + bubbleW - 3f, bubbleY, 3f, bubbleH);
        batch.setColor(Color.WHITE);

        if (!"Narator".equalsIgnoreCase(currentDialog.character)) {
            font.getData().setScale(1.4f);
            font.setColor(Color.GOLD);
            font.draw(batch, currentDialog.character, bubbleX + 25f, bubbleY + bubbleH - 20f);
        }
        font.setColor(Color.WHITE);
        float textY = "Narator".equalsIgnoreCase(currentDialog.character) ? bubbleY + bubbleH - 35f
                : bubbleY + bubbleH - 60f;
        font.draw(batch, displayedText, bubbleX + 25f, textY, bubbleW - 50f, com.badlogic.gdx.utils.Align.left, true);
        if (charIndex >= targetText.length() && (int) (stateTime * 2) % 2 == 0) {
            batch.setColor(Color.GOLD);
            batch.draw(whiteTexture, bubbleX + bubbleW - 36f, bubbleY + 20f, 12f, 3f);
            batch.draw(whiteTexture, bubbleX + bubbleW - 33f, bubbleY + 17f, 6f, 3f);
            batch.draw(whiteTexture, bubbleX + bubbleW - 30f, bubbleY + 14f, 3f, 3f);
            batch.setColor(Color.WHITE);
        }
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        subsceneTimer += delta;

        if (currentSubScene == SubScene.SCENE_8A_TRANSITION) {
            if (subsceneTimer >= 2.0f) {
                LinkedList<DialogLine> list = (LinkedList<DialogLine>) dialogQueue;
                list.addFirst(new DialogLine("Sistem", "START_DAPUR"));
                dequeueNextDialog();
            }
        } else if (currentSubScene == SubScene.SCENE_8D_FADE) {
            if (subsceneTimer >= 3.0f) {
                game.setScreen(new Chapter_Two_Loop3Screen(game));
                return;
            }
        } else if (currentSubScene != SubScene.SCENE_8A_TRANSITION) {
            handleInput(delta);
            updateTypewriter(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (currentSubScene == SubScene.SCENE_8A_TRANSITION) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else if (currentSubScene == SubScene.SCENE_8D_FADE) {
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            float alpha = Math.min(1f, subsceneTimer / 2f);
            batch.setColor(0, 0, 0, alpha);
            batch.draw(blackTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
            if (subsceneTimer >= 2f) {
                font.setColor(Color.RED);
                font.draw(batch, "LOOP 2", WORLD_WIDTH / 2f - 100, WORLD_HEIGHT / 2f);
            }
        } else {
            batch.draw(activeBg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            if (currentSubScene == SubScene.SCENE_8C_IBU_DIALOG) {
                batch.draw(ibuSprite, 1400f, 100f, 198f * 1.05f, 334f * 1.05f);
            }
            if (activeBg != bgKamarTidur)
                drawCharacter(delta);
            renderDialogBox();

            if (currentSubScene == SubScene.SCENE_8A_EXPLORE && currentDialog == null) {
                String hint = ayuX >= WORLD_WIDTH - 300f ? "Tekan E untuk keluar" : "Coba ke kanan menuju dapur...";
                renderHint(hint);
            }
            if (currentSubScene == SubScene.SCENE_8B_EXPLORE && currentDialog == null) {
                String hint = (ayuX >= 1100f && ayuX <= 1300f) ? "Tekan E untuk Memakan Apel"
                        : "Pergi ke kanan menuju meja...";
                renderHint(hint);
            }
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bgKamar.dispose();
        bgKamarTidur.dispose();
        bgDapur.dispose();
        bgKM.dispose();
        textbox.dispose();
    }
}
