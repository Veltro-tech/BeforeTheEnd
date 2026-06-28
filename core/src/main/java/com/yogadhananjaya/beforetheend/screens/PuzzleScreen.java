package com.yogadhananjaya.beforetheend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.yogadhananjaya.beforetheend.BeforeTheEndGame;

import java.util.LinkedList;

public class PuzzleScreen extends ScreenAdapter {
    final BeforeTheEndGame game;

    // Referensi ke parent screen agar bisa kembali melanjutkan dialog
    final ChapterThreeScreen ch3Parent;
    final ChapterFourScreen ch4Parent;

    String puzzleType;

    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    // ====================================================================
    // --- 1. DATA STRUCTURE: TREE (Area 1: Ruang Arsip / Chapter 3) ---
    // Konsep: Root adalah "Tumpukan Utama", percabangan adalah dokumen
    // ====================================================================
    class TreeNode {
        String data;
        boolean isImportant;
        TreeNode left, right;

        public TreeNode(String data, boolean isImportant) {
            this.data = data;
            this.isImportant = isImportant;
        }
    }
    TreeNode arsipRoot;

    // ====================================================================
    // --- 2. DATA STRUCTURE: ARRAY & GRAPH (Koridor Ekspektasi / Chapter 3) ---
    // Konsep: Node merepresentasikan sumber suara yang saling terhubung (Matrix)
    // ====================================================================
    int[][] adjacencyMatrix;
    String[] voiceNodes;
    boolean[] isVoiceMuted;

    // ====================================================================
    // --- 3. DATA STRUCTURE: GRAPH (Area Memory Fragment / Chapter 4) ---
    // Konsep: Nodes adalah Titik kenangan (foto), Edge adalah jalan hubungnya
    // ====================================================================
    class MemoryGraph {
        int vertices;
        LinkedList<Integer>[] adjList;
        boolean[] isFakeMemory;

        public MemoryGraph(int vertices) {
            this.vertices = vertices;
            adjList = new LinkedList[vertices];
            isFakeMemory = new boolean[vertices];
            for (int i = 0; i < vertices; i++) {
                adjList[i] = new LinkedList<>();
            }
        }

        public void addEdge(int src, int dest) {
            adjList[src].add(dest);
        }
    }
    MemoryGraph memoryGraph;


    // --- CONSTRUCTOR ---
    public PuzzleScreen(BeforeTheEndGame game, ChapterThreeScreen ch3, ChapterFourScreen ch4, String puzzleType) {
        this.game = game;
        this.ch3Parent = ch3;
        this.ch4Parent = ch4;
        this.puzzleType = puzzleType;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        // Inisialisasi struktur data berdasarkan jenis puzzle yang dipanggil
        if (puzzleType.equals("TREE_ARSIP")) {
            setupTreePuzzle();
        } else if (puzzleType.equals("GRAPH_SUARA")) {
            setupGraphPuzzle();
        } else if (puzzleType.equals("GRAPH_MEMORY")) {
            setupMemoryGraph();
        }
    }

    // --- SETUP LOGIC ---
    private void setupTreePuzzle() {
        // Dummy data struktur Tree
        arsipRoot = new TreeNode("Revisi Bab 1", false);
        arsipRoot.left = new TreeNode("Ekspektasi Orang Tua", false);
        arsipRoot.right = new TreeNode("Catatan: Aku Takut Gagal", true); // Node Target
    }

    private void setupGraphPuzzle() {
        // Dummy data struktur Graph Matrix (3 sumber suara)
        voiceNodes = new String[]{"Suara Dosen", "Suara Teman", "Suara Diri Sendiri"};
        isVoiceMuted = new boolean[]{false, false, false};
        adjacencyMatrix = new int[][]{
            {0, 1, 1}, // Suara Dosen terhubung ke Teman & Diri Sendiri
            {1, 0, 0},
            {1, 0, 0}
        };
    }

    private void setupMemoryGraph() {
        // Dummy data Graph LinkedList (5 titik memori)
        memoryGraph = new MemoryGraph(5);
        memoryGraph.addEdge(0, 1); // Memori asli
        memoryGraph.addEdge(1, 2); // Jebakan memori palsu
        memoryGraph.addEdge(1, 3); // Memori asli
        memoryGraph.addEdge(3, 4); // Jebakan memori palsu

        memoryGraph.isFakeMemory[2] = true;
        memoryGraph.isFakeMemory[4] = true;
    }

    // --- RENDER & GAMEPLAY ---
    @Override
    public void render(float delta) {
        // LOGIKA PENYELESAIAN SEMENTARA (Klik sembarang tempat untuk Selesai)
        if (Gdx.input.justTouched()) {
            System.out.println("Puzzle " + puzzleType + " Selesai!");

            // Cek darimana puzzle ini dipanggil, lalu kembalikan ke layar tersebut
            if (ch4Parent != null) {
                ch4Parent.resumeStory();
                game.setScreen(ch4Parent);
            } else if (ch3Parent != null) {
                ch3Parent.resumeStory();
                game.setScreen(ch3Parent);
            }
            dispose(); // Bersihkan memori puzzle ini
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "=== MINI GAME PUZZLE ===", 480, 500);

        // Render UI sementara berdasarkan tipe puzzle
        if (puzzleType.equals("TREE_ARSIP")) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "Mekanik: Cari Node (Tree) Dokumen Penting", 350, 400);
        } else if (puzzleType.equals("GRAPH_SUARA")) {
            font.setColor(Color.CYAN);
            font.draw(batch, "Mekanik: Putus Koneksi Edge (Graph) Sumber Suara", 320, 400);
        } else if (puzzleType.equals("GRAPH_MEMORY")) {
            font.setColor(Color.ORANGE);
            font.draw(batch, "Mekanik: Navigasi Graph Memori (Hindari Kenangan Palsu)", 250, 400);
        }

        font.setColor(Color.GRAY);
        font.draw(batch, "(Klik di mana saja untuk menyelesaikan puzzle)", 350, 300);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
