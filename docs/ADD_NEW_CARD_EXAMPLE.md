# Developer Guide: Adding a New Action Card

This guide demonstrates how to add a "Mega-Scan" card (reveals a 3x3 area) to the system in three steps.

## Step 1: Create the Card Class
Create `core/src/main/java/battleships_ex/gdx/model/cards/MegaScanCard.java`.

```java
public class MegaScanCard extends BaseActionCard {
    public MegaScanCard() {
        super("MegaScan", 5, 1); // 5 Energy, 1 Use
    }

    @Override
    public ActionCardResult execute(Player user, Player opponent, Coordinate target) {
        if (target == null) throw new IllegalArgumentException("Target required");
        
        // Define 3x3 area
        List<Coordinate> area = new ArrayList<>();
        for (int r = target.getRow() - 1; r <= target.getRow() + 1; r++) {
            for (int c = target.getCol() - 1; c <= target.getCol() + 1; c++) {
                area.add(new Coordinate(r, c));
            }
        }

        ActionCardEffect effects = ActionCardEffectProvider.getInstance().getEffects();
        
        // Use revealTileInfo for each tile
        for (Coordinate coord : area) {
            if (opponent.getBoard().isWithinBounds(coord)) {
                effects.revealTileInfo(opponent, coord);
            }
        }

        consumeUse(user);
        return ActionCardResult.revealed(cardName, area);
    }

    @Override public boolean endsTurn() { return false; }
    @Override public boolean allowsFireAfterUse() { return true; }
}
```

## Step 2: Register the Card
Update `ActionCardRegistry.java` to include the new card.

```java
// static block
ALL_CARDS.add(new CardMetadata("MegaScan", "Reveal a 3x3 area", 5, 1));

// createCard method
case "MegaScan":
    return new MegaScanCard();
```

## Step 3: Add UI Presentation
Update `BattleScreen.java` to handle the new card's visual identity.

```java
else if (modelCard instanceof MegaScanCard) {
    name = "MEGA SCAN";
    shortDesc = "3x3 reveal";
    longDesc = "Reveal all ships/mines in a 3x3 area. Costs 5 energy. 1 use.";
    icon = Assets.logo; // or your custom texture
}
```

## Done!
The card is now automatically available in the card selection pool and synchronized via Firebase.
