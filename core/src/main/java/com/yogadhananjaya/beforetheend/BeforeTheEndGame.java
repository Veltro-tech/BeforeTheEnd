package com.yogadhananjaya.beforetheend;

import com.badlogic.gdx.Game;
import com.yogadhananjaya.beforetheend.screens.MainMenuScreen;

public class BeforeTheEndGame extends Game {

    @Override
    public void create() {
        // Saat game pertama kali dibuka, langsung arahkan ke Main Menu
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // WAJIB ADA: untuk memanggil render() di Screen yang sedang aktif
    }

    @Override
    public void dispose() {
        // Bersihkan memori secara global di sini nanti
    }
}
