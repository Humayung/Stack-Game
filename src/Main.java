/*
 * 'The Stack' Processing Version
 * Recreating 'The Stack' Android Game by Ketchapp
 *
 * Author: Mirza MY Humayung
 */

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import processing.event.MouseEvent;
import java.util.ArrayList;

public class Main extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    public Main() {
        startColor            = color(random(256),
                random(256),
                random(256));
        endColor              = color(random(256),
                random(256),
                random(256));
        oscillatingSpeed      = .03f;
        playButtonAlpha       = 0;
        titleAlpha            = 255;
        distance              = 0.1f;
        score                 = new Score();
        cameraPos             = new PVector();
        desiredCameraPos      = new PVector();
        desiredGlobalRotation = new PVector(QUARTER_PI, 0, QUARTER_PI);
        globalRotation        = new PVector();
        screenW               = 805;
        screenH               = 483;
        desiredGlobalPosition = new PVector(screenW / 2, screenH / 2, 0);
        globalPosition        = desiredGlobalPosition.copy();
        DEFAULT_TILE_SCALE    = new PVector(screenH / 2.2f, screenH / 2.2f, screenH / 14f);
        BONUS_GAIN            = screenH / 40f;
        ERROR_MARGIN          = screenH / 40f;
        MAX_SCORE_GAIN        = 100;
        cubie                 = new Cubie(this);
        rubbles               = new ArrayList<>();
        waves                 = new Waves(this);
        MAX_TILES_ONSCREEN    = 20;
        MINIMUM_COMBO         = 5;
    }

    public void settings() {
        size(screenW, screenH, P3D);
    }

    private final int screenW;
    private final int screenH;

    private ArrayList<Tile> tiles;
    private ArrayList<Tile> displayTiles;
    private ArrayList<Tile> rubbles;
    private Waves           waves;

    private final int   MAX_TILES_ONSCREEN;
    private final float BONUS_GAIN;
    private final float MAX_SCORE_GAIN;
    private final float ERROR_MARGIN;

    private PVector cameraPos;
    private PVector desiredCameraPos;
    private float   distance;
    private float   desiredDistance;
    private int     focusIndex = 0;
    private PVector desiredGlobalRotation;
    private PVector globalRotation;
    private PVector desiredGlobalPosition;
    private PVector globalPosition;

    static PVector DEFAULT_TILE_SCALE;
    static PVector tileScale;
    private       Tile    oscillatingTile;
    static Tile    topTile;
    private       float   time;
    private       float   oscillatingSpeed;
    private       boolean isMovingOnX;

    private Cubie cubie;

    private final int     MINIMUM_COMBO;
    private       Score   score;
    private       int     combo;
    private       boolean gameOver;
    private       boolean gameStarted;
    private       boolean titleScreen   = true;

    private final int   COLOR_RANGE = 10;
    private       int   colorPos;
    private       int   startColor;
    private       int   endColor;
    private       int   desiredBgColor;
    private       int   bgColor;
    private       float playButtonAlpha;
    private       float titleAlpha;


    PFont titleFont1;
    PFont titleFont2;
    public void setup() {
        titleFont2 = loadFont("Lato-Bold-48.vlw");
        titleFont1 = loadFont("Lato-Hairline-120.vlw");
        newGame();
    }

    public void draw() {
        ortho(-width / 2, width / 2, -height / 2, height / 2, -width, width);
        globalUpdate();
    }

    private void globalUpdate() {
        pushMatrix();
        {
            background(bgColor);
            easeMotion();
            translate(globalPosition.x, globalPosition.y, globalPosition.z);
            lights();

            // Draw Tiles
            pushMatrix();
            {
                rotateX(globalRotation.x);
                rotateY(globalRotation.y);
                rotateZ(globalRotation.z);
                scale(distance);
                translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                displayTiles.forEach(Tile::update);
                updateRubbles();
                waves.update();
                cubie.run();
            }
            popMatrix();
            if (gameStarted) oscillate();
        }
        popMatrix();
        updateUI();
    }

    private void easeMotion() {
        globalPosition.lerp(desiredGlobalPosition, 0.1f);
        globalRotation.lerp(desiredGlobalRotation, 0.1f);
        distance = lerp(distance, desiredDistance, 0.1f);
        titleAlpha = lerp(titleAlpha, titleScreen ? 255 : 0, 0.1f);
        bgColor = lerpColor(bgColor, desiredBgColor, 0.1f);
        cameraPos.lerp(desiredCameraPos, 0.1f);
        score.ease();
    }


    private void updateUI() {
        // Play Button, Score, and HighScore
        pushMatrix();
        translate(width/2, height/2, height);
        textAlign(CENTER);
        if (!titleScreen) {
            fill(255 - red(desiredBgColor), 255 - green(desiredBgColor), 255 - blue(desiredBgColor));
            textFont(titleFont2);
            if (gameStarted) {
                textSize(height/12.8f);
                text(score.score, 0, -height / 3.5f);
                textSize(height/19.2f);
                text(score.stack, 0, -height / 3.5f + height/12.8f);
            } else {
                textSize(height/12.8f);
                text(score.highScore, 0, (height / 3.5f));
                textSize(height/19.2f);
                text(score.highStack, 0, (height / 3.5f) + height/12.8f);
            }
        }
        drawPlayButton(0, 0, height/4f);
        drawTitle();
        popMatrix();
    }

    private void drawTitle() {
        textSize(height/3.45f);
        titleAlpha = lerp(titleAlpha, titleScreen ? 255 : 0, 0.1f);
        fill(255 - red(desiredBgColor), 255 - green(desiredBgColor), 255 - blue(desiredBgColor), titleAlpha);
        if (titleAlpha > 100) {
            textFont(titleFont1);
            textAlign(LEFT, CENTER);
            text("STACKER", -width/2.3f, 0);
            textSize(height/26f);
            text("Inspired by KETCHAPP", -width/2.3f, -height/16.1f + height/5.5f);
        }
    }

    private void newGame() {
        titleScreen = true;
        desiredBgColor = color(
                random(150, 256),
                random(150, 256),
                random(150, 256)
        );

        tiles = new ArrayList<>();
        displayTiles = new ArrayList<>();
        tileScale = DEFAULT_TILE_SCALE.copy();

        topTile = new Tile(this, new PVector(), tileScale, getNextColor());
        tiles.add(topTile);
        displayTiles.add(topTile);
        oscillatingTile = new Tile(this, new PVector(0, 0, tileScale.z), tileScale, getNextColor());
        displayTiles.add(oscillatingTile);
        tiles.add(oscillatingTile);
        oscillatingSpeed = 0.03f;
        gameOver = false;
        gameStarted = false;

        setFocusDistance();

        globalRotation.z %= TWO_PI;
        desiredGlobalRotation = new PVector(QUARTER_PI, 0, QUARTER_PI);
        desiredCameraPos = topTile.pos;
        desiredGlobalPosition.set(width / 2f + height/3.84f, height / 2, 0);
        score.reset();
    }

    public void mousePressed() {
        key = ' ';
        keyPressed();
    }

    public void keyPressed() {
        if (key == ' ') {
            if (!titleScreen) {
                if (gameStarted) {
                    if (gameOver) {
                        newGame();
                    } else {
                        if (!placeTile()) {
                            gameOver();
                        } else {
                            accelerate();
                            desiredCameraPos = topTile.pos;
                        }
                    }
                } else {
                    startGame();
                }
            } else {
                enterGame();
            }
        }
        if (key == CODED) {
            if (gameOver) {
                switch (keyCode) {
                    case UP:
                        focusIndex = constrain(focusIndex + 1, 0, tiles.size() - 1);
                        tiles.get(focusIndex).lightUp();
                        desiredCameraPos = tiles.get(focusIndex).pos;
                        break;
                    case DOWN:
                        focusIndex = constrain(focusIndex - 1, 0, tiles.size() - 1);
                        tiles.get(focusIndex).lightUp();
                        desiredCameraPos = tiles.get(focusIndex).pos;
                        break;
                    case LEFT:
                        desiredGlobalRotation.z += 0.2;
                        break;
                    case RIGHT:
                        desiredGlobalRotation.z -= 0.2;
                        break;
                }
            }
        }
    }

    public void mouseWheel(MouseEvent e) {
        if (gameOver) {
            desiredDistance = constrain(desiredDistance + e.getCount() * 0.1f, getCoverageDistance(), getFocusDistance());
        }
    }

    private void enterGame() {
        titleScreen = false;
        desiredGlobalPosition.set(width / 2, height / 2, 0);
    }

    private void gameOver() {
        setCoverageDistance();
        displayTiles = tiles;
        gameOver = true;
    }

    private void setCoverageDistance() {
        desiredDistance = getCoverageDistance();
    }

    private float getCoverageDistance() {
        return min((1f / tiles.size()) * 8, 0.8f);
    }

    private void setFocusDistance() {
        desiredDistance = getFocusDistance();
    }

    private float getFocusDistance() {
        return 1;
    }

    private void startGame() {
        gameStarted = true;
        gameOver = false;
    }

    private void accelerate() {
        oscillatingSpeed += 0.00018;
    }

    private void drawPlayButton(final float x, final float y, final float size) {
        playButtonAlpha = lerp(playButtonAlpha, gameStarted ? 0 : titleScreen ? 0 : 255, 0.1f);
        if (playButtonAlpha > 0.1) {
            // Centroid
            final float cx = 0.25f;
            final float cy = 0.5f;
            //
            pushMatrix();
            {
                translate(x - (cx * size), y - (cy * size));
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

    private void updateRubbles() {
        for (int i = rubbles.size() - 1; i >= 0; i--) {
            final Tile r = rubbles.get(i);
            if (r.isOff()) {
                rubbles.remove(i);
            } else {
                r.update();
            }
        }
    }

    private boolean placeTile() {
        if (isMovingOnX) {
            final float deltaX = topTile.pos.x - oscillatingTile.pos.x;
            score.add((tileScale.x - min(abs(deltaX), tileScale.x)) / DEFAULT_TILE_SCALE.x * MAX_SCORE_GAIN);
            if (abs(deltaX) > ERROR_MARGIN) {
                combo = 0;
                // Cut tile
                tileScale.x -= abs(deltaX);
                if (tileScale.x < 0) {
                    // Rubble
                    rubbles.add(oscillatingTile);
                    tiles.remove(tiles.size() - 1);
                    oscillatingTile.dissolve();
                    return false;
                }
                // Rubble
                final float rubbleX = oscillatingTile.pos.x + (deltaX > 0 ? -1 : 1) * tileScale.x / 2;
                final PVector rubbleScale = new PVector(abs(deltaX), tileScale.y, tileScale.z);
                Tile rubble = new Tile(this, new PVector(rubbleX, oscillatingTile.pos.y, oscillatingTile.pos.z), rubbleScale, oscillatingTile.getColor());
                rubbles.add(rubble);
                rubble.dissolve();

                final float middle = topTile.pos.x + (oscillatingTile.pos.x / 2);
                oscillatingTile.setScale(tileScale);
                oscillatingTile.setPos(middle - (topTile.pos.x / 2), topTile.pos.y, oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                combo++;
                if (combo >= MINIMUM_COMBO) {
                    oscillatingTile.scaleUp(BONUS_GAIN, 0);
                    waves.init(oscillatingTile.pos, tileScale, true);
                } else {
                    waves.init(oscillatingTile.pos, tileScale);
                }
            }
        } else {
            final float deltaY = topTile.pos.y - oscillatingTile.pos.y;
            score.add((tileScale.y - min(abs(deltaY), tileScale.y)) / DEFAULT_TILE_SCALE.y * MAX_SCORE_GAIN);
            if (abs(deltaY) > ERROR_MARGIN) {
                combo = 0;
                // Cut Tile
                tileScale.y -= abs(deltaY);
                if (tileScale.y < 0) {
                    // Rubble
                    rubbles.add(oscillatingTile);
                    tiles.remove(tiles.size() - 1);
                    oscillatingTile.dissolve();
                    return false;
                }
                // Rubble
                final float rubbleY = oscillatingTile.pos.y + (deltaY > 0 ? -1 : 1) * tileScale.y / 2;
                final PVector rubbleScale = new PVector(tileScale.x, abs(deltaY), tileScale.z);
                Tile rubble = new Tile(this, new PVector(oscillatingTile.pos.x, rubbleY, oscillatingTile.pos.z), rubbleScale, oscillatingTile.getColor());
                rubble.dissolve();
                rubbles.add(rubble);

                final float middle = topTile.pos.y + (oscillatingTile.pos.y / 2);
                oscillatingTile.setScale(tileScale);
                oscillatingTile.setPos(topTile.pos.x, middle - (topTile.pos.y / 2), oscillatingTile.pos.z);
            } else {

                // Align to top tile
                oscillatingTile.setPos(topTile.pos.x, topTile.pos.y, oscillatingTile.pos.z);
                combo++;
                if (combo >= MINIMUM_COMBO) {
                    oscillatingTile.scaleUp(0, BONUS_GAIN);
                    waves.init(oscillatingTile.pos, tileScale, true);
                } else {
                    waves.init(oscillatingTile.pos, tileScale);
                }
            }
        }
        spawnTile();
        return true;
    }

    private void spawnTile() {
        if (displayTiles.size() > MAX_TILES_ONSCREEN) {
            displayTiles.remove(0);
        }
        topTile = tiles.get(tiles.size() - 1);
        oscillatingTile = new Tile(this, oscillatingTile.pos.copy().add(0, 0, tileScale.z), tileScale, getNextColor());
        tiles.add(oscillatingTile);
        displayTiles.add(oscillatingTile);

        //Alternate
        isMovingOnX = !isMovingOnX;
        time = -HALF_PI;
        oscillate();
    }

    private int getNextColor() {
        colorPos = (colorPos + 1) % COLOR_RANGE;
        if (colorPos == 0) {
            startColor = endColor;
            endColor = color(random(256), random(256), random(256));
        }
        final float amt = (float) colorPos / COLOR_RANGE;
        return lerpColor(startColor, endColor, amt);
    }

    private void oscillate() {
        if (!gameOver) {
            time += oscillatingSpeed;
            if (isMovingOnX)
                oscillatingTile.pos.x = topTile.pos.x + sin(time) * (DEFAULT_TILE_SCALE.x * 1.7f);
            else
                oscillatingTile.pos.y = topTile.pos.y + sin(time) * (DEFAULT_TILE_SCALE.y * 1.7f);
        }
    }

    class Score {
        int score;
        int stack = 0;
        int highStack;
        int desiredScore = 0;
        int highScore;

        void add(float add) {
            stack++;
            desiredScore += add;
            highScore = score > highScore ? score : highScore;
            highStack = stack > highStack ? stack : highStack;
        }

        void reset() {
            stack = 0;
            desiredScore = 0;
        }

        void ease() {
            score = floor(lerp(score, desiredScore, 0.3f));
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


}