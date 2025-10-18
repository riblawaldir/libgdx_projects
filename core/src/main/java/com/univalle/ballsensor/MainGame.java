package com.univalle.ballsensor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class MainGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    // Variables del jugador (círculo)
    private float playerX, playerY;
    private float playerRadius = 15f;
    private float speed = 8f;
    
    // Variables del juego
    private int score = 0;
    private boolean gameStarted = false;
    private float gameTime = 0f;
    private boolean gameWon = false;
    
    // Variables del laberinto
    private Rectangle[] walls;
    private Rectangle exit;
    private Rectangle[] collectibles;
    private boolean[] collectibleCollected;
    private final int NUM_COLLECTIBLES = 5;
    
    // Variables de sensores
    private float accelX, accelY, accelZ;
    private float gyroX, gyroY, gyroZ;
    
    // Variables de diseño
    private float mazeOffsetX, mazeOffsetY;
    private float mazeWidth, mazeHeight;
    private Rectangle sensorPanel;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        
        // Calcular dimensiones y posición del laberinto
        calculateMazeLayout();
        
        // Posición inicial del jugador (relativa al laberinto)
        playerX = mazeOffsetX + 50f;
        playerY = mazeOffsetY + 50f;
        
        // Crear el laberinto
        createMaze();
        
        // Crear coleccionables
        createCollectibles();
    }
    
    private void calculateMazeLayout() {
        // Calcular el tamaño del laberinto (dejando espacio para el panel de sensores)
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Panel de sensores ocupa 200px del lado derecho
        float sensorPanelWidth = 200f;
        mazeWidth = screenWidth - sensorPanelWidth - 40f; // 40px de margen
        mazeHeight = screenHeight - 40f; // 40px de margen arriba y abajo
        
        // Centrar el laberinto en el espacio disponible
        mazeOffsetX = 20f; // Margen izquierdo
        mazeOffsetY = 20f; // Margen superior
        
        // Crear el panel de sensores
        sensorPanel = new Rectangle(screenWidth - sensorPanelWidth, 0, sensorPanelWidth, screenHeight);
    }
    
    private void createMaze() {
        // Crear paredes del laberinto (relativas al área del laberinto)
        walls = new Rectangle[8];
        
        // Paredes horizontales
        walls[0] = new Rectangle(mazeOffsetX, mazeOffsetY, mazeWidth, 20); // Pared inferior
        walls[1] = new Rectangle(mazeOffsetX, mazeOffsetY + mazeHeight - 20, mazeWidth, 20); // Pared superior
        walls[2] = new Rectangle(mazeOffsetX + 150, mazeOffsetY + 80, 200, 20); // Pared horizontal central
        walls[3] = new Rectangle(mazeOffsetX + 80, mazeOffsetY + 160, 150, 20); // Pared horizontal superior
        
        // Paredes verticales
        walls[4] = new Rectangle(mazeOffsetX, mazeOffsetY, 20, mazeHeight); // Pared izquierda
        walls[5] = new Rectangle(mazeOffsetX + mazeWidth - 20, mazeOffsetY, 20, mazeHeight); // Pared derecha
        walls[6] = new Rectangle(mazeOffsetX + 200, mazeOffsetY + 100, 20, 120); // Pared vertical central
        walls[7] = new Rectangle(mazeOffsetX + 120, mazeOffsetY + 200, 20, 80); // Pared vertical derecha
        
        // Crear la salida (esquina superior derecha del laberinto)
        exit = new Rectangle(mazeOffsetX + mazeWidth - 50, mazeOffsetY + mazeHeight - 50, 30, 30);
    }
    
    private void createCollectibles() {
        collectibles = new Rectangle[NUM_COLLECTIBLES];
        collectibleCollected = new boolean[NUM_COLLECTIBLES];
        
        // Posiciones de los coleccionables (relativas al laberinto, evitando paredes)
        collectibles[0] = new Rectangle(mazeOffsetX + 60, mazeOffsetY + 60, 20, 20);
        collectibles[1] = new Rectangle(mazeOffsetX + 220, mazeOffsetY + 130, 20, 20);
        collectibles[2] = new Rectangle(mazeOffsetX + 350, mazeOffsetY + 220, 20, 20);
        collectibles[3] = new Rectangle(mazeOffsetX + 100, mazeOffsetY + 280, 20, 20);
        collectibles[4] = new Rectangle(mazeOffsetX + 300, mazeOffsetY + 320, 20, 20);
        
        // Inicializar como no recolectados
        for (int i = 0; i < NUM_COLLECTIBLES; i++) {
            collectibleCollected[i] = false;
        }
    }

    @Override
    public void render() {
        // Fondo oscuro
        ScreenUtils.clear(0.05f, 0.05f, 0.2f, 1);

        if (!gameStarted) {
            // Pantalla de bienvenida
            batch.begin();
            font.getData().setScale(2.5f);
            font.draw(batch, "🧭 LABERINTO SENSOR", 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2 + 100);
            font.getData().setScale(1.5f);
            font.draw(batch, "Inclina tu dispositivo para mover", 
                     Gdx.graphics.getWidth()/2 - 180, Gdx.graphics.getHeight()/2 + 50);
            font.draw(batch, "Recolecta todos los diamantes", 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2);
            font.draw(batch, "Llega a la salida para ganar", 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2 - 30);
            font.draw(batch, "Toca la pantalla para comenzar", 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2 - 60);
            batch.end();
            
            if (Gdx.input.isTouched()) {
                gameStarted = true;
                gameTime = 0f;
                score = 0;
                gameWon = false;
                // Resetear coleccionables
                for (int i = 0; i < NUM_COLLECTIBLES; i++) {
                    collectibleCollected[i] = false;
                }
            }
        } else if (gameWon) {
            // Pantalla de victoria
            batch.begin();
            font.getData().setScale(2.5f);
            font.draw(batch, "🎉 ¡VICTORIA!", 
                     Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2 + 100);
            font.getData().setScale(1.5f);
            font.draw(batch, "Tiempo: " + String.format("%.1f", gameTime) + " segundos", 
                     Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2 + 50);
            font.draw(batch, "Diamantes recolectados: " + score + "/" + NUM_COLLECTIBLES, 
                     Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2);
            font.draw(batch, "Toca para jugar de nuevo", 
                     Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2 - 30);
            batch.end();
            
            if (Gdx.input.isTouched()) {
                gameStarted = false;
            }
        } else {
            // Juego activo
            gameTime += Gdx.graphics.getDeltaTime();
            
            // Leer datos de sensores
            accelX = Gdx.input.getAccelerometerX();
            accelY = Gdx.input.getAccelerometerY();
            accelZ = Gdx.input.getAccelerometerZ();
            
            // Mover el jugador según la inclinación
            float newX = playerX - accelX * speed;
            float newY = playerY + accelY * speed;
            
            // Verificar colisiones con paredes
            Rectangle playerRect = new Rectangle(newX - playerRadius, newY - playerRadius, 
                                               playerRadius * 2, playerRadius * 2);
            
            boolean canMoveX = true, canMoveY = true;
            
            for (Rectangle wall : walls) {
                if (playerRect.overlaps(wall)) {
                    // Verificar si la colisión es horizontal o vertical
                    if (Math.abs(newX - playerX) > Math.abs(newY - playerY)) {
                        canMoveX = false;
                    } else {
                        canMoveY = false;
                    }
                }
            }
            
            // Aplicar movimiento solo si no hay colisión
            if (canMoveX) playerX = newX;
            if (canMoveY) playerY = newY;
            
            // Mantener dentro de los límites de la pantalla
            playerX = Math.max(playerRadius, Math.min(playerX, Gdx.graphics.getWidth() - playerRadius));
            playerY = Math.max(playerRadius, Math.min(playerY, Gdx.graphics.getHeight() - playerRadius));
            
            // Verificar coleccionables
            for (int i = 0; i < NUM_COLLECTIBLES; i++) {
                if (!collectibleCollected[i] && playerRect.overlaps(collectibles[i])) {
                    collectibleCollected[i] = true;
                    score++;
                }
            }
            
            // Verificar si llegó a la salida
            if (playerRect.overlaps(exit) && score == NUM_COLLECTIBLES) {
                gameWon = true;
            }
            
            // Dibujar todo
            drawGame();
        }
    }
    
    private void drawGame() {
        // Dibujar panel de sensores (fondo)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.3f, 0.9f); // Azul oscuro semi-transparente
        shapeRenderer.rect(sensorPanel.x, sensorPanel.y, sensorPanel.width, sensorPanel.height);
        
        // Borde del panel de sensores
        shapeRenderer.setColor(0.3f, 0.3f, 0.6f, 1f); // Azul más claro para el borde
        shapeRenderer.rect(sensorPanel.x, sensorPanel.y, 2, sensorPanel.height); // Borde izquierdo
        shapeRenderer.end();
        
        // Dibujar paredes del laberinto
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle wall : walls) {
            shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
        }
        shapeRenderer.end();
        
        // Dibujar coleccionables (diamantes)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < NUM_COLLECTIBLES; i++) {
            if (!collectibleCollected[i]) {
                shapeRenderer.setColor(Color.CYAN);
                shapeRenderer.rect(collectibles[i].x, collectibles[i].y, 
                                 collectibles[i].width, collectibles[i].height);
            }
        }
        shapeRenderer.end();
        
        // Dibujar salida
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(exit.x, exit.y, exit.width, exit.height);
        shapeRenderer.end();
        
        // Dibujar jugador
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(playerX, playerY, playerRadius);
        shapeRenderer.end();
        
        // Dibujar información del juego (en el área del laberinto)
        batch.begin();
        font.getData().setScale(1.2f);
        font.draw(batch, "Diamantes: " + score + "/" + NUM_COLLECTIBLES, 
                 mazeOffsetX + 10, mazeOffsetY + mazeHeight - 10);
        font.draw(batch, "Tiempo: " + String.format("%.1f", gameTime), 
                 mazeOffsetX + 10, mazeOffsetY + mazeHeight - 35);
        batch.end();
        
        // Dibujar panel de sensores con información
        drawSensorPanel();
    }
    
    private void drawSensorPanel() {
        batch.begin();
        font.getData().setScale(1.0f);
        
        float panelX = sensorPanel.x + 10;
        float panelY = sensorPanel.y + sensorPanel.height - 20;
        
        // Título del panel
        font.getData().setScale(1.3f);
        font.setColor(Color.WHITE);
        font.draw(batch, "📱 SENSORES", panelX, panelY);
        panelY -= 30;
        
        // Datos del acelerómetro
        font.getData().setScale(1.0f);
        font.setColor(Color.CYAN);
        font.draw(batch, "Acelerómetro:", panelX, panelY);
        panelY -= 25;
        
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "X: " + String.format("%.2f", accelX), panelX, panelY);
        panelY -= 20;
        font.draw(batch, "Y: " + String.format("%.2f", accelY), panelX, panelY);
        panelY -= 20;
        font.draw(batch, "Z: " + String.format("%.2f", accelZ), panelX, panelY);
        panelY -= 30;
        
        // Instrucciones
        font.setColor(Color.YELLOW);
        font.draw(batch, "Instrucciones:", panelX, panelY);
        panelY -= 25;
        
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.8f);
        font.draw(batch, "• Inclina para mover", panelX, panelY);
        panelY -= 20;
        font.draw(batch, "• Recolecta diamantes", panelX, panelY);
        panelY -= 20;
        font.draw(batch, "• Llega a la salida", panelX, panelY);
        panelY -= 20;
        font.draw(batch, "• Evita las paredes", panelX, panelY);
        
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
