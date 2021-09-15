package com.ankush.view;
import com.ankush.config.SpringFXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class StageManager {
    private static final Logger LOG = getLogger(StageManager.class);
    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    public StageManager(SpringFXMLLoader springFXMLLoader,Stage stage)
    {
        this.springFXMLLoader = springFXMLLoader;
        this.primaryStage = stage;
    }
    public void switchScene(final FxmlView view)
    {
        Parent viewRootNodeHierarchy = loadViewNodeHierarchy(view.getFxmlFile());
        show(viewRootNodeHierarchy,view.getTitle());
    }
    public void showFullScreen()
    {
        primaryStage.setMaximized(true);

    }
    private void show(Parent rootnode, String title) {
        Scene scene = prepareScene(rootnode);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();

        try {
            primaryStage.show();
        } catch (Exception exception) {
            logAndExit ("Unable to show scene for title" + title,  exception);
        }
    }

    private Scene prepareScene(Parent rootnode) {
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(rootnode);
        }
        scene.setRoot(rootnode);
        return scene;
    }

    private Parent loadViewNodeHierarchy(String fxmlFile) {
        Parent rootNode=null;
        try{
            rootNode = springFXMLLoader.load(fxmlFile);
            Objects.requireNonNull(rootNode,"A Root FXML Node must not be null");
        }catch(Exception e)
        {
            logAndExit("unable to load fxml view: "+fxmlFile,e);
        }
        return rootNode;
    }

    private void logAndExit(String errorMsg, Exception e) {
        LOG.error(errorMsg, e, e.getCause());
    }

}
