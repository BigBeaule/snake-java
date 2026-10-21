module com.bigb.snake {
    // Specify modules the application relies on.
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
    requires transitive javafx.graphics;

    // Specify modules needed at compile time.
    requires static lombok;

    // Open packages to JavaFX reflection.
    opens com.bigb.snake to javafx.fxml;

    // Export internal packages having the main class launcher and classes used in public APIs.
    exports com.bigb.snake;
    exports com.bigb.snake.domain;
}
