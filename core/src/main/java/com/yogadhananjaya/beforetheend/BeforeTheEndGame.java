package com.yogadhananjaya.beforetheend;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.yogadhananjaya.beforetheend.screens.MainMenuScreen;

public class BeforeTheEndGame extends Game {

    // Deklarasi SpriteBatch secara publik agar bisa diakses oleh screen lain
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Panggil MainMenuScreen sebagai layar pembuka
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // WAJIB ADA: Berfungsi untuk mengeksekusi metode render() di Screen yang sedang aktif
        super.render();
    }

    @Override
    public void dispose() {
        // Bersihkan memori secara global di sini
        if (batch != null) {
            batch.dispose();
        }
    }
}
