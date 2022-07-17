package com.burcuerdogan.fruitninjastarter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {
	ShapeRenderer shapes;
	SpriteBatch batch;
	Texture background;
	Texture score1;
	Texture score5;
	Texture life;
	Texture lifenegative;

	BitmapFont font;
	FreeTypeFontGenerator fontGen;

	Random random = new Random();
	Array<Fruit> fruitArray = new Array<Fruit>();

	//Scores & Lives
	int lives = 0;
	int score = 0;

	//Generator Variables
	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;

	//Time Control
	private double currentTime;
	private double gameOverTime = -1.0f;


	@Override
	public void create () {
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		background = new Texture("ninjabackground.jpg");
		score1 = new Texture("scoreone.png");
		score5 = new Texture("scorefive.png");
		life = new Texture("life.png");
		lifenegative = new Texture("lifenegative.png");

		Fruit.radius = Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth()) / 7f;
		Gdx.input.setInputProcessor(this);

		fontGen = new FreeTypeFontGenerator(Gdx.files.internal("JungleAdventurer.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
		params.color = Color.WHITE;
		params.size = 80;
		params.characters = "0123456789 ScreCutoplay:.+-";
		font = fontGen.generateFont(params);

	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		double newTime = TimeUtils.millis() / 1000.0;
		System.out.println("newTime: " + newTime);
		double frameTime = Math.min(newTime - currentTime, 0.3);
		System.out.println("frameTime: " + frameTime);
		float deltaTime = (float) frameTime;
		System.out.println("deltaTime: " + deltaTime);
		currentTime = newTime;

		if (lives <= 0 && gameOverTime == 0f) {
			//Game Over
			gameOverTime = currentTime;
		}

		if (lives > 0) {
			//Game Mode

			genSpeed -= deltaTime * 0.015f;

			System.out.println("genspeed: " + genSpeed);
			System.out.println("gencounter: " + genCounter);

			if (genCounter <= 0f) {
				genCounter = genSpeed;
				addItem();
			} else {
				genCounter -= deltaTime;
			}

			for (int i = 0; lives > i; i++) {
				batch.draw(life, i*90f + 30f, Gdx.graphics.getHeight()-110f,90,90);
			}

			for (Fruit fruit : fruitArray) {
				fruit.update(deltaTime);

				switch (fruit.type) {
					case REGULAR:
						batch.draw(score1, fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case EXTRA:
						batch.draw(score5, fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case ENEMY:
						batch.draw(lifenegative, fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case LIFE:
						batch.draw(life, fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
				}

			}

			boolean holdLives = false;
			Array<Fruit> toRemove = new Array<Fruit>();
			for (Fruit fruit : fruitArray) {
				if (fruit.outOfScreen()) {
					toRemove.add(fruit);

					if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
						lives--;
						holdLives = true;
						break;
					}
				}
			}

			if (holdLives) {
				for (Fruit f : fruitArray) {
					f.living = false;
				}
			}

			for (Fruit f : toRemove) {
				fruitArray.removeValue(f,true);
			}

		}

		font.draw(batch,"Score: " + score,50,80);
		if (lives <= 0) {
			font.draw(batch,"Cut to play!",Gdx.graphics.getWidth()*0.5f,Gdx.graphics.getHeight()*0.5f);
		}
		batch.end();
	}

	private void addItem() {
		float pos = random.nextFloat() * Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth());
		Fruit item = new Fruit(new Vector2(pos,-Fruit.radius), new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f + (random.nextFloat() - 0.5f)),Gdx.graphics.getHeight() * 0.5f));

		float type = random.nextFloat();
		if (type > 0.98) {
			item.type = Fruit.Type.LIFE;
		} else if (type > 0.88) {
			item.type = Fruit.Type.EXTRA;
		} else if (type > 0.78) {
			item.type = Fruit.Type.ENEMY;
		}

		fruitArray.add(item);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		shapes.dispose();
		font.dispose();
		fontGen.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (lives <= 0 && currentTime - gameOverTime > 2f) {//Menu Mode
			gameOverTime = 0f;
			score = 0;
			lives = 4; //Restart Game
			genSpeed = startGenSpeed;
			fruitArray.clear();
		} else {
			//Game Mode
			Array<Fruit> toRemove = new Array<Fruit>();
			Vector2 pos = new Vector2(screenX,Gdx.graphics.getHeight() - screenY);
			int plusScore = 0;
			for (Fruit f : fruitArray) {

				System.out.println("getHeight - y: " + screenY);
				System.out.println("getHeight - y: " + (Gdx.graphics.getHeight()-screenY));
				System.out.println("getHeight - y: " + f.getPos());
				System.out.println("distance: " + pos.dst2(f.pos));
				System.out.println("distance: " + f.clicked(pos));
				System.out.println("distance: " + Fruit.radius * Fruit.radius +1);

				if (f.clicked(pos)) {
					toRemove.add(f);

					switch (f.type) {
						case REGULAR:
							plusScore++;
							break;
						case EXTRA:
							plusScore+=2;
							score++;
							break;
						case ENEMY:
							lives--;
							break;
						case LIFE:
							lives++;
							break;
					}
				}
			}

			score += plusScore * plusScore;
			for (Fruit f : toRemove) {
				fruitArray.removeValue(f,true);
			}

		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
