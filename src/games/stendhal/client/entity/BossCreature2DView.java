package games.stendhal.client.entity;

import games.stendhal.client.Sprite;

import java.util.Map;


public class BossCreature2DView extends Creature2DView {
	public BossCreature2DView(BossCreature creature) {
		super(creature);
    }

	@Override
	protected void buildSprites(Map<Object, Sprite> map) {
		Sprite tiles=getAnimationSprite();
		
		map.put(ActiveEntity.STATE_RIGHT, getAnimatedWalk(tiles, 0, getWidth(), getHeight()));
		map.put(ActiveEntity.STATE_LEFT, getAnimatedWalk(tiles, 1, getWidth(), getHeight()));

		map.put(ActiveEntity.STATE_UP, map.get(ActiveEntity.STATE_RIGHT));
		map.put(ActiveEntity.STATE_DOWN, map.get(ActiveEntity.STATE_LEFT));
	}
}
