import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Main extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    public void settings() {
        fullScreen(P3D);
    }

    private ArrayList<Tile> tiles;
    private ArrayList<Tile> displayTiles;
    private Waves waves;
    private final int ERROR_MARGIN = 10;
    private final int MAX_TILES_ONSCREEN = 17;

    private float scale = 0.1f;
    private float desiredScale = 1f;
    private int bgColor;
    private int combo = 0;

    private PVector tileBounds;
    private PVector initialStackBounds;
    private ParticleSystem particleSystem;

    public void setup() {
        particleSystem = new ParticleSystem();
        tileBounds = new PVector(350, 350, 40);
        initialStackBounds = tileBounds.copy();
        waves = new Waves();
        newGame();
        ortho(-width / 2, width / 2, -height / 2, height / 2, -width, width);
        textSize(60);
        textAlign(CENTER);
    }

    private Tile oscillatingTile;
    private Tile topTile;

    public void draw() {
        background(bgColor);
        scale = lerp(scale, desiredScale, 0.1f);
        lights();
        translate(width / 2, height / 2);

        pushMatrix();
        {
            rotateX(QUARTER_PI);
            rotateZ(QUARTER_PI);
            cameraZ = lerp(cameraZ, desiredCameraZ, 0.08f);
            scale(scale);
            translate(0, 0, cameraZ);

            oscillatingTile.update();
            for (Tile tile : displayTiles) {
                tile.update();
            }
            waves.update();
            particleSystem.run();
        }
        popMatrix();

        if (gameStarted) {
            oscillate();
        }
        drawUI();
    }


    void drawUI(){

        if(gameStarted){
            fill(255);
            text(score, 0, -height / 4, 250);
        }else{
            fill(255, 200, 0);
            text(highScore, 0, height / 4 + 100, 250);
        }
        drawPlayButton(0, 0, 250, height / 4);
    }

    private void newGame() {
        bgColor = color(random(150, 256), random(150, 256), random(150, 256));
        startColor = color(random(256), random(256), random(256));
        endColor = color(random(256), random(256), random(256));

        tiles = new ArrayList<>();
        displayTiles = new ArrayList<>();
        tileBounds = initialStackBounds.copy();

        topTile = new Tile(new PVector(), tileBounds, getNextColor());

        tiles.add(topTile);
        displayTiles.add(topTile);
        oscillatingTile = new Tile(new PVector(0, 0, tileBounds.z), tileBounds, getNextColor());
        desiredCameraZ = 0;
        if(highScore < score) highScore = score;
        score = 0;
        oscillatingSpeed = 0.03f;
    }

    private int score = 0;
    private int highScore = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;

    public void keyPressed() {
        if (key == ' ') {
            if (gameStarted) {
                if (!gameOver) {
                    if (!placeBox()) {
                        desiredScale = min(0.8f, sqrt(tiles.size()) / (tiles.size() / 2f));
                        displayTiles = tiles;
                        gameOver = true;
                    } else {
                        score++;
                        oscillatingSpeed += (sqrt(tiles.size()) / sq(tiles.size())) / 300.0f;
                    }
                } else {
                    gameOver = false;
                    gameStarted = false;
                    desiredScale = 1;
                    newGame();
                }

            } else {
                gameStarted = true;
                gameOver = false;
            }
        }
    }

    private float playButtonAlpha = 255;

    private void drawPlayButton(float x, float y, float z, float size) {
        playButtonAlpha = lerp(playButtonAlpha, gameStarted ? 0 : 255, 0.1f);
        if (playButtonAlpha > 0.1) {
            // Centroid
            float cx = 0.25f;
            float cy = 0.5f;
            /**/
            pushMatrix();
            {
                translate(x - cx * size, y - cy * size, z);
                scale(size);

                // Perimeter
                noFill();
                stroke(0, playButtonAlpha);
                strokeWeight(0.15f);
                ellipse(cx, cy, 1.5f, 1.5f);
                /**/

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

    private float cameraZ = 0;
    private float desiredCameraZ = 0;
    private boolean isMovingOnX = false;

    private boolean placeBox() {
        if (isMovingOnX) {
            combo = 0;
            float deltaX = abs(topTile.pos.x - oscillatingTile.pos.x);
            if (deltaX > ERROR_MARGIN) {

                // Cut tile
                tileBounds.x -= deltaX;
                if (tileBounds.x < 0) return false;
                float middle = topTile.pos.x + oscillatingTile.pos.x / 2;
                oscillatingTile.setScale(tileBounds);
                oscillatingTile.setPos(middle - (topTile.pos.x / 2), topTile.pos.y, oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                waves.init(oscillatingTile.pos, tileBounds);
                combo++;
                score += deltaX;
                if (combo >= 10) {
                    oscillatingTile.scaleUp(10, 0);
                    tileBounds.add(10, 0);
                }
            }
        } else {
            combo = 0;
            float deltaY = abs(topTile.pos.y - oscillatingTile.pos.y);
            if (deltaY > ERROR_MARGIN) {

                // Cut Tile
                tileBounds.y -= deltaY;
                if (tileBounds.y < 0) return false;
                float middle = topTile.pos.y + oscillatingTile.pos.y / 2;
                oscillatingTile.setScale(tileBounds);
                oscillatingTile.setPos(topTile.pos.x, middle - (topTile.pos.y / 2), oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                waves.init(oscillatingTile.pos, tileBounds);
                combo++;
                score += deltaY;
                if (combo >= 10) {
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
        time = -HALF_PI;
        isMovingOnX = !isMovingOnX;
        oscillate();
        return true;
    }

    private int count = 0;
    private int startColor = color(random(256), random(256), random(256));
    private int endColor = color(random(256), random(256), random(256));

    private int getNextColor() {
        final int colorRange = 12;
        count = (count + 1) % colorRange;
        if (count == 0) {
            startColor = endColor;
            endColor = color(random(256), random(256), random(256));
        }
        float amt = (float) count / colorRange;
        return lerpColor(startColor, endColor, amt);
    }

    private float time = 0;
    private float oscillatingSpeed = 0.03f;

    private void oscillate() {
        if (!gameOver) {
            time += oscillatingSpeed;
            if (isMovingOnX)
                oscillatingTile.pos.x = sin(time) * (initialStackBounds.x + 100);
            else
                oscillatingTile.pos.y = sin(time) * (initialStackBounds.y + 100);

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
        PVector scale;
        PVector desiredScale;
        PVector pos;
        PVector desiredPos;
        int color;

        Tile(PVector pos, PVector scale, int color) {
            this.pos = pos.copy();
            this.desiredPos = pos.copy();
            this.scale = scale.copy();
            this.desiredScale = scale.copy();
            this.color = color;

        }

        void update() {
            // Adjust position to the scale changes
            PVector pScale = scale.copy();
            scale.lerp(desiredScale, 0.2f);
            pos.add(PVector.sub(scale, pScale).mult(0.5f));
            display();
        }

        void display() {
            noStroke();
            fill(color);

            // Drawing tile
            pushMatrix();
            {
                translate(pos.x - scale.x / 2, pos.y - scale.y / 2, pos.z - scale.z / 2);
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
            this.scale = scale.copy();
            this.desiredScale = scale.copy();
        }

        private void setPos(float x, float y, float z) {
            this.pos = new PVector(x, y, z);
        }
    }

    class Waves {
        ArrayList<Rect> waves = new ArrayList<>();

        void init(PVector pos, PVector scale) {
            waves.add(new Rect(pos, scale));
        }

        void update() {
            for (Rect r : waves) {
                r.update();
            }
            for (int i = waves.size() - 1; i >= 0; i--) {
                Rect r = waves.get(i);
                if (r.off) {
                    waves.remove(i);
                } else {
                    r.update();
                }
            }
        }

        class Rect {
            PVector pos;
            PVector scale;
            PVector desiredScale;
            float lifespan = 255;
            boolean off;

            Rect(PVector pos, PVector scale) {
                this.pos = pos.copy();
                this.scale = scale.copy();
                this.desiredScale = this.scale.copy().add(scale.copy().mult(0.2f));
            }

            void display() {
                pushMatrix();
                {
                    translate(pos.x - scale.x / 2, pos.y - scale.y / 2, pos.z);
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
        ArrayList<Particle> particles;

        ParticleSystem() {
            particles = new ArrayList<>();
        }

        void run() {
            if (random(1) < 0.05) {
                particles.add(new Particle(random(-width / 2, width / 2) - 300, random(-height / 2, height / 2) - 300, random(-200, 200) - cameraZ));
            }
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                if (p.off) {
                    particles.remove(i);
                } else {
                    p.update();
                }
            }
        }

    }

    class Particle {
        PVector rot;
        PVector aVel;
        PVector pos;
        boolean off;
        int color;
        float alpha;

        Particle(float x, float y, float z) {
            color = color(random(150, 256), random(150, 256), random(150, 256));
            pos = new PVector(x, y, z);
            aVel = PVector.random3D().mult(0.01f);
            rot = PVector.random3D();
        }


        void update() {
            rot.add(aVel);
            alpha = -cos(time) * 128 + 128;
            pos.add(0, 0, 0.3f);
            time += 0.01;
            if (time > TWO_PI) off = true;
            display();
        }

        float time = 0;

        void display() {
            pushMatrix();
            {
                translate(pos.x, pos.y, pos.z);
                rotateX(rot.x);
                rotateY(rot.y);
                rotateZ(rot.z);
                fill(color, alpha);
                box(10);
            }
            popMatrix();
        }
    }
}
