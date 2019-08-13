import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

class Waves {
    private ArrayList<Wave> waves = new ArrayList<>();
    private PApplet         p;

    Waves(PApplet p) {
        this.p = p;
    }

    void init(PVector pos, PVector scale) {
        init(pos, scale, false);
    }

    void init(PVector pos, PVector scale, boolean golden) {
        waves.add(new Wave(pos, scale, golden));
    }

    void update() {
        p.rectMode(p.CENTER);
        for (int i = waves.size() - 1; i >= 0; i--) {
            Wave r = waves.get(i);
            if (r.off) waves.remove(i);
            else r.update();
        }
    }

    class Wave {
        PVector desiredScale;
        float   lifespan;
        PVector scale;
        PVector pos;
        boolean off;
        int     color_;

        Wave(PVector pos, PVector scale, boolean golden) {
            this.desiredScale = scale.copy().add(scale.copy().mult(0.4f));
            this.scale = scale.copy();
            this.pos = pos;
            this.color_ = golden ? p.color(255, 200, 0) : p.color(255);
            lifespan = 255;
        }

        void display() {
            p.pushMatrix();
            {
                p.translate(pos.x, pos.y, pos.z - Main.tileScale.z / 2);
                p.fill(color_, lifespan);
                p.rect(0, 0, scale.x, scale.y);
            }
            p.popMatrix();
        }

        void update() {
            easeMotion();
            if (lifespan < 0.01f) off = true;
            display();
        }

        void easeMotion() {
            lifespan = PApplet.lerp(lifespan, 0, 0.1f);
            scale.lerp(desiredScale, 0.1f);
        }
    }
}