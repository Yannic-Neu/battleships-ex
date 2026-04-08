package battleships_ex.gdx.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Assets {

    public static class Ships {
        public Texture tex2h;
        public Texture tex2v;
        public Texture tex3h;
        public Texture tex3v;
        public Texture tex4h;
        public Texture tex4v;
        public Texture tex5h;
        public Texture tex5v;

        public TextureRegion ship2h;
        public TextureRegion ship2v;
        public TextureRegion ship3h;
        public TextureRegion ship3v;
        public TextureRegion ship4h;
        public TextureRegion ship4v;
        public TextureRegion ship5h;
        public TextureRegion ship5v;
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
        ships.tex2h = new Texture(Gdx.files.internal("ship-sprites/2_h.png"));
        ships.tex2v = new Texture(Gdx.files.internal("ship-sprites/2_v.png"));
        ships.tex3h = new Texture(Gdx.files.internal("ship-sprites/3_h.png"));
        ships.tex3v = new Texture(Gdx.files.internal("ship-sprites/3_v.png"));
        ships.tex4h = new Texture(Gdx.files.internal("ship-sprites/4_h.png"));
        ships.tex4v = new Texture(Gdx.files.internal("ship-sprites/4_v.png"));
        ships.tex5h = new Texture(Gdx.files.internal("ship-sprites/5_h.png"));
        ships.tex5v = new Texture(Gdx.files.internal("ship-sprites/5_v.png"));

        ships.ship2h = new TextureRegion(ships.tex2h);
        ships.ship2v = new TextureRegion(ships.tex2v);
        ships.ship3h = new TextureRegion(ships.tex3h);
        ships.ship3v = new TextureRegion(ships.tex3v);
        ships.ship4h = new TextureRegion(ships.tex4h);
        ships.ship4v = new TextureRegion(ships.tex4v);
        ships.ship5h = new TextureRegion(ships.tex5h);
        ships.ship5v = new TextureRegion(ships.tex5v);


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
        ships.tex2h.dispose();
        ships.tex2v.dispose();
        ships.tex3h.dispose();
        ships.tex3v.dispose();
        ships.tex4h.dispose();
        ships.tex4v.dispose();
        ships.tex5h.dispose();
        ships.tex5v.dispose();

        /// icons.randomizeTexture.dispose();
        /// icons.rotateTexture.dispose();
    }
}
