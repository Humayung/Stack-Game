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

    private final int boxLength = 40;
    private ArrayList<Box> boxes;
    private Waves waves;
    private final int ERROR_MARGIN = 10;
    PVector stackBounds;
    PVector initialStackBounds;

    public void setup() {
        stackBounds = new PVector(350, 350, 40);
        initialStackBounds = stackBounds.copy();
        waves = new Waves();
        reset();
        ortho(-width / 2, width / 2, -height / 2, height / 2, -width, width);
        textSize(60);
        textAlign(CENTER);
    }

    private Box currentBox;

    private float scale = 0.1f;
    private float tScale = 1f;
    private int bgColor;

    public void draw() {
        background(0);
        alternate();
        scale = lerp(scale, tScale, 0.1f);
        lights();
        translate(width / 2, height / 2);
        pushMatrix();
        rotateX(QUARTER_PI);
        rotateZ(QUARTER_PI);
        boxesFocus = lerp(boxesFocus, tBoxesFocus, 0.08f);
        scale(scale);
        translate(0, 0, boxesFocus);

        currentBox.display();
        for (Box box : boxes) {
            box.display();
        }
        waves.update();
        drawOrigin();
        popMatrix();

        fill(255);
        text(score + (isMovingOnX ? "X" : "Y"), 0, -height / 4, 200);

    }

    private int score = 0;
    private boolean gameOver = false;

    public void keyPressed() {
        if (key == ' ') {
            if (gameOver) {
                gameOver = false;
                reset();
                tScale = 1;
            } else {
                if (!placeBox()) {
                    gameOver = true;
                    tScale = min(1, sqrt(boxes.size()) / (boxes.size() / 2f));
                }
            }
        }
    }


    private boolean placeBox() {
        score++;
        if(isMovingOnX){
            float deltaX = abs(topBox.pos.x - currentBox.pos.x);
            if(deltaX > ERROR_MARGIN){
                stackBounds.x -= deltaX;
                if(stackBounds.x < 0) return false;
                float middle = topBox.pos.x + currentBox.pos.x / 2;
                currentBox.setScale(stackBounds);
                currentBox.setPos(middle - (topBox.pos.x/2f), topBox.pos.y, topBox.pos.z + stackBounds.z);
            }else{
                currentBox.setPos(topBox.pos.x, topBox.pos.y, currentBox.pos.z);
            }
        }else{
            float deltaY = abs(topBox.pos.y - currentBox.pos.y);
            if(deltaY > ERROR_MARGIN){
                stackBounds.y -= deltaY;
                if(stackBounds.y < 0) return false;
                float middle = topBox.pos.y + currentBox.pos.y / 2;
                currentBox.setScale(stackBounds);
                currentBox.setPos(topBox.pos.x, middle - (topBox.pos.y/2), topBox.pos.z + stackBounds.z);
            }else{
                currentBox.setPos(topBox.pos.x, topBox.pos.y, currentBox.pos.z);
            }
        }
        topBox = currentBox;
        boxes.add(currentBox);
        currentBox = new Box(currentBox.pos, stackBounds, getNextColor());
        currentBox.pos.z += stackBounds.z;
        tBoxesFocus -= boxLength;
        isMovingOnX = !isMovingOnX;
        return true;
    }

    private int count = 0;
    private int startColor = color(random(256), random(256), random(256));
    private int endColor = color(random(256), random(256), random(256));

    private int getNextColor() {
        final int range = 20;
        count = (count + 1) % range;
        if (count == 0) {
            startColor = endColor;
            endColor = color(random(256), random(256), random(256));
        }
        float amt = (float) count / range;
        return lerpColor(startColor, endColor, amt);
    }


    Box topBox;

    private void reset() {
        bgColor = color(random(150, 256), random(150, 256), random(150, 256));
        startColor = color(random(256), random(256), random(256));
        endColor = color(random(256), random(256), random(256));
        stackBounds = initialStackBounds.copy();
        count = 0;
        boxes = new ArrayList<>();
        topBox = new Box(new PVector(), stackBounds, getNextColor());
        boxes.add(topBox);
        currentBox = new Box(new PVector(), stackBounds, getNextColor());
        currentBox.pos.z += stackBounds.z;
        tBoxesFocus = 0;
        score = 1;
    }

    private float boxesFocus = 0;
    private float tBoxesFocus = 0;
    private float time = 0;
    private boolean isMovingOnX = false;

    private void alternate() {
        if (!gameOver) {
            time += 0.03;
            if (isMovingOnX)
                currentBox.pos.x = sin(time) * (initialStackBounds.x + 100);
            else
                currentBox.pos.y = sin(time) * (initialStackBounds.y + 100);

        }
    }

    void drawOrigin(PVector pos, float length) {
        pushMatrix();
        translate(pos.x, pos.y, pos.z);
        point(0, 0, 0);
        stroke(0, 0, 255);
        line(0, 0, 0, length, 0, 0);
        stroke(0, 255, 0);
        line(0, 0, 0, 0, length, 0);
        stroke(255, 0, 0);
        line(0, 0, 0, 0, 0, length);
        popMatrix();
    }

    void drawOrigin(){
        drawOrigin(new PVector(), 1000);
    }


    class Box {
        PVector scale;
        PVector pos;
        int color;

        Box(PVector pos, PVector scale, int color){
            this.pos = pos.copy();
            this.scale = scale.copy();
            this.color = color;
        }

        void display() {
            stroke(255, 200, 0);
//            fill(color);
            fill(255, 200, 0, 10);
            pushMatrix();
            translate(pos.x - scale.x/2, pos.y - scale.y/2, pos.z - scale.z/2);
            beginShape(QUAD);
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
            endShape();
            popMatrix();
        }

        public void setScale(PVector scale) {
            this.scale = scale.copy();
        }

        public void setPos(float x, float y, float z) {
            this.pos = new PVector(x, y, z);
        }
    }

    class Waves {
        ArrayList<Rect> waves = new ArrayList<>();

        void init(float x, float y, float z, float w, float h) {
            waves.add(new Rect(x, y, z, w, h));
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
            float x, y, z, w, h, iW, iH;
            boolean off;

            Rect(float x, float y, float z, float w, float h) {
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
                this.z = z;
                this.iW = w;
                this.iH = h;
            }

            void display() {
                fill(200, 255, 0, map(w * h, sq(max(iW, iH)), sq(max(iW, iH)) * 3, 255, 0));
                pushMatrix();
                translate((w - iW) / -2, (h - iH) / -2);
                beginShape(QUAD);
                vertex(x, y, z, w, h);
                vertex(x + w, y, z, w, h);
                vertex(x + w, y + h, z, w, h);
                vertex(x, y + h, z, w, h);
                endShape();
                popMatrix();
            }

            void update() {
                w = lerp(w, max(iW, iH) * 3f, 0.02f);
                h = lerp(h, max(iW, iH) * 3, 0.02f);
                if (sq(max(iW, iH)) * 3 - w * h < 0.01) {
                    off = true;
                }
                display();
            }
        }
    }
}
