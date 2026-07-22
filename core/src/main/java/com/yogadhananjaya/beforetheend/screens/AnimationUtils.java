package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationUtils {

    public static Animation<TextureRegion> fromFiles(String folder, String prefix, String suffix,
                                                      int startIndex, int endIndex, float frameDuration,
                                                      Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = startIndex; i <= endIndex; i++) {
            String path = folder + "/" + prefix + i + suffix;
            if (Gdx.files.internal(path).exists()) {
                frames.add(new TextureRegion(new Texture(path)));
            }
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public static Animation<TextureRegion> fromHorizontalStrip(String sheetPath, int frameCount,
                                                                float frameDuration, Animation.PlayMode mode) {
        Texture sheet = new Texture(sheetPath);
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCount];
        System.arraycopy(tmp[0], 0, frames, 0, frameCount);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public static Animation<TextureRegion> fromGrid(String sheetPath, int cols, int rows,
                                                     float frameDuration, Animation.PlayMode mode) {
        Texture sheet = new Texture(sheetPath);
        int frameWidth = sheet.getWidth() / cols;
        int frameHeight = sheet.getHeight() / rows;
        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames.add(tmp[r][c]);
            }
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public static Animation<TextureRegion> fromRegions(TextureRegion[] frames, float frameDuration,
                                                        Animation.PlayMode mode) {
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    public static Animation<TextureRegion> fromStatic(String path) {
        TextureRegion[] frames = new TextureRegion[] { new TextureRegion(new Texture(path)) };
        return new Animation<>(1f, frames);
    }
}
