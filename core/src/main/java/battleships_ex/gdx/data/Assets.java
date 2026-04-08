package battleships_ex.gdx.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Assets {

    public static class Ships {
        public Texture texPatrol2h;
        public Texture texPatrol2v;
        public Texture texCruiser3h;
        public Texture texCruiser3v;
        public Texture tex4h;
        public Texture tex4v;
        public Texture texCarrier5h;
        public Texture texCarrier5v;

        public TextureRegion shipPatrol2h;
        public TextureRegion shipPatrol2v;
        public TextureRegion shipCruiser3h;
        public TextureRegion shipCruiser3v;
        public TextureRegion ship4h;
        public TextureRegion ship4v;
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
        ships.texCruiser3h = new Texture(Gdx.files.internal("ship-sprites/cruiser_3_h.png"));
        ships.texCruiser3v = new Texture(Gdx.files.internal("ship-sprites/cruiser_3_v.png"));
        ships.tex4h = new Texture(Gdx.files.internal("ship-sprites/4_h.png"));
        ships.tex4v = new Texture(Gdx.files.internal("ship-sprites/4_v.png"));
        ships.texCarrier5h = new Texture(Gdx.files.internal("ship-sprites/carrier_5_h.png"));
        ships.texCarrier5v = new Texture(Gdx.files.internal("ship-sprites/carrier_5_v.png"));

        ships.shipPatrol2h = new TextureRegion(ships.texPatrol2h);
        ships.shipPatrol2v = new TextureRegion(ships.texPatrol2v);
        ships.shipCruiser3h = new TextureRegion(ships.texCruiser3h);
        ships.shipCruiser3v = new TextureRegion(ships.texCruiser3v);
        ships.ship4h = new TextureRegion(ships.tex4h);
        ships.ship4v = new TextureRegion(ships.tex4v);
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
        ships.texCruiser3h.dispose();
        ships.texCruiser3v.dispose();
        ships.tex4h.dispose();
        ships.tex4v.dispose();
        ships.texCarrier5h.dispose();
        ships.texCarrier5v.dispose();

        /// icons.randomizeTexture.dispose();
        /// icons.rotateTexture.dispose();
    }
}
