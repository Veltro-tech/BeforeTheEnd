package com.yogadhananjaya.beforetheend.screens; // Sesuaikan dengan foldermu

// Class ini murni cuma buat nampung data dari JSON
public class DialogLine {
    public String character;
    public String text;

    // LibGDX butuh constructor kosong untuk baca JSON
    public DialogLine() {}

    public DialogLine(String character, String text) {
        this.character = character;
        this.text = text;
    }
}
