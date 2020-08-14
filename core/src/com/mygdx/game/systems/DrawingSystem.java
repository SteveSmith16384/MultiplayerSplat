package com.mygdx.game.systems;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Settings;
import com.mygdx.game.components.ImageComponent;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.ScrollsAroundComponent;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;

public class DrawingSystem extends AbstractSystem implements Comparator<AbstractEntity> {

	private MyGdxGame game;
	private SpriteBatch batch;
	//private HashMap<String, Texture> textures = new HashMap<String, Texture>();
	private ShapeRenderer shapeRenderer;

	public DrawingSystem(MyGdxGame _game, BasicECS ecs, SpriteBatch _batch) {
		super(ecs, ImageComponent.class);

		game = _game;
		batch = _batch;

		shapeRenderer = new ShapeRenderer();
	}


	@Override
	public void process() {
		Collections.sort(this.entities, this);
		Iterator<AbstractEntity> it = this.entities.iterator();
		while (it.hasNext()) {
			AbstractEntity entity = it.next();
			this.processEntity(entity);
		}
	}


	//@Override
	public void processEntity(AbstractEntity entity) {
		ImageComponent imageData = (ImageComponent)entity.getComponent(ImageComponent.class);
		PositionComponent posData = (PositionComponent)entity.getComponent(PositionComponent.class);
		if (imageData.sprite == null) {
			// Load sprite for given filename
			//MyGdxGame.p("Creating sprite for " + entity);
			Texture tex = game.getTexture(imageData.imageFilename);
			if (imageData.atlasPosition == null) {
				imageData.sprite = new Sprite(tex);
			} else {
				TextureAtlas atlas = new TextureAtlas();
				atlas.addRegion("r", tex, (int)imageData.atlasPosition.left, (int)imageData.atlasPosition.bottom, (int)imageData.atlasPosition.width(), (int)imageData.atlasPosition.height());
				imageData.sprite = atlas.createSprite("r");
			}
			if (imageData.w > 0 && imageData.h > 0) {
				imageData.sprite.setSize(imageData.w, imageData.h);
			}
		}
		
		// Draw the sprite
		ScrollsAroundComponent scroll = (ScrollsAroundComponent)entity.getComponent(ScrollsAroundComponent.class);
		if (scroll == null) {
			imageData.sprite.setPosition(posData.rect.getX(), posData.rect.getY());
		} else {
			float x = posData.rect.getX()-(game.screen_cam_x) + (Settings.LOGICAL_WIDTH_PIXELS/2);
			float y = posData.rect.getY()-(game.screen_cam_y) + (Settings.LOGICAL_HEIGHT_PIXELS/2);
			imageData.sprite.setPosition(x, y);
		}
		imageData.sprite.draw(batch);
	}

/*
	public Texture getTexture(String filename) {
		if (textures.containsKey(filename)) {
			return textures.get(filename);
		}
		MyGdxGame.p("Loading new tex: " + filename);
		Texture t = new Texture(filename);
		this.textures.put(filename, t);
		return t;
	}
*/

	public void drawDebug(SpriteBatch batch) {
		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

		Iterator<AbstractEntity> it = this.getEntityIterator();
		while (it.hasNext()) {
			AbstractEntity entity = it.next();
			PositionComponent posData = (PositionComponent)entity.getComponent(PositionComponent.class);

			if (posData.rect != null) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				shapeRenderer.setColor(Color.GREEN);
				shapeRenderer.rect(posData.rect.left, posData.rect.bottom, posData.rect.width(), posData.rect.height()); 
				shapeRenderer.end();
			}
		}
	}


	public void dispose() {
		/*for(Texture t : this.textures.values()) {
			t.dispose();
		}*/
		shapeRenderer.dispose();
	}


	@Override
	public int compare(AbstractEntity arg0, AbstractEntity arg1) {
		ImageComponent im0 = (ImageComponent)arg0.getComponent(ImageComponent.class);
		ImageComponent im1 = (ImageComponent)arg1.getComponent(ImageComponent.class);
		return im0.zOrder - im1.zOrder;
	}

}

