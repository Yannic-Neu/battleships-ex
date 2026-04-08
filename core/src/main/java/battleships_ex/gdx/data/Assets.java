package battleships_ex.gdx.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Assets {

    public static class Ships {
        public Texture texPatrol2h;
        public Texture texPatrol2v;
        public Texture texDestroyer3h;
        public Texture texDestroyer3v;
        public Texture texSubmarine3h;
        public Texture texSubmarine3v;
        public Texture texCruiser4h;
        public Texture texCruiser4v;
        public Texture texCarrier5h;
        public Texture texCarrier5v;

        public TextureRegion shipPatrol2h;
        public TextureRegion shipPatrol2v;
        public TextureRegion shipDestroyer3h;
        public TextureRegion shipDestroyer3v;
        public TextureRegion shipSubmarine3h;
        public TextureRegion shipSubmarine3v;
        public TextureRegion shipCruiser4h;
        public TextureRegion shipCruiser4v;
        public TextureRegion shipCarrier5h;
        public TextureRegion shipCarrier5v;
    }

    public static class Icons {
        public Texture randomizeTexture;
        public Texture rotateTexture;

        /// public TextureRegion randomize;
        /// public TextureRegion rotate;
    }

    public static final Ships ships = new Ships();
    public static final Icons icons = new Icons();

    private Assets() {
    }
    public static TextureRegion[] profileIcons;

    public static void load() {
        ships.texPatrol2h = new Texture(Gdx.files.internal("ship-sprites/patrol_2_h.png"));
        ships.texPatrol2v = new Texture(Gdx.files.internal("ship-sprites/patrol_2_v.png"));
        ships.texDestroyer3h = new Texture(Gdx.files.internal("ship-sprites/destroyer_3_h.png"));
        ships.texDestroyer3v = new Texture(Gdx.files.internal("ship-sprites/destroyer_3_v.png"));
        ships.texSubmarine3h = new Texture(Gdx.files.internal("ship-sprites/submarine_3_h.png"));
        ships.texSubmarine3v = new Texture(Gdx.files.internal("ship-sprites/submarine_3_v.png"));
        ships.texCruiser4h = new Texture(Gdx.files.internal("ship-sprites/cruiser_4_h.png"));
        ships.texCruiser4v = new Texture(Gdx.files.internal("ship-sprites/cruiser_4_v.png"));
        ships.texCarrier5h = new Texture(Gdx.files.internal("ship-sprites/carrier_5_h.png"));
        ships.texCarrier5v = new Texture(Gdx.files.internal("ship-sprites/carrier_5_v.png"));

        ships.shipPatrol2h = new TextureRegion(ships.texPatrol2h);
        ships.shipPatrol2v = new TextureRegion(ships.texPatrol2v);
        ships.shipDestroyer3h = new TextureRegion(ships.texDestroyer3h);
        ships.shipDestroyer3v = new TextureRegion(ships.texDestroyer3v);
        ships.shipSubmarine3h = new TextureRegion(ships.texSubmarine3h);
        ships.shipSubmarine3v = new TextureRegion(ships.texSubmarine3v);
        ships.shipCruiser4h = new TextureRegion(ships.texCruiser4h);
        ships.shipCruiser4v = new TextureRegion(ships.texCruiser4v);
        ships.shipCarrier5h = new TextureRegion(ships.texCarrier5h);
        ships.shipCarrier5v = new TextureRegion(ships.texCarrier5v);


        profileIcons = new TextureRegion[] {
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_1.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_2.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_3.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_4.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_5.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_6.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_7.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_8.png"))),
            new TextureRegion(new Texture(Gdx.files.internal("player-icons/pfp_9.png")))
        };


        /// icons.randomizeTexture = new Texture(Gdx.files.internal("navigation-icons/randomize.png"));
        /// icons.rotateTexture = new Texture(Gdx.files.internal("navigation-icons/rotate.png"));

        /// icons.randomize = new TextureRegion(icons.randomizeTexture);
        /// icons.rotate = new TextureRegion(icons.rotateTexture);
    }

    public static void dispose() {
        ships.texPatrol2h.dispose();
        ships.texPatrol2v.dispose();
        ships.texDestroyer3h.dispose();
        ships.texDestroyer3v.dispose();
        ships.texSubmarine3h.dispose();
        ships.texSubmarine3v.dispose();
        ships.texCruiser4h.dispose();
        ships.texCruiser4v.dispose();
        ships.texCarrier5h.dispose();
        ships.texCarrier5v.dispose();

        /// icons.randomizeTexture.dispose();
        /// icons.rotateTexture.dispose();
    }
}
