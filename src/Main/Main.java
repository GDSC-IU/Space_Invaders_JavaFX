package Main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/*
    Yashas H Majmudar
    IU1941230066
    CSE-B
*/

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Pane root;
    private Pane pane;
    private StackPane stackPane;
    private GameObject player;
    private int score = 0;
    private int highScore = 0;
    private AnimationTimer timer;
    private double time = 0;
    private boolean gameOver = false;
    private final Label scoreText = new Label("Score: "+score);
    private final Label highScoreText = new Label("HighScore: "+highScore);
    private double speed = 0.7;

    static final Image PLAYER_IMG =
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\player.png");
    static final Image BULLET_IMG
            = new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\bullet.png");

    static final Image[] ENEMY_IMAGES = {
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_0.png"),
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_1.png"),
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_2.png"),
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_3.png"),
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_4.png"),
            new Image("file:D:\\Programming\\JAVA\\Space Invaders\\src\\Main\\images\\ufo_5.png"),
    };

    private void initiate(){
        root = new Pane();
        pane = new Pane();
        stackPane = new StackPane();
        player = new GameObject(250, 700, 60, 60, "Player", PLAYER_IMG, speed);
        score = 0;
        time = 0;
        gameOver = false;

        try{
            FileInputStream fin = new FileInputStream("SpaceInvaders_HighScore.txt");

            DataInputStream din = new DataInputStream(fin);

            highScore = din.readInt();
            highScoreText.setText("HighScore: "+highScore);

            din.close();
        }catch(Exception e){
            score = 0;
            System.out.println(""+e);
        }
    }

    private Parent createContent() {
        stackPane.setPrefSize(600, 800);
        stackPane.maxHeight(800);
        stackPane.maxWidth(600);
        stackPane.minHeight(800);
        stackPane.minHeight(600);

        root.setPrefSize(600, 800);
        root.getChildren().add(player);
        root.setBackground(new Background(new BackgroundFill(Color.grayRgb(20), null, null)));

        pane.setPrefSize(600, 800);
        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();

        scoreText.setTextFill(Color.CORAL);
        scoreText.setFont(new Font(20));
        scoreText.setTranslateX(0);
        scoreText.setTranslateY(-350);

        highScoreText.setTextFill(Color.CORAL);
        highScoreText.setFont(new Font(20));
        highScoreText.setTranslateX(0);
        highScoreText.setTranslateY(-380);

        nextLevel();
        createStarBG();

        stackPane.getChildren().add(root);
        stackPane.getChildren().add(highScoreText);
        stackPane.getChildren().add(scoreText);
        stackPane.getChildren().add(pane);

        return stackPane;
    }

    private void increaseSpeed(){
        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run() {
                root.getChildren().forEach(obj -> {
                    GameObject object = (GameObject) obj;
                    if(object.type.equals("Enemy"))
                        speed+=0.1;
                        object.speed=speed;
                });
            }
        }, 8000);
    }

    private void createStarBG(){
        Random rand = new Random();
        for(int i = 0;i<60;i++){
            Stars star = new Stars(rand.nextInt(600), rand.nextInt(160-100), rand.nextInt(3), Math.random());
            pane.getChildren().add(star);
        }
    }

    private List<Stars> stars() {
        return pane.getChildren().stream().map(n -> (Stars) n).collect(Collectors.toList());
    }


    private void update() {
        scoreText.setText("Score: "+score);
        time+=0.016;

        if(time>0.5){
            time=0;
            createStarBG();
        }

        if(getAlienCount()<6)
            nextLevel();

        stars().forEach(star->star.moveDown());

        gameObjects().forEach(gameObject -> {
            switch (gameObject.type) {
                case "PlayerBullet":
                    gameObject.moveUp();
                    gameObjects().stream().filter(filter -> filter.type.equals("Enemy")).forEach(enemy -> {
                        if (gameObject.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                            enemy.isExploding = true;
                            enemy.blast();
                            score += 1;
                            gameObject.dead = true;
                        }
                    });
                    break;
                case "Enemy":
                    gameObject.moveDown();
                    gameObject.speed+= (double) (score%5)/100;
                    gameObjects().stream().filter(filter -> filter.type.equals("Player")).forEach(player -> {
                        if (gameObject.getBoundsInParent().intersects(player.getBoundsInParent())) {
                            player.isExploding = true;
                            gameObject.isExploding = true;
                            gameObject.dead = true;
                            player.blast();
                            gameOver();
                        }
                    });
                    break;
            }
        });

        root.getChildren().removeIf(gameObject -> {
            GameObject obj = (GameObject) gameObject;
            return obj.dead;
        });

        pane.getChildren().removeIf(star-> {
           Stars stars = (Stars) star;
           return stars.dead;
        });

    }

    private void nextLevel() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            GameObject enemy = new GameObject(
                    rand.nextInt(500-20),
                    rand.nextInt(160-100),
                    40,
                    40,
                    "Enemy",
                    ENEMY_IMAGES[rand.nextInt(5)],
                    speed
            );
            root.getChildren().add(enemy);
        }
    }

    private void gameOver() {
        timer.stop();
        Label label = new Label("GAME OVER\n Score: " + score+"\n click HERE to restart");
        label.setFont(new Font(50));
        label.setTextFill(Color.CORAL);
        label.setTextAlignment(TextAlignment.CENTER);

        if(highScore<score){
            try{
                FileOutputStream fout = new FileOutputStream("SpaceInvaders_HighScore.txt");
                DataOutputStream dout = new DataOutputStream(fout);

                dout.writeInt(score);
                dout.close();
            }catch (Exception e){
                System.out.println(""+e);
            }
        }

        gameOver = true;
        stackPane.getChildren().add(label);
    }

    private List<GameObject> gameObjects() {
        return root.getChildren().stream().map(n -> (GameObject) n).collect(Collectors.toList());
    }

    private int getAlienCount(){
        return (int) gameObjects().stream().filter(obj -> obj.type.equals("Enemy")).count();
    }

    private void shoot(GameObject player) {
        if (!player.dead) {
            GameObject bullet = new GameObject(
                    (int) player.getTranslateX() + 27,
                    (int) player.getTranslateY(),
                    5,
                    20,
                    player.type + "Bullet",
                    BULLET_IMG,
                    speed
            );
            root.getChildren().add(bullet);
        }
    }

    private void run(Stage stage){
        initiate();
        Scene scene = new Scene(createContent());
        scene.setFill(Color.grayRgb(20));
        scene.setCursor(Cursor.DEFAULT);
        scene.setOnMouseMoved(e -> player.setXAxis(e.getX()));
        scene.setOnMouseClicked(e -> {
            if(gameOver)
                run(stage);
            else
                shoot(player);
        });

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    player.moveLeft();
                    break;
                case RIGHT:
                    player.moveRight();
                    break;
                case SPACE:
                    shoot(player);
                    break;
                case P:
                    timer.stop();
                    break;
                case R:
                    timer.start();
                    break;
            }
        });

        increaseSpeed();

        stage.setScene(scene);
        stage.setTitle("Space Invaders");
        stage.show();
    }

    @Override
    public void start(Stage stage) {
        run(stage);
    }
    
}
