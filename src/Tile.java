import processing.core.PApplet;
import processing.core.PVector;

class Tile {
    private PVector rot;
    private PVector aVel;

    private PVector scale;
    private PVector desiredScale;
    PVector pos;
    private PVector vel;

    private boolean dissolve;
    private float   alpha;
    private float   desiredAlpha;
    private int     color_;
    private int     desiredColor;
    private boolean off;

    private PApplet p;

    Tile(PApplet p, PVector pos, PVector scale, int color_) {
        this.p = p;
        this.desiredScale = scale.copy();
        this.scale = scale.copy();
        this.pos = pos.copy();
        this.setColor(color_);
        this.color_ = color_;
        this.desiredColor = color_;

        aVel = PVector.random3D().setMag(0.01f);
        rot = new PVector();
        desiredAlpha = 255;
    }

    void update() {
        // Adjust position to the scale changes
        final PVector pScale = scale.copy();
        easeChanges();
        pos.add(PVector.sub(scale, pScale).mult(0.5f));
        alpha = PApplet.lerp(alpha, desiredAlpha, 0.05f);
        if (dissolve) {
            rot.add(aVel);
            pos.add(vel);
            if (alpha < 0.1) {
                setOff();
            }
        }

        display();
    }

    private void easeChanges() {
        color_ = p.lerpColor(color_, desiredColor, 0.1f);
        scale.lerp(desiredScale, 0.1f);
    }

    private void display() {
        p.noStroke();
        p.fill(color_, alpha);

        // Drawing tile
        p.pushMatrix();
        {
            //translate(pos.x - (scale.x / 2), pos.y - (scale.y / 2), pos.z - (scale.z / 2));
            p.translate(pos.x, pos.y, pos.z);
            p.rotateX(rot.x);
            p.rotateY(rot.y);
            p.rotateZ(rot.z);
            p.scale(scale.x, scale.y, scale.z);
            p.box(1);
        }
        p.popMatrix();
    }

    void scaleUp(float x, float y) {
        desiredScale.add(x, y, 0);
        desiredScale.x = PApplet.constrain(desiredScale.x, 0, Main.DEFAULT_TILE_SCALE.x);
        desiredScale.y = PApplet.constrain(desiredScale.y, 0, Main.DEFAULT_TILE_SCALE.y);
        Main.tileScale.x = PApplet.constrain(Main.tileScale.x + x, 0, Main.DEFAULT_TILE_SCALE.x);
        Main.tileScale.y = PApplet.constrain(Main.tileScale.y + y, 0, Main.DEFAULT_TILE_SCALE.y);
        lightUp();
    }

    void setScale(PVector scale) {
        this.desiredScale = scale.copy();
        this.scale = scale.copy();
    }

    void setPos(float x, float y, float z) {
        this.pos = new PVector(x, y, z);
    }

    void dissolve() {
        vel = PVector.sub(pos, Main.topTile.pos).normalize().mult(0.5f);
        desiredAlpha = 0;
        alpha = 255;
        dissolve = true;
    }

    int getColor() {
        return color_;
    }

    private void setColor(int color_) {
        this.color_ = color_;
    }

    void lightUp() {
        color_ = p.color(255);
    }

    boolean isOff() {
        return off;
    }

    private void setOff() {
        this.off = true;
    }
}
