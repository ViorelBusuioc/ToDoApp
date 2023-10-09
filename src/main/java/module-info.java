module dev.lpa.todo {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.lpa.todo to javafx.fxml;
    exports dev.lpa.todo;
}