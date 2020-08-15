package com.mygdx.game;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.components.PlayersAvatarComponent;
import com.mygdx.game.datamodels.GameData;
import com.mygdx.game.datamodels.PlayerData;
import com.mygdx.game.helpers.AnimationFramesHelper;
import com.mygdx.game.input.ControllerInput;
import com.mygdx.game.input.IPlayerInput;
import com.mygdx.game.levels.ILevelData;
import com.mygdx.game.levels.LevelGenerator;
import com.mygdx.game.systems.AddAndRemoveMapsquares;
import com.mygdx.game.systems.AnimationCycleSystem;
import com.mygdx.game.systems.CheckIfPlayersAreOffScreenSystem;
import com.mygdx.game.systems.CollectorSystem;
import com.mygdx.game.systems.CollisionSystem;
import com.mygdx.game.systems.DrawInGameGuiSystem;
import com.mygdx.game.systems.DrawPostGameGuiSystem;
import com.mygdx.game.systems.DrawPreGameGuiSystem;
import com.mygdx.game.systems.DrawingSystem;
import com.mygdx.game.systems.InputSystem;
import com.mygdx.game.systems.MoveToOffScreenSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.systems.ProcessCollisionSystem;
import com.mygdx.game.systems.ProcessPlayersSystem;
import com.mygdx.game.systems.ScrollPlayAreaSystem;
import com.mygdx.game.systems.WalkingAnimationSystem;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.libgdx.Generic2DGame;

public final class MyGdxGame extends Generic2DGame {

	public BasicECS ecs;
	public BitmapFont font;
	public EntityFactory entityFactory;
	public GameData gameData;
	public AnimationFramesHelper animFrameHelper;
	public int winnerImageId;
	public int gameStage = -1; // -1, 0, or 1 for before, during and after game
	private boolean nextStage = false;
	public HashMap<IPlayerInput, PlayerData> players = new HashMap<IPlayerInput, PlayerData>();
	public boolean keyboard_joined = false;
	public ILevelData level_data;

	// Systems
	public InputSystem inputSystem;
	private DrawingSystem drawingSystem;
	public CollisionSystem collisionSystem;
	private MovementSystem movementSystem;
	private AnimationCycleSystem animSystem;
	public ProcessCollisionSystem processCollisionSystem;
	public CollectorSystem collectorSystem;
	private WalkingAnimationSystem walkingAnimationSystem;
	private MoveToOffScreenSystem moveToOffScreenSystem;
	private DrawInGameGuiSystem drawInGameGuiSystem;
	private ProcessPlayersSystem processPlayersSystem;
	private DrawPreGameGuiSystem drawPreGameGuiSystem;
	private DrawPostGameGuiSystem drawPostGameGuiSystem;
	private AddAndRemoveMapsquares addAndRemoveMapsquares;
	private ScrollPlayAreaSystem scrollPlayAreaSystem;

	public float screen_cam_x;// = Settings.LOGICAL_WIDTH_PIXELS/2-Settings.MAP_SQ_SIZE; // Centre of current point
	public float screen_cam_y;// = 0;//Settings.LOGICAL_HEIGHT_PIXELS/2;
	public float scroll_speed;// = 20;

	public MyGdxGame() {
		super(Settings.RELEASE_MODE);
	}


	@Override
	public void create() {
		super.create();

		this.generateFonts();
		//font = new BitmapFont();
		//font.getData().setScale(2);

		ecs = new BasicECS();
		entityFactory = new EntityFactory(this);
		animFrameHelper = new AnimationFramesHelper(this);

		// Systems
		this.inputSystem = new InputSystem(this, ecs);
		drawingSystem = new DrawingSystem(this, ecs, batch);
		collisionSystem = new CollisionSystem(ecs);
		movementSystem = new MovementSystem(this, ecs);
		animSystem = new AnimationCycleSystem(ecs);
		processCollisionSystem = new ProcessCollisionSystem(this);
		this.collectorSystem = new CollectorSystem(this);
		this.walkingAnimationSystem = new WalkingAnimationSystem(ecs);
		this.moveToOffScreenSystem = new MoveToOffScreenSystem(ecs);
		this.drawInGameGuiSystem = new DrawInGameGuiSystem(this, batch);
		this.processPlayersSystem = new ProcessPlayersSystem(this);
		this.drawPreGameGuiSystem = new DrawPreGameGuiSystem(this, batch);
		this.drawPostGameGuiSystem = new DrawPostGameGuiSystem(this, batch);
		//ecs.addSystem(new ScrollPlayAreaSystem(this));
		ecs.addSystem(new CheckIfPlayersAreOffScreenSystem(this, ecs));
		this.addAndRemoveMapsquares = new AddAndRemoveMapsquares(this, ecs);
		//ecs.addSystem(this.addAndRemoveMapsquares);

		startPreGame();
	}


	private void generateFonts() {
		int height = Gdx.graphics.getBackBufferHeight();
		//Settings.p("Height: " + height);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SHOWG.TTF"));

		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = height/30;
		//Settings.p("Font size=" + parameter.size);
		font = generator.generateFont(parameter);
		
		generator.dispose();
	}
	
	
	private void addPlayerForController(IPlayerInput controller) {
		if (this.players.containsKey(controller) == false) {
			PlayerData data = new PlayerData(controller);
			this.players.put(controller, data);
			//p("player created");
		}
	}


	public void startNextStage() {
		this.nextStage = true;
	}


	private void startPreGame() {
		this.playMusic("music/IntroLoop.wav");

		this.removeAllEntities();
	}


	private void startPostGame() {
		this.removeAllEntities();
		this.playMusic("music/VictoryMusic.wav");
	}


	public void setWinner(int id) {
		this.nextStage = true;
		this.winnerImageId = id;
	}


	private void startGame() {
		this.removeAllEntities();

		this.playMusic("music/8BitMetal.wav");

		// Reset all player data
		for (PlayerData player : players.values()) {
			player.init();
		}

		gameData = new GameData();

		level_data = new LevelGenerator(ecs);
		level_data.createLevel();

		screen_cam_x = Settings.LOGICAL_WIDTH_PIXELS/2-Settings.MAP_SQ_SIZE; // Centre of current point
		screen_cam_y = 0;//Settings.LOGICAL_HEIGHT_PIXELS/2;
		scroll_speed = 20;

		this.scrollPlayAreaSystem = new ScrollPlayAreaSystem(this);
		this.addAndRemoveMapsquares = new AddAndRemoveMapsquares(this, ecs);
		//this.addAndRemoveMapsquares.runNow();
	}


	private int getNumPlayersInGame() {
		return players.size();
	}


	@Override
	public void render() {
		super.render();

		if (!paused) {
			if (nextStage) {
				nextStage = false;
				if (this.gameStage == -1 && this.getNumPlayersInGame() > 0) {
					this.gameStage = 0;
					this.startGame();
				} else if (this.gameStage == 0) {
					this.gameStage = 1;
					startPostGame();
				} else if (this.gameStage == 1) {
					this.gameStage = -1;
					startPreGame();
				}
			}

			ecs.addAndRemoveEntities();

			this.inputSystem.process();

			if (this.gameStage == 0) {
				// loop through systems
				this.processPlayersSystem.process();
				this.moveToOffScreenSystem.process();
				this.walkingAnimationSystem.process(); // Must be before the movementsystem, as that clears the direction
				this.movementSystem.process();				
				ecs.processSystem(CheckIfPlayersAreOffScreenSystem.class);
				this.scrollPlayAreaSystem.process();
				this.addAndRemoveMapsquares.process();
				this.animSystem.process();
			}

			// Start actual drawing
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			batch.setProjectionMatrix(camera.combined);
			camera.update();

			batch.begin();
			this.drawingSystem.process();
			if (this.gameStage == -1) {
				this.drawPreGameGuiSystem.process();
			} else if (this.gameStage == 0) {
				this.drawInGameGuiSystem.process();
			} else if (this.gameStage == 1) {
				this.drawInGameGuiSystem.process();
				this.drawPostGameGuiSystem.process();
			}

			if (Settings.RELEASE_MODE == false) {
				drawFont(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
			}
			
			batch.end();

			if (Settings.SHOW_OUTLINES) {
				batch.begin();
				this.drawingSystem.drawDebug(batch);
				batch.end();
			}
		}
	}


	public void drawFont(Batch batch, String text, float x, float y) {
		font.draw(batch, text, x, y);
	}


	@Override
	public void dispose() {
		super.dispose();

		removeAllEntities();

		if (font != null) {
			font.dispose();
		}
		this.animFrameHelper.dispose();
		ecs.dispose();
	}


	private void removeAllEntities() {
		ecs.markAllEntitiesForRemoval();
		ecs.addAndRemoveEntities();
	}


	public void getScreenCoords(float x, float y, Vector2 out) {
		out.x = x-(screen_cam_x) + (Settings.LOGICAL_WIDTH_PIXELS/2);
		out.y = y-(screen_cam_y) + (Settings.LOGICAL_HEIGHT_PIXELS/2);
	}


	public void playerKilled(AbstractEntity avatar) {
		avatar.remove();

		PlayersAvatarComponent uic = (PlayersAvatarComponent)avatar.getComponent(PlayersAvatarComponent.class);
		PlayerData player = uic.player;
		player.avatar = null;
		player.timeUntilAvatar = Settings.AVATAR_RESPAWN_TIME_SECS;
		player.lives--;

		sfx.play("sfx/Falling.mp3");

		// Check for winner
		int winner = -1;
		int highestScore = -1;
		boolean all_died = true;
		for (PlayerData p : players.values()) {
			if (p.lives <= 0) {
				if (p.score > highestScore) {
					highestScore = p.score;
					winner = p.playerIdx;
				}
			} else {
				all_died = false;
				break;
			}
		}
		if (all_died) {
			setWinner(winner);
			return;
		}

		this.scroll_speed += 5f;

	}


	@Override
	public void connected(Controller controller) {
		addPlayerForController(new ControllerInput(controller));
		p(this.controllerManager.getInGameControllers().size() + " controllers");
	}


	@Override
	public void disconnected(Controller controller) {
		// todo - remove player
		p(this.controllerManager.getInGameControllers().size() + " controllers");
	}

}

