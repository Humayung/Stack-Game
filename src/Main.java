import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Main extends PApplet {
    public Main() {
        startColor = color(random(256), random(256), random(256));
        endColor = color(random(256), random(256), random(256));
        initialTileBounds = new PVector(350, 350, 40);
        tileBounds = initialTileBounds.copy();
        rubbles = new ArrayList<>();
        oscillatingSpeed = 0.03f;
        MAX_TILES_ONSCREEN = 17;
        playButtonAlpha = 255;
        minimumCombo = 0;
        ERROR_MARGIN = 10;
        desiredScale = 1f;
        scale = 0.1f;
    }

    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    public void settings() {
        fullScreen(P3D);
    }

    private ArrayList<Tile> tiles;
    private ArrayList<Tile> displayTiles;
    private ArrayList<Tile> rubbles;

    private Waves waves;
    private final int ERROR_MARGIN;
    private final int MAX_TILES_ONSCREEN;

    private float scale;
    private float desiredScale;
    private int bgColor;
    private float playButtonAlpha;

    private PVector initialTileBounds;
    private PVector tileBounds;
    private ParticleSystem particleSystem;


    private Tile oscillatingTile;
    private Tile topTile;

    private int minimumCombo;
    private int score;
    private int highScore;
    private int combo;
    private boolean gameOver;
    private boolean gameStarted;

    private float cameraZ;
    private float desiredCameraZ;
    private boolean isMovingOnX;

    private int colorPos;
    private int startColor;
    private int endColor;


    private float time;
    private float oscillatingSpeed;

    public void setup() {
        ortho(-width / 2, width / 2, -height / 2, height / 2, -width, width);
        particleSystem = new ParticleSystem();
        waves = new Waves();
        textAlign(CENTER);
        textSize(60);
        newGame();
    }

    public void draw() {
        background(bgColor);
        scale = lerp(scale, desiredScale, 0.1f);
        translate(width / 2, height / 2);
        lights();

        pushMatrix();
        {
            rotateX(QUARTER_PI);
            rotateZ(QUARTER_PI);
            cameraZ = lerp(cameraZ, desiredCameraZ, 0.08f);
            scale(scale);
            translate(0, 0, cameraZ);

            oscillatingTile.update();
            displayTiles.forEach(Tile::update);
            manageRubbles();
            waves.update();
            particleSystem.run();
        }
        popMatrix();
        if (gameStarted) oscillate();
        drawUI();
    }


    private void drawUI() {
        // Play Button, Score, and HighScore
        if (gameStarted) {
            fill(255);
            text(score, 0, -height / 4, 250);
        } else {
            fill(255, 200, 0);
            text(highScore, 0, (height / 4) + 100, 250);
        }
        drawPlayButton(0, 0, 250, height / 4);
    }

    private void newGame() {
        bgColor = color(
                random(150, 256),
                random(150, 256),
                random(150, 256)
        );
        startColor = color(
                random(256),
                random(256),
                random(256)
        );
        endColor = color(
                random(256),
                random(256),
                random(256)
        );

        tiles = new ArrayList<>();
        displayTiles = new ArrayList<>();
        tileBounds = initialTileBounds.copy();
        topTile = new Tile(new PVector(), tileBounds, getNextColor());
        tiles.add(topTile);
        displayTiles.add(topTile);
        oscillatingTile = new Tile(new PVector(0, 0, tileBounds.z), tileBounds, getNextColor());
        desiredCameraZ = 0;
        highScore = (highScore < score)
                ? score
                : highScore;
        score = 0;
        oscillatingSpeed = .03f;
    }


    @Override
    public void keyPressed() {
        if (key == ' ') {
            if (gameStarted) {
                if (gameOver) {
                    gameOver = false;
                    gameStarted = false;
                    desiredScale = 1;
                    newGame();
                } else {
                    if (!placeBox()) {
                        desiredScale = min(0.8f, sqrt(tiles.size()) / (tiles.size() / 2f));
                        displayTiles = tiles;
                        gameOver = true;
                    } else oscillatingSpeed += (sqrt(tiles.size()) / sq(tiles.size())) / 300f;
                }

            } else {
                gameStarted = true;
                gameOver = false;
            }
        }
    }

    private void drawPlayButton(float x, float y, float z, float size) {
        playButtonAlpha = lerp(playButtonAlpha, gameStarted
                        ? 0
                        : 255,
                0.1f);
        if (playButtonAlpha > 0.1) {
            // Centroid
            float cx = 0.25f;
            float cy = 0.5f;
            //
            pushMatrix();
            {
                translate(x - (cx * size), y - (cy * size), z);
                scale(size);

                // Perimeter
                noFill();
                stroke(0, playButtonAlpha);
                strokeWeight(0.15f);
                ellipse(cx, cy, 1.5f, 1.5f);
                //

                noStroke();
                fill(0, playButtonAlpha);
                beginShape();
                {
                    // Triangle
                    vertex(0, 0);
                    vertex(0, 1);
                    vertex(sqrt(0.75f), 0.5f);
                }
                endShape();
                fill(255, 50, 10);
            }
            popMatrix();
        }
    }

    private void manageRubbles() {
        for (int i = rubbles.size() - 1; i >= 0; i--) {
            Tile r = rubbles.get(i);
            if (r.off) {
                rubbles.remove(i);
            } else {
                r.update();
            }
        }
    }

    private boolean placeBox() {
        if (isMovingOnX) {
            combo = 0;
            float deltaX = topTile.pos.x - oscillatingTile.pos.x;
            if (abs(deltaX) > ERROR_MARGIN) {

                // Cut tile
                tileBounds.x -= abs(deltaX);
                if (tileBounds.x < 0) {
                    rubbles.add(oscillatingTile);
                    oscillatingTile.dissolve();
                    return false;
                }
                float rubbleX = oscillatingTile.pos.x + (deltaX > 0 ? -1 : 1) * tileBounds.x / 2;
                PVector rubbleScale = new PVector(deltaX, tileBounds.y, tileBounds.z);
                Tile rubble = new Tile(new PVector(rubbleX, oscillatingTile.pos.y, oscillatingTile.pos.z), rubbleScale, oscillatingTile.color);
                rubbles.add(rubble);
                rubble.dissolve();
                score++;
                float middle = topTile.pos.x + (oscillatingTile.pos.x / 2);
                oscillatingTile.setScale(tileBounds);
                oscillatingTile.setPos(middle - (topTile.pos.x / 2), topTile.pos.y, oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                waves.init(oscillatingTile.pos, tileBounds);
                combo++;
                score += ERROR_MARGIN - abs(deltaX);
                if (combo >= minimumCombo) {
                    oscillatingTile.scaleUp(10, 0);
                    tileBounds.add(10, 0);
                }
            }
        } else {
            combo = 0;
            float deltaY = topTile.pos.y - oscillatingTile.pos.y;
            if (abs(deltaY) > ERROR_MARGIN) {

                // Cut Tile
                tileBounds.y -= abs(deltaY);
                if (tileBounds.y < 0) {
                    rubbles.add(oscillatingTile);
                    oscillatingTile.dissolve();
                    return false;
                }
                float rubbleY = oscillatingTile.pos.y + (deltaY > 0 ? -1 : 1) * tileBounds.y / 2;
                PVector rubbleScale = new PVector(tileBounds.x, deltaY, tileBounds.z);
                Tile rubble = new Tile(new PVector(oscillatingTile.pos.x, rubbleY, oscillatingTile.pos.z), rubbleScale, oscillatingTile.color);
                rubble.dissolve();
                rubbles.add(rubble);
                score++;
                float middle = topTile.pos.y + (oscillatingTile.pos.y / 2);
                oscillatingTile.setScale(tileBounds);
                oscillatingTile.setPos(topTile.pos.x, middle - (topTile.pos.y / 2), oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                waves.init(oscillatingTile.pos, tileBounds);
                combo++;
                score += ERROR_MARGIN - abs(deltaY);
                if (combo >= minimumCombo) {
                    oscillatingTile.scaleUp(0, 10);
                    tileBounds.add(0, 10);
                }
            }
        }

        // Spawn Tile
        topTile = oscillatingTile;
        tiles.add(oscillatingTile);
        displayTiles.add(oscillatingTile);
        if (displayTiles.size() > MAX_TILES_ONSCREEN) {
            displayTiles.remove(0);
        }
        oscillatingTile = new Tile(oscillatingTile.pos.copy().add(0, 0, tileBounds.z), tileBounds, getNextColor());
        desiredCameraZ -= tileBounds.z;
        isMovingOnX = !isMovingOnX;
        time = -HALF_PI;
        oscillate();
        return true;
    }

    private int getNextColor() {
        final int colorRange = 12;
        colorPos = (colorPos + 1) % colorRange;
        if (colorPos == 0) {
            startColor = endColor;
            endColor = color(random(256), random(256), random(256));
        }
        float amt = (float) colorPos / colorRange;
        return lerpColor(startColor, endColor, amt);
    }

    private void oscillate() {
        if (!gameOver) {
            time += oscillatingSpeed;
            if (isMovingOnX)
                oscillatingTile.pos.x = sin(time) * (initialTileBounds.x + 100);
            else
                oscillatingTile.pos.y = sin(time) * (initialTileBounds.y + 100);
        }
    }

    /*
    private void drawOrigin(PVector pos, float length) {
        pushMatrix();
        {
            translate(pos.x, pos.y, pos.z);
            point(0, 0, 0);
            stroke(0, 0, 255);
            line(0, 0, 0, length, 0, 0);
            stroke(0, 255, 0);
            line(0, 0, 0, 0, length, 0);
            stroke(255, 0, 0);
            line(0, 0, 0, 0, 0, length);
        }
        popMatrix();
    }


    void drawOrigin() {
        drawOrigin(new PVector(), 1000);
    }
    */

    class Tile {
        private PVector rot;
        private PVector aVel;
        float desiredAlpha = 255;
        PVector desiredScale;
        PVector desiredPos;
        PVector scale;
        PVector pos;
        PVector vel;

        boolean dissolve;
        float alpha = 0;
        int color;
        boolean off;

        Tile(PVector pos, PVector scale, int color) {
            this.desiredScale = scale.copy();
            this.desiredPos = pos.copy();
            this.scale = scale.copy();
            this.pos = pos.copy();
            this.color = color;

            aVel = PVector.random3D().mult(0.01f);
            rot = new PVector();
            vel =  PVector.random3D().mult(0.08f);
        }

        void update() {
            // Adjust position to the scale changes
            PVector pScale = scale.copy();
            scale.lerp(desiredScale, 0.2f);
            pos.add(PVector.sub(scale, pScale).mult(0.5f));
            if (alpha < 0.1) off = true;
            alpha = lerp(alpha, desiredAlpha, 0.04f);
            if(dissolve) {
                rot.add(aVel);
                pos.add(vel);
            }

            display();
        }

        void display() {
            noStroke();
            fill(color, alpha);

            // Drawing tile
            pushMatrix();
            {
                translate(pos.x - (scale.x / 2), pos.y - (scale.y / 2), pos.z - (scale.z / 2));
                rotateX(rot.x);
                rotateY(rot.y);
                rotateZ(rot.z);
                beginShape(QUAD);
                {
                    vertex(0, 0, 0);
                    vertex(scale.x, 0, 0);
                    vertex(scale.x, scale.y, 0);
                    vertex(0, scale.y, 0);

                    vertex(0, 0, scale.z);
                    vertex(scale.x, 0, scale.z);
                    vertex(scale.x, scale.y, scale.z);
                    vertex(0, scale.y, scale.z);

                    vertex(0, 0, 0);
                    vertex(0, 0, scale.z);
                    vertex(0, scale.y, scale.z);
                    vertex(0, scale.y, 0);

                    vertex(scale.x, 0, 0);
                    vertex(scale.x, 0, scale.z);
                    vertex(scale.x, scale.y, scale.z);
                    vertex(scale.x, scale.y, 0);

                    vertex(0, 0, 0);
                    vertex(0, 0, scale.z);
                    vertex(scale.x, 0, scale.z);
                    vertex(scale.x, 0, 0);

                    vertex(0, scale.y, 0);
                    vertex(0, scale.y, scale.z);
                    vertex(scale.x, scale.y, scale.z);
                    vertex(scale.x, scale.y, 0);
                }
                endShape();
            }
            popMatrix();
        }

        private void scaleUp(float x, float y) {
            desiredScale.add(x, y, 0);
        }

        private void setScale(PVector scale) {
            this.desiredScale = scale.copy();
            this.scale = scale.copy();
        }

        private void setPos(float x, float y, float z) {
            this.pos = new PVector(x, y, z);
        }

        public void dissolve() {
            desiredAlpha = 0;
            alpha = 255;
            dissolve = true;
        }
    }

    class Waves {
        ArrayList<Rect> waves = new ArrayList<>();

        void init(PVector pos, PVector scale) {
            waves.add(new Rect(pos, scale));
        }

        void update() {
            waves.forEach(Rect::update);
            for (int i = waves.size() - 1; i >= 0; i--) {
                Rect r = waves.get(i);
                if (r.off) waves.remove(i);
                else r.update();
            }
        }

        class Rect {
            PVector desiredScale;
            float lifespan;
            PVector scale;
            PVector pos;
            boolean off;

            Rect(PVector pos, PVector scale) {
                this.desiredScale = scale.copy().add(scale.copy().mult(0.3f));
                this.scale = scale.copy();
                this.pos = pos.copy();
                lifespan = 255;
            }

            void display() {
                pushMatrix();
                {
                    translate(pos.x - (scale.x / 2), pos.y - (scale.y / 2), pos.z);
                    fill(255, 200, 0, lifespan);
                    beginShape(QUAD);
                    {
                        vertex(0, 0, 0);
                        vertex(scale.x, 0, 0);
                        vertex(scale.x, scale.y, 0);
                        vertex(0, scale.y, 0);
                    }
                    endShape();
                }
                popMatrix();
            }

            void update() {
                lifespan = lerp(lifespan, 0, 0.1f);
                scale.lerp(desiredScale, 0.1f);
                if (lifespan < 0.01f) off = true;
                display();
            }
        }
    }

    class ParticleSystem {
        ArrayList<Particle> particles = new ArrayList<>();

        void run() {
            if (random(1) < 0.05) {
                particles.add(new Particle(
                        random(-width / 2, width / 2) - 300,
                        random(-height / 2, height / 2) - 300,
                        random(-200, 200) - cameraZ)
                );
            }
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                if (p.off) particles.remove(i);
                else p.update();
            }
        }

    }

    class Particle {
        PVector rot;
        PVector aVel;
        PVector pos;

        boolean off;

        float alpha;
        float time;
        int color;

        Particle(float x, float y, float z) {
            color = color(
                    random(150, 256),
                    random(150, 256),
                    random(150, 256)
            );
            aVel = PVector.random3D().mult(0.01f);
            pos = new PVector(x, y, z);
            rot = PVector.random3D();
        }


        void update() {
            alpha = (-cos(time) * 128) + 128;
            pos.add(0, 0, 0.3f);
            rot.add(aVel);
            time += 0.01;
            if (time > TWO_PI) {
                off = true;
            }
            display();
        }

        void display() {
            pushMatrix();
            {
                translate(pos.x, pos.y, pos.z);
                fill(color, alpha);
                rotateX(rot.x);
                rotateY(rot.y);
                rotateZ(rot.z);
                box(10);
            }
            popMatrix();
        }
    }
}
