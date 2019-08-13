import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

class ParticleSystem {
    private ArrayList<Particle> particles = new ArrayList<>();
    private PApplet             p;

    ParticleSystem( PApplet p ) {
        this.p = p;
    }

    void run( ) {
        if (p.random(1) < 0.1) {
            final float a = p.random(p.TWO_PI);
            final float r = p.random(p.height / 1.38f, p.height / 0.93f);
            final float x = PApplet.sin(a) * r;
            final float y = PApplet.cos(a) * r;
            particles.add(new Particle(x, y, Main.topTile.pos.z + p.random(- p.height / 2)));
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            final Particle p = particles.get(i);
            if (p.off) particles.remove(i);
            else p.update();
        }
    }

    class Particle {
        PVector rot;
        PVector aVel;
        PVector pos;
        PVector scale;

        boolean off;

        float alpha;
        float time;
        int   color_;

        Particle( float x, float y, float z ) {
            color_ = p.color(
                    p.random(150, 256),
                    p.random(150, 256),
                    p.random(150, 256)
            );
            aVel = PVector.random3D().mult(0.01f);
            pos = new PVector(x, y, z);
            rot = PVector.random3D();
            scale = new PVector(p.height / 76.8f, p.height / 76.8f, p.height / 76.8f);
        }


        void update( ) {
            alpha = ( - PApplet.cos(time) * 128 ) + 128;
            pos.add(0, 0, 0.3f);
            rot.add(aVel);
            time += 0.01;
            if (time > p.TWO_PI) {
                off = true;
            }
            display();
        }

        void display( ) {
            p.pushMatrix();
            {
                p.fill(color_, alpha);
                p.translate(pos.x, pos.y, pos.z);
                p.rotateX(rot.x);
                p.rotateY(rot.y);
                p.rotateZ(rot.z);
                p.scale(scale.x, scale.y, scale.z);
                p.box(1);
            }
            p.popMatrix();
        }
    }


}