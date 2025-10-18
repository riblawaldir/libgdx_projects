package com.univalle.ballsensor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.MathUtils;

public class MainGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture ball;
    private BitmapFont font;
    private float x, y;
    private float speed = 8f;
    private int score = 0;
    private boolean gameStarted = false;
    private float gameTime = 0f;
    private float targetX, targetY;
    private boolean targetCollected = false;
    private float targetSize = 30f;
    
    // Variables para el objeto coleccionable (libgdx.png)
    private Texture collectibleTexture;
    private float collectibleX, collectibleY;
    private boolean collectibleVisible = true;
    private final int COLLECTIBLE_POINTS = 15; // Puntos que da el coleccionable

    @Override
    public void create() {
        batch = new SpriteBatch();
        ball = new Texture("libgdx.png");
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        
        // Posición inicial de la pelota
        x = Gdx.graphics.getWidth() / 2f - ball.getWidth() / 2f;
        y = Gdx.graphics.getHeight() / 2f - ball.getHeight() / 2f;
        
        // Posición inicial del objetivo
        targetX = Gdx.graphics.getWidth() * 0.8f;
        targetY = Gdx.graphics.getHeight() * 0.8f;
        
        // Cargar la textura del coleccionable
        collectibleTexture = new Texture("libgdx.png");
        spawnCollectible();
    }
    
    // Método para generar el coleccionable en una posición aleatoria
    private void spawnCollectible() {
        collectibleX = MathUtils.random(0, Gdx.graphics.getWidth() - collectibleTexture.getWidth());
        collectibleY = MathUtils.random(0, Gdx.graphics.getHeight() - collectibleTexture.getHeight());
        collectibleVisible = true;
    }

    @Override
    public void render() {
        // Fondo con gradiente
        ScreenUtils.clear(0.1f, 0.1f, 0.3f, 1);

        batch.begin();
        
        if (!gameStarted) {
            // Pantalla de bienvenida
            font.getData().setScale(2f);
            font.draw(batch, "¡Bienvenido al Juego de Pelota!", 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2 + 50);
            font.getData().setScale(1f);
            font.draw(batch, "Inclina tu dispositivo para mover la pelota", 
                     Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2);
            font.draw(batch, "Toca la pantalla para comenzar", 
                     Gdx.graphics.getWidth()/2 - 120, Gdx.graphics.getHeight()/2 - 30);
            
            if (Gdx.input.isTouched()) {
                gameStarted = true;
            }
        } else {
            gameTime += Gdx.graphics.getDeltaTime();
            
            // Leer datos del acelerómetro
            float accelX = Gdx.input.getAccelerometerX();
            float accelY = Gdx.input.getAccelerometerY();

            // Mover la pelota según la inclinación del dispositivo
            x -= accelX * speed;
            y += accelY * speed;

            // Evitar que salga de pantalla
            x = Math.max(0, Math.min(x, Gdx.graphics.getWidth() - ball.getWidth()));
            y = Math.max(0, Math.min(y, Gdx.graphics.getHeight() - ball.getHeight()));

            // Dibujar objetivo si no ha sido recolectado
            if (!targetCollected) {
                batch.setColor(Color.GREEN);
                batch.draw(ball, targetX, targetY, targetSize, targetSize);
                batch.setColor(Color.WHITE);
                
                // Verificar colisión con el objetivo
                if (Math.abs(x - targetX) < targetSize && Math.abs(y - targetY) < targetSize) {
                    targetCollected = true;
                    score += 10;
                    // Generar nuevo objetivo
                    targetX = (float) (Math.random() * (Gdx.graphics.getWidth() - targetSize));
                    targetY = (float) (Math.random() * (Gdx.graphics.getHeight() - targetSize));
                    targetCollected = false;
                }
            }
            
            // Dibujar el coleccionable si es visible
            if (collectibleVisible) {
                batch.draw(collectibleTexture, collectibleX, collectibleY);
            }

            // Dibujar la pelota
            batch.draw(ball, x, y);
            
            // Detectar colisión con el coleccionable
            if (collectibleVisible &&
                x < collectibleX + collectibleTexture.getWidth() &&
                x + ball.getWidth() > collectibleX &&
                y < collectibleY + collectibleTexture.getHeight() &&
                y + ball.getHeight() > collectibleY) {
                
                score += COLLECTIBLE_POINTS; // Añadir puntos por recolectar
                spawnCollectible(); // Generar nuevo coleccionable
            }

            // Mostrar información del juego
            font.draw(batch, "Puntuación: " + score, 20, Gdx.graphics.getHeight() - 20);
            font.draw(batch, "Tiempo: " + String.format("%.1f", gameTime), 20, Gdx.graphics.getHeight() - 50);
            
            // Mostrar datos del sensor
            font.draw(batch, "Acelerómetro X: " + String.format("%.2f", accelX), 20, 100);
            font.draw(batch, "Acelerómetro Y: " + String.format("%.2f", accelY), 20, 80);
            font.draw(batch, "Acelerómetro Z: " + String.format("%.2f", Gdx.input.getAccelerometerZ()), 20, 60);
            
            // Instrucciones
            font.draw(batch, "Inclina el dispositivo para mover la pelota", 20, 40);
            font.draw(batch, "Recolecta objetivos verdes (+10 pts) y libgdx (+15 pts)", 20, 20);
        }
        
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        ball.dispose();
        font.dispose();
        collectibleTexture.dispose(); // Limpiar la textura del coleccionable
    }
}
