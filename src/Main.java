import processing.core.PApplet;
import java.util.ArrayList;

public class Main extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    public void settings() {
        fullScreen(P3D);
    }

    private final int boxLength = 40;
    private final int boxSize = 350;
    private ArrayList<Box> boxes;
    private Waves waves;

    public void setup() {
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
        background(bgColor);
        alternate();
        scale = lerp(scale, tScale, 0.1f);
        lights();
        translate(width / 2, height / 2);
        pushMatrix();
        rotateX(QUARTER_PI);
        rotateZ(QUARTER_PI);
        boxesFocus = lerp(boxesFocus, tBoxesFocus, 0.08f);
        scale(scale);
        translate(-boxSize / 2, -boxSize / 2, boxesFocus);

        currentBox.display();
        for (Box box : boxes) {
            box.display();
        }
        waves.update();
        popMatrix();

        fill(255);
        text(score, 0, -height / 4, 200);
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
        Box topBox = boxes.get(boxes.size() - 1);
        Box[] slicedBoxes = getSlicedBox(topBox, currentBox);
        if (slicedBoxes[0] != null) {
            if(topBox.area - slicedBoxes[0].area < 200){
                slicedBoxes[0].set(topBox.x, topBox.y, slicedBoxes[0].z, topBox.w, topBox.h, topBox.l);
                waves.init(slicedBoxes[0].x, slicedBoxes[0].y, slicedBoxes[0].z, slicedBoxes[0].w, slicedBoxes[0].h);
                score += 4;
            }
            currentBox = slicedBoxes[0];
            boxes.add(currentBox);
            currentBox = currentBox.copy();
            currentBox.z += currentBox.l;
            currentBox.setColor(getNextColor());

            score++;
            time = -HALF_PI;
            tBoxesFocus -= currentBox.l;
            return true;
        } else {
            return false;
        }
    }

    private Box[] getSlicedBox(Box knife, Box cheese) {
        Box[] slicedCheese = new Box[3];
        float leftX = max(knife.x, cheese.x);
        float rightX = min(knife.x + knife.w, cheese.x + cheese.w);
        float topY = max(knife.y, cheese.y);
        float bottomY = min(knife.y + knife.h, cheese.y + cheese.h);
        if (leftX < rightX && topY < bottomY) {
            slicedCheese[0] = new Box(leftX, topY, cheese.z, rightX - leftX, bottomY - topY, boxLength);
            slicedCheese[0].setColor(cheese.c);
            fill(0, 255, 0, 100);
            if (cheese.x > knife.x) {
                leftX = rightX;
                rightX = cheese.x + cheese.w;
            } else {
                rightX = leftX;
                leftX = cheese.x;
            }
            slicedCheese[1] = new Box(leftX, topY, cheese.z, rightX - leftX, bottomY - topY, boxLength);
            slicedCheese[1].setColor(cheese.c);
            if (cheese.y > knife.y) {
                topY = knife.y + knife.h;
                bottomY = cheese.y + cheese.w;
            } else {
                topY = knife.y;
                bottomY = cheese.y;
            }
            if (cheese.x < knife.x) {
                leftX = knife.x;
                rightX = cheese.x + cheese.w;
            } else {
                leftX = cheese.x;
                rightX = knife.x + knife.w;
            }

            slicedCheese[2] = new Box(leftX, topY, cheese.z, rightX - leftX, bottomY - topY, boxLength);
            slicedCheese[2].setColor(cheese.c);
        }
        return slicedCheese;
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


    private void reset() {
        bgColor = color(random(150, 256), random(150, 256), random(150, 256));
        startColor = color(random(256), random(256), random(256));
        endColor = color(random(256), random(256), random(256));
        count = 0;
        boxes = new ArrayList<>();
        Box box;
        box = new Box(0, 0, 0, boxSize, boxSize, boxLength * 3);
        boxes.add(box);
        box.setColor(getNextColor());
        box = new Box(0, 0, box.z + box.l, boxSize, boxSize, boxLength);
        box.setColor(getNextColor());
        currentBox = box;
        tBoxesFocus = 0;
        score = 0;
    }

    private float boxesFocus = 0;
    private float tBoxesFocus = 0;
    private float time = 0;

    private void alternate() {
        if (!gameOver) {
            time += 0.03;
            if (boxes.size() % 2 == 0) {
                currentBox.x = sin(time) * (boxSize + 100);
            } else {
                currentBox.y = sin(time) * (boxSize + 100);
            }
        }
    }


    void drawOrigin() {
        point(0, 0, 0);
        stroke(0, 0, 255);
        line(0, 0, 0, 1000, 0, 0);
        stroke(0, 255, 0);
        line(0, 0, 0, 0, 1000, 0);
        stroke(255, 0, 0);
        line(0, 0, 0, 0, 0, 1000);
    }

    class Box {
        float x, y, z, w, h, l;
        float area;
        int c;

        Box(float x, float y, float z, float w, float h, float l) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.l = l;
            area = w * h * l;
        }

        void set(float x, float y, float z, float w, float h, float l) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.l = l;
        }

        void setColor(int c) {
            this.c = c;
        }

        void display() {
            noStroke();
            fill(c);
            beginShape(QUAD);
            vertex(x, y, z);
            vertex(x + w, y, z);
            vertex(x + w, y + h, z);
            vertex(x, y + h, z);

            vertex(x, y, z + l);
            vertex(x + w, y, z + l);
            vertex(x + w, y + h, z + l);
            vertex(x, y + h, z + l);

            vertex(x, y, z);
            vertex(x, y, z + l);
            vertex(x, y + h, z + l);
            vertex(x, y + h, z);

            vertex(x + w, y, z);
            vertex(x + w, y, z + l);
            vertex(x + w, y + h, z + l);
            vertex(x + w, y + h, z);

            vertex(x, y, z);
            vertex(x, y, z + l);
            vertex(x + w, y, z + l);
            vertex(x + w, y, z);

            vertex(x, y + h, z);
            vertex(x, y + h, z + l);
            vertex(x + w, y + h, z + l);
            vertex(x + w, y + h, z);
            endShape();
        }

        Box copy() {
            Box box = new Box(x, y, z, w, h, l);
            box.setColor(c);
            return box;
        }

    }

    class Waves{
        ArrayList<Rect> waves = new ArrayList<>();
        void init(float x, float y, float z, float w, float h){
            waves.add(new Rect(x, y, z, w, h));
        }

        void update(){
            for(Rect r : waves){
                r.update();
            }
            for(int i = waves.size() - 1; i >= 0; i--){
                Rect r = waves.get(i);
                if(r.off){
                    waves.remove(i);
                }else{
                    r.update();
                }
            }
        }

        class Rect{
            float x, y, z, w, h, iW, iH;
            boolean off;
            Rect(float x, float y, float z, float w, float h){
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
                this.z = z;
                this.iW = w;
                this.iH = h;
            }

            void display(){
                fill(200, 255, 0, map(w * h, sq(max(iW, iH)), sq(max(iW, iH)) * 3, 255, 0));
                pushMatrix();
                translate((w- iW)/-2, (h - iH)/-2);
                beginShape(QUAD);
                vertex(x, y, z, w, h);
                vertex(x + w, y, z, w, h);
                vertex(x + w, y + h, z, w, h);
                vertex(x , y + h, z, w, h);
                endShape();
                popMatrix();
            }

            void update(){
                w = lerp(w, max(iW, iH) * 3f, 0.02f);
                h = lerp(h, max(iW, iH) * 3, 0.02f);
                if(sq(max(iW, iH)) * 3 - w * h < 0.01){
                    off = true;
                }
                display();
            }
        }
    }
}
